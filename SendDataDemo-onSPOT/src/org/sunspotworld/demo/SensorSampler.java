package org.sunspotworld.demo;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.squawk.util.StringTokenizer;
//import com.sun.spot.resources.Resources;
//import com.sun.spot.resources.transducers.ITriColorLED;
//import com.sun.spot.resources.transducers.ILightSensor;
//import com.sun.spot.util.Utils;
import java.io.IOException;
import java.util.Hashtable;
import javax.microedition.io.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * This application is the 'on SPOT' portion of the SendDataDemo. It
 * periodically samples a sensor value on the SPOT and transmits it to
 * a desktop application (the 'on Desktop' portion of the SendDataDemo)
 * where the values are displayed.
 *
 * @author: Vipul Gupta
 * modified: Ron Goldman
 */
public class SensorSampler extends MIDlet {

    private static final int HOST_PORT = 67;
    private static final int SPOT_COMMUNICATION_PORT = 43;
    
    private static final int RECEIVE = 0;
    private static final int ROUTE = 1;
    private static final int DONOTHING = 2;
    
    //Escolher que aplica√ß√£o correr. No futuro, se possivel criar set/get aplication que recebe/devolve a classe de aplica√ß√£o
    private HelloApplication app = new HelloApplication();
    private FloodingRoutingLayer layer = new FloodingRoutingLayer();
    
    //tabela com correspond√™ncia entre id no simulador (virtual) e o port dos n√≥s reais
    //private Hashtable portTable;
    //private Hashtable connections;
    //private Short port;
    
    /**
     * The id of the node. It is allowed that two nodes have the same id in the
     * simulator.
     */
    protected short id;
    //private boolean portTableReceived = false;
    
