package se233.contrabossfight.View;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BattleBox extends Group {
    private Rectangle border;
    private double x;
    private double y;
    private double width;
    private double height;
    private double borderWidth = 5.0;

    public BattleBox(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        // Create white border box
        border = new Rectangle(width, height);
        border.setFill(Color.TRANSPARENT);
        border.setStroke(Color.WHITE);
        border.setStrokeWidth(borderWidth);

        this.getChildren().add(border);
        updatePosition();
    }

    private void updatePosition() {
        this.setTranslateX(x);
        this.setTranslateY(y);
    }

    // Setters for position
    public void setX(double x) {
        this.x = x;
        updatePosition();
    }

    public void setY(double y) {
        this.y = y;
        updatePosition();
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        updatePosition();
    }

    // Setters for size
    public void setWidth(double width) {
        this.width = width;
        border.setWidth(width);
    }

    public void setHeight(double height) {
        this.height = height;
        border.setHeight(height);
    }

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
        border.setWidth(width);
        border.setHeight(height);
    }

    // Extend specific edges (keeping opposite edge fixed)
    public void extendLeft(double amount) {
        this.x -= amount;
        this.width += amount;
        updatePosition();
        border.setWidth(width);
    }

    public void extendRight(double amount) {
        this.width += amount;
        border.setWidth(width);
    }

    public void extendTop(double amount) {
        this.y -= amount;
        this.height += amount;
        updatePosition();
        border.setHeight(height);
    }

    public void extendBottom(double amount) {
        this.height += amount;
        border.setHeight(height);
    }

    // Getters for boundaries (inner boundaries, accounting for border width)
    public double getLeftBoundary() {
        return x + borderWidth / 2;
    }

    public double getRightBoundary() {
        return x + width - borderWidth / 2;
    }

    public double getTopBoundary() {
        return y + borderWidth / 2;
    }

    public double getBottomBoundary() {
        return y + height - borderWidth / 2;
    }

    // Getters for position and size
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }

    // Border styling
    public void setBorderColor(Color color) {
        border.setStroke(color);
    }

    public void setBorderWidth(double width) {
        this.borderWidth = width;
        border.setStrokeWidth(width);
    }

    // Animate box movement
    public void moveBy(double dx, double dy) {
        this.x += dx;
        this.y += dy;
        updatePosition();
    }

    // Check if a point is inside the box
    public boolean contains(double pointX, double pointY) {
        return pointX >= getLeftBoundary() && pointX <= getRightBoundary() &&
                pointY >= getTopBoundary() && pointY <= getBottomBoundary();
    }
}