package inventorymanagementsystem;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Date;

public class ChartsWindow extends JFrame {

    public ChartsWindow(MongoDatabase database) {
        setTitle("Charts");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(3, 1));

        add(createPieChartPanel(database));
        add(createBarChartPanel(database));
        add(createLineChartPanel(database));

        setVisible(true);
    }

    private JPanel createPieChartPanel(MongoDatabase database) {
        DefaultPieDataset dataset = new DefaultPieDataset();
       

        AggregateIterable<Document> result = database.getCollection("Orders").aggregate(Arrays.asList(
                new Document("$group", new Document("_id", "$status").append("count", new Document("$sum", 1)))
        ));

        for (Document doc : result) {
            String status = doc.getString("_id");
            Number countNum = doc.get("count", Number.class);
            int count = countNum != null ? countNum.intValue() : 0;
            dataset.setValue(status, count);
        }

        JFreeChart chart = ChartFactory.createPieChart("Orders per Status", dataset, true, true, false);
        return new ChartPanel(chart);
    }

    private JPanel createBarChartPanel(MongoDatabase database) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        AggregateIterable<Document> result = database.getCollection("Products").aggregate(Arrays.asList(
                new Document("$project", new Document("name", 1).append("stock", 1))
        ));

        for (Document doc : result) {
            String name = doc.getString("name");
            Number stockNum = doc.get("stock", Number.class);
            int stock = stockNum != null ? stockNum.intValue() : 0;
            dataset.addValue(stock, "Stock", name);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Products by Stock",
                "Product",
                "Stock",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        return new ChartPanel(chart);
    }

    private JPanel createLineChartPanel(MongoDatabase database) {
        TimeSeries series = new TimeSeries("Total Sales");

        AggregateIterable<Document> result = database.getCollection("Orders").aggregate(Arrays.asList(
                new Document("$group", new Document("_id", new Document("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$order_date")))
                        .append("total_sales", new Document("$sum", "$total_price"))),
                new Document("$sort", new Document("_id", 1))
        ));

        for (Document doc : result) {
            String dateString = doc.getString("_id");
            Number totalSalesNumber = doc.get("total_sales", Number.class);
            double totalSales = totalSalesNumber != null ? totalSalesNumber.doubleValue() : 0.0;

            try {
                String[] parts = dateString.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                series.add(new Day(day, month, year), totalSales);
            } catch (Exception e) {
                System.err.println("Error parsing date: " + dateString);
            }
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Total Sales Over Time",
                "Date",
                "Sales",
                dataset,
                true,
                true,
                false
        );

        return new ChartPanel(chart);
    }
}

