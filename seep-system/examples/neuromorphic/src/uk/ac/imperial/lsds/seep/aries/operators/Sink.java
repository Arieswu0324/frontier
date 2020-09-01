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
import uk.ac.imperial.lsds.seep.event.Event;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Sink implements StatelessOperator {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(Sink.class);

    private long numTuples;

    private long tStart;
    private long tuplesReceived=0;
    int total_detection = 0;


    @Override
    public void setUp() {
        logger.info("Sink setup complete");
        numTuples=5; // change when testing
    }

    @Override
    public void processData(DataTuple data) {

        if (tuplesReceived == 0)
        {
            tStart = System.currentTimeMillis();
            logger.info("SNK: Received initial tuple at t="+System.currentTimeMillis());
        }


            int count = 0;

            long tupleId = data.getLong("tupleId");
            ArrayList<Event> eventList = (ArrayList<Event>) data.getEvents("eventList");
            for(Event e:eventList){
                if(e.label.equals("true")){
                    count++;
                }
            }
            total_detection+=count;



           System.out.println("SNK received tuple: ID " + tupleId + "----" + eventList.size() + " events" +
                    "---starts with timestamp " + eventList.get(0).timestamp);

      //read png file as the benchmark

        String filename = "resources/output/query_cornered.png";   //test
        String dest_name = "resources/output/query_cornered.png";   //save as the same file to accumulate results over tuples

        //read file according to tuple id, for time-based batching

        //String str = "00000000"+tupleId;
        //int length = str.toCharArray().length;
        //String sub_name = str.substring(length-8,length);

        //String filename = "resources/shapes_dataset/images/frame_"+sub_name+".png";
        //String dest_name = "resources/output/cornered_"+tupleId+".png";


        BufferedImage src = null;
        try {
            src = ImageIO.read(new File(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int width = src.getWidth();
        int height = src.getHeight();

            //transform png from 8bit grayscale image to 32bit color image
        BufferedImage background = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        for(int i=0; i<width;i++){
            for (int j = 0; j<height; j++){
                background.setRGB(i,j,src.getRGB(i,j));
            }
        }

            //new a marking layer
        BufferedImage layer = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
            for (Event e : eventList) {
                if (e.label.equals("true")) {
                    logger.info("TupleId: " + tupleId +
                            "----Event timestamp: " + e.timestamp + " ----corner event? " + e.label);
                    count++;
                    if(e.polarity==1){
                        layer.setRGB(e.x,e.y,0xffff0000);//red

                    }else{
                        layer.setRGB(e.x,e.y,0xff00ff00);//green

                    }

                }
            }

            //add layer to background image
        Graphics graphics = background.getGraphics();
            graphics.drawImage(layer,0,0,null);

            //write to file
        try {
            ImageIO.write(background,"png", new File(dest_name));
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("SNK received: tupleId " + tupleId + "----" + count + " corner events are detected");
        //}

        tuplesReceived++;

        if(tuplesReceived>=numTuples){
            logger.info("Sink finished with total tuples: "+ tuplesReceived+" 0 warm-up tuples included");
            //System.out.println("finishes at "+System.currentTimeMillis());
            System.out.println("Total detection of the query: "+total_detection);

            System.exit(0);
        }

        api.ack(data);
    }

    @Override
    public void processData(List<DataTuple> dataList) {

    }
}
