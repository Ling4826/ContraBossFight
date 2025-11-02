package se233.contrabossfight.character;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.media.AudioClip;
import se233.contrabossfight.sprite.*;
import se233.contrabossfight.util.Logger;
import se233.contrabossfight.util.ResourceLoadingException;
import javafx.scene.effect.ColorAdjust;

public class Player extends AbstractSprite {
    private int lives;
    private long score;
    private boolean isAlive = true;

    private boolean isJumping;
    private boolean isProne;
    private boolean isMovingLeft;
    private boolean isMovingRight;
    private boolean isAimingUp;
    private boolean isAimingDown;
    private boolean isDroppingDown;

    private final ConcurrentLinkedQueue<Bullet> bulletQueue;
    private WeaponType currentWeapon;
    private boolean isShooting;
    private double shootCooldown = 0.0;
    private double shootingAnimationTimer;

    private boolean isSilhouetteMode = false;

    private boolean isDying = false;

    public boolean isInvincible() {
        return isInvincible;
    }

    public void setInvincible(boolean invincible) {
        isInvincible = invincible;
    }

    private boolean isInvincible = false;
    private double invincibilityTimer = 0;

    private transient Image spriteSheet;
    private Animation standAnim, walkAnim, jumpAnim, proneAnim, fallAnim;
    private Animation walkAimUpAnim, walkAimDownAnim, walkAndShootAnim;
    private Animation shootStandAnim, shootUpAnim, shootDiagonalUpAnim, shootProneAnim;
    private Animation deathAnim;
    private Animation currentAnimation;

    private Platform currentPlatform;
    private int facingDirection;
    private final double NORMAL_HEIGHT;
    private static final double MOVE_SPEED = 100;
    private static final double GRAVITY = 1200.0;
    private static final double JUMP_VELOCITY = -600.0;
    private static double MIN_X = 30;
    private static double MAX_X = 800 - 480;
    private static final double NORMAL_WEAPON_COOLDOWN = 0.15;
    private static final double SPREAD_WEAPON_COOLDOWN = 0.4;
    private boolean isPlayingDeathAnimation = false;
    private double specialShootCooldown = 0.0;
    private transient AudioClip shootSound;
    private transient AudioClip deathSound;

    public Player(double x, double y, double width, double height, String spriteSheetPath,
                  ConcurrentLinkedQueue<Bullet> bulletQueue) {
        super(x, y, width, height, spriteSheetPath);
        this.bulletQueue = bulletQueue;
        this.lives = 3;
        this.score = 0;
        this.NORMAL_HEIGHT = height;
        this.currentWeapon = WeaponType.NORMAL;
        this.facingDirection = 1;

        initializeAnimationFrames();
        this.currentAnimation = standAnim;

        try {
            loadSpriteSheet(spriteSheetPath);

            String shootSoundPath = "/se233/contrabossfight/sounds/Player_Shoot.wav";
            this.shootSound = new AudioClip(getClass().getResource(shootSoundPath).toExternalForm());

            String deathSoundPath = "/se233/contrabossfight/sounds/Player_Death.wav";
            this.deathSound = new AudioClip(getClass().getResource(deathSoundPath).toExternalForm());

        } catch (Exception e) {
            Logger.log(Logger.LogType.FATAL, "Failed to load player sounds or sprites", e);
            this.shootSound = null;
            this.deathSound = null;
        }
    }

    private void loadSpriteSheet(String path) throws ResourceLoadingException {
        String fullPath = "/se233/contrabossfight/images/" + path;
        try {
            java.io.InputStream stream = getClass().getResourceAsStream(fullPath);
            if (stream == null) {
                throw new ResourceLoadingException("Resource file not found: " + fullPath);
            }

            this.spriteSheet = new Image(stream);
            if (this.spriteSheet.isError()) {
                throw new ResourceLoadingException("Image loaded but has an error: " + fullPath);
            }
        } catch (NullPointerException | IllegalArgumentException e) {

            throw new ResourceLoadingException("Failed to load sprite sheet: " + path, e);
        }
    }

