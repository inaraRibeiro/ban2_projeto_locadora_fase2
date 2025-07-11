import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import objectDAOs.DatabaseConnection;
import objectDAOs.GenreDAO;
import objectDAOs.MongoConnection;
import objects.Genre;
import org.bson.Document;
import utils.Utils;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.function.Consumer;

public class GenreRentalReport {

    private MongoDatabase database;
    private GenreDAO genreDAO;
    private MongoCollection<Document> rentalsCollection;

    public GenreRentalReport() {
        this.database = MongoConnection.getInstance().getDatabase();
        this.rentalsCollection = database.getCollection("rentals");
        this.genreDAO = new GenreDAO();
    }

    public void generateReport() {
        System.out.println("\n=== GENRES BY RENTAL POPULARITY ===\n");

        List<Map<String, Object>> genres = getGenresByRentalCount();

        if (genres.isEmpty()) {
            System.out.println("No rental data available.");
            return;
        }

        int rank = 1;
        for (Map<String, Object> genre : genres) {
            Long genreId = (Long) genre.get("genreId");
            String genreName = (String) genre.get("name");
            Integer rentalCount = (Integer) genre.get("rentalCount");

            System.out.println(rank + ". " + genreName + " (" + rentalCount + " rentals)");

            List<Map<String, Object>> topMovies = getTopMoviesForGenre(genreId, 3);

            if (!topMovies.isEmpty()) {
                System.out.println("   Top movies:");
                for (int i = 0; i < topMovies.size(); i++) {
                    Map<String, Object> movie = topMovies.get(i);
                    System.out.println("   " + (i + 1) + ". " + movie.get("title") +
                            " (" + movie.get("rentalCount") + " rentals)");
                }
            } else {
                System.out.println("   No movies rented in this genre.");
            }

            System.out.println();
            rank++;
        }
    }

    private List<Map<String, Object>> getGenresByRentalCount() {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            List<Document> pipeline = Arrays.asList(
                    // Step 1: Group by genre_id and count rentals
                    new Document("$group",
                            new Document("_id", "$movie.genre")
                                    .append("genreId", new Document("$first", "$genre_id")) // Get genre_id from rentals
                                    .append("name", new Document("$first", "$movie.genre"))
                                    .append("rentalCount", new Document("$sum", 1))
                    ),

                    // Step 2: Filter out genres with 0 rentals (equivalent to HAVING COUNT > 0)
                    new Document("$match",
                            new Document("rentalCount", new Document("$gt", 0))
                    ),

                    // Step 3: Sort by rental count descending
                    new Document("$sort",
                            new Document("rentalCount", -1)
                    ),

                    // Step 4: Project the final structure
                    new Document("$project",
                            new Document("genreId", 1)
                                    .append("name", "$_id")
                                    .append("rentalCount", 1)
                                    .append("_id", 0)
                    )
            );

            // Execute the aggregation
            rentalsCollection.aggregate(pipeline).forEach((Consumer<? super Document>) doc -> {
                Map<String, Object> row = new HashMap<>();

                // Handle genreId - might need to get from genres collection if not in rental
                Object genreIdObj = doc.get("genreId");
                if (genreIdObj != null) {
                    row.put("genreId", ((Number) genreIdObj).longValue());
                } else {
                    // Fallback: look up genre by name
                    String genreName = doc.getString("name");
                    Genre genre = genreDAO.getGenreByName(genreName);
                    if (genre != null) {
                        row.put("genreId", genre.getGenreId());
                    }
                }

                row.put("name", doc.getString("name"));
                row.put("rentalCount", doc.getInteger("rentalCount", 0));
                result.add(row);
            });

        } catch (Exception e) {
            System.err.println("Error getting genres by rental count: " + e.getMessage());
        }

