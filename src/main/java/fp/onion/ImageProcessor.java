package fp.onion;

import fauxpas.event.ProduceConsumeEvent;
import fauxpas.eventqueue.SharedQueuePool;
import fp.image.lang.Interpreter;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class ImageProcessor {

    SharedQueuePool worker;
    GraphicsContext cgc;
    GraphicsContext pgc;
    Interpreter interp;
    private boolean debug;

    public ImageProcessor(GraphicsContext current, GraphicsContext previous, OnionFrameTimeline ft) {
        this(current, previous, false);
    }

    public ImageProcessor(GraphicsContext current, GraphicsContext previous,  boolean debug) {
        this.debug = debug;
        this.worker = new SharedQueuePool(2);
        this.cgc = current;
        this.pgc = previous;
        this.interp = new Interpreter();
    }

    public void enqueue(String script, int n) {
        if (debug) {
            System.out.println( n + "th Script: " + script);
        }

        if (n == 0) {
            this.worker.enqueue( new ProduceConsumeEvent<Image>( ()-> interp.interp(script).getImage(), (image) -> {
                cgc.clearRect(0,0, 1024,768);
                cgc.drawImage(image, 0, 0, 1024, 768);
            } ) );
        }
        else {
            this.worker.enqueue( new ProduceConsumeEvent<Image>(
                ()-> {
                    pgc.clearRect(0,0, 1024,768);
                    return interp.interp(script).getImage();
                },
                (image) -> pgc.drawImage(image, 0,0, 1024, 768) ));
        }
    }

    public void close() {
        if (debug) {
            System.out.println("Stopping Image Processing");
        }
        this.worker.cleanup();
    }
}

