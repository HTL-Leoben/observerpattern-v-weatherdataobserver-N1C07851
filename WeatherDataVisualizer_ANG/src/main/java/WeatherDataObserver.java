import javafx.scene.layout.VBox;

public interface WeatherDataObserver {
    void update(WeatherData weatherData);
    VBox getRoot();
}
