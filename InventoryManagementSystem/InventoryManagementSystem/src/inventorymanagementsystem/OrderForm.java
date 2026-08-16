/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventorymanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class OrderForm extends JFrame {
    private JTextField orderIdField;
    private JComboBox<String> customerComboBox;
    private JSpinner itemCountSpinner;
    private JPanel itemsPanel;
    private JComboBox<String> statusComboBox;
    private JButton submitButton;
    private MongoDatabase database;

    private ArrayList<JComboBox<String>> productFields = new ArrayList<>();
    private ArrayList<JSpinner> quantityFields = new ArrayList<>();
    private ArrayList<JTextField> priceFields = new ArrayList<>();
    private HashMap<String, Integer> productPriceMap = new HashMap<>();

    public OrderForm(MongoDatabase database) {
        this.database = database;
        setTitle("Create Order");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Order ID
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Order ID:"), gbc);
        orderIdField = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(orderIdField, gbc);
        row++;

        // Customer
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Customer:"), gbc);
        customerComboBox = new JComboBox<>();
        loadCustomers();
        gbc.gridx = 1;
        formPanel.add(customerComboBox, gbc);
        row++;

        // Item Count Spinner
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("How many items?"), gbc);
        itemCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        gbc.gridx = 1;
        formPanel.add(itemCountSpinner, gbc);
        row++;

        // Generate Items Button
        JButton generateButton = new JButton("Generate Item Fields");
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(generateButton, gbc);
        row++;

        // Items Panel
        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setPreferredSize(new Dimension(450, 200));
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(scrollPane, gbc);
        row++;

        // Order Status
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Order Status:"), gbc);
        statusComboBox = new JComboBox<>(new String[] { "Pending", "Shipped", "Delivered", "Cancelled" });
        gbc.gridx = 1;
        formPanel.add(statusComboBox, gbc);
        row++;

        // Submit
        submitButton = new JButton("Submit Order");
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(submitButton, gbc);

        // Generate Item Fields
        generateButton.addActionListener(e -> generateItemFields((int)itemCountSpinner.getValue()));
        submitButton.addActionListener(e -> submitOrder());

        add(formPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null); // Center on screen
        setVisible(true);

        loadProductPrices(); // For dropdown + pricing
    }

    private void loadCustomers() {
        for (Document customer : database.getCollection("Customers").find()) {
            customerComboBox.addItem(customer.getString("_id"));
        }
    }

    private void loadProductPrices() {
        for (Document product : database.getCollection("Products").find()) {
            String productId = product.getString("_id");
            Integer price = product.getInteger("price");
            productPriceMap.put(productId, price);
        }
    }

    private void generateItemFields(int count) {
        itemsPanel.removeAll();
        productFields.clear();
        quantityFields.clear();
        priceFields.clear();

        for (int i = 0; i < count; i++) {
            JPanel itemRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JComboBox<String> productComboBox = new JComboBox<>(productPriceMap.keySet().toArray(new String[0]));
            JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
            JTextField priceField = new JTextField(6);
            priceField.setEditable(false);

            int finalI = i;
            ActionListener recalculate = e -> {
                String selected = (String) productComboBox.getSelectedItem();
                int unitPrice = productPriceMap.getOrDefault(selected, 0);
                int quantity = (Integer) quantitySpinner.getValue();
                priceField.setText(String.valueOf(unitPrice * quantity));
            };

            productComboBox.addActionListener(recalculate);
            quantitySpinner.addChangeListener(e -> recalculate.actionPerformed(null));

            itemRow.add(new JLabel("Product:"));
            itemRow.add(productComboBox);
            itemRow.add(new JLabel("Qty:"));
            itemRow.add(quantitySpinner);
            itemRow.add(new JLabel("Price:"));
            itemRow.add(priceField);

            itemsPanel.add(itemRow);
            productFields.add(productComboBox);
            quantityFields.add(quantitySpinner);
            priceFields.add(priceField);
        }

        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private void submitOrder() {
        String orderId = orderIdField.getText().trim();
        if (orderId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Order ID cannot be empty.");
            return;
        }

        // Check for duplicate ID
        MongoCollection<Document> orders = database.getCollection("Orders");
        if (orders.find(new Document("_id", orderId)).first() != null) {
            JOptionPane.showMessageDialog(this, "Order ID already exists. Please use a unique ID.");
            return;
        }

        String customerId = (String) customerComboBox.getSelectedItem();
        String status = (String) statusComboBox.getSelectedItem();

        ArrayList<Document> items = new ArrayList<>();
        int totalPrice = 0;

        for (int i = 0; i < productFields.size(); i++) {
            String productId = (String) productFields.get(i).getSelectedItem();
            int quantity = (Integer) quantityFields.get(i).getValue();
            int price = productPriceMap.getOrDefault(productId, 0) * quantity;
            totalPrice += price;

            Document item = new Document("product_id", productId)
                    .append("quantity", quantity)
                    .append("price", price);
            items.add(item);
        }

        Document order = new Document("_id", orderId)
                .append("customer_id", customerId)
                .append("items", items)
                .append("total_price", totalPrice)
                .append("status", status)
                .append("order_date", new Date());

        orders.insertOne(order);
        JOptionPane.showMessageDialog(this, "Order successfully created!");
        dispose();
    }
}

