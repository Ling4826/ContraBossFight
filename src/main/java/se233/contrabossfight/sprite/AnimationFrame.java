package se233.contrabossfight.sprite;

import javafx.geometry.BoundingBox;

public class AnimationFrame {
    private final BoundingBox sourceRect;
    private final BoundingBox hitboxRect;

    public AnimationFrame(BoundingBox sourceRect, BoundingBox hitboxRect) {
        this.sourceRect = sourceRect;
        this.hitboxRect = hitboxRect;
    }

    public BoundingBox getSourceRect() { return sourceRect; }
    public BoundingBox getHitboxRect() { return hitboxRect; }
}