package objectDAOs;

import objects.Genre;
import objects.Movie;

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
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class MovieDAO {
    private MongoDatabase database;
    private MongoCollection<Document> moviesCollection;
    private MongoCollection<Document> countersCollection;
    private GenreDAO genreDAO;

    public MovieDAO() {
        this.database = MongoConnection.getInstance().getDatabase();
        this.moviesCollection = database.getCollection("movies");
        this.countersCollection = database.getCollection("counters");
        this.genreDAO = new GenreDAO();
    }

    public Movie createMovie(Movie movie) {
        try {
            // Get next sequence value for movie_id
            long nextId = getNextSequence("movies");

            Document movieDoc = new Document("_id", nextId)
                    .append("title", movie.getTitle())
                    .append("release_year", movie.getReleaseYear())
                    .append("director", movie.getDirector())
                    .append("duration_minutes", movie.getDurationMinutes())
                    .append("rating", movie.getRating())
                    .append("total_copies", movie.getTotalCopies())
                    .append("available_copies", movie.getAvailableCopies())
                    .append("date_added", new Date());

            // Handle genre_id
            if (movie.getGenre() != null && movie.getGenre().getGenreId() != null) {
                movieDoc.append("genre_id", movie.getGenre().getGenreId());

                // Embed genre information for denormalization
                Document genreDoc = new Document("name", movie.getGenre().getName());
                if (movie.getGenre().getRentalFee() != null) {
                    genreDoc.append("rental_fee", movie.getGenre().getRentalFee().doubleValue());
                }
                movieDoc.append("genre", genreDoc);
            } else {
                movieDoc.append("genre_id", null);
            }

            moviesCollection.insertOne(movieDoc);

            movie.setMovieId(nextId);
            return movie;
        } catch (Exception e) {
            System.err.println("Error creating movie: " + e.getMessage());
            return null;
        }
    }

    public Movie getMovieById(long movieId) {
        try {
            Document movieDoc = moviesCollection.find(eq("_id", movieId)).first();

            if (movieDoc != null) {
                return mapMovieWithGenre(movieDoc);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving movie: " + e.getMessage());
        }
        return null;
    }

    public List<Movie> searchMovies(String searchTerm) {
        List<Movie> movies = new ArrayList<>();

        try {
            // Create case-insensitive regex pattern
            Pattern pattern = Pattern.compile(Pattern.quote(searchTerm), Pattern.CASE_INSENSITIVE);

            // Search in title, director, or genre name
            Bson filter = or(
                    regex("title", pattern),
                    regex("director", pattern),
                    regex("genre.name", pattern)
            );

            moviesCollection.find(filter)
                    .forEach((Consumer<? super Document>) doc -> movies.add(mapMovieWithGenre(doc)));

            return movies;
        } catch (Exception e) {
            System.err.println("Error searching movies: " + e.getMessage());
        }

        return movies;
    }

    public List<Movie> getMoviesByGenre(long genreId) {
        List<Movie> movies = new ArrayList<>();

        try {
            moviesCollection.find(eq("genre_id", genreId))
                    .forEach((Consumer<? super Document>) doc -> movies.add(mapMovieWithGenre(doc)));

            return movies;
        } catch (Exception e) {
            System.err.println("Error retrieving movies by genre: " + e.getMessage());
        }

        return movies;
    }

    public List<Movie> getAvailableMovies() {
        List<Movie> movies = new ArrayList<>();

        try {
            moviesCollection.find(gt("available_copies", 0))
                    .forEach((Consumer<? super Document>) doc -> movies.add(mapMovieWithGenre(doc)));

            return movies;
        } catch (Exception e) {
            System.err.println("Error retrieving available movies: " + e.getMessage());
        }

        return movies;
    }

    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();

        try {
            moviesCollection.find()
                    .forEach((Consumer<? super Document>) doc -> movies.add(mapMovieWithGenre(doc)));

            return movies;
        } catch (Exception e) {
            System.err.println("Error retrieving all movies: " + e.getMessage());
        }

        return movies;
    }

    public boolean updateMovie(Movie movie) {
        try {
            Bson filter = eq("_id", movie.getMovieId());

            Document updateDoc = new Document("title", movie.getTitle())
                    .append("release_year", movie.getReleaseYear())
                    .append("director", movie.getDirector())
                    .append("duration_minutes", movie.getDurationMinutes())
                    .append("rating", movie.getRating())
                    .append("total_copies", movie.getTotalCopies())
                    .append("available_copies", movie.getAvailableCopies());

            if (movie.getGenre() != null && movie.getGenre().getGenreId() != null) {
                updateDoc.append("genre_id", movie.getGenre().getGenreId());

                Document genreDoc = new Document("name", movie.getGenre().getName());
                if (movie.getGenre().getRentalFee() != null) {
                    genreDoc.append("rental_fee", movie.getGenre().getRentalFee().doubleValue());
                }
                updateDoc.append("genre", genreDoc);
            } else {
                updateDoc.append("genre_id", null);
                updateDoc.append("genre", null);
            }

            UpdateResult result = moviesCollection.updateOne(filter, new Document("$set", updateDoc));
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error updating movie: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteMovie(long movieId) {
        try {
            DeleteResult result = moviesCollection.deleteOne(eq("_id", movieId));
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting movie: " + e.getMessage());
            return false;
        }
    }

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
            countersCollection.insertOne(new Document("_id", sequenceName).append("sequence_value", 1L));
            return 1L;
        }
    }

    private Movie mapMovieWithGenre(Document doc) {
        Movie movie = new Movie();

        movie.setMovieId(Utils.getDocId(doc, "_id"));
        movie.setTitle(doc.getString("title"));
        movie.setReleaseYear(doc.getInteger("release_year", 0));
        movie.setDirector(doc.getString("director"));
        movie.setDurationMinutes(doc.getInteger("duration_minutes", 0));
        movie.setRating(doc.getString("rating"));
        movie.setTotalCopies(doc.getInteger("total_copies", 0));
        movie.setAvailableCopies(doc.getInteger("available_copies", 0));

        // Handle date_added
        Date dateAdded = doc.getDate("date_added");
        if (dateAdded != null) {
            movie.setDateAdded(dateAdded.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        // Handle genre
        Long genreId = Utils.getDocId(doc, "genre_id");
        if (genreId != null) {
            Genre genre = new Genre();
            genre.setGenreId(genreId);

            // Try to get genre info from embedded document first
            Document genreDoc = doc.get("genre", Document.class);
            if (genreDoc != null) {
                genre.setName(genreDoc.getString("name"));
                Object rentalFeeObj = genreDoc.get("rental_fee");
                if (rentalFeeObj instanceof Number) {
                    genre.setRentalFee(BigDecimal.valueOf(((Number) rentalFeeObj).doubleValue()));
                }
            } else {
                // Fallback: get genre info from GenreDAO
                Genre fullGenre = genreDAO.getGenreById(genreId);
                if (fullGenre != null) {
                    genre.setName(fullGenre.getName());
                    genre.setRentalFee(fullGenre.getRentalFee());
                }
            }

            movie.setGenre(genre);
        }

        return movie;
    }
}