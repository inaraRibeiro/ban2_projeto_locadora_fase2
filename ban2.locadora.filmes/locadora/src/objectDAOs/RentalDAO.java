package objectDAOs;

import jdk.jshell.execution.Util;
import objects.Movie;
import objects.Rental;
import objects.User;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import utils.Utils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Sorts.*;

public class RentalDAO {
    private MongoDatabase database;
    private MongoCollection<Document> rentalsCollection;
    private MongoCollection<Document> countersCollection;
    private UserDAO userDAO;
    private MovieDAO movieDAO;

    public RentalDAO() {
        this.database = MongoConnection.getInstance().getDatabase();
        this.rentalsCollection = database.getCollection("rentals");
        this.countersCollection = database.getCollection("counters");
        this.userDAO = new UserDAO();
        this.movieDAO = new MovieDAO();
    }

    public Rental createRental(Rental rental) {
        try {
            // Get next sequence value for rental_id
            long nextId = getNextSequence("rentals");

            // Convert LocalDateTime to Date for MongoDB
            Date rentalDate = Date.from(rental.getRentalDate().atZone(ZoneId.systemDefault()).toInstant());
            Date dueDate = Date.from(rental.getDueDate().atZone(ZoneId.systemDefault()).toInstant());

            Document rentalDoc = new Document("_id", nextId)
                    .append("user_id", rental.getUser().getUserId())
                    .append("movie_id", rental.getMovie().getMovieId())
                    .append("rental_date", rentalDate)
                    .append("due_date", dueDate)
                    .append("return_date", null)
                    .append("rental_fee", rental.getRentalFee().doubleValue())
                    .append("status", rental.getStatus().toString());

            // Embed user and movie information for denormalization
            if (rental.getUser() != null) {
                Document userDoc = new Document("name", rental.getUser().getFirstName() + " " + rental.getUser().getLastName())
                        .append("email", rental.getUser().getEmail())
                        .append("cpf", rental.getUser().getCpf());
                rentalDoc.append("user", userDoc);
            }

            if (rental.getMovie() != null) {
                Document movieDoc = new Document("title", rental.getMovie().getTitle());
                if (rental.getMovie().getGenre() != null) {
                    movieDoc.append("genre", rental.getMovie().getGenre().getName());
                }
                rentalDoc.append("movie", movieDoc);
            }

            rentalsCollection.insertOne(rentalDoc);

            // Update movie available copies
            Movie movie = rental.getMovie();
            movie.setAvailableCopies(movie.getAvailableCopies() - 1);
            movieDAO.updateMovie(movie);

            rental.setRentalId(nextId);
            return rental;
        } catch (Exception e) {
            System.err.println("Error creating rental: " + e.getMessage());
            return null;
        }
    }

