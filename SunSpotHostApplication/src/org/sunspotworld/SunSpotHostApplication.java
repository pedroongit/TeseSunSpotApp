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
import java.util.Date;
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
        RadiogramConnection rCon = null;
        Datagram dg = null;
        DateFormat fmt = DateFormat.getTimeInstance();
         
        try {
            // Open up a server-side broadcast radiogram connection
            // to listen for sensor readings being sent by different SPOTs
            rCon = (RadiogramConnection) Connector.open("radiogram://:" + HOST_PORT);
            dg = rCon.newDatagram(rCon.getMaximumLength());
        } catch (Exception e) {
             System.err.println("setUp caught " + e.getMessage());
             //throw e;
        }

        // Main data collection loop
        while (true) {
            try {
                // Read sensor sample received over the radio
                rCon.receive(dg);
                String addr = dg.getAddress();  // read sender's Id
                long time = dg.readLong();      // read time of the reading
                int val = dg.readInt();         // read the sensor value
                System.out.println(fmt.format(new Date(time)) + "  from: " + addr + "   value = " + val);
            } catch (Exception e) {
                System.err.println("Caught " + e +  " while reading sensor samples.");
                //throw e;
            }
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
