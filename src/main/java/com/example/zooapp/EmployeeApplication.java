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

public class EmployeeApplication extends Application {
    private TextField Password, Name, Address, Birthday, Phone, UserId;
    private ComboBox<String> typeComboBox;
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
        LoginApplication login = new LoginApplication();

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
        MenuItem employeeMenuItem = new MenuItem("Users");
        MenuItem analyticsMenuItem = new MenuItem("Analytics");

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
        employeeMenuItem.setOnAction(event -> {
            primaryStage.setScene(new Scene(new EmployeeApplication().createContent(primaryStage)));
        });
        analyticsMenuItem.setOnAction(event -> {
            primaryStage.setScene(new Scene(new AnalyticsApplication().createContent(primaryStage)));
        });

        // Create menus and add menu items to them
        Menu animalMenu = new Menu("Animal", null, animalMenuItem);
        Menu foodMenu = new Menu("Food", null, foodMenuItem);
        Menu feedingMenu = new Menu("Feeding", null, feedingMenuItem);
        Menu employeeMenu = new Menu("Users", null, employeeMenuItem);
        Menu analyticsMenu = new Menu("Analytics", null, analyticsMenuItem);

        // Create menu bar and add menus to it
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(animalMenu, foodMenu, feedingMenu, employeeMenu, analyticsMenu);

        //User Information
        Name = new TextField();
        Password = new TextField();
        Birthday = new TextField();
        Address = new TextField();
        Phone = new TextField();
        UserId = new TextField();
        typeComboBox = new ComboBox<>();

        typeComboBox.getItems().addAll("Manager", "Employee");
        typeComboBox.setValue("Manager"); // Set default selection to Manager

        pane.add(new Label("Name: "), 0, 0);
        pane.add(Name, 1, 0);
        pane.add(new Label("Password: "), 0, 1);
        pane.add(Password, 1, 1);
        pane.add(new Label("Birthday"), 0, 2);
        pane.add(Birthday, 1, 2);
        pane.add(new Label("Address"), 0, 3);
        pane.add(Address, 1, 3);
        pane.add(new Label("Phone"), 0, 4);
        pane.add(Phone, 1, 4);
        pane.add(new Label("Type: "), 0, 5);
        pane.add(typeComboBox, 1, 5);

        pane.add(new Label("User ID: "), 2, 0);
        pane.add(UserId, 3, 0);

        // Create update button
        Button updateButton = new Button("    Update User    ");
        pane.add(updateButton, 4, 0);

        //Shows update button when logged as Manager
        updateButton.setDisable((login.getUserType().equals("Manager") ? false : true));
        updateButton.setOnAction(event ->
        {
            updateClicked();
        });

        // Create delete button
        Button deleteButton = new Button("    Delete User    ");
        pane.add(deleteButton, 4, 1);

        //Shows delete Button when logged as manager
        deleteButton.setDisable((login.getUserType().equals("Manager") ? false : true));
        deleteButton.setOnAction(event ->
        {
            deleteClicked();
        });

        Button createButton = new Button("     Create User     ");
        pane.add(createButton, 3, 8);

        //Shows create button when logged as Manager
        createButton.setDisable((login.getUserType().equals("Manager") ? false : true));
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
        Label boldLabel1 = new Label("   User Information:                                                                                    " +"Logged as: " + login.getUserName());
        boldLabel1.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        mainpane.add(menuBar, 0, 0);
        mainpane.add(boldLabel1, 0, 1);
        mainpane.add(pane, 0, 2);

