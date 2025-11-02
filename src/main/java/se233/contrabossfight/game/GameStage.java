package se233.contrabossfight.game;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import se233.contrabossfight.sprite.Animation;
import se233.contrabossfight.sprite.AnimationFrame;
import se233.contrabossfight.util.Logger;
import se233.contrabossfight.Model.SansPackage.Sans;
import se233.contrabossfight.Model.Character.Player;
import se233.contrabossfight.View.BattleBox;

public class GameStage {
    private enum GameState {
        MAIN_MENU,
        PLAYING,
        GAME_OVER,
        VICTORY_SEQUENCE,
        STAGE_CLEAR,
        BATTLE_TRANSITION,
        SANS_BATTLE,
        GOOD_ENDING,
        BAD_ENDING,
        EXIT
    }
    private AudioClip playerHurtSound;
    private static final double FADE_DURATION = 1.5;
    private static final double GOOD_ENDING_DURATION = 120.0;

    private int playerHP = 92;
    private GameController gameController;
    private Canvas canvas;
    private final Scene scene;
    private final GraphicsContext gc;
    private final Pane root;
    private GameState currentGameState;

    private boolean isFireKeyPressed = false;
    private boolean isSpecialFireKeyPressed = false;
    private boolean isTimeStopKeyPressed = false;
    private boolean wasTimeStopped = false;
    private int menuSelection = 0;

    private Image titleImage;
    private Animation titleAnimation;
    private Image menuSelectorIcon;
    private Image level1Background;
    private Image level2Background;
    private Image level3Background;
    private Image stageClearImage;
    private Image lifeIconImage;
    private Image goodEndingImage;
    private Image badEndingImage;

    private AudioClip sansHitSound;
    private AudioClip menuSelectSound;
    private AudioClip timeStopSound;
    private AudioClip timeResumeSound;

    private MediaPlayer menuMusicPlayer;
    private MediaPlayer battleMusicPlayer;
    private MediaPlayer victoryMusicPlayer;
    private MediaPlayer gameOverMusicPlayer;
    private MediaPlayer sansMusicPlayer;
    private MediaPlayer goodEndingMusicPlayer;
    private MediaPlayer badEndingMusicPlayer;

    private Media battleMusicMedia1;
    private Media battleMusicMedia2;
    private Media sansBattleMusicMedia;
    private Media goodEndingMusicMedia;
    private Media badEndingMusicMedia;

    private double victoryTimer = 0.0;
    private double goodEndingTimer = 0.0;
    private boolean victoryMusicPlayed = false;
    private boolean gameOverMusicPlayed = false;

    private boolean isFadingIn = false;
    private double fadeOpacity = 0.0;
    private double fadeTimer = 0.0;
    private SansBattleScene sansBattle;

    public GameStage(double width, double height) {
        this.canvas = new Canvas(width, height);
        this.gc = this.canvas.getGraphicsContext2D();
        this.root = new Pane(this.canvas);
        this.scene = new Scene(root, width, height);
        this.gameController = new GameController();
        this.root.setStyle("-fx-background-color: black;");
        loadImages();
        loadSounds();

        this.currentGameState = GameState.MAIN_MENU;
        handleKeyEvents();
    }

    private void loadImages() {
        try {
            this.level1Background = new Image(getClass().getResourceAsStream("/se233/contrabossfight/images/Level1.png"));
            this.level2Background = new Image(getClass().getResourceAsStream("/se233/contrabossfight/images/Level2.png"));
            this.level3Background = new Image(getClass().getResourceAsStream("/se233/contrabossfight/images/Level3.png"));
            this.titleImage = new Image(getClass().getResourceAsStream("/se233/contrabossfight/images/TitleScreen.png"));

            BoundingBox frame1 = new BoundingBox(0, 0, 256, 224);
            BoundingBox frame2 = new BoundingBox(256, 0, 256, 224);
            this.titleAnimation = new Animation(0.5, true,
                    new AnimationFrame(frame1, null),
                    new AnimationFrame(frame2, null)
            );

            this.menuSelectorIcon = new Image(getClass().getResourceAsStream("/se233/contrabossfight/images/MenuSelector.png"));
            this.lifeIconImage = new Image(getClass().getResourceAsStream("/se233/contrabossfight/images/LifeIcon.png"));
            this.stageClearImage = new Image(getClass().getResourceAsStream("/se233/contrabossfight/images/stage_clear_screen.png"));
            this.goodEndingImage = new Image(getClass().getResourceAsStream("/se233/contrabossfight/images/project3ending1.png"));
            this.badEndingImage = new Image(getClass().getResourceAsStream("/se233/contrabossfight/images/project3ending2.png"));
        } catch (Exception e) {
            Logger.log(Logger.LogType.FATAL, "Failed to load image", e);
        }
    }

