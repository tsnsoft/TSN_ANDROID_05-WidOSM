package kz.talipovsn.weather2;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Objects;


public class WeatherWidget extends AppWidgetProvider {

    public static String MY_WIDGET_ACTIVATE = "ActionReceiverActivate!"; // Имя широковещательного сообщения

    @Override
    // Событие вызывается когда нужно обновить наши виджеты
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // Проверяем есть ли соединения с сетью интернет
        // Получаем менеджер сети
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null; // Информация о сети
        if (connectivityManager != null) { // Если менеджер сети не пустой
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo(); // Получаем информацию о сети
        }

        // Обновляем все виджеты разом, для которых настало время обновления, если есть Интернет
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) { // Если есть соединение с сетью
            new WidgetData(context).execute("Pavlodar, KZ", "ru"); // Запускаем асинхронную загрузку данных
        }

    }

    // Класс для асинхронной загрузки данных погоды
    private class WidgetData extends AsyncTask<String, Void, Weather> {
        Context context;

        WidgetData(Context context) {
            this.context = context;
        }

        // Тут реализуем фоновую асинхронную загрузку данных, требующих много времени
        @Override
        protected Weather doInBackground(String... params) {
            return WeatherBuilder.buildWeather(params[0], params[1]); // Получаем объект погоды
        }

        // Тут реализуем что нужно сделать после окончания загрузки данных
        @Override
        protected void onPostExecute(final Weather weather) {
            super.onPostExecute(weather);

            String temperature = String.valueOf(weather.getTemperature()); // Температура
            String time_temp_update = String.valueOf(weather.getDt()); // Время обновления

            // Подключаемся к менеджеру виджетов
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            // Создаем макет наших виджетов в памяти
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

            // Устанавливаем данные в макете виджета
            remoteViews.setTextViewText(R.id.temperature, temperature + " \u2103" +
                    " \u2638" + Weather.getWndDirectionSymbol(weather.getWind_deg())); // Устанавливаем температуру
            remoteViews.setTextViewText(R.id.time_temp_update, time_temp_update); // Устанавливаем время обновления
            remoteViews.setImageViewBitmap(R.id.weather_icon, weather.getIconData()); // Устанавливаем картинку погоды

            // Делаем обработчик нажатия на виджет для вызова окна программы
            // Создаем намерение для вызова окна программы MainActivity.class при нажатии на виджет
            Intent intentMainActivity = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentMainActivity,
                    PendingIntent.FLAG_IMMUTABLE);
            remoteViews.setOnClickPendingIntent(R.id.widget_window, pendingIntent);

            // Делаем обработчик нажатия на картинку виджета для обновления
            Intent intent = new Intent(context, WeatherWidget.class);
            intent.setAction(MY_WIDGET_ACTIVATE);
            PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            remoteViews.setOnClickPendingIntent(R.id.weather_icon, actionPendingIntent);

            // Обновляем все виджеты
            ComponentName widgetComponents = new ComponentName(context.getPackageName(), WeatherWidget.class.getName());
            appWidgetManager.updateAppWidget(widgetComponents, remoteViews); // Обновляем виджеты
        }
    }

    // Прослушивание широковещательных сообщений от виджетов
    @Override
    public void onReceive(Context context, Intent intent) { // При получении широковещательного сообщения
        if (Objects.equals(intent.getAction(), MY_WIDGET_ACTIVATE)) { // Если это наше сообщение
            Toast.makeText(context, context.getText(R.string.обновление_виджетов), Toast.LENGTH_SHORT).show(); // Выводим сообщение
            updateAllWidgets(context, intent); // Обновляем все виджеты
        } else {
            super.onReceive(context, intent); // Вызываем метод родителя
        }
    }

    // Посылка команд обновления всех наших виджетов
    public static void updateAllWidgets(Context context, Intent intentWidget) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context); // Получаем менеджер виджетов
        intentWidget.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE); // Устанавливаем действие обновления виджетов
        // Получаем все идентификаторы виджетов
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context.getPackageName(), WeatherWidget.class.getName()));
        intentWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        context.sendBroadcast(intentWidget); // Отправляем широковещательное сообщение обновления виджетов
    }

}

