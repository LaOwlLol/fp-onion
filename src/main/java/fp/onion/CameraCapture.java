package fp.onion;

import com.github.sarxos.webcam.Webcam;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

public class CameraCapture implements Runnable {

    private Webcam webCam;
    private ImageProcessor ip;
    private boolean capturing;
    private boolean debug;

    public CameraCapture(Webcam webCam, ImageProcessor ip) {
        this(webCam, ip, false);
    }

    public CameraCapture(Webcam webCam,  ImageProcessor ip, boolean debug) {
        this.webCam = webCam;
        this.webCam.setViewSize(new Dimension(640,480));
        this.ip = ip;
        this.capturing = false;
        this.debug = debug;
    }

    @Override
    public void run() {
        final AtomicReference<WritableImage> ref = new AtomicReference<>();
        BufferedImage img = null;

        this.capturing = true;
        this.webCam.open();

        while (this.capturing) {
            try {
                if ((img = webCam.getImage()) != null) {
                    //TODO enqueue a producer consumer event  to sut out the atomic ref.
                    ref.set(SwingFXUtils.toFXImage(img, ref.get()));
                    img.flush();
                    if (capturing) {
                        this.ip.enqueue(ref.get(), 0);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (debug) {
            System.out.println("Webcam process stopped.");
        }
        webCam.close();
    }

    public boolean isCapturing() {
        return capturing;
    }

    public void stopCapturing() {
        this.capturing = false;
    }
}