    private void loadSounds() {
        try {
            String menuSoundPath = "/se233/contrabossfight/sounds/Title_Screen.mp3";
            Media menuMusic = new Media(getClass().getResource(menuSoundPath).toExternalForm());
            this.menuMusicPlayer = new MediaPlayer(menuMusic);
            this.menuMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            this.menuMusicPlayer.play();

            String battleSoundPath1 = "/se233/contrabossfight/sounds/Battle_BGM.mp3";
            this.battleMusicMedia1 = new Media(getClass().getResource(battleSoundPath1).toExternalForm());

            String battleSoundPath2 = "/se233/contrabossfight/sounds/Battle_BGM_2.mp3";
            this.battleMusicMedia2 = new Media(getClass().getResource(battleSoundPath2).toExternalForm());

            String selectSoundPath = "/se233/contrabossfight/sounds/Menu_Select.mp3";
            this.menuSelectSound = new AudioClip(getClass().getResource(selectSoundPath).toExternalForm());

            String victorySoundPath = "/se233/contrabossfight/sounds/Stage_Clear.mp3";
            Media victoryMusic = new Media(getClass().getResource(victorySoundPath).toExternalForm());
            this.victoryMusicPlayer = new MediaPlayer(victoryMusic);
            this.victoryMusicPlayer.setCycleCount(1);

            String gameOverSoundPath = "/se233/contrabossfight/sounds/Game_Over.mp3";
            Media gameOverMusic = new Media(getClass().getResource(gameOverSoundPath).toExternalForm());
            this.gameOverMusicPlayer = new MediaPlayer(gameOverMusic);
            this.gameOverMusicPlayer.setCycleCount(1);

            String timeStopSoundPath = "/se233/contrabossfight/sounds/Time_Stop.mp3";
            this.timeStopSound = new AudioClip(getClass().getResource(timeStopSoundPath).toExternalForm());

            String timeResumeSoundPath = "/se233/contrabossfight/sounds/timeResumeSound.m4a";
            this.timeResumeSound = new AudioClip(getClass().getResource(timeResumeSoundPath).toExternalForm());

            try {
                String sansMusicPath = "/se233/contrabossfight/sounds/Megalovania.mp3";
                this.sansBattleMusicMedia = new Media(getClass().getResource(sansMusicPath).toExternalForm());
                this.sansMusicPlayer = new MediaPlayer(this.sansBattleMusicMedia);
                this.sansMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            } catch (Exception e) {
                Logger.log(Logger.LogType.WARN, "Failed to load Sans battle music.", e);
            }

            try {
                String endingMusicPath = "/se233/contrabossfight/sounds/EndingTheme.mp3";
                this.goodEndingMusicMedia = new Media(getClass().getResource(endingMusicPath).toExternalForm());
                this.goodEndingMusicPlayer = new MediaPlayer(this.goodEndingMusicMedia);
                this.goodEndingMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            } catch (Exception e) {
                Logger.log(Logger.LogType.WARN, "Failed to load Good Ending music.", e);
            }

            try {
                String badEndingMusicPath = "/se233/contrabossfight/sounds/BadEndingTheme.mp3";
                this.badEndingMusicMedia = new Media(getClass().getResource(badEndingMusicPath).toExternalForm());
                this.badEndingMusicPlayer = new MediaPlayer(this.badEndingMusicMedia);
                this.badEndingMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            } catch (Exception e) {
                Logger.log(Logger.LogType.WARN, "Failed to load Bad Ending music.", e);
            }

            try {
                String hitSoundPath = "/se233/contrabossfight/sounds/Hit.wav";
                this.sansHitSound = new AudioClip(getClass().getResource(hitSoundPath).toExternalForm());
            } catch (Exception e) {
                Logger.log(Logger.LogType.WARN, "Failed to load Sans hit sound.", e);
            }
            try {
                String hurtSoundPath = "/se233/contrabossfight/sounds/Hurt.mp3";
                this.playerHurtSound = new AudioClip(getClass().getResource(hurtSoundPath).toExternalForm());
            } catch (Exception e) {
                Logger.log(Logger.LogType.WARN, "Failed to load Player hurt sound.", e);
            }
        } catch (Exception e) {
            Logger.log(Logger.LogType.FATAL, "Failed to load music/sound files", e);
        }
    }

    public Scene getScene() {
        return scene;
    }

    private void setStageMusic(int stage) {
        if (battleMusicPlayer != null) {
            battleMusicPlayer.stop();
        }

        Media newMedia = (stage == 2) ? battleMusicMedia2 : battleMusicMedia1;
        if (stage == 3) {
            Logger.log(Logger.LogType.INFO, "No battle music for Stage 3 (Undertale).");
            return;
        }

        if (newMedia != null) {
            battleMusicPlayer = new MediaPlayer(newMedia);
            battleMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            battleMusicPlayer.play();
        }
    }

