package se233.contrabossfight.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * คลาสสำหรับจัดการ Logging ตามข้อกำหนดของโปรเจกต์
 */
public class Logger {
    private static final String LOG_FILE = "game_log.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final LogType LOG_LEVEL = LogType.DEBUG; // กำหนด log level ที่นี่

    public enum LogType {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL // Log Level ต่างๆ
    }

    /**
     * เมธอดสำหรับบันทึก Log
     * @param type ชนิดของ Log (เช่น INFO, WARN)
     * @param message ข้อความ Log
     */
    public static void log(LogType type, String message) {
        if (type.ordinal() >= LOG_LEVEL.ordinal()) { // ตรวจสอบ Log Level
            String timestamp = LocalDateTime.now().format(FORMATTER);
            String logEntry = String.format("[%s] [%s] %s", timestamp, type.name(), message);

            // พิมพ์ใน Console
            System.out.println(logEntry);

            // เขียนลงไฟล์
            try (FileWriter fw = new FileWriter(LOG_FILE, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                pw.println(logEntry);
            } catch (IOException e) {
                System.err.println("Error writing to log file: " + e.getMessage());
            }
        }
    }
}