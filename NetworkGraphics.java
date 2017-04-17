import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class NetworkGraphics {

    private JFrame f;
    private List<JLabel> routerLabelList;
    private List<JLabel> routerImageLabelList;
    private List<JTextField> connectionSpeedTexts;
    private List<JLabel> connectionSpeedLabels;
    private JLabel clientLabel;
    private JLabel serverLabel;
    private JTextField messageTextBox;
    private String messageToSend;

    private List<JLabel> routerAddDropButtonList;
    private List<JLabel> routerSubDropButtonList;
    private List<JLabel> routerDropButtonLabelList;
    private List<JLabel> routerAddCorruptButtonList;
    private List<JLabel> routerSubCorruptButtonList;
    private List<JLabel> routerCorruptButtonLabelList;

    private int clientLocation;
    private int serverLocation;
    private int stepSize;
    private int packetLocation;
    private int startPacketLocation;
    private int endPacketLocation;
    private int numberOfRouters;
    private List<NetworkHardware> routerList;
    private NetworkHardware client;
    private NetworkHardware server;

    private boolean sendNewMessage;

    private int frameWidth;
    private int frameHeight;
    private int hardwareWidth;
    private int hardwareHeight;

    private int numberOfPackets;
    private boolean changesToClass;
    private int nextNumberOfRouters;


    public NetworkGraphics(int numberOfRouters, List<NetworkHardware> routerList, NetworkHardware client, NetworkHardware server) {
        f = new JFrame("Network Simulation");
        this.numberOfRouters = numberOfRouters;
        nextNumberOfRouters = numberOfRouters;
        this.routerList = routerList;
        this.client = client;
        this.server = server;
        messageToSend = "This is a sample message to send through the network.";
        frameWidth = 1300;
        frameHeight = 700;
        hardwareHeight = 30;
        hardwareWidth = 100;
        clientLocation = 100;
        serverLocation = frameWidth - 150;
        startPacketLocation = clientLocation + 50;
        endPacketLocation = serverLocation - 50;
        packetLocation = startPacketLocation;
        stepSize = (serverLocation - packetLocation - 50) / (10 * (numberOfRouters + 1));
        routerLabelList = new LinkedList<JLabel>();
        routerAddDropButtonList = new LinkedList<>();
        routerSubDropButtonList = new LinkedList<>();
        routerDropButtonLabelList = new LinkedList<>();
        routerAddCorruptButtonList = new LinkedList<>();
        routerSubCorruptButtonList = new LinkedList<>();
        routerCorruptButtonLabelList = new LinkedList<>();
        connectionSpeedTexts = new LinkedList<>();
        connectionSpeedLabels = new LinkedList<>();
        routerImageLabelList = new LinkedList<>();
        sendNewMessage = false;
        setupGraphics();
    }

    public boolean displayPackets(List<Packet> listOfPackets) {
        numberOfPackets = listOfPackets.size();
        for (int i = 0; i < listOfPackets.size(); ++i) {
            if (listOfPackets.get(i).getFromHardwareNumber() == 0) {
                listOfPackets.get(i).setPacketToReady();
            }
            int stepSize = ((endPacketLocation - startPacketLocation) / (numberOfRouters + 1));
            int newPacketStartingLocation = (stepSize * listOfPackets.get(i).getFromHardwareNumber()) + startPacketLocation;
            listOfPackets.get(i).setPacketLocation(newPacketStartingLocation);
            if (listOfPackets.get(i).getFromHardwareNumber() < listOfPackets.get(i).getToHardwareNumber()) {
                listOfPackets.get(i).setPacketFinalLocation(stepSize + newPacketStartingLocation);
            }
            else {
                listOfPackets.get(i).setPacketFinalLocation(newPacketStartingLocation - stepSize);
            }
            listOfPackets.get(i).printPacket();
        }
        //f = new JFrame("Network Simulation");
        //setupGraphics();

        boolean cont = true;
        while (cont) {
            listOfPackets = displayPacketsRecursive(listOfPackets);
            if (listOfPackets.size() == 0) {
                cont = false;
            }
            else if (listOfPackets.get(listOfPackets.size() - 1).isPacketSent()) {
                cont = false;
            }
        }
        return changesToClass;
    }

    public List<Packet> displayPacketsRecursive(List<Packet> listOfPackets) {
        List<Integer> packetsReadyToBeDisplayedIndex = new ArrayList<Integer>();
        for (int i = 0; i < listOfPackets.size(); ++i) {
            //listOfPackets.get(i).printPacket();
            boolean setNextPacketToReady = false;
            if (listOfPackets.get(i).getPacketLocation() >= listOfPackets.get(i).getPacketFinalLocation() && listOfPackets.get(i).isDirectionToRight()) {
                if (!listOfPackets.get(i).isComplete()) {
                    setNextPacketToReady = true;
                }
                listOfPackets.get(i).setPacketToComplete();
            }
            if (listOfPackets.get(i).getPacketLocation() <= listOfPackets.get(i).getPacketFinalLocation() && !listOfPackets.get(i).isDirectionToRight()) {
                if (!listOfPackets.get(i).isComplete()) {
                    setNextPacketToReady = true;
                }
                listOfPackets.get(i).setPacketToComplete();
            }
            if (setNextPacketToReady) {
                if (listOfPackets.size() - i > 1) {
                    for (int j = i + 1; j < listOfPackets.size(); ++j) {
                        Packet currentPacket = listOfPackets.get(j);
                        if (!currentPacket.isComplete() && !currentPacket.isReadyToSend()) {
                            listOfPackets.get(j).setPacketToReady();
                            j = listOfPackets.size();
                        }
                    }
                }
            }
            if (listOfPackets.get(i).isReadyToSend() && !listOfPackets.get(i).isComplete() ) {
                packetsReadyToBeDisplayedIndex.add(i);
            }
        }


        List<JLabel> sequenceNumberLabelList = new ArrayList<>();
        List<JLabel> messageLabelList = new ArrayList<>();
        List<JLabel> packetLabelList = new ArrayList<>();

        for (int i = 0; i < packetsReadyToBeDisplayedIndex.size(); ++i) {
            int index = packetsReadyToBeDisplayedIndex.get(i);
            Packet packet = listOfPackets.get(index);
            sequenceNumberLabelList.add(new JLabel(String.valueOf(packet.getSequenceNumber())));
            messageLabelList.add(new JLabel(packet.getMessage()));
            ImageIcon packetIcon = getImageIcon(packet);
            packetLabelList.add(new JLabel("PACKET", packetIcon, JLabel.CENTER));
        }

        int yLocation = frameHeight / 3 - 20;
        for (int i = 0; i < packetsReadyToBeDisplayedIndex.size(); ++i) {
            int index = packetsReadyToBeDisplayedIndex.get(i);
            Packet currentPacket = listOfPackets.get(index);
            setPacketConnectionSpeed(currentPacket);
            boolean skipStep = false;
            if (i >= 1) {
                Packet previousPacket = listOfPackets.get(packetsReadyToBeDisplayedIndex.get(i-1));
                if (currentPacket.isDirectionToRight() && previousPacket.isDirectionToRight() && currentPacket.getFromHardwareNumber() == 0) {
                    if (previousPacket.getPacketLocation() - currentPacket.getPacketLocation() < 100) {
                        skipStep = true;
                    }
                }
            }
            if (!skipStep) {
                int currentPacketLocation = currentPacket.getPacketLocation();
                sequenceNumberLabelList.get(i).setBounds(currentPacketLocation, yLocation - 20, 50, 50);
                packetLabelList.get(i).setBounds(currentPacketLocation, yLocation, 50, 50);
                messageLabelList.get(i).setBounds(currentPacketLocation, yLocation + 50, 100, 15);
                //System.out.print(index);
                if (index >= (numberOfPackets / 2)) {
                    listOfPackets.get(index).setPacketLocation(currentPacketLocation - currentPacket.getStepSize());
                } else {
                    listOfPackets.get(index).setPacketLocation(currentPacketLocation + currentPacket.getStepSize());
                }
                //System.out.println("DISPLAY");
                f.add(sequenceNumberLabelList.get(i));
                f.add(packetLabelList.get(i));
                f.add(messageLabelList.get(i));
            }

        }
        f.revalidate();
        f.repaint();

        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {

        }
        for (int i = 0; i < packetsReadyToBeDisplayedIndex.size(); ++i) {
            f.remove(sequenceNumberLabelList.get(i));
            f.remove(packetLabelList.get(i));
            f.remove(messageLabelList.get(i));
        }

        return listOfPackets;
    }



    private ImageIcon getImageIcon(Packet packet) {
        ImageIcon packetIcon;
        //System.out.println("Packet is corrupt: " + packet.isCorrupt() + ". Packet is dropped: " + packet.isDropped());
        if (packet.isCorrupt()) {
            packetIcon = new ImageIcon("C:/Users/MatthewT/IdeaProjects/Network/images/redFolder.png");
        }
        else if (packet.isDropped()) {
            packetIcon = new ImageIcon("C:/Users/MatthewT/IdeaProjects/Network/images/blackFolder.png");
        }
        else {
            packetIcon = new ImageIcon("C:/Users/MatthewT/IdeaProjects/Network/images/greenFolder.png");
        }
        return packetIcon;
    }

    public void setupGraphics() {
        changesToClass = false;
        ImageIcon addImage = scaleDownImage2(new ImageIcon("C:/Users/MatthewT/IdeaProjects/Network/images/add.png"));
        ImageIcon subImage = scaleDownImage2(new ImageIcon("C:/Users/MatthewT/IdeaProjects/Network/images/sub.png"));

        JLabel addRouterButton = (new JLabel("", addImage, JLabel.CENTER));
        addRouterButton.setBounds(150,20,60,60);
        JLabel subRouterButton = (new JLabel("", subImage, JLabel.CENTER));
        subRouterButton.setBounds(320,20,60,60);

        JLabel routerAddSubLabel = new JLabel(numberOfRouters + " Routers");
        routerAddSubLabel.setFont(new Font("Serif", Font.BOLD, 18));
        routerAddSubLabel.setBounds(237,25,80,40);

        addRouterButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (nextNumberOfRouters < 10) {
                    nextNumberOfRouters = nextNumberOfRouters + 1;
                    routerAddSubLabel.setText(nextNumberOfRouters + " Routers");
                    changesToClass = true;
                }
            }
        });
        subRouterButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (nextNumberOfRouters > 0) {
                    nextNumberOfRouters = nextNumberOfRouters - 1;
                    routerAddSubLabel.setText(nextNumberOfRouters + " Routers");
                    changesToClass = true;
                }
            }
        });

        f.add(addRouterButton);
        f.add(subRouterButton);
        f.add(routerAddSubLabel);

        initServerClientLabels();
        initRouterLabels();
        initClientServerButtons();
        initRouterButtons(routerList);
        initMessageBox();
        initConnectionSpeeds();

        f.setSize(frameWidth,frameHeight - 100);
        f.getContentPane().setBackground(Color.CYAN);
        f.setLayout(null);
        f.setVisible(true);

    }

    public void initServerClientLabels() {
        clientLabel = new JLabel("Client");
        clientLabel.setFont(new Font("Serif", Font.BOLD, 26));
        clientLabel.setBounds(clientLocation,frameHeight / 3 + 60, hardwareWidth, hardwareHeight);
        ImageIcon clientImage = scaleDownImage(new ImageIcon("C:/Users/MatthewT/IdeaProjects/Network/images/computer.png"));
        JLabel clientImageLabel = (new JLabel("", clientImage, JLabel.CENTER));
        clientImageLabel.setBounds(clientLocation - 20, frameHeight / 3 - 50, 120, 120);

        serverLabel = new JLabel("Server");
        serverLabel.setFont(new Font("Serif", Font.BOLD, 26));
        serverLabel.setBounds(serverLocation,frameHeight / 3 + 60, hardwareWidth, hardwareHeight);
        ImageIcon serverImage = scaleDownImage(new ImageIcon("C:/Users/MatthewT/IdeaProjects/Network/images/server.png"));
        JLabel serverImageLabel = (new JLabel("", serverImage, JLabel.CENTER));
        serverImageLabel.setBounds(serverLocation - 20, frameHeight / 3 - 50, 120, 120);

        f.add(clientLabel);
        f.add(clientImageLabel);
        f.add(serverLabel);
        f.add(serverImageLabel);
    }

    private ImageIcon scaleDownImage(ImageIcon image) {
        Image oldImage = image.getImage(); // transform it
        Image newImage = oldImage.getScaledInstance(75, 75,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
        return new ImageIcon(newImage);  // transform it back
    }

    private ImageIcon scaleDownImage2(ImageIcon image) {
        Image oldImage = image.getImage(); // transform it
        Image newImage = oldImage.getScaledInstance(60, 60,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
        return new ImageIcon(newImage);  // transform it back
    }

    public void initRouterLabels() {
        for (int i = 0; i < routerLabelList.size(); ++i) {
            f.remove(routerLabelList.get(i));
            f.remove(routerImageLabelList.get(i));
        }

        int routerStepSize = (serverLocation - clientLocation) / (numberOfRouters + 1);
        routerLabelList.clear();
        routerImageLabelList.clear();

        JLabel line = new JLabel(getLineString(serverLocation - clientLocation));
        line.setBounds(clientLocation + 75, frameHeight / 3, serverLocation - clientLocation, 10);
        f.add(line);

        for (int i = 0; i < numberOfRouters; ++i){
            JLabel routerLabel = new JLabel("Router");
            routerLabel.setFont(new Font("Serif", Font.BOLD, 26));
            routerLabelList.add(routerLabel);
        }
        for (int i = 0; i < routerLabelList.size(); ++i) {
            int routerLabelXLocation = clientLocation + (routerStepSize * (i + 1));

            ImageIcon routerImage = scaleDownImage(new ImageIcon("C:/Users/MatthewT/IdeaProjects/Network/images/router.png"));
            JLabel routerImageLabel = (new JLabel("", routerImage, JLabel.CENTER));
            routerImageLabel.setBounds(routerLabelXLocation - 20, frameHeight / 3 - 50, 120, 120);
            routerImageLabelList.add(routerImageLabel);
            f.add(routerImageLabel);

            routerLabelList.get(i).setBounds(routerLabelXLocation, frameHeight / 3 + 60,hardwareWidth,hardwareHeight);
            f.add(routerLabelList.get(i));
        }

    }

    private void initClientServerButtons() {
        ImageIcon addImage = scaleDownImage2(new ImageIcon("C:/Users/MatthewT/IdeaProjects/Network/images/add.png"));
        ImageIcon subImage = scaleDownImage2(new ImageIcon("C:/Users/MatthewT/IdeaProjects/Network/images/sub.png"));

        JLabel addServerDropButton = (new JLabel("", addImage, JLabel.CENTER));
        JLabel subServerDropButton = (new JLabel("", subImage, JLabel.CENTER));
        JLabel serverDropAddSubLabel = new JLabel("Drop Rate: " + server.getDropRate() + "%");

        JLabel addClientDropButton = (new JLabel("", addImage, JLabel.CENTER));
        JLabel subClientDropButton = (new JLabel("", subImage, JLabel.CENTER));
        JLabel clientDropAddSubLabel = new JLabel("Drop Rate: " + client.getDropRate() + "%");

        JLabel addServerCorruptButton = (new JLabel("", addImage, JLabel.CENTER));
        JLabel subServerCorruptButton = (new JLabel("", subImage, JLabel.CENTER));
        JLabel serverCorruptAddSubLabel = new JLabel("Corrupt Rate: " + server.getCorruptRate() + "%");

        JLabel addClientCorruptButton = (new JLabel("", addImage, JLabel.CENTER));
        JLabel subClientCorruptButton = (new JLabel("", subImage, JLabel.CENTER));
        JLabel clientCorruptAddSubLabel = new JLabel("Corrupt Rate: " + client.getCorruptRate() + "%");

        addServerDropButton.setBounds(serverLocation + 40, (frameHeight / 3) + 100, 60, 60);
        subServerDropButton.setBounds(serverLocation - 20, (frameHeight / 3) + 100, 60, 60);
        serverDropAddSubLabel.setBounds(serverLocation + 5, (frameHeight / 3) + 150, 90, 40);

        addClientDropButton.setBounds(clientLocation + 40, (frameHeight / 3) + 100, 60, 60);
        subClientDropButton.setBounds(clientLocation - 20, (frameHeight / 3) + 100, 60, 60);
        clientDropAddSubLabel.setBounds(clientLocation + 5, (frameHeight / 3) + 150, 90, 40);

        addServerCorruptButton.setBounds(serverLocation + 40, (frameHeight / 3) + 200, 60, 60);
        subServerCorruptButton.setBounds(serverLocation - 20, (frameHeight / 3) + 200, 60, 60);
        serverCorruptAddSubLabel.setBounds(serverLocation - 5 , (frameHeight / 3) + 250, 110, 40);

        addClientCorruptButton.setBounds(clientLocation + 40, (frameHeight / 3) + 200, 60, 60);
        subClientCorruptButton.setBounds(clientLocation - 20, (frameHeight / 3) + 200, 60, 60);
        clientCorruptAddSubLabel.setBounds(clientLocation + 5, (frameHeight / 3) + 250, 110, 40);

        addServerDropButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                server.incDropRate();
                serverDropAddSubLabel.setText("Drop Rate: " + server.getDropRate() + "%");
            }
        });
        subServerDropButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                server.decDropRate();
                serverDropAddSubLabel.setText("Drop Rate: " + server.getDropRate() + "%");
            }
        });

        addClientDropButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                client.incDropRate();
                clientDropAddSubLabel.setText("Drop Rate: " + client.getDropRate() + "%");
            }
        });
        subClientDropButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                client.decDropRate();
                clientDropAddSubLabel.setText("Drop Rate: " + client.getDropRate() + "%");
            }
        });

        addServerCorruptButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                server.incCorruptRate();
                serverCorruptAddSubLabel.setText("Corrupt Rate: " + server.getCorruptRate() + "%");
            }
        });
        subServerCorruptButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                server.decCorruptRate();
                serverCorruptAddSubLabel.setText("Corrupt Rate: " + server.getCorruptRate() + "%");
            }
        });

        addClientCorruptButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                client.incCorruptRate();
                clientCorruptAddSubLabel.setText("Corrupt Rate: " + client.getCorruptRate() + "%");
            }
        });
        subClientCorruptButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                client.decCorruptRate();
                clientCorruptAddSubLabel.setText("Corrupt Rate: " + client.getCorruptRate() + "%");
            }
        });

        f.add(addClientDropButton);
        f.add(subClientDropButton);
        f.add(clientDropAddSubLabel);
        f.add(addServerDropButton);
        f.add(subServerDropButton);
        f.add(serverDropAddSubLabel);

        f.add(addClientCorruptButton);
        f.add(subClientCorruptButton);
        f.add(clientCorruptAddSubLabel);
        f.add(addServerCorruptButton);
        f.add(subServerCorruptButton);
        f.add(serverCorruptAddSubLabel);
    }

    public void initRouterButtons(List<NetworkHardware> routerList) {
        this.routerList = routerList;
        for (int i = 0; i < routerDropButtonLabelList.size(); ++i) {
            f.remove(routerAddDropButtonList.get(i));
            f.remove(routerSubDropButtonList.get(i));
            f.remove(routerDropButtonLabelList.get(i));

            f.remove(routerAddCorruptButtonList.get(i));
            f.remove(routerSubCorruptButtonList.get(i));
            f.remove(routerCorruptButtonLabelList.get(i));
        }
        routerAddDropButtonList.clear();
        routerSubDropButtonList.clear();
        routerDropButtonLabelList.clear();

        routerAddCorruptButtonList.clear();
        routerSubCorruptButtonList.clear();
        routerCorruptButtonLabelList.clear();

        int routerStepSize = (serverLocation - clientLocation) / (numberOfRouters + 1);
        ImageIcon addImage = scaleDownImage2(new ImageIcon("C:/Users/MatthewT/IdeaProjects/Network/images/add.png"));
        ImageIcon subImage = scaleDownImage2(new ImageIcon("C:/Users/MatthewT/IdeaProjects/Network/images/sub.png"));

        for (int i = 0; i < numberOfRouters; ++i) {
            NetworkHardware currentRouter = routerList.get(i);
            JLabel addRouterDropButton = (new JLabel("", addImage, JLabel.CENTER));
            JLabel subRouterDropButton = (new JLabel("", subImage, JLabel.CENTER));
            JLabel routerDropAddSubLabel = new JLabel("Drop Rate: " + currentRouter.getDropRate() + "%");

            JLabel addRouterCorruptButton = (new JLabel("", addImage, JLabel.CENTER));
            JLabel subRouterCorruptButton = (new JLabel("", subImage, JLabel.CENTER));
            JLabel routerCorruptAddSubLabel = new JLabel("Corrupt Rate: " + currentRouter.getCorruptRate() + "%");

            int routerXLocation = clientLocation + (routerStepSize * (i + 1));
            addRouterDropButton.setBounds(routerXLocation + 40, (frameHeight / 3) + 100, 60, 60);
            subRouterDropButton.setBounds(routerXLocation - 20, (frameHeight / 3) + 100, 60, 60);
            routerDropAddSubLabel.setBounds(routerXLocation, (frameHeight / 3) + 150, 90, 40);

            addRouterCorruptButton.setBounds(routerXLocation + 40, (frameHeight / 3) + 200, 60, 60);
            subRouterCorruptButton.setBounds(routerXLocation - 20, (frameHeight / 3) + 200, 60, 60);
            routerCorruptAddSubLabel.setBounds(routerXLocation, (frameHeight / 3) + 250, 100, 40);

            addRouterDropButton.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    currentRouter.incDropRate();
                    routerDropAddSubLabel.setText("Drop Rate: " + currentRouter.getDropRate() + "%");
                }
            });
            subRouterDropButton.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    currentRouter.decDropRate();
                    routerDropAddSubLabel.setText("Drop Rate: " + currentRouter.getDropRate() + "%");
                }
            });

            addRouterCorruptButton.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    currentRouter.incCorruptRate();
                    routerCorruptAddSubLabel.setText("Corrupt Rate: " + currentRouter.getCorruptRate() + "%");
                }
            });
            subRouterCorruptButton.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    currentRouter.decCorruptRate();
                    routerCorruptAddSubLabel.setText("Corrupt Rate: " + currentRouter.getCorruptRate() + "%");
                }
            });

            routerSubDropButtonList.add(subRouterDropButton);
            routerAddDropButtonList.add(addRouterDropButton);
            routerDropButtonLabelList.add(routerDropAddSubLabel);

            routerSubCorruptButtonList.add(subRouterCorruptButton);
            routerAddCorruptButtonList.add(addRouterCorruptButton);
            routerCorruptButtonLabelList.add(routerCorruptAddSubLabel);

            f.add(addRouterCorruptButton);
            f.add(subRouterCorruptButton);
            f.add(routerCorruptAddSubLabel);

            f.add(addRouterDropButton);
            f.add(subRouterDropButton);
            f.add(routerDropAddSubLabel);
        }
    }

    public void initMessageBox() {
        messageTextBox = new JTextField();
        messageTextBox.setText(messageToSend);
        messageTextBox.setBounds(510,30,500,40);

        JButton updateMessageButton = new JButton("Send Message");
        updateMessageButton.setBounds(1020,40,150,20);

        updateMessageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                messageToSend = messageTextBox.getText();
                sendNewMessage = true;
            }
        });

        f.add(updateMessageButton);
        f.add(messageTextBox);
    }

    public void initConnectionSpeeds() {
        for (int i = 0; i < connectionSpeedTexts.size(); ++i) {
            f.remove(connectionSpeedTexts.get(i));
            f.remove(connectionSpeedLabels.get(i));
        }
        connectionSpeedTexts.clear();
        connectionSpeedLabels.clear();

        if (routerList.size() == 0) {
            JTextField speedTextLabel = new JTextField();
            speedTextLabel.setText("10");
            speedTextLabel.setBounds(((serverLocation - clientLocation) / 2) + clientLocation - 50, frameHeight / 3 - 50,30,30);

            JLabel connectionSpeedLabel = new JLabel("Connection Speed");
            connectionSpeedLabel.setBounds(((serverLocation - clientLocation) / 2) + clientLocation - 80, frameHeight / 3 - 85, 150,30);

            f.add(speedTextLabel);
            f.add(connectionSpeedLabel);
            connectionSpeedTexts.add(speedTextLabel);
            connectionSpeedLabels.add(connectionSpeedLabel);
        }
        else {
            int stepSize = (serverLocation - clientLocation) / (numberOfRouters + 1);
            int beginLocation = (stepSize / 2) + clientLocation;
            for (int i = 0; i < numberOfRouters + 1; ++i) {
                JTextField speedTextLabel = new JTextField("10");
                speedTextLabel.setBounds(beginLocation + (stepSize * i), frameHeight / 3 - 50,30,30);
                JLabel connectionSpeedLabel = new JLabel("Connection Speed");
                connectionSpeedLabel.setBounds(beginLocation + (stepSize * i) - 40, frameHeight / 3 - 85, 150,30);

                f.add(speedTextLabel);
                f.add(connectionSpeedLabel);
                connectionSpeedTexts.add(speedTextLabel);
                connectionSpeedLabels.add(connectionSpeedLabel);
            }
        }
    }

    public int getUpdatedNumberOfRouters() {
        numberOfRouters = nextNumberOfRouters;
        return numberOfRouters;
    }

    public String getMessageToSend() {
        return messageToSend;
    }

    public boolean isSendNewMessage() {
        return sendNewMessage;
    }

    public ArrayList<Double> getRouterDropRates() {
        ArrayList<Double> dropRatesList = new ArrayList<>();
        for (int i = 0; i < routerList.size(); ++i) {
            dropRatesList.add(Double.parseDouble(routerList.get(i).getDropRate()) / 100);
        }
        return dropRatesList;
    }

    private String getLineString(int count) {
        return new String(new char[count / 4 - 10]).replace("\0", "-");
    }

    private void setPacketConnectionSpeed(Packet packet) {
        int packetLocation = packet.getPacketLocation();
        int packetSpeed = 0;
        for (int i = 0; i < numberOfRouters + 1; ++i) {
            int stepSize = (endPacketLocation - startPacketLocation) / (numberOfRouters + 1);
            int beginLocation = startPacketLocation;
            if ((beginLocation + (stepSize * (i + 1))) >= packetLocation) {
                packetSpeed = Integer.parseInt(connectionSpeedTexts.get(i).getText());
                i = numberOfRouters + 1;
            }
        }
        packet.setSpeed(packetSpeed);
    }
}