    private void handleKeyEvents() {
        scene.setOnKeyPressed(event -> handleKeyPressed(event));
        scene.setOnKeyReleased(event -> handleKeyReleased(event));
        Logger.log(Logger.LogType.INFO, "Keyboard event handlers set up.");
    }

    private void handleKeyPressed(KeyEvent event) {
        switch (currentGameState) {
            case MAIN_MENU:
                handleMainMenuKeys(event);
                break;
            case PLAYING:
                handlePlayingKeys(event);
                break;
            case GAME_OVER:
                handleGameOverKeys(event);
                break;
            case VICTORY_SEQUENCE:
                handleVictorySequenceKeys(event);
                break;
            case STAGE_CLEAR:
                handleStageClearKeys(event);
                break;
            case SANS_BATTLE:
                if (sansBattle != null) {
                    sansBattle.handleKeyPressed(event);
                }
                break;
            case GOOD_ENDING:
                handleGoodEndingKeys(event);
                break;
            case BAD_ENDING:
                handleBadEndingKeys(event);
                break;
        }
    }

    private void handleMainMenuKeys(KeyEvent event) {
        switch (event.getCode()) {
            case W:
                menuSelection = 0;
                break;
            case S:
                menuSelection = 1;
                break;
            case J:
                if (menuSelectSound != null) menuSelectSound.play();
                if (menuSelection == 0) {
                    startNewGame();
                } else if (menuSelection == 1) {
                    currentGameState = GameState.EXIT;
                }
                break;
        }
    }

    private void startNewGame() {
        if (menuMusicPlayer != null) menuMusicPlayer.stop();
        this.gameController = new GameController();
        setStageMusic(gameController.getCurrentStage());
        this.victoryMusicPlayed = false;
        this.gameOverMusicPlayed = false;
        Logger.log(Logger.LogType.INFO, "--- STARTING NEW GAME ---");
        this.victoryTimer = 0.0;
        this.wasTimeStopped = false;
        currentGameState = GameState.PLAYING;
    }

    private void handlePlayingKeys(KeyEvent event) {
        if (gameController.getPlayer() == null) return;

        if (gameController.isStage3CutsceneActive()) {
            return;
        }

        switch (event.getCode()) {
            case A:
                gameController.getPlayer().moveLeft(true);
                break;
            case D:
                gameController.getPlayer().moveRight(true);
                break;
            case W:
                gameController.getPlayer().aimUp(true);
                break;
            case S:
                gameController.getPlayer().aimDown(true);
                break;
            case SPACE:
                gameController.getPlayer().jump();
                break;
            case J:
                if (gameController.getCurrentStage() != 3) {
                    if (!isFireKeyPressed) {
                        gameController.getPlayer().shoot();
                        isFireKeyPressed = true;
                    }
                }
                break;
            case K:
                if (gameController.getCurrentStage() != 3) {
                    if (!isSpecialFireKeyPressed) {
                        gameController.getPlayer().shootSpecial();
                        isSpecialFireKeyPressed = true;
                    }
                }
                break;
            case L:
                if (gameController.getCurrentStage() != 3) {
                    if (!isTimeStopKeyPressed) {
                        activateTimeStop();
                        isTimeStopKeyPressed = true;
                    }
                }
                break;
        }
    }

    private void activateTimeStop() {
        boolean success = gameController.activateTimeStop();
        if (success && timeStopSound != null) {
            timeStopSound.play();
            if (battleMusicPlayer != null) {
                battleMusicPlayer.pause();
            }
        }
    }

    private void handleGameOverKeys(KeyEvent event) {
        if (event.getCode() == KeyCode.J) {
            currentGameState = GameState.MAIN_MENU;
            if (gameOverMusicPlayer != null) gameOverMusicPlayer.stop();
            if (menuMusicPlayer != null) {
                menuMusicPlayer.seek(Duration.ZERO);
                menuMusicPlayer.play();
            }
        }
    }

    private void handleVictorySequenceKeys(KeyEvent event) {
        if (gameController.getPlayer() == null) return;

        switch (event.getCode()) {
            case A:
                gameController.getPlayer().moveLeft(true);
                break;
            case D:
                gameController.getPlayer().moveRight(true);
                break;
            case W:
                gameController.getPlayer().aimUp(true);
                break;
            case S:
                gameController.getPlayer().aimDown(true);
                break;
            case SPACE:
                gameController.getPlayer().jump();
                break;
            case J:
                if (!isFireKeyPressed) {
                    gameController.getPlayer().shoot();
                    isFireKeyPressed = true;
                }
                break;
        }
    }

