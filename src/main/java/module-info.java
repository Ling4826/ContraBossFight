module se233.contrabossfight {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media; // 👈 (อันนี้ที่คุณเพิ่ม ถูกต้องแล้ว)
    requires java.logging;
    // --- ✨ START: เพิ่มบรรทัดนี้สำหรับแก้ Error 'IllegalAccessException' ---
    // (อนุญาตให้ javafx.graphics เข้าถึง MainApplication)
    opens se233.contrabossfight to javafx.graphics;
    // --- ✨ END: ---

    // (ที่เหลือเหมือนเดิม)
    exports se233.contrabossfight.game;
    exports se233.contrabossfight.character;
    opens se233.contrabossfight.game to javafx.fxml;
}
