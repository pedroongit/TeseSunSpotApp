/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sunspotworld;

/**
 *
 * @author Pedro
 */
public class HelloApplication {

    protected void onMessageReceived(Object message) {
        Message m = (Message) message;
        //System.out.println("Node: " + getNode().getId() + "\n" + "Data: " + new String(m.getPayload()));
    }

    //parameter hardcoded. Corresponde ao nó target
    public void run(short parameter) {
        sendHelloTo(parameter);
    }

    /*private short askForParameter() {
        try {
            String result = JOptionPane.showInputDialog("Choose destination node ID");
            return Short.valueOf(result);

        } catch (Exception e) {
            Utilities.handleException(e);
        }
        return (short) 0;
    }*/

    void sendHelloTo(short id) {
        FloodingMessage m = new FloodingMessage("HELLO".getBytes());
        m.setType((byte) 1);
        
        short sourceId = 0;
        Short sourceIdshortObj = new Short(sourceId);
        m.setSourceId(sourceIdshortObj);
        short destinationId = id;
        Short destinationIdshortObj = new Short(destinationId);
        m.setDestinationId(destinationIdshortObj);
        m.setMessageData("HELLO");
        //sendMessage(m);
        //enviar datagrama tendo em conta "m"
    }
}

