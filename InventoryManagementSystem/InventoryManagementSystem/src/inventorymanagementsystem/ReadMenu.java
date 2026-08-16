/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventorymanagementsystem;

import com.mongodb.client.*;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;

public class ReadMenu extends JMenu {

    private final MongoDatabase database;
    private final JTable table;

    public ReadMenu(MongoDatabase db, JTable tbl) {
        super("Read");
        this.database = db;
        this.table = tbl;

        setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 16));
        setOpaque(true);
        setBackground(java.awt.Color.decode("#493728"));
        setForeground(java.awt.Color.WHITE);

        String[] collections = {
            "Products", "Orders", "Customers", "Shipping",
            "Payments", "Reviews", "Cart", "Categories", "Suppliers"
        };

        for (String col : collections) {
            JMenuItem item = new JMenuItem(col);
            item.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 14));
            item.addActionListener(e -> displayCollection(col));
            add(item);
        }
    }

    private void displayCollection(String collectionName) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        FindIterable<Document> documents = collection.find();

        Vector<String> columnNames = new Vector<>();
        Vector<Vector<Object>> data = new Vector<>();

        for (Document doc : documents) {
            Vector<Object> row = new Vector<>();
            if (columnNames.isEmpty()) {
                for (String key : doc.keySet()) {
                    columnNames.add(key);
                }
            }
            for (String key : columnNames) {
                row.add(doc.getOrDefault(key, ""));
            }
            data.add(row);
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        table.setModel(model);
    }
}
