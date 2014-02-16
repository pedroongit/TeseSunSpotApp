package org.sunspotworld;

/**
 *
 * @author modified by Pedro
 */
import java.util.Hashtable;

/**
 * @author Pedro Marques da Silva <MSc Student @di.fct.unl.pt>
 *
 */
public class FloodingRoutingLayer {

    //TODO: refactor..usar um array em vez desta estrutura
    protected Hashtable receivedMessages;
    private SunSpotApplication s;

    private static final int RECEIVE = 0;
    private static final int ROUTE = 1;
    private static final int DONOTHING = 2;

    public FloodingRoutingLayer() {
        super();
        this.receivedMessages = new Hashtable();

        s = new SunSpotApplication();
    }
    
    /**
     * The Spot receives a DatagramPacket and calls the onReceiveMessage
     * 
     * Stores the sender from which it first receives the message, and route
     * the message.
     */
    public int onReceiveMessage(Object message) {

        FloodingMessage msg = (FloodingMessage) message;
        //porque o contains nao aceita tipos primitios
        Long lObj = new Long(msg.getMessageNumber());
        
        if (!receivedMessages.contains(lObj)) {
            receivedMessages.put(lObj, lObj);
            //se entra no if significa que o no' e' o destino
            if (msg.getDestinationId().equals(new Long(0))) {//getUniqueId()
                //receiveMessage(msg); //imprime a mensagem?
                return RECEIVE; 
            } else {
                //routeMessage(msg);
                return ROUTE;
            }
        }
        return DONOTHING;
    }
    
    /**
     * envia para a MAC layer
     */
    public boolean onSendMessage(Object message) {

        //try {
                //Thread.sleep(500);
        FloodingMessage msg = (FloodingMessage) message;
        Long lObj = new Long(msg.getMessageNumber());
        receivedMessages.put(lObj, lObj);//add(((Message) message).getMessageNumber());
                //msg = (FloodingMessage) encapsulateMessage((Message) message);
                //send(msg);
        //} catch (InterruptedException ex) {
                //Thread.currentThread().interrupt();
        //}
        return true;
    }

    
    //penso que se pode apagar
    public void onRouteMessage(Object message) {
        /* send the message by flood*/
        //send(message);
    }

    public void sendMessageToAir(Object message) {
        //getNode().getMacLayer().sendMessage(message, FloodingRoutingLayer.this);
        //broadcast
    }

    /*
    public Object getUniqueId() {
        return getNode().getId();
    }*/
}
