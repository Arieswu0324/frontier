package uk.ac.imperial.lsds.seep.aries.operators.state;
import com.sun.org.apache.bcel.internal.generic.RET;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.doublematrix.DoubleMatrix;
import uk.ac.imperial.lsds.seep.state.CustomState;
import uk.ac.imperial.lsds.seep.state.Partitionable;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

import static org.ujmp.core.calculation.Calculation.Ret.NEW;

/*
 * @Author:cw415@imperial.ac.uk
 * @Description: Class used for state store
 * @Version:1.0
 * @Date:2020/6/19
 * @Modified by:
 */

public class SaeState implements CustomState, Partitionable {
    public int sceneWidth;  //rows of pixels
    public int sceneLength; //columns of pixels
    public String sensorId = "sensor01";

    public DoubleMatrix saeMatrix;

    public int getSceneWidth() {
        return sceneWidth;
    }

    public void setSceneWidth(int sceneWidth) {
        this.sceneWidth = sceneWidth;
    }

    public int getSceneLength() {
        return sceneLength;
    }

    public void setSceneLength(int sceneLength) {
        this.sceneLength = sceneLength;
    }

    //Constructor
    public SaeState(){}

    public SaeState(int sceneWidth, int sceneLength){
        saeMatrix = (DoubleMatrix) DoubleMatrix.Factory.zeros(sceneWidth,sceneLength);
    }

    //get current value at position(x,y)
    public double getTimestamp(int x, int y){
        return saeMatrix.getAsDouble(x,y);
    }

    //set timestamp at position(x,y)
    public void setTimestamp( int x, int y, double ts){
        saeMatrix.setAsDouble(ts,x,y);
    }

    @Override
    public void setKeyAttribute(String keyAttribute) {
        this.sensorId = keyAttribute;

    }

    @Override
    public String getKeyAttribute() {
        return this.sensorId;
    }

    @Override
    public StateWrapper[] splitState(StateWrapper toSplit, int key) {
        return new StateWrapper[0];
    }

    @Override
    public void resetState() {

        Calculation.Ret ret = NEW;
        saeMatrix.zeros(ret);
    }
}
