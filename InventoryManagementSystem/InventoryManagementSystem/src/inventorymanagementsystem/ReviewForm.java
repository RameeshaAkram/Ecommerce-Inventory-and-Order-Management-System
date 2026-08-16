package inventorymanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class ReviewForm extends JFrame {
    private JTextField reviewIdField;
    private JComboBox<String> customerIdComboBox, productIdComboBox;
    private JComboBox<Integer> ratingComboBox;
    private JTextArea reviewTextArea;
    private JButton submitButton;
    private MongoDatabase database;

    public ReviewForm(MongoDatabase database) {
        this.database = database;

        setTitle("Add Review");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(7, 2, 5, 5));

        reviewIdField = new JTextField();
        customerIdComboBox = new JComboBox<>(loadIdsFromCollection("Customers", "_id"));
        productIdComboBox = new JComboBox<>(loadIdsFromCollection("Products", "_id"));
        ratingComboBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        reviewTextArea = new JTextArea(3, 20);
        JScrollPane reviewScroll = new JScrollPane(reviewTextArea);

        submitButton = new JButton("Submit Review");

        add(new JLabel("Review ID:"));
        add(reviewIdField);
        add(new JLabel("Customer ID:"));
        add(customerIdComboBox);
        add(new JLabel("Product ID:"));
        add(productIdComboBox);
        add(new JLabel("Rating (1-5):"));
        add(ratingComboBox);
        add(new JLabel("Review Text:"));
        add(reviewScroll);
        add(new JLabel(""));
        add(submitButton);

        submitButton.addActionListener(e -> {
            String reviewId = reviewIdField.getText().trim();
            String customerId = (String) customerIdComboBox.getSelectedItem();
            String productId = (String) productIdComboBox.getSelectedItem();
            int rating = (int) ratingComboBox.getSelectedItem();
            String reviewText = reviewTextArea.getText().trim();
            Date reviewDate = new Date(); // current date

            MongoCollection<Document> reviews = database.getCollection("Reviews");

            // Validation
            if (reviewId.isEmpty() || reviewText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Review ID and Review Text must be filled.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (reviews.find(Filters.eq("_id", reviewId)).first() != null) {
                JOptionPane.showMessageDialog(this, "Review ID already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Document review = new Document("_id", reviewId)
                    .append("customer_id", customerId)
                    .append("product_id", productId)
                    .append("rating", rating)
                    .append("review_text", reviewText)
                    .append("review_date", reviewDate);

            reviews.insertOne(review);
            JOptionPane.showMessageDialog(this, "Review added successfully!");
            dispose();
        });

        setVisible(true);
    }

    private String[] loadIdsFromCollection(String collectionName, String fieldName) {
        List<String> ids = new ArrayList<>();
        MongoCollection<Document> collection = database.getCollection(collectionName);
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                ids.add(doc.getString(fieldName));
            }
        }
        return ids.toArray(new String[0]);
    }
}
