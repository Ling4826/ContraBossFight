package se233.contrabossfight.sprite;

import java.util.Arrays;
import java.util.List;

public class Animation {
    private final List<AnimationFrame> frames;
    private final double frameDuration;
    private boolean loops;
    private double animationTime = 0;
    private int currentFrameIndex = 0;
    private boolean finished = false;

    public Animation(double frameDuration, boolean loops, AnimationFrame... frames) {
        this.frameDuration = frameDuration;
        this.loops = loops;
        this.frames = Arrays.asList(frames);
    }

    public void update(double deltaTime) {
        if (finished) return;

        animationTime += deltaTime;
        if (animationTime >= frameDuration) {
            animationTime -= frameDuration;
            currentFrameIndex++;

            if (currentFrameIndex >= frames.size()) {
                if (loops) {
                    currentFrameIndex = 0;
                } else {
                    currentFrameIndex = frames.size() - 1;
                    finished = true;
                }
            }
        }
    }

    public AnimationFrame getCurrentFrame() {
        return frames.get(currentFrameIndex);
    }

    public void reset() {
        this.currentFrameIndex = 0;
        this.animationTime = 0;
        this.finished = false;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setLoops(boolean loops) {
        this.loops = loops;
    }
}