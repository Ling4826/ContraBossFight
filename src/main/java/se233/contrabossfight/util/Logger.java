package se233.contrabossfight.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public class Logger {

    private static final java.util.logging.Logger JUL_LOGGER =
            java.util.logging.Logger.getLogger(Logger.class.getName());

    private static boolean isInitialized = false;

    public enum LogType {
        DEBUG,
        INFO,
        WARN,
        FATAL,
        ERROR
    }

    public static void setup() {
        if (isInitialized) return;
        try {

            Path logDirectory = Paths.get("logs");
            if (!Files.exists(logDirectory)) {
                Files.createDirectories(logDirectory);
                System.out.println("Logger: Created 'logs' directory successfully.");
            }

            JUL_LOGGER.setUseParentHandlers(true);
            FileHandler fileHandler = new FileHandler("logs/game.log", 1024 * 1024, 5, true);

            fileHandler.setFormatter(new SimpleFormatter());
            JUL_LOGGER.addHandler(fileHandler);
            JUL_LOGGER.setLevel(Level.INFO);

            isInitialized = true;
            log(LogType.INFO, "Logger initialized. Logging to 'logs/game.log'");

        } catch (IOException e) {
            System.err.println("CRITICAL: Could not initialize logger file handler.");
            e.printStackTrace();
        }
    }

    public static void log(LogType type, String message) {
        if (!isInitialized) setup();
        JUL_LOGGER.log(mapLevel(type), message);
    }

    public static void log(LogType type, String message, Throwable thrown) {
        if (!isInitialized) setup();
        JUL_LOGGER.log(mapLevel(type), message, thrown);
    }

    private static Level mapLevel(LogType type) {
        switch (type) {
            case DEBUG:
                return Level.FINE;
            case INFO:
                return Level.INFO;
            case WARN:
                return Level.WARNING;
            case ERROR:
            case FATAL:
                return Level.SEVERE;
            default:
                return Level.CONFIG;
        }
    }
}