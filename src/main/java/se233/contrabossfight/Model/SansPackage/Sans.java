package se233.contrabossfight.Model.SansPackage;

import javafx.animation.AnimationTimer;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.util.Duration;
import se233.contrabossfight.Model.Sprite.*;
import se233.contrabossfight.Model.Sprite.Attack.AttackSprite;
import se233.contrabossfight.Model.Sprite.Character.HeadSprite;
import se233.contrabossfight.Model.Sprite.Character.IdleSprite;
import se233.contrabossfight.Model.Sprite.Character.InjuredSprite;

import java.util.Random;

import static se233.contrabossfight.Model.SansPackage.SansAnimationConfig.*;

public class Sans extends Group {
    public enum State {
        IDLE,
        ATTACKING,
        DODGING,
        RETURNING,
        INJURED,
        HIT_SHAKING
    }

    private IdleSprite idleSprite;
    private InjuredSprite injuredSprite;
    private AttackSprite attackUp;
    private AttackSprite attackDown;
    private AttackSprite attackRight;
    private AttackSprite attackLeft;
    private int currentExpressionIndex = 0;

    private State currentState = State.IDLE;
    private boolean isAttacking = false;
    private String attackDirection = "right";

    private double originalX = 0;
    private double originalY = 0;

    private Timeline shakeTimeline;
    private Random random = new Random();
    private double baseShakeX;
    private double baseShakeY;

    private AnimationTimer timer;
    private long lastFrameTime = 0;

    private boolean eyeFlickerEnabled = false;

    private Runnable onDodgeComplete;
    private Runnable onHitComplete;

