package se233.contrabossfight.game;

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import se233.contrabossfight.character.*;
import se233.contrabossfight.sprite.AbstractSprite;
import se233.contrabossfight.sprite.Bullet;
import se233.contrabossfight.sprite.Wall;
import se233.contrabossfight.util.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import se233.contrabossfight.sprite.Platform;

public class GameController {
    private static final double TIME_STOP_DURATION = 10.0;
    private static final double TIME_STOP_COOLDOWN = 18.0;
    private static final double GAME_HEIGHT = 600;
    private static final double PLAYER_WIDTH = 64 * 2.6;
    private static final double PLAYER_HEIGHT = 64 * 2.6;
    private static final double STAGE_3_CUTSCENE_DURATION = 2.0;

    private Player player;
    private List<Boss> bosses;
    private List<AbstractSprite> gameObjects;
    private List<Bullet> activeBullets;
    private ConcurrentLinkedQueue<Bullet> newBulletsQueue;
    private List<Platform> platforms;
    private List<Wall> walls;
    private GameStage gameStage;
    private StartTriggerDog annoyingDog;

    private int currentStage = 1;
    private boolean bossFightStarted = false;
    private boolean isCountdownActive = false;
    private double countdownTimer = 3.0;
    private boolean isTimeStopped = false;
    private double timeStopTimer = 0.0;
    private double timeStopCooldown = 0.0;

    private Sans sans;
    private BoundingBox stage3Trigger;
    private boolean isStage3CutsceneActive = false;
    private double cutsceneTimer = 0.0;

    public GameController() {
        this.gameObjects = new ArrayList<>();
        this.bosses = new ArrayList<>();
        this.activeBullets = new ArrayList<>();
        this.newBulletsQueue = new ConcurrentLinkedQueue<>();
        this.platforms = new ArrayList<>();
        this.walls = new ArrayList<>();

        this.player = new Player(
                100,
                GAME_HEIGHT - PLAYER_HEIGHT - 100,
                PLAYER_WIDTH,
                PLAYER_HEIGHT,
                "Player.png",
                newBulletsQueue
        );

        loadStage(this.currentStage);
        Logger.log(Logger.LogType.INFO, "GameController initialized successfully for Stage 1.");
    }

    private void loadStage(int stage) {
        gameObjects.clear();
        bosses.clear();
        platforms.clear();
        activeBullets.clear();
        newBulletsQueue.clear();
        walls.clear();

        this.currentStage = stage;
        this.bossFightStarted = false;
        this.isCountdownActive = false;
        this.countdownTimer = 3.0;

        this.isStage3CutsceneActive = false;
        this.cutsceneTimer = 0.0;
        this.sans = null;
        this.stage3Trigger = null;

        Platform mainGround = null;

        switch (stage) {
            case 1:
                Logger.log(Logger.LogType.INFO, "Loading Stage 1 Platforms...");
                platforms.add(new Platform(55, 420, 155, 20));
                mainGround = new Platform(0, 540, 800, 20);
                mainGround.setDroppable(false);
                platforms.add(mainGround);
                platforms.add(new Platform(40, 300, 165, 20));
                platforms.add(new Platform(260, 385, 45, 20));
                platforms.add(new Platform(340, 460, 40, 20));
                break;

            case 2:
                Logger.log(Logger.LogType.INFO, "Loading Stage 2 Platforms...");
                mainGround = new Platform(0, 510, 800, 20);
                mainGround.setDroppable(false);
                Platform p1 = new Platform(50, 420, 100, 20);
                walls.add(new Wall(100, 450, 50, 100));
                p1.setDroppable(false);
                platforms.add(mainGround);
                platforms.add(p1);
                this.player.setStageBoundaries(0, 250);
                break;

            case 3:
                Logger.log(Logger.LogType.INFO, "Loading Stage 3 Platforms...");
                mainGround = new Platform(0, 480, 800, 20);
                mainGround.setDroppable(false);
                platforms.add(mainGround);
                double sansWidth = 64 * 1.5;
                double sansHeight = 64 * 1.5;
                this.sans = new Sans(
                        550,
                        GAME_HEIGHT - sansHeight - 120,
                        sansWidth,
                        sansHeight
                );

                this.stage3Trigger = new BoundingBox(450, 0, 50, GAME_HEIGHT);
                this.player.setStageBoundaries(0, 9999);
                break;

            default:
                Logger.log(Logger.LogType.WARN, "No platforms defined for stage " + stage + ". Loading default ground.");
                mainGround = new Platform(0, 540, 800, 20);
                mainGround.setDroppable(false);
                platforms.add(mainGround);
                break;
        }

        Logger.log(Logger.LogType.INFO, "--- Loading Stage " + stage + " ---");
        gameObjects.addAll(platforms);
        gameObjects.addAll(walls);

        this.player.respawnst();
        gameObjects.add(player);

        if (this.currentStage != 3) {
            if (this.annoyingDog == null) {
                this.annoyingDog = new StartTriggerDog(
                        300, -20,
                        100 * 1.2, 200 * 1.2,
                        "/se233/contrabossfight/images/AnnoyingDog.png"
                );
            }
            this.annoyingDog.setAlive(true);
            this.annoyingDog.setY(-20);
            gameObjects.add(annoyingDog);
        }

        if (this.currentStage == 3 && this.sans != null) {
            gameObjects.add(this.sans);
        }
    }

