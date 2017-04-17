/**
 * Created by MatthewT on 3/30/2017.
 */
public class Packet {

    private int sequenceNumber;
    private String message;
    private boolean isDropped;
    private boolean isCorrupt;

    private int fromHardwareNumber;
    private int toHardwareNumber;

    private boolean isReadyToSend;
    private int packetLocation;
    private int packetFinalLocation;

    private int speed;
    private boolean complete;
    private boolean directionToRight;


    public Packet(int sequenceNumber, String message, int fromHardwareNumber, int toHardwareNumber) {
        this.sequenceNumber = sequenceNumber;
        this.message = message;
        this.fromHardwareNumber = fromHardwareNumber;
        this.toHardwareNumber = toHardwareNumber;

        isCorrupt = false;
        isDropped = false;
        isReadyToSend = false;

        packetLocation = 0;

        speed = 10;
        complete = false;
        directionToRight = true;
    }

    public int getStepSize() {
        return speed;
    }

    public void setSpeed(int x) {
        speed = x;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void corruptPacket() {
        if (!isDropped) {
            isCorrupt = true;
        }
    }

    public void dropPacket() {
        if (!isCorrupt) {
            isDropped = true;
        }
    }

    public void setFromHardwareNumber(int i) {
        fromHardwareNumber = i;
    }

    public void setToHardwareNumber(int i) {
        toHardwareNumber = i;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getMessage() {
        return message;
    }

    public boolean isDropped() {
        return isDropped;
    }

    public boolean isCorrupt() {
        return isCorrupt;
    }

    public int getFromHardwareNumber() {
        return fromHardwareNumber;
    }

    public int getToHardwareNumber() {
        return toHardwareNumber;
    }

    public void setPacketToReady() {
        isReadyToSend = true;
    }

    public boolean isReadyToSend() {
        return isReadyToSend;
    }

    public boolean isDirectionToRight() {
        return directionToRight;
    }

    public void setPacketFinalLocation(int packetFinalLocation) {
        this.packetFinalLocation = packetFinalLocation;
        if (packetFinalLocation - packetLocation > 0) {
            directionToRight = true;
        }
        else {
            directionToRight = false;
        }
    }

    public int getPacketFinalLocation() {
        return packetFinalLocation;
    }

    public void setPacketLocation(int packetLocation) {
        this.packetLocation = packetLocation;
    }

    public int getPacketLocation() {
        return packetLocation;
    }

    public void setPacketToComplete() {
        isReadyToSend = false;
        complete = true;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isPacketSent() {
        return complete;
    }

    public void printPacket() {
        System.out.println(sequenceNumber + ", " + message + ", " + packetLocation + ", " + packetFinalLocation);
    }

}
