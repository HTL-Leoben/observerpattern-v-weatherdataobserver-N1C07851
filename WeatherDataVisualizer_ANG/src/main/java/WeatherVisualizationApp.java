import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalDate;

public class WeatherVisualizationApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Visualizer erstellen
        WeatherDataObserver visualizer = new WeatherVisualizer();

        // Simulator mit Visualizer verbinden
        WeatherDataSimulator simulator = new WeatherDataSimulator(LocalDate.of(2025, 6,1), 60);
        simulator.registerObserver(visualizer);

        // Szene erstellen
        for (WeatherDataObserver ob : simulator.getVisualizers()) {
            Scene scene = new Scene(ob.getRoot(), 800, 600);
            primaryStage.setTitle("Wetter-Visualisierung");
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}