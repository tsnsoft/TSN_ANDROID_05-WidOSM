package kz.talipovsn.widget;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static kz.talipovsn.widget.Widget.updateWidgets;

public class MainActivity extends AppCompatActivity {

    TextView textView; // Переменная для доступа к компоненту текстового поля
    Button buttonSum; // Переменная для доступа к компоненту кнопки

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Доступ к компонентам окна
        textView = findViewById(R.id.textView);
        buttonSum = findViewById(R.id.buttonUpdate);

        updateWidgets(this) ; // Обновляем виджеты
    }

    // МЕТОД КНОПКИ
    public void onClick(View v) {
        updateWidgets(this) ; // Обновляем виджеты
    }

}
