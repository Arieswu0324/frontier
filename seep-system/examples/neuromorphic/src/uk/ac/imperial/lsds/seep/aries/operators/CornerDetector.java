package uk.ac.imperial.lsds.seep.aries.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(CornerDetector.class);

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

            //filter
            if((event.timestamp>tLast+0.05)||(tLast_inv>tLast)){//threshold 50ms=0.05s for test
                tLast=event.timestamp;
                saeLatest[pol].setTimestamp(tLast,x,y);
                sae[pol].setTimestamp(event.timestamp,x,y);
            }else{
                tLast=event.timestamp;
                saeLatest[pol].setTimestamp(tLast,x,y);
                //event.label="false";    //default
                continue;
            }

            //return if too close to the corner
            final int borderLimit = 4;
            if((x<borderLimit||x>240-borderLimit)||(y<borderLimit||y>180-borderLimit))
                continue;   //event.label = false by default

             //check corner event, arc* algorithm
            event.label = String.valueOf(isCorner(event,sae[event.polarity]));
        }


            return eventList;
    }

    private boolean isCorner(Event event, SAE sae) {
        boolean isCorner = false;
        int x = event.x;
        int y = event.y;

        int[][] kSmallCircle = {{0, 3}, {1, 3}, {2, 2}, {3, 1},
                {3, 0}, {3, -1}, {2, -2}, {1, -3},
                {0, -3}, {-1, -3}, {-2, -2}, {-3, -1},
                {-3, 0}, {-3, 1}, {-2, 2}, {-1, 3}};
        int[][] kLargeCircle = {{0, 4}, {1, 4}, {2, 3}, {3, 2},
                {4, 1}, {4, 0}, {4, -1}, {3, -2},
                {2, -3}, {1, -4}, {0, -4}, {-1, -4},
                {-2, -3}, {-3, -2}, {-4, -1}, {-4, 0},
                {-4, 1}, {-3, 2}, {-2, 3}, {-1, 4}};

        final int kSmallCircleSize = 16;
        final int kLargeCircleSize = 20;
        final int kSmallMinThresh = 3;
        final int kSmallMaxThresh = 6;
        final int kLargeMinThresh = 4;
        final int kLargeMaxThresh = 8;

        double arcTimestamp = sae.getTimestamp(x + kSmallCircle[0][0], y + kSmallCircle[0][1]);
        int cwIndex = 0;
        int ccwIndex = 0;


        for (int i =1; i < kSmallCircleSize; i++) {
            double currentCircleTimestamp = sae.getTimestamp(x + kSmallCircle[i][0], y + kSmallCircle[i][1]);
            if (currentCircleTimestamp > arcTimestamp) {
                arcTimestamp = currentCircleTimestamp;
                cwIndex = i;  //index max value in the circle
            }
        }//arcTimestamp records max value in the circle

        ccwIndex = (cwIndex - 1 + kSmallCircleSize) % kSmallCircleSize;
        cwIndex = (cwIndex + 1) % kSmallCircleSize;
        double ccwTimestamp = sae.getTimestamp(x + kSmallCircle[ccwIndex][0], y + kSmallCircle[ccwIndex][1]);
        double cwTimestamp = sae.getTimestamp(x + kSmallCircle[cwIndex][0], y + kSmallCircle[cwIndex][1]);
        double leftMin = ccwTimestamp;
        double rightMin = cwTimestamp;

        //expand arc
        int iteration = 1;
        for (; iteration < kSmallMinThresh; iteration++) {
            if(cwTimestamp>ccwTimestamp) {
                if (rightMin < arcTimestamp) {
                    arcTimestamp = rightMin;
                }

                cwIndex = (cwIndex + 1) % kSmallCircleSize;
                cwTimestamp = sae.getTimestamp(x + kSmallCircle[cwIndex][0], y + kSmallCircle[cwIndex][1]);
                if (cwTimestamp < rightMin) {
                    rightMin = cwTimestamp;
                }
            }else{
                if(leftMin<arcTimestamp){
                    arcTimestamp=leftMin;
                }
                ccwIndex=(ccwIndex-1+kSmallCircleSize)%kSmallCircleSize;
                ccwTimestamp=sae.getTimestamp(x+kSmallCircle[ccwIndex][0],y+kSmallCircle[ccwIndex][1]);
                if(ccwTimestamp<leftMin){
                    leftMin=ccwTimestamp;
                }
            }
        }

        int newArcSize=kSmallMinThresh;

        for(;iteration<kSmallCircleSize;iteration++){
            if(cwTimestamp>ccwTimestamp){
                if((cwTimestamp>=arcTimestamp)){
                    newArcSize=iteration+1;
                    if(rightMin<arcTimestamp){
                        arcTimestamp=rightMin;
                    }
                }

                cwIndex=(cwIndex+1)%kSmallCircleSize;
                cwTimestamp=sae.getTimestamp(x+kSmallCircle[cwIndex][0],y+kSmallCircle[cwIndex][1]);
                if(cwTimestamp<rightMin){
                    rightMin=cwTimestamp;
                }
            }else {
                if(ccwTimestamp>=arcTimestamp){
                    newArcSize=iteration+1;
                    if(leftMin<arcTimestamp){
                        arcTimestamp=leftMin;
                    }
                }

                ccwIndex=(ccwIndex-1+kSmallCircleSize)%kSmallCircleSize;
                ccwTimestamp=sae.getTimestamp(x+kSmallCircle[ccwIndex][0],y+kSmallCircle[ccwIndex][1]);
                if(ccwTimestamp<leftMin){
                    leftMin=ccwTimestamp;
                }
            }
        }

        if((newArcSize<=kSmallMaxThresh)||((newArcSize>=(kSmallCircleSize-kSmallMaxThresh))&&(newArcSize<=(kSmallCircleSize-kSmallMinThresh))))
        {isCorner=true;}

        //Large Circle exploration
        if(isCorner){
            isCorner=false;
            arcTimestamp=sae.getTimestamp(x+kLargeCircle[0][0],y+kLargeCircle[0][1]);
            cwIndex=0;
            ccwIndex=0;
            for(int i = 1; i<kLargeCircleSize;i++ ){
                double currentCircleTimestamp = sae.getTimestamp(x+kLargeCircle[1][0],y+kLargeCircle[i][1]);
                if(currentCircleTimestamp>arcTimestamp){
                    arcTimestamp=currentCircleTimestamp;
                    cwIndex=i;
                }
            }

            ccwIndex= (cwIndex-1+kLargeCircleSize)%kLargeCircleSize;
            cwIndex=(cwIndex+1)%kLargeCircleSize;
            ccwTimestamp=sae.getTimestamp(x+kLargeCircle[ccwIndex][0],y+kLargeCircle[ccwIndex][1]);
            cwTimestamp=sae.getTimestamp(x+kLargeCircle[cwIndex][0],y+kLargeCircle[cwIndex][1]);
            leftMin=ccwTimestamp;
            rightMin=ccwTimestamp;

            iteration = 1;
            for(;iteration<kLargeMinThresh;iteration++){
                if(cwTimestamp>ccwTimestamp){
                    if(rightMin<arcTimestamp){
                        arcTimestamp=rightMin;
                    }

                    cwIndex=(cwIndex+1)%kLargeCircleSize;
                    cwTimestamp=sae.getTimestamp(x+kLargeCircle[cwIndex][0],y+kLargeCircle[cwIndex][1]);

                    if(cwTimestamp<rightMin){
                        rightMin=cwTimestamp;
                    }

                }else {
                    if (leftMin < arcTimestamp) {
                        arcTimestamp = leftMin;
                    }

                    ccwIndex = (ccwIndex - 1 + kLargeCircleSize) % kLargeCircleSize;
                    ccwTimestamp = sae.getTimestamp(x + kLargeCircle[ccwIndex][0], y + kLargeCircle[ccwIndex][1]);

                    if (ccwTimestamp < leftMin) {
                        leftMin = ccwTimestamp;
                    }

                }

            }

            newArcSize=kLargeMinThresh;
            for(;iteration<kLargeCircleSize;iteration++){
                if(cwTimestamp>ccwTimestamp){
                    if(cwTimestamp>=arcTimestamp){
                        newArcSize=iteration+1;
                        if(rightMin<arcTimestamp){
                            arcTimestamp=rightMin;
                        }
                    }

                    cwIndex=(ccwIndex+1)%kLargeCircleSize;
                    cwTimestamp=sae.getTimestamp(x+kLargeCircle[cwIndex][0],kLargeCircle[cwIndex][1]);
                    if(cwTimestamp<rightMin){
                        rightMin=cwTimestamp;
                    }

                }else{
                    if(ccwTimestamp>=arcTimestamp){
                        newArcSize=iteration+1;
                        if(leftMin<arcTimestamp){
                            arcTimestamp=leftMin;
                        }
                    }

                    ccwIndex=(ccwIndex-1+kLargeCircleSize)%kLargeCircleSize;
                    ccwTimestamp=sae.getTimestamp(x+kLargeCircle[ccwIndex][0],y+kLargeCircle[ccwIndex][1]);
                    if(ccwTimestamp<leftMin){
                        leftMin=ccwTimestamp;
                    }
                }

            }

            if((newArcSize<=kLargeMaxThresh)||(newArcSize>=(kLargeCircleSize-kLargeMaxThresh)&&(newArcSize<=(kLargeCircleSize-kLargeMinThresh)))){
                isCorner=true;
            }
        }

        return isCorner;
    }


    @Override
    public void processData(List<DataTuple> dataList) {

    }
}
