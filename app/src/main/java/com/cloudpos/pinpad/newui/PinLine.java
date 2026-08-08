package com.cloudpos.pinpad.newui;

public class PinLine {

    public static final int ORIENTATION_HORIZONTAL = 0;
    public static final int ORIENTATION_VERTICAL = 1;

    private float position;
    private boolean locked;
    private int orientation;

    public PinLine(float position, int orientation) {
        this.position = position;
        this.orientation = orientation;
        this.locked = false;
    }

    public float getPosition() {
        return position;
    }

    public void setPosition(float position) {
        this.position = position;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
}