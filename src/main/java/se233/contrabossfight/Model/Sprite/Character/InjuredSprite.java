package se233.contrabossfight.Model.Sprite.Character;

import javafx.scene.Group;
import javafx.scene.image.Image;

public class InjuredSprite extends Group {
    private HeadSprite headSprite;
    private TorsoSprite torsoSprite;
    private LegSprite legSprite;

    private int ticksPerFrame = 2;
    private int tickCounter = 0;

    private final double[][] torsoOffsets;
    private int torsoFrame = 0;
    private double baseTorsoX;
    private double baseTorsoY;

    private final double[][] headOffsets;
    private int headFrame = 0;
    private double baseHeadX;
    private double baseHeadY;

    private double baseLegX;
    private double baseLegY;

    public InjuredSprite(Image spriteSheet,
                         double[][] torsoOffsets, double[][] headOffsets,
                         double baseTorsoX, double baseTorsoY,
                         double baseHeadX, double baseHeadY,
                         double baseLegX, double baseLegY) {
        this.headSprite = new HeadSprite(spriteSheet);
        this.torsoSprite = new TorsoSprite(spriteSheet);
        this.legSprite = new LegSprite(spriteSheet);

        this.torsoOffsets = torsoOffsets;
        this.headOffsets = headOffsets;

        this.baseTorsoX = baseTorsoX;
        this.baseTorsoY = baseTorsoY;
        this.baseHeadX = baseHeadX;
        this.baseHeadY = baseHeadY;
        this.baseLegX = baseLegX;
        this.baseLegY = baseLegY;

        torsoSprite.setTranslateX(baseTorsoX);
        torsoSprite.setTranslateY(baseTorsoY);
        headSprite.setTranslateX(baseHeadX);
        headSprite.setTranslateY(baseHeadY);
        legSprite.setTranslateX(baseLegX);
        legSprite.setTranslateY(baseLegY);

        torsoSprite.setPose(3);
        legSprite.setPose(1);

        this.getChildren().addAll(legSprite, torsoSprite, headSprite);

        updatePositions();
    }

    public void tick() {
        tickCounter++;
        if (tickCounter >= ticksPerFrame) {
            torsoFrame = (torsoFrame + 1) % torsoOffsets.length;
            headFrame = (headFrame + 1) % headOffsets.length;

            updatePositions();
            tickCounter = 0;
        }
    }

    private void updatePositions() {
        double[] torsoOffset = torsoOffsets[torsoFrame];
        torsoSprite.setTranslateX(baseTorsoX + torsoOffset[0]);
        torsoSprite.setTranslateY(baseTorsoY + torsoOffset[1]);

        double[] headOffset = headOffsets[headFrame];
        headSprite.setTranslateX(baseHeadX + headOffset[0]);
        headSprite.setTranslateY(baseHeadY + headOffset[1]);

        legSprite.setTranslateX(baseLegX);
        legSprite.setTranslateY(baseLegY);
    }

    public void reset() {
        torsoFrame = 0;
        headFrame = 0;
        tickCounter = 0;
        updatePositions();
    }

    public void setAnimationSpeed(double speed) {
        this.ticksPerFrame = Math.max(1, (int)(1 / speed));
    }

    public HeadSprite getHeadSprite() {
        return headSprite;
    }

    public TorsoSprite getTorsoSprite() {
        return torsoSprite;
    }

    public LegSprite getLegSprite() {
        return legSprite;
    }

    public void setTorsoPose(int index) {
        torsoSprite.setPose(index);
    }

    public void setLegPose(int index) {
        legSprite.setPose(index);
    }
}