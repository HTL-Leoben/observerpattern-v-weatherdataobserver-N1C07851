import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class WeatherVisualizer implements WeatherDataObserver {
    private XYChart.Series<Number, Number> temperatureSeries;
    private LineChart<Number, Number> lineChart;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private Label temperatureLabel;
    private Label conditionLabel;
    private Label timeLabel;
    private ImageView weatherIconView;
    private VBox root;

    private static final int MAX_DATA_POINTS = 20;
    private int dataPointCounter = 0;

    private static final Map<WeatherCondition, String> WEATHER_ICONS = new HashMap<>() {{
        put(WeatherCondition.SUNNY, "/sun-icon.png");
        put(WeatherCondition.CLOUDY, "/cloud-icon.png");
        put(WeatherCondition.RAIN, "/rain-icon.png");
        put(WeatherCondition.SNOW, "/snow-icon.png");
        put(WeatherCondition.NEBLIG, "/fog-icon.png");
        put(WeatherCondition.THUNDERSTORM, "/thunder-icon.png");
    }};

    public WeatherVisualizer() {
        initializeComponents();
    }

    private void initializeComponents() {
        // X-Achse initialisieren
        xAxis = new NumberAxis();
        xAxis.setLabel("Zeit");
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);

        // Y-Achse initialisieren
        yAxis = new NumberAxis();
        yAxis.setLabel("Temperatur (°C)");
        yAxis.setAutoRanging(true);

        // Liniendiagramm erstellen
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Echtzeit-Temperaturverlauf");
        lineChart.setAnimated(false);
        lineChart.setHorizontalGridLinesVisible(true);

        // Temperaturserie initialisieren
        temperatureSeries = new XYChart.Series<>();
        temperatureSeries.setName("Temperatur");
        lineChart.getData().add(temperatureSeries);

        // Labels und Icon erstellen
        temperatureLabel = new Label("Temperatur: N/A");
        conditionLabel = new Label("Wetterbedingung: N/A");
        timeLabel = new Label("Zeit: N/A");

        weatherIconView = new ImageView();
        weatherIconView.setFitWidth(100);
        weatherIconView.setFitHeight(100);

        // Root-Layout erstellen
        root = new VBox(10, lineChart, temperatureLabel, conditionLabel, timeLabel, weatherIconView);
    }
    @Override
    public void update(WeatherData weatherData) {
        Platform.runLater(() -> {
            dataPointCounter++;

            // Füge neuen Datenpunkt hinzu
            temperatureSeries.getData().add(new XYChart.Data<>(
                    dataPointCounter,
                    weatherData.getTemperature()
            ));

            // X-Achse anpassen
            if (dataPointCounter >= MAX_DATA_POINTS) {
                xAxis.setLowerBound(dataPointCounter - MAX_DATA_POINTS);
                xAxis.setUpperBound(dataPointCounter);
            } else {
                xAxis.setLowerBound(0);
                xAxis.setUpperBound(MAX_DATA_POINTS);
            }

            // Labels aktualisieren
            temperatureLabel.setText(String.format("Temperatur: %.1f°C", weatherData.getTemperature()));
            conditionLabel.setText("Wetterbedingung: " + weatherData.getCurrentCondition());
            timeLabel.setText("Zeit: " + weatherData.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")));

            // Wetter-Icon aktualisieren
            String iconPath = WEATHER_ICONS.getOrDefault(weatherData.getCurrentCondition(), "/default-icon.png");
            weatherIconView.setImage(new Image(getClass().getResourceAsStream(iconPath)));
        });
    }

    @Override
    public VBox getRoot() {
        return root;
    }
}
