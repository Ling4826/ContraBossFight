module se233.contrabossfight {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.logging;

    // ✅ เปิดให้ JavaFX เข้าถึง MainApplication
    opens se233.contrabossfight to javafx.graphics, javafx.fxml;

    // ✅ ส่งออก package หลักและอื่น ๆ ที่ใช้ในเกม
    exports se233.contrabossfight;
    exports se233.contrabossfight.game;
    exports se233.contrabossfight.character;

    // ✅ สำหรับ FXML (เช่น Controller)
    opens se233.contrabossfight.game to javafx.fxml;
}
