package se233.contrabossfight.game;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import se233.contrabossfight.util.Logger;

public class BattleButton extends Group {

    private ImageView normalView;
    private ImageView highlightedView;
    private boolean isSelected = false;

    public BattleButton(String normalImagePath, String highlightedImagePath) {
        try {
            Image normalImg = new Image(getClass().getResourceAsStream(normalImagePath));
            Image highlightedImg = new Image(getClass().getResourceAsStream(highlightedImagePath));

            normalView = new ImageView(normalImg);
            highlightedView = new ImageView(highlightedImg);

            highlightedView.setVisible(false);
            this.getChildren().addAll(normalView, highlightedView);

        } catch (Exception e) {
            Logger.log(Logger.LogType.FATAL, "Failed to load button images: " + normalImagePath, e);
        }
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        if (normalView == null || highlightedView == null) return;

        normalView.setVisible(!selected);
        highlightedView.setVisible(selected);
    }

    public boolean isSelected() {
        return isSelected;
    }
}