    public boolean loadNextStage() {
        if (currentStage < 3) {
            player.addLife(1);
            currentStage++;
            loadStage(currentStage);
            Logger.log(Logger.LogType.INFO, "Loading Next Stage: " + currentStage);
            return true;
        } else {
            Logger.log(Logger.LogType.INFO, "Game Cleared! Returning to menu.");
            return false;
        }
    }

    private void startBossFight() {
        if (bossFightStarted) return;

        if (currentStage == 3) {
            Logger.log(Logger.LogType.INFO, "--- TRANSITION TO UNDERTALE BATTLE ---");
            return;
        }

        Logger.log(Logger.LogType.INFO, "--- BOSS FIGHT STARTED! (Stage " + currentStage + ") ---");
        this.bossFightStarted = true;
        this.annoyingDog.setAlive(false);

        switch (currentStage) {
            case 1:
                Boss1DefenseWall defenseWall = new Boss1DefenseWall(400, 250, newBulletsQueue);
                this.bosses.add(defenseWall);
                break;

            case 2:
                Boss2Java boss2 = new Boss2Java(400, 0, newBulletsQueue);
                this.bosses.add(boss2);
                Logger.log(Logger.LogType.INFO, "Stage 2 Boss Loaded (Java Head)");
                break;

            case 3:
                break;
        }

        this.gameObjects.addAll(bosses);
    }

    public void updateGame(double deltaTime) {
        player.setSilhouetteMode(this.currentStage == 3);
        player.update(deltaTime, platforms, walls);

        while (!newBulletsQueue.isEmpty()) {
            activeBullets.add(newBulletsQueue.poll());
        }

        if (timeStopCooldown > 0) {
            timeStopCooldown -= deltaTime;
        }

        if (isTimeStopped) {
            timeStopTimer -= deltaTime;
            if (timeStopTimer <= 0) {
                isTimeStopped = false;
                Logger.log(Logger.LogType.INFO, "--- TIME RESUMED ---");
            }
        }

        if (isCountdownActive) {
            countdownTimer -= deltaTime;
            if (countdownTimer <= 0) {
                isCountdownActive = false;
                startBossFight();
            }

            activeBullets.removeIf(bullet -> {
                if (!isTimeStopped) {
                    bullet.update(deltaTime);
                }
                return !bullet.isAlive();
            });
        } else if (!bossFightStarted) {
            if (currentStage == 3) {
                updateStage3PreBattle(deltaTime);
            } else {
                updatePreBossFightPhase(deltaTime);
            }
        } else {
            if (currentStage == 3) {
                player.stopMovement();
            } else {
                updateBossFightPhase(deltaTime);
            }
        }
    }

