package se233.contrabossfight.character;

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import se233.contrabossfight.sprite.*;
import se233.contrabossfight.util.Logger;
import se233.contrabossfight.util.ResourceLoadingException; // <-- เพิ่ม Import
import java.io.InputStream; // <-- เพิ่ม Import
import java.net.URL; // <-- เพิ่ม Import

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Boss2Java extends Boss {

    private double hp = 200;
    private double playerX, playerY;
    private List<Platform> platforms;
    private List<JavaCrawler> activeMinions;
    private transient AudioClip bossHitSound;
    private transient AudioClip bossExplosionSound;
    private transient Image spriteSheet;
    private Animation idleAnim;

    private double spawnTimer = 0.0;
    private double spawnCooldown = 4.0;

    protected ConcurrentLinkedQueue<Bullet> bulletQueue;

    public Boss2Java(double x, double y, ConcurrentLinkedQueue<Bullet> bulletQueue) {
        super(x, y, 113 * 2.5, 113 * 2.5, "", 500);
        this.bulletQueue = bulletQueue;
        this.activeMinions = new ArrayList<>();
        this.isAlive = true;


        BoundingBox hitbox = new BoundingBox(0, 0, (113 * 2.5) - 120, (113 * 2.5) - 50);
        this.idleAnim = new Animation(0.4, true,
                new AnimationFrame(new BoundingBox(0, 0, 113, 113), hitbox),
                new AnimationFrame(new BoundingBox(113, 0, 113, 113), hitbox));
        try {

            loadSpriteSheet();


            String coreHitPath = "/se233/contrabossfight/sounds/Hit.wav";
            URL hitURL = getClass().getResource(coreHitPath);
            if (hitURL == null) {
                throw new ResourceLoadingException("Sound file not found: " + coreHitPath);
            }
            this.bossHitSound = new AudioClip(hitURL.toExternalForm());

            String coreExplodePath = "/se233/contrabossfight/sounds/Core_Explode.wav";
            URL explodeURL = getClass().getResource(coreExplodePath);
            if (explodeURL == null) {
                throw new ResourceLoadingException("Sound file not found: " + coreExplodePath);
            }
            this.bossExplosionSound = new AudioClip(explodeURL.toExternalForm());

        } catch (ResourceLoadingException e) {
            Logger.log(Logger.LogType.FATAL, "Failed to load boss 2 resources", e);
            this.bossHitSound = null;
            this.bossExplosionSound = null;
        } catch (Exception e) {
            Logger.log(Logger.LogType.FATAL, "Unexpected error loading boss 2 sounds", e);
            this.bossHitSound = null;
            this.bossExplosionSound = null;
        }
    }

    private void loadSpriteSheet() throws ResourceLoadingException {
        String path = "/se233/contrabossfight/images/Boss2JavaHead.png";
        try {
            InputStream stream = getClass().getResourceAsStream(path);
            if (stream == null) {
                throw new ResourceLoadingException("Resource file not found: " + path);
            }
            this.spriteSheet = new Image(stream);
            if (this.spriteSheet.isError()) {
                throw new ResourceLoadingException("Image loaded but has an error: " + path);
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new ResourceLoadingException("Failed to load Boss2JavaHead sprite sheet.", e);
        }
    }

    @Override
    public void setPlayerPosition(double x, double y) {
        this.playerX = x;
        this.playerY = y;
    }

    public void setPlatforms(List<Platform> platforms) {
        this.platforms = platforms;
    }

    @Override
    public void update(double deltaTime) {
        if (!isAlive)
            return;
        idleAnim.update(deltaTime);

        spawnTimer += deltaTime;
        if (spawnTimer >= spawnCooldown) {
            spawnTimer = 0;

            Logger.log(Logger.LogType.INFO, "Boss2 spawning 3 JavaCrawlers...");

            for (int i = 0; i < 3; i++) {
                double spawnX = this.x + 30;
                double spawnY = this.y + 100 + (i * 10);

                int direction = (playerX > this.x) ? 1 : -1;

                JavaCrawler minion = new JavaCrawler(spawnX, spawnY, direction);
                activeMinions.add(minion);
            }
        }

        if (platforms != null) {
            activeMinions.removeIf(minion -> {
                if (minion.isAlive()) {
                    minion.setPlayerPosition(playerX, playerY);
                    minion.update(deltaTime, platforms);
                    return false;
                } else {
                    triggerSniperExplosion(minion);
                    return true;
                }
            });
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        for (JavaCrawler minion : activeMinions) {
            minion.render(gc);
        }

        if (!isAlive || spriteSheet == null)
            return;
        AnimationFrame animFrame = idleAnim.getCurrentFrame();
        BoundingBox frame = animFrame.getSourceRect();
        gc.drawImage(
                spriteSheet,
                frame.getMinX(), frame.getMinY(), frame.getWidth(), frame.getHeight(),
                this.x, this.y, this.width, this.height);

        if (isAlive) {
            BoundingBox hb = getBoundingBox();
            gc.setStroke(Color.RED);
            gc.setLineWidth(1);
            gc.strokeRect(hb.getMinX(), hb.getMinY(), hb.getWidth(), hb.getHeight());
        }
    }

    private void triggerSniperExplosion(AbstractSprite target) {
        double centerX = target.getX() + target.getWidth() / 2.0;
        double centerY = target.getY() + target.getHeight() / 2.0;
        for (int i = 0; i < 5; i++) {
            double offsetX = (Math.random() - 0.5) * target.getWidth();
            double offsetY = (Math.random() - 0.5) * target.getHeight();

            Bullet explosionEffect = new Bullet(
                    centerX + offsetX,
                    centerY + offsetY,
                    1, 1, 0, 0, 0,
                    false,
                    WeaponType.Boss,
                    false, false);
            explosionEffect.explode();
            bulletQueue.add(explosionEffect);
        }
    }

    @Override
    public ArrayList<AbstractSprite> getComponents() {
        ArrayList<AbstractSprite> components = new ArrayList<>();
        components.add(this);
        components.addAll(activeMinions);
        return components;
    }

    @Override
    public boolean isAlive() {
        return this.isAlive;
    }

    @Override
    public void takeDamage(double damage) {
        if (!isAlive)
            return;

        this.hp -= damage;
        Logger.log(Logger.LogType.DEBUG, "Boss2Java took " + damage + " damage. HP: " + this.hp);
        if (this.hp <= 0) {
            if (bossExplosionSound != null) {
                bossExplosionSound.play();
            }
            triggerSniperExplosion(this);

            this.isAlive = false;
            Logger.log(Logger.LogType.INFO, "Boss2Java (Head) has been defeated!");

            Logger.log(Logger.LogType.INFO, "Boss is dead, killing all minions...");
            for (JavaCrawler minion : activeMinions) {
                if (minion.isAlive()) {
                    minion.takeDamage(999);
                }
            }
        } else {
            if (bossHitSound != null) {
                bossHitSound.play();
            }
        }
    }

    @Override
    public BoundingBox getBoundingBox() {
        if (idleAnim == null || idleAnim.getCurrentFrame() == null) {
            return new BoundingBox(x, y, 0, 0);
        }

        BoundingBox relativeHitbox = idleAnim.getCurrentFrame().getHitboxRect();
        if (relativeHitbox == null) {
            return new BoundingBox(x, y, 0, 0);
        }

        return new BoundingBox(
                this.x + relativeHitbox.getMinX(),
                this.y + relativeHitbox.getMinY(),
                relativeHitbox.getWidth(),
                relativeHitbox.getHeight());
    }

    @Override
    public void attack() {
    }
}