package objectDAOs;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import objects.User;
import org.bson.Document;
import org.bson.conversions.Bson;
import utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class UserDAO {
    private MongoDatabase database;
    private MongoCollection<Document> usersCollection;
    private MongoCollection<Document> countersCollection;

    public UserDAO() {
        this.database = MongoConnection.getInstance().getDatabase();
        this.usersCollection = database.getCollection("users");
        this.countersCollection = database.getCollection("counters");
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
            // If counter doesn't exist, create it
            countersCollection.insertOne(new Document("_id", sequenceName).append("sequence_value", 1L));
            return 1L;
        }
    }

    public User createUser(User user) {
        try {
            // Get current counter for user, increment and use as id
            long nextId = getNextSequence("users");

            Document userDoc = new Document("_id", nextId)
                    .append("first_name", user.getFirstName())
                    .append("last_name", user.getLastName())
                    .append("email", user.getEmail())
                    .append("phone", user.getPhone())
                    .append("address", user.getAddress())
                    .append("cpf", user.getCpf())
                    .append("created_at", new Date());

            usersCollection.insertOne(userDoc);

            user.setUserId(nextId);
            return user;
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            return null;
        }
    }

    public User getUserById(long userId) {
        try {
            Document userDoc = usersCollection.find(eq("_id", userId)).first();

            if (userDoc != null) {
                return mapUser(userDoc);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving user: " + e.getMessage());
        }
        return null;
    }

    public User getUserByEmail(String email) {
        try {
            Document userDoc = usersCollection.find(eq("email", email)).first();

            if (userDoc != null) {
                return mapUser(userDoc);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving user by email: " + e.getMessage());
        }
        return null;
    }

    public User getUserByCpf(String cpf) {
        try {
            Document userDoc = usersCollection.find(eq("cpf", cpf)).first();

            if (userDoc != null) {
                return mapUser(userDoc);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving user by CPF: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        try {
            usersCollection.find()
                    .forEach((Consumer<? super Document>) doc -> users.add(mapUser(doc)));
        } catch (Exception e) {
            System.err.println("Error retrieving all users: " + e.getMessage());
        }

        return users;
    }

    public boolean updateUser(User user) {
        try {
            Bson filter = eq("_id", user.getUserId());

            Document updateDoc = new Document("first_name", user.getFirstName())
                    .append("last_name", user.getLastName())
                    .append("email", user.getEmail())
                    .append("phone", user.getPhone())
                    .append("address", user.getAddress())
                    .append("cpf", user.getCpf());

            UpdateResult result = usersCollection.updateOne(filter, new Document("$set", updateDoc));
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(long userId) {
        try {
            DeleteResult result = usersCollection.deleteOne(eq("_id", userId));
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    private User mapUser(Document doc) {
        User user = new User();
        user.setUserId(Utils.getDocId(doc, "_id"));
        user.setFirstName(doc.getString("first_name"));
        user.setLastName(doc.getString("last_name"));
        user.setEmail(doc.getString("email"));
        user.setPhone(doc.getString("phone"));
        user.setAddress(doc.getString("address"));
        user.setCpf(doc.getString("cpf"));

        return user;
    }
}