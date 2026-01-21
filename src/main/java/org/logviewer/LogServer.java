package org.logviewer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.logviewer.entity.Log;
import org.logviewer.listener.LogListener;


public class LogServer {

    int port = 5000;
    private List<LogListener> listeners=new ArrayList<>();

    void addLogListener(LogListener listener) {
        listeners.add(listener);
    }

    public void start() {

        ObjectMapper mapper = new ObjectMapper();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur en écoute sur le port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connexion de : " + socket.getInetAddress());

                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            try {
                                JsonNode jsonNode = mapper.readTree(line);
                                JsonNode payload = jsonNode.get("jsonPayload");
                                if (payload != null) {

                                    try {
                                        final Log log = mapper.treeToValue(payload, Log.class);
                                        listeners.forEach((listener ->  listener.logAdded(log)));
                                    } catch (JsonProcessingException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Erreur parsing JSON: " + e.getMessage());
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Erreur socket: " + e.getMessage());
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}