    private void handleStageClearKeys(KeyEvent event) {
        if (event.getCode() == KeyCode.J) {
            if (menuSelectSound != null) {
                menuSelectSound.play();
            }

            boolean hasNextStage = gameController.loadNextStage();

            if (hasNextStage) {
                currentGameState = GameState.PLAYING;
                setStageMusic(gameController.getCurrentStage());
                this.wasTimeStopped = false;
            } else {
                currentGameState = GameState.MAIN_MENU;
                if (menuMusicPlayer != null) {
                    menuMusicPlayer.seek(Duration.ZERO);
                    menuMusicPlayer.play();
                }
            }
        }
    }

    private void handleGoodEndingKeys(KeyEvent event) {
        if (fadeTimer >= FADE_DURATION && event.getCode() == KeyCode.J) {
            if (menuSelectSound != null) menuSelectSound.play();
            goToMainMenu();
        }
    }

    private void handleBadEndingKeys(KeyEvent event) {
        if (fadeTimer >= FADE_DURATION && event.getCode() == KeyCode.J) {
            if (menuSelectSound != null) menuSelectSound.play();
            goToMainMenu();
        }
    }

    private void handleKeyReleased(KeyEvent event) {
        switch (event.getCode()) {
            case J:
                isFireKeyPressed = false;
                break;
            case K:
                isSpecialFireKeyPressed = false;
                break;
            case L:
                isTimeStopKeyPressed = false;
                break;
        }

        if (gameController == null) return;

        if (gameController.isStage3CutsceneActive()) {
            gameController.getPlayer().stopMovement();
            return;
        }

        if (currentGameState == GameState.PLAYING || currentGameState == GameState.VICTORY_SEQUENCE) {
            if (gameController.getPlayer() == null) return;
            switch (event.getCode()) {
                case A:
                    gameController.getPlayer().moveLeft(false);
                    break;
                case D:
                    gameController.getPlayer().moveRight(false);
                    break;
                case W:
                    gameController.getPlayer().aimUp(false);
                    break;
                case S:
                    gameController.getPlayer().aimDown(false);
                    break;
            }
        }

        if (currentGameState == GameState.SANS_BATTLE) {
            if (sansBattle != null) {
                sansBattle.handleKeyReleased(event);
            }
        }
    }

    public void start() {
        new AnimationTimer() {
            private long lastNanoTime = System.nanoTime();

            @Override
            public void handle(long currentNanoTime) {
                double deltaTime = (currentNanoTime - lastNanoTime) / 1_000_000_000.0;
                lastNanoTime = currentNanoTime;

                switch (currentGameState) {
                    case MAIN_MENU:
                        renderMainMenu(deltaTime);
                        break;
                    case PLAYING:
                        updatePlaying(deltaTime);
                        break;
                    case GAME_OVER:
                        updateGameOver();
                        break;
                    case VICTORY_SEQUENCE:
                        updateVictorySequence(deltaTime);
                        break;
                    case STAGE_CLEAR:
                        renderStageClear();
                        break;
                    case BATTLE_TRANSITION:
                        updateBattleTransition(deltaTime);
                        break;
                    case SANS_BATTLE:
                        updateSansBattle(deltaTime);
                        break;
                    case GOOD_ENDING:
                        updateGoodEnding(deltaTime);
                        break;
                    case BAD_ENDING:
                        updateBadEnding(deltaTime);
                        break;
                    case EXIT:
                        Logger.log(Logger.LogType.INFO, "--- EXITING GAME ---");
                        stopAllSounds();
                        Platform.exit();
                        break;
                }
            }
        }.start();
        Logger.log(Logger.LogType.INFO, "Game loop (AnimationTimer) started.");
    }

    private void updatePlaying(double deltaTime) {
        if (gameController == null) return;

        gameController.updateGame(deltaTime);
        render();

        handleTimeStopped();
        checkGameStateTransitions();
    }

    private void handleTimeStopped() {
        boolean isCurrentlyStopped = gameController.isTimeStopped();
        if (wasTimeStopped && !isCurrentlyStopped) {
            if (timeResumeSound != null) {
                timeResumeSound.play();
            }
            if (battleMusicPlayer != null) {
                battleMusicPlayer.play();
            }
        }
        wasTimeStopped = isCurrentlyStopped;
    }

