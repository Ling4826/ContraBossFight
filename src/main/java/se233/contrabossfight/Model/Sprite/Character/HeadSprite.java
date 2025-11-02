package se233.contrabossfight.Model.Sprite.Character;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import se233.contrabossfight.Model.Sprite.Base.AnimatedSprite;

public class HeadSprite extends AnimatedSprite {
    private int expressionIndex = 0;
    private static final int TOTAL_EXPRESSIONS = 13;

    private boolean isFlicking = false;
    private boolean flickToggle = false;
    private int flickCounter = 0;
    private static final int FLICK_INTERVAL = 2;

    public HeadSprite(Image spriteSheet) {
        super(spriteSheet, TOTAL_EXPRESSIONS, 8, 1, 0, 292, 36, 36);
    }

    public void setExpression(int index) {
        if (index >= 0 && index < TOTAL_EXPRESSIONS) {
            this.expressionIndex = index;
            this.setViewport(getViewportForIndex(index));
        }
    }

    private Rectangle2D getViewportForIndex(int index) {
        int x = (index % 13) * 36;
        int y = 292 + (index / 13) * 36;
        return new Rectangle2D(x, y, 36, 36);
    }

    public void startExpressionFlickerEye() {
        isFlicking = true;
        flickCounter = 0;
        flickToggle = false;
    }

    public void stopExpressionFlickerEye() {
        isFlicking = false;
        setExpression(0);
    }

    public void tickFlicking() {
        if (!isFlicking) return;
        flickCounter++;
        if (flickCounter % FLICK_INTERVAL == 0) {
            flickToggle = !flickToggle;
            setExpression(flickToggle ? 12 : 11);
        }
    }
}