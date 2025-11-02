package se233.contrabossfight.character;

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.media.AudioClip;
import se233.contrabossfight.sprite.AbstractSprite;
import se233.contrabossfight.sprite.Animation;
import se233.contrabossfight.sprite.AnimationFrame;
import se233.contrabossfight.sprite.Bullet;
import se233.contrabossfight.sprite.WeaponType;
import se233.contrabossfight.util.Logger;
import se233.contrabossfight.util.ResourceLoadingException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Boss1DefenseWall extends Boss {
    private final ArrayList<AbstractSprite> components;
    private final BossWallCore core;
    private final BossTurret leftTurret;
    private final BossTurret leftTurret2;
    private final ConcurrentLinkedQueue<Bullet> bulletQueue;
    private final BossSniper sniper;
    private transient AudioClip coreHitSound;
    private transient AudioClip coreExplosionSound;
    private transient AudioClip componentHitSound;
    private transient AudioClip componentExplosionSound;
    private double attackTimer = 0;
    private double phaseDuration = 5.0;
    private int currentPhase = 0;
    private double shotCooldown = 0;
    private static final double SHOOT_INTERVAL = 0.5;
    private double playerX = 0;
    private double playerY = 0;
    private static final double BULLET_WIDTH = 48;
    private static final double BULLET_HEIGHT = 48;
    private static final double BULLET_DAMAGE = 10;
    private boolean upperTurretTurn = true;

    public Boss1DefenseWall(double x, double y, ConcurrentLinkedQueue<Bullet> bulletQueue) {
        super(x, y, 400, 150, null, 20);
        this.bulletQueue = bulletQueue;
        this.components = new ArrayList<>();
        double coreWidth = 256;
        double coreHeight = 192;
        this.core = new BossWallCore(x, y, coreWidth, coreHeight);
        double turretWidth = 96;
        double turretHeight = 51;
        String basePath = "/se233/contrabossfight/images/";
        String sniperSpritePath = basePath + "Sniper.png";
        double sniperWidth = 64;
        double sniperHeight = 64;
        this.sniper = new BossSniper(
                x + 40,
                y - 100,
                sniperWidth, sniperHeight,
                sniperSpritePath);
        this.leftTurret = new BossTurret(
                x + 5, y + 50,
                turretWidth, turretHeight, bulletQueue, -1,
                basePath + "BossTurret_Left_Idle.png",
                basePath + "BossTurret_Left_Shooting.png",
                basePath + "BossTurret_Left_Destroyed.png");
        this.leftTurret2 = new BossTurret(
                x + 65, y + 50,
                turretWidth, turretHeight, bulletQueue, -1,
                basePath + "BossTurret_Left_Idle1.png",
                basePath + "BossTurret_Left_Shooting1.png",
                basePath + "BossTurret_Left_Destroyed1.png");
        this.components.add(core);
        this.components.add(leftTurret);
        this.components.add(leftTurret2);
        this.components.add(sniper);
        try {
            String coreHitPath = "/se233/contrabossfight/sounds/Hit.wav";
            this.coreHitSound = new AudioClip(getClass().getResource(coreHitPath).toExternalForm());
            String coreExplodePath = "/se233/contrabossfight/sounds/Core_Explode.wav";
            this.coreExplosionSound = new AudioClip(getClass().getResource(coreExplodePath).toExternalForm());
            String compHitPath = "/se233/contrabossfight/sounds/Hit.wav";
            this.componentHitSound = new AudioClip(getClass().getResource(compHitPath).toExternalForm());
            String compExplodePath = "/se233/contrabossfight/sounds/Component_Explode.wav";
            this.componentExplosionSound = new AudioClip(getClass().getResource(compExplodePath).toExternalForm());
        } catch (Exception e) {
            Logger.log(Logger.LogType.FATAL, "Failed to load audio clip", e);
            this.coreHitSound = null;
            this.coreExplosionSound = null;
            this.componentHitSound = null;
            this.componentExplosionSound = null;
        }
    }

    public void setPlayerPosition(double playerX, double playerY) {
        this.playerX = playerX;
        this.playerY = playerY;
    }

    private void createBullet(double x, double y, double vx, double vy) {
        Bullet bullet = new Bullet(
                x - BULLET_WIDTH / 2,
                y - BULLET_HEIGHT / 2,
                BULLET_WIDTH,
                BULLET_HEIGHT,
                vx, vy,
                BULLET_DAMAGE,
                false,
                WeaponType.Boss,
                true);
        bulletQueue.add(bullet);
    }

    private void singleShot(int pattern) {
        if (upperTurretTurn && leftTurret.isAlive()) {
            shootPattern(leftTurret, pattern);
        } else if (!upperTurretTurn && leftTurret2.isAlive()) {
            shootPattern(leftTurret2, pattern);
        }
        upperTurretTurn = !upperTurretTurn;
    }

    private void fireTurret(BossTurret turret, double baseAngleDeg, double tiltDeg) {
        double centerX = turret.getX() + turret.getWidth() / 2;
        double centerY = turret.getY() + turret.getHeight() / 2;
        double randomOffset = Math.random() * 10 - 5;
        double angle = Math.toRadians(baseAngleDeg + tiltDeg + randomOffset);
        double baseSpeed = 280;
        double speed = baseSpeed * (0.9 + Math.random() * 0.2);
        double vx = Math.cos(angle) * speed;
        double vy = Math.sin(angle) * speed;
        createBullet(centerX, centerY, vx, vy);
        turret.playShootAnimation();
    }

    private void shootPattern(BossTurret turret, int pattern) {
        switch (pattern) {
            case 0:
                fireTurret(turret, 180, 12);
                break;
            case 1:
                fireTurret(turret, 180, 12);
                break;
            case 2:
                fireTurret(turret, 180, -10);
                break;
        }
    }

    private void triggerSniperExplosion(AbstractSprite sniper) {
        double centerX = sniper.getX() + sniper.getWidth() / 2.0;
        double centerY = sniper.getY() + sniper.getHeight() / 2.0;
        for (int i = 0; i < 5; i++) {
            double offsetX = (Math.random() - 0.5) * sniper.getWidth();
            double offsetY = (Math.random() - 0.5) * sniper.getHeight();
            Bullet explosionEffect = new Bullet(
                    centerX + offsetX,
                    centerY + offsetY,
                    1, 1,
                    0, 0,
                    0,
                    false,
                    WeaponType.Boss,
                    false,
                    false);
            explosionEffect.explode();
            bulletQueue.add(explosionEffect);
        }
    }

    private int attackPattern = 0;

    @Override
    public void attack() {
        attackPattern = (int) (Math.random() * 3);
        singleShot(attackPattern);
    }

    @Override
    public void update(double deltaTime) {
        for (AbstractSprite component : components) {
            component.update(deltaTime);
        }
        attackTimer += deltaTime;
        shotCooldown -= deltaTime;
        if (attackTimer >= phaseDuration) {
            attackTimer = 0;
            currentPhase = (currentPhase + 1) % 3;
            shotCooldown = 0;
        }
        if (shotCooldown <= 0) {
            attack();
            shotCooldown = SHOOT_INTERVAL;
        }
        if (!core.isAlive()) {
            this.isAlive = false;
            if (leftTurret.isAlive()) {
                leftTurret.takeDamage(999);
            }
            if (leftTurret2.isAlive()) {
                leftTurret2.takeDamage(999);
            }
            if (sniper.isAlive()) {
                sniper.takeDamage(999);
            }
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        for (AbstractSprite component : components) {
            component.render(gc);
        }
    }

    @Override
    public void takeDamage(double damage) {
        if (core.isAlive()) {
            core.takeDamage(damage);
        }
    }

    public ArrayList<AbstractSprite> getComponents() {
        return components;
    }

    public int getCurrentPhase() {
        return currentPhase;
    }

    private class BossWallCore extends AbstractSprite {
        private int health;
        private Animation currentAnimation;
        private transient Image destroyedSprite;

        public BossWallCore(double x, double y, double width, double height) {
            super(x, y, width, height, "BossCore.png");
            this.health = 100;
            this.isAlive = true;
            try {
                String destroyedImageUrl = "/se233/contrabossfight/images/BossCore_Destroyed.png";
                this.destroyedSprite = new Image(getClass().getResourceAsStream(destroyedImageUrl));
                if (this.destroyedSprite.isError()) {
                    Logger.log(Logger.LogType.FATAL, "Failed to load image: " + destroyedImageUrl);
                }
            } catch (Exception e) {
                Logger.log(Logger.LogType.FATAL, "Error loading destroyed sprite for BossWallCore", e);
            }
            double weakSpotX_relative = 60;
            double weakSpotY_relative = 160;
            double weakSpotWidth = 60;
            double weakSpotHeight = 60;
            BoundingBox hitbox = new BoundingBox(
                    weakSpotX_relative,
                    weakSpotY_relative,
                    weakSpotWidth,
                    weakSpotHeight);
            BoundingBox dummySourceRect = new BoundingBox(0, 0, 1, 1);
            this.currentAnimation = new Animation(0.1, true,
                    new AnimationFrame(dummySourceRect, hitbox));
        }

        public void takeDamage(double damage) {
            if (!isAlive)
                return;
            this.health -= damage;
            if (this.health <= 0) {
                this.health = 0;
                this.isAlive = false;
                if (coreExplosionSound != null) {
                    coreExplosionSound.play();
                }
                Logger.log(Logger.LogType.INFO, "BossWallCore destroyed.");
                triggerSniperExplosion(this);
            } else {
                if (coreHitSound != null) {
                    coreHitSound.play();
                }
            }
        }

        @Override
        public void update(double deltaTime) {
            if (!isAlive)
                return;
            if (currentAnimation != null) {
                currentAnimation.update(deltaTime);
            }
        }

        @Override
        public void render(GraphicsContext gc) {
            Image imageToDraw = null;
            if (isAlive) {
                imageToDraw = this.spriteImage;
            } else {
                imageToDraw = this.destroyedSprite;
            }
            if (imageToDraw != null) {
                gc.drawImage(imageToDraw, x + 20, y + 50, width + 70, height + 70);
            }
            // if (isAlive) {
            //     BoundingBox hb = getBoundingBox();
            //     gc.setStroke(Color.YELLOW);
            //     gc.setLineWidth(1);
            //     gc.strokeRect(hb.getMinX(), hb.getMinY(), hb.getWidth(), hb.getHeight());
            // }
        }

        @Override
        public BoundingBox getBoundingBox() {
            if (currentAnimation == null) {
                return new BoundingBox(this.x, this.y, this.width, this.height);
            }
            AnimationFrame animFrame = currentAnimation.getCurrentFrame();
            BoundingBox relativeHitbox = animFrame.getHitboxRect();
            return new BoundingBox(
                    this.x + relativeHitbox.getMinX(),
                    this.y + relativeHitbox.getMinY(),
                    relativeHitbox.getWidth(),
                    relativeHitbox.getHeight());
        }
    }

    private class BossTurret extends AbstractSprite {
        private int health;
        private transient Image idleSprite;
        private transient Image shootingSprite;
        private transient Image destroyedSprite;
        private static final double SHOOT_ANIMATION_DURATION = 0.3;
        private Animation idleAnim, shootAnim, destroyedAnim;
        private Animation currentAnimation;
        private int facingDirection;

        public BossTurret(double x, double y, double width, double height,
                          ConcurrentLinkedQueue<Bullet> bulletQueue, int facingDirection,
                          String idleImgPath, String shootingImgPath, String destroyedImgPath) {
            super(x, y, width, height, null);
            this.health = 50;
            this.isAlive = true;
            this.facingDirection = facingDirection;
            try {
                InputStream idleStream = getClass().getResourceAsStream(idleImgPath);
                if (idleStream == null) throw new ResourceLoadingException("File not found: " + idleImgPath);
                idleSprite = new Image(idleStream);

                InputStream shootingStream = getClass().getResourceAsStream(shootingImgPath);
                if (shootingStream == null) throw new ResourceLoadingException("File not found: " + shootingImgPath);
                shootingSprite = new Image(shootingStream);

                InputStream destroyedStream = getClass().getResourceAsStream(destroyedImgPath);
                if (destroyedStream == null) throw new ResourceLoadingException("File not found: " + destroyedImgPath);
                destroyedSprite = new Image(destroyedStream);
            } catch (Exception e) {
                Logger.log(Logger.LogType.FATAL, "Failed to load BossTurret images", e);
            }
            double h_x = 25;
            double h_y = 15;
            double h_w = 64;
            double h_h = 32;
            BoundingBox customHitbox = new BoundingBox(h_x, h_y, h_w, h_h);
            BoundingBox dummyRect = new BoundingBox(0, 0, 1, 1);
            AnimationFrame idleFrame = new AnimationFrame(dummyRect, customHitbox);
            AnimationFrame shootFrame = new AnimationFrame(dummyRect, customHitbox);
            AnimationFrame destroyedFrame = new AnimationFrame(dummyRect, customHitbox);
            idleAnim = new Animation(0.1, true, idleFrame);
            shootAnim = new Animation(SHOOT_ANIMATION_DURATION, false, shootFrame);
            destroyedAnim = new Animation(0.1, true, destroyedFrame);
            currentAnimation = idleAnim;
        }

        public void takeDamage(double damage) {
            if (!isAlive)
                return;
            this.health -= damage;
            Logger.log(Logger.LogType.DEBUG, "BossTurret took " + damage + " damage. HP: " + this.health);
            if (this.health <= 0) {
                this.isAlive = false;
                this.currentAnimation = destroyedAnim;
                if (componentExplosionSound != null) {
                    componentExplosionSound.play();
                }
                triggerSniperExplosion(this);
                Logger.log(Logger.LogType.INFO, "BossTurret destroyed.");
            } else {
                if (componentHitSound != null) {
                    componentHitSound.play();
                }
            }
        }

        @Override
        public void update(double deltaTime) {
            currentAnimation.update(deltaTime);
            if (!isAlive)
                return;
            if (currentAnimation == shootAnim && currentAnimation.isFinished()) {
                currentAnimation = idleAnim;
            }
        }

        public void playShootAnimation() {
            if (!isAlive || currentAnimation == shootAnim) {
                return;
            }
            currentAnimation = shootAnim;
            currentAnimation.reset();
        }

        @Override
        public void render(GraphicsContext gc) {
            Image imageToDraw;
            if (currentAnimation == destroyedAnim) {
                imageToDraw = destroyedSprite;
            } else if (currentAnimation == shootAnim) {
                imageToDraw = shootingSprite;
            } else {
                imageToDraw = idleSprite;
            }
            if (imageToDraw != null) {
                gc.drawImage(imageToDraw, x, y, width, height);
            }

            // if (isAlive) {
            //     BoundingBox hb = getBoundingBox();
            //     gc.setStroke(Color.YELLOW);
            //     gc.setLineWidth(1);
            //     gc.strokeRect(hb.getMinX(), hb.getMinY(), hb.getWidth(), hb.getHeight());
            // }
        }

        @Override
        public BoundingBox getBoundingBox() {
            if (currentAnimation == null) {
                return new BoundingBox(this.x, this.y, this.width, this.height);
            }
            AnimationFrame animFrame = currentAnimation.getCurrentFrame();
            BoundingBox relativeHitbox = animFrame.getHitboxRect();
            return new BoundingBox(
                    this.x + relativeHitbox.getMinX(),
                    this.y + relativeHitbox.getMinY(),
                    relativeHitbox.getWidth(),
                    relativeHitbox.getHeight());
        }
    }

    private class BossSniper extends AbstractSprite {
        private int health;
        private transient Image spriteSheet;
        private Animation currentAnimation;

        private enum SniperState {
            HIDING, EMERGING, SHOOTING
        }

        private SniperState currentState;
        private Animation aimStraightAnim, aimDownAnim;
        private Animation emergeAnim;
        private Animation dodgeAnim;
        private double stateTimer = 0;
        private static final double HIDE_DURATION = 2.0;
        private static final double SHOOT_DURATION = 3.0;
        private double attackCooldown = 1.0;
        private double attackTimer = 0;

        public BossSniper(double x, double y, double width, double height, String spriteSheetPath) {
            super(x, y, width, height, spriteSheetPath);
            this.health = 10;
            this.isAlive = true;
            try {
                InputStream stream = getClass().getResourceAsStream(spriteSheetPath);
                if (stream == null) throw new ResourceLoadingException("File not found: " + spriteSheetPath);
                this.spriteSheet = new Image(stream);
            } catch (Exception e) {
                Logger.log(Logger.LogType.FATAL, "Error loading Sniper sprite", e);
            }
            BoundingBox shootHitbox = new BoundingBox(5, 5, 64 - 10, 64 - 10);
            BoundingBox dodgeHitbox = new BoundingBox(5, 5, 64 - 10, 64 - 10);
            dodgeAnim = new Animation(0.1, true,
                    new AnimationFrame(new BoundingBox(0, 0, 64, 64), dodgeHitbox));
            aimDownAnim = new Animation(0.1, true,
                    new AnimationFrame(new BoundingBox(64, 64, 64, 64), shootHitbox));
            aimStraightAnim = new Animation(0.1, true,
                    new AnimationFrame(new BoundingBox(0, 128, 64, 64), shootHitbox));
            emergeAnim = new Animation(0.2, false,
                    new AnimationFrame(new BoundingBox(0, 0, 64, 64), dodgeHitbox),
                    new AnimationFrame(new BoundingBox(0, 64, 64, 64), dodgeHitbox),
                    new AnimationFrame(new BoundingBox(64, 0, 64, 64), shootHitbox));
            this.stateTimer = HIDE_DURATION;
            this.currentState = SniperState.HIDING;
            this.currentAnimation = dodgeAnim;
        }

        public void takeDamage(double damage) {
            if (currentState != SniperState.SHOOTING || !isAlive)
                return;
            this.health -= damage;
            Logger.log(Logger.LogType.DEBUG, "BossSniper took " + damage + " damage. HP: " + this.health);
            if (this.health <= 0) {
                this.health = 0;
                this.isAlive = false;
                Logger.log(Logger.LogType.INFO, "BossSniper destroyed.");
                if (componentExplosionSound != null) {
                    componentExplosionSound.play();
                }
                triggerSniperExplosion(this);
            } else {
                if (componentHitSound != null) {
                    componentHitSound.play();
                }
            }
        }

        private void aimAndShoot() {
            if (!isAlive || currentState != SniperState.SHOOTING)
                return;
            double targetX = playerX + 5;
            double targetY = playerY;
            double muzzleX = this.x + this.width * 0.4;
            double muzzleY = this.y + this.height * 0.3;
            double dx = targetX - muzzleX;
            double dy = targetY - muzzleY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            double speed = 250;
            double vx = (dx / distance) * speed;
            double vy = (dy / distance) * speed + 60;
            double angle = Math.toDegrees(Math.atan2(dy, dx));
            if (angle > 150 && angle < 210) {
                currentAnimation = aimStraightAnim;
            } else {
                currentAnimation = aimDownAnim;
            }
            Bullet bullet = new Bullet(
                    muzzleX, muzzleY, 20, 20, vx, vy, 5,
                    false, WeaponType.Boss, false, false);
            bulletQueue.add(bullet);
        }

        @Override
        public void update(double deltaTime) {
            if (!isAlive)
                return;
            switch (currentState) {
                case HIDING:
                    stateTimer -= deltaTime;
                    if (stateTimer <= 0) {
                        currentState = SniperState.EMERGING;
                        currentAnimation = emergeAnim;
                        emergeAnim.reset();
                    }
                    break;
                case EMERGING:
                    currentAnimation.update(deltaTime);
                    if (currentAnimation.isFinished()) {
                        currentState = SniperState.SHOOTING;
                        stateTimer = SHOOT_DURATION;
                        attackTimer = 0;
                    }
                    break;
                case SHOOTING:
                    stateTimer -= deltaTime;
                    if (stateTimer <= 0) {
                        currentState = SniperState.HIDING;
                        stateTimer = HIDE_DURATION;
                        currentAnimation = dodgeAnim;
                    } else {
                        attackTimer -= deltaTime;
                        if (attackTimer <= 0) {
                            aimAndShoot();
                            attackTimer = attackCooldown;
                        }
                    }
                    break;
            }
            if (currentState != SniperState.EMERGING) {
                currentAnimation.update(deltaTime);
            }
        }

        @Override
        public void render(GraphicsContext gc) {
            if (!isAlive)
                return;
            if (spriteSheet == null || currentAnimation == null) {
                gc.setFill(Color.ORANGE);
                gc.fillRect(x, y, width, height);
                return;
            }
            AnimationFrame animFrame = currentAnimation.getCurrentFrame();
            BoundingBox frame = animFrame.getSourceRect();
            gc.drawImage(
                    spriteSheet,
                    frame.getMinX(), frame.getMinY(), frame.getWidth(), frame.getHeight(),
                    this.x, this.y, this.width, this.height);
            // BoundingBox hb = getBoundingBox();
            // gc.setStroke(Color.YELLOW);
            // gc.setLineWidth(1);
            // gc.strokeRect(hb.getMinX(), hb.getMinY(), hb.getWidth(), hb.getHeight());
        }

        @Override
        public BoundingBox getBoundingBox() {
            if (currentState != SniperState.SHOOTING || currentAnimation == null) {
                return new BoundingBox(this.x, this.y, 0, 0);
            }
            AnimationFrame animFrame = currentAnimation.getCurrentFrame();
            BoundingBox relativeHitbox = animFrame.getHitboxRect();
            return new BoundingBox(
                    this.x + relativeHitbox.getMinX(),
                    this.y + relativeHitbox.getMinY(),
                    relativeHitbox.getWidth(),
                    relativeHitbox.getHeight());
        }
    }
}