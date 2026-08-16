package inventorymanagementsystem;

import com.mongodb.client.*;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;

public class MenuPage extends JFrame {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private JTable table;
    private JScrollPane scrollPane;

    public MenuPage() {
        mongoClient = MongoConfig.createClient();
        database = mongoClient.getDatabase(System.getenv().getOrDefault("MONGO_DATABASE", "EcommerceInventoryManagment"));

        setTitle("Inventory Management System");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.decode("#E6A1AC"));
        setLayout(null);

        JLabel heading = new JLabel("Inventory Management System");
        heading.setBounds(250, 10, 600, 60);
        heading.setFont(new Font("Viner Hand ITC", Font.BOLD, 32));
        heading.setForeground(Color.decode("#493728"));
        add(heading);

        // Table setup
        table = new JTable();
        scrollPane = new JScrollPane(table);
        scrollPane.setBounds(50, 100, 900, 400);
        add(scrollPane);

        JButton summaryButton = new JButton("Show Summary Statistics");
        summaryButton.setBounds(230, 530, 200, 30);
        summaryButton.setBackground(Color.decode("#493728"));
        summaryButton.setForeground(Color.WHITE);
        summaryButton.addActionListener(e -> new SummaryStatisticsWindow(database));

        JButton chartsButton = new JButton("Display Charts");
        chartsButton.setBounds(570, 530, 200, 30);
        chartsButton.setBackground(Color.decode("#493728"));
        chartsButton.setForeground(Color.WHITE);
        chartsButton.addActionListener(e -> new ChartsWindow(database));

        add(summaryButton);
        add(chartsButton);



        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setLayout(new GridLayout(1, 5));

        
        menuBar.add(new CreateMenu(database));
        menuBar.add(new ReadMenu(database, table));
        menuBar.add(new UpdateMenu(database));
        menuBar.add(new DeleteMenu(database));
        menuBar.add(new QueriesMenu(database, table));

        setJMenuBar(menuBar);
        setVisible(true);
    }
}
