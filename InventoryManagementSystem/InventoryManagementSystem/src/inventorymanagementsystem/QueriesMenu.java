/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */package inventorymanagementsystem;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;

import javax.swing.*;
import java.awt.Font;
import java.awt.Color;

import java.util.ArrayList;
 import java.util.List;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class QueriesMenu extends JMenu {

    private final MongoDatabase database;
    private final JTable table;

    public QueriesMenu(MongoDatabase database, JTable tbl) {
        super("Queries");
        this.database = database;
        this.table= tbl;

        setFont(new Font("Tahoma", Font.BOLD, 16));
        setOpaque(true);
        setBackground(Color.decode("#493728"));
        setForeground(Color.WHITE);

        addQueryItem("Top 5 Selling Products", this::topSellingProducts);
        addQueryItem("High Spending Customers", this::highSpendingCustomers);
        addQueryItem("Popular Payment Methods", this::popularPaymentMethods);
        addQueryItem("Out-of-Stock Products", this::outOfStockProducts);
        addQueryItem("Customers with Most Orders", this::customersWithMostOrders);
        addQueryItem("Customers with Most Reviews", this::customersWithMostReviews);
        addQueryItem("Orders Not Delivered", this::ordersNotDelivered);
        addQueryItem("Pending Payments", this::pendingPayments);
        addQueryItem("Low Stock Products (<5)", this::lowStockProducts);
        addQueryItem("Customers by City", this::customersByCity);
        addQueryItem("Products Sold Per Supplier", this::productsSoldPerSupplier);
    }

    private void addQueryItem(String title, Runnable action) {
        JMenuItem item = new JMenuItem(title);
        item.addActionListener(e -> action.run());
        add(item);
    }

    private void showResults(AggregateIterable<Document> results) {
        List<String> columnNames = new ArrayList<>();
        List<List<Object>> rowData = new ArrayList<>();

        for (Document doc : results) {
            Map<String, Object> flatMap = flattenDocument(doc);
            if (columnNames.isEmpty()) {
                columnNames.addAll(flatMap.keySet());
            }

            List<Object> row = new ArrayList<>();
            for (String col : columnNames) {
                row.add(flatMap.getOrDefault(col, ""));
            }
            rowData.add(row);
        }

        // Convert to TableModel
        DefaultTableModel model = new DefaultTableModel();
        for (String col : columnNames) {
            model.addColumn(col);
        }
        for (List<Object> row : rowData) {
            model.addRow(row.toArray());
        }

        table.setModel(model);
    }

    private Map<String, Object> flattenDocument(Document doc) {
    Map<String, Object> flatMap = new LinkedHashMap<>();
    flattenHelper("", doc, flatMap);
    return flatMap;
}

    private void flattenHelper(String prefix, Object value, Map<String, Object> result) {
        if (value instanceof Document) {
            Document doc = (Document) value;
            for (Map.Entry<String, Object> entry : doc.entrySet()) {
                flattenHelper(prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey(), entry.getValue(), result);
            }
        } else if (value instanceof List) {
            result.put(prefix, value.toString()); // You can format lists better if needed
        } else {
            result.put(prefix, value);
        }
    }

    private void topSellingProducts() {
        AggregateIterable<Document> result = database.getCollection("Orders").aggregate(Arrays.asList(
            new Document("$unwind", "$items"),
            new Document("$group", new Document("_id", "$items.product_id")
                    .append("total_sold", new Document("$sum", "$items.quantity"))),
            new Document("$sort", new Document("total_sold", -1)),
            new Document("$limit", 5)
        ));
        showResults(result);
    }

    private void highSpendingCustomers() {
        AggregateIterable<Document> result = database.getCollection("Orders").aggregate(Arrays.asList(
            new Document("$group", new Document("_id", "$customer_id")
                .append("total_spent", new Document("$sum", "$total_price"))
                .append("order_count", new Document("$sum", 1))),
            new Document("$sort", new Document("total_spent", -1)),
            new Document("$limit", 5),
            new Document("$project", new Document("_id", 0)
                .append("customer_id", "$_id")
                .append("total_spent", 1)
                .append("order_count", 1))
        ));

        showResults(result);
    }


    private void popularPaymentMethods() {
        AggregateIterable<Document> result = database.getCollection("Payments").aggregate(Arrays.asList(
            new Document("$group", new Document("_id", "$payment_method")
                .append("count", new Document("$sum", 1))
                .append("total_amount", new Document("$sum", "$amount"))),
            new Document("$sort", new Document("count", -1))
        ));
        showResults(result);
    }

    private void outOfStockProducts() {
        AggregateIterable<Document> result = database.getCollection("Products").aggregate(Arrays.asList(
            new Document("$match", new Document("stock", 0))
        ));
        showResults(result);
    }

    private void customersWithMostOrders() {
        AggregateIterable<Document> result = database.getCollection("Orders").aggregate(Arrays.asList(
            new Document("$group", new Document("_id", "$customer_id")
                .append("total_orders", new Document("$sum", 1))),
            new Document("$sort", new Document("total_orders", -1)),
            new Document("$limit", 5)
        ));
        showResults(result);
    }

    private void customersWithMostReviews() {
        AggregateIterable<Document> result = database.getCollection("Reviews").aggregate(Arrays.asList(
            new Document("$group", new Document("_id", "$customer_id")
                .append("reviews", new Document("$sum", 1))),
            new Document("$sort", new Document("reviews", -1)),
            new Document("$limit", 5)
        ));
        showResults(result);
    }


    private void ordersNotDelivered() {
        AggregateIterable<Document> result = database.getCollection("Shipping").aggregate(Arrays.asList(
            new Document("$match", new Document("expected_delivery", new Document("$lt", new java.util.Date()))
                .append("status", new Document("$ne", "Delivered"))),
            new Document("$project", new Document("_id", 1)
                .append("order_id", 1)
                .append("customer_id", 1)
                .append("status", 1))
        ));
        showResults(result);
    }


    private void pendingPayments() {
        AggregateIterable<Document> result = database.getCollection("Payments").aggregate(Arrays.asList(
            new Document("$match", new Document("status", "Pending")),
            new Document("$project", new Document("_id", 1)
                .append("order_id", 1)
                .append("customer_id", 1)
                .append("status", 1))
        ));
        showResults(result);
    }


    private void lowStockProducts() {
        AggregateIterable<Document> result = database.getCollection("Products").aggregate(Arrays.asList(
            new Document("$match", new Document("stock", new Document("$lt", 5))),
            new Document("$project", new Document("_id", 1)
                .append("name", 1)
                .append("stock", 1))
        ));
        showResults(result);
    }


    private void customersByCity() {
        AggregateIterable<Document> result = database.getCollection("Customers").aggregate(Arrays.asList(
            new Document("$group", new Document("_id", "$address.city")
                .append("count", new Document("$sum", 1))),
            new Document("$sort", new Document("count", -1))
        ));
        showResults(result);
    }

    private void productsSoldPerSupplier() {
        AggregateIterable<Document> result = database.getCollection("Orders").aggregate(Arrays.asList(
            new Document("$unwind", "$items"),
            new Document("$lookup", new Document("from", "Products")
                .append("localField", "items.product_id")
                .append("foreignField", "_id")
                .append("as", "product_info")),
            new Document("$unwind", "$product_info"),
            new Document("$group", new Document("_id", "$product_info.supplier_id")
                .append("total_quantity_sold", new Document("$sum", "$items.quantity")))
        ));
        showResults(result);
    }
}
