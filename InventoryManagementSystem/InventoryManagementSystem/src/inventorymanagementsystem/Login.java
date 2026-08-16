
package inventorymanagementsystem;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Login extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    private MongoCollection<Document> usersCollection;

    public Login() {
        MongoClient mongoClient = MongoConfig.createClient();
        MongoDatabase database = mongoClient.getDatabase(System.getenv().getOrDefault("MONGO_DATABASE", "EcommerceInventoryManagment"));
        usersCollection = database.getCollection("Users");

        // Layout settings
        getContentPane().setBackground(Color.decode("#E6A1AC"));
        setLayout(null);
        
        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icons/start1.png"));
        JLabel image = new JLabel(i1);
        image.setBounds(0, 20, 600, 500);
        add(image);

        JLabel heading = new JLabel("Inventory Management System");
        heading.setBounds(500, 135, 600, 60);
        heading.setFont(new Font("Viner Hand ITC", Font.BOLD, 32));
        heading.setForeground(Color.decode("#493728"));
        add(heading);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(570, 220, 100, 30);
        userLabel.setFont(new Font("Viner Hand ITC", Font.BOLD, 16));
        userLabel.setForeground(Color.decode("#493728"));
        add(userLabel);

        usernameField = new JTextField();
        usernameField.setBounds(700, 220, 200, 30);
        add(usernameField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(570, 270, 100, 30);
        passLabel.setFont(new Font("Viner Hand ITC", Font.BOLD, 16));
        passLabel.setForeground(Color.decode("#493728"));
        add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(700, 270, 200, 30);
        add(passwordField);

        loginButton = new JButton("Login");
        loginButton.setBounds(650, 330, 200, 30);
        loginButton.setBackground(Color.decode("#493728"));
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(new MyHandler());
        add(loginButton);
    }

    class MyHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            Document user = usersCollection.find(
                    Filters.and(
                            Filters.eq("username", username),
                            Filters.eq("password", password)
                    )
            ).first();

            if (user != null) {
                JOptionPane.showMessageDialog(Login.this, "Login successful!");

                // Open MenuPage window
                setVisible(false);
                new MenuPage();
                

                // Close login window
                dispose();
            } else {
                JOptionPane.showMessageDialog(Login.this, "Invalid username or password");
            }
        }
    }

   
    public static void main(String[] args) {
        Login frame = new Login();
        frame.setTitle("Inventory Management System");
        frame.setSize(1050, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}