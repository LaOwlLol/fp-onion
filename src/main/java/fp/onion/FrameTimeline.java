package fp.onion;

import java.util.ArrayList;

public class FrameTimeline {

    private ArrayList<String> frames;
    private boolean debug;
    private int pointer;
    private int head;

    public FrameTimeline() {
        this(false);
    }

    public FrameTimeline(boolean debug) {
        frames = new ArrayList<>();
        this.pointer = 0;
        this.head = 0;
        this.debug = debug;
    }

    public void addFrame(String frame) {
        this.frames.add(head, frame);
    }


    public boolean containsFrame(String frame) {
        return this.frames.size() > 0 && this.frames.contains(frame);
    }

    public int frameCount() {
        return frames.size();
    }

    /**
     * Get n frames.
     * @param n number of frames.
     * @return an list of frames starting from the pointer.
     */
    public ArrayList<String> getFrameSequence(int n) {

        ArrayList<String> temp = new ArrayList<>();

        for (int i = this.pointer; i < this.frames.size() && i < this.pointer + n; i++) {
            temp.add(this.frames.get(i));
        }

        return temp;
    }
}