package se233.contrabossfight.Model.SansPackage;

public class SansAnimationConfig {

    public static final double[][] IDLE_TORSO_OFFSETS = {
            {0, 0}, {0, 0}, {-1, 1}, {-1, 0}, {-1, 0}, {-1, -1},
            {0, 0}, {0, 0}, {0, 0}, {1, 1}, {1, 0}, {1, 0}, {1, -1}
    };

    public static final double[][] IDLE_HEAD_OFFSETS = {
            {0, 0}, {0, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 1},
            {1, 1}, {1, 0}, {1, 0}, {1, 0}, {1, 0}, {1, 0}, {1, 1}
    };

    public static final double[][] ATTACK_UP_HEAD_OFFSETS = {
            {0, 2}, {0, 2}, {0, 1}, {0, 0}, {0, -1}, {0, -1}
    };

    public static final double[][] ATTACK_DOWN_HEAD_OFFSETS = {
            {0, -1}, {0, -1}, {0, 0}, {0, 1}, {0, 2}, {0, 2}
    };

    public static final double[][] ATTACK_SIDE_HEAD_OFFSETS = {
            {0, 0}, {0, 0}, {1, 0}, {2, 0}, {2, 0}, {2, 0}
    };

    public static final double DODGE_DISTANCE = 250;
    public static final double DODGE_DURATION = 300;
    public static final double RETURN_DURATION = 300;
    public static final double DODGE_PAUSE_DURATION = 500;
    public static final double HIT_DELAY = 200;
    public static final double PRE_SHAKE_DELAY = 100;

    public static final double SHAKE_INTENSITY = 8.0;
    public static final int SHAKE_DURATION_MS = 400;
    public static final int SHAKE_INTERVAL_MS = 30;

    public static final int MAX_EXPRESSIONS = 11;
    public static final int INJURED_EXPRESSION = 10;
    public static final long FRAME_DURATION_NS = 60_000_000;

    public static class SpritePositions {
        public static final double BASE_TORSO_X = 0;
        public static final double BASE_TORSO_Y = 0;
        public static final double BASE_HEAD_X = 17;
        public static final double BASE_HEAD_Y = -5;
        public static final double BASE_LEG_X = 0;
        public static final double BASE_LEG_Y = 0;
    }

    public static class AnimationSpeeds {
        public static final double IDLE_SPEED = 0.5;
        public static final double INJURED_SPEED = 0.5;
        public static final double ATTACK_SPEED = 1.0;
    }
}