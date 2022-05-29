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
        Element descriptionElement = document.selectFirst(".style-ugc");
        assert descriptionElement != null;
        res.append(descriptionElement.text());
        return res.toString();
    }

    @Override
    public List<Post> list(String link) {
        List<Post> result = new ArrayList<>();
        try {
            Connection connection = Jsoup.connect(link);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element title = row.selectFirst(".vacancy-card__title");
                Element date = row.selectFirst(".vacancy-card__date");
                assert title != null;
                String linkVacancy = getElement(title);
                String descriptionVacancy = null;
                try {
                    descriptionVacancy = retrieveDescription(linkVacancy);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assert date != null;
                Post post = new Post(title.text(),
                        linkVacancy,
                        descriptionVacancy,
                        dateTimeParser.parse(date.child(0).attr("datetime")));
                result.add(post);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getElement(Element element) {
        StringBuilder res  = new StringBuilder();
        Element elementLink = element.child(0);
        res.append(SOURCE_LINK).append(elementLink.attr("href"));
        return res.toString();
    }

    public static void main(String[] args) {
        HabrCareerParse habr = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> arr = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            arr = habr.list(PAGE_LINK + i);
        }
        System.out.println(arr);
    }
}
