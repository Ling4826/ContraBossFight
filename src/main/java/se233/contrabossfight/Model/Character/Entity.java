package se233.contrabossfight.Model.Character;

import javafx.scene.Group;

public abstract class Entity extends Group {
    protected double x;
    protected double y;
    protected double speed;
    protected double velocityX = 0;
    protected double velocityY = 0;

    public Entity(double startX, double startY, double speed) {
        this.x = startX;
        this.y = startY;
        this.speed = speed;
    }

    public abstract void update(double deltaTime);

    protected void updatePosition() {
        this.setTranslateX(x);
        this.setTranslateY(y);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; updatePosition(); }
    public void setY(double y) { this.y = y; updatePosition(); }
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        updatePosition();
    }
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
}