/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventorymanagementsystem;
import com.mongodb.client.MongoDatabase;

import javax.swing.*;

public class CreateMenu extends JMenu {

    private final MongoDatabase database;

    public CreateMenu(MongoDatabase database) {
        super("Create");
        this.database = database;

        setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 16));
        setOpaque(true);
        setBackground(java.awt.Color.decode("#493728"));
        setForeground(java.awt.Color.WHITE);

        String[] collections = {
            "Products", "Orders", "Customers", "Shipping",
            "Payments", "Reviews", "Cart", "Categories", "Suppliers"
        };

        for (String col : collections) {
            JMenuItem item = new JMenuItem("Add to " + col);
            item.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 14));
            item.addActionListener(e -> openCreateForm(col));
            add(item);
        }
    }

    private void openCreateForm(String collectionName) {
        switch (collectionName) {
            case "Products":
                new ProductForm(database);
                break;
            case "Orders":
                new OrderForm(database);
                break;
            case "Customers":
                new CustomerForm(database);
                break;
            case "Shipping":
                new ShippingForm(database);
                break;
            case "Payments":
                new PaymentForm(database);
                break;
            case "Reviews":
                new ReviewForm(database);
                break;
            case "Cart":
                new CartForm(database);
                break;
            case "Categories":
                new CategoryForm(database);
                break;
            case "Suppliers":
                new SupplierForm(database);
                break;
            default:
                JOptionPane.showMessageDialog(null, "Unknown collection: " + collectionName);
        }
    }
}
