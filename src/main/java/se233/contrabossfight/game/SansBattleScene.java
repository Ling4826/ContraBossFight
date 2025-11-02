package se233.contrabossfight.game;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import se233.contrabossfight.Model.Character.Player;
import se233.contrabossfight.Model.SansPackage.Sans;
import se233.contrabossfight.View.BattleBox;
import se233.contrabossfight.util.Logger;
import javafx.scene.input.KeyCode;
import javafx.scene.media.AudioClip;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import se233.contrabossfight.Model.Sprite.SansSpriteFactory;
import se233.contrabossfight.Model.Sprite.Base.AnimatedSprite;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

// (ลบ import se233.contrabossfight.game.AttackSequencer;)

public class SansBattleScene extends Group {
    record AttackData(double velocityX, String attackType) {}

    private int playerMaxHP ;
    private Text hpLabel;
    private Text hpValue;
    private Rectangle hpBarBackground;
    private Rectangle hpBarForeground;
    // --- ขยายสถานะการต่อสู้ ---
    public enum BattleState {
        SELECTING,      // 1. กำลังเลือก FIGHT, ACT
        SELECTING_FIGHT, // <--- เพิ่ม: กำลังเลือกเมนูย่อย FIGHT
        SELECTING_ACT,  // 2. กำลังเลือกเมนูย่อยใน ACT
        PLAYER_ATTACKING, // 3. กำลังโจมตี
        ATTACKING       // 4. กำลังหลบ
    }
    private AudioClip playerShootSound;
    private AudioClip sansHitSound;
    private AudioClip playerHurtSound;
    private boolean isPlayerDead = false;
    private boolean playerInvincible = false;
    private double invincibilityTimer = 0;
    // (ลบ private AttackSequencer attackSequencer;)

    public enum BattleResult { NONE, GOOD_ENDING, BAD_ENDING }
    private BattleResult battleResult = BattleResult.NONE;
    public BattleResult getBattleResult() { return battleResult; }
    private BattleState currentState = BattleState.SELECTING;
    // -------------------------

    private AudioClip selectSound; // <--- เพิ่มบรรทัดนี้
    private Sans sans;
    private Player playerHeart;
    private BattleBox battleBox;
    private Timeline attackTimeline;
    private int playerHP ;

    private BattleButton fightButton;
    private BattleButton actButton;
    private int currentSelection = 0; // 0 = FIGHT, 1 = ACT

    // --- แก้ไข/เพิ่ม Text สำหรับเมนูย่อย ---
    private Text actFriendText;
    private Text fightShootText; // <--- เพิ่ม "ยิง Sans"
    private Text fightSkipText;  // <--- เพิ่ม "Skip"
    private int currentSubMenuSelection = 0; // 0 = ตัวเลือกซ้าย, 1 = ตัวเลือกขวา


    private List<Node> activeAttacks = new ArrayList<>(); // (เปลี่ยนจาก Rectangle เป็น Node)
    private double attackMoveSpeed = -200.0; //
    private int attackPatternIndex = 0; //
    private static final String TYPE_WHITE_BONE = "WHITE";
    private static final String TYPE_BLUE_BONE = "BLUE";
    private static final String TYPE_BEAM = "BEAM"; // สำหรับ Blasters

    /** ตัวแปรเก็บไฟล์รูปภาพ (โหลดครั้งเดียว) */
    private Image boneWhiteImg, boneBlueImg;
    // ------------------------------------

    private boolean heartUp = false, heartDown = false, heartLeft = false, heartRight = false;

    public SansBattleScene(int startingHP, AudioClip selectSound, AudioClip playerShootSound, AudioClip sansHitSound, AudioClip playerHurtSound) {

        this.playerHP = startingHP;
        this.playerMaxHP = startingHP;
        this.battleBox = new BattleBox(200, 350, 400, 150);
        this.playerHeart = new Player(400, 425);
        this.playerHeart.setSoulColor(Player.SoulColor.RED);
        this.selectSound = selectSound;
        this.playerShootSound = playerShootSound;
        this.sansHitSound = sansHitSound;
        this.sans = new Sans();
        this.sans.setTranslateX(370);
        this.sans.setTranslateY(150);
        this.sans.setScaleX(2.5);
        this.sans.setScaleY(2.5);
        this.sans.startIdleAnimation();
        this.playerHurtSound = playerHurtSound;

        try {
            fightButton = new BattleButton(
                    "/se233/contrabossfight/images/fight.png",
                    "/se233/contrabossfight/images/fighthighlight.png"
            );
            actButton = new BattleButton(
                    "/se233/contrabossfight/images/act.png",
                    "/se233/contrabossfight/images/acthighlight.png"
            );

            double buttonY = 520;
            double centerX = 350;
            double spacing = 120;
            fightButton.setTranslateX(centerX - spacing);
            fightButton.setTranslateY(buttonY);
            actButton.setTranslateX(centerX + 100);
            actButton.setTranslateY(buttonY);
            double hpUI_X = centerX -50;
            double hpUI_Y = buttonY - 200;
            Font undertaleFont = Font.font("Determination Mono", 20); //

            hpLabel = new Text(hpUI_X, hpUI_Y + 15, "HP");
            hpLabel.setFont(undertaleFont);
            hpLabel.setFill(Color.WHITE);

            hpBarBackground = new Rectangle(hpUI_X + 35, hpUI_Y, 100, 20);
            hpBarBackground.setFill(Color.RED);

            hpBarForeground = new Rectangle(hpUI_X + 35, hpUI_Y, 100, 20);
            hpBarForeground.setFill(Color.LIMEGREEN);

            hpValue = new Text(hpUI_X + 145, hpUI_Y + 15, playerHP + " / " + playerMaxHP);
            hpValue.setFont(undertaleFont);
            hpValue.setFill(Color.WHITE);

            actFriendText = new Text(240, 380, "* Be Friends");
            actFriendText.setFont(undertaleFont);
            actFriendText.setFill(Color.WHITE);
            actFriendText.setVisible(false);

            fightShootText = new Text(240, 380, "* ยิง Sans");
            fightShootText.setFont(undertaleFont);
            fightShootText.setFill(Color.WHITE);
            fightShootText.setVisible(false); // ซ่อนไว้ก่อน

            fightSkipText = new Text(400, 380, "* Skip");
            fightSkipText.setFont(undertaleFont);
            fightSkipText.setFill(Color.WHITE);
            fightSkipText.setVisible(false); // ซ่อนไว้ก่อน

            this.getChildren().addAll(
                    this.sans, this.battleBox, this.playerHeart,
                    fightButton, actButton, actFriendText, fightShootText, fightSkipText,
                    hpLabel, hpBarBackground, hpBarForeground, hpValue
            );
            playerHeart.setVisible(false);


            try {
                // (ใช้ชื่อไฟล์ที่คุณอัปโหลด)
                String pathV = "/se233/contrabossfight/images/BoneSingle.png";
                String pathBV = "/se233/contrabossfight/images/BoneBlueSingle.png";

                boneWhiteImg = new Image(getClass().getResourceAsStream(pathV));
                boneBlueImg = new Image(getClass().getResourceAsStream(pathBV));

                if (boneWhiteImg.isError() || boneBlueImg.isError()) {
                    throw new IllegalArgumentException("Failed to load bone images.");
                }
            } catch (Exception e) {
                Logger.log(Logger.LogType.FATAL, "Failed to load bone images", e);
            }
            // --- (จบส่วนที่เพิ่ม) ---

        } catch (Exception e) {
            Logger.log(Logger.LogType.FATAL, "Failed to initialize battle components", e);
        }
    }
    private void playSound() {
        if (selectSound != null) {
            selectSound.play();
        }
    }
    public void startBattle() {
        currentState = BattleState.SELECTING;
        returnToPlayerTurn();
    }

