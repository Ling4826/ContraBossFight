package se233.contrabossfight.character;

import se233.contrabossfight.sprite.AbstractSprite;
import se233.contrabossfight.sprite.Bullet;
import se233.contrabossfight.util.Logger; // ใช้สำหรับการ Logging
import javafx.scene.canvas.GraphicsContext;
import java.util.concurrent.ConcurrentLinkedQueue; // สำหรับส่งกระสุน

public class Player extends AbstractSprite {
    private int lives;
    private long score;
    private boolean isJumping;
    private boolean isProne;
    private boolean isMovingLeft;
    private boolean isMovingRight;
    private static final double MOVE_SPEED = 200.0;
    private static final double GRAVITY = 600.0; // แรงโน้มถ่วง
    private static final double JUMP_VELOCITY = -400.0;
    private final ConcurrentLinkedQueue<Bullet> bulletQueue; // คิวสำหรับส่งกระสุนไป GameController

    public Player(double x, double y, double width, double height, String spriteSheetPath,
                  ConcurrentLinkedQueue<Bullet> bulletQueue) {
        super(x, y, width, height, spriteSheetPath);
        this.lives = 3; // 3 ชีวิตเริ่มต้น
        this.score = 0;
        this.bulletQueue = bulletQueue;
        Logger.log(Logger.LogType.DEBUG, "Player created at (" + x + ", " + y + ")");
    }

    @Override
    public void update(double deltaTime) {
        // 1. การเคลื่อนที่ตามแกน X
        if (isMovingLeft) {
            velocityX = -MOVE_SPEED;
            Logger.log(Logger.LogType.TRACE, "Player moves left"); // Log การเคลื่อนไหว
        } else if (isMovingRight) {
            velocityX = MOVE_SPEED;
            Logger.log(Logger.LogType.TRACE, "Player moves right"); // Log การเคลื่อนไหว
        } else {
            velocityX = 0;
        }

        // 2. ฟิสิกส์ (แรงโน้มถ่วง)
        if (isJumping) {
            velocityY += GRAVITY * deltaTime; // แรงโน้มถ่วงดึงลง
        }

        // 3. อัปเดตตำแหน่ง
        this.x += this.velocityX * deltaTime;
        this.y += this.velocityY * deltaTime;

        // 4. การจัดการพื้น (Platform) - ต้องมีการตรวจสอบการชนกับพื้น
        if (this.y >= 500) { // สมมติว่า 500 คือระดับพื้น
            this.y = 500;
            this.velocityY = 0;
            this.isJumping = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        // ... (โค้ดสำหรับวาดภาพ Player ตามสถานะ: เดิน, กระโดด, หมอบ)
        // gc.drawImage(image, x, y, width, height);
    }

    // ===================================
    // Player Actions
    // ===================================

    public void jump() {
        if (!isJumping && !isProne) {
            this.isJumping = true;
            this.velocityY = JUMP_VELOCITY;
            Logger.log(Logger.LogType.INFO, "Player jumps"); // Log การกระทำ
        }
    }

    public void shoot() {
        if (!isProne) {
            // สร้าง Bullet ที่ตำแหน่ง Player และกำหนดให้เคลื่อนที่ไปขวา
            Bullet newBullet = new Bullet(this.x + this.width, this.y + (this.height / 2),
                    15, 5, "path/to/bullet.png", 1, 10, true);
            bulletQueue.add(newBullet); // เพิ่มกระสุนเข้าคิวเพื่อ GameController นำไปประมวลผล
            Logger.log(Logger.LogType.INFO, "Player shoots"); // Log การกระทำ
        }
    }

    public void takeHit() {
        this.lives--;
        Logger.log(Logger.LogType.WARN, "Player hit! Lives remaining: " + this.lives);

        if (this.lives <= 0) {
            this.isAlive = false;
            Logger.log(Logger.LogType.FATAL, "Player dies. Game Over."); // Log การตาย
        } else {
            // ... (Respawn logic)
        }
    }

    public void addScore(long points) {
        this.score += points;
        Logger.log(Logger.LogType.INFO, "Score updated: +" + points + " (Total: " + this.score + ")"); // Log คะแนน
    }

    // ... (moveLeft/moveRight/prone methods)
}