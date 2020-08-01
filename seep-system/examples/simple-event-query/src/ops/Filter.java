package ops;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.event.Event;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/*
 * @Author:cw415@imperial.ac.uk
 * @Description:
 * @Version:1.0
 * @Date:2020/7/21
 * @Modified by:
 */public class Filter implements StatelessOperator {
    private static final long serialVersionUID = 1L;


    @Override
    public void setUp() throws FileNotFoundException {

    }

    @Override
    public void processData(DataTuple data) {
        int batchId = data.getInt("batchId");
        ArrayList<Event> events = (ArrayList<Event>) data.getEvents("eventList");

        for(Event e: events){
            e.timestamp+=1;
        }
        DataTuple output = data.newTuple(batchId,events);

        api.send(output);

    }

    @Override
    public void processData(List<DataTuple> dataList) {

    }
}
