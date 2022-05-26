package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        Connection connection = Jsoup.connect(PAGE_LINK);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            assert titleElement != null;
            Element linkElement = titleElement.child(0);
            Element dataElement = row.selectFirst(".vacancy-card__date");
            assert dataElement != null;
            Element dataLink = dataElement.child(0);
            String vacancyName = titleElement.text();
            String vacancyDate = dataElement.text();
            String link = String.format("%s%s %s", SOURCE_LINK, linkElement.attr("href"), dataLink.attr("datetime"));
            System.out.printf("%s: %s %s%n", vacancyDate, vacancyName, link);
        });
    }

}
