package com.example.zooapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class FoodApplication extends Application {
    private TextField Name, Quantity, FoodId;
    private TextArea DisplayArea;
    private JTable resultTable;


    // JDBC connection parameters
    final String url = Common.url;
    final String user = Common.user;
    final String password = Common.password;;

    @Override
    public void start(Stage primaryStage) {

        //Create a scene  and place it in the stage
        primaryStage.setScene(new Scene(createContent(primaryStage)));
        primaryStage.show();

    }

    public Parent createContent(Stage primaryStage) {
        //Change page title
        primaryStage.setTitle("Food Page");

        //Create pane and set its properties
        GridPane mainpane = new GridPane();
        mainpane.setPadding(new Insets(10, 10, 10, 10));
        mainpane.setHgap(10);
        mainpane.setVgap(10);
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(10, 10, 10, 10));
        pane.setHgap(10);
        pane.setVgap(10);

        // Create menu
        MenuItem animalMenuItem = new MenuItem("Animal");
        MenuItem foodMenuItem = new MenuItem("Food");
        MenuItem feedingMenuItem = new MenuItem("Feeding");

        // Create event handlers for menu items
        animalMenuItem.setOnAction(event -> {
            primaryStage.setScene(new Scene(new HelloApplication().createContent(primaryStage)));
        });
        foodMenuItem.setOnAction(event -> {
            primaryStage.setScene(new Scene(new FoodApplication().createContent(primaryStage)));
        });
        feedingMenuItem.setOnAction(event -> {
            primaryStage.setScene(new Scene(new FeedingApplication().createContent(primaryStage)));
        });

        // Create menus and add menu items to them
        Menu animalMenu = new Menu("Animal", null, animalMenuItem);
        Menu foodMenu = new Menu("Food", null, foodMenuItem);
        Menu feedingMenu = new Menu("Feeding", null, feedingMenuItem);

        // Create menu bar and add menus to it
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(animalMenu, foodMenu, feedingMenu);


        //Food Information
        Name = new TextField();
        Quantity = new TextField();
        FoodId = new TextField();

        pane.add(new Label("Name: "), 0, 0);
        pane.add(Name, 1, 0);
        pane.add(new Label("Quantity: "), 0, 1);
        pane.add(Quantity, 1, 1);

        pane.add(new Label("Food ID: "), 2, 0);
        pane.add(FoodId, 3, 0);

        // Create update button
        Button updateButton = new Button("    Update Food    ");
        pane.add(updateButton, 4, 0);
        updateButton.setOnAction(event ->
        {
            updateClicked();
        });

        // Create delete button
        Button deleteButton = new Button("    Delete Food    ");
        pane.add(deleteButton, 4, 1);
        deleteButton.setOnAction(event ->
        {
            deleteClicked();
        });

        Button createButton = new Button("     Create Food     ");
        pane.add(createButton, 3, 8);
        createButton.setOnAction(event ->
        {
            createClicked();
        });

        Button displayButton = new Button("Display All Food");
        pane.add(displayButton, 4, 8);
        displayButton.setOnAction(event ->
        {
            displayClicked();
        });

        //Creating the display area to display all errors
        DisplayArea = new TextArea();
        DisplayArea.setEditable(false);
        mainpane.add(DisplayArea, 0, 3);
        DisplayArea.setPrefHeight(150);


        // Create main pane
        Label boldLabel1 = new Label("   Food Information:");
        boldLabel1.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        mainpane.add(menuBar, 0, 0);
        mainpane.add(boldLabel1, 0, 1);
        mainpane.add(pane, 0, 2);

        return mainpane;
    }

    //Method for Display button
    public void displayClicked() {
        // Create a new JFrame for the resultTable
        JFrame resultFrame = new JFrame("Query Result");
        resultFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create the JTable
        resultTable = new JTable();

        // Add the JTable to a JScrollPane for scrollability
        JScrollPane scrollPane = new JScrollPane(resultTable);
        resultFrame.add(scrollPane);

        // Set size and make the frame visible
        resultFrame.setSize(840, 400);  // Adjust the size as needed
        resultFrame.setVisible(true);

        try {
            System.out.println("> Start Program ...");

            Class.forName("oracle.jdbc.driver.OracleDriver");

            System.out.println("> Driver Loaded successfully.");

            Connection connection = DriverManager.getConnection(url, user, password);

            System.out.println("Database connected successfully.");

            // Create a statement
            Statement statement = connection.createStatement();

            // Execute the SQL query
            ResultSet resultSet = statement.executeQuery(
                    "SELECT * from food ORDER BY food_id");

            // Create a DefaultTableModel to hold the query result
            DefaultTableModel tableModel = new DefaultTableModel();

            // Add column headers to the table model
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                tableModel.addColumn(metaData.getColumnName(columnIndex));
            }

            // Add rows to the table model
            while (resultSet.next()) {
                Object[] rowData = new Object[columnCount];
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    rowData[columnIndex - 1] = resultSet.getObject(columnIndex);
                }
                tableModel.addRow(rowData);
            }

            // Set the table model for the JTable
            resultTable.setModel(tableModel);

            // Close resources
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method for Create button
    public void createClicked() {
        DisplayArea.clear();
        String name = Name.getText();
        String quantity = Quantity.getText();

        // Check if any text fields are empty
        if (Name.getText().isEmpty() || Quantity.getText().isEmpty() )
        {
            DisplayArea.appendText("Error: All fields must be filled.\n");
            return; // Stop processing if any field is empty
        }

        // Check if quantity is a valid integer
        boolean testQty = isValidQty(quantity);

        if(!testQty)
        {
            return;
        }

        int quantityValue = Integer.parseInt(quantity);

        //SQL query to add record
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            connection.setAutoCommit(false); // Start transaction

            // Insert into Food table
            String playerQuery = "INSERT INTO FOOD VALUES(Food_Seq.NEXTVAL, ?, ?)";
            try (PreparedStatement foodStatement = connection.prepareStatement(playerQuery)) {
                foodStatement.setString(1, name);
                foodStatement.setInt(2, quantityValue);
                foodStatement.executeUpdate();
            }

            // Commit the transaction
            connection.commit();

            DisplayArea.appendText("New food record created successfully.");
        } catch (SQLException e) {
            DisplayArea.appendText("Error creating new record: " + e.getMessage());
        }
    }

    // Method for delete button
    private void deleteClicked()
    {
        DisplayArea.clear();

        // Validate ID
        boolean testId = isValidId(FoodId.getText());

        if(testId)
        {
            String animalId = FoodId.getText();
            int foodIdValue = Integer.parseInt(animalId);
            DisplayArea.appendText("Selected food id for deletion is: " + foodIdValue + "\n");

            // Check if the food ID exists in the player table
            try (Connection connection = DriverManager.getConnection(url, user, password))
            {
                connection.setAutoCommit(false);
                String selectQuery = "SELECT COUNT(*) FROM food WHERE food_id = ?";
                PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                selectStatement.setInt(1, foodIdValue);
                ResultSet resultSet = selectStatement.executeQuery();
                resultSet.next();
                int rowCount = resultSet.getInt(1);

                if (rowCount == 0)
                {
                    DisplayArea.appendText("Food Id does not exists.\n");
                }
                else
                {
                    DisplayArea.appendText("Food Id exists. \n");
                }

            } catch (SQLException e) {
                e.printStackTrace(); // Handle the exception appropriately
                DisplayArea.appendText("SQL Exception: " + e.getMessage() + "\n");
            }

            //Delete the food record
            try (Connection connection = DriverManager.getConnection(url, user, password))
            {
                String updateQuery = "DELETE FROM food WHERE food_id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                    preparedStatement.setInt(1, foodIdValue);

                    int rowsUpdated = preparedStatement.executeUpdate();

                    if (rowsUpdated > 0) {
                        DisplayArea.appendText("Food deletion successful.\n");
                    } else {
                        DisplayArea.appendText("Food deletion unsuccessful.\n");
                    }
                }

                // Commit the transaction
                connection.commit();

            } catch (SQLException e) {
                e.printStackTrace(); // Handle the exception appropriately
            }

        }

    }

    // Method for Update button
    private void updateClicked()
    {
        DisplayArea.clear();

        // Validate ID
        boolean testId = isValidId(FoodId.getText());

        if(testId)
        {
            String foodId = FoodId.getText();
            int foodIdValue = Integer.parseInt(foodId);
            DisplayArea.appendText("Selected food id for update is: " + foodIdValue + "\n");

            // Check if the food ID exists in the player table
            try (Connection connection = DriverManager.getConnection(url, user, password))
            {
                connection.setAutoCommit(false);
                String selectQuery = "SELECT COUNT(*) FROM food WHERE food_id = ?";
                PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                selectStatement.setInt(1, foodIdValue);
                ResultSet resultSet = selectStatement.executeQuery();
                resultSet.next();
                int rowCount = resultSet.getInt(1);

                if (rowCount == 0)
                {
                    DisplayArea.appendText("Food Id does not exists.\n");
                }
                else
                {
                    DisplayArea.appendText("Food Id exists. \n");
                }

            } catch (SQLException e) {
                e.printStackTrace(); // Handle the exception appropriately
                DisplayArea.appendText("SQL Exception: " + e.getMessage() + "\n");
            }

            //Update query for Name
            String name = Name.getText();
            if (!name.isEmpty())
            {
                try (Connection connection = DriverManager.getConnection(url, user, password))
                {
                    String updateQuery = "UPDATE food SET food_name = ? WHERE food_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                        preparedStatement.setString(1, name);
                        preparedStatement.setInt(2, foodIdValue);

                        int rowsUpdated = preparedStatement.executeUpdate();

                        if (rowsUpdated > 0) {
                            DisplayArea.appendText("Food name update successful.\n");
                        } else {
                            DisplayArea.appendText("Food name update unsuccessful.\n");
                        }
                    }

                    // Commit the transaction
                    connection.commit();

                } catch (SQLException e) {
                    e.printStackTrace(); // Handle the exception appropriately
                }
            }

            //Update query for Quantity
            String quantity = Quantity.getText();
            if (!quantity.isEmpty())
            {
                // Check if quantity is a valid integer
                boolean testQty = isValidQty(quantity);

                if(!testQty)
                {
                    return;
                }

                int quantityValue = Integer.parseInt(quantity);

                try (Connection connection = DriverManager.getConnection(url, user, password))
                {
                    String updateQuery = "UPDATE food SET food_stock = ? WHERE food_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                        preparedStatement.setInt(1, quantityValue);
                        preparedStatement.setInt(2, foodIdValue);

                        int rowsUpdated = preparedStatement.executeUpdate();

                        if (rowsUpdated > 0) {
                            DisplayArea.appendText("Food stock update successful.\n");
                        } else {
                            DisplayArea.appendText("Food stock update unsuccessful.\n");
                        }
                    }

                    // Commit the transaction
                    connection.commit();

                } catch (SQLException e) {
                    e.printStackTrace(); // Handle the exception appropriately
                }
            }

        }
    }

    // Method to check if a string is in the YYYY-MM-DD format
    private boolean isValidDateFormat(String date) {
        String dateFormatRegex = "\\d{4}-\\d{2}-\\d{2}";
        return date.matches(dateFormatRegex);
    }

    // Method to check if a number ID is valid
    private boolean isValidId(String id) {
        // Check if id field is empty
        if (id.isEmpty())
        {
            DisplayArea.appendText("Please enter an ID number.\n");
            return false;
        }

        try {
            // Check if id is a valid integer
            int IdValue = Integer.parseInt(id);
        }
        catch (NumberFormatException e)
        {
            // Handle the case where id is not a valid integer
            DisplayArea.appendText("ID must be valid integer.\n");
            return false;
        }

        return true;
    }

    // Method to check if quantity is valid int
    public boolean isValidQty(String quantity)
    {
        // Check if quantity field is empty
        if (quantity.isEmpty())
        {
            DisplayArea.appendText("Please enter a food quantity.\n");
            return false;
        }

        try {
            // Check if quantity is a valid integer
            int quantityValue = Integer.parseInt(quantity);
        }
        catch (NumberFormatException e)
        {
            // Handle the case where id is not a valid integer
            DisplayArea.appendText("Quantity must be valid integer.\n");
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        HelloApplication.launch();
    }
}