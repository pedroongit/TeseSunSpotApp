/*
 * SunSpotApplication.java
 *
 * Created on Feb 16, 2014 9:55:51 PM;
 */

package org.sunspotworld;

//import com.sun.spot.resources.Resources;
//import com.sun.spot.resources.transducers.ITriColorLED;
//import com.sun.spot.resources.transducers.ITriColorLEDArray;
//import com.sun.spot.util.Utils;
import javax.microedition.io.*;
import com.sun.spot.io.j2me.radiogram.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * The startApp method of this class is called by the VM to start the
 * application.
 * 
 * The manifest specifies this class as MIDlet-1, which means it will
 * be selected for execution.
 */
public class SunSpotApplication extends MIDlet {

    private static final int HOST_PORT = 67;
    private static final int SPOT_COMMUNICATION_PORT = 43;
    
    private static final int RECEIVE = 0;
    private static final int ROUTE = 1;
    private static final int DONOTHING = 2;
    
    //Escolher que aplicaÃ§Ã£o correr. No futuro, se possivel criar set/get aplication que recebe/devolve a classe de aplicaÃ§Ã£o
    private final HelloApplication app = new HelloApplication();
    private final FloodingRoutingLayer layer = new FloodingRoutingLayer();
        
    /**
     * The id of the node. It is allowed that two nodes have the same id in the
     * simulator.
     */
    protected short id;
    //private boolean portTableReceived = false;
    
    protected void startApp() throws MIDletStateChangeException {
        System.out.print("MAMAKUDIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");
        RadiogramConnection hostReceiveConnection = null;
        //TODO: nao sei se sencon vai ser usado...
        RadiogramConnection hostSendConnection = null;
        Datagram dg = null;
        RadiogramConnection spotCommunicationReceiveConnection = null;
        RadiogramConnection spotCommunicationSendConnection = null;
        
        // Listen for downloads/commands over USB connection
        new com.sun.spot.service.BootloaderListenerService().getInstance().start();

        this.openHostConnections(hostReceiveConnection, hostSendConnection);

        // Recebe o seu id vindo do host. Enquanto não tiver recebido, continua a tentar.
        this.receiveIdFromHost(hostReceiveConnection, hostReceiveConnection, dg);

        
        this.openSpotCommunicationConnections(spotCommunicationReceiveConnection, spotCommunicationSendConnection);
        
        
        /*
         * Loop infinito
         * Espera por mensagens do host (vindas do simulador)
         * Se este for o destinatário da mensagem, executa função de recepção
         * Else, faz broadcast da mensagem
         * 
         * Enquanto espera por mensagens do host, também tem de esperar por mensagens dos outros SunSpots (como?)
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

    /**
     * @param spotCommunicationReceiveConnection
     * @param spotCommunicationSendConnection
     * @return 
     */
    public boolean openSpotCommunicationConnections(RadiogramConnection spotCommunicationReceiveConnection, RadiogramConnection spotCommunicationSendConnection) {
        
        try{
            spotCommunicationReceiveConnection = (RadiogramConnection) Connector.open("radiogram://:" + SPOT_COMMUNICATION_PORT);
            spotCommunicationSendConnection = (RadiogramConnection) Connector.open("radiogram://:" + SPOT_COMMUNICATION_PORT);
            return true;
        } catch(Exception e){
            System.err.println("Caught " + e + " in spot connection initialization.");
            //TODO: ver que é isto
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
            //TODO: ver que é isto
            notifyDestroyed();
            return false;
        }
    }
    
    public boolean receiveIdFromHost(RadiogramConnection hostReceiveConnection, RadiogramConnection hostSendConnection, Datagram dg){
        try {
            dg = hostReceiveConnection.newDatagram(hostReceiveConnection.getMaximumLength());
            short idFromHost = dg.readShort();
            this.setId(idFromHost);
            return true;
        } catch (Exception e) {
            // on catch talvez pedir de novo
            
            return false;
        }        
    }
    
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
        
    /** 
     * SetId
     * 
     * @param id
     */
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
