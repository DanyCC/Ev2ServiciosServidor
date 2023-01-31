package com.example.servidorchatgpt;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ServidorUDP extends Application {
    private final int PORT = 5010;
    private final int SEND_PORT = 6010;
    private List<InetAddress> clientsIP;
    private Map<InetAddress, String> nicks;
    private ObservableList<String> messages;
    private ListView<String> listView;
    private DatagramSocket socket;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        clientsIP = new ArrayList<>();
        nicks = new HashMap<>();
        messages = FXCollections.observableArrayList();
        listView = new ListView<>(messages);
        StackPane root = new StackPane(listView);
        Scene scene = new Scene(root, 300, 250);
        primaryStage.setTitle("Chat UDP Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread(() -> {
            try {
                socket = new DatagramSocket(PORT);
                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    if (message.equalsIgnoreCase("STOP")) {
                        socket.close();
                        System.exit(0);
                    } else {
                        InetAddress clientIP = packet.getAddress();
                        if (!clientsIP.contains(clientIP)) {
                            if (!nicks.containsValue(message)) {
                                clientsIP.add(clientIP);
                                nicks.put(packet.getAddress(), message);
                                messages.add(clientIP + ": " + message);
                                String nickEscogido = packet.getAddress().getHostAddress() + ",false";
                                packet = new DatagramPacket(nickEscogido.getBytes(), nickEscogido.getBytes().length, clientIP, SEND_PORT);
                                socket.send(packet);
                            } else {
                                String nickEscogido = packet.getAddress().getHostAddress() + ",true";
                                packet = new DatagramPacket(nickEscogido.getBytes(), nickEscogido.getBytes().length, clientIP, SEND_PORT);
                                socket.send(packet);
                            }
                        } else {
                            String nick = "";
                            for (Map.Entry<InetAddress, String> ip : nicks.entrySet()) {
                                if (packet.getAddress().getHostAddress().equals(ip.getKey().getHostAddress())){
                                    nick = ip.getValue();
                                }
                            }
                            for (InetAddress ip : clientsIP) {
                                //if (!ip.equals(clientIP)) {
                                    String mensaje = new String(packet.getData(), 0, packet.getLength());
                                    String mensajeFinal = nick + ": " + mensaje;
                                    buffer = mensajeFinal.getBytes();
                                    packet = new DatagramPacket(buffer, buffer.length, ip, SEND_PORT);
                                    socket.send(packet);
                                //}
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}