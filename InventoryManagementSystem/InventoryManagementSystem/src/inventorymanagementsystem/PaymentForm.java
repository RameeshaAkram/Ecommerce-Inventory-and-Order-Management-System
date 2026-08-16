package inventorymanagementsystem;

import javax.swing.*;
import java.awt.GridLayout;
import java.util.*;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class PaymentForm extends JFrame {
    private JTextField paymentIdField;
    private JComboBox<String> orderIdComboBox;
    private JTextField customerIdField, amountField;
    private JComboBox<String> paymentMethodComboBox, statusComboBox;
    private JButton submitButton;
    private MongoDatabase database;

    public PaymentForm(MongoDatabase database) {
        this.database = database;
        setTitle("Add Payment");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(8, 2, 5, 5));

        MongoCollection<Document> orders = database.getCollection("Orders");
        MongoCollection<Document> payments = database.getCollection("Payments");

        // Get order IDs without a payment
        List<String> paidOrders = payments.find().map(doc -> doc.getString("order_id")).into(new ArrayList<>());
        List<String> availableOrderIds = orders.find()
                .map(doc -> doc.getString("_id"))
                .into(new ArrayList<>());
        availableOrderIds.removeAll(paidOrders);

        if (availableOrderIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No orders available for payment!", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        // Initialize components
        paymentIdField = new JTextField();
        orderIdComboBox = new JComboBox<>(availableOrderIds.toArray(new String[0]));
        customerIdField = new JTextField();
        customerIdField.setEditable(false);
        amountField = new JTextField();
        amountField.setEditable(false);

        paymentMethodComboBox = new JComboBox<>(new String[]{"Credit Card", "Debit Card", "PayPal", "Bank Transfer"});
        statusComboBox = new JComboBox<>(new String[]{"Paid", "Pending", "Cancelled"});
        submitButton = new JButton("Submit Payment");

        // Layout
        add(new JLabel("Payment ID:"));
        add(paymentIdField);
        add(new JLabel("Order ID:"));
        add(orderIdComboBox);
        add(new JLabel("Customer ID:"));
        add(customerIdField);
        add(new JLabel("Amount:"));
        add(amountField);
        add(new JLabel("Payment Method:"));
        add(paymentMethodComboBox);
        add(new JLabel("Status:"));
        add(statusComboBox);
        add(new JLabel(""));
        add(submitButton);

        // Load order data when an order is selected
        orderIdComboBox.addActionListener(e -> {
            String selectedOrderId = (String) orderIdComboBox.getSelectedItem();
            Document order = orders.find(Filters.eq("_id", selectedOrderId)).first();
            if (order != null) {
                customerIdField.setText(order.getString("customer_id"));
                Object priceObj = order.get("total_price");
                amountField.setText(priceObj != null ? priceObj.toString() : "");
            }
        });

        // Trigger initial load
        orderIdComboBox.setSelectedIndex(0);

        // Submit logic
        submitButton.addActionListener(e -> {
            String paymentId = paymentIdField.getText().trim();
            String orderId = (String) orderIdComboBox.getSelectedItem();
            String customerId = customerIdField.getText().trim();
            String amountStr = amountField.getText().trim();
            String paymentMethod = (String) paymentMethodComboBox.getSelectedItem();
            String status = (String) statusComboBox.getSelectedItem();
            Date transactionDate = new Date(); // current date

            if (paymentId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Payment ID cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (payments.find(Filters.eq("_id", paymentId)).first() != null) {
                JOptionPane.showMessageDialog(this, "Payment ID already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Document payment = new Document("_id", paymentId)
                    .append("order_id", orderId)
                    .append("customer_id", customerId)
                    .append("amount", amount)
                    .append("payment_method", paymentMethod)
                    .append("status", status)
                    .append("transaction_date", transactionDate);

            payments.insertOne(payment);
            JOptionPane.showMessageDialog(this, "Payment recorded successfully!");
            dispose();
        });

        setVisible(true);
    }
}
