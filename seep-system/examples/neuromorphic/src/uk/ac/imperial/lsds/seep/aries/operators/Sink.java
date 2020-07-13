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
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

import java.util.List;

public class Sink implements StatelessOperator {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(Sink.class);

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
