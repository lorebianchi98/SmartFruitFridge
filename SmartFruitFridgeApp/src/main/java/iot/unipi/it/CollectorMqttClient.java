package iot.unipi.it;

import java.util.Map;
import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class CollectorMqttClient implements MqttCallback{
	
	private final String broker = "tcp://127.0.0.1:1884";
	private final String clientId = "JavaApp";
	
	private final String tempSubTopic = "temperature";
	private final String tempPubTopic = "chiller";
	
	private final String oxSubTopic = "oxygen";
	private final String oxPubTopic = "oxygen-regulator";
	
	private MqttClient mqttClient = null;
	
	// State variable for control the ripening of fruit
	private boolean ripening = false;
	
	// Limit temperatures in Celsius
	private final int lowTemp = -4;
	private final int upTemp = 8;
	private int currentTemp;
	private boolean chiller = false;
	
	// Limit level of Oxygen in part per thousand
	private final int lowOx = 100;
	private final int upOx = 350;
	private int currentOx;
	private boolean oxygen_emitter = false;

	
	//-----------------------------------------------------------------------*/
	
	public CollectorMqttClient() throws InterruptedException {
		do {
			try {
				this.mqttClient = new MqttClient(this.broker,this.clientId);
		        System.out.println("Connecting to broker: "+broker);
		        
				this.mqttClient.setCallback( this );
				this.mqttClient.connect();
				
				this.mqttClient.subscribe(this.tempSubTopic);
		        System.out.println("Subscribed to topic: "+this.tempSubTopic);
		        
				this.mqttClient.subscribe(this.oxSubTopic);
		        System.out.println("Subscribed to topic: "+this.oxSubTopic);
			}catch(MqttException me) {
				System.out.println("I could not connect, Retrying ...");
			}
		}while(!this.mqttClient.isConnected());
	}
	

	public void publish(final String topic, final String content) throws MqttException{
		try {
			MqttMessage message = new MqttMessage(content.getBytes());
			this.mqttClient.publish(topic, message);
			System.out.println("Ho mandato una publish");
		} catch(MqttException me) {
			me.printStackTrace();
		}
	}
	
	
	public void connectionLost(Throwable cause) {
		System.out.println("Connection is broken: " + cause);
		int timeWindow = 3000;
		while (!this.mqttClient.isConnected()) {
			try {
				System.out.println("Trying to reconnect in " + timeWindow/1000 + " seconds.");
				Thread.sleep(timeWindow);
				System.out.println("Reconnecting ...");
				timeWindow *= 2;
				this.mqttClient.connect();
				
				this.mqttClient.subscribe(this.tempSubTopic);
				this.mqttClient.subscribe(this.oxSubTopic);
				System.out.println("Connection is restored");
			}catch(MqttException me) {
				System.out.println("I could not connect");
			} catch (InterruptedException e) {
				System.out.println("I could not connect");
			}
		}
	}

	//-------------------------------------------------------------------------------------------------

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		byte[] payload = message.getPayload();
		try {
			JSONObject sensorMessage = (JSONObject) JSONValue.parseWithException(new String(payload));
			
			if(topic.equals(this.tempSubTopic)) 
			{
				if (sensorMessage.containsKey("temperature")
					&& sensorMessage.containsKey("unit")
					&& sensorMessage.containsKey("chiller")
					) {
					// Parsing
					this.currentTemp = Integer.parseInt(sensorMessage.get("temperature").toString());
					//System.out.println("Ho letto la temperatura ed è "+ this.currentTemp);
					String unitTemp = sensorMessage.get("unit").toString();
					int stateChiller = Integer.parseInt(sensorMessage.get("chiller").toString());
					// Put data in DB
					SmartFridgeDB.insertTemperature(currentTemp,unitTemp,stateChiller);
					
					System.out.println("[TEMPERATURE]");
					
					if(currentTemp <=  lowTemp){
						System.out.println("Temperature is too low! Switch off chiller");
						publish(this.tempPubTopic, "OFF");
						chiller = false;
						
					} else if(currentTemp >= upTemp){
						System.out.println("Temperature is too high! Switch on chiller");
						publish(this.tempPubTopic, "ON");
						chiller = true;
					} else if(ripening){
						if(!chiller)
							System.out.println("Chiller is already off, the fruit is ripening");
						else{
							publish(this.tempPubTopic, "OFF");
							System.out.println("Chiller turned off, the fruit ripens faster!");
							chiller = false;
						}
					} else {
						if(chiller)
							System.out.println("Chiller is already on, the fruit is in conservation state");
						else{
							publish(this.tempPubTopic, "ON");
							System.out.println("Chiller turned on, the fruit is conserved!");
							chiller = true;
						}
					}
					
					//-----------------------------------------------------------------------
				} else {
					System.out.println("Non-compliant data from sensor");
				}	
				
			// --------------------------------------------------------------------------------	
				
				
			} else if (topic.equals(this.oxSubTopic)) {
				if (sensorMessage.containsKey("oxygen")
					&& sensorMessage.containsKey("unit")
				    && sensorMessage.containsKey("emitter")
					) {
					// Parsing
					this.currentOx = Integer.parseInt(sensorMessage.get("oxygen").toString());
					//System.out.println("Ho l'ossigeno ed è "+ this.currentOx);
					String unitOx = sensorMessage.get("unit").toString();
					int stateEmitter = Integer.parseInt(sensorMessage.get("emitter").toString());

					// Put data in DB
					SmartFridgeDB.insertOxygen(currentOx,unitOx,stateEmitter);
					
					System.out.println("[OXYGEN]");
					
					if(currentOx >= upOx){
						System.out.println("Oxygen level is too high! Switch off oxygen emitter");
						publish(this.oxPubTopic, "OFF");
						oxygen_emitter = false;
					} else if(currentOx <= lowOx){
						System.out.println("Oxygen level is too low! Switch on oxygen emitter");
						publish(this.oxPubTopic, "ON");
						oxygen_emitter = true;
					} else if(ripening){
						if(oxygen_emitter)
							System.out.println("Oxygen emitter is already on, the fruit is ripening");
						else{
							publish(this.oxPubTopic, "ON");
							System.out.println("Oxygen emitter turned on, the fruit ripens faster!");
							oxygen_emitter = true;
						}
					} else {
						if(!oxygen_emitter)
							System.out.println("Oxygen emitter is already off, the fruit is in conservation state");
						else{
							publish(this.oxPubTopic, "OFF");
							System.out.println("Oxygen emitter turned off, the fruit is conserved!");
							oxygen_emitter = false;
						}
					}
		
					// --------------------------------------------------------------------
				} else {
					System.out.println("Non-compliant data from sensor");
				}	
			} else {
				System.out.println(String.format("Unknown topic: [%s] %s", topic, new String(payload)));
			}
		} catch (ParseException e) {
			System.out.println(String.format("Received badly formatted message: [%s] %s", topic, new String(payload)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//--------------------------------------------------------------------------------------------------------

	public void deliveryComplete(IMqttDeliveryToken token) {
		//System.out.println("Delivery Complete\n");
	}

	public void setRipening(boolean state){
		ripening = state;
	}

	public double getCurrentTemp() {
		return this.currentTemp;
	}
	
	public double getCurrentOx() {
		return this.currentOx;
	}
	
}
