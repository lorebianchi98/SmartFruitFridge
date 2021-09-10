package iot.unipi.it;

import java.sql.*;

public class SmartFridgeDB {

	 @SuppressWarnings("finally")
		private static Connection makeJDBCConnection() {
		Connection databaseConnection = null;


        String databaseIP = "localhost";
        String databasePort = "3306";
        String databaseUsername = "root";
        String databasePassword = "root";
        String databaseName = "smart_fridge";
        
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
	
	 
	 
	public static void insertTemperature(final int temperature, final String unit, final int chiller) {
    	String insertQueryStatement = "INSERT INTO temperature (temperature,unit,chiller) VALUES (?, ?, ?)";
    	
    	try (Connection smartPoolConnection = makeJDBCConnection();
        		PreparedStatement smartPoolPrepareStat = smartPoolConnection.prepareStatement(insertQueryStatement);
           ) {
    		smartPoolPrepareStat.setInt(1, temperature);
        	smartPoolPrepareStat.setString(2,unit);
        	smartPoolPrepareStat.setInt(3,chiller);
        	
        	                
        	smartPoolPrepareStat.executeUpdate();
 
    	 } catch (SQLException sqlex) {
             sqlex.printStackTrace();
         }
	}
	 
	public static void insertOxygen(final int oxygen, final String unit, final int emitter) {
    	String insertQueryStatement = "INSERT INTO oxygen (oxygen,unit,emitter) VALUES (?, ?, ?)";
    	
    	try (Connection smartPoolConnection = makeJDBCConnection();
        		PreparedStatement smartPoolPrepareStat = smartPoolConnection.prepareStatement(insertQueryStatement);
           ) {
    		smartPoolPrepareStat.setInt(1, oxygen);
        	smartPoolPrepareStat.setString(2,unit);
        	smartPoolPrepareStat.setInt(3,emitter);
        	
        	                
        	smartPoolPrepareStat.executeUpdate();
 
    	 } catch (SQLException sqlex) {
             sqlex.printStackTrace();
         }
	}
	
	
    public static void logFruitState(float ethylene_level) {
    	
    	String insertQueryStatement = "INSERT INTO ripening_notifier (ethylene_level) VALUES (?)";
        try (Connection smartFridgeConnection = makeJDBCConnection();
        		PreparedStatement smartFridgePrepareStat = smartFridgeConnection.prepareStatement(insertQueryStatement);
           ) {
            smartFridgePrepareStat.setFloat(1, ethylene_level);
        	smartFridgePrepareStat.executeUpdate();
            
        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }
	
}
