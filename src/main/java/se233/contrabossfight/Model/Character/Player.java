package se233.contrabossfight.Model.Character;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import se233.contrabossfight.View.BattleBox;


public class Player extends Entity {
    private ImageView sprite;
    private Rectangle hitbox;
    private Image soulSpriteSheet;

    // Soul types
    public enum SoulColor {
        RED(1),
        BLUE(19),
        TRANSPARENT(37);

        private final int offsetX;
        SoulColor(int offsetX) { this.offsetX = offsetX; }
        public int getOffsetX() { return offsetX; }
    }

    // Gravity directions
    public enum GravityDirection {
        DOWN(0),
        UP(180),
        LEFT(90),
        RIGHT(270);

        private final int rotation;
        GravityDirection(int rotation) { this.rotation = rotation; }
        public int getRotation() { return rotation; }
    }

    private SoulColor currentColor = SoulColor.RED;
    private GravityDirection gravityDirection = GravityDirection.DOWN;

    // Sprite dimensions
    private static final int SPRITE_SIZE = 16;
    private static final int SPRITE_Y = 0;

    // Hitbox matches sprite size
    private double hitboxWidth = SPRITE_SIZE;
    private double hitboxHeight = SPRITE_SIZE;

    // --- (ปรับค่าฟิสิกส์) ---
    private double vy = 0; // ความเร็วแนวตั้ง (Vertical Velocity)
    private double gravity = 500; // (ลดลงจาก 900 ให้นุ่มนวลขึ้น)
    private double jumpStrength = -300; // (ลดลงจาก -500 ให้โดดเตี้ยลง)
    private double variableJumpDampen = 0.4; // (คงเดิม)
    private double risingGravityMultiplier = 0.9; // (ใหม่) แรงต้านตอนลอยขึ้น (เลียนแบบ GML)

    private boolean isGrounded = false;
    private boolean isJumping = false;

    // Debug flag
    private boolean showHitbox = false;

    public Player(double startX, double startY) {
        super(startX, startY, 160.0); // (คงเดิม) [cite: 63]

        // (โค้ด Load SOUL sprite sheet, hitbox, ฯลฯ เหมือนเดิม) [cite: 66-99]
        try {
            soulSpriteSheet = new Image(
                    getClass().getResourceAsStream("/se233/contrabossfight/Asset/SOUL_sprite.png")
            );
            sprite = new ImageView(soulSpriteSheet);

            sprite.setViewport(new Rectangle2D(
                    SoulColor.RED.getOffsetX(),
                    SPRITE_Y,
                    SPRITE_SIZE,
                    SPRITE_SIZE
            ));

            sprite.setTranslateX(-SPRITE_SIZE / 2.0);
            sprite.setTranslateY(-SPRITE_SIZE / 2.0);

        } catch (Exception e) {
            System.err.println("Failed to load SOUL sprite: " + e.getMessage());
            e.printStackTrace();
            Rectangle fallback = new Rectangle(SPRITE_SIZE, SPRITE_SIZE, Color.RED);
            fallback.setTranslateX(-SPRITE_SIZE / 2.0);
            fallback.setTranslateY(-SPRITE_SIZE / 2.0);
            this.getChildren().add(fallback);
        }

        hitbox = new Rectangle(hitboxWidth, hitboxHeight);
        hitbox.setFill(Color.TRANSPARENT);
        hitbox.setStroke(Color.YELLOW);
        hitbox.setStrokeWidth(1);
        hitbox.setVisible(showHitbox);

        hitbox.setTranslateX(-hitboxWidth / 2);
        hitbox.setTranslateY(-hitboxHeight / 2);

        if (sprite != null) {
            this.getChildren().addAll(hitbox, sprite);
        } else {
            this.getChildren().add(hitbox);
        }

        updatePosition();
    }

