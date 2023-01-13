package scrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import scrapper.records.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HotelBookingScrapper implements HotelScrapper {
    public final String URL = "https://www.booking.com/hotel/%s/%s.es.html";

    public HotelBookingScrapper() {
    }

    @Override
    public Location locationOf(Map<String, String> params) {
        Document document = getHtmlDocument(String.format(URL, params.get(":country"), params.get(":name")));
        if (document == null) return null;
        Elements header = document.select("div.bui-grid.bui-grid--size-small");
        String address = header.select("span.hp_address_subtitle.js-hp_address_subtitle.jq_tooltip").text();
        String coordinates = header.select("a").attr("data-atlas-latlng");
        return new Location(address, coordinates);
    }

    @Override
    public Service servicesOf(Map<String, String> params) {
        Document document = getHtmlDocument(String.format(URL, params.get(":country"), params.get(":name")));
        return new Service(readServices(Objects.requireNonNull(document)));
    }

    private Map<String, List<String>> readServices(Document document) {
        Map<String, List<String>> serviceMap = new HashMap<>();
        Elements services = document.select("div.hotel-facilities-group");
        for (Element service : services) {
            String title = service.select("div.bui-title__text.hotel-facilities-group__title-text").text();
            Elements accessories = service.select("li.bui-list__item.bui-spacer--medium.hotel-facilities-group__list-item");
            serviceMap.put(title, getAccessories(accessories));
        }
        return serviceMap;
    }

    private List<String> getAccessories(Elements accessories) {
        List<String> accessoriesList = new ArrayList<>();
        for (Element accessory : accessories) accessoriesList.add(accessory.select("div.bui-list__description").text());
        return accessoriesList;
    }

    @Override
    public List<Comment> commentsOf(Map<String, String> params, String page) {
        String newUrl = urlConverter(String.format(URL, params.get(":country"), params.get(":name")), page);
        return readComments(newUrl);
    }

    private List<Comment> readComments(String newUrl) {
        Document document = getHtmlDocument(newUrl);
        List<Comment> commentList = new ArrayList<>();
        Elements comments = document.select("li.review_item");
        for (Element comment : comments) {
            String mark = comment.getElementsByClass("review-score-badge").text();
            String date = comment.getElementsByClass("review_item_date").text();
            String title = comment.getElementsByClass("review_item_header_content").text();
            commentList.add(new Comment(title, mark, date, getReviews(comment, "review_pos"), getReviews(comment, "review_neg")));
        }
        return commentList;
    }

    private List<String> getReviews(Element comment, String type) {
        List<String> reviewList = new ArrayList<>();
        Elements reviews = comment.select("div.review_item_review_content");
        for (Element review : reviews) reviewList.add(review.getElementsByClass(type).select("span").text());
        return reviewList;
    }

    private String urlConverter(String url, String page) {
        Pattern pattern = Pattern.compile("(?<=hotel/([a-z]{2})/)(.*)");
        Matcher matcher = pattern.matcher(url);
        if (!matcher.find()) return "";
        return "https://www.booking.com/reviews/" + matcher.group(1) + "/hotel/" + matcher.group(2) + "?hp_nav=0&old_page=0&order=featuredreviews&page="
                + page + "&r_lang=es&rows=25&srpvid=bbed5277667202b3&";
    }

    @Override
    public Rating ratingsOf(Map<String, String> params) {
        Document document = getHtmlDocument(String.format(URL, params.get(":country"), params.get(":name")));
        if (document == null) return null;
        Elements ratings = document.getElementsByAttributeValue("data-capla-component", "b-property-web-property-page/PropertyReviewSubscores");
        return new Rating(ratings.select("div.a1b3f50dcd.b2fe1a41c3.a1f3ecff04.e187349485.d19ba76520").eachText());
    }

    private Document getHtmlDocument(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(100000).get();
        } catch (IOException ex) {
            System.out.println("Excepción al obtener el HTML de la página" + ex.getMessage());
        }
        return doc;
    }
}