        return result;
    }

    private List<Map<String, Object>> getTopMoviesForGenre(Long genreId, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // MongoDB aggregation pipeline to replicate the SQL query
            List<Document> pipeline = Arrays.asList(
                    // Step 1: Filter rentals by genre_id (equivalent to WHERE m.genre_id = ?)
                    new Document("$match",
                            new Document("movie_id", new Document("$exists", true))
                    ),

                    // Step 2: Lookup movie details for each rental
                    new Document("$lookup",
                            new Document("from", "movies")
                                    .append("localField", "movie_id")
                                    .append("foreignField", "_id")
                                    .append("as", "movieDetails")
                    ),

                    // Step 3: Unwind movie details
                    new Document("$unwind", "$movieDetails"),

                    // Step 4: Filter by genre_id
                    new Document("$match",
                            new Document("movieDetails.genre_id", genreId)
                    ),

                    // Step 5: Group by movie and count rentals
                    new Document("$group",
                            new Document("_id", "$movie_id")
                                    .append("title", new Document("$first", "$movieDetails.title"))
                                    .append("rentalCount", new Document("$sum", 1))
                    ),

                    // Step 6: Filter movies with rental count > 0 (equivalent to HAVING COUNT > 0)
                    new Document("$match",
                            new Document("rentalCount", new Document("$gt", 0))
                    ),

                    // Step 7: Sort by rental count descending
                    new Document("$sort",
                            new Document("rentalCount", -1)
                    ),

                    // Step 8: Limit results
                    new Document("$limit", limit),

                    // Step 9: Project final structure
                    new Document("$project",
                            new Document("movieId", "$_id")
                                    .append("title", 1)
                                    .append("rentalCount", 1)
                                    .append("_id", 0)
                    )
            );

            // Execute the aggregation on rentals collection
            rentalsCollection.aggregate(pipeline).forEach((Consumer<? super Document>) doc -> {
                Map<String, Object> row = new HashMap<>();
                row.put("movieId", Utils.getDocId(doc, "movieId"));
                row.put("title", doc.getString("title"));
                row.put("rentalCount", doc.getInteger("rentalCount", 0));
                result.add(row);
            });

        } catch (Exception e) {
            System.err.println("Error getting top movies for genre: " + e.getMessage());
        }

        return result;
    }

    public List<Map<String, Object>> getGenrePerformanceReport() {
        List<Map<String, Object>> report = new ArrayList<>();

        try {
            List<Document> pipeline = Arrays.asList(
                    new Document("$group",
                            new Document("_id", "$movie.genre")
                                    .append("totalRentals", new Document("$sum", 1))
                                    .append("totalRevenue", new Document("$sum", "$rental_fee"))
                                    .append("averageRentalFee", new Document("$avg", "$rental_fee"))
                                    .append("uniqueCustomers", new Document("$addToSet", "$user_id"))
                                    .append("uniqueMovies", new Document("$addToSet", "$movie_id"))
                                    .append("activeRentals", new Document("$sum",
                                            new Document("$cond", Arrays.asList(
                                                    new Document("$eq", Arrays.asList("$status", "ACTIVE")),
                                                    1, 0
                                            ))
                                    ))
                                    .append("earliestRental", new Document("$min", "$rental_date"))
                                    .append("latestRental", new Document("$max", "$rental_date"))
                    ),

                    new Document("$addFields",
                            new Document("uniqueCustomerCount", new Document("$size", "$uniqueCustomers"))
                                    .append("uniqueMovieCount", new Document("$size", "$uniqueMovies"))
                                    .append("revenuePerRental",
                                            new Document("$divide", Arrays.asList("$totalRevenue", "$totalRentals"))
                                    )
                    ),

                    new Document("$match",
                            new Document("_id", new Document("$ne", null))
                    ),

                    new Document("$sort", new Document("totalRevenue", -1)),

                    new Document("$project",
                            new Document("genre", "$_id")
                                    .append("totalRentals", 1)
                                    .append("totalRevenue", 1)
                                    .append("averageRentalFee", 1)
                                    .append("uniqueCustomerCount", 1)
                                    .append("uniqueMovieCount", 1)
                                    .append("activeRentals", 1)
                                    .append("revenuePerRental", 1)
                                    .append("earliestRental", 1)
                                    .append("latestRental", 1)
                                    .append("_id", 0)
                    )
            );

            MongoCollection<Document> rentalsCollection = database.getCollection("rentals");
            rentalsCollection.aggregate(pipeline).forEach((Consumer<? super Document>) doc -> {
                Map<String, Object> row = new HashMap<>();

                row.put("genre", doc.getString("genre"));
                row.put("totalRentals", doc.getInteger("totalRentals", 0));

                Object totalRevenueObj = doc.get("totalRevenue");
                if (totalRevenueObj instanceof Number) {
                    row.put("totalRevenue", String.format("$%.2f", ((Number) totalRevenueObj).doubleValue()));
                }

                Object avgFeeObj = doc.get("averageRentalFee");
                if (avgFeeObj instanceof Number) {
                    row.put("averageRentalFee", String.format("$%.2f", ((Number) avgFeeObj).doubleValue()));
                }

                row.put("uniqueCustomers", doc.getInteger("uniqueCustomerCount", 0));
                row.put("uniqueMovies", doc.getInteger("uniqueMovieCount", 0));
                row.put("activeRentals", doc.getInteger("activeRentals", 0));

                // Handle revenue per rental
                Object revenuePerRentalObj = doc.get("revenuePerRental");
                if (revenuePerRentalObj instanceof Number) {
                    row.put("revenuePerRental", String.format("$%.2f", ((Number) revenuePerRentalObj).doubleValue()));
                }

                // Format dates
                Date earliestRental = doc.getDate("earliestRental");
                Date latestRental = doc.getDate("latestRental");

                row.put("earliestRental", earliestRental != null ? formatDate(earliestRental) : "N/A");
                row.put("latestRental", latestRental != null ? formatDate(latestRental) : "N/A");

                // Calculate activity period in days
                if (earliestRental != null && latestRental != null) {
                    long diffInMillies = latestRental.getTime() - earliestRental.getTime();
                    long daysDiff = diffInMillies / (1000 * 60 * 60 * 24);
                    row.put("activityPeriodDays", daysDiff);
                } else {
                    row.put("activityPeriodDays", 0);
                }

                report.add(row);
            });

        } catch (Exception e) {
            System.err.println("Error generating genre performance report: " + e.getMessage());
            e.printStackTrace();
        }

        return report;
    }

    private String formatDate(Date date) {
        if (date == null) return "N/A";

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }
}
