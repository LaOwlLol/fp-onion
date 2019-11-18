package fp.onion;

import java.util.ArrayList;

public class FrameTimeline {
    ArrayList<String> frames;
    private boolean debug;

    public FrameTimeline() {
        this(false);
    }

    public FrameTimeline(boolean debug) {
        frames = new ArrayList<>();
        this.debug = debug;
    }

    public String getCurrent() {
        return this.getFrame(0);
    }

    /**
     * Return a previous frame.
     * @param previous which frame to fetch.
     * @return if previous = 0 return the current frame, otherwise return the previous frame.
     */
    public String getPrevious(int previous) {
        return this.getFrame(previous);
    }

    public void addFrame(String frame) {
        this.frames.add(0, frame);
    }

    public String getFrame(int index) {
        return this.frames.get(index);
    }

    public boolean isLastFrame(String frame) {
        return (frames.size() > 0 && frames.get(0).equals(frame));
    }

    public int frameCount() {
        return frames.size();
    }
}