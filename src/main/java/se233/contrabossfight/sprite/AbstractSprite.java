package se233.contrabossfight.sprite;

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public abstract class AbstractSprite {
    protected double x;
    protected double y;
    protected double width;
    protected double height;
    protected double velocityX;
    protected double velocityY;
    protected boolean isAlive;
    protected String spriteSheetPath;
    protected Image spriteImage;

    public AbstractSprite(double x, double y, double width, double height, String spriteSheetPath) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.spriteSheetPath = spriteSheetPath;
        this.isAlive = true;
        this.velocityX = 0;
        this.velocityY = 0;
    }

    public abstract void update(double deltaTime);

    public abstract void render(GraphicsContext gc);

    public BoundingBox getBoundingBox() {
        return new BoundingBox(x, y, width, height);
    }

    public void takeDamage(double damage) {
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }
}