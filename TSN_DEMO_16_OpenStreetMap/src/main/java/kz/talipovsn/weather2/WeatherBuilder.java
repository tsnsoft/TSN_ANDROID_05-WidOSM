package kz.talipovsn.weather2;

import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONObject;

import static kz.talipovsn.weather2.HttpClient.getHTMLData;
import static kz.talipovsn.weather2.HttpClient.getHTMLImage;

// СОЗДАТЕЛЬ ПОГОДЫ
public class WeatherBuilder {

    // Получение JSON html-данных погоды по городу и языку
    private static String getWeatherData(String location, String lang) {
        String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?units=metric";
        String API = "52c336c8ceb8a6d8e4ee482985660291";
        String url = BASE_URL + "&q=" + location + "&lang=" + lang + "&appid=" + API;
        return getHTMLData(url); // Получение html-данных с web
    }

    // Получение с web bitmap картинки погоды по ее коду
    private static Bitmap getWheaterImage(String code) {
        String IMG_URL = "http://openweathermap.org/img/w/"; // Адрес картинок погоды
        String url = IMG_URL + code + ".png"; // Формирование адреса картинки
        return getHTMLImage(url); // Получение картинки с web
    }

    // Парсинг даты в формате JSON с созданием объекта погоды
    private static Weather dataParsing(String json) {
        Weather weather = new Weather(); // Создание объекта погоды
        try {
            JSONObject _obj = new JSONObject(json); // Создание объекта JSON из строки
            JSONObject _sys = _obj.getJSONObject("sys"); // Получение объекта sys
            JSONObject _wind = _obj.getJSONObject("wind"); // Получение объекта wind
            JSONArray _weather = _obj.getJSONArray("weather"); // Получение массива weather
            JSONObject _weather0 = _weather.getJSONObject(0); // Получение объекта weather[0]
            JSONObject _main = _obj.getJSONObject("main"); // Получение объекта main
            weather.setCountry(_sys.getString("country")); // Установка страны
            weather.setCity(_obj.getString("name")); // Установка города
            weather.setDt(_obj.getLong("dt")); // Установка даты и времени
            weather.setDescription(_weather0.getString("description")); // Установка описания
            weather.setCondition(_weather0.getString("main")); // Установка состояния
            weather.setIconName(_weather0.getString("icon")); // Установка кода иконки
            weather.setTemperature(_main.getDouble("temp")); // Установка температуры
            weather.setWind_speed(_wind.getDouble("speed")); // Установка скорости ветра
            weather.setWind_deg(_wind.getDouble("deg")); // Установка направления ветра
        } catch (Exception ignore) {
        }
        return weather; // Возвращение объекта погоды
    }

    // Получение готового объекта погоды по городу и языку
    public static Weather buildWeather (String location, String lang) {
        Weather weather = dataParsing(getWeatherData(location, lang)); // Получение объекта погоды
        weather.setIconData(getWheaterImage(weather.getIconName())); // Получение картинки погоды
        return weather; // Возвращение объекта погоды
    }
}