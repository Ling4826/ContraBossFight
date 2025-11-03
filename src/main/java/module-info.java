module se233.contrabossfight {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.logging;

    opens se233.contrabossfight to javafx.graphics, javafx.fxml;
    exports se233.contrabossfight;
    exports se233.contrabossfight.game;
    exports se233.contrabossfight.character;

    opens se233.contrabossfight.game to javafx.fxml;
}
