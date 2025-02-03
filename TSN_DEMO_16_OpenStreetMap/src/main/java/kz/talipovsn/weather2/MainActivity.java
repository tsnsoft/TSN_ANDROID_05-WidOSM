package kz.talipovsn.weather2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.GroundOverlay2;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import static kz.talipovsn.weather2.WeatherWidget.updateAllWidgets;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView imageView; // Иконка погоды
    private TextView textView; // Компонент для данных погоды
    private TextView textView2; // Компонент для данных котировок

    private SensorManager mSensorManager; // Менеджер сенсоров

    private MapView mapView; // Компонент карты
    private Bitmap bitmapArrows; // Компонент для хранения картинки
    private GroundOverlay2 overlay; // Компонент наложения на карту

    private String compassDegree = ""; // Направление компаса
    private int degree = 0; // Угол вращения компаса

    private final int PERSMISSION_ALL = 1; // Код запроса разрешений

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main); // Устанавливаем макет
        } catch (Exception ignore) {
        }

        // Установка темы
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        // Требуемые разрешения для Android >= 6
        String[] PERMISSIONS = { // Массив разрешений
                Manifest.permission.WRITE_EXTERNAL_STORAGE, // Разрешение на запись во внешнее хранилище
                Manifest.permission.ACCESS_FINE_LOCATION, // Разрешение на доступ к точному местоположению
                Manifest.permission.READ_EXTERNAL_STORAGE, // Разрешение на чтение из внешнего хранилища
                Manifest.permission.INTERNET, // Разрешение на доступ к интернету
                Manifest.permission.ACCESS_NETWORK_STATE // Разрешение на доступ к состоянию сети
        };

        // Запрос разрешений, если их еще нет для Android >= 6
        if (!hasPermissions(this, PERMISSIONS)) { // Если нет разрешений
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERSMISSION_ALL); // Запрашиваем разрешения
        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); // Получаем менеджер сенсоров

        imageView = findViewById(R.id.imageView); // Инициализация компонента для картинки погоды
        textView = findViewById(R.id.textView); // Инициализация компонента для данных погоды
        textView2 = findViewById(R.id.textView2); // Инициализация компонента для данных котировок

        JSONWeatherTask task = new JSONWeatherTask(); // Создание потока определения погоды
        task.execute(getString(R.string.Город), getString(R.string.Язык)); // Активация потока

        JSOUPRatesTask task2 = new JSOUPRatesTask(); // Создание потока определения котировок
        task2.execute(); // Активация потока

        // Подключение карт OSM
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        // Инициализация компонента карты
        mapView = this.findViewById(R.id.mapview); // Инициализация компонента карты
        mapView.setBuiltInZoomControls(true); // Разрешаем масштабирование карты
        mapView.setMultiTouchControls(true); // Разрешаем мультитач карты
        mapView.setTileSource(TileSourceFactory.MAPNIK); // Устанавливаем источник тайлов (картинок) карты

        // Инициализация контроллера карты
        IMapController mapController = mapView.getController(); // Получаем контроллер карты
        mapController.setZoom(11.0); // Устанавливаем начальное масштабирование карты
        mapController.setCenter(new GeoPoint(52.2772, 76.9865)); // Устанавливаем начальную позицию карты на центр города
        mapController.stopPanning(); // Останавливаем панорамирование карты (перемещение карты)

        // Установка вращения карты
        final DisplayMetrics dm = this.getResources().getDisplayMetrics(); // Получаем метрики экрана
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(mapView); // Создаем компонент вращения карты
        mRotationGestureOverlay.setEnabled(true); // Разрешаем вращение компонента карты
        mapView.getOverlays().add(mRotationGestureOverlay); // Добавляем компонент вращения карты

        // Установка масштабной линейки на карту
        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(mapView); // Создаем компонент линейки масштаба
        mScaleBarOverlay.setScaleBarOffset(0, (int) (40 * dm.density)); // Устанавливает смещение экрана шкалы для линейки
        mScaleBarOverlay.setCentred(true); // Устанавливаем центрирование масштаба
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10); // Устанавливаем отступ масштаба
        mapView.getOverlays().add(mScaleBarOverlay); // Добавляем компонент масштабирования карты

        // Конвертируем Drawable в Bitmap
        // bitmapArrows - изображение стрелок ветра
        bitmapArrows = BitmapFactory.decodeResource(getResources(), R.drawable.arrows); // Получаем картинку стрелок ветра

        overlay = new GroundOverlay2(); // Создаем компонент наложения на карту

        // Разрешаем определять текущую позицию
        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        mLocationOverlay.enableMyLocation(); // Разрешаем определение текущей позиции
        mapView.getOverlays().add(mLocationOverlay); // Добавляем компонент определения текущей позиции на карту

        // Разрешаем компас на карте
        CompassOverlay mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mapView);
        mCompassOverlay.setPointerMode(true); // Устанавливаем режим указателя
        mCompassOverlay.enableCompass(); // Разрешаем компас
        mapView.getOverlays().add(mCompassOverlay); // Добавляем компас на карту

        // Устанавливаем маркер 1 на карту
        Marker startMarker = new Marker(mapView); // Создаем маркер
        startMarker.setPosition(new GeoPoint(52.2561941, 77.0422887)); // Устанавливаем позицию маркера
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); // Устанавливаем якорь маркера
        startMarker.setTitle("Алюминиевый завод"); // Устанавливаем заголовок маркера
        mapView.getOverlays().add(startMarker); // Добавляем маркер на карту

        // Устанавливаем маркер 2 на карту
        Marker startMarker2 = new Marker(mapView); // Создаем маркер
        startMarker2.setPosition(new GeoPoint(52.3699894, 76.9072292)); // Устанавливаем позицию маркера
        startMarker2.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); // Устанавливаем якорь маркера
        startMarker2.setTitle("ПНХЗ"); // Устанавливаем заголовок маркера
        mapView.getOverlays().add(startMarker2); // Добавляем маркер на карту

        // Обновление виджетов
        updateAllWidgets(this,  new Intent(this, WeatherWidget.class)) ; // Обновление виджетов
    }

    // Действия проверки нужных разрешений для Android >= 6
    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) { // Если контекст и разрешения не пустые
            for (String permission : permissions) { // Перебираем разрешения
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false; // Если разрешения не получены, возвращаем false
                }
            }
        }
        return true; // Если разрешения получены, возвращаем true
    }

    // Действия при окончании установки разрешений для Android >= 6
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERSMISSION_ALL) { // Если запрос разрешений
            for (int  result : grantResults)  // Перебираем результаты запроса
            if (result != PackageManager.PERMISSION_GRANTED) { // Если разрешения не получены
                return; // Выходим из метода
            }
            reload(); // Перезагружаем окно
        }
    }

    // Перезагрузка окна
    private void reload() {
        Intent intent = getIntent(); // Получаем намерение
        overridePendingTransition(0, 0); // Отключаем анимацию
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); // Добавляем флаг отключения анимации
        finish(); // Закрываем окно
        overridePendingTransition(0, 0); // Отключаем анимацию
        startActivity(intent); // Запускаем окно программы заново
    }

    // Кнопка "Обновить"
    public void onClick(View view) {
        new JSONWeatherTask().execute(getString(R.string.Город), getString(R.string.Язык)); // Создание и активация потока определения погоды
        new JSOUPRatesTask().execute(); // Создание и активация потока определения котировок
        Toast.makeText(getApplicationContext(), R.string.обновление, Toast.LENGTH_SHORT).show();
        updateAllWidgets(this,  new Intent(this, WeatherWidget.class)) ;
    }

    // Создание меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Обработчик меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.newCheck) { // Пункт меню "Проверить обновления"
            Intent i = new Intent(android.content.Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://play.google.com/store/apps/details?id=kz.proffix4.wrates"));
            startActivity(i);
            return true;
        }

        if (id == R.id.about) { // Пункт меню "О программе"
            Toast toast = Toast.makeText(getApplicationContext(), R.string.email, Toast.LENGTH_LONG);
            toast.show();
            return true;
        }

        if (id == R.id.exit) { // Пункт меню "Выход"
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item); // Вызываем суперкласс для обработки остальных пунктов меню
    }

    // Обработчик сенсора компаса, вызывается при изменении позиции компаса
    @Override
    public void onSensorChanged(SensorEvent event) {
        degree = Math.round(event.values[0]); // Получаем угол вращения компаса
        String degreeStr = Weather.getCompassDirectionSymbol(degree); // Получаем значок направления компаса
        if (!degreeStr.equals(compassDegree)) { // Если направление компаса изменилось
            compassDegree = degreeStr; // Запоминаем новое направление компаса
            new JSONWeatherTask().execute(getString(R.string.Город), getString(R.string.Язык)); // Создание и активация потока определения погоды
        }
    }

    // Обработчик сенсора компаса, вызывается при изменении точности компаса
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // Событие при возобновлении программы
    @Override
    protected void onResume() {
        super.onResume();
        // Регистрируем слушатель сенсора компаса
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    // Событие при остановке программы
    @Override
    protected void onPause() {
        super.onPause();
        // Отменяем регистрацию слушателя сенсора компаса
        mSensorManager.unregisterListener(this);
    }

    // Класс отдельного асинхронного потока для определения погоды
    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        // Тут реализуем фоновую асинхронную загрузку данных, требующих много времени
        // Входные параметры - город и язык для определения погоды
        @Override
        protected Weather doInBackground(String... params) {
            return WeatherBuilder.buildWeather(params[0], params[1]);
        }
        // ----------------------------------------------------------------------------

        // Тут реализуем что нужно сделать после окончания загрузки данных погоды
        @Override
        protected void onPostExecute(final Weather weather) {
            super.onPostExecute(weather);

            Matrix matrix = new Matrix(); // Создаем матрицу для вращения картинки
            matrix.postRotate((float) weather.getWind_deg()); // Устанавливаем угол вращения картинки

            // Вращаем картинку стрелок ветра
            Bitmap rotated = Bitmap.createBitmap(bitmapArrows, 0, 0, bitmapArrows.getWidth(),
                    bitmapArrows.getHeight(), matrix, true);

            overlay.setTransparency(0.2f); // Устанавливаем прозрачность наложения картинки
            overlay.setImage(rotated); // Устанавливаем картинку наложения на карту
            // Устанавливаем позицию наложения на карту - углы: 52.395728, 76.824624, 52.202483, 77.200086
            overlay.setPosition(new GeoPoint(52.395728, 76.824624),
                    new GeoPoint(52.202483, 77.200086));
            mapView.getOverlayManager().add(overlay); // Добавляем наложение на карту


            // Устанавливаем картинку погоды
            imageView.post(new Runnable() { //  Используем синхронизацию с UI
                @Override
                public void run() {
                    // Если есть считанная иконка с web
                    if (weather.getIconData() != null) { // Если есть иконка
                        imageView.setImageBitmap(weather.getIconData()); // Установка иконки
                    } else {
                        imageView.setImageResource(R.mipmap.ic_launcher); // Установка своей иконки при ошибке
                    }
                    imageView.invalidate(); // Принудительная прорисовка картинки на всякий случай
                }
            });

            // Выдаем данные о погоде в компонент
            textView.post(new Runnable() { //  с использованием синхронизации UI
                @Override
                public void run() {
                    textView.setText(""); // Очищаем компонент
                    if (weather.getCity() != null) { // Если есть данные о погоде
                        textView.append(weather.getDt() + "\n"); // Добавляем дату и время
                        textView.append(weather.getTemperature() + " \u2103, " +
                                weather.getDescription() + "\n"); // Добавляем температуру и описание
                        textView.append(getString(R.string.ветер) + " " + weather.getWind_speed() + " " + getString(R.string.мс) + "\n"); // Добавляем скорость ветра
                        textView.append(getString(R.string.напр) + " " + weather.getWind_deg() + " \u00b0" + " " +
                                Weather.getWndDirectionSymbol(weather.getWind_deg())); // Добавляем направление ветра
                        if (!compassDegree.equals("")) {
                            textView.append(", " + getString(R.string.вы) + ": " +
                                    degree + " \u00b0" + " " + compassDegree); // Добавляем направление компаса
                        }
                    } else { // Если нет данных о погоде
                        textView.append(getString(R.string.Нет_данных_о_погоде) + "\n"); // Добавляем сообщение об ошибке
                        textView.append(getString(R.string.Проверьте_доступность_интернета));
                    }
                }
            });

        }
        // ------------------------------------------------------------------------------------

    }

    // Класс отдельного асинхронного потока для определения котировок
    private class JSOUPRatesTask extends AsyncTask<String, Void, String> {

        // Тут реализуем фоновую асинхронную загрузку данных, требующих много времени
        @Override
        protected String doInBackground(String... params) {
            return RatesReader.getRatesData(); // Получаем данные котировок
        }
        // ----------------------------------------------------------------------------

        // Тут реализуем что нужно сделать после окончания загрузки данных
        @Override
        protected void onPostExecute(final String rates) {
            super.onPostExecute(rates);

            // Выдаем данные о котировках в компонент
            textView.post(new Runnable() { //  с использованием синхронизации UI
                @Override
                public void run() {
                    if (rates != null) { // Если есть данные о котировках
                        textView2.setText(rates); // Выводим данные о котировках
                    } else { // Если нет данных о котировках
                        textView2.setText(""); // Очищаем компонент
                        textView2.append(getString(R.string.Нет_данных_о_котировках) + "\n"); // Добавляем сообщение об ошибке
                        textView2.append(getString(R.string.Проверьте_доступность_интернета));
                    }
                }
            });

        }
        // ------------------------------------------------------------------------------------

    }

}
