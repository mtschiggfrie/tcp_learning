import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by MatthewT on 3/30/2017.
 */
public class Network {

    private NetworkHardware client;
    private List<NetworkHardware> routerList;
    private NetworkHardware server;
    private List<Packet> packetListToSend;

    private NetworkGraphics networkGraphics;
    private int sequenceNumber;
    private int numberOfRouters;
    private int numberOfPacketsInBurst;
    private String message;

    public Network() {
        init();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Run Network");
                runNetwork();
            }
        }, 0, 1000);
    }


    private void init() {
        numberOfRouters = 3;
        sequenceNumber = 0;
        numberOfPacketsInBurst = 1;

        routerList = new ArrayList<>();
        initRouterList();
        client = new NetworkHardware(0,20,0,0);
        server = new NetworkHardware(numberOfRouters + 1,20,0,0);

        networkGraphics = new NetworkGraphics(numberOfRouters, routerList, client, server);
        message = networkGraphics.getMessageToSend();

        packetListToSend = new ArrayList<Packet>();
        updateMessage(message);
    }

    private void initRouterList() {
        routerList.clear();
        for (int i = 0; i < numberOfRouters; ++i) {
            routerList.add(new NetworkHardware(1 + i,20,0,0.0));
        }
    }

    private void updateMessage(String newMessage) {
        packetListToSend.clear();
        String tmpArray[] = newMessage.split(" ");
        for (int i = 0; i < tmpArray.length; ++i) {
            packetListToSend.add(new Packet(i, tmpArray[i], 0, 1));
        }
        sequenceNumber = 0;
    }

    private void updateRouterList(ArrayList<Double> dropRatesList) {
        if (dropRatesList.size() == numberOfRouters) {
            routerList.clear();
            for (int i = 0; i < numberOfRouters; ++i) {
                routerList.add(new NetworkHardware(1 + i, 20, dropRatesList.get(i), 0.0));
            }
        }
        else {
            routerList.clear();
            initRouterList();
        }
    }

    private void runNetwork() {
        List<Packet> packetsToPaint = new ArrayList<>();
        int amountOfPacketsSent = 0;

        for (int i = 0; i < numberOfPacketsInBurst; ++i) {
            if (packetListToSend.size() - i == 0) {
                break;
            }
            else {
                Packet originalPacket = packetListToSend.get(i);
                client.addPacketToBuffer(originalPacket, 1);
                Packet packetClientSending = client.getPacketToSend();
                packetsToPaint.add(packetClientSending);
                amountOfPacketsSent++;
            }
        }

        for (int j = 0; j < numberOfRouters; ++j) {
            NetworkHardware router = routerList.get(j);
            for (int i = j * amountOfPacketsSent; i < amountOfPacketsSent * (j + 1); ++i) {
                router.addPacketToBuffer(packetsToPaint.get(i), 2 + j);
                Packet packetRouterSending = router.getPacketToSend();
                packetsToPaint.add(packetRouterSending);
            }
        }

        for (int i = amountOfPacketsSent * numberOfRouters; i < amountOfPacketsSent * (1 + numberOfRouters); ++i) {
            server.addPacketToBuffer(packetsToPaint.get(i), numberOfRouters);
            Packet packetServerSendingBack = server.getACKPacketToSend();
            packetsToPaint.add(packetServerSendingBack);
        }

        for (int j = 0; j < numberOfRouters; ++j) {
            int currentRouterNumber = numberOfRouters - 1 - j;
            NetworkHardware router = routerList.get(currentRouterNumber);
            for (int i = amountOfPacketsSent * (1 + numberOfRouters + j); i < amountOfPacketsSent * (2 + numberOfRouters + j); ++i) {
                router.addPacketToBuffer(packetsToPaint.get(i), currentRouterNumber);
                Packet packetRouterSendingBack = router.getPacketToSend();
                packetsToPaint.add(packetRouterSendingBack);
            }
        }

        int numberOfPacketsRecievedSuccessfully = 0;
        if (numberOfRouters > 0 ) {
            for (int i = amountOfPacketsSent * (2 + numberOfRouters); i < amountOfPacketsSent * (3 + numberOfRouters); ++i) {
                Packet curPack = packetsToPaint.get(i);
                if (curPack.getSequenceNumber() == sequenceNumber + 1 && !curPack.isDropped() && !curPack.isCorrupt()) {
                    sequenceNumber++;
                    numberOfPacketsRecievedSuccessfully++;
                    packetListToSend.remove(0);
                }
            }
        }
        else {
            for (int i = amountOfPacketsSent; i < amountOfPacketsSent * 2; ++i) {
                Packet curPack = packetsToPaint.get(i);
                if (curPack.getSequenceNumber() == sequenceNumber + 1 && !curPack.isDropped() && !curPack.isCorrupt()) {
                    sequenceNumber++;
                    numberOfPacketsRecievedSuccessfully++;
                    packetListToSend.remove(0);
                }
            }
        }
        if (numberOfPacketsRecievedSuccessfully == numberOfPacketsInBurst) {
            numberOfPacketsInBurst *= 2;
        }
        else {
            numberOfPacketsInBurst = numberOfPacketsInBurst / 2;
            if (numberOfPacketsInBurst == 0) {
                numberOfPacketsInBurst = 1;
            }
        }

        boolean changesToGraphics = networkGraphics.displayPackets(packetsToPaint);
        if (changesToGraphics) {
            numberOfRouters = networkGraphics.getUpdatedNumberOfRouters();
            server.updateNumberOfRouters(numberOfRouters);
            networkGraphics.initRouterLabels();
            updateRouterList(networkGraphics.getRouterDropRates());
            networkGraphics.initRouterButtons(routerList);
            networkGraphics.initConnectionSpeeds();
        }

        if (amountOfPacketsSent == 0) {
            changesToGraphics = networkGraphics.displayPackets(packetsToPaint);
            if (changesToGraphics) {
                numberOfRouters = networkGraphics.getUpdatedNumberOfRouters();
                server.updateNumberOfRouters(numberOfRouters);
                networkGraphics.initRouterLabels();
                updateRouterList(networkGraphics.getRouterDropRates());
                networkGraphics.initRouterButtons(routerList);
            }
            if (networkGraphics.isSendNewMessage()) {
                String newMessage = networkGraphics.getMessageToSend();
                updateMessage(newMessage);
                server.resetSeqNum();
            }
        }

    }

}
