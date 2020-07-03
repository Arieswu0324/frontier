package uk.ac.imperial.lsds.seep.aries.operators;
/*
 * @Author:cw415@imperial.ac.uk
 * @Description: Base class for Corner Event Detection Query
 * @Version:1.0
 * @Date:2020/6/19
 * @Modified by:
 */

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

import java.util.List;

public class Sink implements StatelessOperator {
    @Override
    public void setUp() {

    }

    @Override
    public void processData(DataTuple data) {


    }

    @Override
    public void processData(List<DataTuple> dataList) {

    }
}
