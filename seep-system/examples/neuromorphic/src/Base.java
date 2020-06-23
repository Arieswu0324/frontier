/*
 * @Author:cw415@imperial.ac.uk
 * @Description: Base class for Corner Event Detection Query
 * @Version:1.0
 * @Date:2020/6/19
 * @Modified by:
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.imperial.lsds.seep.api.QueryBuilder;
import uk.ac.imperial.lsds.seep.api.QueryComposer;
import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.aries.operators.CornerEventDetector;
import uk.ac.imperial.lsds.seep.aries.operators.Sink;
import uk.ac.imperial.lsds.seep.aries.operators.Source;
import uk.ac.imperial.lsds.seep.aries.operators.state.SaeState;
import uk.ac.imperial.lsds.seep.operator.Connectable;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

import java.util.ArrayList;

public class Base implements QueryComposer {

    private final static Logger logger = LoggerFactory.getLogger(Base.class);
    private int REPLICATION_FACTOR;


    @Override
    public QueryPlan compose() {
        REPLICATION_FACTOR= 1;  //initialization to be integrated into config file

        String sensorId = "sensorId";   //should be put into source.java


        //Declare source
        ArrayList<String> srcFields= new ArrayList<>();
        srcFields.add("sensorId");
        srcFields.add("tupleId");
        srcFields.add("timestamp");
        srcFields.add("x");
        srcFields.add("y");
        srcFields.add("polarity");
        srcFields.add("label");
        Connectable src = QueryBuilder.newStatelessSource(new Source(),-1,srcFields);

        //Declare CornerEventDetector, Stateful
        ArrayList<String> cornerDetectorFields = new ArrayList<>();
        cornerDetectorFields.add("sensorId");
        cornerDetectorFields.add("tupleId");
        cornerDetectorFields.add("timestamp");
        cornerDetectorFields.add("x");
        cornerDetectorFields.add("y");
        cornerDetectorFields.add("polarity");
        cornerDetectorFields.add("label");

        SaeState state = new SaeState();// modfity later, CustomState
        StateWrapper saeStateWrapper = QueryBuilder.newCustomState(state,0,1,sensorId);
        //what does the checkpointInterval refer to? is the keyAttribute the same as the stateTag in StateWrapper Class.
        Connectable cornerDetector = QueryBuilder.newStatefulOperator(new CornerEventDetector(), 0, saeStateWrapper, cornerDetectorFields);

        //Declare Sink
        ArrayList<String> snkFields = new ArrayList<>();
        snkFields.add("sensorId");
        snkFields.add("tupleId");
        snkFields.add("timestamp");
        snkFields.add("x");
        snkFields.add("y");
        snkFields.add("polarity");
        snkFields.add("label");
        Connectable snk = QueryBuilder.newStatelessSink(new Sink(), -2, snkFields);

        //Connect into topology
        src.connectTo(cornerDetector, true,0);  //what does streamId refer to? originalQuery?
        cornerDetector.connectTo(snk,true,1);

        if(REPLICATION_FACTOR>1){
            QueryBuilder.scaleOut(cornerDetector.getOperatorId(),REPLICATION_FACTOR);
            logger.info("CornerDetector Operator: "+cornerDetector.getOperatorId()+
                    " scaleOut at "+REPLICATION_FACTOR+" replicas");
        }


        return QueryBuilder.build();
    }
}
