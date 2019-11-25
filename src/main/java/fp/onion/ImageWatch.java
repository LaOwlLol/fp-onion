package fp.onion;

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

                    try {
                        File file = new File(filename);
                        String mimetype = Files.probeContentType(file.toPath());
                        //mimetype should be something like "image/png"
                        if (mimetype != null && mimetype.split("/")[0].equals("image")) {
                            if (debug) {
                                System.out.println("Setting image: " + file.toURI().toString());
                            }
                            app.captureFrame(filename);
                        }
                        else {
                            if (debug) {
                                System.out.println("not an image: " + filename + " type: " + mimetype);
                            }
                        }
                    }
                    catch (IOException e) {
                        System.err.println("IOException on:" + filename);
                        e.printStackTrace();
                        continue;
                    }
                }
                // Reset the key -- this step is critical if you want to
                // receive further watch events.  If the key is no longer valid,
                // the directory is inaccessible so exit the loop.
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
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

        }

        if (debug) {
            System.out.println("Watch ended.");
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
