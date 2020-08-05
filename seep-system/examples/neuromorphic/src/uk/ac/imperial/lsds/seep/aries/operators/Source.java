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


    private final String testDir="resources/shapes_dataset/5000_events-warmUps.txt";
    String eventLine=null;

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

            eventLine=fr.readLine();
            while(eventLine!=null){ //condition: EOF

                String[] strings = eventLine.split("\\s+");

                Event event = new Event(strings[0],strings[1],strings[2],strings[3]);
                eventCount++;
                eventList.add(event);
		
		        eventLine=fr.readLine();

                if(eventCount%1000==0 || eventLine == null) {  //batch size 50 for experiment
                    DataTuple output = data.newTuple(tupleId, eventList);


                    api.send_highestWeight(output);
                    //api.send(output);
                    Thread.sleep(1000); //time control
                    System.out.println("Source sending tuple: Id---"+tupleId+"---"+eventList.size()+"events, " +
                            "starts with timestamp: "+eventList.get(0).timestamp);
                    tupleId++;

                    //empty eventList for next batch
                    eventCount=0;
                    eventList.clear();

                }

            }
            fr.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        finally{
            //System.exit(0);
        }

    }

    @Override
    public void processData(List<DataTuple> dataList) {

    }
}
