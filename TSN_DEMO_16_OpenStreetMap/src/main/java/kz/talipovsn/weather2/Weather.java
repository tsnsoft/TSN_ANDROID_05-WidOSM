package kz.talipovsn.weather2;

import android.graphics.Bitmap;

import java.text.SimpleDateFormat;
import java.util.Date;

// Класс погоды
public class Weather {
    private String city; // Город
    private String country; // Страна
    private long dt; // Дата и время в формате Unix
    private double temperature; // Температура в градусах Цельсия
    private String condition; // Состояние погоды
    private String description;  // Описание погоды
    private String iconName; // Код иконки погоды
    private Bitmap iconData; // Иконка погоды
    private double wind_speed; //Скорость ветра м/с
    private double wind_deg; //Направление ветра градусы

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setDt(long dt) {
        this.dt = dt;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public void setIconData(Bitmap iconData) {
        this.iconData = iconData;
    }

    public void setWind_speed(double wind_speed) {
        this.wind_speed = wind_speed;
    }

    public void setWind_deg(double wind_deg) {
        this.wind_deg = wind_deg;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getDt() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(dt * 1000));

    }

    public double getTemperature() {
        return temperature;
    }

    public String getCondition() {
        return condition;
    }

    public String getDescription() {
        return description;
    }

    public String getIconName() {
        return iconName;
    }

    public Bitmap getIconData() {
        return iconData;
    }

    public double getWind_speed() {
        return wind_speed;
    }

    public double getWind_deg() {
        return wind_deg;
    }

    // Получение символа направления компаса
    public static String getCompassDirectionSymbol(double deg) {
        String code = "";
        if (deg >= 0 && deg < 45) code = "\u2191"; // ↑ вверх
        else if (deg >= 45 && deg < 90) code = "\u2197"; // ↗ вверх вправо
        else if (deg >= 90 && deg < 135) code = "\u2192"; // → вправо
        else if (deg >= 135 && deg < 180) code = "\u2198"; // ↘ вниз вправо
        else if (deg >= 180 && deg < 225) code = "\u2193"; // ↓ вниз
        else if (deg >= 225 && deg < 270) code = "\u2199"; // ↙ вниз влево
        else if (deg >= 270 && deg < 315) code = "\u2190"; // ← влево
        else if (deg >= 315 && deg <= 360) code = "\u2196"; // ↖ вверх влево
        return code;
    }

    // Получение символа направления ветра по градусам в формате стрелки для погоды
    public static String getWndDirectionSymbol(double deg) {
        String code = "";
        if (deg >= 0 && deg < 45) code = "\u2193"; // вниз ↓
        else if (deg >= 45 && deg < 90) code = "\u2199"; // вниз влево ↙
        else if (deg >= 90 && deg < 135) code = "\u2190"; // влево ←
        else if (deg >= 135 && deg < 180) code = "\u2196"; // вверх влево ↖
        else if (deg >= 180 && deg < 225) code = "\u2191"; // вверх ↑
        else if (deg >= 225 && deg < 270) code = "\u2197"; // вверх вправо ↗
        else if (deg >= 270 && deg < 315) code = "\u2192"; // вправо →
        else if (deg >= 315 && deg <= 360) code = "\u2198"; // вниз вправо ↘
        return code;
    }

}
