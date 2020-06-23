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
import java.util.List;
import java.util.Map;

public class Source implements StatelessOperator {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(Source.class);

    private final String testDir="resources/shapes_dataset/5000_events.txt";
    String sensorId = "sensor01";   // put into config file later
    String eventLine=null;

    @Override
    public void setUp(){

        System.out.println("Setting up EVENT_SOURCE operator with id="+api.getOperatorId());
        logger.info("EVENT_SOURCE setup complete.");
    }

    @Override
    public void processData(DataTuple dt) {

            Map<String, Integer> mapper = api.getDataMapper();
            DataTuple data = new DataTuple(mapper, new TuplePayload());
            long tupleId = 0;
        try{
            BufferedReader fr = new BufferedReader(new FileReader(testDir));
            while((eventLine=fr.readLine())!=null){ //condition: EOF
                eventLine = fr.readLine();
                String[] strings = eventLine.split("\\s+");
                String timestamp = strings[0];
                String x = strings[1];
                String y = strings[2];
                String polarity = strings[3];
                String label = "false";

                DataTuple output = data.newTuple(sensorId,tupleId,timestamp,x,y,polarity,label);
                output.getPayload().timestamp = System.currentTimeMillis();

                api.send_highestWeight(output);
                logger.info("Source sending event tuple from "+sensorId+", tupleId= "+tupleId+": " +
                        "timestamp="+timestamp+", x="+x+", y="+y+", polarity="+polarity);
                tupleId++;
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
