package com.example.zooapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class LoginApplication extends Application
{
    //Global Values Login
    private static String gvUserName;
    private static String gvUserType;
    private static int  gvLoginAttempts;

    //Content Scene
    private TextField UserId;
    private PasswordField UserPassword;
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
        //Change page title
        primaryStage.setTitle("Login Page");

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

        //Animal Information
        UserId = new TextField();
        UserPassword = new PasswordField();

        pane.add(new Label("User Id: "), 0, 0);
        pane.add(UserId, 1, 0);
        pane.add(new Label("Password: "), 0, 1);
        pane.add(UserPassword, 1, 1);

        // Create update button
        Button loginButton = new Button("    Login    ");
        pane.add(loginButton, 4, 0);
        loginButton.setOnAction(event ->
        {
            
            int rowCount = 0;

                //Verify Credentials
                rowCount = validateLoginCredentials();
               if(rowCount == 0){
                   if(gvLoginAttempts == 2)
                   {
                       loginButton.setDisable(true);
                       //User doesn't exist
                       Alert alert = new Alert(Alert.AlertType.INFORMATION);
                       alert.setTitle("Information Dialog");
                       alert.setHeaderText(null);
                       alert.setContentText("Too many attempts. Session Blocked. Contact your administrator!");

                       // Show the message box
                       alert.showAndWait();
                   }else {
                       gvLoginAttempts += 1;

                       //User doesn't exist
                       Alert alert = new Alert(Alert.AlertType.INFORMATION);
                       alert.setTitle("Information Dialog");
                       alert.setHeaderText(null);
                       alert.setContentText("User does not exist. Try again!.");

                       // Show the message box
                       alert.showAndWait();
                   }
                }
                else{
                    //Granted Access
                    primaryStage.setScene(new Scene(new HelloApplication().createContent(primaryStage)));
                }
        });

        // Create main pane
        Label boldLabel1 = new Label("   Zoo Management System    ");
        boldLabel1.setFont(Font.font("Arial", FontWeight.BOLD, 12));
         mainpane.add(boldLabel1, 0, 1);
        mainpane.add(pane, 0, 2);

        return mainpane;
    }

    //Method for Display button
    public int validateLoginCredentials()
    {
        int rowCount = 0;

        // Create a new JFrame for the resultTable
        try
        {
            //Get login data
            String userId = UserId.getText();
            String userPassword = UserPassword.getText();

            System.out.println("> Start Program ...");
            Class.forName("oracle.jdbc.driver.OracleDriver");
            System.out.println("> Driver Loaded successfully.");
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Database connected successfully.");

            String selectQuery = "SELECT * from users WHERE user_id = ? and password_hash = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
            selectStatement.setString(1, userId);
            selectStatement.setString(2, userPassword);
            ResultSet resultSet = selectStatement.executeQuery();
            resultSet.next();
            rowCount = resultSet.getInt(1);

            //Set UserName as Global Value
            setUserName(resultSet.getString(4));

            //Set UserType as Global Value
            setUserType(resultSet.getString(3));

            // Close resources
            resultSet.close();
            selectStatement.close();
            connection.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return rowCount;
    }

    public String getUserName() {
        return gvUserName;
    }

    public void setUserName(String userName) {
        gvUserName = userName;
    }

    public String getUserType() {
        return gvUserType;
    }

    public void setUserType(String userType) {
        gvUserType = userType;
    }

    public static void main(String[] args)
    {
        LoginApplication.launch();
    }
}

