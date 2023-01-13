package scrapper.records;

import java.util.List;

public record Comment(String title, String mark, String date,
                      List<String> positiveReview, List<String> negativeReview) {
}