    private void initializeAnimationFrames() {

        BoundingBox standHitbox = new BoundingBox(25, 30, 20, 35);
        BoundingBox jumpHitbox = new BoundingBox(23, 35, 17, 20);
        BoundingBox proneHitbox = new BoundingBox(14, 48, 35, 17);
        BoundingBox deadHitbox = new BoundingBox(0, 0, 0, 0);

        standAnim = new Animation(0.1, true,
                new AnimationFrame(new BoundingBox(128, 64, 64, 64), standHitbox));

        walkAnim = new Animation(0.1, true,
                new AnimationFrame(new BoundingBox(0, 0, 64, 64), standHitbox),
                new AnimationFrame(new BoundingBox(64, 0, 64, 64), standHitbox),
                new AnimationFrame(new BoundingBox(128, 0, 64, 64), standHitbox),
                new AnimationFrame(new BoundingBox(192, 0, 64, 64), standHitbox),
                new AnimationFrame(new BoundingBox(256, 0, 64, 64), standHitbox),
                new AnimationFrame(new BoundingBox(320, 0, 64, 64), standHitbox));

        jumpAnim = new Animation(0.1, true,
                new AnimationFrame(new BoundingBox(192, 256, 64, 64), jumpHitbox),
                new AnimationFrame(new BoundingBox(256, 256, 64, 64), jumpHitbox),
                new AnimationFrame(new BoundingBox(320, 256, 64, 64), jumpHitbox));

        proneAnim = new Animation(0.1, true,
                new AnimationFrame(new BoundingBox(128, 128, 64, 64), proneHitbox));

        shootProneAnim = new Animation(0.1, true,
                new AnimationFrame(new BoundingBox(128, 128, 64, 64), proneHitbox));

        deathAnim = new Animation(0.15, false,
                new AnimationFrame(new BoundingBox(128, 320, 64, 64), deadHitbox),
                new AnimationFrame(new BoundingBox(192, 320, 64, 64), deadHitbox),
                new AnimationFrame(new BoundingBox(256, 320, 64, 64), deadHitbox),
                new AnimationFrame(new BoundingBox(320, 320, 64, 64), deadHitbox),
                new AnimationFrame(new BoundingBox(64, 320, 64, 64), deadHitbox));

        fallAnim = new Animation(0.1, true,
                new AnimationFrame(new BoundingBox(0, 64, 64, 64), standHitbox));

        walkAimUpAnim = new Animation(0.1, true,
                new AnimationFrame(new BoundingBox(192, 128, 64, 64), standHitbox),
                new AnimationFrame(new BoundingBox(256, 128, 64, 64), standHitbox),
                new AnimationFrame(new BoundingBox(320, 128, 64, 64), standHitbox));

        walkAimDownAnim = new Animation(0.1, true,
                new AnimationFrame(new BoundingBox(0, 256, 64, 64), standHitbox),
                new AnimationFrame(new BoundingBox(64, 256, 64, 64), standHitbox),
                new AnimationFrame(new BoundingBox(128, 256, 64, 64), standHitbox));

        walkAndShootAnim = new Animation(0.1, true,
                new AnimationFrame(new BoundingBox(64, 64, 64, 64), standHitbox),
                new AnimationFrame(new BoundingBox(128, 64, 64, 64), standHitbox),
                new AnimationFrame(new BoundingBox(192, 64, 64, 64), standHitbox),
                new AnimationFrame(new BoundingBox(256, 64, 64, 64), standHitbox),
                new AnimationFrame(new BoundingBox(320, 64, 64, 64), standHitbox));

        shootStandAnim = new Animation(0.1, true,
                new AnimationFrame(new BoundingBox(192, 64, 64, 64), standHitbox));

        shootUpAnim = new Animation(0.1, true,
                new AnimationFrame(new BoundingBox(0, 128, 64, 64), standHitbox),
                new AnimationFrame(new BoundingBox(64, 128, 64, 64), standHitbox));

        shootDiagonalUpAnim = new Animation(0.1, true,

                new AnimationFrame(new BoundingBox(128, 64, 64, 64), standHitbox));
    }

    @Override
    public void update(double deltaTime) {
        update(deltaTime, new ArrayList<>(), new ArrayList<>());
    }

    public boolean isPlayingDeathAnimation() {
        return isPlayingDeathAnimation;
    }

