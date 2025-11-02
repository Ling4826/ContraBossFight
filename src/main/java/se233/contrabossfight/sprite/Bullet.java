package se233.contrabossfight.sprite;

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import se233.contrabossfight.util.Logger;
import se233.contrabossfight.util.ResourceLoadingException;

import java.io.InputStream;

public class Bullet extends AbstractSprite {
    private static final double GRAVITY = 600.0;
    private static final double GAME_WIDTH = 800.0;
    private static final double GAME_HEIGHT = 600.0;
    private static final double EXPLOSION_FRAME_DURATION = 0.08;
    private static final double EXPLOSION_SIZE = 100.0;

    private final double damage;
    private final boolean isPlayerBullet;
    private final WeaponType type;

    private boolean useGravity;
    private boolean rotatesWithVelocity;
    private transient Image spriteSheet;
    private Animation currentAnimation;
    private boolean isExploding = false;
    private double explosionAnimTimer = 0;
    private transient Image[] explosionAnimation;

    public Bullet(double x, double y, double width, double height,
                  double vx, double vy, double damage, boolean isPlayerBullet, WeaponType type) {
        this(x, y, width, height, vx, vy, damage, isPlayerBullet, type, false, false);
    }

    public Bullet(double x, double y, double width, double height,
                  double vx, double vy, double damage, boolean isPlayerBullet,
                  WeaponType type, boolean useGravity) {
        this(x, y, width, height, vx, vy, damage, isPlayerBullet, type, useGravity, useGravity);
    }

    public Bullet(double x, double y, double width, double height,
                  double vx, double vy, double damage, boolean isPlayerBullet,
                  WeaponType type, boolean useGravity, boolean rotatesWithVelocity) {
        super(x, y, width, height, "");
        this.damage = damage;
        this.isPlayerBullet = isPlayerBullet;
        this.type = type;
        this.velocityX = vx;
        this.velocityY = vy;
        this.useGravity = useGravity;
        this.rotatesWithVelocity = rotatesWithVelocity;
        initializeAnimation(type);
    }

    private void initializeAnimation(WeaponType type) {
        String spritePath = "";
        double animDuration = 0.08;

        double scale = 0.6;
        double hbWidth = this.width * scale;
        double hbHeight = this.height * scale;
        double hbX = (this.width - hbWidth) / 2;
        double hbY = (this.height - hbHeight) / 2;
        BoundingBox bulletHitbox = new BoundingBox(hbX, hbY, hbWidth, hbHeight);

        switch (type) {
            case SPREAD:
                spritePath = "/se233/contrabossfight/images/NORMAL.png";
                this.currentAnimation = new Animation(animDuration, true,
                        new AnimationFrame(new BoundingBox(0, 0, 32, 32), bulletHitbox)
                );
                break;

            case Boss:
                spritePath = "/se233/contrabossfight/images/BossBullet.png";
                this.currentAnimation = new Animation(animDuration, true,
                        new AnimationFrame(new BoundingBox(0, 0, 32, 32), bulletHitbox)
                );
                break;

            case NORMAL:
            default:
                spritePath = "/se233/contrabossfight/images/NORMAL.png";
                this.currentAnimation = new Animation(animDuration, true,
                        new AnimationFrame(new BoundingBox(0, 0, 12, 12), bulletHitbox)
                );
                break;
        }

        loadSpriteSheet(spritePath);
        loadExplosionAnimation(type);
    }

    private void loadSpriteSheet(String spritePath) {
        try {
            InputStream stream = getClass().getResourceAsStream(spritePath);
            if (stream == null) {
                throw new ResourceLoadingException("Sprite sheet not found: " + spritePath);
            }
            this.spriteSheet = new Image(stream);
        } catch (Exception e) {
            Logger.log(Logger.LogType.FATAL, "Failed to load bullet sprite sheet: " + spritePath, e);
            this.spriteSheet = null;
        }
    }

    private void loadExplosionAnimation(WeaponType type) {
        try {
            String path = "/se233/contrabossfight/images/";

            if (type == WeaponType.Boss) {
                this.explosionAnimation = new Image[4];
                Logger.log(Logger.LogType.INFO, "Loading BOSS explosion sprites");
                explosionAnimation[0] = new Image(getClass().getResourceAsStream(path + "BossExplosion_0.png"));
                explosionAnimation[1] = new Image(getClass().getResourceAsStream(path + "BossExplosion_1.png"));
                explosionAnimation[2] = new Image(getClass().getResourceAsStream(path + "BossExplosion_2.png"));
                explosionAnimation[3] = new Image(getClass().getResourceAsStream(path + "BossExplosion_3.png"));
            } else {
                this.explosionAnimation = new Image[2];
                Logger.log(Logger.LogType.INFO, "Loading PLAYER explosion sprites");
                explosionAnimation[0] = new Image(getClass().getResourceAsStream(path + "sprite_1.png"));
                explosionAnimation[1] = new Image(getClass().getResourceAsStream(path + "sprite_2.png"));
            }

            for (Image img : explosionAnimation) {
                if (img.isError()) {
                    Logger.log(Logger.LogType.FATAL, "Failed to load an explosion frame.");
                }
            }
        } catch (Exception e) {
            Logger.log(Logger.LogType.FATAL, "Failed to load explosion sprites", e);
        }
    }

