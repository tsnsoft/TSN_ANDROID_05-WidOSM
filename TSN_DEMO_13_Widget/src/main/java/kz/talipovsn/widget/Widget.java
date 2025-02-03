package kz.talipovsn.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Widget extends AppWidgetProvider {

    StringBuffer rates = new StringBuffer(); // Строка для хранения данных о котировках

    @Override
    // Событие вызывается когда нужно обновить наши виджеты
    // (например, при первом запуске виджета или при обновлении данных)
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        new Rates(context).execute(); // Запускаем наш поток получения данных
    }

    // Класс-поток получения данных для виджета
    class Rates extends AsyncTask<Void, Void, Void> {
        Context context;

        Rates(Context context) { // Конструктор
            this.context = context;
        }

        @Override
        // Метод фонового получения данных потока
        protected Void doInBackground(Void... params) {
            rates.setLength(0); // Очищаем строку
            String data = RatesReader.getData(); // Получаем данные о котировках с сайта
            rates.append(data); // Добавляем данные о котировках в строку
            return null;
        }

        @Override
        // Метод при окончании фоновой задачи потока для устанновки данных в виджеты
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setWidgetViews(context); // Устанавливаем данные в виджетах
        }

        // Метод установки данных в наших виджетах
        private void setWidgetViews(Context context) {
            // Получаем менеджер виджетов
            AppWidgetManager awm = AppWidgetManager.getInstance(context);

            // Создаем макет наших виджетов из layout_widget.xml
            RemoteViews widgets = new RemoteViews(context.getPackageName(), R.layout.layout_widget);

            // Подготовка строки с датой для отображения в виджете
            String s = String.format("Данные от %s",
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())); // Строка с датой

            // Устанавливаем данные в TextView (R.id.date) в нашем макете виджета
            widgets.setTextViewText(R.id.date, s);

            // Устанавливаем данные в TextView (R.id.rates) в нашем макете виджета
            widgets.setTextViewText(R.id.rates, rates.toString());

            // В коде ниже написано, что если нажать окно виджета, то будет сделан переход
            // на основное окно MainActivity.class программы
            widgets.setOnClickPendingIntent(R.id.widget_window, PendingIntent.getActivity(context, 0,
                    new Intent(context, MainActivity.class), PendingIntent.FLAG_MUTABLE));

            // Обновляем все наши виджеты используя имя класса и макет виджета
            awm.updateAppWidget(getWidgetFullName(context), widgets);
        }
    }

    // Посылка команды в систему для обновления всех наших виджетов
    public static void updateWidgets(Context context) {
        // Выводим сообщение об обновлении виджетов на экран
        Toast.makeText(context, "Обновление виджетов", Toast.LENGTH_SHORT).show();

        // Получаем менеджер виджетов
        AppWidgetManager awm = AppWidgetManager.getInstance(context);

        // Создаем намерение для наших виджетов
        Intent intent = new Intent(context, Widget.class);

        // Устанавливаем намерение обновить все виджеты
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        // Устанавливаем намерению какие именно нужно обновить виджеты
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                awm.getAppWidgetIds(getWidgetFullName(context)));

        context.sendBroadcast(intent); // Отправляем широковещательное сообщение в систему
    }

    // Получение имени компонента виджета, типа "kz.talipovsn.widget.Widget"
    private static ComponentName getWidgetFullName(Context context) {
        return new ComponentName(context.getPackageName(), Widget.class.getName());
    }
}