    public void setSoulColor(SoulColor color) {
        this.currentColor = color;
        if (sprite != null && soulSpriteSheet != null) {
            sprite.setViewport(new Rectangle2D(
                    color.getOffsetX(),
                    SPRITE_Y,
                    SPRITE_SIZE,
                    SPRITE_SIZE
            ));
        }

        if (color != SoulColor.BLUE) {
            // (ถ้าเปลี่ยนเป็นสีแดง ให้รีเซ็ตฟิสิกส์)
            this.vy = 0; // [cite: 114]
            sprite.setRotate(0);
        } else {
            // (คง "Slam Down" ที่คุณขอไว้)
            this.isGrounded = false; // [cite: 119]
            this.isJumping = false; // [cite: 120]
            sprite.setRotate(gravityDirection.getRotation()); // [cite: 122]
        }
    }
    /**
     * (ใหม่) ตั้งค่าทิศทางแรงโน้มถ่วงโดยใช้ตัวเลข
     * 1 = DOWN (ลง)
     * 2 = UP (ขึ้น)
     * 3 = LEFT (ซ้าย)
     * 4 = RIGHT (ขวา)
     */
    public void setGravityByNumber(int direction) {
        GravityDirection newDirection;
        switch (direction) {
            case 1:
                newDirection = GravityDirection.DOWN; //
                break;
            case 2:
                newDirection = GravityDirection.UP;   //
                break;
            case 3:
                newDirection = GravityDirection.LEFT; //
                break;
            case 4:
                newDirection = GravityDirection.RIGHT;//
                break;
            default:
                // ถ้าใส่เลขอื่น, ให้กลับไปเป็น DOWN
                newDirection = GravityDirection.DOWN; //
                break;
        }
        // เรียกเมธอดเดิมที่มีอยู่
        setGravityDirection(newDirection); //
    }
    public void setGravityDirection(GravityDirection direction) {
        this.gravityDirection = direction; // [cite: 126]
        if (currentColor == SoulColor.BLUE && sprite != null) {
            sprite.setRotate(direction.getRotation()); // [cite: 128]
        }
        vy = 0; // [cite: 131]
    }

    public SoulColor getCurrentColor() {
        return currentColor;
    }

    public GravityDirection getGravityDirection() {
        return gravityDirection;
    }

    public void setMovement(boolean up, boolean down, boolean left, boolean right) {
        // (โค้ด setMovement ทั้งหมดเหมือนเดิม) [cite: 144-181]
        if (currentColor == SoulColor.BLUE) {
            velocityX = 0;
            velocityY = 0;

            // Movement based on gravity direction [cite: 148-167]
            switch (gravityDirection) {
                case DOWN -> {
                    if (left) velocityX -= 1;
                    if (right) velocityX += 1;
                }
                case UP -> {
                    if (left) velocityX -= 1;
                    if (right) velocityX += 1;
                }
                case LEFT -> {
                    if (up) velocityY -= 1;
                    if (down) velocityY += 1;
                }
                case RIGHT -> {
                    if (up) velocityY -= 1;
                    if (down) velocityY += 1;
                }
            }
        } else {
            // RED soul - free 8-directional movement [cite: 169-181]
            velocityX = 0;
            velocityY = 0;

            if (up) velocityY -= 1;
            if (down) velocityY += 1;
            if (left) velocityX -= 1;
            if (right) velocityX += 1;

            if (velocityX != 0 && velocityY != 0) {
                double length = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
                velocityX /= length;
                velocityY /= length;
            }
        }
    }

    /**
     * (แก้ไข) เริ่มการกระโดด (เรียกตอนกดปุ่ม)
     */
    public void startJump() {
        if (currentColor == SoulColor.BLUE && isGrounded) { // [cite: 182]
            this.vy = this.jumpStrength; // (ใช้ค่าใหม่ -500)
            this.isGrounded = false;
            this.isJumping = true; // [cite: 185]
        }
    }

    /**
     * (แก้ไข) สิ้นสุดการกระโดด (เรียกตอนปล่อยปุ่ม)
     */
    /**
     * (แก้ไข) สิ้นสุดการกระโดด (เรียกตอนปล่อยปุ่ม)
     */
    /**
     * (แก้ไข) สิ้นสุดการกระโดด (เรียกตอนปล่อยปุ่ม)
     */
    public void releaseJump() {
        this.isJumping = false; //

        // --- (นี่คือ Logic ที่คุณขอ) ---
        // ถ้าผู้เล่นปล่อยปุ่ม "ในขณะที่" หัวใจยังลอยขึ้น (vy < 0)
        if (this.vy < 0) {
            // ให้ลดความเร็วที่ลอยขึ้นทันที (คูณด้วย 0.4)
            this.vy *= this.variableJumpDampen;
        }
    }

