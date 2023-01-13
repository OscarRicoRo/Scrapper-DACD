package scrapper;

import scrapper.records.Comment;
import scrapper.records.Location;
import scrapper.records.Rating;
import scrapper.records.Service;

import java.util.List;
import java.util.Map;

public interface HotelScrapper {
    Location locationOf(Map<String, String> params);

    Service servicesOf(Map<String, String> params);

    List<Comment> commentsOf(Map<String, String> params, String queryParams);

    Rating ratingsOf(Map<String, String> params);

}
