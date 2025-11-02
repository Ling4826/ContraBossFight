package se233.contrabossfight.Model.Sprite.Base;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AnimatedSprite extends ImageView {
    private int count;
    private int columns;
    private int rows;
    private int offsetX;
    private int offsetY;
    private int width;
    private int height;
    private int curIndex = 0;
    private int curColumnIndex = 0;
    private int curRowIndex = 0;

    public AnimatedSprite(Image image, int count, int columns, int rows,
                          int offsetX, int offsetY, int width, int height) {
        this.setImage(image);
        this.count = count;
        this.columns = columns;
        this.rows = rows;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.height = height;
        this.setViewport(new Rectangle2D(offsetX, offsetY, width, height));
    }

    public boolean isLastFrame() {
        return curIndex == count - 1;
    }

    public void tick() {
        curColumnIndex = curIndex % columns;
        curRowIndex = curIndex / columns;
        curIndex = (curIndex + 1) % count;
        interpolate();
    }

    public void tickReverse() {
        curIndex = curIndex - 1;
        if (curIndex < 0) {
            curIndex = count - 1;
        }
        curColumnIndex = curIndex % columns;
        curRowIndex = curIndex / columns;
        interpolate();
    }

    protected void interpolate() {
        int x = curColumnIndex * width + offsetX;
        int y = curRowIndex * height + offsetY;
        this.setViewport(new Rectangle2D(x, y, width, height));
    }

    public void reset() {
        curIndex = 0;
        interpolate();
    }

    public void setToLastFrame() {
        curIndex = count - 1;
        interpolate();
    }

    public int getCurrentIndex() {
        return curIndex;
    }
}