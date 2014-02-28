/*
 * SunSpotApplication.java
 *
 * Created on Feb 16, 2014 9:55:51 PM;
 */

package org.sunspotworld;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.resources.transducers.ITriColorLEDArray;
import com.sun.spot.util.Utils;
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
    
    private static final String HOST_ADDRESS = "0014.4F01.0000.612C";
    
    private RadiogramConnection hostReceiveConnection = null;
//    private RadiogramConnection hostSendConnection = null;
    private RadiogramConnection rConMessage = null;
    //private RadiogramConnection spotCommunicationSendConnection = null;
    
    //Escolher que aplicaÃ§Ã£o correr. No futuro, se possivel criar set/get aplication que recebe/devolve a classe de aplicaÃ§Ã£o
//    private HelloApplication app = new HelloApplication();
    private FloodingRoutingLayer RoutingLayer = new FloodingRoutingLayer();
        
    /**
     * The id of the node. It is allowed that two nodes have the same id in the
     * simulator.
     */
    protected short id;
    
    protected void startApp() throws MIDletStateChangeException {
        // Listen for downloads/commands over USB connection
        new com.sun.spot.service.BootloaderListenerService().getInstance().start();

        // Recebe o seu id vindo do host. Enquanto não tiver recebido, continua a tentar.
        this.openHostConnections();
        this.receiveIdFromHost();

        this.openSpotCommunicationConnections();
        
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
                Datagram dgReceiveMessage = rConMessage.newDatagram(rConMessage.getMaximumLength());
                rConMessage.receive(dgReceiveMessage);
                
                long numberOfTotalMessage = dgReceiveMessage.readLong();
                long messageNumber = dgReceiveMessage.readLong();
                byte[] b = "somebytes".getBytes();//null;
//                dgReceiveMessage.readFully(b);
                byte[] payload = b;
                long totalHops = dgReceiveMessage.readLong();
                Short sourceId = new Short(dgReceiveMessage.readShort());
                Short destinationId = new Short(dgReceiveMessage.readShort());
                Short uniqueId = new Short(dgReceiveMessage.readShort());
                Message m = new Message(numberOfTotalMessage, messageNumber, payload, totalHops, sourceId, destinationId, uniqueId);
                
                int result = RoutingLayer.onReceiveMessage(m);
                switch(result){
                    case RECEIVE:
//                        app.onMessageReceived(m);
                        break;
                    case ROUTE:
                        //m.hop();
//                        layer.onRouteMessage(m);
                        //broadcast(m);
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
     * Abre conexao com o Host para posteriormente receber id
     * @return 
     */
    public boolean openHostConnections(){
        try{
            hostReceiveConnection = (RadiogramConnection) Connector.open("radiogram://:" + HOST_PORT);
//            hostSendConnection = (RadiogramConnection) Connector.open("radiogram://"+HOST_ADDRESS+":"+HOST_PORT);
            return true;
        } catch(Exception e){
            System.err.println("Caught " + e + " in host connection initialization.");
            //Penso que isto faz com que termine a aplicacao
            //notifyDestroyed();
            return false;
        }
    }
    
    /**
     * Recebe id vindo do Host
     */
    public void receiveIdFromHost(){
        try {
            //recebe id
            Datagram dgId = hostReceiveConnection.newDatagram(hostReceiveConnection.getMaximumLength());
            //bloqueia ate' receber datagram do host
            hostReceiveConnection.receive(dgId);
            short idFromHost = dgId.readShort();
            dgId.reset();
            this.setId(idFromHost);
//            Datagram dgAck = hostSendConnection.newDatagram(hostSendConnection.getMaximumLength());
//            dgAck.writeBoolean(true);
//            hostSendConnection.send(dgAck);
//            dgAck.reset();
        } catch (Exception e) {
            // on catch talvez pedir de novo
        }
    }
    
    /**
     * @return 
     */
    public boolean openSpotCommunicationConnections() {
        
        try{
            rConMessage = (RadiogramConnection) Connector.open("radiogram://:" + SPOT_COMMUNICATION_PORT);
            //spotCommunicationSendConnection = (RadiogramConnection) Connector.open("radiogram://:" + SPOT_COMMUNICATION_PORT);
            return true;
        } catch(Exception e){
            System.err.println("Caught " + e + " in spot connection initialization.");
            //notifyDestroyed();
            return false;
        }
    }
    
/*    public void broadcast(Object message){
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
        
    }*/
    
    /***************************************
     *       end communication methods     *
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
        RoutingLayer.setUniqueId(id);
    }
    
    public short getId() {
        return id;
    }
    
    /***************************************
     *       end auxiliary methods         *
     ***************************************/
    
    protected void pauseApp() {
        // This will never be called by the Squawk VM
    }
    
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        // Only called if startApp throws any exception other than MIDletStateChangeException
    }
    
}
