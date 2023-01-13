package api;

import com.google.gson.Gson;
import scrapper.HotelBookingScrapper;
import scrapper.HotelScrapper;

import static spark.Spark.*;

public class SparkWebService implements WebService {
    private final HotelScrapper scrap;
    private final Gson gson;

    public SparkWebService() {
        this.scrap = new HotelBookingScrapper();
        this.gson = new Gson();
    }

    @Override
    public void run() {
        path("/:country/hotels/:name", () -> {
            get("", "application/json", ((request, response) -> {
                response.type("application/json");
                if (scrap.locationOf(request.params()) != null) return gson.toJson(scrap.locationOf(request.params()));
                else return halt(404, "Página desconocida");
            }));

            get("/services", "application/json", ((request, response) -> {
                response.type("application/json");
                if (scrap.servicesOf(request.params()) != null) return gson.toJson(scrap.servicesOf(request.params()));
                else return halt(404, "Página desconocida");
            }));

            get("/comments", "application/json", ((request, response) -> {
                response.type("application/json");
                if (scrap.commentsOf(request.params(), request.queryParams("page")) != null)
                    return gson.toJson(scrap.commentsOf(request.params(), request.queryParams("page")));
                else return halt(404, "Página desconocida");
            }));

            get("/ratings", "application/json", ((request, response) -> {
                response.type("application/json");
                if (scrap.ratingsOf(request.params()) != null) return gson.toJson(scrap.ratingsOf(request.params()));
                else return halt(404, "Página desconocida");
            }));

            notFound("""
                    Página desconocida, prueba con uno de los siguientes formatos:\s
                    /:country/hotels/:name\s
                    /:country/hotels/:name/services\s
                    /:country/hotels/:name/ratings\s
                    /:country/hotels/:name/comments
                    """);
        });
    }
}