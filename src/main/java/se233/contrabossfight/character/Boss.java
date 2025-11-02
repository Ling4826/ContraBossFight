package se233.contrabossfight.character;

import se233.contrabossfight.sprite.AbstractSprite;
import java.util.ArrayList;

public abstract class Boss extends AbstractSprite {

    protected int health;
    protected long scoreValue;

    public Boss(double x, double y, double width, double height, String spriteSheetPath, long scoreValue) {
        super(x, y, width, height, spriteSheetPath);
        this.scoreValue = scoreValue;
        this.isAlive = true;
    }

    public abstract void attack();

    public abstract ArrayList<AbstractSprite> getComponents();

    public abstract void setPlayerPosition(double playerX, double playerY);

}