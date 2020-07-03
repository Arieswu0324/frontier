package uk.ac.imperial.lsds.seep.aries.operators;

import java.io.Serializable;

/*
 * @Author:cw415@imperial.ac.uk
 * @Description: Class for events
 * @Version:1.0
 * @Date:2020/7/1
 * @Modified by:
 */

public class Event implements Serializable {//fix me : cast final, cannot be changed except for label
    private static final long serialVersionUID = 1L;

    public int x;
    public int y;
    public double timestamp;
    public int polarity;
    public String label;

    Event(String timestamp, String x, String y, String polarity){
        this.timestamp = Double.valueOf(timestamp);
        this.x = Integer.valueOf(x);
        this.y=Integer.valueOf(y);
        this.polarity=Integer.valueOf(polarity);
        this.label="false";
    }
}
