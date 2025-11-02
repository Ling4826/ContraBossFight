package se233.contrabossfight.character;

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import se233.contrabossfight.sprite.AbstractSprite;
import se233.contrabossfight.sprite.Animation;
import se233.contrabossfight.sprite.AnimationFrame;
import se233.contrabossfight.util.Logger;

public class StartTriggerDog extends AbstractSprite {

    private transient Image spriteSheet;
    private Animation hangAnim;

    private double baseScreenY;
    private double bobTimer = 0;
    private double bobAmount = 10.0;

    public StartTriggerDog(double x, double y, double width, double height, String spriteSheetPath) {
        super(x, y, width, height, spriteSheetPath);
        this.isAlive = true;

        this.baseScreenY = y;

        try {
            this.spriteSheet = new Image(getClass().getResourceAsStream(spriteSheetPath));
            if (this.spriteSheet.isError()) {
                Logger.log(Logger.LogType.FATAL, "Failed to load Dog sprite: " + spriteSheetPath);
            }
        } catch (Exception e) {
            Logger.log(Logger.LogType.FATAL, "Error loading Dog sprite: " + e.getMessage());
        }

        BoundingBox sourceRect1 = new BoundingBox(0, 0, 100, 200);
        BoundingBox sourceRect2 = new BoundingBox(100, 0, 100, 200);
        BoundingBox sourceRect3 = new BoundingBox(200, 0, 100, 200);
        BoundingBox sourceRect4 = new BoundingBox(300, 0, 100, 200);
        BoundingBox sourceRect5 = new BoundingBox(0, 200, 100, 200);
        BoundingBox sourceRect6 = new BoundingBox(100, 200, 100, 200);
        BoundingBox sourceRect7 = new BoundingBox(200, 200, 100, 200);
        BoundingBox sourceRect8 = new BoundingBox(300, 200, 100, 200);
        BoundingBox sourceRect9 = new BoundingBox(0, 400, 100, 200);
        BoundingBox sourceRect10 = new BoundingBox(100, 400, 100, 200);
        BoundingBox hitbox = new BoundingBox(
                width * 0.1,
                height * 0.4,
                width,
                height);
        this.hangAnim = new Animation(0.3, true,
                new AnimationFrame(sourceRect1, hitbox),
                new AnimationFrame(sourceRect2, hitbox),
                new AnimationFrame(sourceRect3, hitbox),
                new AnimationFrame(sourceRect4, hitbox),
                new AnimationFrame(sourceRect5, hitbox),
                new AnimationFrame(sourceRect6, hitbox),
                new AnimationFrame(sourceRect7, hitbox),
                new AnimationFrame(sourceRect8, hitbox),
                new AnimationFrame(sourceRect9, hitbox),
                new AnimationFrame(sourceRect10, hitbox));
    }

    @Override
    public void update(double deltaTime) {
        if (!isAlive)
            return;
        hangAnim.update(deltaTime);

        bobTimer += deltaTime;

        double smallBobAmount = 4.0;
        this.y = baseScreenY + (Math.sin(bobTimer * 2.0) * smallBobAmount);

        double sideAmplitude = 35.0;
        double sideSpeed = 1.0;
        this.x = this.x + Math.cos(bobTimer * sideSpeed) * 1.0;
    }

    @Override
    public BoundingBox getBoundingBox() {
        if (hangAnim == null) {
            return new BoundingBox(this.x, this.y, this.width * 0.6, this.height * 0.6);
        }
        AnimationFrame animFrame = hangAnim.getCurrentFrame();
        BoundingBox relativeHitbox = animFrame.getHitboxRect();
        double shrinkX = (relativeHitbox.getWidth() * 0.2) + 35;
        double shrinkY = (relativeHitbox.getWidth() * 0.2) + 165;

        return new BoundingBox(
                (this.x + relativeHitbox.getMinX() + shrinkX / 2) - 10,
                (this.y + relativeHitbox.getMinY() + shrinkY / 2) - 30,
                relativeHitbox.getWidth() - shrinkX,
                relativeHitbox.getHeight() - shrinkY);
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!isAlive)
            return;

        if (spriteSheet == null || hangAnim == null) {
            gc.setFill(Color.WHITE);
            gc.fillRect(x, y, width, height);
            return;
        }

        AnimationFrame animFrame = hangAnim.getCurrentFrame();
        BoundingBox frame = animFrame.getSourceRect();

        gc.drawImage(
                spriteSheet,
                frame.getMinX(), frame.getMinY(), frame.getWidth(), frame.getHeight(),
                this.x, this.y, this.width, this.height);

        BoundingBox hb = getBoundingBox();
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(1);
        gc.strokeRect(hb.getMinX(), hb.getMinY(), hb.getWidth(), hb.getHeight());
    }

}