package seep.operator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import seep.comm.routing.Router;
import seep.comm.serialization.DataTuple;
import seep.infrastructure.NodeManager;
import seep.processingunit.StatefulProcessingUnit;

public abstract class Operator implements Serializable, QuerySpecificationI, EndPoint{
	
	private static final long serialVersionUID = 1L;
	
	private int operatorId;
	private OperatorContext opContext = new OperatorContext();
	private State state = null;
	private boolean ready = false;
	public Operator subclassOperator = null;
	public StatefulProcessingUnit processingUnit = null;
	private Router router = null;
	// By default value
	private DataAbstractionMode dataAbs = DataAbstractionMode.ONE_AT_A_TIME;
	
	public enum DataAbstractionMode{
		ONE_AT_A_TIME, WINDOW, ORDERED, UPSTREAM_SYNC_BARRIER
	}
	
	public Operator(int operatorId, State state){
		this.operatorId = operatorId;
		this.state = state;
		subclassOperator = this;
	}
	
	public State getState(){
		return state;
	}
	
	public void setState(State state){
		this.state = state;
	}
	
	public void setReady(boolean ready){
		this.ready = ready;
	}
	
	public boolean getReady(){
		return ready;
	}
	
	public Router getRouter(){
		return router;
	}
	
	public void setRouter(Router router){
		this.router = router;
	}
	
	public Operator getSubclassOperator() {
		return subclassOperator;
	}
	
	public void setProcessingUnit(StatefulProcessingUnit processingUnit){
		this.processingUnit = processingUnit;
	}
	
	/** Mandatory methods to implement by developers **/
	
	public abstract void setUp();

	//public abstract void processData(DataTuple dt);
	public abstract void processData(DataTuple dt);
	
	public abstract void processData(ArrayList<DataTuple> ldt);
	
	/** Methods used by the developers to send data **/
	
	public void sendDownToIndex(DataTuple dt, int idx){
		ArrayList<Integer> targets = new ArrayList<Integer>();
		targets.add(idx);
		processingUnit.sendData(dt, targets);
	}
	
	public void sendDown(DataTuple dt){
		/// \todo{FIX THIS, look for a value that cannot be present in the tuples...}
		// We check the targets with our routers
		ArrayList<Integer> targets = router.forward(dt, Integer.MIN_VALUE, false);
		processingUnit.sendData(dt, targets);
	}
	
	public void sendDownRouteByValue(DataTuple dt, int value){
		// We check the targets with our routers
		ArrayList<Integer> targets = router.forward(dt, value, false);
		processingUnit.sendData(dt, targets);
	}
	
	public void sendDownAll(DataTuple dt){
		// When routing to all, targets are all the logical downstreamoperators
		ArrayList<Integer> targets = router.forwardToAllDownstream(dt);
		processingUnit.sendData(dt, targets);
	}
	
	/** System Configuration Settings **/
	
	public void disableCheckpointing(){
		// Disable checkpointing the state for operator with operatorId
		processingUnit.disableCheckpointForOperator(operatorId);
	}
	
	/** Data Delivery methods **/
	
	public DataAbstractionMode getDataAbstractionMode(){
		return dataAbs;
	}
	
	public void setDataAbstractionMode(DataAbstractionMode mode){
		this.dataAbs = mode;
	}
	
	/** Implementation of QuerySpecificationI **/
	
	public int getOperatorId(){
		return operatorId;
	}
	
	public OperatorContext getOpContext(){
		return opContext;
	}
	
	public void setOpContext(OperatorContext opContext){
		this.opContext = opContext;
	}
	
	public void setOriginalDownstream(ArrayList<Integer> originalDownstream){
		this.opContext.setOriginalDownstream(originalDownstream);
	}
	
	public void connectTo(QuerySpecificationI down, boolean originalQuery) {
		opContext.addDownstream(down.getOperatorId());
		if(originalQuery)opContext.addOriginalDownstream(down.getOperatorId());
		down.getOpContext().addUpstream(getOperatorId());
//		NodeManager.nLogger.info("Operator: "+this.toString()+" is now connected to Operator: "+down.toString());
	}
	
//	public void setRoutingQueryFunction(String queryFunction_methodName){
////		router.setQueryFunction(queryFunction_methodName);
//		opContext.setQueryFunction(queryFunction_methodName);
//		NodeManager.nLogger.info("Configured Routing Query Function: "+queryFunction_methodName+" in Operator: "+this.toString());
//	}
//	
//	public void route(Router.RelationalOperator operand, int value, Operator toConnect){
//		int opId = toConnect.getOperatorId();
//		//router.routeValueToDownstream(operand, value, opId);
//		opContext.routeValueToDownstream(operand, value, opId);
//		NodeManager.nLogger.info("Operator: "+this.toString()+" sends data with value: "+value+" to Operator: "+toConnect.toString());
//	}
	
	public void route(String attributToQuery, Router.RelationalOperator operand, int valueToMatch, Operator toConnect){
		int opId = toConnect.getOperatorId();
		opContext.setQueryAttribute(attributToQuery);
		opContext.routeValueToDownstream(operand, valueToMatch, opId);
		NodeManager.nLogger.info("Operator: "+this.toString()+" sends data with value: "+valueToMatch+" to Operator: "+toConnect.toString());
	}
	
	public void _declareWorkingAttributes(List<String> attributes){
		opContext.setDeclaredWorkingAttributes(attributes);
	}
	
//	@Deprecated
//	public void declareWorkingAttributes(String... attributes){
//		opContext.setDeclaredWorkingAttributes(Arrays.asList(attributes));
//	}
	
	/** HELPER METHODS **/
	
	@Override 
	public String toString() {
		return "Operator [operatorId="+operatorId+", opContext= "+opContext+"]";
	}
}