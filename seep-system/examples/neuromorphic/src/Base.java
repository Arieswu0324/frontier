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
import uk.ac.imperial.lsds.seep.aries.operators.CornerDetector;
import uk.ac.imperial.lsds.seep.aries.operators.Sink;
import uk.ac.imperial.lsds.seep.aries.operators.Source;
import uk.ac.imperial.lsds.seep.operator.Connectable;

import java.util.ArrayList;

public class Base implements QueryComposer {

    private final static Logger logger = LoggerFactory.getLogger(Base.class);
    private int REPLICATION_FACTOR;


    @Override
    public QueryPlan compose() {
        REPLICATION_FACTOR= 1;  //initialization to be integrated into config file

        //Declare source
        ArrayList<String> srcFields= new ArrayList<>();
        srcFields.add("batchId");
        srcFields.add("eventList");
        Connectable src = QueryBuilder.newStatelessSource(new Source(),-1,srcFields);

        //Declare CornerEventDetector
        ArrayList<String> cornerDetectorFields = new ArrayList<>();
        cornerDetectorFields.add("batchId");
        cornerDetectorFields.add("eventList");

        Connectable cornerDetector = QueryBuilder.newStatelessOperator(new CornerDetector(), 0, cornerDetectorFields);

        //Declare Sink
        ArrayList<String> snkFields = new ArrayList<>();
        snkFields.add("batchId");
        snkFields.add("eventList");

        Connectable snk = QueryBuilder.newStatelessSink(new Sink(), -2, snkFields);

        //Connect into topology
        src.connectTo(cornerDetector, true,0);  //what does streamId refer to? originalQuery?
        cornerDetector.connectTo(snk,true,1);

        //scale-out intermediate operator
        if(REPLICATION_FACTOR>1){
            QueryBuilder.scaleOut(cornerDetector.getOperatorId(),REPLICATION_FACTOR);
            logger.info("CornerDetector Operator: "+cornerDetector.getOperatorId()+
                    " scaleOut at "+REPLICATION_FACTOR+" replicas");
        }


        return QueryBuilder.build();
    }
}
