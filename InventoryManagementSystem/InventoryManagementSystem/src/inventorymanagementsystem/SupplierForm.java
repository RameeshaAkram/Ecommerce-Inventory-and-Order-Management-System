package inventorymanagementsystem;

import com.mongodb.client.*;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SupplierForm extends JFrame {
    private JTextField idField, nameField, contactInfoField, emailField;
    private JPanel checkboxPanel;
    private List<JCheckBox> productCheckboxes;
    private MongoDatabase database;

    public SupplierForm(MongoDatabase database) {
        this.database = database;
        setTitle("Add New Supplier");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(null);

        productCheckboxes = new ArrayList<>();

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        idField = new JTextField(15);
        nameField = new JTextField(15);
        contactInfoField = new JTextField(15);
        emailField = new JTextField(15);
        checkboxPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        // Row 0 - Supplier ID
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Supplier ID:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(idField, gbc);

        // Row 1 - Name
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(nameField, gbc);

        // Row 2 - Contact Info
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Contact Info:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(contactInfoField, gbc);

        // Row 3 - Email
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(emailField, gbc);

        // Row 4 - Products Label
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        mainPanel.add(new JLabel("Products Supplied:"), gbc);

        // Row 5 - Checkbox Panel in Scroll Pane
        List<String> availableProducts = getAvailableProductIds();
        if (availableProducts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All products are already assigned to suppliers.");
            dispose();
            return;
        }

        for (String productId : availableProducts) {
            JCheckBox checkBox = new JCheckBox(productId);
            productCheckboxes.add(checkBox);
            checkboxPanel.add(checkBox);
        }

        JScrollPane scrollPane = new JScrollPane(checkboxPanel);
        scrollPane.setPreferredSize(new Dimension(300, 150));
        gbc.gridy = 5;
        mainPanel.add(scrollPane, gbc);

        // Row 6 - Submit Button
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> submitSupplier());

        gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(submitButton, gbc);

        add(mainPanel);
        setVisible(true);
    }

    private List<String> getAvailableProductIds() {
        MongoCollection<Document> productCol = database.getCollection("Products");
        MongoCollection<Document> supplierCol = database.getCollection("Suppliers");

        Set<String> allProductIds = new HashSet<>();
        for (Document doc : productCol.find()) {
            allProductIds.add(doc.getString("_id"));
        }

        Set<String> suppliedProducts = new HashSet<>();
        for (Document doc : supplierCol.find()) {
            List<String> supplied = (List<String>) doc.get("products_supplied");
            if (supplied != null) {
                suppliedProducts.addAll(supplied);
            }
        }

        return allProductIds.stream()
                .filter(p -> !suppliedProducts.contains(p))
                .sorted()
                .collect(Collectors.toList());
    }

    private void submitSupplier() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String contactInfo = contactInfoField.getText().trim();
        String email = emailField.getText().trim();

        if (id.isEmpty() || name.isEmpty() || contactInfo.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        List<String> selectedProducts = productCheckboxes.stream()
                .filter(JCheckBox::isSelected)
                .map(AbstractButton::getText)
                .collect(Collectors.toList());

        if (selectedProducts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one product.");
            return;
        }

        Document supplierDoc = new Document("_id", id)
                .append("name", name)
                .append("contact_info", contactInfo)
                .append("email", email)
                .append("products_supplied", selectedProducts);

        try {
            MongoCollection<Document> supplierCol = database.getCollection("Suppliers");
            supplierCol.insertOne(supplierDoc);
            JOptionPane.showMessageDialog(this, "Supplier added successfully!");
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding supplier: " + e.getMessage());
        }
    }
}