    private void updateStage3PreBattle(double deltaTime) {
        if (isStage3CutsceneActive) {
            cutsceneTimer -= deltaTime;
            if (cutsceneTimer <= 0) {
                isStage3CutsceneActive = false;
                bossFightStarted = true;
                Logger.log(Logger.LogType.INFO, "--- 5s STARE-DOWN OVER. TRANSITION TO BATTLE ---");
            }
        } else if (player.getBoundingBox().intersects(stage3Trigger)) {
            isStage3CutsceneActive = true;
            cutsceneTimer = STAGE_3_CUTSCENE_DURATION;
            player.stopMovement();
            Logger.log(Logger.LogType.INFO, "Player encountered Sans. Stare-down initiated (5s).");
        } else {
            activeBullets.removeIf(bullet -> {
                bullet.update(deltaTime);
                return !bullet.isAlive();
            });
        }
    }

    private void updatePreBossFightPhase(double deltaTime) {
        if (annoyingDog == null) return;
        annoyingDog.update(deltaTime);

        activeBullets.removeIf(bullet -> {
            bullet.update(deltaTime);
            if (bullet.isPlayerBullet() && !bullet.isExploding() &&
                    annoyingDog.isAlive() &&
                    bullet.getBoundingBox().intersects(annoyingDog.getBoundingBox())) {
                bullet.explode();
                this.annoyingDog.setAlive(false);
                this.isCountdownActive = true;
                this.countdownTimer = 3.0;
                Logger.log(Logger.LogType.INFO, "AnnoyingDog hit! Starting boss countdown.");
                return false;
            }
            return !bullet.isAlive();
        });
    }

    private void updateBossFightPhase(double deltaTime) {
        if (!isTimeStopped) {
            updateBosses(deltaTime);
        }

        updateBullets(deltaTime);
        checkPlayerCollisionWithMinions();
    }

    private void updateBosses(double deltaTime) {
        for (Boss boss : bosses) {
            if (boss instanceof Boss1DefenseWall) {
                ((Boss1DefenseWall) boss).setPlayerPosition(player.getX(), player.getY());
            } else if (boss instanceof Boss2Java) {
                ((Boss2Java) boss).setPlayerPosition(player.getX(), player.getY());
                ((Boss2Java) boss).setPlatforms(platforms);
            }
            boss.update(deltaTime);
        }
    }

    private void updateBullets(double deltaTime) {
        activeBullets.removeIf(bullet -> {
            if (!isTimeStopped) {
                bullet.update(deltaTime);
            }

            if (!isTimeStopped) {
                if (bullet.isPlayerBullet()) {
                    if (handlePlayerBulletCollision(bullet)) {
                        return false;
                    }
                } else {
                    if (handleBossBulletCollision(bullet)) {
                        return false;
                    }
                }
            }

            return !bullet.isAlive();
        });
    }

