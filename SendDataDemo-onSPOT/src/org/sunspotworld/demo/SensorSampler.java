package org.sunspotworld.demo;

import com.sun.spot.io.j2me.radiogram.*;
//import com.sun.spot.resources.Resources;
//import com.sun.spot.resources.transducers.ITriColorLED;
//import com.sun.spot.resources.transducers.ILightSensor;
//import com.sun.spot.util.Utils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
//import java.io.ObjectInput;
//import java.io.ObjectInputStream;
import java.util.Hashtable;
//import java.util.Iterator;
//import java.util.Map.Entry;
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
    
    private static final int RECEIVE = 0;
    private static final int ROUTE = 1;
    private static final int DONOTHING = 2;
    
    //Escolher que aplicação correr. No futuro, se possivel criar set/get aplication que recebe/devolve a classe de aplicação
    private HelloApplication app = new HelloApplication();
    private FloodingRoutingLayer layer = new FloodingRoutingLayer();
    
    //tabela com correspondência entre id no simulador (virtual) e o port dos nós reais
    private Hashtable portTable;
    private Hashtable connections;
    private int port;
    
    /**
     * The id of the node. It is allowed that two nodes have the same id in the
     * simulator.
     */
    protected short id;
    private boolean portTableReceived = false;
    
    protected void startApp() throws MIDletStateChangeException {
        RadiogramConnection rCon = null;
        Datagram dg = null;
        //String ourAddress = System.getProperty("IEEE_ADDRESS");
        //System.out.println("Starting sensor sampler application on " + ourAddress + " ...");

    // Listen for downloads/commands over USB connection
    new com.sun.spot.service.BootloaderListenerService().getInstance().start();

        try {
            rCon = (RadiogramConnection) Connector.open("radiogram://:" + HOST_PORT);
            dg = rCon.newDatagram(rCon.getMaximumLength());
            
        } catch (Exception e) {
            System.err.println("Caught " + e + " in connection initialization.");
            notifyDestroyed();
        }
        
        //recever ports do host
        this.receivePorts(rCon, dg);
        //set port deste spot
        
        Short sId = new Short(getId());
        setPort(portTable.get(sId));
        
        this.openConnections();
        
        while (true) {
            try {
                rCon.receive(dg);
                //inicializar como deve ser?
                byte[] b = null;
                dg.readFully(b);
                
                Message m = (Message) bytesToObject(b);
                
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
    
    //receber a tabela de ports
    public void receivePorts(RadiogramConnection rCon, Datagram dg) {
        while(!portTableReceived) {
            try{
                rCon.receive(dg);
                //inicializar como deve ser?
                byte[] b = null;
                dg.readFully(b);
                this.setPortTable((Hashtable) bytesToObject(b));
                
                portTableReceived = true;
            } catch(Exception e) {
                System.err.println("Caught " + e + " while connecting to host to receive Port Table.");
            }
        }
    }
    
    //abre conexoes para cada outro spot
    public void openConnections(){
        
        Iterator<Entry<Short, Integer>> portsIt = portTable.entrySet().iterator();
        short thisID = this.getId();
        while(portsIt.hasNext()){
            
            RadiogramConnection newCon = null;
            short currentID = portsIt.next().getKey();
            //não abrir conexão para si mesmo
            if(thisID != currentID){
               
                int currentPort = portsIt.next().getValue();
                try {
                    newCon=  (RadiogramConnection) Connector.open("radiogram://:" + currentPort);
                    connections.put(currentID, newCon);

                } catch (Exception e) {
                    System.err.println("Caught " + e + " in connection initialization.");
                    notifyDestroyed();
                }
            }
            
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
            connection = (DatagramConnection) Connector.open("radiogram://broadcast:" + getPort());
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
    
    // convert byte[] to object (desserializar)
    public Object bytesToObject(byte[] bytes) throws ClassNotFoundException, IOException{
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            Object o = in.readObject();
            return o;
        } finally {
            try {
                bis.close();
            } catch (IOException ex) {
            // ignore close exception
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
            // ignore close exception
            }
        }
    }
    
    public final void setId(short id) {
        this.id = id;
    }
    
    public short getId() {
        return id;
    }

    public Hashtable getPortTable() {
        return portTable;
    }

   public void setPortTable(Hashtable portTable) {
        this.portTable = portTable;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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