    public Sans() {
        idleSprite = SansSpriteFactory.createIdleSprite(IDLE_TORSO_OFFSETS, IDLE_HEAD_OFFSETS);
        injuredSprite = SansSpriteFactory.createInjuredSprite(IDLE_TORSO_OFFSETS, IDLE_HEAD_OFFSETS);
        attackUp = SansSpriteFactory.createAttackUpSprite(ATTACK_UP_HEAD_OFFSETS);
        attackDown = SansSpriteFactory.createAttackDownSprite(ATTACK_DOWN_HEAD_OFFSETS);
        attackRight = SansSpriteFactory.createAttackRightSprite(ATTACK_SIDE_HEAD_OFFSETS);
        attackLeft = SansSpriteFactory.createAttackLeftSprite(ATTACK_SIDE_HEAD_OFFSETS);

        this.getChildren().add(idleSprite);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastFrameTime >= FRAME_DURATION_NS) {
                    update();
                    lastFrameTime = now;
                }
            }
        };
    }

    public void nextExpression() {
        currentExpressionIndex = (currentExpressionIndex + 1) % MAX_EXPRESSIONS;
        setExpression(currentExpressionIndex);
    }

    public void previousExpression() {
        currentExpressionIndex--;
        if (currentExpressionIndex < 0) {
            currentExpressionIndex = MAX_EXPRESSIONS - 1;
        }
        setExpression(currentExpressionIndex);
    }

    public void startIdleAnimation() {
        if (currentState == State.DODGING || currentState == State.RETURNING || currentState == State.HIT_SHAKING) {
            return;
        }

        currentState = State.IDLE;
        isAttacking = false;
        eyeFlickerEnabled = false;
        idleSprite.reset();
        idleSprite.getHeadSprite().stopExpressionFlickerEye();

        this.getChildren().clear();
        this.getChildren().add(idleSprite);

        if (originalX != 0 || originalY != 0) {
            this.setTranslateX(originalX);
            this.setTranslateY(originalY);
        }
        timer.start();
    }

    public void startInjuredAnimation() {
        currentState = State.INJURED;
        isAttacking = false;
        injuredSprite.reset();

        this.getChildren().clear();
        this.getChildren().add(injuredSprite);

        currentExpressionIndex = INJURED_EXPRESSION;
        injuredSprite.getHeadSprite().setExpression(INJURED_EXPRESSION);

        timer.start();
    }

    public void stopAnimation() {
        timer.stop();
    }

    public void startAttack(String direction) {

        if (currentState == State.DODGING || currentState == State.RETURNING || currentState == State.HIT_SHAKING) {
            return;
        }

        currentState = State.ATTACKING;
        isAttacking = true;
        attackDirection = direction.toLowerCase();

        AttackSprite currentAttack = switch (attackDirection) {
            case "up" -> attackUp;
            case "down" -> attackDown;
            case "left" -> attackLeft;
            case "right" -> attackRight;
            default -> attackRight;
        };

        currentAttack.reset();

        this.getChildren().clear();
        this.getChildren().add(currentAttack);

        timer.start();
    }

    public void startDodgeSequence(Runnable onComplete) {
        if (currentState != State.IDLE) {
            return;
        }

        this.onDodgeComplete = onComplete;
        currentState = State.DODGING;

        originalX = this.getTranslateX();
        originalY = this.getTranslateY();

        if (!this.getChildren().contains(idleSprite)) {
            this.getChildren().clear();
            this.getChildren().add(idleSprite);
        }

        Timeline dodgeTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(this.translateXProperty(), originalX, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(DODGE_DURATION),
                        new KeyValue(this.translateXProperty(), originalX - DODGE_DISTANCE, Interpolator.EASE_OUT))
        );

        dodgeTimeline.setOnFinished(e -> {
            Timeline pauseTimeline = new Timeline(
                    new KeyFrame(Duration.millis(DODGE_PAUSE_DURATION), event -> startReturnSequence())
            );
            pauseTimeline.play();
        });

        dodgeTimeline.play();
    }

    public void startHitSequence(Runnable onComplete) {
        if (currentState != State.IDLE) {
            return;
        }

        this.onHitComplete = onComplete;
        currentState = State.DODGING;

        originalX = this.getTranslateX();
        originalY = this.getTranslateY();

        if (!this.getChildren().contains(idleSprite)) {
            this.getChildren().clear();
            this.getChildren().add(idleSprite);
        }

        Timeline dodgeTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(this.translateXProperty(), originalX, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(DODGE_DURATION),
                        new KeyValue(this.translateXProperty(), originalX - DODGE_DISTANCE, Interpolator.EASE_OUT))
        );

        dodgeTimeline.setOnFinished(e -> {
            Timeline hitDelayTimeline = new Timeline(
                    new KeyFrame(Duration.millis(HIT_DELAY), event -> startHitImpact())
            );
            hitDelayTimeline.play();
        });

        dodgeTimeline.play();
    }

    private void startHitImpact() {
        currentState = State.INJURED;
        this.getChildren().clear();
        this.getChildren().add(injuredSprite);

        currentExpressionIndex = INJURED_EXPRESSION;
        injuredSprite.getHeadSprite().setExpression(INJURED_EXPRESSION);

        Timeline preShakeDelay = new Timeline(
                new KeyFrame(Duration.millis(PRE_SHAKE_DELAY), e -> startShakeEffect())
        );
        preShakeDelay.play();
    }

    private void startShakeEffect() {
        currentState = State.HIT_SHAKING;

        baseShakeX = this.getTranslateX();
        baseShakeY = this.getTranslateY();

        int shakeCycles = SHAKE_DURATION_MS / SHAKE_INTERVAL_MS;

        shakeTimeline = new Timeline(
                new KeyFrame(Duration.millis(SHAKE_INTERVAL_MS), e -> shake())
        );
        shakeTimeline.setCycleCount(shakeCycles);

        shakeTimeline.setOnFinished(e -> {
            this.setTranslateX(baseShakeX);
            this.setTranslateY(baseShakeY);
            currentState = State.INJURED;

            if (onHitComplete != null) {
                onHitComplete.run();
            }
        });

        shakeTimeline.play();
    }

    private void shake() {
        double offsetX = (random.nextDouble() - 0.5) * SHAKE_INTENSITY * 2;
        double offsetY = (random.nextDouble() - 0.5) * SHAKE_INTENSITY * 2;

        this.setTranslateX(baseShakeX + offsetX);
        this.setTranslateY(baseShakeY + offsetY);
    }

    private void startReturnSequence() {
        currentState = State.RETURNING;

        Timeline returnTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(this.translateXProperty(), originalX - DODGE_DISTANCE, Interpolator.EASE_IN)),
                new KeyFrame(Duration.millis(RETURN_DURATION),
                        new KeyValue(this.translateXProperty(), originalX, Interpolator.EASE_IN))
        );

        returnTimeline.setOnFinished(e -> {
            currentState = State.IDLE;
            if (onDodgeComplete != null) {
                onDodgeComplete.run();
            }
        });

        returnTimeline.play();
    }

    public void toggleEyeFlicker() {
        eyeFlickerEnabled = !eyeFlickerEnabled;
        if (isAttacking) {
            AttackSprite current = switch (attackDirection) {
                case "up" -> attackUp;
                case "down" -> attackDown;
                case "left" -> attackLeft;
                case "right" -> attackRight;
                default -> attackRight;
            };
            HeadSprite attackHead = current.getHeadSprite();
            if (eyeFlickerEnabled) {
                attackHead.startExpressionFlickerEye();
            } else {
                attackHead.stopExpressionFlickerEye();
            }
        } else if (currentState == State.IDLE) {
            HeadSprite idleHead = idleSprite.getHeadSprite();
            if (eyeFlickerEnabled) {
                idleHead.startExpressionFlickerEye();
            } else {
                idleHead.stopExpressionFlickerEye();
            }
        } else if (currentState == State.INJURED) {
            HeadSprite injuredHead = injuredSprite.getHeadSprite();
            if (eyeFlickerEnabled) {
                injuredHead.startExpressionFlickerEye();
            } else {
                injuredHead.stopExpressionFlickerEye();
            }
        }
    }

    public void update() {
        if (currentState == State.ATTACKING) {
            AttackSprite currentAttack = switch (attackDirection) {
                case "up" -> attackUp;
                case "down" -> attackDown;
                case "left" -> attackLeft;
                case "right" -> attackRight;
                default -> attackRight;
            };

            currentAttack.tick();

            if (eyeFlickerEnabled) {
                currentAttack.getHeadSprite().tickFlicking();
            }
        } else if (currentState == State.IDLE || currentState == State.DODGING || currentState == State.RETURNING) {
            idleSprite.tick();

            if (eyeFlickerEnabled) {
                idleSprite.getHeadSprite().tickFlicking();
            }
        } else if (currentState == State.HIT_SHAKING) {
            injuredSprite.tick();

            if (eyeFlickerEnabled) {
                injuredSprite.getHeadSprite().tickFlicking();
            }
        }
    }

    public void setExpression(int index) {
        if (isAttacking) {
            AttackSprite current = switch (attackDirection) {
                case "up" -> attackUp;
                case "down" -> attackDown;
                case "left" -> attackLeft;
                case "right" -> attackRight;
                default -> attackRight;
            };
            current.getHeadSprite().setExpression(index);
        } else if (currentState == State.IDLE) {
            idleSprite.getHeadSprite().setExpression(index);
        } else if (currentState == State.INJURED) {
            injuredSprite.getHeadSprite().setExpression(index);
        }
    }

    public void setTorsoPose(int index) {
        if (!isAttacking && currentState == State.IDLE) {
            idleSprite.setTorsoPose(index);
        }
    }

    public void setLegPose(int index) {
        if (!isAttacking && currentState == State.IDLE) {
            idleSprite.setLegPose(index);
        }
    }

    public void setIdleSpeed(double speed) {
        idleSprite.setAnimationSpeed(speed);
    }

    public void setInjuredSpeed(double speed) {
        injuredSprite.setAnimationSpeed(speed);
    }

    public void setAttackUpSpeed(double speed) {
        attackUp.setAnimationSpeed(speed);
    }

    public void setAttackDownSpeed(double speed) {
        attackDown.setAnimationSpeed(speed);
    }

    public void setAttackRightSpeed(double speed) {
        attackRight.setAnimationSpeed(speed);
    }

    public void setAttackLeftSpeed(double speed) {
        attackLeft.setAnimationSpeed(speed);
    }

    public int getCurrentExpressionIndex() {
        return currentExpressionIndex;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public State getCurrentState() {
        return currentState;
    }

    public double getOriginalX() {
        return originalX;
    }

    public double getOriginalY() {
        return originalY;
    }
}