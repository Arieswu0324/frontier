package uk.ac.imperial.lsds.seep.aries.operators;
/*
 * @Author:cw415@imperial.ac.uk
 * @Description: Base class for Corner Event Detection Query
 * @Version:1.0
 * @Date:2020/6/19
 * @Modified by:
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.event.Event;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

import java.util.ArrayList;
import java.util.List;

public class Sink implements StatelessOperator {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(Sink.class);

    private long numTuples;
    private int warmUpTuples;
    private long tStart;
    private long tuplesReceived=0;


    @Override
    public void setUp() {
        logger.info("Sink setup complete");
        numTuples=5; // test
        warmUpTuples=2;
    }

    @Override
    public void processData(DataTuple data) {

        if (tuplesReceived == 0)
        {
            tStart = System.currentTimeMillis();
            logger.info("SNK: Received initial tuple at t="+System.currentTimeMillis());
        }

        if(tuplesReceived<warmUpTuples){
            System.out.println("Warm up tuples received");
        }else {

            int count = 0;

            long tupleId = data.getLong("tupleId");
            ArrayList<Event> eventList = (ArrayList<Event>) data.getEvents("eventList");

            System.out.println("SNK received tuple: ID " + tupleId + "----" + eventList.size() + " events" +
                    "---starts with timestamp " + eventList.get(0).timestamp);

            for (Event e : eventList) {
                if (e.label.equals("true")) {
                    logger.info("TupleId: " + tupleId +
                            "----Event timestamp: " + e.timestamp + " ----corner event? " + e.label);
                    count++;
                }
            }

            logger.info("SNK received: tupleId " + tupleId + "----" + count + " corner events are detected");
        }

        tuplesReceived++;

        if(tuplesReceived>=numTuples+warmUpTuples){
            logger.info("Sink finished with total tuples: "+ tuplesReceived+" ("+warmUpTuples+") warm-up tuples included");
            System.exit(0);
        }

        api.ack(data);
    }

    @Override
    public void processData(List<DataTuple> dataList) {

    }
}
