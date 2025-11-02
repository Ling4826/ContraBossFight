package se233.contrabossfight.Model.Sprite;

import javafx.scene.image.Image;
import se233.contrabossfight.Model.Sprite.Attack.BulletAttackSprite;
import se233.contrabossfight.Model.Sprite.Attack.DamageTextSprite;
import se233.contrabossfight.Model.Sprite.Attack.AttackSprite;
import se233.contrabossfight.Model.Sprite.Character.*;

public class SansSpriteFactory {

    private static final String SANS_SPRITE_PATH = "/se233/contrabossfight/Asset/SansSprite.png";
    private static final String DAMAGE_TEXT_PATH = "/se233/contrabossfight/Asset/Miss_Damage_Sprite.png";
    private static final String BULLET_ATTACK_PATH = "/se233/contrabossfight/Asset/bulletAttackSprite.png";

    private static final Image sansSpriteSheet;
    private static final Image damageTextSheet;
    private static final Image bulletAttackSheet;

    static {
        sansSpriteSheet = new Image(
                SansSpriteFactory.class.getResourceAsStream(SANS_SPRITE_PATH)
        );

        if (sansSpriteSheet.isError()) {
            System.err.println("ERROR: Failed to load Sans sprite sheet from: " + SANS_SPRITE_PATH);
            sansSpriteSheet.getException().printStackTrace();
        } else {
            System.out.println("Sans sprite sheet loaded successfully: " +
                    sansSpriteSheet.getWidth() + "x" + sansSpriteSheet.getHeight());
        }

        damageTextSheet = new Image(
                SansSpriteFactory.class.getResourceAsStream(DAMAGE_TEXT_PATH)
        );

        if (damageTextSheet.isError()) {
            System.err.println("ERROR: Failed to load damage text sprite from: " + DAMAGE_TEXT_PATH);
            damageTextSheet.getException().printStackTrace();
        } else {
            System.out.println("Damage text sprite loaded successfully: " +
                    damageTextSheet.getWidth() + "x" + damageTextSheet.getHeight());
        }

        bulletAttackSheet = new Image(
                SansSpriteFactory.class.getResourceAsStream(BULLET_ATTACK_PATH)
        );

        if (bulletAttackSheet.isError()) {
            System.err.println("ERROR: Failed to load bullet attack sprite from: " + BULLET_ATTACK_PATH);
            bulletAttackSheet.getException().printStackTrace();
        } else {
            System.out.println("Bullet attack sprite loaded successfully: " +
                    bulletAttackSheet.getWidth() + "x" + bulletAttackSheet.getHeight());
        }
    }

    public static HeadSprite createHeadSprite() {
        return new HeadSprite(sansSpriteSheet);
    }

    public static TorsoSprite createTorsoSprite() {
        return new TorsoSprite(sansSpriteSheet);
    }

    public static LegSprite createLegSprite() {
        return new LegSprite(sansSpriteSheet);
    }

    public static AttackSprite createAttackUpSprite(double[][] headOffsets) {
        AttackSprite sprite = new AttackSprite(sansSpriteSheet, 6, 6, 1, 0, 0, 96, 73,
                headOffsets, 17, -5, false);
        sprite.setAnimationSpeed(1.0);
        return sprite;
    }

    public static AttackSprite createAttackDownSprite(double[][] headOffsets) {
        AttackSprite sprite = new AttackSprite(sansSpriteSheet, 6, 6, 1, 0, 73, 96, 73,
                headOffsets, 17, -5, false);
        sprite.setAnimationSpeed(1.0);
        return sprite;
    }

    public static AttackSprite createAttackRightSprite(double[][] headOffsets) {
        AttackSprite sprite = new AttackSprite(sansSpriteSheet, 6, 6, 1, 0, 146, 96, 73,
                headOffsets, 17, -5, false);
        sprite.setAnimationSpeed(1.0);
        return sprite;
    }

    public static AttackSprite createAttackLeftSprite(double[][] headOffsets) {
        AttackSprite sprite = new AttackSprite(sansSpriteSheet, 6, 6, 1, 0, 146, 96, 73,
                headOffsets, 17, -5, true);
        sprite.setAnimationSpeed(1.0);
        return sprite;
    }

    public static IdleSprite createIdleSprite(double[][] torsoOffsets, double[][] headOffsets) {
        IdleSprite sprite = new IdleSprite(
                sansSpriteSheet,
                torsoOffsets, headOffsets,
                0, 0,
                17, -5,
                0, 0
        );
        sprite.setAnimationSpeed(0.5);
        return sprite;
    }

    public static InjuredSprite createInjuredSprite(double[][] torsoOffsets, double[][] headOffsets) {
        InjuredSprite sprite = new InjuredSprite(
                sansSpriteSheet,
                torsoOffsets, headOffsets,
                0, 0,
                17, -5,
                0, 0
        );
        sprite.setAnimationSpeed(0.5);
        return sprite;
    }

    public static DamageTextSprite createMissText() {
        return new DamageTextSprite(damageTextSheet, DamageTextSprite.TextType.MISS);
    }

    public static DamageTextSprite createDamageNumber() {
        return new DamageTextSprite(damageTextSheet, DamageTextSprite.TextType.DAMAGE_999999);
    }

    public static BulletAttackSprite createBulletAttack(int frameCount, int columns, int rows) {
        return new BulletAttackSprite(bulletAttackSheet, frameCount, columns, rows);
    }
}