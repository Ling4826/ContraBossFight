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

public class SansBattleScene extends Group {
    record AttackData(double velocityX, String attackType) {}

    private int playerMaxHP;
    private Text hpLabel;
    private Text hpValue;
    private Rectangle hpBarBackground;
    private Rectangle hpBarForeground;

    public enum BattleState {
        SELECTING,
        SELECTING_FIGHT,
        SELECTING_ACT,
        PLAYER_ATTACKING,
        ATTACKING
    }

    private AudioClip playerShootSound;
    private AudioClip sansHitSound;
    private AudioClip playerHurtSound;
    private boolean isPlayerDead = false;
    private boolean playerInvincible = false;
    private double invincibilityTimer = 0;

    public enum BattleResult { NONE, GOOD_ENDING, BAD_ENDING }
    private BattleResult battleResult = BattleResult.NONE;
    public BattleResult getBattleResult() { return battleResult; }
    private BattleState currentState = BattleState.SELECTING;

    private AudioClip selectSound;
    private Sans sans;
    private Player playerHeart;
    private BattleBox battleBox;
    private Timeline attackTimeline;
    private int playerHP;

    private BattleButton fightButton;
    private BattleButton actButton;
    private int currentSelection = 0;

    private Text actFriendText;
    private Text fightShootText;
    private Text fightSkipText;
    private int currentSubMenuSelection = 0;

    private List<Node> activeAttacks = new ArrayList<>();
    private double attackMoveSpeed = -200.0;
    private int attackPatternIndex = 0;
    private static final String TYPE_WHITE_BONE = "WHITE";
    private static final String TYPE_BLUE_BONE = "BLUE";
    private static final String TYPE_BEAM = "BEAM";

    private Image boneWhiteImg, boneBlueImg;

    private boolean heartUp = false, heartDown = false, heartLeft = false, heartRight = false;

