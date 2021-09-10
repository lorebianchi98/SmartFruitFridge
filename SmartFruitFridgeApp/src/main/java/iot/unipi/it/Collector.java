package iot.unipi.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

import org.eclipse.paho.client.mqttv3.MqttException;

public class Collector {
	
	public static void main(String[] args) throws SocketException, InterruptedException{
		
		CollectorMqttClient mc = new CollectorMqttClient();
		
		RegistrationServer rs = new RegistrationServer();
		rs.start();
		
		// to read inputs from terminal
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		String command = "";
		String[] tokens;
		
		System.out.println("\nCommand list:");
		System.out.println("!exit: exit the program");
		System.out.println("!commands: list possible commands");					
		System.out.println("!checkTemp: get current temperature");
		System.out.println("!checkOxygen: get current oxygen level");
		System.out.println("!triggerRipening: speeds up fruit ripening");
		System.out.println("!conservation: slows down fruit ripening");
		System.out.println("!checkEthylene: get current level of ethylene");
		System.out.println("!getSensor: get sensor information");
		System.out.println("!getNotifier: get notifier information");
		System.out.println("\n");
		
		while(true) {
			try {
				command = reader.readLine();
				tokens = command.split(" ");
				
				if (tokens[0].equals("!exit")) 
				{
					System.exit(1);
				} else if (tokens[0].equals("!commands")) 
				{
					System.out.println("\nCommand list:");
					System.out.println("!exit: exit the program");
					System.out.println("!commands: list possible commands");					
					System.out.println("!checkTemp: get current temperature");
					System.out.println("!checkOxygen: get current oxygen level");
					System.out.println("!triggerRipening: speeds up fruit ripening");
					System.out.println("!conservation: slows down fruit ripening");
					System.out.println("!checkEthylene: get current level of ethylene");
					System.out.println("!getSensor: get sensor information");
					System.out.println("!getNotifier: get notifier information");
					System.out.println("\n");
					
				} else if (tokens[0].equals("!checkTemp")) 
				{
					System.out.format("The temperature in the fridge is %f °C", mc.getCurrentTemp());
					
				} else if (tokens[0].equals("!checkOxygen")) 
				{
					System.out.format("The oxygen level in the fridge is of %f ppt", mc.getCurrentOx());
					
				} else if (tokens[0].equals("!triggerRipening"))
				{
					mc.setRipening(true);
					
				} else if (tokens[0].equals("!conservation"))
				{
					mc.setRipening(false);
				} else if (tokens[0].equals("!checkEthylene"))
				{
					rs.checkEthyleneLevel();
				} else if (tokens[0].equals("!getSensor"))
				{
					rs.stampPresenceSensor();
					
				} else if (tokens[0].equals("!getNotifier"))
				{
					rs.stampRipeningNotifier();
				} else {
					throw new IOException();
				}

				System.out.println("\n");
				
			} catch (IOException e) {
				System.out.println("Command not found, please retry!");
			}
		}
	}
}