package fp.onion;

import java.util.ArrayList;

public class OnionFrameTimeline {

    ArrayList<String> frames;
    private boolean debug;
    private int pointer;
    private int head;

    public OnionFrameTimeline() {
        this(false);
    }

    public OnionFrameTimeline(boolean debug) {
        frames = new ArrayList<>();
        this.pointer = 0;
        this.head = 0;
        this.debug = debug;
    }

    public void addFrame(String frame) {
        this.frames.add(0, frame);
    }

    public String getFrame(int index) {
        return this.frames.get(index);
    }

    /**
     * Is the current frame the given frame? Calculated by string equal(paths are the same).
     * @param frame Path to an image.
     * @return True if the pointer frame is equal to the given frame. False otherwise.
     */
    public boolean isCurrentFrame(String frame) {
        return (frames.size() > 0 && frames.size() > (this.pointer) && frames.get(this.pointer).equals(frame));
    }

    /**
     * Is the previous frame the given frame? Calculated by string equal(paths are the same).
     * @param frame Path to an image.
     * @return True if the frame after the pointer is equal to the given frame. False otherwise.
     */
    public boolean isPreviousFrame(String frame) {
        return (frames.size() > 0 && frames.size() > (this.pointer+1) && frames.get(this.pointer+1).equals(frame));
    }

    public boolean containsFrame(String frame) {
        return this.frames.size() > 0 && this.frames.contains(frame);
    }
    public int frameCount() {
        return frames.size();
    }

    public ArrayList<String> getOnion() {
        ArrayList<String> temp = new ArrayList<>();

        if (this.frames.size() > pointer) {
            temp.add(this.frames.get(pointer));
        }
        if (this.frames.size() > pointer+1) {
            temp.add(this.frames.get(pointer + 1));
        }

        return temp;
    }
}