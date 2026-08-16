package inventorymanagementsystem;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;

public class CategoryForm extends JFrame {
    private JTextField idField, nameField;
    private JTextArea descriptionArea;
    private MongoDatabase database;

    public CategoryForm(MongoDatabase database) {
        this.database = database;

        setTitle("Add New Category");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel idLabel = new JLabel("Category ID:");
        idField = new JTextField(15);

        JLabel nameLabel = new JLabel("Name:");
        nameField = new JTextField(15);

        JLabel descriptionLabel = new JLabel("Description:");
        descriptionArea = new JTextArea(4, 15);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> submitCategory());

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(idLabel, gbc);
        gbc.gridx = 1;
        panel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(nameLabel, gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(descriptionLabel, gbc);
        gbc.gridx = 1;
        panel.add(scrollPane, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(submitButton, gbc);

        add(panel);
        setVisible(true);
    }

    private void submitCategory() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (id.isEmpty() || name.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        MongoCollection<Document> categoryCol = database.getCollection("Categories");

        if (categoryCol.find(new Document("_id", id)).first() != null) {
            JOptionPane.showMessageDialog(this, "Category ID already exists. Please use a different ID.");
            return;
        }

        Document newCategory = new Document("_id", id)
                .append("name", name)
                .append("description", description);

        try {
            categoryCol.insertOne(newCategory);
            JOptionPane.showMessageDialog(this, "Category added successfully!");
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding category: " + e.getMessage());
        }
    }
}
