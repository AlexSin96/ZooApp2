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

public class AnalyticsApplication extends Application
{
    private TextField Name, Quantity, FoodId;
    private ComboBox<String> foodComboBox;
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
        primaryStage.setTitle("Analytics Page");

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

        //Populate combo box
        foodComboBox = new ComboBox<>();
        populateFoodComboBox(foodComboBox);
        foodComboBox.getSelectionModel().selectFirst();

        pane.add(new Label("Food: "), 0, 0);
        pane.add(foodComboBox, 1, 0);


        // Create Display button
        Button displayButton = new Button("Display Analytics");
        pane.add(displayButton, 2, 0);

        displayButton.setDisable((login.getUserType().equals("Manager") ? false : true));
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
        Label boldLabel1 = new Label("   Analytics Information:                                                                                      " +"Logged as: " + login.getUserName());
        boldLabel1.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        mainpane.add(menuBar, 0, 0);
        mainpane.add(boldLabel1, 0, 1);
        mainpane.add(pane, 0, 2);

        return mainpane;
    }

    //Method for Display button
    public void displayClicked() {
        DisplayArea.clear();
        String food = foodComboBox.getValue();
        int foodId = ParseId(food);

        //Get food info
        String foodName = "";
        int foodStock = 0;
        int weeklyConsumption = 0;
        int forecast = 0;

        // Get food info from database
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // SQL query to retrieve user_type
            String query = "SELECT food_name, food_stock FROM food WHERE food_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // Set the feedingIdValue as a parameter in the query
                statement.setInt(1, foodId);

                // Execute the query and retrieve the results
                ResultSet resultSet = statement.executeQuery();

                // Process the result set
                if (resultSet.next()) {
                    foodName = resultSet.getString("food_name");
                    foodStock = resultSet.getInt("food_stock");
                } else {
                    System.out.println("No record found for Food ID: " + foodId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Get weekly consumption from database
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // SQL query to retrieve user_type
            String query = "SELECT SUM(FOOD_Quantity) AS weekly_consumption FROM feedingrecords WHERE food_ID = ? AND Feeding_date >= SYSDATE - 7";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // Set the feedingIdValue as a parameter in the query
                statement.setInt(1, foodId);

                // Execute the query and retrieve the results
                ResultSet resultSet = statement.executeQuery();

                // Process the result set
                if (resultSet.next()) {
                    weeklyConsumption = resultSet.getInt("weekly_consumption");
                } else {
                    System.out.println("No record found for Food ID: " + foodId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Display text
        DisplayArea.appendText("Food selected: " + foodName +".\n");
        DisplayArea.appendText("Current stock: " + foodStock +" Kg.\n");
        DisplayArea.appendText("Last 7 days consumption: " + weeklyConsumption + " Kg.\n");

        if (weeklyConsumption == 0)
        {
            DisplayArea.appendText("No data available to make forecast.\n");
        }
        else
        {
            forecast = foodStock / weeklyConsumption;
            DisplayArea.appendText("Current stock lasting forecast: " + forecast + " weeks.\n");
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

    //Methods to get the id from combo box
    public int ParseId(String selection)
    {
        // Split the string by space
        String[] parts = selection.split(" ");

        // Parse the first element as an integer
        int intValue = Integer.parseInt(parts[0]);

        return intValue;
    }


    public static void main(String[] args) {
        HelloApplication.launch();
    }
}
