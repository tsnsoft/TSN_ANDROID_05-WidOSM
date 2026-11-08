package kz.talipovsn.weather2;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

// КОТИРОВКИ ВАЛЮТЫ
public class RatesReader {

    // Стабильный официальный адрес с котировками Национального Банка РК (по HTTPS)
    private static final String BASE_URL = "https://nationalbank.kz/rss/rates_all.xml";

    // Парсинг котировок с сервера НБ РК, при ошибке доступа возвращаем null
    public static String getRatesData() {
        StringBuilder data = new StringBuilder();
        try {
            // Создание документа JSOUP из XML-потока НБ РК
            Document doc = Jsoup.connect(BASE_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(5000)
                    .parser(Parser.xmlParser())
                    .get();

            data.append("Курсы валюты НБ РК:\n");
            data.append(String.format("%12s %12s %12s %12s\n", "валюта", "наименование", "курс", "изменение"));
            data.append("\n");

            // Ищем все элементы <item> с данными о валютах
            Elements items = doc.select("item");
            int count = 0;

            for (Element item : items) {
                Element titleEl = item.selectFirst("title");
                Element descEl = item.selectFirst("description");
                Element changeEl = item.selectFirst("change");
                Element indexEl = item.selectFirst("index");

                if (titleEl != null && descEl != null) {
                    String code = titleEl.text().trim();

                    // Берем первые 3 основные валюты (аналогично ограничению i > 2 в исходном коде)
                    if (code.equals("USD") || code.equals("EUR") || code.equals("RUB")) {
                        String rate = descEl.text().trim();
                        String change = (changeEl != null) ? changeEl.text().trim() : "0.0";
                        String index = (indexEl != null) ? indexEl.text().trim() : "";

                        String name = getCurrencyName(code);
                        String arrow = "UP".equalsIgnoreCase(index) ? "↑ " : ("DOWN".equalsIgnoreCase(index) ? "↓ " : "");

                        data.append(String.format("%12s %12s %12s %12s\n",
                                code, name, rate, arrow + change));
                        count++;
                    }
                }
            }

            // Если ни одной валюты не найдено, возвращаем null для срабатывания обработки ошибки в UI
            if (count == 0) {
                return null;
            }

        } catch (Exception ignored) {
            return null; // При ошибке доступа возвращаем null
        }
        return data.toString().trim(); // Возвращаем результат
    }

    // Наименования валют для таблицы
    private static String getCurrencyName(String code) {
        switch (code) {
            case "USD": return "Доллар США";
            case "EUR": return "Евро";
            case "RUB": return "Рос. рубль";
            default:    return code;
        }
    }
}