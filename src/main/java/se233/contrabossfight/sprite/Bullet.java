package se233.contrabossfight.sprite;

import se233.contrabossfight.character.Player;
import se233.contrabossfight.character.Boss;
// อาจต้องมีการ import คลาสของศัตรูอื่น ๆ เช่น Minion

/**
 * คลาสสำหรับวัตถุ 'กระสุน' ที่ใช้ในการต่อสู้
 * กระสุนสามารถถูกยิงโดย Player หรือ Boss
 */
public class Bullet extends AbstractSprite {

    private final double damage;
    private final boolean isPlayerBullet; // true ถ้าเป็นกระสุนของผู้เล่น, false ถ้าเป็นของศัตรู

    // กำหนดความเร็วคงที่
    private static final double BULLET_SPEED = 300.0;

    /**
     * Constructor สำหรับ Bullet
     * @param x ตำแหน่งเริ่มต้นแกน X
     * @param y ตำแหน่งเริ่มต้นแกน Y
     * @param width ความกว้างของกระสุน
     * @param height ความสูงของกระสุน
     * @param spriteSheetPath path ของภาพกระสุน
     * @param direction ทิศทาง 1 คือไปขวา, -1 คือไปซ้าย (กำหนด velocityX)
     * @param damage ค่าความเสียหาย
     * @param isPlayerBullet ระบุว่าเป็นกระสุนของผู้เล่นหรือไม่
     */
    public Bullet(double x, double y, double width, double height, String spriteSheetPath,
                  int direction, double damage, boolean isPlayerBullet) {

        super(x, y, width, height, spriteSheetPath);
        this.damage = damage;
        this.isPlayerBullet = isPlayerBullet;

        // กำหนดความเร็วตามทิศทาง (กระสุนเคลื่อนที่เชิงเส้นอย่างรวดเร็ว)
        this.velocityX = direction * BULLET_SPEED;
        this.velocityY = 0;
    }

    /**
     * การอัปเดตสถานะในแต่ละเฟรม (Multithreading/Game Loop)
     * กระสุนจะเคลื่อนที่ไปตาม velocityX
     */
    @Override
    public void update(double deltaTime) {
        // อัปเดตตำแหน่งตามความเร็ว
        this.x += this.velocityX * deltaTime;

        // ตรวจสอบว่ากระสุนออกนอกขอบเขตของหน้าจอแล้วหรือไม่
        // ถ้าออกนอกจอให้ตั้งค่า isAlive = false เพื่อรอการลบออกจาก GameController
        // if (this.x < 0 || this.x > GAME_WIDTH) { // ต้องมีค่าคงที่ GAME_WIDTH
        //     this.isAlive = false;
        // }
    }

    /**
     * เมธอดสำหรับวาด (Render)
     */
    @Override
    public void render(Object graphicsContext) {
        // Implement logic to draw the bullet sprite
    }

    // ===================================
    // Getter
    // ===================================

    public double getDamage() {
        return damage;
    }

    public boolean isPlayerBullet() {
        return isPlayerBullet;
    }
}