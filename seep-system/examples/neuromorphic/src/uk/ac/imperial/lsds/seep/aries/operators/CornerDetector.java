package uk.ac.imperial.lsds.seep.aries.operators;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

import java.io.FileNotFoundException;
import java.util.List;

/*
 * @Author:cw415@imperial.ac.uk
 * @Description: Operator for Corner Event Detector, stateless, optimised
 * @Version:1.0
 * @Date:2020/7/1
 * @Modified by:
 */

public class CornerDetector implements StatelessOperator {
    @Override
    public void setUp() throws FileNotFoundException {

    }

    @Override
    public void processData(DataTuple data) {

    }

    @Override
    public void processData(List<DataTuple> dataList) {

    }
}
