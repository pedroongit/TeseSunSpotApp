/*
 * SunSpotHostApplication.java
 *
 * Created on Feb 15, 2014 12:31:08 PM;
 */

package org.sunspotworld;

import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.peripheral.radio.IRadioPolicyManager;
import com.sun.spot.io.j2me.radiostream.*;
import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.peripheral.ota.OTACommandServer;
import com.sun.spot.util.IEEEAddress;

import java.io.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.microedition.io.*;


/**
 * Sample Sun SPOT host application
 */
public class SunSpotHostApplication {
    // Broadcast port on which we listen for sensor samples
    private static final int HOST_PORT = 67;

    /**
     * Print out our radio address.
     */
    public void run() {
        RadiogramConnection sCon = null;
        Datagram dg = null;
        DateFormat fmt = DateFormat.getTimeInstance();
        Map<Short, String> idAddressMap = new HashMap<Short, String>();
        
        //2 spots existentes. TODO: em vez desta maneira, os spots enviam um ping so para que o host conheça o seu IEEE Address
        idAddressMap.put(new Short("1"), "0014.4F01.0000.6414");
        idAddressMap.put(new Short("2"), "0014.4F01.0000.612C");
        
        Iterator<Entry<Short,String>> idAddressIterator = idAddressMap.entrySet().iterator();
        while(idAddressIterator.hasNext()){
            try {
                Entry<Short,String> next = idAddressIterator.next();
                Short id = next.getKey();
                String ieeeAddress = next.getValue();
                
                // Open up a server-side broadcast radiogram connection
                // to listen for sensor readings being sent by different SPOTs
                sCon = (RadiogramConnection) Connector.open("radiogram://:" + ieeeAddress + HOST_PORT);
                dg = sCon.newDatagram(sCon.getMaximumLength());
            } catch (Exception e) {
                 System.err.println("setUp caught " + e.getMessage());
                 //throw e;
            }
        }
        /* Envio do id a cada Spot.
         * Numa fase inicial, address dos spots é hardcodded
         */
        
        try {

            dg.reset();
            dg.writeShort(now);
            sCon.send(dg);

            System.out.println(fmt.format(new Date(time)) + "  from: " + addr + "   value = " + val);
        } catch (Exception e) {
            System.err.println("Caught " + e +  " while reading sensor samples.");
            //throw e;
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
