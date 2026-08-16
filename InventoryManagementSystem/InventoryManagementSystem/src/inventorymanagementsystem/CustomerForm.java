/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventorymanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;
import java.util.ArrayList;
import java.util.List;

public class CustomerForm extends JFrame {
    private JTextField idField, nameField, emailField, phoneField;
    private JTextField streetField, cityField, stateField, zipField, countryField;
    private JButton submitButton;
    private MongoDatabase database;
    private JPanel orderCheckboxPanel;
    private List<JCheckBox> orderCheckBoxes;

    public CustomerForm(MongoDatabase database) {
        this.database = database;
        setTitle("Add New Customer");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // center the form
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(11, 2, 5, 5));

        // Fields
        idField = new JTextField();
        nameField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();

        streetField = new JTextField();
        cityField = new JTextField();
        stateField = new JTextField();
        zipField = new JTextField();
        countryField = new JTextField();

        submitButton = new JButton("Submit Customer");

        formPanel.add(new JLabel("Customer ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Street:"));
        formPanel.add(streetField);
        formPanel.add(new JLabel("City:"));
        formPanel.add(cityField);
        formPanel.add(new JLabel("State:"));
        formPanel.add(stateField);
        formPanel.add(new JLabel("ZIP Code:"));
        formPanel.add(zipField);
        formPanel.add(new JLabel("Country:"));
        formPanel.add(countryField);

        add(formPanel, BorderLayout.NORTH);

        // Order checkboxes
        orderCheckboxPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        orderCheckBoxes = new ArrayList<>();

        MongoCollection<Document> ordersCollection = database.getCollection("Orders");
        for (Document doc : ordersCollection.find()) {
            String orderId = doc.getString("_id");
            JCheckBox checkBox = new JCheckBox(orderId);
            orderCheckBoxes.add(checkBox);
            orderCheckboxPanel.add(checkBox);
        }

        JScrollPane scrollPane = new JScrollPane(orderCheckboxPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Select Order History"));
        scrollPane.setPreferredSize(new Dimension(480, 150));
        add(scrollPane, BorderLayout.CENTER);

        // Submit button
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action listener
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String customerId = idField.getText().trim();
                if (customerId.isEmpty()) {
                    JOptionPane.showMessageDialog(CustomerForm.this, "Customer ID cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                MongoCollection<Document> customers = database.getCollection("Customers");

                Bson filter = Filters.eq("_id", customerId);
                Document existingCustomer = customers.find(filter).first();

                if (existingCustomer != null) {
                    JOptionPane.showMessageDialog(CustomerForm.this, "Customer ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Collect order history from checkboxes
                List<String> orderHistory = new ArrayList<>();
                for (JCheckBox checkBox : orderCheckBoxes) {
                    if (checkBox.isSelected()) {
                        orderHistory.add(checkBox.getText());
                    }
                }

                Document address = new Document("street", streetField.getText().trim())
                        .append("city", cityField.getText().trim())
                        .append("state", stateField.getText().trim())
                        .append("zip", zipField.getText().trim())
                        .append("country", countryField.getText().trim());

                Document customer = new Document("_id", customerId)
                        .append("name", nameField.getText().trim())
                        .append("email", emailField.getText().trim())
                        .append("phone", phoneField.getText().trim())
                        .append("order_history", orderHistory)
                        .append("address", address);

                customers.insertOne(customer);
                JOptionPane.showMessageDialog(CustomerForm.this, "Customer added successfully!");
                dispose();
            }
        });

        setVisible(true);
    }
}
