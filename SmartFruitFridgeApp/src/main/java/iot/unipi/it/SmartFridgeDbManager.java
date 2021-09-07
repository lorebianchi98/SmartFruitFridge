package iot.unipi.it;

import java.sql.*;

public class SmartFridgeDbManager {
	private static SmartFridgeDbManager instance = null;
	private static String databaseIP = "localhost";
    private static String databasePort = "3306";
    private static String databaseUsername = "root";
    private static String databasePassword = "PASSWORD";
    private static String databaseName = "smart_fridge";

	private static Connection makeJDBCConnection() {
        Connection databaseConnection = null;
        
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");//checks if the Driver class exists (correctly available)
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return databaseConnection;
        }
        
        try {
            // DriverManager: The basic service for managing a set of JDBC drivers.
            databaseConnection = DriverManager.getConnection(
                    "jdbc:mysql://" + databaseIP + ":" + databasePort +
                            "/" + databaseName + "?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=CET",
                    databaseUsername,
                    databasePassword);
            //The Driver Manager provides the connection specified in the parameter string
            if (databaseConnection == null) {
                System.err.println("Connection to Db failed");
            }
        } catch (SQLException e) {
        	System.err.println("MySQL Connection Failed!\n");
            e.printStackTrace();
        }finally {
            return databaseConnection;
        }
    }
    


    public static void logFruitState(String notifierURI, String state) {
    	
    	String insertQueryStatement = "INSERT INTO ripening_notifier (uri, state) VALUES (?, ?)";
        int numericState = 0;
        try (Connection smartFridgeConnection = makeJDBCConnection();
        		PreparedStatement smartFridgePrepareStat = smartFridgeConnection.prepareStatement(insertQueryStatement);
           ) {
            //converting the state from string to numeric
            switch(state) {
                case "unripe":
                    numericState = 0;
                    break;
                case "ripe":
                    numericState = 1;
                    break;
                case "expired":
                    numericState = 2;
                    break;
                default:
                    System.out.println("Invalid state! This information will not be saved on the database");
            }
        	smartFridgePrepareStat.setString(1, notifierURI);
            smartFridgePrepareStat.setInt(2, numericState);
        	smartFridgePrepareStat.executeUpdate();
            
        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }
}
