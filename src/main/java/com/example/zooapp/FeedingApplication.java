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

public class FeedingApplication extends Application{
    private TextField Date, FeedingId, Quantity;
    private ComboBox<String> foodComboBox, userComboBox, animalComboBox;
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

    public Parent createContent(Stage primaryStage){
        LoginApplication login = new LoginApplication();

        //Change page title
        primaryStage.setTitle("Feeding Page");

        //Create pane and set its properties
        GridPane mainpane= new GridPane();
        mainpane.setPadding(new Insets(10,10,10,10));
        mainpane.setHgap(10);
        mainpane.setVgap(10);
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(10,10,10,10));
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


        //Feeding Information
        Date = new TextField();
        FeedingId = new TextField();
        userComboBox = new ComboBox<>();
        animalComboBox = new ComboBox<>();
        foodComboBox = new ComboBox<>();
        Quantity = new TextField();

        //Populate combo box
        populateFoodComboBox(foodComboBox);
        foodComboBox.getSelectionModel().selectFirst();
        populateUserComboBox(userComboBox);
        userComboBox.getSelectionModel().selectFirst();
        populateAnimalComboBox(animalComboBox);
        animalComboBox.getSelectionModel().selectFirst();

        pane.add(new Label("User: "), 0, 0);
        pane.add(userComboBox, 1, 0);
        pane.add(new Label("Animal: "), 0, 1);
        pane.add(animalComboBox, 1, 1);
        pane.add(new Label("Date"), 0, 2);
        pane.add(Date, 1, 2);
        pane.add(new Label("Food: "), 0, 3);
        pane.add(foodComboBox, 1, 3);
        pane.add(new Label("Food Quantity"), 0, 4);
        pane.add(Quantity, 1, 4);


        pane.add(new Label("Feeding ID: "), 2, 0);
        pane.add(FeedingId, 3, 0);

        // Create update button
        Button updateButton = new Button("    Update Feeding    ");
        pane.add(updateButton, 4, 0);
        updateButton.setOnAction(event ->
        {
            updateClicked();
        });

        // Create delete button
        Button deleteButton = new Button("    Delete Feeding    ");
        pane.add(deleteButton, 4, 1);

        //Shows delete Button when logged as Manager
        deleteButton.setDisable((login.getUserType().equals("Manager") ? false : true));
        deleteButton.setOnAction(event ->
        {
            deleteClicked();
        });

        Button createButton = new Button("     Create Feeding     ");
        pane.add(createButton, 3, 8);

        //Show create button when logged as Employee
        createButton.setDisable((login.getUserType().equals("Employee") ? false : true));
        createButton.setOnAction(event ->
        {
            createClicked();
        });

        Button displayButton = new Button("Display All Feedings");
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
        Label boldLabel1 = new Label("   Feeding Information:                                                                                                " +"Logged as: " + login.getUserName());
        boldLabel1.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        mainpane.add(menuBar, 0, 0);
        mainpane.add(boldLabel1, 0, 1);
        mainpane.add(pane, 0, 2);

