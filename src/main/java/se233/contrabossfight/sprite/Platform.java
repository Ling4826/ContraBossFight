package se233.contrabossfight.sprite;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Platform extends AbstractSprite {

    private boolean isDroppable;

    private static final boolean IS_DEBUG_MODE = true;

    public Platform(double x, double y, double width, double height) {
        super(x, y, width, height, "");

        this.isDroppable = true;
    }

    public boolean isDroppable() {
        return isDroppable;
    }

    public void setDroppable(boolean droppable) {
        isDroppable = droppable;
    }


    @Override
    public void update(double deltaTime) {

    }

    @Override
    public void render(GraphicsContext gc) {

        if (IS_DEBUG_MODE) {
            gc.setFill(Color.rgb(0, 255, 0, 0.4));
            gc.fillRect(x, y, width, height);
        }
    }
    @Override
    public javafx.geometry.BoundingBox getBoundingBox() {
        return new javafx.geometry.BoundingBox(x, y, width, height);
    }
}