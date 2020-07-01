package uk.ac.imperial.lsds.seep.aries.operators;
/*
 * @Author:cw415@imperial.ac.uk
 * @Description: Base class for Corner Event Detection Query
 * @Version:1.0
 * @Date:2020/6/19
 * @Modified by: Aries: Deprecated for optimised query design
 */

import uk.ac.imperial.lsds.seep.aries.operators.state.SaeState;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

import java.util.List;

public class CornerEventDetector implements StatefulOperator {
    int sceneLength = 240;
    int sceneWith = 180;
    String sensorId;// to be integrated into config file

    @Override
    public StateWrapper getState() {
        SaeState sae = new SaeState(sceneLength,sceneWith);
        sae.setKeyAttribute(sensorId);//retrieve SaeSate through keyAttributes
        StateWrapper saeStateWrapper = new StateWrapper(api.getOperatorId(),1,sae);
        //where to config checkpointInterval;
        return saeStateWrapper;
    }

    @Override
    public void replaceState(StateWrapper state) {
        //？？

    }

    @Override
    public void setUp() {

    }

    @Override
    public void processData(DataTuple data) {
        //arc* algorithm

    }

    @Override
    public void processData(List<DataTuple> dataList) {

    }
}
