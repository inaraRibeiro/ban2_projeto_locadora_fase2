package objectDAOs;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import objects.Genre;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.set;


public class GenreDAO {
    private MongoDatabase database;
    private MongoCollection<Document> genresCollection;
    private MongoCollection<Document> countersCollection;

    public GenreDAO() {
        this.database = MongoConnection.getInstance().getDatabase();
        this.genresCollection = database.getCollection("genres");
        this.countersCollection = database.getCollection("counters");
    }

    public Genre createGenre(Genre genre) {
        try {
            // Get next sequence value for genre_id
            long nextId = getNextSequence("genres");

            Document genreDoc = new Document("_id", nextId)
                    .append("name", genre.getName())
                    .append("rental_fee", genre.getRentalFee().doubleValue());

            genresCollection.insertOne(genreDoc);

            genre.setGenreId(nextId);
            return genre;
        } catch (Exception e) {
            System.err.println("Error creating genre: " + e.getMessage());
            return null;
        }
    }

    public Genre getGenreById(long genreId) {
        try {
            Document genreDoc = genresCollection.find(eq("_id", genreId)).first();

            if (genreDoc != null) {
                return mapGenre(genreDoc);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving genre: " + e.getMessage());
        }
        return null;
    }

    public Genre getGenreByName(String name) {
        try {
            // Case-insensitive search using regex
            Pattern pattern = Pattern.compile("^" + Pattern.quote(name) + "$", Pattern.CASE_INSENSITIVE);
            Document genreDoc = genresCollection.find(regex("name", pattern)).first();

            if (genreDoc != null) {
                return mapGenre(genreDoc);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving genre by name: " + e.getMessage());
        }
        return null;
    }

    public List<Genre> getAllGenres() {
        List<Genre> genres = new ArrayList<>();

        try {
            // Sort by name (ascending)
            genresCollection.find()
                    .sort(new Document("name", 1))
                    .forEach((Consumer<? super Document>) doc -> genres.add(mapGenre(doc)));

            return genres;
        } catch (Exception e) {
            System.err.println("Error retrieving all genres: " + e.getMessage());
        }

        return genres;
    }

    public boolean updateGenre(Genre genre) {
        try {
            Bson filter = eq("_id", genre.getGenreId());
            Bson update = set("name", genre.getName());

            UpdateResult result = genresCollection.updateOne(filter, update);
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error updating genre: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteGenre(long genreId) {
        try {
            DeleteResult result = genresCollection.deleteOne(eq("_id", genreId));
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting genre: " + e.getMessage());
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
            return result.getInteger("sequence_value").longValue();
        } else {
            // If counter doesn't exist, create it
            countersCollection.insertOne(new Document("_id", sequenceName).append("sequence_value", 1L));
            return 1L;
        }
    }

    // Helper method to map Document to Genre object
    private Genre mapGenre(Document doc) {
        Genre genre = new Genre();
        genre.setGenreId(doc.getInteger("_id").longValue());
        genre.setName(doc.getString("name"));

        // Handle rental_fee - it might be stored as Double or need conversion
        Object rentalFeeObj = doc.get("rental_fee");
        if (rentalFeeObj instanceof Double) {
            genre.setRentalFee(BigDecimal.valueOf((Double) rentalFeeObj));
        } else if (rentalFeeObj instanceof Integer) {
            genre.setRentalFee(BigDecimal.valueOf((Integer) rentalFeeObj));
        } else if (rentalFeeObj instanceof Number) {
            genre.setRentalFee(BigDecimal.valueOf(((Number) rentalFeeObj).doubleValue()));
        }

        return genre;
    }
}