    public void explode() {
        if (this.isExploding) return;
        this.isExploding = true;
        this.explosionAnimTimer = 0;
        this.velocityX = 0;
        this.velocityY = 0;
        this.useGravity = false;
    }

    @Override
    public BoundingBox getBoundingBox() {
        if (currentAnimation == null || isExploding) {
            return new BoundingBox(this.x, this.y, 0, 0);
        }
        AnimationFrame animFrame = currentAnimation.getCurrentFrame();
        BoundingBox relativeHitbox = animFrame.getHitboxRect();
        return new BoundingBox(
                this.x + relativeHitbox.getMinX(),
                this.y + relativeHitbox.getMinY(),
                relativeHitbox.getWidth(),
                relativeHitbox.getHeight()
        );
    }

    @Override
    public void update(double deltaTime) {
        if (isExploding) {
            updateExplosion(deltaTime);
            return;
        }

        if (useGravity) {
            this.velocityY += GRAVITY * deltaTime;
        }

        this.x += this.velocityX * deltaTime;
        this.y += this.velocityY * deltaTime;

        if (currentAnimation != null) {
            currentAnimation.update(deltaTime);
        }

        checkOutOfBounds();
    }

    private void updateExplosion(double deltaTime) {
        explosionAnimTimer += deltaTime;
        if (explosionAnimTimer >= EXPLOSION_FRAME_DURATION * explosionAnimation.length) {
            this.isAlive = false;
        }
    }

    private void checkOutOfBounds() {
        boolean outOfBounds = (this.x < -this.width || this.x > GAME_WIDTH ||
                this.y < -this.height || this.y > GAME_HEIGHT);
        if (outOfBounds) {
            this.isAlive = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (isExploding) {
            renderExplosion(gc);
            return;
        }

        if (spriteSheet == null || currentAnimation == null) {
            renderFallback(gc);
        } else {
            renderSprite(gc);
        }

        renderDebugHitbox(gc);
    }

    private void renderExplosion(GraphicsContext gc) {
        int frame = (int) (explosionAnimTimer / EXPLOSION_FRAME_DURATION);
        Image imgToDraw = null;

        if (explosionAnimation != null && frame < explosionAnimation.length) {
            imgToDraw = explosionAnimation[frame];
        }

        if (imgToDraw != null) {
            double centerX = this.x + this.width / 2;
            double centerY = this.y + this.height / 2;
            double drawX = centerX - (EXPLOSION_SIZE / 2);
            double drawY = centerY - (EXPLOSION_SIZE / 2);
            gc.drawImage(imgToDraw, drawX, drawY, EXPLOSION_SIZE, EXPLOSION_SIZE);
        }
    }

    private void renderFallback(GraphicsContext gc) {
        gc.setFill(javafx.scene.paint.Color.YELLOW);
        gc.fillOval(x, y, width, height);
    }

    private void renderSprite(GraphicsContext gc) {
        AnimationFrame animFrame = currentAnimation.getCurrentFrame();
        BoundingBox frame = animFrame.getSourceRect();

        if (this.rotatesWithVelocity && (this.velocityX != 0 || this.velocityY != 0)) {
            renderRotatedSprite(gc, frame);
        } else {
            renderNormalSprite(gc, frame);
        }
    }

    private void renderRotatedSprite(GraphicsContext gc, BoundingBox frame) {
        double angle = Math.toDegrees(Math.atan2(velocityY, velocityX));
        double centerX = this.x + this.width / 2;
        double centerY = this.y + this.height / 2;

        gc.save();
        gc.translate(centerX, centerY);
        gc.rotate(angle);
        gc.drawImage(
                spriteSheet,
                frame.getMinX(), frame.getMinY(), frame.getWidth(), frame.getHeight(),
                -this.width / 2, -this.height / 2,
                this.width, this.height
        );
        gc.restore();
    }

    private void renderNormalSprite(GraphicsContext gc, BoundingBox frame) {
        gc.drawImage(
                spriteSheet,
                frame.getMinX(), frame.getMinY(), frame.getWidth(), frame.getHeight(),
                this.x, this.y, this.width, this.height
        );
    }

    private void renderDebugHitbox(GraphicsContext gc) {
        BoundingBox hb = getBoundingBox();
        gc.setStroke(javafx.scene.paint.Color.RED);
        gc.strokeRect(hb.getMinX(), hb.getMinY(), hb.getWidth(), hb.getHeight());
    }

    public double getDamage() {
        return damage;
    }

    public boolean isPlayerBullet() {
        return isPlayerBullet;
    }

    public boolean isUsingGravity() {
        return useGravity;
    }

    public boolean isExploding() {
        return isExploding;
    }
}