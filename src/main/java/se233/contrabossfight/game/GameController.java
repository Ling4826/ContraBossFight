package se233.contrabossfight.game;

import se233.contrabossfight.character.Player;
import se233.contrabossfight.character.Boss1DefenseWall; // Boss 1 ของด่านแรก
import se233.contrabossfight.sprite.Bullet;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * จัดการลอจิกของเกมทั้งหมด รวมถึง Game Loop, วัตถุ, และการชน
 */
public class GameController {
    private Player player;
    private Boss1DefenseWall boss1;
    private ArrayList<Bullet> activeBullets;
    private ConcurrentLinkedQueue<Bullet> newBulletsQueue;

    public GameController() {
        this.activeBullets = new ArrayList<>();
        this.newBulletsQueue = new ConcurrentLinkedQueue<>();

        // 1. สร้าง Player
        // ส่ง newBulletsQueue เข้าไปเพื่อให้ Player ส่งกระสุนออกมาได้
        this.player = new Player(100, 500, 30, 40, "path/to/player.png", newBulletsQueue);

        // 2. สร้าง Boss 1 (ด่านแรก)
        this.boss1 = new Boss1DefenseWall(700, 450, 150, 150, "path/to/boss1.png");
    }

    /**
     * เมธอดนี้จะถูกเรียกจาก Game Loop/Animation Timer
     * @param deltaTime เวลาที่ผ่านไประหว่างเฟรม
     */
    public void updateGame(double deltaTime) {
        // 1. อัปเดต Player และ Boss
        player.update(deltaTime);
        boss1.update(deltaTime);

        // 2. จัดการกระสุนใหม่จาก Player
        while (!newBulletsQueue.isEmpty()) {
            activeBullets.add(newBulletsQueue.poll());
        }

        // 3. อัปเดตและตรวจจับการชนของกระสุน
        activeBullets.removeIf(bullet -> {
            bullet.update(deltaTime);

            if (!bullet.isAlive()) return true; // ลบกระสุนที่ออกนอกจอ

            // ตรวจสอบการชนกัน (Collision Detection)
            if (bullet.isPlayerBullet()) {
                if (bullet.getBoundingBox().intersects(boss1.getBoundingBox())) {
                    boss1.takeDamage(bullet.getDamage());
                    player.addScore(1); // เพิ่มคะแนนเมื่อกระสุนชน
                    return true; // ลบกระสุน
                }
            } else { // กระสุนของ Boss/ศัตรู
                if (bullet.getBoundingBox().intersects(player.getBoundingBox())) {
                    player.takeHit(); // One-hit kill
                    return true; // ลบกระสุน
                }
            }

            return false;
        });

        // 4. ตรวจสอบสถานะ Boss (เพื่อเปลี่ยนด่าน)
        if (!boss1.isAlive()) {
            // TODO: โหลด Boss 2 ต่อไป
            // Display a text animation upon completion
        }
    }

    // ... (Getters สำหรับ Player, Boss, Bullets เพื่อใช้ใน GameStage.render())
}