package fp.onion;

import fauxpas.event.ProduceConsumeEvent;
import fauxpas.eventqueue.SharedQueuePool;
import fauxpas.eventqueue.SingleQueue;
import fp.image.lang.Interpreter;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class ImageProcessor {

    SharedQueuePool worker;
    GraphicsContext cgc;
    GraphicsContext pgc;
    Interpreter interp;

    public ImageProcessor(GraphicsContext current, GraphicsContext previous) {
        this.worker = new SharedQueuePool(2);
        this.cgc = current;
        this.pgc = previous;
        this.interp = new Interpreter();
    }

    public void enqueueCurrent(String script) {
        this.worker.enqueue( new ProduceConsumeEvent<Image>( ()-> interp.interp(script).getImage(), (image) -> {
            cgc.clearRect(0,0, 1024,768);
            cgc.drawImage(image, 0, 0, 1024, 768);
        } ) );
    }

    public void enqueuePrevious(String script) {
        this.worker.enqueue( new ProduceConsumeEvent<Image>(
            ()-> {
                pgc.clearRect(0,0, 1024,768);
                return interp.interp(script).getImage();
            },
            (image) -> pgc.drawImage(image, 0,0, 1024, 768) ));
    }

    public void close() {
        System.out.println("Stopping Image Processing");
        this.worker.cleanup();
    }
}

