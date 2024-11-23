package it.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
// import java.util.ArrayList;
import java.util.HashMap;

public class MyThread extends Thread {

    private Socket s;
    // private ArrayList<String> list;

    private HashMap<String, Socket> connectedUsers;
    private String currentUsername;

    public MyThread(Socket s, HashMap<String, Socket> connectedUsers) {
        this.s = s;
        this.connectedUsers = connectedUsers;
    }

    @Override
    public void run() {
        Boolean closed = true;
        Boolean connected = false;

        do {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                String stringaRicevuta = in.readLine();
                System.out.println("Client invia: " + stringaRicevuta);

                // ArrayList<String> users = new ArrayList<>();

                String[] words = stringaRicevuta.split(" ");

                String stringaInviare;

                do {
                    switch (words[0]) {

                        // Connesione
                        case "CONNECT":
                            if (connectedUsers.containsKey(words[1])) {
                                stringaInviare = "KO user-not-available";
                                out.writeBytes(stringaInviare + "\n");
                            } else {
                                currentUsername = words[1];
                                connectedUsers.put(currentUsername, s);
                                stringaInviare = "JOIN " + currentUsername;
                                SendGlobal(stringaInviare);
                                // out.writeBytes(stringaInviare + "\n");
                                System.out.println("Server invia: " + stringaInviare);

                                connected = true;
                                words = null;
                            }
                            break;

                        // Messaggi che non contengono comandi esistenti (nn serve perche il client gia
                        // controlla prima di inviare)
                        default:
                            stringaInviare = "KO incorrect-command";
                            out.writeBytes(stringaInviare + "\n");
                            System.out.println(stringaInviare);
                            break;
                    }
                } while (!connected);

                do {
                    stringaRicevuta = in.readLine();
                    words = stringaRicevuta.split(" ");

                    switch (words[0]) {

                        case "PRIVATE":

                            String destination = words[1];
                            stringaInviare = words[2];

                            if (connectedUsers.containsKey(destination)) {
                                out.writeBytes("OK" + "\n");
                                System.out.println("Server invia: OK, e inoltra: " + stringaInviare + "\n");

                                Socket destinationSocket = connectedUsers.get(destination);
                                DataOutputStream destinationOut = new DataOutputStream(
                                        destinationSocket.getOutputStream());
                                destinationOut.writeBytes("PRIVATE " + currentUsername + ": " + stringaInviare + "\n");
                                System.out.println("Server invia: " + stringaInviare + "\n");

                            } else {
                                out.writeBytes("KO user-not-found\n");
                                System.out.println("Server invia: KO user-not-found\n");

                            }

                            break;

                        case "GLOBAL":

                            stringaInviare = words[2];

                            SendGlobal(stringaInviare);

                            break;

                        // case "CHANGE": ---> ***E' DA MODIFICARE PERCHE AL POSTO DELL' ARRAYLIST HO
                        // MESSO UN HASHMAP***

                        // if (users.contains(words[1])) {
                        // // server deve mandare KO
                        // stringaInviare = "KO";
                        // out.writeBytes(stringaInviare + "\n");
                        // System.out.println("Server invia:" + stringaInviare);
                        // } else {
                        // // server deve mandare JOIN + username
                        // stringaInviare = "JOIN";
                        // out.writeBytes(stringaInviare + "\n");
                        // System.out.println("Server invia:" + stringaInviare);

                        // users.set(users.indexOf(words[1]), words[1]); // aggiorna l'username
                        // nell'array degli
                        // // users collegati

                        // connected = true;
                        // words = null;
                        // }
                        // break;

                        // case "USERS":
                        // stringaInviare = "USERS";

                        // for (int i = 0; i < users.size(); i++) {
                        // stringaInviare += " " + users.get(i);
                        // out.writeBytes(stringaInviare + "\n");
                        // System.out.println("Server invia:" + stringaInviare);
                        // }
                        // break;

                        // Uscita dalla chat
                        case "ESC":
                            connectedUsers.remove(currentUsername);

                            stringaInviare = "BYE " + currentUsername + "\n";

                            SendGlobal(stringaInviare);

                            s.close();

                            connected = false;

                            break;

                        // Messaggi che non contengono comandi esistenti (nn serve perche il client gia
                        // controlla prima di inviare)
                        default:
                            break;
                    }

                } while (connected);

            } catch (Exception e) {
                System.out.println("Errore, comunicazione fallita");
            }
        } while (closed);
    }

    public void SendGlobal(String str) throws IOException {
        for (Socket clientSocket : connectedUsers.values()) {
            if (clientSocket != s) { // per evitare di mandare il messaggio globale anche a chi lo
                                     // ha creato
                DataOutputStream clientOut = new DataOutputStream(clientSocket.getOutputStream());
                clientOut.writeBytes("GLOBAL " + currentUsername + ": " + str + "\n");
                System.out.println("Server invia: " + str + "\n");

            }
        }
    }

}