    public void update(double deltaTime, List<Platform> platforms, List<Wall> walls) {

        if (isPlayingDeathAnimation) {
            setAnimation(deathAnim);
            currentAnimation.update(deltaTime);

            if (currentAnimation.isFinished()) {
                isPlayingDeathAnimation = false;
                if (isAlive) {
                    respawn();
                } else {
                    isDying = true;
                }
            }
            velocityX = 0;

        } else if (isDying) {
            velocityX = 0;

        } else {
            if (isInvincible) {
                invincibilityTimer -= deltaTime;
                if (invincibilityTimer <= 0) {
                    isInvincible = false;
                }
            }

            if (shootCooldown > 0)
                shootCooldown -= deltaTime;
            if (specialShootCooldown > 0)
                specialShootCooldown -= deltaTime;
            if (shootingAnimationTimer > 0) {
                shootingAnimationTimer -= deltaTime;
                if (shootingAnimationTimer <= 0)
                    isShooting = false;
            }

            if (isMovingLeft) {
                velocityX = -MOVE_SPEED;
                facingDirection = -1;
            } else if (isMovingRight) {
                velocityX = MOVE_SPEED;
                facingDirection = 1;
            } else {
                velocityX = 0;
            }
        }

        this.x += this.velocityX * deltaTime;
        velocityY += GRAVITY * deltaTime;

        boolean onGround = false;
        for (Platform platform : platforms) {
            boolean horizontalMatch = (this.x + this.width) - 50 > platform.getX()
                    && this.x < (platform.getX() + platform.getWidth()) - 50;
            boolean verticalMatch = (this.y + this.height) <= platform.getY()
                    && (this.y + this.height + velocityY * deltaTime) >= platform.getY();

            if (horizontalMatch && verticalMatch && velocityY >= 0) {
                this.y = platform.getY() - this.height;
                this.velocityY = 0;
                onGround = true;
                this.currentPlatform = platform;
                break;
            }
        }

        if (!onGround) {
            this.y += this.velocityY * deltaTime;
            this.currentPlatform = null;
        }

        if (!isDying && !isPlayingDeathAnimation) {
            this.isJumping = !onGround && this.velocityY < 0;
            if (onGround) {
                this.isDroppingDown = false;
            }
            this.isProne = isAimingDown && !isMovingLeft && !isMovingRight && !isJumping;
            updateAnimation(onGround);
            if (currentAnimation != null) {
                currentAnimation.update(deltaTime);
            }
        }
        double nextX = this.x + this.velocityX * deltaTime;

        BoundingBox currentHitbox = getBoundingBox();
        BoundingBox nextHitbox = new BoundingBox(
                currentHitbox.getMinX() + (this.velocityX * deltaTime),
                currentHitbox.getMinY(),
                currentHitbox.getWidth(),
                currentHitbox.getHeight());

        boolean willCollide = false;
        if (this.velocityX != 0) {
            for (Wall wall : walls) {
                if (nextHitbox.intersects(wall.getBoundingBox())) {
                    willCollide = true;
                    this.velocityX = 0;

                    if (this.velocityX > 0) {
                        this.x = wall.getX() - (currentHitbox.getMinX() - this.x) - currentHitbox.getWidth() - 0.1;
                    } else {
                        this.x = wall.getX() + wall.getWidth() - (currentHitbox.getMinX() - this.x) + 0.1;
                    }
                    break;
                }
            }
        }

        if (!willCollide) {
            this.x += this.velocityX * deltaTime;
        }

        if (this.x < MIN_X)
            this.x = MIN_X;
        else if (this.x > MAX_X)
            this.x = MAX_X;
    }

    private void updateAnimation(boolean onGround) {
        boolean isMoving = isMovingLeft || isMovingRight;
        if (!onGround) {
            setAnimation(isDroppingDown ? fallAnim : jumpAnim);
        } else {
            if (isMoving) {
                if (isAimingUp)
                    setAnimation(walkAimUpAnim);
                else if (isAimingDown)
                    setAnimation(walkAimDownAnim);
                else if (isShooting)
                    setAnimation(walkAndShootAnim);
                else
                    setAnimation(walkAnim);
            } else {
                if (isProne)
                    setAnimation(isShooting ? shootProneAnim : proneAnim);
                else if (isAimingUp)
                    setAnimation(shootUpAnim);
                else if (isShooting)
                    setAnimation(shootStandAnim);
                else
                    setAnimation(standAnim);
            }
        }
    }