    public Rental getRentalById(long rentalId) {
        try {
            Document rentalDoc = rentalsCollection.find(eq("_id", rentalId)).first();

            if (rentalDoc != null) {
                return mapRental(rentalDoc);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving rental: " + e.getMessage());
        }
        return null;
    }

    public List<Rental> getRentalsByUser(long userId) {
        List<Rental> rentals = new ArrayList<>();

        try {
            rentalsCollection.find(eq("user_id", userId))
                    .sort(descending("rental_date"))
                    .forEach((Consumer<? super Document>) doc -> rentals.add(mapRental(doc)));
        } catch (Exception e) {
            System.err.println("Error retrieving rentals by user: " + e.getMessage());
        }

        return rentals;
    }

    public List<Rental> getRentalsByMovie(long movieId) {
        List<Rental> rentals = new ArrayList<>();

        try {
            rentalsCollection.find(eq("movie_id", movieId))
                    .sort(descending("rental_date"))
                    .forEach((Consumer<? super Document>) doc -> rentals.add(mapRental(doc)));

            return rentals;
        } catch (Exception e) {
            System.err.println("Error retrieving rentals by movie: " + e.getMessage());
        }

        return rentals;
    }

    public List<Rental> getActiveRentals() {
        List<Rental> rentals = new ArrayList<>();

        try {
            rentalsCollection.find(eq("status", "ACTIVE"))
                    .sort(ascending("due_date"))
                    .forEach((Consumer<? super Document>) doc -> rentals.add(mapRental(doc)));

            return rentals;
        } catch (Exception e) {
            System.err.println("Error retrieving active rentals: " + e.getMessage());
        }

        return rentals;
    }

    public List<Rental> getOverdueRentals() {
        List<Rental> rentals = new ArrayList<>();

        try {
            Date currentDate = new Date();

            rentalsCollection.find(and(
                            eq("status", "ACTIVE"),
                            lt("due_date", currentDate)
                    ))
                    .sort(ascending("due_date"))
                    .forEach((Consumer<? super Document>) doc -> rentals.add(mapRental(doc)));

            return rentals;
        } catch (Exception e) {
            System.err.println("Error retrieving overdue rentals: " + e.getMessage());
        }

        return rentals;
    }

    public List<Rental> getAllRentals() {
        List<Rental> rentals = new ArrayList<>();

        try {
            rentalsCollection.find()
                    .sort(descending("rental_date"))
                    .forEach((Consumer<? super Document>) doc -> rentals.add(mapRental(doc)));
        } catch (Exception e) {
            System.err.println("Error retrieving all rentals: " + e.getMessage());
        }

        return rentals;
    }

    public boolean updateRental(Rental rental) {
        try {
            Bson filter = eq("_id", rental.getRentalId());

            // Convert LocalDateTime to Date for MongoDB
            Date rentalDate = Date.from(rental.getRentalDate().atZone(ZoneId.systemDefault()).toInstant());
            Date dueDate = Date.from(rental.getDueDate().atZone(ZoneId.systemDefault()).toInstant());
            Date returnDate = null;

            if (rental.getReturnDate() != null) {
                returnDate = Date.from(rental.getReturnDate().atZone(ZoneId.systemDefault()).toInstant());
            }

            Document updateDoc = new Document("user_id", rental.getUser().getUserId())
                    .append("movie_id", rental.getMovie().getMovieId())
                    .append("rental_date", rentalDate)
                    .append("due_date", dueDate)
                    .append("return_date", returnDate)
                    .append("rental_fee", rental.getRentalFee().doubleValue())
                    .append("status", rental.getStatus().toString());

            // Update embedded user and movie information
            if (rental.getUser() != null) {
                Document userDoc = new Document("name", rental.getUser().getFirstName() + " " + rental.getUser().getLastName())
                        .append("email", rental.getUser().getEmail())
                        .append("cpf", rental.getUser().getCpf());
                updateDoc.append("user", userDoc);
            }

            if (rental.getMovie() != null) {
                Document movieDoc = new Document("title", rental.getMovie().getTitle());
                if (rental.getMovie().getGenre() != null) {
                    movieDoc.append("genre", rental.getMovie().getGenre().getName());
                }
                updateDoc.append("movie", movieDoc);
            }

            UpdateResult result = rentalsCollection.updateOne(filter, new Document("$set", updateDoc));

            // If rental is being returned, update movie available copies
            if (result.getModifiedCount() > 0 && rental.getStatus() == Rental.RentalStatus.RETURNED) {
                Movie movie = rental.getMovie();
                movie.setAvailableCopies(movie.getAvailableCopies() + 1);
                movieDAO.updateMovie(movie);
            }

            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error updating rental: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteRental(long rentalId) {
        try {
            Rental rental = getRentalById(rentalId);
            if (rental == null) {
                return false;
            }

            boolean isActive = rental.getStatus() == Rental.RentalStatus.ACTIVE;

            DeleteResult result = rentalsCollection.deleteOne(eq("_id", rentalId));

            // If deleted rental was active, update movie available copies
            if (result.getDeletedCount() > 0 && isActive) {
                Movie movie = rental.getMovie();
                movie.setAvailableCopies(movie.getAvailableCopies() + 1);
                movieDAO.updateMovie(movie);
            }

            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting rental: " + e.getMessage());
            return false;
        }
    }

    // Helper method to get next sequence value for auto-incrementing IDs
    private long getNextSequence(String sequenceName) {
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
        options.returnDocument(ReturnDocument.AFTER);

        Document result = countersCollection.findOneAndUpdate(
                eq("_id", sequenceName),
                inc("sequence_value", 1),
                options
        );

        if (result != null) {
            return Utils.getDocId(result, "sequence_value");
        } else {
            // If counter doesn't exist, create it
            countersCollection.insertOne(new Document("_id", sequenceName).append("sequence_value", 1L));
            return 1L;
        }
    }

    // Helper method to map Document to Rental object
    private Rental mapRental(Document doc) {
        try {
            Rental rental = new Rental();
            rental.setRentalId(Utils.getDocId(doc, "_id"));

            // Get User and Movie objects using their DAOs
            User user = userDAO.getUserById(Utils.getDocId(doc, "user_id"));
            Movie movie = movieDAO.getMovieById(Utils.getDocId(doc, "movie_id"));

            rental.setUser(user);
            rental.setMovie(movie);

            // Convert Date to LocalDateTime
            Date rentalDate = doc.getDate("rental_date");
            if (rentalDate != null) {
                rental.setRentalDate(rentalDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }

            Date dueDate = doc.getDate("due_date");
            if (dueDate != null) {
                rental.setDueDate(dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }

            Date returnDate = doc.getDate("return_date");
            if (returnDate != null) {
                rental.setReturnDate(returnDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }

            // Handle rental_fee
            Object rentalFeeObj = doc.get("rental_fee");
            if (rentalFeeObj instanceof Number) {
                rental.setRentalFee(BigDecimal.valueOf(((Number) rentalFeeObj).doubleValue()));
            }

            // Handle status
            String statusStr = doc.getString("status");
            if (statusStr != null) {
                rental.setStatus(Rental.RentalStatus.valueOf(statusStr));
            }

            return rental;
        } catch (Exception e) {
            System.err.println("Error mapping rental: " + e.getMessage());
            return null;
        }
    }
}