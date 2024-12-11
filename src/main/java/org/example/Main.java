package org.example;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("Введите поисковый запрос:");
            String query = scanner.nextLine().trim();

            if (query.isEmpty()) {
                System.out.println("Пустой запрос.");
                return;
            }
            String encodedQuery = URLEncoder.encode(query, "UTF-8");

            String apiUrl = "https://ru.wikipedia.org/w/api.php?action=query&list=search&utf8=&format=json&srsearch=" + encodedQuery;

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Парсинг JSON-ответа
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray searchResults = jsonResponse.getAsJsonObject("query").getAsJsonArray("search");

            if (searchResults.size() == 0) {
                System.out.println("Ничего не найдено по запросу: " + query);
                return;
            }

            System.out.println("Результаты поиска:");
            for (int i = 0; i < searchResults.size(); i++) {
                JsonObject result = searchResults.get(i).getAsJsonObject();
                String title = result.get("title").getAsString();
                System.out.println((i + 1) + ". " + title);
            }

            System.out.println("Введите номер статьи, чтобы открыть ее в браузере:");
            int choice = scanner.nextInt();

            while (choice < 1 || choice > searchResults.size()) {
                System.out.println("Неверный выбор. Допустимые значения от "+1+" до "+searchResults.size());
                choice = scanner.nextInt();
            }

            JsonObject selectedResult = searchResults.get(choice - 1).getAsJsonObject();
            int pageId = selectedResult.get("pageid").getAsInt();

            String articleUrl = "https://ru.wikipedia.org/w/index.php?curid=" + pageId;

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URL(articleUrl).toURI());
                System.out.println("Открыто в браузере: " + articleUrl);
            } else {
                System.out.println("Открытие браузера не поддерживается на этом устройстве.");
            }

        } catch (Exception e) {
            System.out.println("Произошла ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
