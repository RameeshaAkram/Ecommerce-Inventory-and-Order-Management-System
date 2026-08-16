
package inventorymanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class ShippingForm extends JFrame {
    private JTextField shipmentIdField, shippingDateField, deliveryDateField;
    private JComboBox<String> orderIdBox, customerIdBox, statusBox;
    private JButton submitButton;
    private MongoDatabase database;

    public ShippingForm(MongoDatabase database) {
        this.database = database;

        setTitle("Add New Shipment");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(7, 2, 5, 5));

        shipmentIdField = new JTextField();
        shippingDateField = new JTextField("2025-03-01");
        deliveryDateField = new JTextField("2025-03-05");

        orderIdBox = new JComboBox<>();
        customerIdBox = new JComboBox<>();
        statusBox = new JComboBox<>(new String[]{"Pending", "Shipped", "In Transit", "Delivered", "Cancelled"});

        submitButton = new JButton("Submit Shipment");

        // Populate dropdowns
        populateComboBox(orderIdBox, "Orders", "_id");
        populateComboBox(customerIdBox, "Customers", "_id");

        // Add components
        add(new JLabel("Shipment ID:"));
        add(shipmentIdField);
        add(new JLabel("Order ID:"));
        add(orderIdBox);
        add(new JLabel("Customer ID:"));
        add(customerIdBox);
        add(new JLabel("Shipping Date (YYYY-MM-DD):"));
        add(shippingDateField);
        add(new JLabel("Expected Delivery (YYYY-MM-DD):"));
        add(deliveryDateField);
        add(new JLabel("Status:"));
        add(statusBox);
        add(new JLabel(""));
        add(submitButton);

        // Action
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String shipmentId = shipmentIdField.getText().trim();
                String orderId = (String) orderIdBox.getSelectedItem();
                String customerId = (String) customerIdBox.getSelectedItem();
                String shippingDateStr = shippingDateField.getText().trim();
                String deliveryDateStr = deliveryDateField.getText().trim();
                String status = (String) statusBox.getSelectedItem();

                if (shipmentId.isEmpty()) {
                    JOptionPane.showMessageDialog(ShippingForm.this, "Shipment ID cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                MongoCollection<Document> shipments = database.getCollection("Shipping");
                if (shipments.find(Filters.eq("_id", shipmentId)).first() != null) {
                    JOptionPane.showMessageDialog(ShippingForm.this, "Shipment ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    Document shipment = new Document("_id", shipmentId)
                            .append("order_id", orderId)
                            .append("customer_id", customerId)
                            .append("shipping_date", java.sql.Date.valueOf(LocalDate.parse(shippingDateStr)))
                            .append("expected_delivery", java.sql.Date.valueOf(LocalDate.parse(deliveryDateStr)))
                            .append("status", status);

                    shipments.insertOne(shipment);
                    JOptionPane.showMessageDialog(ShippingForm.this, "Shipment added successfully!");
                    dispose();
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(ShippingForm.this, "Invalid date format! Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setVisible(true);
    }

    private void populateComboBox(JComboBox<String> comboBox, String collectionName, String fieldName) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        for (Document doc : collection.find()) {
            comboBox.addItem(doc.getString(fieldName));
        }
    }
}
