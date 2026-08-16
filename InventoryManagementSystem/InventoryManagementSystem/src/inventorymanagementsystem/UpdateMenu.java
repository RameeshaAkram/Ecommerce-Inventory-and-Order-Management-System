package inventorymanagementsystem;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.Updates;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import org.bson.Document;

public class UpdateMenu extends JMenu {

    private final MongoDatabase database;

    public UpdateMenu(MongoDatabase database) {
        super("Update");
        this.database = database;

        setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 16));
        setOpaque(true);
        setBackground(java.awt.Color.decode("#493728"));
        setForeground(java.awt.Color.WHITE);

        addUpdateMenuItem("Categories");
        addUpdateMenuItem("Cart");
        addUpdateMenuItem("Products");
        addUpdateMenuItem("Customers");
        addUpdateMenuItem("Orders");
        addUpdateMenuItem("Payments");
        addUpdateMenuItem("Shipping");
        
    }

    private void addUpdateMenuItem(String collectionName) {
        JMenuItem item = new JMenuItem("Update " + collectionName);
        item.addActionListener(e -> openUpdateDialog(collectionName));
        add(item);
    }

    private void openUpdateDialog(String collectionName) {
        String orderId = JOptionPane.showInputDialog(
            null,
            "Enter Document ID to update in " + collectionName + ":",
            "Update " + collectionName,
            JOptionPane.PLAIN_MESSAGE
        );

        if (orderId != null && !orderId.trim().isEmpty()) {
            switch (collectionName) {
                case "Categories":
                    updateCategories(orderId);
                    break;
                case "Cart":
                    updateCart(orderId);
                    break;
                case "Products":
                    updateProducts(orderId);
                    break;
                case "Customers":
                    updateCustomers(orderId);
                    break;
                case "Orders":
                    updateOrders(orderId);
                    break;
                case "Payments":
                    updatePayments(orderId);
                    break;
              
                case "Shipping":
                    updateShipping(orderId);
                    break;
                
            }
        }
    }

    private void updateCategories(String categoryId) {
        String newDescription = JOptionPane.showInputDialog(
            null,
            "Enter new description for Category ID '" + categoryId + "':",
            "Update Category",
            JOptionPane.PLAIN_MESSAGE
        );

        if (newDescription != null && !newDescription.trim().isEmpty()) {
            try {
                // Update the category description in MongoDB
                database.getCollection("Categories")
                        .updateOne(
                            new org.bson.Document("_id", categoryId),
                            new org.bson.Document("$set", new org.bson.Document("description", newDescription))
                        );

                JOptionPane.showMessageDialog(
                    null,
                    "Description for Category ID '" + categoryId + "' updated successfully.",
                    "Update Successful",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    null,
                    "Error updating category: " + ex.getMessage(),
                    "Update Failed",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } else {
            JOptionPane.showMessageDialog(
                null,
                "No description provided. Update cancelled.",
                "Update Cancelled",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }


    private void updateCart(String cartId) {
    MongoCollection<Document> cartCollection = database.getCollection("Cart");
    MongoCollection<Document> productCollection = database.getCollection("Products");

    Document existingCart = cartCollection.find(new Document("_id", cartId)).first();
    if (existingCart == null) {
        JOptionPane.showMessageDialog(null, "Cart ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Load product prices
    HashMap<String, Integer> productPriceMap = new HashMap<>();
    for (Document product : productCollection.find()) {
        productPriceMap.put(product.getString("_id"), product.getInteger("total_price", 0));
    }

    // Ask how many products to add
    String countStr = JOptionPane.showInputDialog(null, "How many new products to add?", "Add Products", JOptionPane.PLAIN_MESSAGE);
    if (countStr == null || countStr.trim().isEmpty()) return;

    int count;
    try {
        count = Integer.parseInt(countStr.trim());
        if (count <= 0) throw new NumberFormatException();
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Invalid number of products.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    ArrayList<JComboBox<String>> productBoxes = new ArrayList<>();
    ArrayList<JSpinner> quantitySpinners = new ArrayList<>();

    for (int i = 0; i < count; i++) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> productBox = new JComboBox<>(productPriceMap.keySet().toArray(new String[0]));
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        row.add(new JLabel("Product " + (i + 1) + ":"));
        row.add(productBox);
        row.add(new JLabel("Qty:"));
        row.add(quantitySpinner);

        productBoxes.add(productBox);
        quantitySpinners.add(quantitySpinner);
        panel.add(row);
    }

    JScrollPane scrollPane = new JScrollPane(panel);
    scrollPane.setPreferredSize(new Dimension(500, Math.min(300, 80 * count)));

    int result = JOptionPane.showConfirmDialog(null, scrollPane, "Add Items to Cart", JOptionPane.OK_CANCEL_OPTION);
    if (result != JOptionPane.OK_OPTION) return;

    // Build new items list
    ArrayList<Document> newItems = new ArrayList<>();
    int additionalPrice = 0;

    for (int i = 0; i < count; i++) {
        String productId = (String) productBoxes.get(i).getSelectedItem();
        int quantity = (Integer) quantitySpinners.get(i).getValue();
        int price = productPriceMap.getOrDefault(productId, 0) * quantity;

        newItems.add(new Document("product_id", productId).append("quantity", quantity));
        additionalPrice += price;
    }

    // Merge with existing cart items
    @SuppressWarnings("unchecked")
    ArrayList<Document> existingItems = (ArrayList<Document>) existingCart.get("items");
    existingItems.addAll(newItems);

    int updatedTotal = existingCart.getInteger("total_price", 0) + additionalPrice;

    // Update cart
    cartCollection.updateOne(
        new Document("_id", cartId),
        new Document("$set", new Document("items", existingItems).append("total_price", updatedTotal))
    );

    JOptionPane.showMessageDialog(null, "Cart updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
}

    


    private void updateProducts(String productId) {
        MongoCollection<Document> productCollection = database.getCollection("Products");

        // Retrieve the product by ID
        Document product = productCollection.find(new Document("_id", productId)).first();
        if (product == null) {
            JOptionPane.showMessageDialog(null, "Product with ID \"" + productId + "\" not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create fields pre-filled with existing data
        JTextField descField = new JTextField(product.getString("description"));
        JTextField priceField = new JTextField(String.valueOf(product.getInteger("price", 0)));
        JTextField stockField = new JTextField(String.valueOf(product.getInteger("stock", 0)));

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Stock:"));
        panel.add(stockField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Update Product", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        // Validate and parse input
        String desc = descField.getText().trim();
        int price, stock;
        try {
            price = Integer.parseInt(priceField.getText().trim());
            stock = Integer.parseInt(stockField.getText().trim());
            if (price < 0 || stock < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Price and Stock must be non-negative integers.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update document in MongoDB
        Document updateDoc = new Document("$set", new Document("description", desc)
            .append("price", price)
            .append("stock", stock));

        productCollection.updateOne(new Document("_id", productId), updateDoc);

        JOptionPane.showMessageDialog(null, "Product updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }


    
    private void updateCustomers(String customerId) {
    MongoCollection<Document> customerCollection = database.getCollection("Customers");
    MongoCollection<Document> orderCollection = database.getCollection("Orders");

    // Fetch customer
    Document customer = customerCollection.find(new Document("_id", customerId)).first();
    if (customer == null) {
        JOptionPane.showMessageDialog(null, "Customer with ID \"" + customerId + "\" not found.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Current order history
    List<String> currentOrders = customer.getList("order_history", String.class, new ArrayList<>());

    // Fetch all orders
    List<String> allOrderIds = orderCollection.find()
    .map(order -> {
    Object id = order.get("_id");
    return (id != null) ? id.toString() : "";
    })
    .into(new ArrayList<>());


    // Filter only those orders not already in order_history
    List<String> availableOrders = new ArrayList<>();
    for (String orderId : allOrderIds) {
        if (!currentOrders.contains(orderId)) {
            availableOrders.add(orderId);
        }
    }

    if (availableOrders.isEmpty()) {
        JOptionPane.showMessageDialog(null, "No new orders available to add for this customer.", "Info", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    // Checkbox panel for available orders
    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(new JLabel("Select orders to add to order history:"));

    JCheckBox[] checkBoxes = new JCheckBox[availableOrders.size()];
    for (int i = 0; i < availableOrders.size(); i++) {
        checkBoxes[i] = new JCheckBox(availableOrders.get(i));
        panel.add(checkBoxes[i]);
    }

    int result = JOptionPane.showConfirmDialog(null, panel, "Update Order History", JOptionPane.OK_CANCEL_OPTION);
    if (result != JOptionPane.OK_OPTION) return;

    // Collect selected orders
    List<String> selectedOrders = new ArrayList<>();
    for (JCheckBox checkBox : checkBoxes) {
        if (checkBox.isSelected()) {
            selectedOrders.add(checkBox.getText());
        }
    }

    if (selectedOrders.isEmpty()) {
        JOptionPane.showMessageDialog(null, "No orders selected to add.", "Info", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    // Merge existing and new orders
    currentOrders.addAll(selectedOrders);

    // Update in database
    Document updateDoc = new Document("$set", new Document("order_history", currentOrders));
    customerCollection.updateOne(new Document("_id", customerId), updateDoc);

    JOptionPane.showMessageDialog(null, "Order history updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
}

public void updateOrders(String orderId) {
    MongoCollection<Document> ordersCollection = database.getCollection("Orders");

    // Find the document by ID
    Document order = ordersCollection.find(eq("_id", orderId)).first();
    if (order == null) {
        JOptionPane.showMessageDialog(null, "Order ID not found: " + orderId);
        return;
    }

    // Status options based on your schema
    String[] statusOptions = {"Pending", "Processed", "Shipped", "Delivered", "Cancelled"};
    JComboBox<String> statusDropdown = new JComboBox<>(statusOptions);
    statusDropdown.setSelectedItem(order.getString("status")); // pre-select current status

    // Create panel for input
    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(new JLabel("Update Order Status for ID: " + orderId));
    panel.add(new JLabel("Select New Status:"));
    panel.add(statusDropdown);

    int result = JOptionPane.showConfirmDialog(null, panel, "Update Order Status",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
        String newStatus = (String) statusDropdown.getSelectedItem();

        // Update status in DB
        ordersCollection.updateOne(eq("_id", orderId), Updates.set("status", newStatus));
        JOptionPane.showMessageDialog(null, "Order status updated successfully.");
    }
}


    public void updatePayments(String paymentId) {
    MongoCollection<Document> paymentCollection = database.getCollection("Payments");

    // Find the document by ID
    Document payment = paymentCollection.find(eq("_id", paymentId)).first();
    if (payment == null) {
        JOptionPane.showMessageDialog(null, "Payment ID not found: " + paymentId);
        return;
    }

    // Create the status dropdown
    String[] statusOptions = {"Paid", "Pending", "Cancelled"};
    JComboBox<String> statusDropdown = new JComboBox<>(statusOptions);
    statusDropdown.setSelectedItem(payment.getString("status")); // pre-select current status

    // Create panel for input
    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(new JLabel("Update Payment Status for ID: " + paymentId));
    panel.add(new JLabel("Select New Status:"));
    panel.add(statusDropdown);

    int result = JOptionPane.showConfirmDialog(null, panel, "Update Payment Status",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
        String newStatus = (String) statusDropdown.getSelectedItem();

        // Update status in DB
        paymentCollection.updateOne(eq("_id", paymentId), Updates.set("status", newStatus));
        JOptionPane.showMessageDialog(null, "Payment status updated successfully.");
    }
}



    public void updateShipping(String shipmentId) {
        MongoCollection<Document> shippingCollection = database.getCollection("Shipping");

        // Find the document by ID
        Document shipment = shippingCollection.find(eq("_id", shipmentId)).first();
        if (shipment == null) {
            JOptionPane.showMessageDialog(null, "Shipment ID not found: " + shipmentId);
            return;
        }

        // Create the status dropdown
        String[] statusOptions = {"Pending", "Shipped", "In Transit", "Delivered", "Cancelled"};
        JComboBox<String> statusDropdown = new JComboBox<>(statusOptions);
        statusDropdown.setSelectedItem(shipment.getString("status")); // pre-select current status

        // Create panel for input
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Update Shipping Status for ID: " + shipmentId));
        panel.add(new JLabel("Select New Status:"));
        panel.add(statusDropdown);

        int result = JOptionPane.showConfirmDialog(null, panel, "Update Shipping Status",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newStatus = (String) statusDropdown.getSelectedItem();

            // Update status in DB
            shippingCollection.updateOne(eq("_id", shipmentId), Updates.set("status", newStatus));
            JOptionPane.showMessageDialog(null, "Shipping status updated successfully.");
        }
    }


}
