import javafx.animation.AnimationTimer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class WeatherDataSimulator {

    private List<WeatherDataObserver> visualizers;
    private Random random;
    private double lastTemperature;
    private int intervalMinutes;
    private LocalDateTime lastTimestamp;
    private Season currentSeason;

    // Enum für Jahreszeiten bleibt unverändert
    public enum Season {
        SPRING(Month.MARCH, Month.MAY),
        SUMMER(Month.JUNE, Month.AUGUST),
        AUTUMN(Month.SEPTEMBER, Month.NOVEMBER),
        WINTER(Month.DECEMBER, Month.FEBRUARY);

        private final Month startMonth;
        private final Month endMonth;

        Season(Month startMonth, Month endMonth) {
            this.startMonth = startMonth;
            this.endMonth = endMonth;
        }

        public static Season getCurrentSeason(LocalDateTime dateTime) {
            Month month = dateTime.getMonth();
            for (Season season : values()) {
                if ((month.equals(season.startMonth) || month.compareTo(season.startMonth) > 0) &&
                        (month.equals(season.endMonth) || month.compareTo(season.endMonth) < 0)) {
                    return season;
                }
            }
            // Für Dezember (Winter über Jahreswechsel)
            return WINTER;
        }
    }

    // Konstruktor bleibt unverändert
    public WeatherDataSimulator(LocalDate startDate, int intervalMinutes) {
        this.visualizers = new LinkedList<>();

        this.lastTimestamp = LocalDateTime.of(startDate.getYear(), startDate.getMonth(), startDate.getDayOfMonth(),0,0);
        this.currentSeason = Season.getCurrentSeason(lastTimestamp);

        this.random = new Random();
        this.intervalMinutes = intervalMinutes;
        this.lastTemperature = getInitialTemperatureForSeason(currentSeason);
        startWeatherDataSimulation();
    }

    public void registerObserver(WeatherDataObserver observer) {
        this.visualizers.add(observer);
    }

    public void removeObserver(WeatherDataObserver observer) {
        this.visualizers.remove(observer);
    }

    private void notifyObserver(WeatherData currentWeatherData) {
        for (WeatherDataObserver observer : visualizers) {
            observer.update(currentWeatherData);
        }
    }

    public List<WeatherDataObserver> getVisualizers() {
        return visualizers;
    }

    private double getInitialTemperatureForSeason(Season season) {
        switch (season) {
            case WINTER:
                // Temperaturbereich: -15 bis 15°C
                return random.nextDouble() * 30 - 15;
            case SPRING:
            case AUTUMN:
                // Temperaturbereich: 5 bis 25°C
                return random.nextDouble() * 20 + 5;
            case SUMMER:
                // Temperaturbereich: 15 bis 45°C
                return random.nextDouble() * 30 + 15;
            default:
                return 15.0;
        }
    }

    private double calculateTemperatureChange(WeatherCondition condition, LocalDateTime timestamp, Season season) {
        double baseChange = random.nextDouble() * 1.5 - 0.75;
        int hour = timestamp.getHour();

        // Temperaturänderung basierend auf Tageszeit
        if (hour >= 22 || hour < 6) {
            // Nachttemperaturen sinken
            baseChange -= (season == Season.SUMMER? -1.5 : -0.5);
        } else if (hour >= 10 && hour < 16) {
            // Mittagshitze
            baseChange += 0.7;
        }

        // Saisonale Temperaturanpassungen und Grenzen
        switch (season) {
            case WINTER:
                // Begrenzen auf -15 bis 15°C
                if (lastTemperature > 15) baseChange -= 0.5;
                if (lastTemperature < -15) baseChange += 0.5;
                baseChange -= 0.3;
                break;
            case SUMMER:
                // Begrenzen auf 15 bis 45°C
                if (lastTemperature > 45) baseChange -= 1.5;
                if (lastTemperature < 15) baseChange += 1.5;
                baseChange += 0.3;
                break;
            case SPRING:
            case AUTUMN:
                // Begrenzen auf 5 bis 25°C
                if (lastTemperature > 25) baseChange -= 0.3;
                if (lastTemperature < 5) baseChange += 0.3;
                break;
        }

        // Spezielle Bedingungen für Temperaturänderung
        switch (condition) {
            case THUNDERSTORM:
                return baseChange - 4.5;
            case SNOW:
                return baseChange - 0.5;
            case SUNNY:
                return baseChange + 0.9;
            case CLOUDY:
                return baseChange - 1.3;
            case RAIN:
                return baseChange - 2.2;
            default:
                return baseChange;
        }
    }

    // Restliche Methoden bleiben unverändert
    private void startWeatherDataSimulation() {
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 2_000_000_000L) {
                    WeatherData currentWeatherData = generateRealisticWeatherData();
                    notifyObserver(currentWeatherData);
                    lastUpdate = now;
                }
            }
        };
        timer.start();
    }

    private WeatherData generateRealisticWeatherData() {
        // Restliche Implementierung bleibt unverändert
        LocalDateTime newTimestamp = lastTimestamp.plusMinutes(intervalMinutes);
        Season season = Season.getCurrentSeason(newTimestamp);

        WeatherCondition condition = generateWeatherCondition(newTimestamp, season);

        double temperatureChange = calculateTemperatureChange(condition, newTimestamp, season);
        double temperature = lastTemperature + temperatureChange;

        // Temperatur nach Saisonbereichen beschränken
        switch (season) {
            case WINTER:
                temperature = Math.max(-15, Math.min(15, temperature));
                break;
            case SUMMER:
                temperature = Math.max(15, Math.min(45, temperature));
                break;
            case SPRING:
            case AUTUMN:
                temperature = Math.max(5, Math.min(25, temperature));
                break;
        }

        int rainProbability = calculateRainProbability(condition, season, temperature);

        // Restliche Wetterdaten-Logik bleibt unverändert
        if (condition == WeatherCondition.SNOW && temperature > 2.0) {
            condition = WeatherCondition.CLOUDY;
        }

        if (temperature < 3.0) {
            condition = (random.nextDouble() < 0.5) ? WeatherCondition.SNOW : WeatherCondition.CLOUDY;
        }

        CurrentWeatherData weatherData = new CurrentWeatherData(
                temperature,
                rainProbability,
                condition,
                newTimestamp
        );

        lastTemperature = temperature;
        lastTimestamp = newTimestamp;
        currentSeason = season;

        return weatherData;
    }

    // Restliche Methoden (generateWeatherCondition, calculateRainProbability) bleiben unverändert
    private WeatherCondition generateWeatherCondition(LocalDateTime timestamp, Season season) {
        int hour = timestamp.getHour();
        double conditionProbability = random.nextDouble();

        // Keine Sonne in der Nacht (22 Uhr bis 6 Uhr)
        if (hour >= 22 || hour < 6) {
            if (conditionProbability < 0.2) return WeatherCondition.THUNDERSTORM;
            if (conditionProbability < 0.5) return WeatherCondition.CLOUDY;
            return WeatherCondition.RAIN;
        }

        // Saisonale Wahrscheinlichkeiten
        switch (season) {
            case WINTER:
                if (conditionProbability < 0.3) return WeatherCondition.SNOW;
                if (conditionProbability < 0.6) return WeatherCondition.CLOUDY;
                return WeatherCondition.SUNNY;
            case SPRING:
                if (conditionProbability < 0.2) return WeatherCondition.RAIN;
                if (conditionProbability < 0.4) return WeatherCondition.CLOUDY;
                return WeatherCondition.SUNNY;
            case SUMMER:
                if (conditionProbability < 0.1) return WeatherCondition.THUNDERSTORM;
                if (conditionProbability < 0.3) return WeatherCondition.CLOUDY;
                return WeatherCondition.SUNNY;
            case AUTUMN:
                if (conditionProbability < 0.3) return WeatherCondition.RAIN;
                if (conditionProbability < 0.6) return WeatherCondition.CLOUDY;
                return WeatherCondition.SUNNY;
            default:
                return WeatherCondition.SUNNY;
        }
    }

    private int calculateRainProbability(WeatherCondition condition, Season season, double temperature) {
        // Keine Niederschläge unter 3 Grad Celsius
        if (temperature < 3.0) {
            // Bei kalten Temperaturen nur Schnee oder keine Niederschläge
            return condition == WeatherCondition.SNOW ? random.nextInt(41) + 30 : 0;
        }

        switch (season) {
            case WINTER:
                // Im Winter Schnee bei kalten Temperaturen
                if (condition == WeatherCondition.SNOW) return random.nextInt(41) + 30; // 30-70%
                return random.nextInt(21); // 0-20%
            case SPRING:
                // Frühling: höhere Regenwahrscheinlichkeit
                return random.nextInt(51) + 30; // 30-80%
            case SUMMER:
                // Sommer: unregelmäßige, aber intensive Niederschläge
                if (condition == WeatherCondition.THUNDERSTORM) return random.nextInt(51) + 50; // 50-100%
                return random.nextInt(31); // 0-30%
            case AUTUMN:
                // Herbst: häufige Niederschläge
                return random.nextInt(61) + 40; // 40-100%
            default:
                return random.nextInt(41);
        }
    }
}