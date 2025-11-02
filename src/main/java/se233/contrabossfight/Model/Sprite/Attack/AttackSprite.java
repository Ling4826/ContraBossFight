package se233.contrabossfight.Model.Sprite.Attack;

import javafx.scene.Group;
import javafx.scene.image.Image;
import se233.contrabossfight.Model.Sprite.Base.AnimatedSprite;
import se233.contrabossfight.Model.Sprite.Character.HeadSprite;

public class AttackSprite extends Group {
    private AnimatedSprite bodySprite;
    private HeadSprite headSprite;
    private double[][] headOffsets;
    private double baseHeadX;
    private double baseHeadY;
    private int ticksPerFrame = 1;
    private int tickCounter = 0;
    private boolean hasReachedLastFrame = false;
    private int maxFrames;
    private boolean isReversed = false;

    public AttackSprite(Image spriteSheet, int count, int columns, int rows,
                        int offsetX, int offsetY, int width, int height,
                        double[][] headOffsets, double baseHeadX, double baseHeadY) {
        this(spriteSheet, count, columns, rows, offsetX, offsetY, width, height,
                headOffsets, baseHeadX, baseHeadY, false);
    }

    public AttackSprite(Image spriteSheet, int count, int columns, int rows,
                        int offsetX, int offsetY, int width, int height,
                        double[][] headOffsets, double baseHeadX, double baseHeadY,
                        boolean isReversed) {
        this.bodySprite = new AnimatedSprite(spriteSheet, count, columns, rows,
                offsetX, offsetY, width, height);
        this.headSprite = new HeadSprite(spriteSheet);
        this.headOffsets = headOffsets;
        this.baseHeadX = baseHeadX;
        this.baseHeadY = baseHeadY;
        this.maxFrames = count;
        this.isReversed = isReversed;

        headSprite.setTranslateX(baseHeadX);
        headSprite.setTranslateY(baseHeadY);

        this.getChildren().addAll(bodySprite, headSprite);

        updateHeadPosition();
    }

    public void tick() {
        if (!hasReachedLastFrame) {
            tickCounter++;
            if (tickCounter >= ticksPerFrame) {
                if (isReversed) {
                    int nextIndex = bodySprite.getCurrentIndex() - 1;

                    if (nextIndex <= 0) {
                        bodySprite.tickReverse();
                        hasReachedLastFrame = true;
                    } else {
                        bodySprite.tickReverse();
                    }
                } else {
                    int nextIndex = bodySprite.getCurrentIndex() + 1;

                    if (nextIndex >= maxFrames - 1) {
                        bodySprite.tick();
                        hasReachedLastFrame = true;
                    } else {
                        bodySprite.tick();
                    }
                }

                updateHeadPosition();
                tickCounter = 0;
            }
        }
    }

    private void updateHeadPosition() {
        int frameIndex = bodySprite.getCurrentIndex();

        if (frameIndex >= headOffsets.length) {
            frameIndex = headOffsets.length - 1;
        }

        double[] offset = headOffsets[frameIndex];
        headSprite.setTranslateX(baseHeadX + offset[0]);
        headSprite.setTranslateY(baseHeadY + offset[1]);
    }

    public void reset() {
        if (isReversed) {
            bodySprite.setToLastFrame();
        } else {
            bodySprite.reset();
        }
        tickCounter = 0;
        hasReachedLastFrame = false;
        updateHeadPosition();
    }

    public boolean hasReachedLastFrame() {
        return hasReachedLastFrame;
    }

    public void setAnimationSpeed(double speed) {
        this.ticksPerFrame = Math.max(1, (int)(1.0 / speed));
    }

    public HeadSprite getHeadSprite() {
        return headSprite;
    }

    public boolean isReversed() {
        return isReversed;
    }
}