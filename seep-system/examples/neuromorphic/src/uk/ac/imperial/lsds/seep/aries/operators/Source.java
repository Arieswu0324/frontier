package uk.ac.imperial.lsds.seep.aries.operators;

/*
 * @Author:cw415@imperial.ac.uk
 * @Description: Class for Source Ope
 * @Version:1.0
 * @Date:2020/6/19
 * @Modified by:
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.event.Event;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Source implements StatelessOperator {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(Source.class);


    private final String testDir="resources/shapes_dataset/test.txt";
    //private final String timeDir = "resources/shapes_dataset/images.txt";
    String eventLine=null;
    String timeLine =null;

    @Override
    public void setUp(){

        System.out.println("Setting up EVENT_SOURCE operator with id="+api.getOperatorId());
        logger.info("EVENT_SOURCE setup complete.");
    }

    @Override
    public void processData(DataTuple dt) {
        long tupleId = 0;
        int eventCount = 0;
        ArrayList<Event> eventList = new ArrayList<>();

            Map<String, Integer> mapper = api.getDataMapper();
            DataTuple data = new DataTuple(mapper, new TuplePayload());

        try{
            BufferedReader fr = new BufferedReader(new FileReader(testDir));    //read data through IO streams
            //BufferedReader fr2 = new BufferedReader(new FileReader(timeDir)); //for time-based batching

            eventLine=fr.readLine();
            //timeLine=fr2.readLine();
//            String[] image_ts=timeLine.split("\\s+");
//            double ts = Double.parseDouble(image_ts[0]);  //time-based batching

            while(eventLine!=null){ //condition: EOF

                String[] strings = eventLine.split("\\s+");

                Event event = new Event(strings[0],strings[1],strings[2],strings[3]);
                eventCount++;
                eventList.add(event);
		
		        eventLine=fr.readLine();

                if(eventCount%5000==0 || eventLine == null) {//size-based batching

                //if(event.timestamp>=ts||eventLine==null||timeLine==null){ //time-based batching
                    DataTuple output = data.newTuple(tupleId, eventList);


                    api.send_highestWeight(output);
                    //Thread.sleep(1000); //time control
                    System.out.println("Source sending tuple: Id---"+tupleId+"---"+eventList.size()+"events, " +
                            "starts with timestamp: "+eventList.get(0).timestamp);
                    tupleId++;

                    //empty eventList for next batch
                    eventCount=0;
                    eventList = new ArrayList<>(); //Avoid reusing the same eventlist array in different events, so there could potentially be concurrent access by a different thread
                    //ConcurrentModificationException

//                    timeLine=fr2.readLine();
//                    image_ts=timeLine.split("\\s+");
//                    ts = Double.parseDouble(image_ts[0]);
//                    System.out.println(ts);   //time-based batching
                }

            }
            fr.close();
            //fr2.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        finally{
            //add time-control before exit
            //System.exit(0);
        }

    }

    @Override
    public void processData(List<DataTuple> dataList) {

    }
}
