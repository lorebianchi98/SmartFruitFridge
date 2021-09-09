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

    private CoapClient clientEthyleneSensor;
    private CoapObserveRelation observeSensor;

    //a map which use as key the URI of the ethylene sensor associated with the actuator
    private CoapClient clientRipeningNotifier;

    private static CoapNetworkHandler instance = null;

    public static CoapNetworkHandler getInstance() {
        if (instance == null)
            instance = new CoapNetworkHandler();
        return instance;
    }

    public boolean addRipeningNotifier(String ipAddress) {
        if (clientRipeningNotifier != null) {
            System.out.println("There is already a ripening notifier registered");
            return false;
        }

        clientRipeningNotifier = new CoapClient("coap://[" + ipAddress + "]/ripening_notifier");
        System.out.println("The ripening notifier: [" + ipAddress + "] + is now registered");
        return true;
    }


    public void addEthyleneSensor(String ipAddress) {

        System.out.println("The ethylene sensor: [" + ipAddress + "] + is now registered");
        clientEthyleneSensor = new CoapClient("coap://[" + ipAddress + "]/ethylene_sensor");
        observeSensor = clientEthyleneSensor.observe(
                new CoapHandler() {
                    public void onLoad(CoapResponse response) {
                        String responseString = response.getResponseText();
                        if (responseString.compareTo("") != 0) {
                            float ethylene_level = Float.parseFloat(responseString);
                            String state;
                            //printing the state of the fruit based on the ethylene level
                            if (ethylene_level < 250)
                                state = "unripe";
                            else if (ethylene_level < 400)
                                state = "ripe";
                            else
                                state = "expired";
                            System.out.println("Ethylene level: " + ethylene_level + ", fruit state: " + state);
                            toggleRipeningNotifier(responseString);
                            if (clientRipeningNotifier != null)
                                SmartFridgeDbManager.logFruitState(ethylene_level);
                        }
                        System.out.println("");
                    }

                    public void onError() {
                        System.err.println("OBSERVING FAILED");
                    }
                });
    }


    private void toggleRipeningNotifier(String responseString) {
        if(clientRipeningNotifier != null) {
            clientRipeningNotifier.put(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    if (response != null) {
                        if(!response.isSuccess())
                            System.out.println("Something went wrong with the ripening notifier");
                    }
                }

                public void onError() {
                    System.err.println("[ERROR: RipeningNotifier " + clientRipeningNotifier.getURI() + "] ");
                }

            }, responseString, MediaTypeRegistry.TEXT_PLAIN);
        }
        else
            System.out.println("There is no ripening notifier associated");
    }

    public void cutAllConnection() {
        observeSensor.proactiveCancel();
    }


    public boolean deleteRipeningNotifier() {
        if (clientRipeningNotifier == null)
            return false;
        clientRipeningNotifier = null;
        return true;
    }



    public boolean deleteEthyleneSensor() {
        if (clientEthyleneSensor == null)
            return false;
        clientEthyleneSensor = null;
        return true;
    }



    public void stampEthyleneSensors() {
        if (clientEthyleneSensor != null)
            System.out.println("> " + clientEthyleneSensor + "\n");
        else
            System.out.println("There is no sensor registered" + '\n');
    }


    public void stampRipeningNotifiers() {
        if (clientRipeningNotifier != null)
            System.out.println("> " + clientRipeningNotifier.getURI() + '\n');
        else
            System.out.println("There is no notifier registered" + '\n');
    }


}