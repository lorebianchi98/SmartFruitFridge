package iot.unipi.it;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;

public class CoapNetworkHandler {

    private List<CoapClient> clientEthyleneSensorList = new ArrayList<CoapClient>();
    private List<CoapObserveRelation> observeSensorList = new ArrayList<CoapObserveRelation>();

    //a map which use as key the URI of the ethylene sensor associated with the actuator
    private Map<String, CoapClient> clientRipeningNotifierMap = new HashMap<String, CoapClient>();

    private static CoapNetworkHandler instance = null;

    public static CoapNetworkHandler getInstance() {
        if (instance == null)
            instance = new CoapNetworkHandler();
        return instance;
    }

    public boolean addRipeningNotifier(String ipAddress) {
        if (clientEthyleneSensorList.size() == 0) {
            System.out.println("There is no sensor to associate with the ripeining notifier");
            return false;
        }

        CoapClient newRipeningNotifier = new CoapClient("coap://[" + ipAddress + "]/ripening_notifier");
        String sensorURI = clientEthyleneSensorList.get(clientEthyleneSensorList.size() - 1).getURI();
        System.out.println("The ripening notifier: [" + ipAddress + "] + is now registered");
        clientRipeningNotifierMap.put(sensorURI, newRipeningNotifier);
        //ipActuator = ipAddress;
        return true;
    }


    public void addEthyleneSensor(String ipAddress) {

        System.out.println("The ethylene sensor: [" + ipAddress + "] + is now registered");
        CoapClient newEthyleneSensor = new CoapClient("coap://[" + ipAddress + "]/ethylene_sensor");
        final String sensorURI = newEthyleneSensor.getURI();
        CoapObserveRelation newObservePresence = newEthyleneSensor.observe(
                new CoapHandler() {
                    public void onLoad(CoapResponse response) {
                        String responseString = response.getResponseText();

                        System.out.println(sensorURI + ": Fruit state: " + responseString);
                        toggleRipeningNotifier(responseString, sensorURI);
                        //SmartFruitDbManager.logFruitState(false);

                        System.out.println("");
                    }

                    public void onError() {
                        System.err.println("OBSERVING FAILED");
                    }
                });

        clientEthyleneSensorList.add(newEthyleneSensor);
        observeSensorList.add(newObservePresence);
    }


    private void toggleRipeningNotifier(String state, String sensorURI) {
        final CoapClient ripeningNotifier = clientRipeningNotifierMap.get(sensorURI);
        if(ripeningNotifier != null) {
            ripeningNotifier.put(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    if (response != null) {
                        if(!response.isSuccess())
                            System.out.println("Something went wrong with the ripening notifier");
                    }
                }

                public void onError() {
                    System.err.println("[ERROR: RipeningNotifier " + ripeningNotifier.getURI() + "] ");
                }

            }, state, MediaTypeRegistry.TEXT_PLAIN);
        }
        else
            System.out.println("The sensor is not associated with any ripening notifier");
    }

    public void cutAllConnection() {
        for(CoapObserveRelation relationToCancel: observeSensorList)
            relationToCancel.proactiveCancel();
    }


    public boolean deleteRipeningNotifier(String ipAddress) {
        for(Map.Entry<String, CoapClient> entry : clientRipeningNotifierMap.entrySet()) {
            String key = entry.getKey();
            CoapClient ripeningNotifier = entry.getValue();

            if (ripeningNotifier.getURI().equals(ipAddress)) {
                clientRipeningNotifierMap.remove(key);
                return true;
            }
        }
        return false;
    }



    public boolean deleteEthyleneSensor(String ipAddress) {
        for(int i = 0; i < clientEthyleneSensorList.size(); i++)
            if(clientEthyleneSensorList.get(i).getURI().equals(ipAddress)) {
                clientEthyleneSensorList.remove(i);
                observeSensorList.get(i).proactiveCancel();
                observeSensorList.remove(i);
                return true;
            }
        return false;

    }

    public int getNumberOfEthyleneSensors() {
        return clientEthyleneSensorList.size();
    }

    public void stampEthyleneSensors() {
        for(CoapClient cc: clientEthyleneSensorList)
            System.out.println("> " + cc.getURI() + "\n");
    }
    
    public int getNumberOfRipeningNotifiers() {
        return clientRipeningNotifierMap.size();
    }

    public void stampRipeningNotifiers() {
        for(Map.Entry<String, CoapClient> entry : clientRipeningNotifierMap.entrySet()) {
            String ipSensor = entry.getKey();
            CoapClient cc = entry.getValue();

            System.out.println("> " + cc.getURI() + " associated with sensor: " + ipSensor);
        }
    }


}