package se233.contrabossfight;

import javafx.application.Application;
import javafx.stage.Stage;
import se233.contrabossfight.game.GameStage;
import se233.contrabossfight.util.Logger;

public class MainApplication extends Application {

    public static final double WIDTH = 800;
    public static final double HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) {
        Logger.log(Logger.LogType.INFO, "Application starting up.");

        try {
            GameStage gameStage = new GameStage(WIDTH, HEIGHT);

            primaryStage.setTitle("SE233 Contra Boss Fight");
            primaryStage.setScene(gameStage.getScene());
            primaryStage.setResizable(false);
            primaryStage.show();

            gameStage.start();
        } catch (Exception e) {
            Logger.log(Logger.LogType.FATAL, "Failed to start application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}