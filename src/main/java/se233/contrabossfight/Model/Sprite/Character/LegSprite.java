package se233.contrabossfight.Model.Sprite.Character;

import javafx.scene.image.Image;
import se233.contrabossfight.Model.Sprite.Base.AnimatedSprite;

public class LegSprite extends AnimatedSprite {
    private int poseIndex = 0;
    private static final int TOTAL_POSES = 2;

    public LegSprite(Image spriteSheet) {
        super(spriteSheet, TOTAL_POSES, 2, 1, 0, 438, 96, 73);  // At (0, 438)
    }

    public void setPose(int index) {
        if (index >= 0 && index < TOTAL_POSES) {
            this.poseIndex = index;
            this.setViewport(getViewportForIndex(index));
        }
    }

    private javafx.geometry.Rectangle2D getViewportForIndex(int index) {
        int x = index * 96;
        int y = 438;
        return new javafx.geometry.Rectangle2D(x, y, 96, 73);
    }

    @Override
    public void tick() {

    }

    public void reset() {
        poseIndex = 0;
        setPose(0);
    }

    public int getCurrentPose() {
        return poseIndex;
    }
}
