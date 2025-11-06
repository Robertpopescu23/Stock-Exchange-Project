package org.example.engine;


import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.ConnectionString;
import org.bson.Document;

public class DatabaseManager {
    private final MongoClient mongoClient;
    private final MongoCollection<Document> transactionsCollection;

    public DatabaseManager(String connectionString, String dbName, String collectionName) {
        mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(connectionString))
                        .build()
        );
        MongoDatabase database = mongoClient.getDatabase(dbName);
        transactionsCollection = database.getCollection(collectionName);
        System.out.println("Connected to MongoDB database: " + dbName);
    }

    public MongoCollection<Document> getTransactionsCollection() {
        return transactionsCollection;
    }

    public void close() {
        mongoClient.close();
        System.out.println("MongoDB connection closed.");
    }
}