    private void checkGameStateTransitions() {
        if (currentGameState != GameState.BATTLE_TRANSITION &&
                gameController.getCurrentStage() == 3 &&
                gameController.isBossFightStarted()) {

            if (battleMusicPlayer != null) battleMusicPlayer.stop();
            render();

            Logger.log(Logger.LogType.FATAL, "Stage 3 Battle Triggered. Fading to black.");
            int lives = gameController.getPlayer().getLives();
            this.playerHP = 92 * lives;

            Logger.log(Logger.LogType.INFO, "Player HP set to: " + this.playerHP);
            this.fadeTimer = 0.0;
            this.fadeOpacity = 0.0;
            this.currentGameState = GameState.BATTLE_TRANSITION;
            return;
        }

        if (gameController.areAllBossesDefeated()) {
            currentGameState = GameState.VICTORY_SEQUENCE;
            victoryTimer = 0.0;
            victoryMusicPlayed = false;
            Logger.log(Logger.LogType.INFO, "bosses defeated. Starting VICTORY_SEQUENCE.");
        } else if (gameController.getPlayer() != null &&
                !gameController.getPlayer().isAlive() &&
                !gameController.getPlayer().isDying()) {
            currentGameState = GameState.GAME_OVER;
            Logger.log(Logger.LogType.FATAL, "Player is dead. Triggering GAME_OVER.");
            if (battleMusicPlayer != null) battleMusicPlayer.stop();
            gameOverMusicPlayed = false;
        }
    }

    private void updateGameOver() {
        // (1) จัดการเสียงเมื่อเข้าสู่ Game Over
        if (gameOverMusicPlayer != null && !gameOverMusicPlayed) {
            if (sansMusicPlayer != null) sansMusicPlayer.stop(); // <--- แก้ไข/เพิ่ม: หยุดเพลง Sans
            gameOverMusicPlayer.seek(Duration.ZERO);
            gameOverMusicPlayer.play();
            gameOverMusicPlayed = true;
        }

        // (2) ทำความสะอาดฉาก Sans หากมี <--- (เพิ่มใหม่)
        if (sansBattle != null) {
            root.getChildren().remove(sansBattle); // ลบ SansBattleScene ออกจาก root
            sansBattle = null;
            canvas.setVisible(true); // ทำให้ canvas หลักกลับมาแสดง
            Logger.log(Logger.LogType.INFO, "SansBattleScene cleared and canvas restored.");
        }

        render();
    }

    private void updateVictorySequence(double deltaTime) {
        if (gameController != null) {
            gameController.updateGame(deltaTime);
            render();
        }

        if (gameController != null) {
            handleTimeStopped();
        }

        victoryTimer += deltaTime;

        if (victoryTimer >= 4.0 && !victoryMusicPlayed) {
            if (battleMusicPlayer != null) {
                battleMusicPlayer.stop();
            }
            if (victoryMusicPlayer != null) {
                victoryMusicPlayer.seek(Duration.ZERO);
                victoryMusicPlayer.play();
            }
            victoryMusicPlayed = true;
        }

        if (victoryTimer >= 10.0) {
            currentGameState = GameState.STAGE_CLEAR;
            if (victoryMusicPlayer != null) victoryMusicPlayer.stop();
            renderStageClear();
        }
    }

    private void updateBattleTransition(double deltaTime) {
        if (fadeTimer < FADE_DURATION) {
            fadeTimer += deltaTime;
            fadeOpacity = Math.min(1.0, fadeTimer / FADE_DURATION);
        } else {
            fadeOpacity = 1.0;

            if (this.sansBattle == null) {
                this.sansBattle = new SansBattleScene(
                        this.playerHP,
                        this.menuSelectSound,
                        gameController.getPlayer().getShootSound(),
                        this.sansHitSound,
                        this.playerHurtSound
                );
                this.root.getChildren().add(this.sansBattle);
            }

            this.canvas.setVisible(false);
            this.sansBattle.setVisible(true);

            this.isFadingIn = true;
            this.fadeTimer = 0.0;
            this.currentGameState = GameState.SANS_BATTLE;
            Logger.log(Logger.LogType.INFO, "--- SANS BATTLE INITIATED ---");
        }

        renderGameOnly();
        drawFadeOverlay();
    }

    private void updateSansBattle(double deltaTime) {
        if (isFadingIn) {
            if (fadeTimer < FADE_DURATION) {
                fadeTimer += deltaTime;
                fadeOpacity = 1.0 - Math.min(1.0, fadeTimer / FADE_DURATION);
            } else {
                fadeOpacity = 0.0;
                isFadingIn = false;

                sansBattle.startBattle();
                if (sansMusicPlayer != null && sansMusicPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                    sansMusicPlayer.seek(Duration.ZERO);
                    sansMusicPlayer.play();
                }
            }
        } else {
            if (sansBattle != null) {
                sansBattle.update(deltaTime);

                if (sansBattle.isPlayerDead()) {
                    Logger.log(Logger.LogType.INFO, "Player died in Sans Battle. Triggering GAME_OVER.");
                    if (sansMusicPlayer != null) sansMusicPlayer.stop();

                    this.fadeTimer = 0.0;
                    this.currentGameState = GameState.GAME_OVER;
                    return;
                }

                if (sansBattle.isBattleOver()) {
                    if (sansBattle.getBattleResult() == SansBattleScene.BattleResult.GOOD_ENDING) {
                        Logger.log(Logger.LogType.INFO, "Good Ending Triggered. Fading out Sans scene.");
                        if (sansMusicPlayer != null) sansMusicPlayer.stop();

                        this.fadeTimer = 0.0;
                        this.currentGameState = GameState.GOOD_ENDING;
                        this.goodEndingTimer = 0.0;
                    } else if (sansBattle.getBattleResult() == SansBattleScene.BattleResult.BAD_ENDING) {
                        Logger.log(Logger.LogType.INFO, "Bad Ending Triggered. Fading out Sans scene.");
                        if (sansMusicPlayer != null) sansMusicPlayer.stop();

                        this.fadeTimer = 0.0;
                        this.currentGameState = GameState.BAD_ENDING;
                        this.goodEndingTimer = 0.0;
                    } else {
                        goToMainMenu();
                    }
                }
            }
        }

        if (fadeOpacity > 0) {
            drawFadeOverlay();
        }
    }