        return mainpane;
    }

    //Method for Display button
    public void displayClicked()
    {
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

        try
        {
            System.out.println("> Start Program ...");

            Class.forName("oracle.jdbc.driver.OracleDriver");

            System.out.println("> Driver Loaded successfully.");

            Connection connection = DriverManager.getConnection(url, user, password);

            System.out.println("Database connected successfully.");

            // Create a statement
            Statement statement = connection.createStatement();

            // Execute the SQL query
            ResultSet resultSet = statement.executeQuery(
                    "SELECT a.feeding_id, a.user_id, b.user_name, a.animal_id, c.animal_name, a.feeding_date, a.food_id, d.food_name, a.food_quantity " +
                            "FROM feedingrecords a " +
                            "JOIN users b ON a.user_id = b.user_id " +
                            "JOIN animals c ON a.animal_id = c.animal_id " +
                            "JOIN food d ON a.food_id = d.food_id ORDER BY a.feeding_id"
            );


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
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Method for Create button
    public void createClicked()
    {
        DisplayArea.clear();

        // Check if any text fields are empty
        if (Date.getText().isEmpty() || Quantity.getText().isEmpty())
        {
            DisplayArea.appendText("Error: All fields must be filled.\n");
            return; // Stop processing if any field is empty
        }

        // Check if date is valid
        if (!isValidDateFormat(Date.getText())) {
            DisplayArea.appendText("Error: Date must be in the YYYY-MM-DD format.\n");
            return; // Stop processing if any field is empty
        }

        //Get values from the form
        String user1 = userComboBox.getValue();
        String animal = animalComboBox.getValue();
        String date = Date.getText();
        String food = foodComboBox.getValue();
        String quantity = Quantity.getText();

        int userId = ParseId(user1);
        int animalId = ParseId(animal);
        int foodId = ParseId(food);

        // Check if quantity is valid int
        boolean testQty = isValidQty(quantity);

        if(!testQty)
        {
            return;
        }

        int quantityValue = Integer.parseInt(quantity);

        //Check if stock is enough
        boolean checkStock = CheckStock(foodId, quantityValue);
        if (!checkStock) {
            DisplayArea.appendText("Error: Not enough food stock for this feeding.\n");
            return; // Stop processing
        }

        // Insert the feeding record into database
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            connection.setAutoCommit(false); // Start transaction

            // Insert into Animal table
            String playerQuery = "INSERT INTO FEEDINGRECORDS VALUES(Feeding_Seq.NEXTVAL, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?)";
            try (PreparedStatement feedingStatement = connection.prepareStatement(playerQuery)) {
                feedingStatement.setInt(1, userId);
                feedingStatement.setInt(2, animalId);
                feedingStatement.setString(3, date);
                feedingStatement.setInt(4, foodId);
                feedingStatement.setInt(5, quantityValue);
                feedingStatement.executeUpdate();
            }

            // Commit the transaction
            connection.commit();

            DisplayArea.appendText("New feeding record created successfully.");
        }
        catch (SQLException e)
        {
            DisplayArea.appendText("Error creating new record: " + e.getMessage());
        }

        // Update food table
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // SQL query to update food_quantity for a specific food_id
            String query = "UPDATE food SET food_stock = food_stock - ? WHERE food_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // Set the parameters in the query
                statement.setInt(1, quantityValue);
                statement.setInt(2, foodId);

                // Execute the update query
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    DisplayArea.appendText("\nFood stock updated successfully.");
                    connection.commit(); // Commit the transaction
                } else {
                    DisplayArea.appendText("\nError: Food stock was not updated.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method for delete button
    private void deleteClicked()
    {
        DisplayArea.clear();

        // Validate ID
        boolean testId = isValidId(FeedingId.getText());

        if(testId)
        {
            String feedingId = FeedingId.getText();
            int feedingIdValue = Integer.parseInt(feedingId);
            DisplayArea.appendText("Selected feeding id for deletion is: " + feedingIdValue + "\n");

            // Check if the animal ID exists in the player table
            try (Connection connection = DriverManager.getConnection(url, user, password))
            {
                connection.setAutoCommit(false);
                String selectQuery = "SELECT COUNT(*) FROM feedingrecords WHERE feeding_id = ?";
                PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                selectStatement.setInt(1, feedingIdValue);
                ResultSet resultSet = selectStatement.executeQuery();
                resultSet.next();
                int rowCount = resultSet.getInt(1);

                if (rowCount == 0)
                {
                    DisplayArea.appendText("Feeding Id does not exists.\n");
                }
                else
                {
                    DisplayArea.appendText("Feeding Id exists. \n");
                }

            } catch (SQLException e) {
                e.printStackTrace(); // Handle the exception appropriately
                DisplayArea.appendText("SQL Exception: " + e.getMessage() + "\n");
            }

            //Delete the animal record
            try (Connection connection = DriverManager.getConnection(url, user, password))
            {
                String updateQuery = "DELETE FROM feedingrecords WHERE feeding_id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                    preparedStatement.setString(1, feedingId);

                    int rowsUpdated = preparedStatement.executeUpdate();

                    if (rowsUpdated > 0) {
                        DisplayArea.appendText("Feeding record deletion successful.\n");
                    } else {
                        DisplayArea.appendText("Feeding record deletion unsuccessful.\n");
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
        boolean testId = isValidId(FeedingId.getText());

        if(testId)
        {
            String feedingId = FeedingId.getText();
            int feedingIdValue = Integer.parseInt(feedingId);
            DisplayArea.appendText("Selected feeding id for update is: " + feedingIdValue + "\n");

            // Check if the feeding ID exists in the feeding records table
            try (Connection connection = DriverManager.getConnection(url, user, password))
            {
                connection.setAutoCommit(false);
                String selectQuery = "SELECT COUNT(*) FROM feedingrecords WHERE feeding_id = ?";
                PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                selectStatement.setInt(1, feedingIdValue);
                ResultSet resultSet = selectStatement.executeQuery();
                resultSet.next();
                int rowCount = resultSet.getInt(1);

                if (rowCount == 0)
                {
                    DisplayArea.appendText("Feeding Id does not exists.\n");
                    return;
                }
                else
                {
                    DisplayArea.appendText("Feeding Id exists. \n");
                }

            } catch (SQLException e) {
                e.printStackTrace(); // Handle the exception appropriately
                DisplayArea.appendText("SQL Exception: " + e.getMessage() + "\n");
            }

            //Get values from the form
            String user1 = userComboBox.getValue();
            String animal = animalComboBox.getValue();
            String food = foodComboBox.getValue();
            int userId = ParseId(user1);
            int animalId = ParseId(animal);
            int foodId = ParseId(food);

            //Get old values for user, animal, and food ID
            int oldUser = 0;
            int oldAnimal = 0;
            int oldFood = 0;

            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                // SQL query to retrieve user_id, animal_id, and food_id for a specific feeding_id
                String query = "SELECT user_id, animal_id, food_id FROM feedingrecords WHERE feeding_id = ?";

                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    // Set the feedingIdValue as a parameter in the query
                    statement.setInt(1, feedingIdValue);

                    // Execute the query and retrieve the results
                    ResultSet resultSet = statement.executeQuery();

                    // Process the result set
                    if (resultSet.next()) {
                        oldUser = resultSet.getInt("user_id");
                        oldAnimal = resultSet.getInt("animal_id");
                        oldFood = resultSet.getInt("food_id");
                    } else {
                        System.out.println("No record found for feeding ID: " + feedingIdValue);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


            //Update query for User
            if (userId != oldUser)
            {
                try (Connection connection = DriverManager.getConnection(url, user, password))
                {
                    String updateQuery = "UPDATE feedingrecords SET user_id = ? WHERE feeding_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                        preparedStatement.setInt(1, userId);
                        preparedStatement.setInt(2, feedingIdValue);

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

            //Update query for Animal
            if (animalId != oldAnimal)
            {
                try (Connection connection = DriverManager.getConnection(url, user, password))
                {
                    String updateQuery = "UPDATE feedingrecords SET animal_id = ? WHERE feeding_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                        preparedStatement.setInt(1, animalId );
                        preparedStatement.setInt(2, feedingIdValue);

                        int rowsUpdated = preparedStatement.executeUpdate();

                        if (rowsUpdated > 0) {
                            DisplayArea.appendText("Animal update successful.\n");
                        } else {
                            DisplayArea.appendText("Animal update unsuccessful.\n");
                        }
                    }

                    // Commit the transaction
                    connection.commit();

                } catch (SQLException e) {
                    e.printStackTrace(); // Handle the exception appropriately
                }
            }

            //Update query for Food
            if (foodId != oldFood)
            {
                try (Connection connection = DriverManager.getConnection(url, user, password))
                {
                    String updateQuery = "UPDATE feedingrecords SET food_id = ? WHERE feeding_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                        preparedStatement.setInt(1, foodId );
                        preparedStatement.setInt(2, feedingIdValue);

                        int rowsUpdated = preparedStatement.executeUpdate();

                        if (rowsUpdated > 0) {
                            DisplayArea.appendText("Food update successful.\n");
                        } else {
                            DisplayArea.appendText("Food update unsuccessful.\n");
                        }
                    }

                    // Commit the transaction
                    connection.commit();

                } catch (SQLException e) {
                    e.printStackTrace(); // Handle the exception appropriately
                }
            }

            //Update query for Date
            String date = Date.getText();
            if (!date.isEmpty())
            {
                // Check if date is in the YYYY-MM-DD format
                if (!isValidDateFormat(date))
                {
                    DisplayArea.appendText("Error: Date must be in the YYYY-MM-DD format.\n");
                    return; // Stop processing if date format is invalid
                }

                try (Connection connection = DriverManager.getConnection(url, user, password))
                {
                    String updateQuery = "UPDATE feedingrecords SET feeding_date = TO_DATE(?, 'YYYY-MM-DD') WHERE feeding_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                        preparedStatement.setString(1, date);
                        preparedStatement.setInt(2, feedingIdValue);

                        int rowsUpdated = preparedStatement.executeUpdate();

                        if (rowsUpdated > 0) {
                            DisplayArea.appendText("Feeding date update successful.\n");
                        } else {
                            DisplayArea.appendText("Feeding date update unsuccessful.\n");
                        }
                    }

                    // Commit the transaction
                    connection.commit();

                } catch (SQLException e) {
                    e.printStackTrace(); // Handle the exception appropriately
                }
            }

            //Update query for Food quantity
            String quantity = Quantity.getText();
            if (!quantity.isEmpty())
            {
                //Check if quantity is valid
                boolean testQty = isValidQty(quantity);

                if (testQty)
                {
                    int quantityValue = Integer.parseInt(quantity);

                    // Query to update food quantity
                    try (Connection connection = DriverManager.getConnection(url, user, password))
                    {
                        String updateQuery = "UPDATE feedingrecords SET food_quantity = ? WHERE feeding_id = ?";
                        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                            preparedStatement.setInt(1, quantityValue);
                            preparedStatement.setInt(2, feedingIdValue);

                            int rowsUpdated = preparedStatement.executeUpdate();

                            if (rowsUpdated > 0) {
                                DisplayArea.appendText("Food quantity update successful.\n");
                            } else {
                                DisplayArea.appendText("Food quantity update unsuccessful.\n");
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
    }


    //Methods to populate combo boxes
    public void populateFoodComboBox(ComboBox<String> foodComboBox) {
        // Clear existing items in the ComboBox
        foodComboBox.getItems().clear();

        // SQL query to retrieve food IDs and names
        String query = "SELECT food_id, food_name FROM food order by food_id";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            // Iterate over the result set and add each food ID and name to the ComboBox
            while (resultSet.next()) {
                String foodId = resultSet.getString("food_id");
                String foodName = resultSet.getString("food_name");
                // Concatenate food ID and name and add to the ComboBox
                String foodOption = foodId + " " + foodName;
                foodComboBox.getItems().add(foodOption);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions
        }
    }

    public void populateUserComboBox(ComboBox<String> userComboBox) {
        // Clear existing items in the ComboBox
        userComboBox.getItems().clear();

        // SQL query to retrieve food IDs and names
        String query = "SELECT user_id, user_name FROM users order by user_id";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            // Iterate over the result set and add each food ID and name to the ComboBox
            while (resultSet.next()) {
                String userId = resultSet.getString("user_id");
                String userName = resultSet.getString("user_name");
                // Concatenate user ID and name and add to the ComboBox
                String userOption = userId + " " + userName;
                userComboBox.getItems().add(userOption);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions
        }
    }

    public void populateAnimalComboBox(ComboBox<String> animalComboBox) {
        // Clear existing items in the ComboBox
        animalComboBox.getItems().clear();

        // SQL query to retrieve food IDs and names
        String query = "SELECT animal_id, animal_name, animal_species FROM animals order by animal_id";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            // Iterate over the result set and add each food ID and name to the ComboBox
            while (resultSet.next()) {
                String animalId = resultSet.getString("animal_id");
                String animalName = resultSet.getString("animal_name");
                String animalSpecies = resultSet.getString("animal_species");
                // Concatenate animal ID and name and add to the ComboBox
                String animalOption = animalId + " " + animalName + " (" + animalSpecies + ")";
                animalComboBox.getItems().add(animalOption);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions
        }
    }


    //Methods to get the id from combo box
    public int ParseId(String selection)
    {
        // Split the string by space
        String[] parts = selection.split(" ");

        // Parse the first element as an integer
        int intValue = Integer.parseInt(parts[0]);

        return intValue;
    }

    //Methods to check food stock
    public boolean CheckStock(int id, int quantity)
    {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // SQL query to retrieve stock for specific food
            String query = "SELECT food_stock FROM food WHERE food_id=?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // Set the food_id parameter in the query
                statement.setInt(1, id);

                // Execute the query
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Retrieve the stock value from the result set
                        int stock = resultSet.getInt("food_stock");

                        // Compare quantity with stock
                        return quantity <= stock;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Return false if there's any error or if no stock information is found for the given food_id
        return false;
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

    public static void main(String[] args)
    {
        HelloApplication.launch();
    }
}
