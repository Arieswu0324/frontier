package uk.ac.imperial.lsds.seep.aries.operators;

import org.ujmp.core.doublematrix.DoubleMatrix;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/*
 * @Author:cw415@imperial.ac.uk
 * @Description: Operator for Corner Event Detector, stateless, optimised
 * @Version:1.0
 * @Date:2020/7/1
 * @Modified by:
 */

public class CornerDetector implements StatelessOperator {
    @Override
    public void setUp() throws FileNotFoundException {

    }

    @Override
    public void processData(DataTuple data) {

        int batchId = data.getInt("batchId");
        ArrayList<Event> eventList = (ArrayList<Event>) data.getEvents("eventList");

        ArrayList<Event> newEventList = cornerDetector(eventList);

        DataTuple output = data.newTuple(batchId,newEventList);
        api.send_highestWeight(output);
    }

    private ArrayList<Event> cornerDetector(ArrayList<Event> eventList) {

        SAE[] sae = new SAE[2];
        SAE[] saeLatest = new SAE[2];
        sae[0] = new SAE(240,180);
        sae[1] = new SAE(240,180);
        saeLatest[0]=new SAE(240,180);
        saeLatest[1]=new SAE(240,180);

        for(Event event: eventList){
            int x = event.x;
            int y = event.y;
            int pol = event.polarity==1 ? 1 : 0;
            int pol_inv = event.polarity==0 ? 1:0;

            double tLast = saeLatest[pol].getTimestamp(x,y);
            double tLast_inv=saeLatest[pol_inv].getTimestamp(x,y);

            if((event.timestamp>tLast+0.05)||(tLast_inv>tLast)){//threshold 50ms=0.05s for test
                tLast=event.timestamp;
                saeLatest[pol].setTimestamp(tLast,x,y);
                sae[pol].setTimestamp(event.timestamp,x,y);
            }else{
                tLast=event.timestamp;
                saeLatest[pol].setTimestamp(tLast,x,y);
                continue;
            }

            event.label = String.valueOf(isCorner(event,sae));
        }


            return eventList;
    }

    private boolean isCorner(Event event, SAE[] sae) {

        return false;
    }


    @Override
    public void processData(List<DataTuple> dataList) {

    }
}