    /**
     * (แก้ไข) อัปเดตฟิสิกส์โดยใช้ DeltaTime
     * @param deltaTime เวลาที่ผ่านไปตั้งแต่เฟรมที่แล้ว (หน่วยเป็นวินาที)
     */
    @Override
    public void update(double deltaTime) {
        if (currentColor == SoulColor.BLUE) {
            // --- (โลจิกแรงโน้มถ่วงใหม่ - แก้ไข) ---

            // (ลบ if (!isGrounded) ออกจากตรงนี้)

            // (1) เช็กว่า: ถ้ากำลังลอยขึ้น (vy < 0) และ ยังกดปุ่มกระโดดแช่ (isJumping)
            if (vy < 0 && isJumping) { //
                // ใช้แรงโน้มถ่วงน้อย (ลอยตัวสูง)
                vy += (gravity * risingGravityMultiplier) * deltaTime; //
            } else {
                // ใช้แรงโน้มถ่วงปกติ (ร่วงเร็ว หรือ โดดเตี้ย)
                vy += gravity * deltaTime; //
            }

            // (ลบ } ปิดของ if (!isGrounded) ออกจากตรงนี้)

            // --- (จบส่วนแก้ไข) ---

            // 2. อัปเดตตำแหน่งตามแรงโน้มถ่วงและทิศทาง (เหมือนเดิม)
            switch (gravityDirection) {
                case DOWN -> {
                    x += velocityX * speed * deltaTime;
                    y += vy * deltaTime;
                }
                case UP -> {
                    x += velocityX * speed * deltaTime;
                    y -= vy * deltaTime;
                }
                case LEFT -> {
                    x -= vy * deltaTime;
                    y += velocityY * speed * deltaTime; // เคลื่อนที่บน/ล่าง
                }
                case RIGHT -> {
                    x += vy * deltaTime;
                    y += velocityY * speed * deltaTime;
                }
            }
        } else {
            // RED soul - เคลื่อนที่ (ต้องคูณ deltaTime ด้วย)
            x += velocityX * speed * deltaTime;
            y += velocityY * speed * deltaTime;
            this.vy = 0; // รีเซ็ตฟิสิกส์
        }

        updatePosition();
    }

    // (โค้ด setPlayerScale, getHitbox..., intersects... เหมือนเดิม) [cite: 263-311]
    public void setPlayerScale(double scale) {
        super.setScaleX(scale);
        super.setScaleY(scale);
    }

    public double getHitboxLeft() {
        return x - (hitboxWidth * getScaleX()) / 2;
    }

    public double getHitboxRight() {
        return x + (hitboxWidth * getScaleX()) / 2;
    }

    public double getHitboxTop() {
        return y - (hitboxHeight * getScaleY()) / 2;
    }

    public double getHitboxBottom() {
        return y + (hitboxHeight * getScaleY()) / 2;
    }

    public double getHitboxWidth() { return hitboxWidth * getScaleX(); }
    public double getHitboxHeight() { return hitboxHeight * getScaleY(); }

    public boolean intersects(Player other) {
        return !(this.getHitboxRight() < other.getHitboxLeft() ||
                this.getHitboxLeft() > other.getHitboxRight() ||
                this.getHitboxBottom() < other.getHitboxTop() ||
                this.getHitboxTop() > other.getHitboxBottom());
    }

    public boolean intersects(double rectX, double rectY, double rectWidth, double rectHeight) {
        return !(this.getHitboxRight() < rectX ||
                this.getHitboxLeft() > rectX + rectWidth ||
                this.getHitboxBottom() < rectY ||
                this.getHitboxTop() > rectY + rectHeight);
    }

    public void constrainToBounds(double minX, double minY, double maxX, double maxY) {
        double halfWidth = (hitboxWidth * getScaleX()) / 2;
        double halfHeight = (hitboxHeight * getScaleY()) / 2;

        if (x - halfWidth < minX) x = minX + halfWidth;
        if (x + halfWidth > maxX) x = maxX - halfWidth;
        if (y - halfHeight < minY) y = minY + halfHeight;
        if (y + halfHeight > maxY) y = maxY - halfHeight;
        updatePosition();
    }

