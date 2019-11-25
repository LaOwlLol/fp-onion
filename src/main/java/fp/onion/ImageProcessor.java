package fp.onion;

import com.github.sarxos.webcam.Webcam;
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
    private CameraCapture cameraCapture;

    public ImageProcessor(GraphicsContext current, GraphicsContext previous, FrameTimeline ft) {
        this(current, previous, false);
    }

    public ImageProcessor(GraphicsContext current, GraphicsContext previous,  boolean debug) {
        this.debug = debug;
        this.worker = new SharedQueuePool(4);
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

    public void enqueue(Image img, int n) {
        if (debug) {
            System.out.println( n + "th drawing image.");
        }

        if (n == 0) {
            this.worker.enqueue( () -> {
                cgc.clearRect(0,0, 1024,768);
                if (img != null) {
                    cgc.drawImage(img, 0, 0, 1024, 768);
                }
            } );
        }
        else {
            this.worker.enqueue( () -> {
                pgc.clearRect(0,0, 1024,768);
                if (img != null) {
                    pgc.drawImage(img, 0, 0, 1024, 768);
                }
            } );
        }
    }

    public void enqueue(Webcam cam) {

        this.stopCameraCapture();

        this.cameraCapture = new CameraCapture(cam, this, debug);
        this.worker.enqueue(this.cameraCapture::run);

    }

    private void stopCameraCapture() {
        if (this.cameraCapture != null) {
            this.cameraCapture.stopCapturing();
        }
    }

    public void enqueue(Runnable event) {
        this.worker.enqueue(event);
    }

    public void close() {
        if (debug) {
            System.out.println("Stopping Image Processing");
        }
        this.stopCameraCapture();
        this.worker.cleanup();
    }
}

