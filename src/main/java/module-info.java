module se233.contrabossfight {
    requires javafx.controls;
    requires javafx.fxml;


    opens se233.contrabossfight to javafx.fxml;
    exports se233.contrabossfight;
}