    private boolean handlePlayerBulletCollision(Bullet bullet) {
        for (Boss boss : bosses) {
            if (boss instanceof Boss1DefenseWall) {
                ArrayList<AbstractSprite> components = ((Boss1DefenseWall) boss).getComponents();
                for (AbstractSprite component : components) {
                    if (!bullet.isExploding() && component.isAlive() &&
                            bullet.getBoundingBox().intersects(component.getBoundingBox())) {
                        component.takeDamage(bullet.getDamage());
                        Logger.log(Logger.LogType.DEBUG, "Player bullet hit boss component: "
                                + component.getClass().getSimpleName());
                        player.addScore(10);
                        bullet.explode();
                        return true;
                    }
                }
            } else if (boss instanceof Boss2Java) {
                ArrayList<AbstractSprite> components = ((Boss2Java) boss).getComponents();
                for (AbstractSprite component : components) {
                    if (!bullet.isExploding() && component.isAlive() &&
                            bullet.getBoundingBox().intersects(component.getBoundingBox())) {
                        component.takeDamage(bullet.getDamage());
                        Logger.log(Logger.LogType.DEBUG, "Player bullet hit boss component: "
                                + component.getClass().getSimpleName());
                        if (component instanceof JavaCrawler) {
                            player.addScore(5);
                        } else {
                            player.addScore(30);
                        }

                        bullet.explode();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean handleBossBulletCollision(Bullet bullet) {
        if (!bullet.isExploding() && player.isAlive() && !player.isDying() &&
                !player.isPlayingDeathAnimation() &&
                bullet.getBoundingBox().intersects(player.getBoundingBox())) {
            player.takeHit();
            bullet.explode();
            return true;
        }

        if (!bullet.isExploding()) {
            for (Platform platform : platforms) {
                if (bullet.getBoundingBox().intersects(platform.getBoundingBox()) &&
                        !platform.isDroppable()) {
                    Logger.log(Logger.LogType.DEBUG, "Boss bullet hit platform.");
                    bullet.explode();
                    return true;
                }
            }
        }
        return false;
    }

    private void checkPlayerCollisionWithMinions() {
        if (!isTimeStopped && player.isAlive() && !player.isDying() &&
                !player.isPlayingDeathAnimation() && !player.isInvincible()) {

            for (Boss boss : bosses) {
                if (boss instanceof Boss2Java) {
                    ArrayList<AbstractSprite> components = ((Boss2Java) boss).getComponents();

                    for (AbstractSprite component : components) {
                        if (component instanceof JavaCrawler && component.isAlive()) {
                            if (player.getBoundingBox().intersects(component.getBoundingBox())) {
                                Logger.log(Logger.LogType.INFO, "Player collided with JavaCrawler!");
                                player.takeHit();
                                component.takeDamage(999);
                                break;
                            }
                        }
                    }
                }

                if (!player.isAlive()) {
                    break;
                }
            }
        }
    }

    public boolean activateTimeStop() {
        if (areAllBossesDefeated()) {
            return false;
        }
        if (timeStopCooldown > 0 || isTimeStopped || !bossFightStarted || currentStage == 3) {
            return false;
        }

        isTimeStopped = true;
        timeStopTimer = TIME_STOP_DURATION;
        timeStopCooldown = TIME_STOP_COOLDOWN;
        Logger.log(Logger.LogType.INFO, "--- TIME STOP ACTIVATED! (10s) ---");
        return true;
    }

    public boolean areAllBossesDefeated() {
        if (currentStage == 3) {
            return false;
        }

        if (!bossFightStarted || bosses.isEmpty()) {
            return false;
        }

        for (Boss boss : bosses) {
            if (boss.isAlive()) {
                return false;
            }
        }
        return true;
    }

    public Player getPlayer() {
        return player;
    }

    public List<AbstractSprite> getGameObjects() {
        List<AbstractSprite> allObjects = new ArrayList<>(gameObjects);
        allObjects.addAll(activeBullets);
        return allObjects;
    }

    public boolean isCountdownActive() {
        return this.isCountdownActive;
    }

    public double getCountdownTimer() {
        return this.countdownTimer;
    }

    public boolean isBossFightStarted() {
        return this.bossFightStarted;
    }

    public double getTimeStopTimer() {
        return this.timeStopTimer;
    }

    public boolean isTimeStopped() {
        return this.isTimeStopped;
    }

    public double getTimeStopCooldown() {
        return this.timeStopCooldown;
    }

    public int getCurrentStage() {
        return this.currentStage;
    }

    public boolean isStage3CutsceneActive() {
        return this.isStage3CutsceneActive;
    }
}