    public void setStageBoundaries(double minX, double maxX) {
        this.MIN_X = minX;
        this.MAX_X = maxX;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (isInvincible && invincibilityTimer % 0.2 < 0.1) {
            return;
        }

        if (spriteSheet == null || currentAnimation == null) {
            gc.setFill(Color.MAGENTA);
            gc.fillRect(x, y, width, height);
            return;
        }
        AnimationFrame animFrame = currentAnimation.getCurrentFrame();
        BoundingBox frame = animFrame.getSourceRect();
        double sx = frame.getMinX();
        double sy = frame.getMinY();
        double sWidth = frame.getWidth();
        double sHeight = frame.getHeight();

        if (isSilhouetteMode) {
            gc.save();
            ColorAdjust blackEffect = new ColorAdjust();
            blackEffect.setBrightness(-1.0);
            gc.setEffect(blackEffect);
        }

        if (facingDirection == -1) {
            gc.drawImage(spriteSheet, sx + sWidth, sy, -sWidth, sHeight, x, y, width, height);
        } else {
            gc.drawImage(spriteSheet, sx, sy, sWidth, sHeight, x, y, width, height);
        }

        if (isSilhouetteMode) {
            gc.restore();
        }

        //renderHitbox(gc);
        //renderw(gc);
    }

