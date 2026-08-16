package inventorymanagementsystem;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class SummaryStatisticsWindow extends JFrame {

    public SummaryStatisticsWindow(MongoDatabase database) {
        super("Summary Statistics");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new GridLayout(0, 2, 10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        content.add(createStatCard("Total Products", getCount(database, "Products")));
        content.add(createStatCard("Total Orders", getCount(database, "Orders")));
        content.add(createStatCard("Total Customers", getCount(database, "Customers")));
        content.add(createStatCard("Total Suppliers", getCount(database, "Suppliers")));
        content.add(createStatCard("Total Payments", getCount(database, "Payments")));
        content.add(createStatCard("Total Reviews", getCount(database, "Reviews")));

        add(new JScrollPane(content), BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createStatCard(String label, long value) {
        JPanel card = new JPanel();
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(73, 55, 40), 1),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        card.setBackground(new Color(230, 161, 172));
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel(label, SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.BOLD, 16));
        title.setForeground(new Color(73, 55, 40));

        JLabel count = new JLabel(String.valueOf(value), SwingConstants.CENTER);
        count.setFont(new Font("Tahoma", Font.BOLD, 26));
        count.setForeground(new Color(73, 55, 40));

        card.add(title, BorderLayout.NORTH);
        card.add(count, BorderLayout.CENTER);
        return card;
    }

    private long getCount(MongoDatabase database, String collectionName) {
        try {
            AggregateIterable<Document> result = database.getCollection(collectionName)
                    .aggregate(Arrays.asList(new Document("$group", new Document("_id", null).append("count", new Document("$sum", 1)))));
            for (Document doc : result) {
                Number count = doc.get("count", Number.class);
                if (count != null) {
                    return count.longValue();
                }
            }
        } catch (Exception e) {
            System.err.println("Could not compute summary for " + collectionName + ": " + e.getMessage());
        }
        return 0L;
    }
}