    public void update(double deltaTime) {


        if (playerInvincible) {
            invincibilityTimer -= deltaTime;
            if (invincibilityTimer <= 0) {
                playerInvincible = false;
                playerHeart.setOpacity(1.0);
            }
        }

        if (currentState == BattleState.ATTACKING) { //
            playerHeart.setMovement(heartUp, heartDown, heartLeft, heartRight); //
            playerHeart.update(deltaTime); //
            playerHeart.constrainToBattleBox(battleBox); //

            // --- (เพิ่มโค้ดส่วนนี้) ---
            // อัปเดตและเช็คการชนของกระดูก
            Iterator<Node> iterator = activeAttacks.iterator(); // (แก้ไข)
            while (iterator.hasNext()) {
                Node attack = iterator.next(); // (แก้ไข)

                // (ถ้าไม่มี Data ให้ข้ามไปเลย)
                if (attack.getUserData() == null || !(attack.getUserData() instanceof AttackData)) {
                    continue;
                }

                AttackData data = (AttackData) attack.getUserData(); // (แก้ไข)

                // 1. ขยับกระดูก (ถ้ามีความเร็ว)
                double velocity = data.velocityX();
                if (velocity != 0) {
                    attack.setTranslateX(attack.getTranslateX() + velocity * deltaTime);
                }

                // 2. เช็คการชน (ใช้ getBoundsInParent() สำหรับ Node)
                if (!playerInvincible &&
                        playerHeart.intersects(attack.getBoundsInParent())) { // (แก้ไข)

                    String type = data.attackType(); // (แก้ไข)

                    if (TYPE_BLUE_BONE.equals(type)) { // (แก้ไข)
                        // --- นี่คือกระดูกสีฟ้า ---
                        boolean isPlayerMoving = heartUp || heartDown || heartLeft || heartRight;
                        if (isPlayerMoving) {
                            playerTakeDamage(10);
                        }
                    } else {
                        // --- กระดูกสีขาว (TYPE_WHITE_BONE) หรือ บีม (TYPE_BEAM) ---
                        playerTakeDamage(10);
                    }
                }

                // 3. ลบกระดูกที่ออกนอกจอ (ด้านซ้าย หรือ ด้านขวา)
                double attackX = attack.getTranslateX();
                // (ใช้ getBoundsInParent() เพื่อหาความกว้างที่แท้จริง)
                double attackWidth = attack.getBoundsInParent().getWidth();

                if (velocity < 0 && (attackX + attackWidth < 0)) { //
                    this.getChildren().remove(attack);
                    iterator.remove(); //
                } else if (velocity > 0 && (attackX > 800)) { // 800 = ความกว้างจอ
                    this.getChildren().remove(attack);
                    iterator.remove();
                }
            }
            // --- (จบส่วนที่เพิ่ม) ---
        }
    }

    // --- ซ่อน/แสดง ปุ่มหลัก ---
    private void hideMainButtons() {
        fightButton.setVisible(false);
        actButton.setVisible(false);
    }
    private void showMainButtons() {
        fightButton.setVisible(true);
        actButton.setVisible(true);
        updateButtonSelection();
    }

    // --- ซ่อน/แสดง เมนูย่อย ACT ---
    private void showActMenu() {
        actFriendText.setVisible(true);
        updateActMenuHighlight();
    }
    private void hideActMenu() {
        actFriendText.setVisible(false);
    }
    private void updateActMenuHighlight() {
        actFriendText.setFill(Color.YELLOW);
    }
    public boolean isPlayerDead() { // <--- (เพิ่มใหม่: เมธอดสำหรับตรวจสอบสถานะ)
        return isPlayerDead;
    }

    private void showFightMenu() {
        fightShootText.setVisible(true);
        fightSkipText.setVisible(true);
        currentSubMenuSelection = 0;
        updateFightMenuHighlight();
    }
    private void hideFightMenu() {
        fightShootText.setVisible(false);
        fightSkipText.setVisible(false);
    }
    private void updateFightMenuHighlight() {
        fightShootText.setFill(currentSubMenuSelection == 0 ? Color.YELLOW : Color.WHITE);
        fightSkipText.setFill(currentSubMenuSelection == 1 ? Color.YELLOW : Color.WHITE);
    }

    private void updateButtonSelection() {
        if (fightButton == null || actButton == null) return;
        fightButton.setSelected(currentSelection == 0);
        actButton.setSelected(currentSelection == 1);
    }

