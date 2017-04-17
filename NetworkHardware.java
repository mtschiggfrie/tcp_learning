import java.util.ArrayList;
import java.util.List;

/**
 * Created by MatthewT on 3/30/2017.
 */
public class NetworkHardware {

    private int hardwareNumber;
    private int bufferSize;
    private int sequenceNumber;

    private double dropRate;
    private double corruptRate;

    private List<Packet> packetList;

    public NetworkHardware(int hardwareNumber, int bufferSize, double dropRate, double corruptRate) {
        this.hardwareNumber = hardwareNumber;
        this.bufferSize = bufferSize;
        this.dropRate = dropRate;
        this.corruptRate = corruptRate;
        packetList = new ArrayList<>();
        sequenceNumber = 0;
    }

    private Packet checkToDropOrCorruptPacket(Packet packet) {
        double randomNumber = Math.random();
        if (packet.isCorrupt()) {
            return packet;
        }
        if (packet.isDropped()) {
            return packet;
        }
        if (randomNumber <= dropRate) {
            packet.dropPacket();
        }
        else if (randomNumber <= dropRate + corruptRate) {
            packet.corruptPacket();
        }
        return packet;
    }

    public void addPacketToBuffer(Packet packet, int sendToHardwareNumber) {
        Packet newPacket = new Packet(packet.getSequenceNumber(), packet.getMessage(), hardwareNumber, sendToHardwareNumber);
        if (packet.isCorrupt()) {
            newPacket.corruptPacket();
        }
        if (packet.isDropped()) {
            newPacket.dropPacket();
        }
        if (packetList.size() < bufferSize) {
            newPacket = checkToDropOrCorruptPacket(newPacket);
            newPacket.setFromHardwareNumber(hardwareNumber);
            newPacket.setToHardwareNumber(sendToHardwareNumber);
            packetList.add(newPacket);
        }
    }

    public Packet getACKPacketToSend() {
        Packet packetToReturn;
        if (packetList.size() > 0) {
            packetToReturn = packetList.remove(0);
            if (packetToReturn.getSequenceNumber() == sequenceNumber && !packetToReturn.isDropped() && !packetToReturn.isCorrupt()) {
                packetToReturn = new Packet(sequenceNumber + 1, "ACK", hardwareNumber, packetToReturn.getToHardwareNumber());
                sequenceNumber++;

            }
            else {

                packetToReturn = new Packet(sequenceNumber, "RESEND", hardwareNumber, packetToReturn.getToHardwareNumber());
            }
        }
        else {
            packetToReturn = new Packet(-2,"DEL",-2,-2);
        }
        return packetToReturn;
    }

    public Packet getPacketToSend() {
        Packet packetToReturn;
        if (packetList.size() > 0) {
            packetToReturn = packetList.remove(0);
        }
        else {
            packetToReturn = new Packet(-2,"DEL",-2,-2);
        }
        return packetToReturn;
    }

    public void updateNumberOfRouters(int n) {
        hardwareNumber = n + 1;
    }

    public String getDropRate() {
        return Integer.toString((int) (dropRate * 100));
    }

    public void incDropRate() {
        if (dropRate < 1) {
            dropRate += 0.01;
        }
    }

    public void decDropRate() {
        if (dropRate > 0) {
            dropRate -= 0.01;
        }
    }

    public void incCorruptRate() {
        if (corruptRate < 1) {
            corruptRate += 0.01;
        }
    }

    public void decCorruptRate() {
        if (corruptRate > 0) {
            corruptRate -= 0.01;
        }
    }

    public void resetSeqNum() {
        sequenceNumber = 0;
    }

    public String getCorruptRate() {
        return Integer.toString((int) (corruptRate * 100));
    }

}
