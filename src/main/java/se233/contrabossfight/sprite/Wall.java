package se233.contrabossfight.sprite;

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Wall extends AbstractSprite {

    public Wall(double x, double y, double width, double height) {
        super(x, y, width, height, "");
    }

    @Override
    public void update(double deltaTime) {

    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.rgb(255, 0, 0, 1)); // สีแดง 40%
        gc.fillRect(this.x, this.y, this.width, this.height);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(this.x, this.y, this.width, this.height);
    }
}