    // (โค้ด constrainToBattleBox เหมือนเดิม)
    public void constrainToBattleBox(BattleBox box) {
        double halfWidth = (hitboxWidth * getScaleX()) / 2;
        double halfHeight = (hitboxHeight * getScaleY()) / 2;

        isGrounded = false; //

        if (currentColor == SoulColor.BLUE) {
            switch (gravityDirection) {
                case DOWN -> {
                    if (x - halfWidth < box.getLeftBoundary()) {
                        x = box.getLeftBoundary() + halfWidth;
                    }
                    if (x + halfWidth > box.getRightBoundary()) {
                        x = box.getRightBoundary() - halfWidth;
                    }
                    if (y - halfHeight < box.getTopBoundary()) {
                        y = box.getTopBoundary() + halfHeight;
                        vy = 0; // (แก้ไข)
                    }
                    if (y + halfHeight > box.getBottomBoundary()) {
                        y = box.getBottomBoundary() - halfHeight;
                        vy = 0; // (แก้ไข)
                        isGrounded = true; //
                        isJumping = false; //
                    }
                }
                case UP -> {
                    if (x - halfWidth < box.getLeftBoundary()) {
                        x = box.getLeftBoundary() + halfWidth;
                    }
                    if (x + halfWidth > box.getRightBoundary()) {
                        x = box.getRightBoundary() - halfWidth;
                    }
                    if (y - halfHeight < box.getTopBoundary()) {
                        y = box.getTopBoundary() + halfHeight;
                        vy = 0; // (แก้ไข)
                        isGrounded = true; //
                        isJumping = false; //
                    }
                    if (y + halfHeight > box.getBottomBoundary()) {
                        y = box.getBottomBoundary() - halfHeight;
                        vy = 0; // (แก้ไข)
                    }
                }
                case LEFT -> {
                    if (y - halfHeight < box.getTopBoundary()) {
                        y = box.getTopBoundary() + halfHeight;
                    }
                    if (y + halfHeight > box.getBottomBoundary()) {
                        y = box.getBottomBoundary() - halfHeight;
                    }
                    if (x - halfWidth < box.getLeftBoundary()) {
                        x = box.getLeftBoundary() + halfWidth;
                        vy = 0; // (แก้ไข)
                        isGrounded = true; //
                        isJumping = false; //
                    }
                    if (x + halfWidth > box.getRightBoundary()) {
                        x = box.getRightBoundary() - halfWidth;
                        vy = 0; // (แก้ไข)
                    }
                }
                case RIGHT -> {
                    if (y - halfHeight < box.getTopBoundary()) {
                        y = box.getTopBoundary() + halfHeight;
                    }
                    if (y + halfHeight > box.getBottomBoundary()) {
                        y = box.getBottomBoundary() - halfHeight;
                    }
                    if (x - halfWidth < box.getLeftBoundary()) {
                        x = box.getLeftBoundary() + halfWidth;
                        vy = 0; // (แก้ไข)
                    }
                    if (x + halfWidth > box.getRightBoundary()) {
                        x = box.getRightBoundary() - halfWidth;
                        vy = 0; // (แก้ไข)
                        isGrounded = true; //
                        isJumping = false; //
                    }
                }
            }
        } else {
            // (โค้ดของ Red Soul ไม่เปลี่ยนแปลง)
            if (x - halfWidth < box.getLeftBoundary()) {
                x = box.getLeftBoundary() + halfWidth;
            }
            if (x + halfWidth > box.getRightBoundary()) {
                x = box.getRightBoundary() - halfWidth;
            }
            if (y - halfHeight < box.getTopBoundary()) {
                y = box.getTopBoundary() + halfHeight;
            }
            if (y + halfHeight > box.getBottomBoundary()) {
                y = box.getBottomBoundary() - halfHeight;
            }
        }

        updatePosition();
    }

    public boolean isGrounded() { return isGrounded; }

    // (โค้ด setShowHitbox, printDebugInfo, isShowHitbox เหมือนเดิม)
    public void setShowHitbox(boolean show) {
        this.showHitbox = show;
        hitbox.setVisible(show);
    }

    public void printDebugInfo() {
        System.out.println("=== Player Debug Info ===");
        System.out.println("Position: (" + x + ", " + y + ")");
        System.out.println("Scale: (" + getScaleX() + ", " + getScaleY() + ")");
        System.out.println("Hitbox size: " + hitboxWidth + "x" + hitboxHeight);
        System.out.println("Calculated hitbox: " + getHitboxWidth() + "x" + getHitboxHeight());
        System.out.println("Hitbox bounds: L=" + getHitboxLeft() + " R=" + getHitboxRight()
                + " T=" + getHitboxTop() + " B=" + getHitboxBottom());
        System.out.println("======================");
    }

    public boolean isShowHitbox() { return showHitbox; }
}