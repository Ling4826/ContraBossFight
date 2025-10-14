package se233.contrabossfight.sprite;

import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.BoundingBox;

public abstract class AbstractSprite {
    protected double x;
    protected double y;
    protected double width;
    protected double height;
    protected double velocityX;
    protected double velocityY;
    protected boolean isAlive;
    protected String spriteSheetPath; // Path หรือ ID สำหรับการโหลดภาพ

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

    /**
     * อัปเดตสถานะของวัตถุในแต่ละเฟรม
     * @param deltaTime เวลาที่ผ่านไประหว่างเฟรม (วินาที)
     */
    public abstract void update(double deltaTime);

    /**
     * วาดวัตถุบนหน้าจอ
     * @param gc GraphicsContext ของ Canvas
     */
    public abstract void render(GraphicsContext gc);

    /**
     * ตรวจสอบการชนกันโดยใช้ Bounding Box
     */
    public BoundingBox getBoundingBox() {
        return new BoundingBox(x, y, width, height);
    }

    // Getters และ Setters (ไม่แสดงทั้งหมดเพื่อความกระชับ)
    public boolean isAlive() { return isAlive; }
    public double getX() { return x; }
    // ...
}