    /**
     * ยืนยันการเลือก (FIGHT/ACT)
     */
    private void confirmSelection() {
        hideMainButtons();

        if (currentSelection == 0) { // FIGHT
            Logger.log(Logger.LogType.INFO, "Player selected FIGHT.");
            currentState = BattleState.SELECTING_FIGHT;
            showFightMenu();
        } else { // ACT
            Logger.log(Logger.LogType.INFO, "Player selected ACT.");
            currentState = BattleState.SELECTING_ACT;
            showActMenu();
        }
    }

    private void confirmFightSelection() {
        hideFightMenu(); //
        playSound(); // (1. เสียงเลือกเมนู)

        if (currentSubMenuSelection == 0) { // 1. "ยิง Sans"
            Logger.log(Logger.LogType.INFO, "Player selected FIGHT. Loading attack " + attackPatternIndex); //
            if (playerShootSound != null) {
                playerShootSound.play();
            }
            // --- (เพิ่ม/แก้ไข: ให้ Sans หลบก่อน) ---
            if (this.sans != null) {
                // (เรียกอนิเมชันหลบ และใส่โค้ดเดิมของการโจมตีเข้าไปใน Callback)
                this.sans.startDodgeSequence(() -> {
                    // --- โค้ดเดิมที่เคยถูกเรียกทันที ถูกย้ายมาที่นี่ ---
                    currentState = BattleState.ATTACKING; //
                    playerHeart.setVisible(true); //

                    if (attackTimeline != null) {
                        attackTimeline.stop();
                    }

                    Timeline nextAttack = getNextAttackPattern();

                    if (nextAttack == null) {
                        // --- ด่านหมดแล้ว: ผู้เล่น "ยิงโดน" และ "ชนะ" ---
                        Logger.log(Logger.LogType.FATAL, "FINAL HIT! Player wins (Good Ending).");

                        if (sansHitSound != null) {
                            sansHitSound.play();
                        }
                        currentState = BattleState.PLAYER_ATTACKING;
                        this.sans.startHitSequence(this::triggerBadEnding);
                    } else {
                        // --- ยังมีด่านเหลือ: เล่นด่านต่อไป ---
                        attackTimeline = nextAttack;
                        attackTimeline.play();
                    }
                    // ----------------------------------------------------
                });
            } else {
                // กรณี Sans เป็น null (เพื่อป้องกัน NullPointerException)
                Logger.log(Logger.LogType.WARN, "Sans object is null, skipping dodge sequence.");
            }
            // --- (จบส่วนแก้ไข) ---

        } else { // 2. "Skip" (ส่วนนี้เหมือนเดิม)
            Logger.log(Logger.LogType.WARN, "Player selected FIGHT -> Skip (FORCING BAD ENDING)"); //
            if (sansHitSound != null) { //
                sansHitSound.play(); //
            }
            currentState = BattleState.PLAYER_ATTACKING; //
            this.sans.startHitSequence(this::triggerBadEnding); //
        }
    }


    private void confirmActSelection() {
        hideActMenu();
        playSound();

        Logger.log(Logger.LogType.INFO, "Player selected ACT -> Be Friends");

        this.battleResult = BattleResult.GOOD_ENDING;
        this.currentState = BattleState.ATTACKING;
    }

    public Sans getSans() {
        return this.sans;
    }

    /** (Helper) ให้ Sans เรียก */
    public void triggerBadEnding() {
        this.battleResult = BattleResult.BAD_ENDING;
    }
    public BattleBox getBattleBox() {
        return this.battleBox;
    }
    public void resetBattleBox() {
        battleBox.setPosition(200, 350);
        battleBox.setSize(400, 150);
    }
    public void returnToPlayerTurn() {
        sans.startIdleAnimation();
        currentState = BattleState.SELECTING;
        setHeartColor(Player.SoulColor.RED); //
        showMainButtons(); //
        hideFightMenu(); //
        hideActMenu(); //


        clearAllBones();
        // --- (จบส่วนที่เพิ่ม) ---

        resetBattleBox(); //

        playerHeart.setVisible(false); //
        // ... (โค้ดที่เหลือ) ...
    }


