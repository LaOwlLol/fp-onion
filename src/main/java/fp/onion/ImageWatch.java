package fp.onion;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.*;

public class ImageWatch implements Runnable {

    private WatchService watch_service;
    private Thread watch_thread;
    private Path watch_dir;
    private App app;
    private AtomicBoolean watching;
    private boolean debug;

    public ImageWatch(App app) {
        this(app, false);
    }

    public ImageWatch(App app, boolean debug) {
        try {
            watch_service = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            System.err.println("Unable to get FileSystems new watch service.");
            //e.printStackTrace();
        }
        this.watching = new AtomicBoolean(false);
        this.app = app;
        this.debug = debug;
    }

    public void startWatch(Path path) {

        watch_dir = path;
        //setup watch on selected directory.
        try {
            WatchKey key = watch_dir.register(watch_service, ENTRY_CREATE, ENTRY_MODIFY);
            if (watch_thread == null) {
                watch_thread = new Thread( this);
                watch_thread.start();
            }
        } catch (IOException e) {
            System.err.println("Unable to setup directory watch.");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        watching.set(true);
        while (this.watching.get()) {
            // wait for key to be signaled
            WatchKey key;
            try {
                key = watch_service.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (debug) {
                    System.out.println("ImageWatch was interrupted.");
                }
                try {
                    watch_service.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == OVERFLOW) {
                    continue;
                }

                // The filename is the context of the event.
                WatchEvent<Path> ev = (WatchEvent<Path>)event;
                String filename = watch_dir.toString() + File.separator + ev.context().toString();

                filename = filename.replace("\\", "/");

                //if there is a last, and it's name is the same as this event's context
                if (app.hasFrame(filename)) {
                    //skip because we already worked on this image
                    //this should be a redundant event.
                    continue;
                }

                File file = new File(filename);
                try {
                    String mimetype = Files.probeContentType(file.toPath());
                    //mimetype should be something like "image/png"
                    if (mimetype != null && mimetype.split("/")[0].equals("image")) {

                    }
                    else {
                        if (debug) {
                            System.out.println("Not an image: " + filename + " type: " + mimetype);
                            continue;
                        }
                    }
                }
                catch (IOException e) {
                    if (debug) {
                        System.err.println("IOException on:" + filename);
                        e.printStackTrace();
                    }
                    continue;
                }

                //TODO This only seems to apply TO windows file system (and is critical there)
                // but never appears to throw errors on linux and prevents the successful finish of an image.
                // really going to need to rethink this whole architecture file watch may now cut it.
                /*Image i = new Image(file.toURI().toString());
                if (i.getWidth() < 1 || i.getHeight() < 1 ) {
                    if (debug) {
                        System.out.println("Malformed image: " + filename);
                    }
                    continue;
                }*/

                if (debug) {
                    System.out.println("Capturing image: " + filename);
                }
                app.captureFrame(filename);
            }
            // Reset the key -- this step is critical if you want to
            // receive further watch events.  If the key is no longer valid,
            // the directory is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }

        if (debug) {
            System.out.println("Image Watch ended.");
        }
    }

    public void stop() {
        if (debug) {
            System.out.println("Stop called on ImageWatch.");
        }
        this.watching.set(false);
        watch_thread.interrupt();
    }

    public boolean isWatching() {
        return this.watching.get();
    }
}