    private void updateGoodEnding(double deltaTime) {
        if (goodEndingMusicPlayer != null && goodEndingMusicPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            goodEndingMusicPlayer.seek(Duration.ZERO);
            goodEndingMusicPlayer.play();
        }

        if (fadeTimer < FADE_DURATION) {
            fadeTimer += deltaTime;
            fadeOpacity = 1.0 - Math.min(1.0, fadeTimer / FADE_DURATION);

            if (sansBattle != null) {
                sansBattle.setOpacity(fadeOpacity);
            }
        } else {
            if (sansBattle != null) {
                sansBattle.setVisible(false);
            }
            canvas.setVisible(true);

            goodEndingTimer += deltaTime;

            if (goodEndingTimer >= GOOD_ENDING_DURATION) {
                goToMainMenu();
                return;
            }

            double endingFadeInOpacity = 1.0;
            if (goodEndingTimer < FADE_DURATION) {
                endingFadeInOpacity = goodEndingTimer / FADE_DURATION;
            }
            gc.setGlobalAlpha(endingFadeInOpacity);

            if (goodEndingImage != null) {
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                gc.drawImage(goodEndingImage, 0, 0, canvas.getWidth(), canvas.getHeight());
            } else {
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            }

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Press Start 2P", FontWeight.NORMAL, 16));
            gc.setTextAlign(TextAlignment.LEFT);
            String scoreText = String.format("SCORE: %d", gameController.getPlayer().getScore());
            gc.fillText(scoreText, 20, 30);

            double lifeX = 550;
            double lifeY = 550;
            if (lifeIconImage != null) {
                gc.drawImage(lifeIconImage, lifeX, lifeY, 40, 40);
            }
            gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 24));
            gc.setTextAlign(TextAlignment.LEFT);
            String livesText = String.format("x %d", gameController.getPlayer().getLives());
            gc.fillText(livesText, lifeX + 40 + 10, lifeY + 30);

