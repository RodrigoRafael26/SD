import java.rmi.*;
import java.io.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

// message = "registo ; " + message_id +" ; " + username + " ; " + password;

class MulticastConnection extends Thread {
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT_SEND = 1000;
    private int PORT_RECEIVE = 1000;
    private String message;
    private String ID;

    public MulticastConnection(String message) {
        super("Multicast Connection");
        this.message = message;
        String[] aux = message.split(" ; ");
        String[] aux2 = aux[1].split(" : ");
        this.ID = aux2[1];
    }

    public String getResponse() {
        this.start();
        try {
            this.join();
        } catch (Exception e) {
            System.out.println("Error in Multicast Thread");
        }
        return message;
    }

    public void run() {
        System.out.println("COCO");
    }
}

public class ServidorRMI extends UnicastRemoteObject implements ServerInterface {
    private int i;
    private static ArrayList<ClientInterface> online = new ArrayList<ClientInterface>();
    private static ArrayList<String> users_online = new ArrayList<String>();
    String message;
    String message_id;
    MulticastConnection connection;
    String[] process_message;
    ArrayList<String> message_processed;
    String[] aux;

    protected ServidorRMI() throws RemoteException {
        super();
    }

    public void ping() throws RemoteException {

    }

    public String[] getAnswer(String message) {
        message_id = UUID.randomUUID().toString();

        connection = new MulticastConnection(message);
        message = connection.getResponse();

        process_message = message.split(" ; ");
        message_processed = new ArrayList<String>();

        for(String s : process_message){
            aux = s.split(" ; ");
            message_processed.add(aux[0]);
            message_processed.add(aux[1]);
        }
        if(message_processed.get(5).compareTo("true")==0){
            String[] Info = new String[3];
            Info[0] = message_processed.get(5);
            Info[1] = message_processed.get(7);
            Info[2] = message_processed.get(9);
            return Info;
        }else{
            String[] Info = new String[1];
            Info[0] = "false";
            return Info;
        }
    }

    public String[] recordUser(String username, String password) throws RemoteException {
        message = "registo ; " + message_id +" ; " + username + " ; " + password;
        return getAnswer(message);
    }

    public String[] checkUser(String username, String password) throws RemoteException {
        message = "login ; " + message_id + " ; " + username + " ; " + password;
        return getAnswer(message);
    }

    public void newUser(ClientInterface client, String username) throws RemoteException {
        i = 0;
        while(online.get(i) != null){
            if(online.get(i) != client) i++;
            else return;
        }
        online.set(i, client);
        users_online.set(i, username);
    }

    public void userQuit(ClientInterface client, String username) throws RemoteException {
        i = 0;
        while(users_online.get(i).compareTo(username) != 0) i++;
        online.set(i, null);
        users_online.set(i, " ");
    }

    public static void main(String[] args) throws RemoteException {

    }
}