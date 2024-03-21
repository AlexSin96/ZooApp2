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

public class UserApplication extends Application {
    private TextField EmpName, EmpTitle, EmpId;
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
        primaryStage.setTitle("User Page");

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
        MenuItem userMenuItem = new MenuItem("User");

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
        userMenuItem.setOnAction(event -> {
            primaryStage.setScene(new Scene(new UserApplication().createContent(primaryStage)));
        });

        // Create menus and add menu items to them
        Menu animalMenu = new Menu("Animal", null, animalMenuItem);
        Menu foodMenu = new Menu("Food", null, foodMenuItem);
        Menu feedingMenu = new Menu("Feeding", null, feedingMenuItem);
        Menu userMenu = new Menu("User", null, userMenuItem);

        // Create menu bar and add menus to it
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(animalMenu, foodMenu, feedingMenu, userMenu);


        //Food Information
        EmpName = new TextField();
        EmpTitle = new TextField();
        EmpId = new TextField();

        pane.add(new Label("Employee Name: "), 0, 0);
        pane.add(EmpName, 1, 0);
        pane.add(new Label("Title: "), 0, 1);
        pane.add(EmpTitle, 1, 1);

        pane.add(new Label("Employee ID: "), 2, 0);
        pane.add(EmpId, 3, 0);

        // Create update button
        Button updateButton = new Button("    Update User    ");
        pane.add(updateButton, 4, 0);
        updateButton.setOnAction(event ->
        {
            updateClicked();
        });

        // Create delete button
        Button deleteButton = new Button("    Delete User    ");
        pane.add(deleteButton, 4, 1);
        deleteButton.setOnAction(event ->
        {
            deleteClicked();
        });

        Button createButton = new Button("     Create User     ");
        pane.add(createButton, 3, 8);
        createButton.setOnAction(event ->
        {
            createClicked();
        });

        Button displayButton = new Button("Display All Employee");
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
        Label boldLabel1 = new Label("   User Information:");
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
                    "SELECT * from user ORDER BY user_id");

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
        String name = EmpName.getText();
        String empTitle = EmpTitle.getText();

        // Check if any text fields are empty
        if (EmpName.getText().isEmpty() || EmpTitle.getText().isEmpty() )
        {
            DisplayArea.appendText("Error: All fields must be filled.\n");
            return; // Stop processing if any field is empty
        }

        // Check if quantity is a valid integer
//        try
//        {
//            int IdValue = Integer.parseInt(quantity);
//        }
//        catch (NumberFormatException e)
//        {
//            // Handle the case where quantity is not a valid integer
//            DisplayArea.appendText("Quantity must be valid integer.\n");
//        }
//
//        int quantityValue = Integer.parseInt(quantity);

        //SQL query to add record
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            connection.setAutoCommit(false); // Start transaction

//            // Insert into Food table
//            String playerQuery = "INSERT INTO FOOD VALUES(Food_Seq.NEXTVAL, ?, ?)";
//            try (PreparedStatement foodStatement = connection.prepareStatement(playerQuery)) {
//                foodStatement.setString(1, name);
//                foodStatement.setInt(2, quantityValue);
//                foodStatement.executeUpdate();
//            }

            // Commit the transaction
            connection.commit();

            DisplayArea.appendText("New user record created successfully.");
        } catch (SQLException e) {
            DisplayArea.appendText("Error creating new record: " + e.getMessage());
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

    private void deleteClicked()
    {
        DisplayArea.clear();

        // Validate ID
        boolean testId = isValidId(EmpId.getText());

        if(testId)
        {
            String userId = EmpId.getText();
            int userIdValue = Integer.parseInt(userId);
            DisplayArea.appendText("Selected user id for deletion is: " + userIdValue + "\n");

            // Check if the food ID exists in the player table
            try (Connection connection = DriverManager.getConnection(url, user, password))
            {
                connection.setAutoCommit(false);
                String selectQuery = "SELECT COUNT(*) FROM user WHERE user_id = ?";
                PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                selectStatement.setInt(1, userIdValue);
                ResultSet resultSet = selectStatement.executeQuery();
                resultSet.next();
                int rowCount = resultSet.getInt(1);

                if (rowCount == 0)
                {
                    DisplayArea.appendText("User Id does not exists.\n");
                }
                else
                {
                    DisplayArea.appendText("User Id exists. \n");
                }

            } catch (SQLException e) {
                e.printStackTrace(); // Handle the exception appropriately
                DisplayArea.appendText("SQL Exception: " + e.getMessage() + "\n");
            }

            //Delete the food record
            try (Connection connection = DriverManager.getConnection(url, user, password))
            {
                String updateQuery = "DELETE FROM user WHERE user_id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                    preparedStatement.setInt(1, userIdValue);

                    int rowsUpdated = preparedStatement.executeUpdate();

                    if (rowsUpdated > 0) {
                        DisplayArea.appendText("User deletion successful.\n");
                    } else {
                        DisplayArea.appendText("User deletion unsuccessful.\n");
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
        boolean testId = isValidId(EmpId.getText());

        if(testId)
        {
            String foodId = EmpId.getText();
            int foodIdValue = Integer.parseInt(foodId);
            DisplayArea.appendText("Selected User id for update is: " + foodIdValue + "\n");

            // Check if the food ID exists in the player table
            try (Connection connection = DriverManager.getConnection(url, user, password))
            {
                connection.setAutoCommit(false);
                String selectQuery = "SELECT COUNT(*) FROM user WHERE user_id = ?";
                PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                selectStatement.setInt(1, foodIdValue);
                ResultSet resultSet = selectStatement.executeQuery();
                resultSet.next();
                int rowCount = resultSet.getInt(1);

                if (rowCount == 0)
                {
                    DisplayArea.appendText("User Id does not exists.\n");
                }
                else
                {
                    DisplayArea.appendText("User Id exists. \n");
                }

            } catch (SQLException e) {
                e.printStackTrace(); // Handle the exception appropriately
                DisplayArea.appendText("SQL Exception: " + e.getMessage() + "\n");
            }

            //Update query for Name
            String name = EmpName.getText();
            if (!name.isEmpty())
            {
                try (Connection connection = DriverManager.getConnection(url, user, password))
                {
                    String updateQuery = "UPDATE food SET user_name = ? WHERE user_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                        preparedStatement.setString(1, name);
                        preparedStatement.setInt(2, foodIdValue);

                        int rowsUpdated = preparedStatement.executeUpdate();

                        if (rowsUpdated > 0) {
                            DisplayArea.appendText("User name update successful.\n");
                        } else {
                            DisplayArea.appendText("User name update unsuccessful.\n");
                        }
                    }

                    // Commit the transaction
                    connection.commit();

                } catch (SQLException e) {
                    e.printStackTrace(); // Handle the exception appropriately
                }
            }

            //Update query for Quantity
            String empTitle = EmpTitle.getText();
            if (!empTitle.isEmpty())
            {

                try (Connection connection = DriverManager.getConnection(url, user, password))
                {
                    String updateQuery = "UPDATE user SET user_type = ? WHERE user_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                        preparedStatement.setString(1, empTitle);
                        preparedStatement.setString(2, empTitle);

                        int rowsUpdated = preparedStatement.executeUpdate();

                        if (rowsUpdated > 0) {
                            DisplayArea.appendText("User update successful.\n");
                        } else {
                            DisplayArea.appendText("User update unsuccessful.\n");
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

    public static void main(String[] args) {
        HelloApplication.launch();
    }
}