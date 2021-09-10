package iot.unipi.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

public class Collector {
	
	public static void main(String[] args) throws SocketException, InterruptedException{

		
		RegistrationServer rs = new RegistrationServer();
		rs.start();
		
		// to read inputs from terminal
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		String command = "";
		String[] tokens;
		
		System.out.println("\nCommand list:");
		System.out.println("Command list:");
		System.out.println("!exit: exit the program");
		System.out.println("!commands: list possible commands");
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
				} else if (tokens[0].equals("!commands")) {
					System.out.println("Command list:");
					System.out.println("!exit: exit the program");
					System.out.println("!commands: list possible commands");
					System.out.println("!checkEthylene: get current level of ethylene");
					System.out.println("!getSensor: get sensor information");
					System.out.println("!getNotifier: get notifier information");

				}
				else if (tokens[0].equals("!checkEthylene"))
				{
					rs.checkEthyleneLevel();
				}
				else if (tokens[0].equals("!getSensor"))
				{
					rs.stampPresenceSensor();
					
				} 
				else if (tokens[0].equals("!getNotifier"))
				{
					rs.stampRipeningNotifier();
					
				}
				else
				{
					System.out.println("Invalid command");
				}

				System.out.println("\n");
				
			} catch (IOException e) {
				System.out.println("Error while reading the command; please retry!");
			}
		}
	}
}
	