    public void renderw(GraphicsContext gc) {
        BoundingBox box = getBoundingBox();
        gc.setStroke(Color.YELLOW);
        gc.strokeRect(box.getMinX(), box.getMinY(), box.getWidth(), box.getHeight());

        if (currentPlatform != null) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            double lineY = currentPlatform.getY();
            gc.strokeLine(currentPlatform.getX(), lineY,
                    currentPlatform.getX() + currentPlatform.getWidth(), lineY);
        }
    }

    private void renderHitbox(GraphicsContext gc) {
        BoundingBox hb = getBoundingBox();
        gc.setStroke(Color.RED);
        gc.setLineWidth(1);
        gc.strokeRect(hb.getMinX(), hb.getMinY(), hb.getWidth(), hb.getHeight());
    }

    @Override
    public BoundingBox getBoundingBox() {
        if (currentAnimation == null) {
            return new BoundingBox(this.x, this.y, this.width, this.height);
        }

        AnimationFrame animFrame = currentAnimation.getCurrentFrame();
        BoundingBox relativeHitbox = animFrame.getHitboxRect();
        BoundingBox sourceRect = animFrame.getSourceRect();
        double baseWidth = sourceRect.getWidth();
        double baseHeight = sourceRect.getHeight();
        double scaleX = this.width / baseWidth;
        double scaleY = this.height / baseHeight;
        double scaledBoxWidth = relativeHitbox.getWidth() * scaleX;
        double scaledBoxHeight = relativeHitbox.getHeight() * scaleY;
        double scaledBoxX = relativeHitbox.getMinX() * scaleX;
        double scaledBoxY = relativeHitbox.getMinY() * scaleY;
        double finalHitboxX;

        if (facingDirection == 1) {
            finalHitboxX = this.x + scaledBoxX;
        } else {
            finalHitboxX = (this.x + this.width) - scaledBoxX - scaledBoxWidth;
        }
        return new BoundingBox(
                finalHitboxX,
                this.y + scaledBoxY,
                scaledBoxWidth,
                scaledBoxHeight);
    }

    public void jump() {
        if (isAimingDown && currentPlatform != null && currentPlatform.isDroppable()) {
            this.y += 2;
            this.isDroppingDown = true;
            this.velocityY = 0;
            Logger.log(Logger.LogType.DEBUG, "Player dropping down from platform.");
            return;
        }
        if (currentPlatform != null && !isProne) {
            this.velocityY = JUMP_VELOCITY;
            Logger.log(Logger.LogType.DEBUG, "Player jumped.");
        }
    }

    private void createNormalBullet() {
        Point2D muzzlePos = getGunMuzzlePosition();
        double speed = 600;
        double diagonal = 0.7071;
        double bulletVX = facingDirection;
        double bulletVY = 0;
        boolean isMoving = isMovingLeft || isMovingRight;

        if (isAimingUp) {
            if (isMoving) {
                bulletVX = facingDirection * diagonal;
                bulletVY = -diagonal;
            } else {
                bulletVX = 0;
                bulletVY = -1;
            }
        } else if (isAimingDown) {
            if (isMoving) {
                bulletVX = facingDirection * diagonal;
                bulletVY = diagonal;
            }
        }
        Logger.log(Logger.LogType.DEBUG, "Player shot NORMAL bullet.");
        Bullet newBullet = new Bullet(muzzlePos.getX(), muzzlePos.getY(), 12, 12,
                bulletVX * speed, bulletVY * speed, 10, true, WeaponType.NORMAL);
        bulletQueue.add(newBullet);
    }

    private void createSpreadBullets() {
        Point2D muzzlePos = getGunMuzzlePosition();
        double speed = 600;

        double diagonal = 0.7071;
        boolean isMoving = isMovingLeft || isMovingRight;

        double v_mid_X = facingDirection * speed;
        double v_mid_Y = 0;
        double v_up_X = facingDirection * speed * 0.92;
        double v_up_Y = -speed * 0.38;
        double v_dn_X = facingDirection * speed * 0.92;
        double v_dn_Y = speed * 0.38;

        if (isAimingUp) {
            if (isMoving) {
                v_mid_X = facingDirection * speed * diagonal;
                v_mid_Y = -speed * diagonal;
                v_up_X = facingDirection * speed * 0.38;
                v_up_Y = -speed * 0.92;
                v_dn_X = facingDirection * speed * 0.92;
                v_dn_Y = -speed * 0.38;
            } else {
                v_mid_X = 0;
                v_mid_Y = -speed;
                v_up_X = facingDirection * speed * 0.38;
                v_up_Y = -speed * 0.92;
                v_dn_X = facingDirection * -speed * 0.38;
                v_dn_Y = -speed * 0.92;
            }
        } else if (isAimingDown && isMoving) {
            v_mid_X = facingDirection * speed * diagonal;
            v_mid_Y = speed * diagonal;
            v_up_X = facingDirection * speed * 0.92;
            v_up_Y = speed * 0.38;
            v_dn_X = facingDirection * speed * 0.38;
            v_dn_Y = speed * 0.92;
        }
        Logger.log(Logger.LogType.DEBUG, "Player shot SPREAD bullets.");
        bulletQueue.add(new Bullet(muzzlePos.getX(), muzzlePos.getY(), 12, 12,
                v_up_X, v_up_Y, 10, true, WeaponType.SPREAD));
        bulletQueue.add(new Bullet(muzzlePos.getX(), muzzlePos.getY(), 12, 12,
                v_mid_X, v_mid_Y, 10, true, WeaponType.SPREAD));
        bulletQueue.add(new Bullet(muzzlePos.getX(), muzzlePos.getY(), 12, 12,
                v_dn_X, v_dn_Y, 10, true, WeaponType.SPREAD));
    }

    private Point2D getGunMuzzlePosition() {
        double startX;
        double startY;
        boolean isMoving = isMovingLeft || isMovingRight;

        if (isProne) {
            startX = this.x + (facingDirection == 1 ? this.width * 0.75 : this.width * 0.25);
            startY = this.y + this.height * 0.78;
        } else if (isAimingUp && !isMoving) {
            startX = this.x + (facingDirection == 1 ? this.width * 0.55 : this.width * 0.40);
            startY = this.y + this.height * 0.01;
        } else if (isAimingUp && isMoving) {
            startX = this.x + (facingDirection == 1 ? this.width * 0.95 : this.width * 0.05);
            startY = this.y + this.height * 0.30;
        } else if (isAimingDown && isMoving) {
            startX = this.x + (facingDirection == 1 ? this.width * 0.88 : this.width * 0.12);
            startY = this.y + this.height * 0.75;
        } else {
            startX = this.x + (facingDirection == 1 ? this.width * 0.75 : this.width * 0.25);
            startY = this.y + this.height * 0.58;
        }
        return new Point2D(startX, startY);
    }

    public void takeHit() {
        if (isInvincible || isPlayingDeathAnimation || isDying) {
            Logger.log(Logger.LogType.DEBUG, "Player hit but was invincible.");
            return;
        }

        if (deathSound != null) {
            deathSound.play();
        }

        this.isPlayingDeathAnimation = true;
        this.lives--;
        Logger.log(Logger.LogType.WARN, "Player took hit! Lives remaining: " + this.lives);
        if (this.lives <= 0) {
            this.isAlive = false;
        }
    }

    public void respawn() {
        this.x = 50;
        this.y = 50;
        this.velocityY = 0;
        this.isInvincible = true;
        this.invincibilityTimer = 5.0;
        Logger.log(Logger.LogType.INFO, "Player respawned after death.");
    }

    public void respawnst() {
        this.x = 50;
        this.y = 50;
        this.velocityY = 0;
        this.isInvincible = false;
        this.invincibilityTimer = 5.0;
        Logger.log(Logger.LogType.INFO, "Player initial spawn.");
    }

    public void shoot() {
        if (shootCooldown > 0)
            return;

        if (shootSound != null) {
            shootSound.play();
        }

        if (currentWeapon == WeaponType.NORMAL) {
            createNormalBullet();
            shootCooldown = NORMAL_WEAPON_COOLDOWN;
        } else if (currentWeapon == WeaponType.SPREAD) {
            createSpreadBullets();
            shootCooldown = SPREAD_WEAPON_COOLDOWN;
        }

        isShooting = true;
        shootingAnimationTimer = 0.2;
    }

    public void shootSpecial() {
        if (specialShootCooldown > 0)
            return;

        if (shootSound != null) {
            shootSound.play();
        }

        createSpreadBullets();

        specialShootCooldown = 1.0;
        Logger.log(Logger.LogType.INFO, "Player used SPECIAL shoot.");
        isShooting = true;
        shootingAnimationTimer = 0.2;
    }

    public void moveLeft(boolean moving) {
        this.isMovingLeft = moving;
        if (moving)
        {
            Logger.log(Logger.LogType.DEBUG, "Player started moving left.");
        }
        else {
            Logger.log(Logger.LogType.DEBUG, "Player stopped moving left.");
        }
    }

    public void moveRight(boolean moving) {
        this.isMovingRight = moving;
        if (moving) {
            Logger.log(Logger.LogType.DEBUG, "Player started moving right.");
        }
        else {
            Logger.log(Logger.LogType.DEBUG, "Player stopped moving right.");
        }
    }

    public void aimUp(boolean aiming) {
        if (aiming && isAimingDown)
            return;
        this.isAimingUp = aiming;
    }

    public void aimDown(boolean aiming) {
        if (aiming && isAimingUp)
            return;
        this.isAimingDown = aiming;
    }

    public void setWeaponType(WeaponType newWeapon) {
        this.currentWeapon = newWeapon;
        Logger.log(Logger.LogType.INFO, "Player switched weapon to: " + newWeapon);
    }

    public void addScore(long points) {
        this.score += points;
        Logger.log(Logger.LogType.INFO, "Player score: " + this.score + " (+" + points + ")");
    }

    public int getLives() {
        return lives;
    }

    public long getScore() {
        return score;
    }

    public boolean isDying() {
        return isDying;
    }

    public boolean isAlive() {
        return isAlive;
    }

    private void setAnimation(Animation newAnimation) {
        if (currentAnimation != newAnimation) {
            currentAnimation = newAnimation;
            currentAnimation.reset();
        }
    }

    public void addLife(int amount) {
        this.lives += amount;
        Logger.log(Logger.LogType.INFO, "Player gained 1UP! Total lives: " + this.lives);
    }
    public Platform getCurrentPlatform() {
        return currentPlatform;
    }
    public boolean isProne() {
        return isProne;
    }

    public void setProne(boolean prone) {
        this.isProne = prone;
    }
    public void stopMovement() {
        this.isMovingLeft = false;
        this.isMovingRight = false;
        this.velocityX = 0;
    }

    public void setSilhouetteMode(boolean mode) {
        this.isSilhouetteMode = mode;
    }
    public AudioClip getShootSound() {
        return shootSound;
    }
}