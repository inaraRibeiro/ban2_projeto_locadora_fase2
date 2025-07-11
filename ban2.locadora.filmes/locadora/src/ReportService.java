
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import objectDAOs.MongoConnection;
import objectDAOs.RentalDAO;
import objects.Rental;
import org.bson.Document;
import utils.Utils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

public class ReportService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private MongoDatabase database;
    private RentalDAO rentalDAO;
    private MongoCollection<Document> rentalsCollection;

    public ReportService() {
        this.database = MongoConnection.getInstance().getDatabase();
        this.rentalsCollection = database.getCollection("rentals");
        this.rentalDAO = new RentalDAO();
    }

    public List<Map<String, Object>> getUserRentalHistory(long userId) {
        List<Map<String, Object>> report = new ArrayList<>();

        try {
            List<Document> pipeline = Arrays.asList(

                    new Document("$match",
                            new Document("user_id", userId)
                    ),

                    new Document("$lookup",
                            new Document("from", "movies")
                                    .append("localField", "movie_id")
                                    .append("foreignField", "_id")
                                    .append("as", "movieDetails")
                    ),

                    // Step 3: Unwind movie details
                    new Document("$unwind",
                            new Document("path", "$movieDetails")
                                    .append("preserveNullAndEmptyArrays", true)
                    ),

                    // Step 4: Lookup genre details (equivalent to LEFT JOIN genres g ON m.genre_id = g.genre_id)
                    new Document("$lookup",
                            new Document("from", "genres")
                                    .append("localField", "movieDetails.genre_id")
                                    .append("foreignField", "_id")
                                    .append("as", "genreDetails")
                    ),

                    // Step 5: Unwind genre details (preserving null for LEFT JOIN behavior)
                    new Document("$unwind",
                            new Document("path", "$genreDetails")
                                    .append("preserveNullAndEmptyArrays", true)
                    ),

                    // Step 6: Sort by rental_date descending (equivalent to ORDER BY r.rental_date DESC)
                    new Document("$sort",
                            new Document("rental_date", -1)
                    ),

                    // Step 7: Project the final structure with all required fields
                    new Document("$project",
                            new Document("rentalId", "$_id")
                                    .append("movieTitle", "$movieDetails.title")
                                    .append("genre", "$genreDetails.name")
                                    .append("rentalDate", "$rental_date")
                                    .append("dueDate", "$due_date")
                                    .append("returnDate", "$return_date")
                                    .append("rentalFee", "$rental_fee")
                                    .append("status", "$status")
                                    .append("_id", 0)
                    )
            );

            // Execute the aggregation on rentals collection
            rentalsCollection.aggregate(pipeline).forEach((Consumer<? super Document>) doc -> {
                Map<String, Object> row = new HashMap<>();

                row.put("rentalId", Utils.getDocId(doc, "rentalId"));
                row.put("movieTitle", doc.getString("movieTitle"));
                row.put("genre", doc.getString("genre"));

                // Format dates using the same formatDateTime method
                Date rentalDate = doc.getDate("rentalDate");
                row.put("rentalDate", rentalDate != null ? formatDateTime(new Timestamp(rentalDate.getTime())) : null);

                Date dueDate = doc.getDate("dueDate");
                row.put("dueDate", dueDate != null ? formatDateTime(new Timestamp(dueDate.getTime())) : null);

                Date returnDate = doc.getDate("returnDate");
                row.put("returnDate", returnDate != null ? formatDateTime(new Timestamp(returnDate.getTime())) : "Not Returned");

                // Handle rental fee
                Object rentalFeeObj = doc.get("rentalFee");
                if (rentalFeeObj instanceof Number) {
                    row.put("rentalFee", BigDecimal.valueOf(((Number) rentalFeeObj).doubleValue()));
                } else {
                    row.put("rentalFee", null);
                }

                row.put("status", doc.getString("status"));

                report.add(row);
            });

        } catch (Exception e) {
            System.err.println("Error generating user rental history report: " + e.getMessage());
        }

        return report;
    }

    public List<Map<String, Object>> getOverdueRentalsReport() {
        List<Map<String, Object>> report = new ArrayList<>();

        try {
            Date currentDate = new Date();

            // MongoDB aggregation pipeline to replicate the SQL query
            List<Document> pipeline = Arrays.asList(
                    // Step 1: Filter for active rentals that are overdue
                    // (equivalent to WHERE r.status = 'ACTIVE' AND r.due_date < CURRENT_TIMESTAMP)
                    new Document("$match",
                            new Document("$and", Arrays.asList(
                                    new Document("status", "ACTIVE"),
                                    new Document("due_date", new Document("$lt", currentDate))
                            ))
                    ),

                    // Step 2: Lookup user details (equivalent to JOIN users u ON r.user_id = u.user_id)
                    new Document("$lookup",
                            new Document("from", "users")
                                    .append("localField", "user_id")
                                    .append("foreignField", "_id")
                                    .append("as", "userDetails")
                    ),

                    // Step 3: Unwind user details
                    new Document("$unwind", "$userDetails"),

                    // Step 4: Lookup movie details (equivalent to JOIN movies m ON r.movie_id = m.movie_id)
                    new Document("$lookup",
                            new Document("from", "movies")
                                    .append("localField", "movie_id")
                                    .append("foreignField", "_id")
                                    .append("as", "movieDetails")
                    ),

                    // Step 5: Unwind movie details
                    new Document("$unwind", "$movieDetails"),

                    // Step 6: Lookup genre details (equivalent to LEFT JOIN genres g ON m.genre_id = g.genre_id)
                    new Document("$lookup",
                            new Document("from", "genres")
                                    .append("localField", "movieDetails.genre_id")
                                    .append("foreignField", "_id")
                                    .append("as", "genreDetails")
                    ),

                    // Step 7: Unwind genre details (preserving null for LEFT JOIN behavior)
                    new Document("$unwind",
                            new Document("path", "$genreDetails")
                                    .append("preserveNullAndEmptyArrays", true)
                    ),

                    // Step 8: Add calculated field for days overdue
                    // (equivalent to EXTRACT(DAY FROM (CURRENT_TIMESTAMP - r.due_date)) AS days_overdue)
                    new Document("$addFields",
                            new Document("daysOverdue",
                                    new Document("$floor",
                                            new Document("$divide", Arrays.asList(
                                                    new Document("$subtract", Arrays.asList(currentDate, "$due_date")),
                                                    1000 * 60 * 60 * 24 // Convert milliseconds to days
                                            ))
                                    )
                            )
                    ),

                    // Step 9: Sort by days overdue descending (equivalent to ORDER BY days_overdue DESC)
                    new Document("$sort",
                            new Document("daysOverdue", -1)
                    ),

                    // Step 10: Project the final structure with all required fields
                    new Document("$project",
                            new Document("rentalId", "$_id")
                                    .append("customerId", "$userDetails._id")
                                    .append("firstName", "$userDetails.first_name")
                                    .append("lastName", "$userDetails.last_name")
                                    .append("email", "$userDetails.email")
                                    .append("phone", "$userDetails.phone")
                                    .append("movieId", "$movieDetails._id")
                                    .append("movieTitle", "$movieDetails.title")
                                    .append("genre", "$genreDetails.name")
                                    .append("rentalDate", "$rental_date")
                                    .append("dueDate", "$due_date")
                                    .append("daysOverdue", "$daysOverdue")
                                    .append("_id", 0)
                    )
            );

            // Execute the aggregation on rentals collection
            rentalsCollection.aggregate(pipeline).forEach((Consumer<? super Document>) doc -> {
                Map<String, Object> row = new HashMap<>();

                row.put("rentalId", Utils.getDocId(doc, "rentalId"));
                row.put("customerId", Utils.getDocId(doc, "customerId"));

                // Combine first and last name
                String firstName = doc.getString("firstName");
                String lastName = doc.getString("lastName");
                String customerName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                row.put("customerName", customerName.trim());

                row.put("email", doc.getString("email"));
                row.put("phone", doc.getString("phone"));
                row.put("movieId", Utils.getDocId(doc, "movieId"));
                row.put("movieTitle", doc.getString("movieTitle"));
                row.put("genre", doc.getString("genre"));

                // Format dates using the same formatDateTime method
                Date rentalDate = doc.getDate("rentalDate");
                row.put("rentalDate", rentalDate != null ? formatDateTime(rentalDate) : null);

                Date dueDate = doc.getDate("dueDate");
                row.put("dueDate", dueDate != null ? formatDateTime(dueDate) : null);

                row.put("daysOverdue", Utils.getIntegerDoc(doc, "daysOverdue", 0));

                report.add(row);
            });

        } catch (Exception e) {
            System.err.println("Error generating overdue rentals report: " + e.getMessage());
        }

        return report;
    }

    public List<Map<String, Object>> getCustomerActivityReport() {
        List<Map<String, Object>> report = new ArrayList<>();

        try {
            Date currentDate = new Date();

            List<Document> pipeline = Arrays.asList(
                    new Document("$group",
                            new Document("_id", "$user_id")
                                    .append("customerName", new Document("$first", "$user.name"))
                                    .append("customerEmail", new Document("$first", "$user.email"))
                                    .append("customerCpf", new Document("$first", "$user.cpf"))
                                    .append("totalRentals", new Document("$sum", 1))
                                    .append("totalSpent", new Document("$sum", "$rental_fee"))
                                    .append("averageRentalFee", new Document("$avg", "$rental_fee"))
                                    .append("firstRental", new Document("$min", "$rental_date"))
                                    .append("lastRental", new Document("$max", "$rental_date"))
                                    .append("activeRentals", new Document("$sum",
                                            new Document("$cond", Arrays.asList(
                                                    new Document("$eq", Arrays.asList("$status", "ACTIVE")),
                                                    1, 0
                                            ))
                                    ))
                                    .append("returnedRentals", new Document("$sum",
                                            new Document("$cond", Arrays.asList(
                                                    new Document("$eq", Arrays.asList("$status", "RETURNED")),
                                                    1, 0
                                            ))
                                    ))
                                    .append("overdueRentals", new Document("$sum",
                                            new Document("$cond", Arrays.asList(
                                                    new Document("$and", Arrays.asList(
                                                            new Document("$eq", Arrays.asList("$status", "ACTIVE")),
                                                            new Document("$lt", Arrays.asList("$due_date", currentDate))
                                                    )),
                                                    1, 0
                                            ))
                                    ))
                                    .append("genresRented", new Document("$addToSet", "$movie.genre"))
                                    .append("moviesRented", new Document("$addToSet", "$movie.title"))
                    ),

                    new Document("$addFields",
                            new Document("customerTenureDays",
                                    new Document("$floor",
                                            new Document("$divide", Arrays.asList(
                                                    new Document("$subtract", Arrays.asList(currentDate, "$firstRental")),
                                                    1000 * 60 * 60 * 24
                                            ))
                                    )
                            )
                                    .append("daysSinceLastRental",
                                            new Document("$floor",
                                                    new Document("$divide", Arrays.asList(
                                                            new Document("$subtract", Arrays.asList(currentDate, "$lastRental")),
                                                            1000 * 60 * 60 * 24
                                                    ))
                                            )
                                    )
                                    .append("genreVariety", new Document("$size", "$genresRented"))
                                    .append("uniqueMovies", new Document("$size", "$moviesRented"))
                                    .append("returnRate",
                                            new Document("$cond", Arrays.asList(
                                                    new Document("$gt", Arrays.asList("$totalRentals", 0)),
                                                    new Document("$multiply", Arrays.asList(
                                                            new Document("$divide", Arrays.asList("$returnedRentals", "$totalRentals")),
                                                            100
                                                    )),
                                                    0
                                            ))
                                    )
                                    .append("overdueRate",
                                            new Document("$cond", Arrays.asList(
                                                    new Document("$gt", Arrays.asList("$totalRentals", 0)),
                                                    new Document("$multiply", Arrays.asList(
                                                            new Document("$divide", Arrays.asList("$overdueRentals", "$totalRentals")),
                                                            100
                                                    )),
                                                    0
                                            ))
                                    )
                                    .append("customerSegment",
                                            new Document("$switch",
                                                    new Document("branches", Arrays.asList(
                                                            new Document("case", new Document("$gte", Arrays.asList("$totalSpent", 50)))
                                                                    .append("then", "Gold"),
                                                            new Document("case", new Document("$gte", Arrays.asList("$totalSpent", 20)))
                                                                    .append("then", "Premium"),
                                                            new Document("case", new Document("$gte", Arrays.asList("$totalSpent", 10)))
                                                                    .append("then", "Platinum")
                                                    ))
                                                            .append("default", "Basic")
                                            )
                                    )
                                    .append("customerStatus",
                                            new Document("$switch",
                                                    new Document("branches", Arrays.asList(
                                                            new Document("case", new Document("$gt", Arrays.asList("$activeRentals", 0)))
                                                                    .append("then", "Active"),
                                                            new Document("case", new Document("$lte", Arrays.asList("$daysSinceLastRental", 30)))
                                                                    .append("then", "Recent"),
                                                            new Document("case", new Document("$lte", Arrays.asList("$daysSinceLastRental", 90)))
                                                                    .append("then", "Inactive")
                                                    ))
                                                            .append("default", "Churned")
                                            )
                                    )
                    ),

                    // Step 3: Sort by total spent (highest value customers first)
                    new Document("$sort", new Document("totalSpent", -1)),

                    // Step 4: Project final structure (clean up the output)
                    new Document("$project",
                            new Document("customerId", "$_id")
                                    .append("customerName", 1)
                                    .append("customerEmail", 1)
                                    .append("customerCpf", 1)
                                    .append("totalRentals", 1)
                                    .append("totalSpent", 1)
                                    .append("averageRentalFee", 1)
                                    .append("activeRentals", 1)
                                    .append("returnedRentals", 1)
                                    .append("overdueRentals", 1)
                                    .append("firstRental", 1)
                                    .append("lastRental", 1)
                                    .append("customerTenureDays", 1)
                                    .append("daysSinceLastRental", 1)
                                    .append("genreVariety", 1)
                                    .append("uniqueMovies", 1)
                                    .append("returnRate", 1)
                                    .append("overdueRate", 1)
                                    .append("customerSegment", 1)
                                    .append("customerStatus", 1)
                                    .append("_id", 0)
                    )
            );

            MongoCollection<Document> rentalsCollection = database.getCollection("rentals");
            rentalsCollection.aggregate(pipeline).forEach((Consumer<? super Document>) doc -> {
                Map<String, Object> row = new HashMap<>();

                row.put("customerId", Utils.getDocId(doc, "customerId"));
                row.put("customerName", doc.getString("customerName"));
                row.put("customerEmail", doc.getString("customerEmail"));
                row.put("customerCpf", doc.getString("customerCpf"));
                row.put("totalRentals", doc.getInteger("totalRentals", 0));

                Object totalSpentObj = doc.get("totalSpent");
                if (totalSpentObj instanceof Number) {
                    row.put("totalSpent", String.format("$%.2f", ((Number) totalSpentObj).doubleValue()));
                }

                Object avgFeeObj = doc.get("averageRentalFee");
                if (avgFeeObj instanceof Number) {
                    row.put("averageRentalFee", String.format("$%.2f", ((Number) avgFeeObj).doubleValue()));
                }

                row.put("activeRentals", doc.getInteger("activeRentals", 0));
                row.put("returnedRentals", doc.getInteger("returnedRentals", 0));
                row.put("overdueRentals", doc.getInteger("overdueRentals", 0));

                // Format dates
                Date firstRental = doc.getDate("firstRental");
                Date lastRental = doc.getDate("lastRental");

                row.put("firstRental", firstRental != null ? formatDate(firstRental) : "N/A");
                row.put("lastRental", lastRental != null ? formatDate(lastRental) : "N/A");

                row.put("customerTenureDays", Utils.getIntegerDoc(doc, "customerTenureDays", 0));
                row.put("daysSinceLastRental", Utils.getIntegerDoc(doc, "daysSinceLastRental", 0));
                row.put("genreVariety", Utils.getIntegerDoc(doc, "genreVariety", 0));
                row.put("uniqueMovies", Utils.getIntegerDoc(doc, "uniqueMovies", 0));

                // Format percentages
                Object returnRateObj = doc.get("returnRate");
                if (returnRateObj instanceof Number) {
                    row.put("returnRate", String.format("%.1f%%", ((Number) returnRateObj).doubleValue()));
                }

                Object overdueRateObj = doc.get("overdueRate");
                if (overdueRateObj instanceof Number) {
                    row.put("overdueRate", String.format("%.1f%%", ((Number) overdueRateObj).doubleValue()));
                }

                row.put("customerSegment", doc.getString("customerSegment"));
                row.put("customerStatus", doc.getString("customerStatus"));

                report.add(row);
            });

        } catch (Exception e) {
            System.err.println("Error generating customer activity report: " + e.getMessage());
            e.printStackTrace();
        }

        return report;
    }

    private String formatDateTime(Date date) {
        if (date == null) return null;

        Timestamp timestamp = new Timestamp(date.getTime());
        return formatDateTime(timestamp);
    }

    private String formatDate(Date date) {
        if (date == null) return "N/A";

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }

    private String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime().format(DATE_FORMATTER);
    }
}
