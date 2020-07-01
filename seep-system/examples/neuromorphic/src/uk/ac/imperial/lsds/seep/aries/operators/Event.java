package uk.ac.imperial.lsds.seep.aries.operators;

import java.io.Serializable;

/*
 * @Author:cw415@imperial.ac.uk
 * @Description: Class for events
 * @Version:1.0
 * @Date:2020/7/1
 * @Modified by:
 */

public class Event implements Serializable {
    private static final long serialVersionUID = 1L;

    public String x;
    public String y;
    public String timestamp;
    public String polarity;
    public String label;

    Event(){}
    Event(String timestamp, String x, String y, String polarity){
        this.timestamp = timestamp;
        this.x = x;
        this.y=y;
        this.polarity=polarity;
        this.label="false";
    }
}
