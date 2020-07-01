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
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Source implements StatelessOperator {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(Source.class);

    private final String testDir="resources/shapes_dataset/5000_events.txt";
    String eventLine=null;

    @Override
    public void setUp(){

        System.out.println("Setting up EVENT_SOURCE operator with id="+api.getOperatorId());
        logger.info("EVENT_SOURCE setup complete.");
    }

    @Override
    public void processData(DataTuple dt) {
        int batchId = 0;
        int eventCount = 0;
        ArrayList<Event> eventList = new ArrayList<>();

            Map<String, Integer> mapper = api.getDataMapper();
            DataTuple data = new DataTuple(mapper, new TuplePayload());

        try{
            BufferedReader fr = new BufferedReader(new FileReader(testDir));    //read data through IO streams

            while((eventLine=fr.readLine())!=null){ //condition: EOF
                eventLine = fr.readLine();
                String[] strings = eventLine.split("\\s+");
//                String timestamp = strings[0];
//                String x = strings[1];
//                String y = strings[2];
//                String polarity = strings[3];
//                String label = "false";
                Event event = new Event(strings[0],strings[1],strings[2],strings[3]);
                eventCount++;
                eventList.add(event);

                if(eventCount%50==0) {  //batch size 50 for experiment, fix me when EOF but not 50
                    DataTuple output = data.newTuple(batchId, eventList);
                    api.send_highestWeight(output);
                    batchId++;

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
            System.exit(0);
        }

    }

    @Override
    public void processData(List<DataTuple> dataList) {

    }
}
