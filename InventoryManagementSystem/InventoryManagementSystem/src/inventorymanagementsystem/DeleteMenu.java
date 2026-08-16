/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventorymanagementsystem;

import com.mongodb.client.*;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DeleteMenu extends JMenu {
    private final MongoDatabase database;

    public DeleteMenu(MongoDatabase database) {
        super("Delete");
        this.database = database;

        setFont(new Font("Tahoma", Font.BOLD, 16));
        setOpaque(true);
        setBackground(Color.decode("#493728"));
        setForeground(Color.WHITE);

        // Add delete submenus for all relevant collections
        addDeleteSubmenu("Products");
        addDeleteSubmenu("Orders");
        addDeleteSubmenu("Customers");
        addDeleteSubmenu("Payments");
        addDeleteSubmenu("Shipping");
        addDeleteSubmenu("Cart");
        addDeleteSubmenu("Suppliers");
        addDeleteSubmenu("Categories");
        addDeleteSubmenu("Reviews");
    }

    private void addDeleteSubmenu(String collectionName) {
        JMenu submenu = new JMenu("Delete from " + collectionName);
        submenu.setFont(new Font("Tahoma", Font.PLAIN, 14));

        List<String> ids = fetchIdsFromCollection(collectionName);
        if (ids.isEmpty()) {
            JMenuItem emptyItem = new JMenuItem("No items to delete");
            emptyItem.setEnabled(false);
            submenu.add(emptyItem);
        } else {
            for (String id : ids) {
                JMenuItem idItem = new JMenuItem(id);
                idItem.addActionListener(e -> confirmAndDelete(collectionName, id));
                submenu.add(idItem);
            }
        }

        add(submenu);
    }

    private List<String> fetchIdsFromCollection(String collectionName) {
        List<String> ids = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            for (Document doc : collection.find()) {
                Object idObj = doc.get("_id");
                if (idObj != null) {
                    ids.add(idObj.toString());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Error accessing collection " + collectionName + ": " + e.getMessage());
        }
        return ids;
    }

    private void confirmAndDelete(String collectionName, String id) {
        int confirm = JOptionPane.showConfirmDialog(null,
            "Are you sure you want to delete the item with ID: " + id + " from " + collectionName + "?",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                MongoCollection<Document> collection = database.getCollection(collectionName);
                collection.deleteOne(new Document("_id", id));
                JOptionPane.showMessageDialog(null,
                    "Item deleted successfully from " + collectionName + ".");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Error deleting from " + collectionName + ": " + e.getMessage());
            }
        }
    }
}
