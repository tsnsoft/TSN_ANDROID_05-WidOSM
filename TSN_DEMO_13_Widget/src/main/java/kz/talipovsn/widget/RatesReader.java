package kz.talipovsn.widget;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

// СОЗДАТЕЛЬ КОТИРОВОК ВАЛЮТ
public class RatesReader {

    private static final String BASE_URL = "https://kazfin.info/kazakhstan"; // Адрес с котировками

    // Парсинг котировок из формата html web-страницы банка, при ошибке доступа возвращаем null
    public static String getData() {
        StringBuilder data = new StringBuilder();
        try {
            setTrustAllCertificates(); // Установка доверенных сертификатов
            Document doc = Jsoup.connect(BASE_URL).timeout(5000).get(); // Создание документа JSOUP из html
            data.append("Курсы валюты:\n"); // Считываем заголовок страницы
            data.append(String.format("%12s %12s %12s %12s\n", "валюта", "наименование", "курс", "изменение"));
            Elements e = doc.select("div.exchange"); // Ищем в документе "<div class="exchange"> с данными о валютах
            Elements tables = e.select("table"); // Ищем таблицы с котировками
            Element table = tables.get(0); // Берем 1 таблицу для физических лиц
            // Цикл по строкам таблицы
            int i = 0;
            for (Element row : table.select("tr")) { // Пропускаем 1 строку с заголовком (eq - конкретная , lt - ниже, gt - выше)
                // Цикл по столбцам таблицы
                for (Element col : row.select("td")) { //
                    data.append(String.format("%12s ", col.text())); // Считываем данные с ячейки таблицы
                }
                if (i++ > 2) { // Пропускаем первые 2 строки с заголовками
                    break;
                } else {
                    data.append("\n"); // Добавляем переход на следующую строку;
                }
            }

        } catch (Exception e) {
            return "Нет данных"; // При ошибке доступа возвращаем "Нет данных"
        }
        return data.toString(); // Возвращаем результат
    }

    // Установка доверенных сертификатов для всех сайтов
    private static void setTrustAllCertificates() {
        try {
            // Создаем доверенный сертификатный менеджер, который не проверяет сертификаты
            TrustManager[] trustAllCertificates = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCertificates, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
