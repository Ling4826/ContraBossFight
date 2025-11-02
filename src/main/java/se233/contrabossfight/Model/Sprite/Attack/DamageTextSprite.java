package se233.contrabossfight.Model.Sprite.Attack;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class DamageTextSprite extends ImageView {
    public enum TextType {
        MISS(0, 0, 120, 32),
        DAMAGE_999999(0, 32, 212, 32);

        private final int x;
        private final int y;
        private final int width;
        private final int height;

        TextType(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    private TextType textType;
    private Timeline bounceAnimation;

    public DamageTextSprite(Image spriteSheet, TextType type) {
        super(spriteSheet);
        this.textType = type;

        this.setViewport(new Rectangle2D(
                type.getX(),
                type.getY(),
                type.getWidth(),
                type.getHeight()
        ));
    }

    public void playBounceAnimation(double startX, double startY, Runnable onComplete) {
        this.setTranslateX(startX);
        this.setTranslateY(startY);
        this.setOpacity(1.0);

        bounceAnimation = new Timeline(
                new KeyFrame(Duration.millis(0),
                        e -> {
                            this.setTranslateY(startY);
                            this.setOpacity(1.0);
                        }),
                new KeyFrame(Duration.millis(200),
                        e -> this.setTranslateY(startY - 30)),
                new KeyFrame(Duration.millis(400),
                        e -> this.setTranslateY(startY - 20)),
                new KeyFrame(Duration.millis(800),
                        e -> this.setOpacity(1.0)),
                new KeyFrame(Duration.millis(1200),
                        e -> this.setOpacity(0.0))
        );

        bounceAnimation.setOnFinished(e -> {
            if (onComplete != null) onComplete.run();
        });

        bounceAnimation.play();
    }

    public void stopAnimation() {
        if (bounceAnimation != null) {
            bounceAnimation.stop();
        }
    }

    public TextType getTextType() {
        return textType;
    }
}