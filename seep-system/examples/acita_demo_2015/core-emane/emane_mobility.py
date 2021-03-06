import sys,time,traceback
from core.pycore import Session 
from core.mobility import Ns2ScriptedMobility 
from emanesh.events import EventService, LocationEvent

def publish_loc(nem, x, y, z, session, verbose=False):
    loc = LocationEvent() 
    lat,lon,alt = session.location.getgeo(x, y, z)
    rtx, rty, rtz = session.location.getxyz(lat, lon, alt)
    if verbose: 
        session.info('Publishing location event for nem %d: (%.1f %.1f %.1f) -> (%.6f %.6f %.6f) (diff rt %d %d %d)' \
            %(nem,x,y,z,lat,lon,alt, x-rtx,y-rty,z-rtz))

    if int(x-rtx) > 0 or int(y-rty) > 0 or int(z - rtz) > 0: raise Exception("Error converting between coordinate systems!")
    loc.append(nem, latitude=lat,longitude=lon, altitude=alt)

    session.emane.service.publish(0, loc)

def register_emane_ns2_model(session):
    m = EmaneNs2ScriptedMobility
    session.addconfobj(m._name, m._type, m.configure_mob)
    session.mobility._modelclsmap[m._name] = m

class EmaneNs2ScriptedMobility(Ns2ScriptedMobility):
    ''' Handles the ns-2 script format, generated by scengen/setdest or
        BonnMotion, but converts all position updates into Emane location
        events.
    '''
    _name = "emaneNs2script"
    nem_offset = 2
    remote_nodes = {} 

    def __init__(self, session, objid, verbose = False, values = None):
        ''' 
        '''
        super(EmaneNs2ScriptedMobility, self).__init__(session = session, objid = objid,
                                                  verbose = verbose, values = values)

    def setnodeposition(self, node, x, y, z):
        ''' Helper to move a node, notify any GUI (connected session handlers),
            without invoking the interface poshook callback that may perform
            range calculation.
        '''
        # this would cause PyCoreNetIf.poshook() callback (range calculation)
        #node.setposition(x, y, z)

        #Slaves won't call this on their node, but might not matter if all
        #SEEP nodes are on the master?
        if self.verbose: 
            (oldx,oldy,oldz) = node.position.get()
            #traceback.print_stack()
            self.session.info('%s setting node position for n%d: (%s %s %s) -> (%d %d %d)' \
                %(str(self.__class__.__name__), node.objid, str(oldx), str(oldy), str(oldz), x, y, z))

        node.position.set(x, y, z)  
        #msg = node.tonodemsg(flags=0)
        #TODO: Record of remote node positions might be needed if SEEP nodes on slaves
        if not node.objid in self.remote_nodes: self.writenodeposition(node, x, y, z)
        publish_loc(node.objid-self.nem_offset, x, y, z, self.session, verbose=self.verbose)
        #self.session.broadcastraw(None, msg)
        #self.session.sdt.updatenode(node.objid, flags=0, x=x, y=y, z=z)

    def set_remote_nodes(self, remote_nodes):
        self.remote_nodes = dict([(n.objid, n) for n in remote_nodes])
        if self.verbose: 
            self.session.info("Set %s remote nodes: %s" \
                %(str(self.__class__.__name__), str(self.remote_nodes.keys())))

    def runround(self):
        ''' Advance script time and move nodes.
        '''
        if self.verbose: self.session.info("Using runround from %s"%(str(self.__class__.__name__)))
        if self.state != self.STATE_RUNNING:
            return        
        t = self.lasttime
        self.lasttime = time.time()
        now = self.lasttime - self.timezero
        dt = self.lasttime - t
        #print "runround(now=%.2f, dt=%.2f)" % (now, dt)
        
        # keep current waypoints up-to-date
        self.updatepoints(now)
        
        if not len(self.points):
            if len(self.queue):
                # more future waypoints, allow time for self.lasttime update
                nexttime = self.queue[0].time - now
                if nexttime > (0.001 * self.refresh_ms):
                    nexttime -= (0.001 * self.refresh_ms)
                self.session.evq.add_event(nexttime, self.runround)
                return
            else:
                # no more waypoints or queued items, loop?
                if not self.empty_queue_stop:
                    # keep running every refresh_ms, even with empty queue
                    self.session.evq.add_event(0.001 * self.refresh_ms, self.runround)
                    return
                if not self.loopwaypoints():
                    #return self.stop(move_initial=False)
                    raise Exception("Mobility script expired (1)!")
                if not len(self.queue):
                    # prevent busy loop
                    #return
                    raise Exception("Mobility script expired (2)!")
                return self.run()
        
        # only move netifs attached to self.wlan, or all nodenum in script?
        moved = []
        moved_netifs = []
        for netif in self.wlan.netifs():
            node = netif.node
            if self.movenode(node, dt):
                moved.append(node)
                moved_netifs.append(netif)

        for nodeid in self.remote_nodes:
            node = self.remote_nodes[nodeid] 
            self.movenode(node, dt)
        
        # calculate all ranges after moving nodes; this saves calculations
        #self.wlan.model.update(moved)
        self.session.mobility.updatewlans(moved, moved_netifs)
        
        # TODO: check session state
        self.session.evq.add_event(0.001 * self.refresh_ms, self.runround)

    def movenodesinitial(self):
        ''' Move nodes to their initial positions. Then calculate the ranges.
        '''
        if self.verbose: self.session.info("Using movenodesinitial from %s"%(str(self.__class__.__name__)))
        moved = []
        moved_netifs = []
        self.session.info("Mobility: Setting initial positions for nodes %s"%(str(self.initial.keys())))
        for netif in self.wlan.netifs():
            self.session.info("Mobility: Initial move of node %d"%netif.node.objid)
            node = netif.node
            if node.objid not in self.initial:
                continue
            (x, y, z) = self.initial[node.objid].coords
            self.setnodeposition(node, x, y, z)
            moved.append(node)
            moved_netifs.append(netif)

        for nodeid in self.remote_nodes:
            self.session.info("Mobility: Initial move of remote node %d"%nodeid)
            node = self.remote_nodes[nodeid]
            if node.objid not in self.initial:
                continue
            (x, y, z) = self.initial[node.objid].coords
            self.setnodeposition(node, x, y, z)

        self.session.mobility.updatewlans(moved, moved_netifs)

class EmaneNs2Session(Session):

    def __init__(self, sessionid = None, cfg = {}, server = None, persistent = False, mkdir = True):
        super(EmaneNs2Session, self).__init__(sessionid = sessionid, cfg=cfg, server=server, persistent=persistent, mkdir=mkdir)

    def instantiate(self, wlan, remote_nodes, handler=None):
        ''' We have entered the instantiation state, invoke startup methods
            of various managers and boot the nodes. Validate nodes and check
            for transition to the runtime state.
        '''
        
        self.writeobjs()
        # controlnet may be needed by some EMANE models
        self.addremovectrlif(node=None, remove=False)
        if self.emane.startup() == self.emane.NOT_READY:
            return # instantiate() will be invoked again upon Emane.configure()
        self.broker.startup()
        self.mobility.startup()

        wlan.mobility.set_remote_nodes(remote_nodes)
 
        # boot the services on each node
        self.bootnodes(handler)
        # allow time for processes to start
        time.sleep(0.125)
        self.validatenodes()
        # assume either all nodes have booted already, or there are some
        # nodes on slave servers that will be booted and those servers will
        # send a node status response message
        self.checkruntime()
