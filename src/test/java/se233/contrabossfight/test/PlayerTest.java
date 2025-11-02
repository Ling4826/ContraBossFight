package se233.contrabossfight.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import se233.contrabossfight.character.Player;
import se233.contrabossfight.sprite.Bullet;
import se233.contrabossfight.sprite.Platform;
import se233.contrabossfight.sprite.WeaponType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

class PlayerTest {
    private static final double PLAYER_WIDTH = 64 * 2.6;
    private static final double PLAYER_HEIGHT = 64 * 2.6;
    private static final double MOVE_SPEED = 100.0;
    private static final double JUMP_VELOCITY = -600.0;
    private static final double GRAVITY = 1200.0;
    private static final double TEST_DELTA_TIME = 0.1;

    private Player player;
    private ConcurrentLinkedQueue<Bullet> bulletQueue;
    private List<Platform> platforms;

    @BeforeEach
    void setUp() {
        bulletQueue = new ConcurrentLinkedQueue<>();
        platforms = new ArrayList<>();
        player = new Player(100, 100, PLAYER_WIDTH, PLAYER_HEIGHT, "dummy.png", bulletQueue);
    }

    @Test
    void testMoveLeft() {
        double startX = player.getX();
        player.moveLeft(true);
        player.update(TEST_DELTA_TIME, platforms, new ArrayList<>());
        assertEquals(80.0, player.getX(), 0.01, "Player should move left");
    }

    @Test
    void testMoveRight() {
        double startX = player.getX();
        player.moveRight(true);
        player.update(TEST_DELTA_TIME, platforms, new ArrayList<>());
        assertEquals(120.0, player.getX(), 0.01, "Player should move right");
    }

    @Test
    void testJump() {
        Platform ground = new Platform(0, 500, 800, 20);
        platforms.add(ground);
        player.setY(500 - PLAYER_HEIGHT);

        player.update(0.01, platforms, new ArrayList<>());
        assertEquals(333.6, player.getY(), 0.01, "Player should be on the platform");

        player.jump();
        player.update(TEST_DELTA_TIME, platforms, new ArrayList<>());
        assertEquals(285.6, player.getY(), 0.01, "Player's Y position should decrease after jumping");
    }

    @Test
    void testProneStatePreventsJump() {
        Platform ground = new Platform(0, 500, 800, 20);
        platforms.add(ground);

        double playerHeight = player.getHeight();
        player.setY(ground.getY() - playerHeight - 0.1);

        player.update(0.02, platforms, new ArrayList<>());
        assertEquals(ground.getY() - playerHeight, player.getY(), 0.001, "Player should be sitting on the platform after landing");
        assertNotNull(player.getCurrentPlatform(), "Player should have current platform set");

        player.aimDown(true);
        player.update(0.02, platforms, new ArrayList<>());
        assertTrue(player.isProne(), "Player should be prone after aiming down on the ground");

        double yBeforeJump = player.getY();
        player.jump();
        assertEquals(yBeforeJump, player.getY(), 3.0, "Player should not move vertically when trying to jump while prone");
    }

    @Test
    void testShootCreatesBullet() {
        assertEquals(0, bulletQueue.size(), "Bullet queue should be empty initially");
        player.shoot();
        assertEquals(1, bulletQueue.size(), "Bullet queue should contain one bullet after shooting");
    }

    @Test
    void testShootBulletProperties() {
        player.shoot();
        Bullet bullet = bulletQueue.poll();

        assertNotNull(bullet, "Bullet should not be null");
        assertTrue(bullet.isPlayerBullet(), "Bullet should be a player bullet");
        assertEquals(10, bullet.getDamage(), 0.01, "Bullet damage should be 10 (for NORMAL type)");
        assertEquals(600, bullet.getVelocityX(), 0.01, "Bullet should move right");
        assertEquals(0, bullet.getVelocityY(), 0.01, "Bullet should have no initial Y velocity");
    }

    @Test
    void testAddScore() {
        assertEquals(0, player.getScore(), "Initial score should be 0");

        player.addScore(100);
        assertEquals(100, player.getScore(), "Score should be 100 after adding points");

        player.addScore(50);
        assertEquals(150, player.getScore(), "Score should be 150 after adding more points");
    }

    @Test
    void testTakeHit() {
        assertEquals(3, player.getLives(), "Player should start with 3 lives");

        player.takeHit();
        assertEquals(2, player.getLives(), "Player should have 2 lives after being hit");
        assertTrue(player.isPlayingDeathAnimation(), "Player should be playing death animation after hit");
    }

    @Test
    void testTakeHitWhileInvincible() {
        assertEquals(3, player.getLives(), "Player should start with 3 lives");

        player.setInvincible(true);
        player.takeHit();

        assertEquals(3, player.getLives(), "Player should not lose a life while invincible");
        assertFalse(player.isPlayingDeathAnimation(), "Player should not start death animation while invincible");
    }
}