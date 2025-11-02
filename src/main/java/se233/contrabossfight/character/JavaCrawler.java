package se233.contrabossfight.character;

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import se233.contrabossfight.sprite.AbstractSprite;
import se233.contrabossfight.sprite.Animation;
import se233.contrabossfight.sprite.AnimationFrame;
import se233.contrabossfight.sprite.Platform;
import se233.contrabossfight.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class JavaCrawler extends AbstractSprite {

    private double hp = 1;
    private int facingDirection = 1;
    private Animation currentAnimation;
    private static final double MOVE_SPEED = 50.0;

    private transient Image spriteSheet;

    private double playerX;
    private double playerY;

    private enum AiState {
        RANDOM_WANDER,
        HOMING
    }

    private AiState currentState;
    private double stateTimer;
    private static final double WANDER_DURATION = 1.5;

    public JavaCrawler(double x, double y, int facingDirection) {
        super(x, y, 64, 64, "");
        loadSpriteSheet();

        BoundingBox hitbox = new BoundingBox(10, 20, 44, 44);
        if (Math.random() < 0.5) {
            this.currentAnimation = new Animation(0.2, true,
                    new AnimationFrame(new BoundingBox(0, 0, 96, 96), hitbox),
                    new AnimationFrame(new BoundingBox(96, 0, 96, 96), hitbox));
        } else {
            this.currentAnimation = new Animation(0.2, true,
                    new AnimationFrame(new BoundingBox(0, 96, 96, 96), hitbox),
                    new AnimationFrame(new BoundingBox(96, 96, 96, 96), hitbox));
        }

        this.currentState = AiState.RANDOM_WANDER;
        this.stateTimer = WANDER_DURATION;

        double randomAngle = Math.random() * 2 * Math.PI;
        this.velocityX = Math.cos(randomAngle) * MOVE_SPEED;
        this.velocityY = Math.sin(randomAngle) * MOVE_SPEED;
        this.facingDirection = (this.velocityX > 0) ? 1 : -1;
    }

    private void loadSpriteSheet() {
        try {
            this.spriteSheet = new Image(
                    getClass().getResourceAsStream("/se233/contrabossfight/images/JavaCrawler.png"));
        } catch (Exception e) {
            Logger.log(Logger.LogType.FATAL, "Failed to load JavaCrawler sprite sheet.");
        }
    }

    public void setPlayerPosition(double x, double y) {
        this.playerX = x;
        this.playerY = y;
    }

    @Override
    public void update(double deltaTime) {
        update(deltaTime, new ArrayList<>());
    }

    public void update(double deltaTime, List<Platform> platforms) {
        if (!isAlive)
            return;

        if (currentState == AiState.RANDOM_WANDER) {
            stateTimer -= deltaTime;
            if (stateTimer <= 0) {
                currentState = AiState.HOMING;
            }
        }

        if (currentState == AiState.HOMING) {
            double dx = playerX - this.x + 50;
            double dy = playerY - this.y + 50;
            double distance = Math.sqrt(dx * dx + dy * dy);

            this.velocityX = (dx / distance) * MOVE_SPEED;
            this.velocityY = (dy / distance) * MOVE_SPEED;
            this.facingDirection = (this.velocityX > 0) ? 1 : -1;

        }

        this.x += this.velocityX * deltaTime;
        this.y += this.velocityY * deltaTime;

        currentAnimation.update(deltaTime);

        if (this.y > 600 || this.y < -100 || this.x < -100 || this.x > 900) {
            this.isAlive = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!isAlive)
            return;

        if (spriteSheet != null && currentAnimation != null) {
            AnimationFrame animFrame = currentAnimation.getCurrentFrame();
            BoundingBox frame = animFrame.getSourceRect();
            double sx = frame.getMinX();
            double sy = frame.getMinY();
            double sWidth = frame.getWidth();
            double sHeight = frame.getHeight();
            if (facingDirection == -1) {
                gc.drawImage(spriteSheet, sx + sWidth, sy, -sWidth, sHeight, x, y, width, height);
            } else {
                gc.drawImage(spriteSheet, sx, sy, sWidth, sHeight, x, y, width, height);
            }
        }

    }

    @Override
    public BoundingBox getBoundingBox() {
        if (currentAnimation == null)
            return new BoundingBox(x, y, 0, 0);
        AnimationFrame animFrame = currentAnimation.getCurrentFrame();
        BoundingBox relativeHitbox = animFrame.getHitboxRect();
        return new BoundingBox(
                this.x + relativeHitbox.getMinX(),
                this.y + relativeHitbox.getMinY(),
                relativeHitbox.getWidth(),
                relativeHitbox.getHeight());
    }

    @Override
    public void takeDamage(double damage) {
        this.hp -= damage;
        if (this.hp <= 0) {
            this.isAlive = false;
        }
    }
}