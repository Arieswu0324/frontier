package uk.ac.imperial.lsds.seep.aries.operators;

/*
 * @Author:cw415@imperial.ac.uk
 * @Description: Matrix recording Surface of Active Events
 * @Version:1.0
 * @Date:2020/7/3
 * @Modified by:
 */

import org.ujmp.core.doublematrix.DoubleMatrix;
import uk.ac.imperial.lsds.seep.api.largestateimpls.Matrix;

public class SAE extends Matrix {
    int rows;
    int cols;
    DoubleMatrix sae;

    public SAE(int rows, int cols){
        this.sae= (DoubleMatrix) DoubleMatrix.Factory.zeros(rows, cols);
    }

    public double getTimestamp(int x, int y){
        return sae.getAsDouble(x,y);
    }

    public void setTimestamp(double timestamp, int x, int y){
        sae.setAsDouble(timestamp,x,y);
    }

}
