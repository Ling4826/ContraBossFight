package se233.contrabossfight.character;

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import se233.contrabossfight.sprite.AbstractSprite;
import se233.contrabossfight.sprite.Animation;
import se233.contrabossfight.sprite.AnimationFrame;
import se233.contrabossfight.util.Logger;

public class Sans extends AbstractSprite {

    private Animation standAnim;
    private Image spriteSheet;
    private Animation currentAnimation;

    public Sans(double x, double y, double width, double height) {
        super(x, y, width, height, "/se233/contrabossfight/images/Sans.png");
        this.isAlive = true;

        try {
            String path = "/se233/contrabossfight/images/Sans.png";
            this.spriteSheet = new Image(getClass().getResourceAsStream(path));
            if (this.spriteSheet.isError()) {
                throw new Exception("Image loaded with an error.");
            }
        } catch (Exception e) {
            Logger.log(Logger.LogType.ERROR, "Failed to load Sans.png sprite sheet", e);
            this.spriteSheet = null;
        }

        BoundingBox standHitbox = new BoundingBox(x, y, width, height);
        BoundingBox frame;

        if (this.spriteSheet != null) {
            frame = new BoundingBox(0, 0, this.spriteSheet.getWidth(), this.spriteSheet.getHeight());
        } else {
            frame = new BoundingBox(0, 0, 32, 32);
        }

        standAnim = new Animation(0.2, true,
                new AnimationFrame(frame, standHitbox)
        );

        this.currentAnimation = standAnim;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (spriteSheet == null || currentAnimation == null) {
            gc.setFill(Color.BLUE); // Placeholder
            gc.fillRect(x, y, width, height);
            return;
        }


        AnimationFrame animFrame = currentAnimation.getCurrentFrame();
        BoundingBox frame = animFrame.getSourceRect();

        gc.drawImage(spriteSheet,
                frame.getMinX(), frame.getMinY(),
                frame.getWidth(), frame.getHeight(),
                x, y,
                width, height
        );
    }

    @Override
    public void update(double deltaTime) {
    }
}