package inventorymanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class CartForm extends JFrame {
    private JTextField cartIdField;
    private JComboBox<String> customerComboBox;
    private JSpinner itemCountSpinner;
    private JPanel itemsPanel;
    private JButton submitButton;
    private MongoDatabase database;

    private ArrayList<JComboBox<String>> productFields = new ArrayList<>();
    private ArrayList<JSpinner> quantityFields = new ArrayList<>();
    private ArrayList<JTextField> priceFields = new ArrayList<>();
    private HashMap<String, Integer> productPriceMap = new HashMap<>();

    public CartForm(MongoDatabase database) {
        this.database = database;
        setTitle("Create Cart");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Cart ID
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Cart ID:"), gbc);
        cartIdField = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(cartIdField, gbc);
        row++;

        // Customer
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Customer:"), gbc);
        customerComboBox = new JComboBox<>();
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

        // Submit
        submitButton = new JButton("Submit Cart");
        gbc.gridwidth = 1;
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(submitButton, gbc);

        generateButton.addActionListener(e -> generateItemFields((int) itemCountSpinner.getValue()));
        submitButton.addActionListener(e -> submitCart());

        add(formPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);

        loadProductPrices();

        // Check eligible customers BEFORE showing form
        if (!loadEligibleCustomers()) {
            JOptionPane.showMessageDialog(this, "All customers already have a cart. Cannot create a new one.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose(); // Close form immediately
            return;
        }

        setVisible(true);
    }

    private boolean loadEligibleCustomers() {
        MongoCollection<Document> customers = database.getCollection("Customers");
        MongoCollection<Document> carts = database.getCollection("Cart");

        ArrayList<String> allCustomerIds = new ArrayList<>();
        for (Document doc : customers.find()) {
            allCustomerIds.add(doc.getString("_id"));
        }

        ArrayList<String> customersWithCarts = new ArrayList<>();
        for (Document cart : carts.find()) {
            String custId = cart.getString("customer_id");
            if (custId != null) {
                customersWithCarts.add(custId);
            }
        }

        ArrayList<String> eligibleCustomers = new ArrayList<>();
        for (String id : allCustomerIds) {
            if (!customersWithCarts.contains(id)) {
                eligibleCustomers.add(id);
            }
        }

        if (eligibleCustomers.isEmpty()) {
            return false; // No eligible customers
        }

        for (String id : eligibleCustomers) {
            customerComboBox.addItem(id);
        }
        return true;
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

            ActionListener recalculate = e -> {
                String selected = (String) productComboBox.getSelectedItem();
                int unitPrice = productPriceMap.getOrDefault(selected, 0);
                int quantity = (Integer) quantitySpinner.getValue();
                priceField.setText(String.valueOf(unitPrice * quantity));
            };

            productComboBox.addActionListener(recalculate);
            quantitySpinner.addChangeListener(e -> recalculate.actionPerformed(null));

            // Initial price update
            recalculate.actionPerformed(null);

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

    private void submitCart() {
        String cartId = cartIdField.getText().trim();
        if (cartId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart ID cannot be empty.");
            return;
        }

        MongoCollection<Document> carts = database.getCollection("Cart");
        if (carts.find(new Document("_id", cartId)).first() != null) {
            JOptionPane.showMessageDialog(this, "Cart ID already exists. Please use a unique ID.");
            return;
        }

        String customerId = (String) customerComboBox.getSelectedItem();

        ArrayList<Document> items = new ArrayList<>();
        int totalPrice = 0;

        for (int i = 0; i < productFields.size(); i++) {
            String productId = (String) productFields.get(i).getSelectedItem();
            int quantity = (Integer) quantityFields.get(i).getValue();
            int price = productPriceMap.getOrDefault(productId, 0) * quantity;
            totalPrice += price;

            items.add(new Document("product_id", productId).append("quantity", quantity));
        }

        Document cart = new Document("_id", cartId)
                .append("customer_id", customerId)
                .append("items", items)
                .append("total_price", totalPrice);

        carts.insertOne(cart);
        JOptionPane.showMessageDialog(this, "Cart successfully created!");
        dispose();
    }
}