            gc.setGlobalAlpha(1.0);
        }
    }

    private void updateBadEnding(double deltaTime) {
        if (badEndingMusicPlayer != null && badEndingMusicPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            badEndingMusicPlayer.seek(Duration.ZERO);
            badEndingMusicPlayer.play();
        }

        if (fadeTimer < FADE_DURATION) {
            fadeTimer += deltaTime;
            fadeOpacity = 1.0 - Math.min(1.0, fadeTimer / FADE_DURATION);
            if (sansBattle != null) {
                sansBattle.setOpacity(fadeOpacity);
            }
        } else {
            if (sansBattle != null) {
                sansBattle.setVisible(false);
            }
            canvas.setVisible(true);

            goodEndingTimer += deltaTime;

            if (goodEndingTimer >= GOOD_ENDING_DURATION) {
                goToMainMenu();
                return;
            }

            double endingFadeInOpacity = 1.0;
            if (goodEndingTimer < FADE_DURATION) {
                endingFadeInOpacity = goodEndingTimer / FADE_DURATION;
            }
            gc.setGlobalAlpha(endingFadeInOpacity);

            if (badEndingImage != null) {
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                gc.drawImage(badEndingImage, 0, 0, canvas.getWidth(), canvas.getHeight());
            } else {
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            }

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Press Start 2P", FontWeight.NORMAL, 16));
            gc.setTextAlign(TextAlignment.LEFT);
            String scoreText = String.format("SCORE: %d", gameController.getPlayer().getScore());
            gc.fillText(scoreText, 20, 30);

            double lifeX = 550;
            double lifeY = 550;
            if (lifeIconImage != null) {
                gc.drawImage(lifeIconImage, lifeX, lifeY, 40, 40);
            }
            gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 24));
            gc.setTextAlign(TextAlignment.LEFT);
            String livesText = String.format("x %d", gameController.getPlayer().getLives());
            gc.fillText(livesText, lifeX + 40 + 10, lifeY + 30);

            gc.setGlobalAlpha(1.0);
        }
    }

    private void goToMainMenu() {
        currentGameState = GameState.MAIN_MENU;

        if (gameOverMusicPlayer != null) gameOverMusicPlayer.stop();
        if (goodEndingMusicPlayer != null) goodEndingMusicPlayer.stop();
        if (sansMusicPlayer != null) sansMusicPlayer.stop();
        if (badEndingMusicPlayer != null) badEndingMusicPlayer.stop();

        if (menuMusicPlayer != null) {
            menuMusicPlayer.seek(Duration.ZERO);
            menuMusicPlayer.play();
        }

        canvas.setVisible(true);
        canvas.setOpacity(1.0);

        if (sansBattle != null) {
            root.getChildren().remove(sansBattle);
            sansBattle = null;
        }
    }

    private void stopAllSounds() {
        if (menuMusicPlayer != null) menuMusicPlayer.stop();
        if (battleMusicPlayer != null) battleMusicPlayer.stop();
        if (victoryMusicPlayer != null) victoryMusicPlayer.stop();
        if (gameOverMusicPlayer != null) gameOverMusicPlayer.stop();
        if (sansMusicPlayer != null) sansMusicPlayer.stop();
        if (badEndingMusicPlayer != null) badEndingMusicPlayer.stop();
        if (goodEndingMusicPlayer != null) goodEndingMusicPlayer.stop();
        if (timeStopSound != null) timeStopSound.stop();
        if (timeResumeSound != null) timeResumeSound.stop();
    }

    private void render() {
        if (gameController == null) {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
            return;
        }

        Image bgToDraw = getBackgroundForStage(gameController.getCurrentStage());
        drawBackground(bgToDraw);
        drawGameObjects();
        drawUI();
    }

    private Image getBackgroundForStage(int stage) {
        if (stage == 2) return level2Background;
        if (stage == 3) return level3Background;
        return level1Background;
    }

    private void drawBackground(Image background) {
        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();
        double imageWidth = background.getWidth();
        double imageHeight = background.getHeight();
        double scale = Math.min(canvasWidth / imageWidth, canvasHeight / imageHeight);
        double scaledWidth = imageWidth * scale;
        double scaledHeight = imageHeight * scale;
        double x = (canvasWidth - scaledWidth) / 2;
        double y = (canvasHeight - scaledHeight) / 2;

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        boolean timeStopped = gameController.isTimeStopped();
        if (timeStopped) {
            javafx.scene.effect.ColorAdjust desaturate = new javafx.scene.effect.ColorAdjust();
            desaturate.setSaturation(-1.0);
            desaturate.setBrightness(-0.2);
            gc.save();
            gc.setEffect(desaturate);
        }

        gc.drawImage(background, x, y, scaledWidth, scaledHeight);

        if (timeStopped) {
            gc.restore();
        }
    }

    private void drawGameObjects() {
        if (gameController != null) {
            gameController.getGameObjects().forEach(obj -> obj.render(gc));
        }
    }

    private void renderGameOnly() {
        Image bgToDraw = level1Background;
        if (gameController != null) {
            bgToDraw = getBackgroundForStage(gameController.getCurrentStage());
        }

        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();
        double imageWidth = bgToDraw.getWidth();
        double imageHeight = bgToDraw.getHeight();
        double scale = Math.min(canvasWidth / imageWidth, canvasHeight / imageHeight);
        double scaledWidth = imageWidth * scale;
        double scaledHeight = imageHeight * scale;
        double x = (canvasWidth - scaledWidth) / 2;
        double y = (canvasHeight - scaledHeight) / 2;

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        gc.drawImage(bgToDraw, x, y, scaledWidth, scaledHeight);

        if (gameController != null) {
            gameController.getGameObjects().forEach(obj -> obj.render(gc));
        }
    }

    private void renderMainMenu(double deltaTime) {
        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        if (titleAnimation != null) {
            titleAnimation.update(deltaTime);
            BoundingBox currentFrame = titleAnimation.getCurrentFrame().getSourceRect();

            gc.drawImage(
                    this.titleImage,
                    currentFrame.getMinX(), currentFrame.getMinY(),
                    currentFrame.getWidth(), currentFrame.getHeight(),
                    0, 0,
                    canvasWidth, canvasHeight
            );
        }

        if (menuSelectorIcon != null) {
            double iconWidth = 45;
            double iconHeight = 45;
            double selectorX = (canvasWidth * 0.25) - 35;
            double option1Y = canvasHeight * 0.72;
            double option2Y = canvasHeight * 0.80;
            double drawY = (menuSelection == 0) ?
                    option1Y + (16/2.0) - (iconHeight/2.0) :
                    option2Y + (16/2.0) - (iconHeight/2.0);
            gc.drawImage(menuSelectorIcon, selectorX, drawY, iconWidth, iconHeight);
        }
    }

    private void drawUI() {
        if (gameController == null || gameController.getPlayer() == null) return;

        drawScore();
        drawLives();
        drawTimeStopUI();
        drawCountdown();
        drawGameOverUI();
    }

    private void drawScore() {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Press Start 2P", FontWeight.NORMAL, 16));
        gc.setTextAlign(TextAlignment.LEFT);
        String scoreText = String.format("SCORE: %d", gameController.getPlayer().getScore());
        gc.fillText(scoreText, 20, 30);
    }

    private void drawLives() {
        double canvasWidth = gc.getCanvas().getWidth();
        double lifeX = canvasWidth - 120;
        double lifeY = 20;
        double iconSize = 40;

        if (lifeIconImage != null) {
            gc.drawImage(lifeIconImage, lifeX, lifeY, iconSize, iconSize);
        }

        gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 24));
        gc.setTextAlign(TextAlignment.LEFT);
        String livesText = String.format("x %d", gameController.getPlayer().getLives());
        gc.fillText(livesText, lifeX + iconSize + 10, lifeY + 30);
    }

    private void drawTimeStopUI() {
        if (!gameController.isBossFightStarted()) return;

        double timeStopY = 60;
        double cooldown = gameController.getTimeStopCooldown();

        if (gameController.isTimeStopped()) {
            gc.setFill(Color.CYAN);
            gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 18));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText(String.format("TIME STOP: %.1fs", gameController.getTimeStopTimer()), 20, timeStopY);
        } else if (cooldown > 0) {
            gc.setFill(Color.GRAY);
            gc.setFont(Font.font("Press Start 2P", FontWeight.NORMAL, 14));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText(String.format("TIME STOP: %.1fs", cooldown), 20, timeStopY);
        } else {
            gc.setFill(Color.CYAN);
            gc.setFont(Font.font("Press Start 2P", FontWeight.NORMAL, 14));
            gc.setTextAlign(TextAlignment.LEFT);
            if ((System.nanoTime() / 500_000_000.0) % 2 < 1) {
                gc.fillText("TIME STOP [L] READY", 20, timeStopY);
            }
        }
    }

    private void drawCountdown() {
        double centerX = this.canvas.getWidth() / 2;
        double centerY = this.canvas.getHeight() / 2;

        if (gameController.getCurrentStage() == 3) {
            return;
        }

        if (gameController.isCountdownActive()) {
            gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 80));
            gc.setFill(Color.WHITE);
            gc.setTextAlign(TextAlignment.CENTER);
            int num = (int) Math.ceil(gameController.getCountdownTimer());
            gc.fillText(Integer.toString(num), centerX, centerY);
            gc.setTextAlign(TextAlignment.LEFT);
        } else if (gameController.isTimeStopped() && gameController.isBossFightStarted()) {
            gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 80));
            gc.setFill(Color.CYAN);
            gc.setTextAlign(TextAlignment.CENTER);
            int num = (int) Math.ceil(gameController.getTimeStopTimer());
            if ((System.nanoTime() / 250_000_000.0) % 2 < 1) {
                gc.fillText(Integer.toString(num), centerX, centerY);
            }
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    private void drawGameOverUI() {
        if (currentGameState != GameState.GAME_OVER) return;

        gc.setFill(Color.RED);
        gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 60));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("GAME OVER", this.canvas.getWidth() / 2, this.canvas.getHeight() / 2 - 50);

        gc.setFont(Font.font("Press Start 2P", FontWeight.NORMAL, 20));
        gc.setFill(Color.WHITE);
        if ((System.nanoTime() / 500_000_000.0) % 2 < 1) {
            gc.fillText("PRESS J TO RETURN TO MENU", this.canvas.getWidth() / 2, this.canvas.getHeight() / 2 + 50);
        }
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void renderStageClear() {
        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        int stageCleared = gameController.getCurrentStage();
        Logger.log(Logger.LogType.INFO, "Rendering Stage Clear screen for stage: " + stageCleared);
        gc.setFont(Font.font("Press Start 2P", FontWeight.NORMAL, 30));
        gc.fillText(String.format("STAGE %d CLEAR", stageCleared), canvasWidth / 2, canvasHeight / 2 - 20);

        gc.setFont(Font.font("Press Start 2P", FontWeight.NORMAL, 20));
        if ((System.nanoTime() / 500_000_000.0) % 2 < 1) {
            gc.fillText("PRESS J TO CONTINUE", canvasWidth / 2, canvasHeight / 2 + 30);
        }
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawFadeOverlay() {
        if (stageClearImage == null) {
            gc.setFill(new Color(0, 0, 0, fadeOpacity));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        } else {
            gc.setGlobalAlpha(fadeOpacity);
            gc.drawImage(stageClearImage, 0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setGlobalAlpha(1.0);
        }
    }

}