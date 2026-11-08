package kz.talipovsn.widget;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

// СОЗДАТЕЛЬ КОТИРОВОК ВАЛЮТ ДЛЯ ВИДЖЕТА
public class RatesReader {

    // Стабильный официальный XML-фид Национального Банка РК
    private static final String BASE_URL = "https://nationalbank.kz/rss/rates_all.xml";

    // Парсинг котировок с сервера НБ РК, при ошибке доступа возвращаем "Нет данных"
    public static String getData() {
        StringBuilder data = new StringBuilder();
        try {
            // Загружаем документ Jsoup с указанием XML-парсера
            Document doc = Jsoup.connect(BASE_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(5000)
                    .parser(Parser.xmlParser())
                    .get();

            data.append("Курсы валюты НБ РК:\n");
            data.append(String.format("%-6s %-10s %-10s %-8s\n", "Валюта", "Имя", "Курс", "Изм."));

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

                    // Для компактного виджета берем только 3 основные валюты (как в исходном цикле i <= 2)
                    if (code.equals("USD") || code.equals("EUR") || code.equals("RUB")) {
                        String rate = descEl.text().trim();
                        String change = (changeEl != null) ? changeEl.text().trim() : "0.0";
                        String index = (indexEl != null) ? indexEl.text().trim() : "";

                        String name = getShortName(code);
                        String arrow = "UP".equalsIgnoreCase(index) ? "↑" : ("DOWN".equalsIgnoreCase(index) ? "↓" : "");

                        data.append(String.format("%-6s %-10s %-10s %-8s\n",
                                code, name, rate, arrow + change));
                        count++;
                    }
                }
            }

            if (count == 0) {
                return "Нет данных";
            }

        } catch (Exception e) {
            return "Нет данных"; // При ошибке доступа возвращаем "Нет данных"
        }
        return data.toString().trim(); // Возвращаем результат
    }

    // Короткие названия валют, чтобы таблица помещалась в узкое окно виджета на рабочем столе
    private static String getShortName(String code) {
        switch (code) {
            case "USD": return "Доллар";
            case "EUR": return "Евро";
            case "RUB": return "Рубль";
            default:    return code;
        }
    }
}