        return mainpane;
    }

    //Method for Display button
    public void displayClicked() {
        // Create a new JFrame for the resultTable
        JFrame resultFrame = new JFrame("Displaying all employees");
        resultFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create the JTable
        resultTable = new JTable();

        // Add the JTable to a JScrollPane for scrollability
        JScrollPane scrollPane = new JScrollPane(resultTable);
        resultFrame.add(scrollPane);

        // Set size and make the frame visible
        resultFrame.setSize(840, 400);  // Adjust the size as needed
        resultFrame.setVisible(true);
        resultFrame.setLocationRelativeTo(null); // Center the frame

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
                    "SELECT * from users ORDER BY user_id");

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
    public void createClicked()
    {
        DisplayArea.clear();

        // Check if any text fields are empty
        if (Name.getText().isEmpty() || Password.getText().isEmpty() ||
                Birthday.getText().isEmpty() || Address.getText().isEmpty() || Phone.getText().isEmpty()) {

            DisplayArea.appendText("Error: All fields must be filled.\n");
            return; // Stop processing if any field is empty
        }

        // Check if birthdate is valid format
        if (!isValidDateFormat(Birthday.getText())) {
            // Handle the case where birthday is not valid format
            DisplayArea.appendText("Error: Birthday must be in the YYYY-MM-DD format.\n");
            return; // Stop processing if any field is empty
        }


        String name = Name.getText();
        String pass = Password.getText();
        String birthday = Birthday.getText();
        String address = Address.getText();
        String phone = Phone.getText();
        String type = typeComboBox.getValue();


        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            connection.setAutoCommit(false); // Start transaction

            // Insert into Animal table
            String playerQuery = "INSERT INTO users VALUES(Worker_Seq.NEXTVAL, ?, ?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'))";
            try (PreparedStatement animalStatement = connection.prepareStatement(playerQuery)) {
                animalStatement.setString(1, pass);
                animalStatement.setString(2, type);
                animalStatement.setString(3, name);
                animalStatement.setString(4, address);
                animalStatement.setString(5, phone);
                animalStatement.setString(6, birthday);
                animalStatement.executeUpdate();
            }

            // Commit the transaction
            connection.commit();

            DisplayArea.appendText("New user record created successfully.");
        }
        catch (SQLException e)
        {
            DisplayArea.appendText("Error creating new record: " + e.getMessage());
        }
    }

    private void deleteClicked()
    {
        DisplayArea.clear();

        // Validate ID
        boolean testId = isValidId(UserId.getText());

        if(testId)
        {
            String userId = UserId.getText();
            int userIdValue = Integer.parseInt(userId);
            DisplayArea.appendText("Selected user id for deletion is: " + userIdValue + "\n");

            // Check if the food ID exists in the player table
            try (Connection connection = DriverManager.getConnection(url, user, password))
            {
                connection.setAutoCommit(false);
                String selectQuery = "SELECT COUNT(*) FROM users WHERE user_id = ?";
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
                String updateQuery = "DELETE FROM users WHERE user_id = ?";
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
        boolean testId = isValidId(UserId.getText());

        if(testId)
        {
            String userId = UserId.getText();
            int userIdValue = Integer.parseInt(userId);
            DisplayArea.appendText("Selected User id for update is: " + userIdValue + "\n");

            // Check if the user ID exists in the users table
            try (Connection connection = DriverManager.getConnection(url, user, password))
            {
                connection.setAutoCommit(false);
                String selectQuery = "SELECT COUNT(*) FROM users WHERE user_id = ?";
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

            //Update query for Password
            String pass = Password.getText();
            if (!pass.isEmpty())
            {
                try (Connection connection = DriverManager.getConnection(url, user, password))
                {
                    String updateQuery = "UPDATE users SET password_hash = ? WHERE user_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                        preparedStatement.setString(1, pass);
                        preparedStatement.setInt(2, userIdValue);

                        int rowsUpdated = preparedStatement.executeUpdate();

                        if (rowsUpdated > 0) {
                            DisplayArea.appendText("User password update successful.\n");
                        } else {
                            DisplayArea.appendText("User password update unsuccessful.\n");
                        }
                    }

                    // Commit the transaction
                    connection.commit();

                } catch (SQLException e) {
                    e.printStackTrace(); // Handle the exception appropriately
                }
            }

            //Update query for Type
            String type = typeComboBox.getValue();

            //Get old values for type
            String oldType = "";

            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                // SQL query to retrieve user_type
                String query = "SELECT user_type FROM users WHERE user_id = ?";

                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    // Set the feedingIdValue as a parameter in the query
                    statement.setInt(1, userIdValue);

                    // Execute the query and retrieve the results
                    ResultSet resultSet = statement.executeQuery();

                    // Process the result set
                    if (resultSet.next()) {
                        oldType = resultSet.getString("user_type");
                    } else {
                        System.out.println("No record found for User ID: " + userIdValue);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (!type.equals(oldType))
            {
                try (Connection connection = DriverManager.getConnection(url, user, password))
                {
                    String updateQuery = "UPDATE users SET user_type = ? WHERE user_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                        preparedStatement.setString(1, type);
                        preparedStatement.setInt(2, userIdValue);

                        int rowsUpdated = preparedStatement.executeUpdate();

                        if (rowsUpdated > 0) {
                            DisplayArea.appendText("User type update successful.\n");
                        } else {
                            DisplayArea.appendText("User type update unsuccessful.\n");
                        }
                    }

                    // Commit the transaction
                    connection.commit();

                } catch (SQLException e) {
                    e.printStackTrace(); // Handle the exception appropriately
                }
            }

            //Update query for Name
            String name = Name.getText();
            if (!name.isEmpty())
            {
                try (Connection connection = DriverManager.getConnection(url, user, password))
                {
                    String updateQuery = "UPDATE users SET user_name = ? WHERE user_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                        preparedStatement.setString(1, name);
                        preparedStatement.setInt(2, userIdValue);

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

            //Update query for Address
            String address = Address.getText();
            if (!address.isEmpty())
            {
                try (Connection connection = DriverManager.getConnection(url, user, password))
                {
                    String updateQuery = "UPDATE users SET user_address = ? WHERE user_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                        preparedStatement.setString(1, address);
                        preparedStatement.setInt(2, userIdValue);

                        int rowsUpdated = preparedStatement.executeUpdate();

                        if (rowsUpdated > 0) {
                            DisplayArea.appendText("User address update successful.\n");
                        } else {
                            DisplayArea.appendText("User address update unsuccessful.\n");
                        }
                    }

                    // Commit the transaction
                    connection.commit();

                } catch (SQLException e) {
                    e.printStackTrace(); // Handle the exception appropriately
                }
            }

            //Update query for Phone
            String phone = Phone.getText();
            if (!phone.isEmpty())
            {
                try (Connection connection = DriverManager.getConnection(url, user, password))
                {
                    String updateQuery = "UPDATE users SET user_phone = ? WHERE user_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                        preparedStatement.setString(1, phone);
                        preparedStatement.setInt(2, userIdValue);

                        int rowsUpdated = preparedStatement.executeUpdate();

                        if (rowsUpdated > 0) {
                            DisplayArea.appendText("User phone update successful.\n");
                        } else {
                            DisplayArea.appendText("User phone update unsuccessful.\n");
                        }
                    }

                    // Commit the transaction
                    connection.commit();

                } catch (SQLException e) {
                    e.printStackTrace(); // Handle the exception appropriately
                }
            }

            //Update query for Birthdate
            String birthdate = Birthday.getText();
            if (!birthdate.isEmpty())
            {
                // Check if date is in the YYYY-MM-DD format
                if (!isValidDateFormat(birthdate))
                {
                    DisplayArea.appendText("Error: Birthdate must be in the YYYY-MM-DD format.\n");
                    return; // Stop processing if date format is invalid
                }

                try (Connection connection = DriverManager.getConnection(url, user, password))
                {
                    String updateQuery = "UPDATE users SET user_birthdate = TO_DATE(?, 'YYYY-MM-DD') WHERE user_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                        preparedStatement.setString(1, birthdate);
                        preparedStatement.setInt(2, userIdValue);

                        int rowsUpdated = preparedStatement.executeUpdate();

                        if (rowsUpdated > 0) {
                            DisplayArea.appendText("User birthdate update successful.\n");
                        } else {
                            DisplayArea.appendText("User birthdate update unsuccessful.\n");
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

    public static void main(String[] args) {
        HelloApplication.launch();
    }
}