    protected void startApp() throws MIDletStateChangeException {
        RadiogramConnection hostReceiveConnection = null;
        //TODO: nao sei se sencon vai ser usado...
        RadiogramConnection hostSendConnection = null;
        Datagram dg = null;
        RadiogramConnection spotCommunicationReceiveConnection = null;
        RadiogramConnection spotCommunicationSendConnection = null;
        //String ourAddress = System.getProperty("IEEE_ADDRESS");
        //System.out.println("Starting sensor sampler application on " + ourAddress + " ...");

        // Listen for downloads/commands over USB connection
        new com.sun.spot.service.BootloaderListenerService().getInstance().start();

        this.openHostConnections(hostReceiveConnection, hostSendConnection);
        // Recebe o seu id vindo do host. Enquanto n„o tiver recebido, continua a tentar.
//        boolean receiveIdFromHostWorked = false;
//        while(!receiveIdFromHostWorked) {
            //TODO: penso que se nao recebe, tem de pedir de novo!! 
    //        receiveIdFromHostWorked = 
        this.receiveIdFromHost(hostReceiveConnection, hostReceiveConnection, dg);
//        }
        
        this.openSpotCommunicationConnections(spotCommunicationReceiveConnection, spotCommunicationSendConnection);
        
        
        /*
         * Loop infinito
         * Espera por mensagens do host (vindas do simulador)
         * Se este for o destinat·rio da mensagem, executa funÁ„o de recepÁ„o
         * Else, faz broadcast da mensagem
         * 
         * Enquanto espera por mensagens do host, tambÈm tem de esperar por mensagens dos outros SunSpots (como?)
         * 
         */
        while (true) {
            try {
                spotCommunicationReceiveConnection.receive(dg);
                
                long numberOfTotalMessage = dg.readLong();
                long messageNumber = dg.readLong();
                byte[] b = null;
                dg.readFully(b);
                byte[] payload = b;
                long totalHops = dg.readLong();
                Short sourceId = new Short(dg.readShort());
                Short destinationId = new Short(dg.readShort());
                Short uniqueId = new Short(dg.readShort());
                Message m = new Message(numberOfTotalMessage, messageNumber, payload, totalHops, sourceId, destinationId, uniqueId);
                
                int result = layer.onReceiveMessage(m);
                switch(result){
                    case RECEIVE:
                        app.onMessageReceived(m);
                        break;
                    case ROUTE:
                        m.hop();
                        layer.onRouteMessage(m);
                        broadcast(m);
                        break;
                    case DONOTHING:
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                System.err.println("Caught " + e + " while collecting/sending sensor sample.");
            }
        }
    }
    
    /***************************************
     *         communication methods       *
     ***************************************/
    
    
    public boolean openSpotCommunicationConnections(RadiogramConnection spotCommunicationReceiveConnection, RadiogramConnection spotCommunicationSendConnection) {
        
        try{
            spotCommunicationReceiveConnection = (RadiogramConnection) Connector.open("radiogram://:" + SPOT_COMMUNICATION_PORT);
            spotCommunicationSendConnection = (RadiogramConnection) Connector.open("radiogram://:" + SPOT_COMMUNICATION_PORT);
            return true;
        } catch(Exception e){
            System.err.println("Caught " + e + " in spot connection initialization.");
            //TODO: ver que È isto
            notifyDestroyed();
            return false;
        }
    }
    
    //talvez seja preciso criar um metodo para cada
    public boolean openHostConnections(RadiogramConnection hostReceiveConnection, RadiogramConnection hostSendConnection){
        try{
            hostReceiveConnection = (RadiogramConnection) Connector.open("radiogram://:" + HOST_PORT);
            hostSendConnection = (RadiogramConnection) Connector.open("radiogram://:" + HOST_PORT);
            return true;
        } catch(Exception e){
            System.err.println("Caught " + e + " in host connection initialization.");
            //TODO: ver que È isto
            notifyDestroyed();
            return false;
        }
    }
    
    public boolean receiveIdFromHost(RadiogramConnection hostReceiveConnection, RadiogramConnection hostSendConnection, Datagram dg){
        try {
            dg = hostReceiveConnection.newDatagram(hostReceiveConnection.getMaximumLength());
            short id = dg.readShort();
            this.setId(id);
            return true;
        } catch (Exception e) {
            // on catch talvez pedir de novo
            
            return false;
        }        
    }
    
    //receber um dg com um Short id a uma String com todos os dados que v„o compor a tabela de ports
//    public void receivePorts(RadiogramConnection hostConnection, Datagram dg) {
//        while(!portTableReceived) {
//            try{
//                hostConnection.receive(dg);
//                setId(dg.readShort());
//                String rawPortsString = dg.readUTF();
//                //Divide a string recebida (ex: "54,65;87,99,7;82") em varios tokens
//                StringTokenizer portIdPairs = new StringTokenizer(rawPortsString, ";");
//                StringTokenizer portAndId = null;
//                //Divide os tokens em pares id/porta e adiciona a tabela
//                while(portIdPairs.hasMoreTokens()) {
//                    portAndId = new StringTokenizer(portIdPairs.nextToken(), ",");
//                    String id = portAndId.nextToken();
//                    String port = portAndId.nextToken();
//                    portTable.put(id, port);
//                }
//                portTableReceived = true;
//            } catch(Exception e) {
//                System.err.println("Caught " + e + " while connecting to host to receive Port Table.");
//            }
//        }
//    }
    
    //abre conexoes para cada outro spot
//    public void openConnections(){
//        
//        Iterator<Entry<Short, Integer>> portsIt = portTable.entrySet().iterator();
//        short thisID = this.getId();
//        while(portsIt.hasNext()){
//            
//            RadiogramConnection newCon = null;
//            short currentID = portsIt.next().getKey();
//            //n√£o abrir conex√£o para si mesmo
//            if(thisID != currentID){
//               
//                int currentPort = portsIt.next().getValue();
//                try {
//                    newCon=  (RadiogramConnection) Connector.open("radiogram://:" + currentPort);
//                    connections.put(currentID, newCon);
//
//                } catch (Exception e) {
//                    System.err.println("Caught " + e + " in connection initialization.");
//                    notifyDestroyed();
//                }
//            }
//            
//        }
//        
//    }
    
    public void broadcast(Object message){
        DatagramConnection connection = null;
        Datagram datagram = null;
        
        String ourAddress = System.getProperty("IEEE_ADDRESS");
        System.out.println("Starting sensor sampler application on " + ourAddress + " ...");

    // Listen for downloads/commands over USB connection
    //new com.sun.spot.service.BootloaderListenerService().getInstance().start();

        //criar conexao e datagrama
        try {
            // Open up a broadcast connection to the host port where the 'on Desktop' portion of this demo is listening
            connection = (DatagramConnection) Connector.open("radiogram://broadcast:" + SPOT_COMMUNICATION_PORT);
            datagram = connection.newDatagram(50);  // only sending 12 bytes of data
        } catch (Exception e) {
            System.err.println("Caught " + e + " in connection initialization.");
            notifyDestroyed();
        }
        
        //envio broadcast
        try {
            datagram.reset();
            datagram.writeChars("Hello!");
            connection.send(datagram);
        } catch (Exception e) {
            System.err.println("Caught " + e + " while sending datagrams.");
        }
    }
    
    public void send(){
        
    }
    
    public void receive(){
        
    }
    
    /***************************************
     *         communication methods       *
     ***************************************/
    
    
    
    
    
    /***************************************
     *         auxiliary methods       *
     ***************************************/
    
    public final void setId(short id) {
        this.id = id;
    }
    
    public short getId() {
        return id;
    }
    
    /***************************************
     *         auxiliary methods       *
     ***************************************/
    
    protected void pauseApp() {
        // This will never be called by the Squawk VM
    }
    
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        // Only called if startApp throws any exception other than MIDletStateChangeException
    }
}
