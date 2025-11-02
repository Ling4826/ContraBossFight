package se233.contrabossfight.Model.Sprite.Attack;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.util.Duration;
import se233.contrabossfight.Model.Sprite.Base.AnimatedSprite;

import java.util.Random;

public class BulletAttackSprite extends AnimatedSprite {

    private Timeline shakeTimeline;
    private Random random = new Random();
    private double baseX;
    private double baseY;
    private boolean isShaking = false;

    public BulletAttackSprite(Image spriteSheet, int frameCount, int columns, int rows) {
        super(spriteSheet, frameCount, columns, rows, 0, 0, 96, 73);
    }

    public void playShakeAnimation(Runnable onComplete) {
        if (isShaking) return;

        isShaking = true;
        baseX = this.getTranslateX();
        baseY = this.getTranslateY();

        this.reset();
        this.setVisible(true);

        shakeTimeline = new Timeline(
                new KeyFrame(Duration.millis(30), e -> shake())
        );
        shakeTimeline.setCycleCount(20);

        shakeTimeline.setOnFinished(e -> {
            this.setVisible(false);
            isShaking = false;
            if (onComplete != null) {
                onComplete.run();
            }
        });

        shakeTimeline.play();
    }

    private void shake() {
        double offsetX = (random.nextDouble() - 0.5) * 8;
        double offsetY = (random.nextDouble() - 0.5) * 8;

        this.setTranslateX(baseX + offsetX);
        this.setTranslateY(baseY + offsetY);
    }

    public void stopAnimation() {
        if (shakeTimeline != null) {
            shakeTimeline.stop();
        }
        isShaking = false;
        this.setVisible(false);
    }

    @Override
    public void tick() {
    }
}