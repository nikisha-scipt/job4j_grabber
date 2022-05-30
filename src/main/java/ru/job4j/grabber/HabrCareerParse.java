package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.Parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }


    private String retrieveDescription(String link) throws IOException {
        StringBuilder res = new StringBuilder();
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Element descriptionElement = document.selectFirst(".job_show_description__vacancy_description");
        assert descriptionElement != null;
        descriptionElement.child(0).getAllElements().forEach(e -> res.append(e.text()).append(System.lineSeparator()));
        return res.toString();
    }

    @Override
    public List<Post> list(String link) {
        List<Post> result = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            try {
                Connection connection = Jsoup.connect(link + i);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    try {
                        result.add(getPost(row));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private Post getPost(Element element) throws IOException {
        Element title = element.selectFirst(".vacancy-card__title");
        assert  title != null;
        Element titleLink = title.child(0);
        String linkVacancy = SOURCE_LINK + titleLink.attr("href");
        Element dataElement = element.selectFirst(".vacancy-card__date");
        assert dataElement != null;
        Element dataLink = dataElement.child(0);
        String descriptionVacancy = retrieveDescription(linkVacancy);
        return new Post(title.text(), linkVacancy, descriptionVacancy, dateTimeParser.parse(dataLink.attr("datetime")));
    }

}