    public SansBattleScene(int startingHP, AudioClip selectSound, AudioClip playerShootSound,
                           AudioClip sansHitSound, AudioClip playerHurtSound) {
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

            double hpUI_X = centerX - 50;
            double hpUI_Y = buttonY - 200;
            Font undertaleFont = Font.font("Determination Mono", 20);

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
            fightShootText.setVisible(false);

            fightSkipText = new Text(400, 380, "* Skip");
            fightSkipText.setFont(undertaleFont);
            fightSkipText.setFill(Color.WHITE);
            fightSkipText.setVisible(false);

            this.getChildren().addAll(
                    this.sans, this.battleBox, this.playerHeart,
                    fightButton, actButton, actFriendText, fightShootText, fightSkipText,
                    hpLabel, hpBarBackground, hpBarForeground, hpValue
            );
            playerHeart.setVisible(false);

            try {
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

        if (currentState == BattleState.ATTACKING) {
            playerHeart.setMovement(heartUp, heartDown, heartLeft, heartRight);
            playerHeart.update(deltaTime);
            playerHeart.constrainToBattleBox(battleBox);

            Iterator<Node> iterator = activeAttacks.iterator();
            while (iterator.hasNext()) {
                Node attack = iterator.next();

                if (attack.getUserData() == null || !(attack.getUserData() instanceof AttackData)) {
                    continue;
                }

                AttackData data = (AttackData) attack.getUserData();

                double velocity = data.velocityX();
                if (velocity != 0) {
                    attack.setTranslateX(attack.getTranslateX() + velocity * deltaTime);
                }

                if (!playerInvincible && playerHeart.intersects(attack.getBoundsInParent())) {
                    String type = data.attackType();

                    if (TYPE_BLUE_BONE.equals(type)) {
                        boolean isPlayerMoving = heartUp || heartDown || heartLeft || heartRight;
                        if (isPlayerMoving) {
                            playerTakeDamage(10);
                        }
                    } else {
                        playerTakeDamage(10);
                    }
                }

                double attackX = attack.getTranslateX();
                double attackWidth = attack.getBoundsInParent().getWidth();

                if (velocity < 0 && (attackX + attackWidth < 0)) {
                    this.getChildren().remove(attack);
                    iterator.remove();
                } else if (velocity > 0 && (attackX > 800)) {
                    this.getChildren().remove(attack);
                    iterator.remove();
                }
            }
        }
    }

    private void hideMainButtons() {
        fightButton.setVisible(false);
        actButton.setVisible(false);
    }

    private void showMainButtons() {
        fightButton.setVisible(true);
        actButton.setVisible(true);
        updateButtonSelection();
    }

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

    public boolean isPlayerDead() {
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

    private void confirmSelection() {
        hideMainButtons();

        if (currentSelection == 0) {
            Logger.log(Logger.LogType.INFO, "Player selected FIGHT.");
            currentState = BattleState.SELECTING_FIGHT;
            showFightMenu();
        } else {
            Logger.log(Logger.LogType.INFO, "Player selected ACT.");
            currentState = BattleState.SELECTING_ACT;
            showActMenu();
        }
    }

    private void confirmFightSelection() {
        hideFightMenu();
        playSound();

        if (currentSubMenuSelection == 0) {
            Logger.log(Logger.LogType.INFO, "Player selected FIGHT. Loading attack " + attackPatternIndex);
            if (playerShootSound != null) {
                playerShootSound.play();
            }

            if (this.sans != null) {
                this.sans.startDodgeSequence(() -> {
                    currentState = BattleState.ATTACKING;
                    playerHeart.setVisible(true);

                    if (attackTimeline != null) {
                        attackTimeline.stop();
                    }

                    Timeline nextAttack = getNextAttackPattern();

                    if (nextAttack == null) {
                        Logger.log(Logger.LogType.FATAL, "FINAL HIT! Player wins (Good Ending).");

                        if (sansHitSound != null) {
                            sansHitSound.play();
                        }
                        currentState = BattleState.PLAYER_ATTACKING;
                        this.sans.startHitSequence(this::triggerBadEnding);
                    } else {
                        attackTimeline = nextAttack;
                        attackTimeline.play();
                    }
                });
            } else {
                Logger.log(Logger.LogType.WARN, "Sans object is null, skipping dodge sequence.");
            }

        } else {
            Logger.log(Logger.LogType.WARN, "Player selected FIGHT -> Skip (FORCING BAD ENDING)");
            if (sansHitSound != null) {
                sansHitSound.play();
            }
            currentState = BattleState.PLAYER_ATTACKING;
            this.sans.startHitSequence(this::triggerBadEnding);
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
        setHeartColor(Player.SoulColor.RED);
        showMainButtons();
        hideFightMenu();
        hideActMenu();
        clearAllBones();
        resetBattleBox();
        playerHeart.setVisible(false);
    }

    public void handleKeyPressed(KeyEvent event) {
        switch (currentState) {
            case SELECTING:
                switch (event.getCode()) {
                    case A: case LEFT:
                        currentSelection = 0;
                        updateButtonSelection();
                        playSound();
                        break;
                    case D: case RIGHT:
                        currentSelection = 1;
                        updateButtonSelection();
                        playSound();
                        break;
                    case J:
                        confirmSelection();
                        playSound();
                        break;
                }
                break;

            case SELECTING_FIGHT:
                switch (event.getCode()) {
                    case A: case LEFT:
                        currentSubMenuSelection = 0;
                        updateFightMenuHighlight();
                        playSound();
                        break;
                    case D: case RIGHT:
                        currentSubMenuSelection = 1;
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

            case SELECTING_ACT:
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

            case PLAYER_ATTACKING:
                break;

            case ATTACKING:
                if (playerHeart.getCurrentColor() == Player.SoulColor.RED) {
                    switch (event.getCode()) {
                        case W: case UP: heartUp = true; break;
                        case S: case DOWN: heartDown = true; break;
                        case A: case LEFT: heartLeft = true; break;
                        case D: case RIGHT: heartRight = true; break;
                    }
                } else {
                    Player.GravityDirection dir = playerHeart.getGravityDirection();
                    switch (dir) {
                        case DOWN:
                            if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) heartLeft = true;
                            if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) heartRight = true;
                            if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) playerHeart.startJump();
                            break;
                        case UP:
                            if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) heartLeft = true;
                            if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) heartRight = true;
                            if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) playerHeart.startJump();
                            break;
                        case LEFT:
                            if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) heartUp = true;
                            if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) heartDown = true;
                            if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) playerHeart.startJump();
                            break;
                        case RIGHT:
                            if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) heartUp = true;
                            if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) heartDown = true;
                            if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) playerHeart.startJump();
                            break;
                    }
                }
                break;
        }
    }

    public void startEnemyAttack() {
        currentState = BattleState.ATTACKING;
        playerHeart.setVisible(true);

        if (attackTimeline != null) {
            attackTimeline.stop();
        }

        Logger.log(Logger.LogType.INFO, "AttackSequencer removed. Using 1s placeholder attack.");
        attackTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> returnToPlayerTurn())
        );

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

    public void playerTakeDamage(int amount) {
        if (playerInvincible) return;

        this.playerHP -= amount;
        if (this.playerHP < 0) this.playerHP = 0;

        updateHPDisplay();
        Logger.log(Logger.LogType.WARN, "Player Hit! HP left: " + this.playerHP);

        if (playerHurtSound != null) {
            playerHurtSound.play();
        }

        if (this.playerHP <= 0) {
            this.isPlayerDead = true;
            Logger.log(Logger.LogType.FATAL, "Player is Dead! Initiating Game Over.");
        }

        playerInvincible = true;
        invincibilityTimer = 0.1;
        playerHeart.setOpacity(0.5);
    }

    public void handleKeyReleased(KeyEvent event) {
        if (currentState == BattleState.ATTACKING) {
            if (playerHeart.getCurrentColor() == Player.SoulColor.RED) {
                switch (event.getCode()) {
                    case W: case UP: heartUp = false; break;
                    case S: case DOWN: heartDown = false; break;
                    case A: case LEFT: heartLeft = false; break;
                    case D: case RIGHT: heartRight = false; break;
                }
            } else {
                Player.GravityDirection dir = playerHeart.getGravityDirection();
                switch (dir) {
                    case DOWN:
                        if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) heartLeft = false;
                        if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) heartRight = false;
                        if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) playerHeart.releaseJump();
                        break;
                    case UP:
                        if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) heartLeft = false;
                        if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) heartRight = false;
                        if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) playerHeart.releaseJump();
                        break;
                    case LEFT:
                        if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) heartUp = false;
                        if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) heartDown = false;
                        if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) playerHeart.releaseJump();
                        break;
                    case RIGHT:
                        if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) heartUp = false;
                        if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) heartDown = false;
                        if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) playerHeart.releaseJump();
                        break;
                }
            }

            playerHeart.setMovement(heartUp, heartDown, heartLeft, heartRight);
        }
    }

    public boolean isBattleOver() {
        return battleResult != BattleResult.NONE;
    }

    public Player getPlayerHeart() {
        return this.playerHeart;
    }

    private void spawnBoneGapAttack(double gapCenterY, double gapHeight, double boneWidth, String boneType, String side) {
        double boxTop = battleBox.getTopBoundary();
        double boxBottom = battleBox.getBottomBoundary();
        double boxRight = battleBox.getRightBoundary();
        double boxLeft = battleBox.getLeftBoundary();

        double gapTop = gapCenterY - (gapHeight / 2);
        double gapBottom = gapCenterY + (gapHeight / 2);
        double topBoneHeight = gapTop - boxTop;
        double bottomBoneHeight = boxBottom - gapBottom;

        Image img = (boneType.equals(TYPE_BLUE_BONE)) ? boneBlueImg : boneWhiteImg;

        if (side.equals("RIGHT") || side.equals("BOTH")) {
            double speed = attackMoveSpeed;
            if (topBoneHeight > 5) {
                ImageView topBoneR = new ImageView(img);
                topBoneR.setFitWidth(boneWidth);
                topBoneR.setFitHeight(topBoneHeight);
                topBoneR.setPreserveRatio(false);
                topBoneR.setTranslateX(boxRight);
                topBoneR.setTranslateY(boxTop);
                topBoneR.setUserData(new AttackData(speed, boneType));
                activeAttacks.add(topBoneR);
                this.getChildren().add(topBoneR);
            }
            if (bottomBoneHeight > 5) {
                ImageView bottomBoneR = new ImageView(img);
                bottomBoneR.setFitWidth(boneWidth);
                bottomBoneR.setFitHeight(bottomBoneHeight);
                bottomBoneR.setPreserveRatio(false);
                bottomBoneR.setTranslateX(boxRight);
                bottomBoneR.setTranslateY(gapBottom);
                bottomBoneR.setUserData(new AttackData(speed, boneType));
                activeAttacks.add(bottomBoneR);
                this.getChildren().add(bottomBoneR);
            }
        }

        if (side.equals("LEFT") || side.equals("BOTH")) {
            double speed = -attackMoveSpeed;
            if (topBoneHeight > 5) {
                ImageView topBoneL = new ImageView(img);
                topBoneL.setFitWidth(boneWidth);
                topBoneL.setFitHeight(topBoneHeight);
                topBoneL.setPreserveRatio(false);
                topBoneL.setTranslateX(boxLeft - boneWidth);
                topBoneL.setTranslateY(boxTop);
                topBoneL.setUserData(new AttackData(speed, boneType));
                activeAttacks.add(topBoneL);
                this.getChildren().add(topBoneL);
            }
            if (bottomBoneHeight > 5) {
                ImageView bottomBoneL = new ImageView(img);
                bottomBoneL.setFitWidth(boneWidth);
                bottomBoneL.setFitHeight(bottomBoneHeight);
                bottomBoneL.setPreserveRatio(false);
                bottomBoneL.setTranslateX(boxLeft - boneWidth);
                bottomBoneL.setTranslateY(gapBottom);
                bottomBoneL.setUserData(new AttackData(speed, boneType));
                activeAttacks.add(bottomBoneL);
                this.getChildren().add(bottomBoneL);
            }
        }
    }

    private void spawnFloorCeilingBone(double boneX, double boneHeight, double boneWidth, boolean fromCeiling, double velocityX, String boneType) {
        double boxTop = battleBox.getTopBoundary();
        double boxBottom = battleBox.getBottomBoundary();

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

        bone.setUserData(new AttackData(velocityX, boneType));
        activeAttacks.add(bone);
        this.getChildren().add(bone);
    }

    private Timeline buildAttack_BoneGaps() {
        Logger.log(Logger.LogType.INFO, "--- Building: Attack_BoneGaps (Pattern 0) ---");

        setHeartColor(Player.SoulColor.BLUE);
        playerHeart.setGravityByNumber(1);
        double gapCenterY = 450;
        double gapHeight = 30;
        double boneWidth = 7;

        Timeline t = new Timeline(
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
                    spawnFloorCeilingBone(boxLeft - boneWidth, floorBoneHeight, boneWidth, false, rightSpeed, TYPE_WHITE_BONE);
                    spawnFloorCeilingBone(boxLeft - boneWidth - spacing, floorBoneHeight, boneWidth, false, rightSpeed, TYPE_WHITE_BONE);
                    spawnFloorCeilingBone(boxLeft - boneWidth - (spacing*2), floorBoneHeight, boneWidth, false, rightSpeed, TYPE_WHITE_BONE);
                    spawnFloorCeilingBone(boxLeft - boneWidth - (spacing*3), floorBoneHeight, boneWidth, false, rightSpeed, TYPE_WHITE_BONE);
                    spawnFloorCeilingBone(boxLeft - boneWidth - (spacing*4), floorBoneHeight, boneWidth, false, rightSpeed, TYPE_WHITE_BONE);
                }),
                new KeyFrame(Duration.seconds(1.0), e -> {
                    spawnFloorCeilingBone(boxRight, ceilingBoneHeight, boneWidth, true, leftSpeed, TYPE_WHITE_BONE);
                    spawnFloorCeilingBone(boxRight + spacing, ceilingBoneHeight, boneWidth, true, leftSpeed, TYPE_WHITE_BONE);
                    spawnFloorCeilingBone(boxRight + (spacing*2), ceilingBoneHeight, boneWidth, true, leftSpeed, TYPE_WHITE_BONE);
                    spawnFloorCeilingBone(boxLeft + (spacing*3), ceilingBoneHeight, boneWidth, true, leftSpeed, TYPE_WHITE_BONE);
                    spawnFloorCeilingBone(boxLeft + (spacing*4), ceilingBoneHeight, boneWidth, true, leftSpeed, TYPE_WHITE_BONE);
                }),
                new KeyFrame(Duration.seconds(9), e -> {
                    Logger.log(Logger.LogType.INFO, "Attack finished. Returning to turn.");
                    returnToPlayerTurn();
                })
        );
        return t;
    }

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
                selectedAttack = buildAttack_AlternatingGaps();
                break;
            case 3:
                selectedAttack = buildAttack_SlamAndFloor();
                break;
            case 4:
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
        Logger.log(Logger.LogType.INFO, "--- Building: Attack_AlternatingGaps (Pattern 3) ---");

        setHeartColor(Player.SoulColor.BLUE);
        playerHeart.setGravityByNumber(1);
        double boneWidth = 7;
        double floorBoneHeight = 40;
        double floorBoneHeight1 = 120;
        double boxLeft = battleBox.getLeftBoundary();
        double boxRight = battleBox.getRightBoundary();
        double leftSpeed = -attackMoveSpeed + 100;
        double rightSpeed = attackMoveSpeed - 100;

        Timeline t = new Timeline(
                new KeyFrame(Duration.seconds(1), e ->
                        spawnFloorCeilingBone(boxLeft - boneWidth, floorBoneHeight, boneWidth, false, leftSpeed, TYPE_WHITE_BONE)),
                new KeyFrame(Duration.seconds(2), e ->
                        spawnFloorCeilingBone(boxRight, floorBoneHeight1, boneWidth, false, rightSpeed, TYPE_BLUE_BONE)),
                new KeyFrame(Duration.seconds(3), e ->
                        spawnFloorCeilingBone(boxLeft - boneWidth, floorBoneHeight, boneWidth, false, leftSpeed, TYPE_WHITE_BONE)),
                new KeyFrame(Duration.seconds(4), e ->
                        spawnFloorCeilingBone(boxRight, floorBoneHeight1, boneWidth, false, rightSpeed, TYPE_BLUE_BONE)),
                new KeyFrame(Duration.seconds(5), e ->
                        spawnFloorCeilingBone(boxLeft - boneWidth, floorBoneHeight1, boneWidth, false, leftSpeed, TYPE_BLUE_BONE)),
                new KeyFrame(Duration.seconds(6), e ->
                        spawnFloorCeilingBone(boxRight, floorBoneHeight, boneWidth, false, rightSpeed, TYPE_WHITE_BONE)),
                new KeyFrame(Duration.seconds(7), e ->
                        spawnFloorCeilingBone(boxLeft - boneWidth, floorBoneHeight1, boneWidth, false, leftSpeed, TYPE_BLUE_BONE)),
                new KeyFrame(Duration.seconds(8), e ->
                        spawnFloorCeilingBone(boxRight, floorBoneHeight, boneWidth, false, rightSpeed, TYPE_WHITE_BONE)),
                new KeyFrame(Duration.seconds(9), e ->
                        spawnFloorCeilingBone(boxRight, floorBoneHeight1, boneWidth, false, rightSpeed, TYPE_BLUE_BONE)),
                new KeyFrame(Duration.seconds(10), e ->
                        spawnFloorCeilingBone(boxLeft - boneWidth, floorBoneHeight1, boneWidth, false, leftSpeed, TYPE_BLUE_BONE)),
                new KeyFrame(Duration.seconds(12), e -> {
                    Logger.log(Logger.LogType.INFO, "Attack finished. Returning to turn.");
                    returnToPlayerTurn();
                })
        );
        return t;
    }

    private void spawnWarningBox(double x, double y, double width, double height, double duration) {
        Rectangle warningBox = new Rectangle(x, y, width, height);
        warningBox.setFill(Color.RED);
        warningBox.setOpacity(0.7);

        this.getChildren().add(warningBox);

        Timeline cleanup = new Timeline(
                new KeyFrame(Duration.seconds(duration), e -> {
                    this.getChildren().remove(warningBox);
                })
        );
        cleanup.play();
    }

    private Timeline buildAttack_SlamAndFloor() {
        Logger.log(Logger.LogType.INFO, "--- Building: Attack_SlamAndFloor (Pattern 3) ---");

        battleBox.setSize(200, 200);
        battleBox.setPosition(300, 350);

        double boneWidthVertical = battleBox.getWidth();
        double boneHeightVertical = 40;
        double boneWidthHorizontal = 40;
        double boneHeightHorizontal = battleBox.getHeight();

        double boxLeft = battleBox.getLeftBoundary();
        double boxBottom = battleBox.getBottomBoundary();
        double boxRight = battleBox.getRightBoundary();
        double boxTop = battleBox.getTopBoundary();

        double stationarySpeed = 0.0;

        Timeline t = new Timeline(
                new KeyFrame(Duration.seconds(0.1), e -> sans.startAttack("down")),
                new KeyFrame(Duration.seconds(0.2), e -> {
                    setHeartColor(Player.SoulColor.BLUE);
                    playerHeart.setGravityByNumber(1);
                    double floorY = boxBottom - (playerHeart.getHitboxHeight() / 2.0);
                    playerHeart.setY(floorY);
                }),
                new KeyFrame(Duration.seconds(0.3), e -> {
                    spawnWarningBox(boxLeft, boxBottom - 10, boneWidthVertical, 10, 1.0);
                }),
                new KeyFrame(Duration.seconds(1.0), e -> {
                    spawnFloorCeilingBone(boxLeft, boneHeightVertical, boneWidthVertical, false, stationarySpeed, TYPE_WHITE_BONE);
                }),
                new KeyFrame(Duration.seconds(1.5), e -> {
                    clearAllBones();
                }),
                new KeyFrame(Duration.seconds(2.0), e -> {
                    sans.startAttack("right");
                }),
                new KeyFrame(Duration.seconds(2.1), e -> {
                    playerHeart.setGravityByNumber(4);
                    double rightWallX = boxRight - (playerHeart.getHitboxWidth() / 2.0);
                    playerHeart.setX(rightWallX);
                }),
                new KeyFrame(Duration.seconds(2.2), e -> {
                    spawnWarningBox(boxRight - 10, boxTop, 10, boneHeightHorizontal, 0.7);
                }),
                new KeyFrame(Duration.seconds(2.9), e -> {
                    spawnSideBone(boxTop, boneWidthHorizontal, boneHeightHorizontal, false, TYPE_WHITE_BONE);
                }),
                new KeyFrame(Duration.seconds(3.4), e -> {
                    clearAllBones();
                }),
                new KeyFrame(Duration.seconds(4.0), e -> {
                    Logger.log(Logger.LogType.INFO, "Attack finished. Returning to turn.");
                    returnToPlayerTurn();
                })
        );
        return t;
    }

    private void clearAllBones() {
        Iterator<Node> iterator = activeAttacks.iterator();
        while (iterator.hasNext()) {
            Node attack = iterator.next();
            if (attack.getUserData() != null && (attack.getUserData() instanceof AttackData)) {
                this.getChildren().remove(attack);
                iterator.remove();
            }
        }
    }

    private void spawnSideBone(double boneY, double boneWidth, double boneHeight, boolean fromLeft, String boneType) {
        double boxLeft = battleBox.getLeftBoundary();
        double boxRight = battleBox.getRightBoundary();

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

        bone.setUserData(new AttackData(0.0, boneType));
        activeAttacks.add(bone);
        this.getChildren().add(bone);
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
        beam.setTranslateX(x);
        beam.setTranslateY(y);
        beam.setRotate(rotation);

        beam.setUserData(new AttackData(0.0, TYPE_BEAM));
        beam.setStroke(Color.RED);
        beam.setStrokeWidth(1);

        activeAttacks.add(beam);
        this.getChildren().add(beam);

        Timeline cleanup = new Timeline(
                new KeyFrame(Duration.seconds(duration), e -> {
                    this.getChildren().remove(beam);
                    activeAttacks.remove(beam);
                })
        );
        cleanup.play();
    }

    private Timeline buildAttack_BlasterCircle() {
        Logger.log(Logger.LogType.INFO, "--- Building: Attack_BlasterCircle (Pattern 4) ---");

        resetBattleBox();
        setHeartColor(Player.SoulColor.RED);

        double centerX = 400;
        double centerY = 425;
        double radius = 150;
        double beamWidth = 300;
        double beamHeight = 20;

        double warningTime = 0.7;
        double fireTime = 0.4;
        double timeStep = 0.2;
        int totalBlasters = 8;

        Timeline t = new Timeline();
        t.getKeyFrames().add(new KeyFrame(Duration.seconds(0.1), e -> sans.startAttack("up")));

        double lastFireTime = 0;

        for (int i = 0; i < totalBlasters; i++) {
            final double angle = i * (360.0 / totalBlasters);
            final double x = centerX + Math.cos(Math.toRadians(angle)) * radius;
            final double y = centerY + Math.sin(Math.toRadians(angle)) * radius;

            double warnStartTime = 0.5 + (i * timeStep);
            double fireStartTime = warnStartTime + warningTime;
            lastFireTime = fireStartTime;

            t.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(warnStartTime), e -> {
                        spawnWarningBeam(x, y, beamWidth, beamHeight, angle, warningTime);
                    })
            );

            t.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(fireStartTime), e -> {
                        spawnBlasterBeam(x, y, beamWidth, beamHeight, angle, fireTime);
                    })
            );
        }

        double totalDuration = lastFireTime + fireTime + 1.5;
        t.getKeyFrames().add(
                new KeyFrame(Duration.seconds(totalDuration), e -> {
                    Logger.log(Logger.LogType.INFO, "Attack finished. Returning to turn.");
                    returnToPlayerTurn();
                })
        );

        return t;
    }
}