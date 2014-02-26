/*
 * SunSpotHostApplication.java
 *
 * Created on Feb 15, 2014 12:31:08 PM;
 */

package org.sunspotworld;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.peripheral.ota.OTACommandServer;
import com.sun.spot.util.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.microedition.io.*;


/**
 * Sample Sun SPOT host application
 */
public class SunSpotHostApplication {
    // Broadcast port on which we listen for sensor samples
    private static final int HOST_PORT = 67;
    private static final int SPOT_COMMUNICATION_PORT = 43;

    /**
     * Print out our radio address.
     */
    public void run() {
        RadiogramConnection sConSendId = null;
//        RadiogramConnection rAckCon = null;
//        try {
//            rAckCon = (RadiogramConnection) Connector.open("radiogram://:" + HOST_PORT);
//        } catch (Exception e) {}
        
        RadiogramConnection sConBroadcastMessage = null;
        Datagram dg = null;
        Map<Short, String> idAddressMap = new HashMap<Short, String>();
//      Map<Short, RadiogramConnection> idConnectionMap = new HashMap<Short, RadiogramConnection>();
        
        //2 spots existentes. Numa fase inicial, address dos spots é hardcodded TODO: em vez desta maneira, os spots enviam um ping so para que o host conheça o seu IEEE Address
        idAddressMap.put(new Short("2"), "0014.4F01.0000.6414");
        //idAddressMap.put(new Short("1"), "0014.4F01.0000.612C");
        
        Iterator<Entry<Short,String>> idAddressIterator = idAddressMap.entrySet().iterator();
        while(idAddressIterator.hasNext()){
            try {
                Entry<Short,String> next = idAddressIterator.next();
                System.out.println("iterating on " + next.getValue());
                Short id = next.getKey();
                String ieeeAddress = next.getValue();

                // Abre conexao para o Spot corrente
                sConSendId = (RadiogramConnection) Connector.open("radiogram://" + ieeeAddress + ":"+HOST_PORT);

                // Escreve e envia o datagrama
                dg = sConSendId.newDatagram(sConSendId.getMaximumLength());
                dg.writeShort(id);

//                Datagram dgAck = rAckCon.newDatagram(rAckCon.getMaximumLength());
                boolean received = false;
                while(!received) {
                    try {
                        System.out.println("Sending id and trying to receive ack");
                        sConSendId.send(dg);
                        received = true;
//                        //receber ACK timeout 10 segundos
//                        rAckCon.setTimeout(30*1000);
//                        rAckCon.receive(dgAck);
//                        received = dgAck.readBoolean();
                        System.out.println("Sent");
                    } catch(Exception e){
                        System.err.println("Caugth exception: " + e.getMessage());
                    }
                }
//                dgAck.reset();
                dg.reset();
            } catch (Exception e) {
                 System.err.println("setUp caught " + e.getMessage());
                 //throw e;
            }
        }
        
        try{
            System.out.println("Broadcasting Messages!");
            sConBroadcastMessage = (RadiogramConnection) Connector.open("radiogram://broadcast:" + SPOT_COMMUNICATION_PORT);
            System.out.println("Broadcast Connection opened");
        } catch(Exception e){
            System.err.println("Caught " + e + " in spot broadcast connection initialization.");
        }
        
        /*
         * Ciclo que envia mensagens para os Spots.
         * No futuro receberá mensagens vindas do simulador em vez de criar mensagens random
         */
        while(true) {
            System.out.println("Entered the send message cicle");
            // dummy message values
            long numberOfTotalMessage = (long)(Math.random() * 10);
            long messageNumber = (long)(Math.random() * 10);
            //byte[] payload = "Mensage".getBytes();
            long totalHops = (long)(Math.random() * 10);
            Short sourceId = new Short((short)(Math.random() * 10));
            Short destinationId = new Short((short)(Math.random() * 10));
            Short uniqueId = new Short((short)(Math.random() * 100));
            
            Datagram dgMessageBroadcast = null;
            try {
                dgMessageBroadcast = sConBroadcastMessage.newDatagram(sConBroadcastMessage.getMaximumLength());
                
                dgMessageBroadcast.writeLong(numberOfTotalMessage);
                dgMessageBroadcast.writeLong(messageNumber);
                //dg.write(payload);
                dgMessageBroadcast.writeLong(totalHops);
                dgMessageBroadcast.writeShort(sourceId);
                dgMessageBroadcast.writeShort(destinationId);
                dgMessageBroadcast.writeShort(uniqueId);
            }catch(Exception e){
                //throw e;
                System.out.println("Exception writing dg: " + e);
            }
            
            try {
                System.out.println("Started broadcasting");
                sConBroadcastMessage.send(dgMessageBroadcast);
                System.out.println("Finished broadcasting");
            
                System.out.println("Broadcasted message: "+" numberOfTotalMessage: "+ numberOfTotalMessage
                        +" messageNumber: "+ messageNumber+/*" payload: "+ payload +*/" totalHops: "+ totalHops
                        +" sourceId: "+ sourceId +" destinationId: "+ destinationId +" uniqueId: "+ uniqueId);
            }catch(Exception e) {
                //throw e;
                System.out.print("Exception sending dg: " + e);
            }
            
            // sleep 10 segundos
            Utils.sleep(10*1000);
        }
                
    }

    /**
     * Start up the host application.
     *
     * @param args any command line arguments
     */
    public static void main(String[] args) {
        // register the application's name with the OTA Command server & start OTA running
        OTACommandServer.start("SunSpotHostApplication");
        SunSpotHostApplication app = new SunSpotHostApplication();
        app.run();
    }
}
