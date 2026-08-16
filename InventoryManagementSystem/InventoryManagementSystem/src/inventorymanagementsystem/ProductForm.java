/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventorymanagementsystem;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ProductForm extends JFrame {

    private JTextField idField, nameField, descriptionField, priceField, stockField;
    private JTextField colorField, sizeField;
    private JComboBox<String> categoryDropdown, supplierDropdown;
    private final MongoDatabase database;

    public ProductForm(MongoDatabase db) {
        this.database = db;
        setTitle("Add New Product");
        setSize(500, 600);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(10, 2, 10, 10));

        // Initialize fields
        idField = new JTextField();
        nameField = new JTextField();
        descriptionField = new JTextField();
        priceField = new JTextField();
        stockField = new JTextField();
        colorField = new JTextField();
        sizeField = new JTextField();

        categoryDropdown = new JComboBox<>(getCollectionIds("Categories"));
        supplierDropdown = new JComboBox<>(getCollectionIds("Suppliers"));

        // Add fields to frame
        add(new JLabel("Product ID:"));
        add(idField);
        add(new JLabel("Name:"));
        add(nameField);
        add(new JLabel("Description:"));
        add(descriptionField);
        add(new JLabel("Price:"));
        add(priceField);
        add(new JLabel("Stock:"));
        add(stockField);
        add(new JLabel("Category:"));
        add(categoryDropdown);
        add(new JLabel("Supplier:"));
        add(supplierDropdown);
        add(new JLabel("Color:"));
        add(colorField);
        add(new JLabel("Size:"));
        add(sizeField);

        JButton submitBtn = new JButton("Add Product");
        submitBtn.addActionListener(this::handleSubmit);
        add(new JLabel()); // spacer
        add(submitBtn);

        setVisible(true);
    }

    private String[] getCollectionIds(String collectionName) {
        List<String> ids = new ArrayList<>();
        MongoCollection<Document> collection = database.getCollection(collectionName);
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                ids.add(doc.getString("_id"));
            }
        }
        return ids.toArray(new String[0]);
    }

    private void handleSubmit(ActionEvent e) {
        try {
            Document product = new Document();
            product.append("_id", idField.getText().trim());
            product.append("name", nameField.getText().trim());
            product.append("description", descriptionField.getText().trim());
            product.append("price", Integer.parseInt(priceField.getText().trim()));
            product.append("stock", Integer.parseInt(stockField.getText().trim()));
            product.append("category_id", categoryDropdown.getSelectedItem());
            product.append("supplier_id", supplierDropdown.getSelectedItem());
            

            Document attributes = new Document();
            attributes.append("color", colorField.getText().trim());
            attributes.append("size", sizeField.getText().trim());
            product.append("attributes", attributes);
            
            product.append("is_deleted", false);
            
            MongoCollection<Document> products = database.getCollection("Products");
            products.insertOne(product);

            JOptionPane.showMessageDialog(this, "Product added successfully!");
            this.dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Insertion Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
