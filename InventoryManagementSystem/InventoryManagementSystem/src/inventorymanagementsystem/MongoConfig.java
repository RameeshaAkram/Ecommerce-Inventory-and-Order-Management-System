package inventorymanagementsystem;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.Locale;

public final class MongoConfig {
    private static final String DEFAULT_URI = "mongodb://localhost:27017";
    private static final String DEFAULT_DATABASE = "EcommerceInventoryManagment";

    private MongoConfig() {
    }

    public static MongoClient createClient() {
        String uri = System.getenv("MONGO_URI");
        if (uri == null || uri.isBlank()) {
            uri = DEFAULT_URI;
        }
        return MongoClients.create(uri);
    }

    public static MongoDatabase getDatabase() {
        String databaseName = System.getenv("MONGO_DATABASE");
        if (databaseName == null || databaseName.isBlank()) {
            databaseName = DEFAULT_DATABASE;
        }
        return createClient().getDatabase(databaseName);
    }
}
