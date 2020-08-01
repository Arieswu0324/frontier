package uk.ac.imperial.lsds.seep.event;

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

    public Event(){}

    public Event(String timestamp, String x, String y, String polarity){
        this.timestamp = Double.valueOf(timestamp);
        this.x = Integer.valueOf(x);
        this.y=Integer.valueOf(y);
        this.polarity=Integer.valueOf(polarity);
        this.label="false";
    }

    public Event(double timestamp, int x, int y, int polarity){
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.polarity = polarity;
        this.label="false";
    }


    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public int getPolarity() {
        return polarity;
    }

    public void setPolarity(int polarity) {
        this.polarity = polarity;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "Event{" +
                "x=" + x +
                ", y=" + y +
                ", timestamp=" + timestamp +
                ", polarity=" + polarity +
                ", label='" + label + '\'' +
                '}';
    }
}