    public void handleKeyPressed(KeyEvent event) {
        switch (currentState) {
            case SELECTING: // --- 1. โหมดเลือกเมนูหลัก ---
                switch (event.getCode()) {
                    case A: case LEFT:
                        currentSelection = 0; // FIGHT
                        updateButtonSelection();
                        playSound();
                        break;
                    case D: case RIGHT:
                        currentSelection = 1; // ACT
                        updateButtonSelection();
                        playSound();
                        break;
                    case J:
                        confirmSelection();
                        playSound();
                        break;
                }
                break;

            case SELECTING_FIGHT: // --- 2. โหมดเลือกเมนูย่อย FIGHT ---
                switch (event.getCode()) {
                    case A: case LEFT:
                        currentSubMenuSelection = 0; // ยิง Sans
                        updateFightMenuHighlight();
                        playSound();
                        break;
                    case D: case RIGHT:
                        currentSubMenuSelection = 1; // Skip
                        updateFightMenuHighlight();
                        playSound();
                        break;
                    case J:
                        confirmFightSelection();
                        playSound();
                        break;
                    case K:
                        returnToPlayerTurn();
                        playSound();
                        break;
                }
                break;

            case SELECTING_ACT: // --- 3. โหมดเลือกเมนูย่อย ACT ---
                switch (event.getCode()) {
                    case J:
                        confirmActSelection();
                        playSound();
                        break;
                    case K:
                        returnToPlayerTurn();
                        playSound();
                        break;
                }
                break;

            case PLAYER_ATTACKING: // --- 4. โหมดโจมตี ---
                // (ว่าง)
                break;

            case ATTACKING: // --- 5. โหมดหลบ (นี่คือส่วนที่แก้ไข) ---
                if (playerHeart.getCurrentColor() == Player.SoulColor.RED) {
                    // --- ถ้าหัวใจสีแดง (เหมือนเดิม) ---
                    switch (event.getCode()) {
                        case W: case UP: heartUp = true; break;
                        case S: case DOWN: heartDown = true; break;
                        case A: case LEFT: heartLeft = true; break;
                        case D: case RIGHT: heartRight = true; break;
                    }
                } else {
                    // --- ถ้าหัวใจสีฟ้า (โลจิกใหม่) ---
                    Player.GravityDirection dir = playerHeart.getGravityDirection(); //
                    switch (dir) {
                        case DOWN: //
                            if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) heartLeft = true;
                            if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) heartRight = true;
                            if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) playerHeart.startJump(); //
                            break;
                        case UP: //
                            if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) heartLeft = true;
                            if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) heartRight = true;
                            if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) playerHeart.startJump(); //
                            break;
                        case LEFT: //
                            if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) heartUp = true;
                            if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) heartDown = true;
                            if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) playerHeart.startJump(); //
                            break;
                        case RIGHT: //
                            if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) heartUp = true;
                            if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) heartDown = true;
                            if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) playerHeart.startJump(); //
                            break;
                    }
                }
                break;

        }
    }


    /**
     * เริ่มตาของ Sans (เมธอดนี้ไม่ได้ถูกเรียกใช้แล้ว แต่แก้ไขไว้กันพัง)
     */
    public void startEnemyAttack() {
        currentState = BattleState.ATTACKING;
        playerHeart.setVisible(true);

        if (attackTimeline != null) {
            attackTimeline.stop();
        }

        // --- (แก้ไข) attackSequencer ถูกลบไปแล้ว ---
        // (สร้าง Timeline เปล่าๆ 1 วินาที (ชั่วคราว) กันเกมพัง)
        Logger.log(Logger.LogType.INFO, "AttackSequencer removed. Using 1s placeholder attack.");
        attackTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> returnToPlayerTurn())
        );
        // ---------------------------------------------

        attackTimeline.play();
    }


    public void setHeartColor(Player.SoulColor color) {
        if (playerHeart != null) {
            playerHeart.setSoulColor(color);

            if (color == Player.SoulColor.BLUE) {
                playerHeart.setGravityDirection(Player.GravityDirection.DOWN);
            }
        }
    }



    private void updateHPDisplay() {
        hpValue.setText(playerHP + " / " + playerMaxHP);
        double hpPercent = (double) playerHP / playerMaxHP;
        hpBarForeground.setWidth(hpBarBackground.getWidth() * hpPercent);
    }

    /**
     * สั่งให้ผู้เล่น (หัวใจ) ได้รับความเสียหาย
     */
    public void playerTakeDamage(int amount) {
        if (playerInvincible) return; // ไม่โดน ถ้าเป็นอมตะ

        this.playerHP -= amount;
        if (this.playerHP < 0) this.playerHP = 0;

        updateHPDisplay(); // อัปเดตหน้าจอ
        Logger.log(Logger.LogType.WARN, "Player Hit! HP left: " + this.playerHP);

        if (playerHurtSound != null) {
            playerHurtSound.play();
        }
        if (this.playerHP <= 0) {
            this.isPlayerDead = true;
            Logger.log(Logger.LogType.FATAL, "Player is Dead! Initiating Game Over.");
        }
        playerInvincible = true;
        invincibilityTimer = 0.1; // อมตะ 1 วินาที

        playerHeart.setOpacity(0.5);
    }
    public void handleKeyReleased(KeyEvent event) {
        if (currentState == BattleState.ATTACKING) {

            if (playerHeart.getCurrentColor() == Player.SoulColor.RED) {
                // --- ถ้าหัวใจสีแดง (เหมือนเดิม) ---
                switch (event.getCode()) {
                    case W: case UP: heartUp = false; break;
                    case S: case DOWN: heartDown = false; break;
                    case A: case LEFT: heartLeft = false; break;
                    case D: case RIGHT: heartRight = false; break;
                }
            } else {
                // --- ถ้าหัวใจสีฟ้า (โลจิกใหม่) ---
                Player.GravityDirection dir = playerHeart.getGravityDirection(); //
                switch (dir) {
                    case DOWN: //
                        if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) heartLeft = false;
                        if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) heartRight = false;
                        if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) playerHeart.releaseJump(); //
                        break;
                    case UP: //
                        if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) heartLeft = false;
                        if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) heartRight = false;
                        if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) playerHeart.releaseJump(); //
                        break;
                    case LEFT: //
                        if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) heartUp = false;
                        if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) heartDown = false;
                        if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) playerHeart.releaseJump(); //
                        break;
                    case RIGHT: //
                        if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) heartUp = false;
                        if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) heartDown = false;
                        if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) playerHeart.releaseJump(); //
                        break;
                }
            }

            // (คำสั่ง setMovement นี้ ต้องอยู่นอกสุด)
            playerHeart.setMovement(heartUp, heartDown, heartLeft, heartRight); //
        }
    }
    public boolean isBattleOver() {
        return battleResult != BattleResult.NONE;
    }
    /** (Helper) */
    public Player getPlayerHeart() {
        return this.playerHeart;
    }



    // (แก้ไข) เพิ่ม String boneType, String side
    private void spawnBoneGapAttack(double gapCenterY, double gapHeight, double boneWidth, String boneType, String side) {
        double boxTop = battleBox.getTopBoundary();
        double boxBottom = battleBox.getBottomBoundary();
        double boxRight = battleBox.getRightBoundary();
        double boxLeft = battleBox.getLeftBoundary();

        double gapTop = gapCenterY - (gapHeight / 2);
        double gapBottom = gapCenterY + (gapHeight / 2);
        double topBoneHeight = gapTop - boxTop;
        double bottomBoneHeight = boxBottom - gapBottom;

        // (แก้ไข) เลือกรูปภาพ
        Image img = (boneType.equals(TYPE_BLUE_BONE)) ? boneBlueImg : boneWhiteImg;

        if (side.equals("RIGHT") || side.equals("BOTH")) {
            double speed = attackMoveSpeed;
            if (topBoneHeight > 5) {
                // (แก้ไข) สร้าง ImageView
                ImageView topBoneR = new ImageView(img);
                topBoneR.setFitWidth(boneWidth);
                topBoneR.setFitHeight(topBoneHeight);
                topBoneR.setPreserveRatio(false); // (ยืดรูปให้พอดี)
                topBoneR.setTranslateX(boxRight);
                topBoneR.setTranslateY(boxTop);
                topBoneR.setUserData(new AttackData(speed, boneType)); // (แก้ไข)
                activeAttacks.add(topBoneR); // (แก้ไข)
                this.getChildren().add(topBoneR);
            }
            if (bottomBoneHeight > 5) {
                // (แก้ไข) สร้าง ImageView
                ImageView bottomBoneR = new ImageView(img);
                bottomBoneR.setFitWidth(boneWidth);
                bottomBoneR.setFitHeight(bottomBoneHeight);
                bottomBoneR.setPreserveRatio(false);
                bottomBoneR.setTranslateX(boxRight);
                bottomBoneR.setTranslateY(gapBottom);
                bottomBoneR.setUserData(new AttackData(speed, boneType)); // (แก้ไข)
                activeAttacks.add(bottomBoneR); // (แก้ไข)
                this.getChildren().add(bottomBoneR);
            }
        }
        if (side.equals("LEFT") || side.equals("BOTH")) {
            double speed = -attackMoveSpeed;
            if (topBoneHeight > 5) {
                // (แก้ไข) สร้าง ImageView
                ImageView topBoneL = new ImageView(img);
                topBoneL.setFitWidth(boneWidth);
                topBoneL.setFitHeight(topBoneHeight);
                topBoneL.setPreserveRatio(false);
                topBoneL.setTranslateX(boxLeft - boneWidth);
                topBoneL.setTranslateY(boxTop);
                topBoneL.setUserData(new AttackData(speed, boneType)); // (แก้ไข)
                activeAttacks.add(topBoneL); // (แก้ไข)
                this.getChildren().add(topBoneL);
            }
            if (bottomBoneHeight > 5) {
                // (แก้ไข) สร้าง ImageView
                ImageView bottomBoneL = new ImageView(img);
                bottomBoneL.setFitWidth(boneWidth);
                bottomBoneL.setFitHeight(bottomBoneHeight);
                bottomBoneL.setPreserveRatio(false);
                bottomBoneL.setTranslateX(boxLeft - boneWidth);
                bottomBoneL.setTranslateY(gapBottom);
                bottomBoneL.setUserData(new AttackData(speed, boneType)); // (แก้ไข)
                activeAttacks.add(bottomBoneL); // (แก้ไข)
                this.getChildren().add(bottomBoneL);
            }
        }
    }
    /**
     * (ใหม่) สร้างกระดูกที่โผล่จากพื้นหรือเพดาน (เหมือนในรูป)
     * @param boneX ตำแหน่ง X ที่จะสร้างกระดูก
     * @param boneHeight ความสูงของกระดูก
     * @param boneWidth ความกว้างของกระดูก (จะเหมือนกระดูกเคลื่อนที่)
     * @param fromCeiling true ถ้าสร้างจากเพดาน, false ถ้าสร้างจากพื้น
     */
    // (แก้ไข) เพิ่ม String boneType
    private void spawnFloorCeilingBone(double boneX, double boneHeight, double boneWidth, boolean fromCeiling, double velocityX, String boneType) {
        double boxTop = battleBox.getTopBoundary();
        double boxBottom = battleBox.getBottomBoundary();

        // (แก้ไข) เลือกรูปภาพ
        Image img = (boneType.equals(TYPE_BLUE_BONE)) ? boneBlueImg : boneWhiteImg;
        ImageView bone = new ImageView(img);
        bone.setFitWidth(boneWidth);
        bone.setFitHeight(boneHeight);
        bone.setPreserveRatio(false);

        bone.setTranslateX(boneX);
        if (fromCeiling) {
            bone.setTranslateY(boxTop);
        } else {
            bone.setTranslateY(boxBottom - boneHeight);
        }

        bone.setUserData(new AttackData(velocityX, boneType)); // (แก้ไข)
        activeAttacks.add(bone); // (แก้ไข)
        this.getChildren().add(bone); // (แก้ไข)
    }

    /**
     * (ใหม่) ด่านที่ 1: กระดูกช่องว่าง 4 ระลอก
     * @return Timeline ของการโจมตี
     */
    private Timeline buildAttack_BoneGaps() {
        // ... (โค้ดตั้งค่าเหมือนเดิม) ...
        Logger.log(Logger.LogType.INFO, "--- Building: Attack_BoneGaps (Pattern 0) ---");

        setHeartColor(Player.SoulColor.BLUE);
        playerHeart.setGravityByNumber(1);
        double gapCenterY = 450;
        double gapHeight = 30;
        double boneWidth = 7;

        Timeline t = new Timeline(
                // (แก้ไข) เพิ่ม TYPE_WHITE และ "BOTH"
                new KeyFrame(Duration.seconds(1.0), e -> spawnBoneGapAttack(gapCenterY, gapHeight, boneWidth, TYPE_WHITE_BONE, "BOTH")),
                new KeyFrame(Duration.seconds(2.0), e -> spawnBoneGapAttack(gapCenterY, gapHeight, boneWidth, TYPE_WHITE_BONE, "BOTH")),
                new KeyFrame(Duration.seconds(3.0), e -> spawnBoneGapAttack(gapCenterY, gapHeight, boneWidth, TYPE_WHITE_BONE, "BOTH")),
                new KeyFrame(Duration.seconds(4.0), e -> spawnBoneGapAttack(gapCenterY, gapHeight, boneWidth, TYPE_WHITE_BONE, "BOTH")),
                new KeyFrame(Duration.seconds(5.0), e -> spawnBoneGapAttack(gapCenterY, gapHeight, boneWidth, TYPE_WHITE_BONE, "BOTH")),
                new KeyFrame(Duration.seconds(6.0), e -> spawnBoneGapAttack(gapCenterY, gapHeight, boneWidth, TYPE_WHITE_BONE, "BOTH")),
                new KeyFrame(Duration.seconds(7.0), e -> spawnBoneGapAttack(gapCenterY, gapHeight, boneWidth, TYPE_WHITE_BONE, "BOTH")),
                new KeyFrame(Duration.seconds(9.0), e -> {
                    Logger.log(Logger.LogType.INFO, "Attack finished. Returning to turn.");
                    returnToPlayerTurn();
                })
        );
        return t;
    }


    private Timeline buildAttack_GapsAndFloor() {
        // ... (โค้ดตั้งค่าเหมือนเดิม) ...
        Logger.log(Logger.LogType.INFO, "--- Building: Attack_PlatformRide (Pattern 1) ---");

        setHeartColor(Player.SoulColor.BLUE);
        playerHeart.setGravityByNumber(1);
        double boneWidth = 10;
        double floorBoneHeight = 40;
        double ceilingBoneHeight = 100;
        double boxLeft = battleBox.getLeftBoundary();
        double boxRight = battleBox.getRightBoundary();
        double leftSpeed = -100;
        double rightSpeed = 100;
        double spacing = 80.0;

        Timeline t = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    // (แก้ไข) เพิ่ม TYPE_WHITE_BONE
                    spawnFloorCeilingBone(boxLeft - boneWidth, floorBoneHeight, boneWidth, false, rightSpeed, TYPE_WHITE_BONE);
                    spawnFloorCeilingBone(boxLeft - boneWidth - spacing, floorBoneHeight, boneWidth, false, rightSpeed, TYPE_WHITE_BONE);
                    spawnFloorCeilingBone(boxLeft - boneWidth - (spacing*2), floorBoneHeight, boneWidth, false, rightSpeed, TYPE_WHITE_BONE);
                    spawnFloorCeilingBone(boxLeft - boneWidth - (spacing*3), floorBoneHeight, boneWidth, false, rightSpeed, TYPE_WHITE_BONE);
                    spawnFloorCeilingBone(boxLeft - boneWidth - (spacing*4), floorBoneHeight, boneWidth, false, rightSpeed, TYPE_WHITE_BONE);
                }),
                new KeyFrame(Duration.seconds(1.0), e -> { // (แก้เวลาเป็น 1.5)
                    // (แก้ไข) เพิ่ม TYPE_WHITE_BONE
                    spawnFloorCeilingBone(boxRight, ceilingBoneHeight, boneWidth, true, leftSpeed, TYPE_WHITE_BONE);
                    spawnFloorCeilingBone(boxRight + spacing, ceilingBoneHeight, boneWidth, true, leftSpeed, TYPE_WHITE_BONE);
                    spawnFloorCeilingBone(boxRight + (spacing*2), ceilingBoneHeight, boneWidth, true, leftSpeed, TYPE_WHITE_BONE);
                    spawnFloorCeilingBone(boxLeft +   (spacing*3), ceilingBoneHeight, boneWidth, true, leftSpeed, TYPE_WHITE_BONE);
                    spawnFloorCeilingBone(boxLeft +   (spacing*4), ceilingBoneHeight, boneWidth, true, leftSpeed, TYPE_WHITE_BONE);
                }),
                // ... (KeyFrame จบ) ...
                new KeyFrame(Duration.seconds(9), e -> {
                    Logger.log(Logger.LogType.INFO, "Attack finished. Returning to turn.");
                    returnToPlayerTurn();
                })
        );
        return t;
    }

    /**
     * (ใหม่) ตัวเลือกด่าน (Sequencer)
     * @return Timeline ของด่านถัดไป
     */
    private Timeline getNextAttackPattern() {
        Timeline selectedAttack;

        switch (attackPatternIndex) {
            case 0:
                selectedAttack = buildAttack_BoneGaps();
                break;
            case 1:
                selectedAttack = buildAttack_GapsAndFloor();
                break;
            case 2:
                selectedAttack =  buildAttack_AlternatingGaps();
                break;
            case 3:
                selectedAttack = buildAttack_SlamAndFloor();
                break;
            case 4:
                // --- (ด่านยิงวงกลม) ---
                selectedAttack = buildAttack_BlasterCircle();
                break;
            default:
                Logger.log(Logger.LogType.INFO, "Attack patterns finished!");
                selectedAttack = null;
                break;
        }
        attackPatternIndex++;
        return selectedAttack;
    }



    private Timeline buildAttack_AlternatingGaps() {
        // ... (โค้ดตั้งค่าเหมือนเดิม) ...
        Logger.log(Logger.LogType.INFO, "--- Building: Attack_AlternatingGaps (Pattern 3) ---");

        setHeartColor(Player.SoulColor.BLUE);
        playerHeart.setGravityByNumber(1);
        double boneWidth = 7;
        double floorBoneHeight = 40;
        double floorBoneHeight1 = 120;
        double boxLeft = battleBox.getLeftBoundary();
        double boxRight = battleBox.getRightBoundary();
        double leftSpeed = -attackMoveSpeed +100;
        double rightSpeed = attackMoveSpeed -100;

        Timeline t = new Timeline(
                // (แก้ไข) เรียก spawnFloorCeilingBone เสมอ
                new KeyFrame(Duration.seconds(1), e ->
                        spawnFloorCeilingBone(boxLeft - boneWidth, floorBoneHeight, boneWidth, false, leftSpeed, TYPE_WHITE_BONE)), //
                new KeyFrame(Duration.seconds(2), e ->
                        spawnFloorCeilingBone(boxRight, floorBoneHeight1, boneWidth, false, rightSpeed, TYPE_BLUE_BONE)), // (แก้ไข)
                new KeyFrame(Duration.seconds(3), e ->
                        spawnFloorCeilingBone(boxLeft - boneWidth, floorBoneHeight, boneWidth, false, leftSpeed, TYPE_WHITE_BONE)), //
                new KeyFrame(Duration.seconds(4), e ->
                        spawnFloorCeilingBone(boxRight, floorBoneHeight1, boneWidth, false, rightSpeed, TYPE_BLUE_BONE)), // (แก้ไข)
                new KeyFrame(Duration.seconds(5), e ->
                        spawnFloorCeilingBone(boxLeft - boneWidth, floorBoneHeight1, boneWidth, false, leftSpeed, TYPE_BLUE_BONE)), // (แก้ไข)
                new KeyFrame(Duration.seconds(6), e ->
                        spawnFloorCeilingBone(boxRight, floorBoneHeight, boneWidth, false, rightSpeed, TYPE_WHITE_BONE)), //
                new KeyFrame(Duration.seconds(7), e ->
                        spawnFloorCeilingBone(boxLeft - boneWidth, floorBoneHeight1, boneWidth, false, leftSpeed, TYPE_BLUE_BONE)), // (แก้ไข)
                new KeyFrame(Duration.seconds(8), e ->
                        spawnFloorCeilingBone(boxRight, floorBoneHeight, boneWidth, false, rightSpeed, TYPE_WHITE_BONE)), //
                new KeyFrame(Duration.seconds(9), e ->
                        spawnFloorCeilingBone(boxRight, floorBoneHeight1, boneWidth, false, rightSpeed, TYPE_BLUE_BONE)), // (แก้ไข)
                new KeyFrame(Duration.seconds(10), e ->
                        spawnFloorCeilingBone(boxLeft - boneWidth, floorBoneHeight1, boneWidth, false, leftSpeed, TYPE_BLUE_BONE)), // (แก้ไข)
                new KeyFrame(Duration.seconds(12), e -> {
                    Logger.log(Logger.LogType.INFO, "Attack finished. Returning to turn.");
                    returnToPlayerTurn();
                })
        );
        return t;
    }


    private void spawnWarningBox(double x, double y, double width, double height, double duration) {
        Rectangle warningBox = new Rectangle(x, y, width, height);
        warningBox.setFill(Color.RED); // (แก้ไข) เตือนเป็นสีแดง
        warningBox.setOpacity(0.7);

        this.getChildren().add(warningBox);

        // สร้าง Timeline สั้นๆ เพื่อลบมันออก
        Timeline cleanup = new Timeline(
                new KeyFrame(Duration.seconds(duration), e -> {
                    this.getChildren().remove(warningBox);
                })
        );
        cleanup.play();
    }
    private Timeline buildAttack_SlamAndFloor() {
        Logger.log(Logger.LogType.INFO, "--- Building: Attack_SlamAndFloor (Pattern 3) ---");

        // 1. ตั้งค่าสภาพแวดล้อม (กล่องแคบ)
        battleBox.setSize(200, 200);
        battleBox.setPosition(300, 350);

        // 2. กำหนดค่าการโจมตี
        // (กระดูกพื้น)
        double boneWidthVertical = battleBox.getWidth();
        double boneHeightVertical = 40;
        // (กระดูกข้าง)
        double boneWidthHorizontal = 40; // (กระดูกข้างๆ กว้าง 40)
        double boneHeightHorizontal = battleBox.getHeight(); // (กระดูกข้างๆ สูงเต็มกล่อง)

        double boxLeft = battleBox.getLeftBoundary();
        double boxBottom = battleBox.getBottomBoundary();
        double boxRight = battleBox.getRightBoundary();
        double boxTop = battleBox.getTopBoundary();

        double stationarySpeed = 0.0;

        // 3. สร้าง Timeline (แก้ไข)
        Timeline t = new Timeline(
                // --- Part 1: (Slam Down) (โค้ดเดิมของคุณ) ---
                new KeyFrame(Duration.seconds(0.1), e -> sans.startAttack("down")),
                new KeyFrame(Duration.seconds(0.2), e -> {
                    setHeartColor(Player.SoulColor.BLUE);
                    playerHeart.setGravityByNumber(1); // DOWN
                    double floorY = boxBottom - (playerHeart.getHitboxHeight() / 2.0);
                    playerHeart.setY(floorY);
                }),
                new KeyFrame(Duration.seconds(0.3), e -> {
                    spawnWarningBox(boxLeft, boxBottom - 10, boneWidthVertical, 10, 1.0);
                }),
                new KeyFrame(Duration.seconds(1.0), e -> {
                    spawnFloorCeilingBone(boxLeft, boneHeightVertical, boneWidthVertical, false, stationarySpeed, TYPE_WHITE_BONE); //
                }),
                new KeyFrame(Duration.seconds(1.5), e -> {
                    clearAllBones();
                }),

                new KeyFrame(Duration.seconds(2.0), e -> {
                    // 1. Sans ปัดมือ (ขวา)
                    sans.startAttack("right");
                }),
                new KeyFrame(Duration.seconds(2.1), e -> {
                    playerHeart.setGravityByNumber(4);

                    double rightWallX = boxRight - (playerHeart.getHitboxWidth() / 2.0);
                    playerHeart.setX(rightWallX);
                }),
                new KeyFrame(Duration.seconds(2.2), e -> {
                    // 3. เตือน "ข้างๆ" (ที่ผนังซ้าย, เตือน 0.7 วิ)
                    spawnWarningBox(boxRight - 10, boxTop, 10, boneHeightHorizontal, 0.7);
                }),
                new KeyFrame(Duration.seconds(2.9), e -> {
                    // 4. สร้างกระดูก "ข้างๆ"
                    spawnSideBone(boxTop, boneWidthHorizontal, boneHeightHorizontal, false, TYPE_WHITE_BONE); // (false = fromRight)
                }),
                new KeyFrame(Duration.seconds(3.4), e -> {
                    // 5. ลบกระดูกข้าง (0.5 วิ)
                    clearAllBones();
                }),

                new KeyFrame(Duration.seconds(4.0), e -> { // (หน่วงเวลาจบ)
                    Logger.log(Logger.LogType.INFO, "Attack finished. Returning to turn.");
                    returnToPlayerTurn();
                })
        );
        return t;
    }
    /**
     * (ใหม่) ลบกระดูกทั้งหมดออกจากจอและออกจาก List
     */
    private void clearAllBones() {
        Iterator<Node> iterator = activeAttacks.iterator(); // (แก้ไข)
        while (iterator.hasNext()) {
            Node attack = iterator.next(); // (แก้ไข)
            // (เราลบเฉพาะที่มี AttackData, ไม่ลบ WarningBox)
            if (attack.getUserData() != null && (attack.getUserData() instanceof AttackData)) {
                this.getChildren().remove(attack);
                iterator.remove();
            }
        }
    }
    /**
     * (ใหม่) สร้างกระดูกที่โผล่จาก "ด้านข้าง" (แนวนอน)
     * @param boneY ตำแหน่ง Y ที่จะสร้างกระดูก
     * @param boneWidth ความกว้าง (ที่ยื่นออกมา)
     * @param boneHeight ความสูง (ความหนา)
     * @param fromLeft true ถ้าสร้างจากซ้าย, false ถ้าสร้างจากขวา
     */
    // (แก้ไข) เพิ่ม String boneType
    private void spawnSideBone(double boneY, double boneWidth, double boneHeight, boolean fromLeft, String boneType) {
        double boxLeft = battleBox.getLeftBoundary();
        double boxRight = battleBox.getRightBoundary();

        // (แก้ไข) เลือกรูปภาพ (เราจะใช้รูปแนวตั้งและยืดมัน)
        Image img = (boneType.equals(TYPE_BLUE_BONE)) ? boneBlueImg : boneWhiteImg;
        ImageView bone = new ImageView(img);
        bone.setFitWidth(boneWidth);
        bone.setFitHeight(boneHeight);
        bone.setPreserveRatio(false);

        bone.setTranslateY(boneY);
        if (fromLeft) {
            bone.setTranslateX(boxLeft);
        } else {
            bone.setTranslateX(boxRight - boneWidth);
        }

        bone.setUserData(new AttackData(0.0, boneType)); // (แก้ไข)
        activeAttacks.add(bone); // (แก้ไข)
        this.getChildren().add(bone); // (แก้ไข)
    }

    private void spawnWarningBeam(double x, double y, double width, double height, double rotation, double duration) {
        Rectangle warningBox = new Rectangle(width, height);
        warningBox.setFill(Color.RED);
        warningBox.setOpacity(0.7);
        warningBox.setTranslateX(x);
        warningBox.setTranslateY(y);
        warningBox.setRotate(rotation);

        this.getChildren().add(warningBox);

        Timeline cleanup = new Timeline(
                new KeyFrame(Duration.seconds(duration), e -> {
                    this.getChildren().remove(warningBox);
                })
        );
        cleanup.play();
    }

    private void spawnBlasterBeam(double x, double y, double width, double height, double rotation, double duration) {
        Rectangle beam = new Rectangle(width, height, Color.WHITE);
        // ... (โค้ด setTranslate/Rotate เหมือนเดิม) ...
        beam.setTranslateX(x);
        beam.setTranslateY(y);
        beam.setRotate(rotation);

        beam.setUserData(new AttackData(0.0, TYPE_BEAM)); // (แก้ไข)
        beam.setStroke(Color.RED);
        beam.setStrokeWidth(1);

        activeAttacks.add(beam); // (แก้ไข)
        this.getChildren().add(beam); // (แก้ไข)

        // ... (Cleanup Timeline เหมือนเดิม) ...
        Timeline cleanup = new Timeline(
                new KeyFrame(Duration.seconds(duration), e -> {
                    this.getChildren().remove(beam);
                    activeAttacks.remove(beam); // (สำคัญ)
                })
        );
        cleanup.play();
    }
    /**
     * (ใหม่) ด่านที่ 5: "มังกร" (Blasters) ยิงเป็นวงกลม
     * (แก้ไข) ใช้ for loop เพื่อให้ "วน" จริงๆ
     */
    private Timeline buildAttack_BlasterCircle() {
        Logger.log(Logger.LogType.INFO, "--- Building: Attack_BlasterCircle (Pattern 4) ---");

        resetBattleBox(); // (ใช้กล่องยาว 400x150)
        setHeartColor(Player.SoulColor.RED); // (หัวใจแดง ขยับอิสระ)

        // 2. กำหนดค่าการโจมตี
        double centerX = 400; // (กลางจอ)
        double centerY = 425; // (กลางกล่อง)
        double radius = 150; // (รัศมีวงกลม)
        double beamWidth = 300; // (ความยาวบีม)
        double beamHeight = 20; // (ความหนาบีม)

        double warningTime = 0.7; // (เตือน 0.7 วิ)
        double fireTime = 0.4;    // (ยิง 0.4 วิ)
        double timeStep = 0.2;    // (ช่องว่างระหว่างบีม 0.2 วิ)
        int totalBlasters = 8;    // (ยิง 8 ทิศ)

        // 3. สร้าง Timeline (แก้ไข)
        Timeline t = new Timeline();
        t.getKeyFrames().add(new KeyFrame(Duration.seconds(0.1), e -> sans.startAttack("up"))); //

        double lastFireTime = 0;

        // (นี่คือ Loop ที่สร้างการ "วน")
        for (int i = 0; i < totalBlasters; i++) {
            // (ต้องเป็น final เพราะจะใช้ใน Lambda)
            final double angle = i * (360.0 / totalBlasters); // 0, 45, 90, ...
            final double x = centerX + Math.cos(Math.toRadians(angle)) * radius;
            final double y = centerY + Math.sin(Math.toRadians(angle)) * radius;

            double warnStartTime = 0.5 + (i * timeStep);
            double fireStartTime = warnStartTime + warningTime;
            lastFireTime = fireStartTime; // (เก็บเวลาของบีมสุดท้ายไว้)

            // A. สร้าง KeyFrame สำหรับ "เตือน"
            t.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(warnStartTime), e -> {
                        spawnWarningBeam(x, y, beamWidth, beamHeight, angle, warningTime);
                    })
            );

            // B. สร้าง KeyFrame สำหรับ "ยิง"
            t.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(fireStartTime), e -> {
                        spawnBlasterBeam(x, y, beamWidth, beamHeight, angle, fireTime);
                    })
            );
        }

        // --- (จบ) ---
        // (รอให้บีมสุดท้ายยิงจบ + 1.5 วินาที)
        double totalDuration = lastFireTime + fireTime + 1.5;
        t.getKeyFrames().add(
                new KeyFrame(Duration.seconds(totalDuration), e -> {
                    Logger.log(Logger.LogType.INFO, "Attack finished. Returning to turn.");
                    returnToPlayerTurn(); //
                })
        );

        return t;
    }



}