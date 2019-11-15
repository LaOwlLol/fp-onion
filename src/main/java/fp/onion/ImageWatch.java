package fp.onion;

import fauxpas.entities.FilterableImage;
import fp.image.lang.Interpreter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class ImageWatch implements Runnable {

    private WatchService watch_service;
    private Thread watch_thread;
    private Path watch_dir;
    private App app;
    private ArrayList<String> last;
    private AtomicBoolean watching;
    private AtomicBoolean used;

    public ImageWatch(App app) {
        try {
            watch_service = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            System.err.println("Unable to get FileSystems new watch service.");
            //e.printStackTrace();
        }
        this.watching = new AtomicBoolean(false);
        this.used = new AtomicBoolean(false);
        this.app = app;
        this.last = new ArrayList<>();
    }

    public void startWatch(Path path) {

        watch_dir = path;
        //setup watch on selected directory.
        try {
            WatchKey key = watch_dir.register(watch_service, ENTRY_CREATE);
            if (watch_thread == null) {
                watch_thread = new Thread( this);
                watch_thread.start();
            }
        } catch (IOException e) {
            System.err.println(e);
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

                    File file = new File(filename);

                    //mimetype should be something like "image/png"
                    try {
                        String mimetype = Files.probeContentType(file.toPath());
                        if (mimetype != null && mimetype.split("/")[0].equals("image")) {
                            System.out.println("Setting image: "+file.toURI().toString());
                            if (used.get()) {
                                last.add(0, filename);
                                app.setCurrent(last.get(0));
                                app.setPrevious(last.get(1));

                            }
                            else {
                                last.add(0, filename);
                                app.setCurrent(last.get(0));
                                used.set(true);
                            }
                        }
                        else {
                            System.out.println("not an image: "+filename+" type: "+mimetype);
                        }
                    }
                    catch (IOException e) {
                        System.out.println("IOException:");
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
                System.out.println("ImageWatch was interrupted.");
                try {
                    watch_service.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return;
            }

        }

        System.out.println("Watch ended.");
    }

    public void stop() {
        System.out.println("Stop called on ImageWatch.");
        this.watching.set(false);
        watch_thread.interrupt();
    }

    public boolean isWatching() {
        return this.watching.get();
    }
}
