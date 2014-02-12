package org.sunspotworld.demo;

/**
 *
 * @author Pedro Marques da Silva <MSc Student @di.fct.unl.pt>
 */
public class FloodingMessage extends Message {

    private byte type;
    private short source;
    private short destin;
    private String data;

    /**
     *
     */
    public FloodingMessage() {
        super("NULL".getBytes());
    }

    /**
     * 
     * @param payload
     */
    public FloodingMessage(byte[] payload) {
        super(payload);
    }

    /**
     *
     * @return
     */
    public String getMessageData() {
        return data;
    }

    /**
     *
     * @param data
     */
    public void setMessageData(String data) {
        this.data = data;
    }

    /**
     *
     * @return
     */
    public byte getType() {
        return type;
    }

    /**
     *
     * @param type
     */
    public void setType(byte type) {
        this.type = type;
    }

}
