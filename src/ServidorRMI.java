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

    public String GetResponse() {
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

    protected ServidorRMI() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws RemoteException {

    }

    public String[] registUser(String username, String password, boolean isAdmin) throws RemoteException{
        String message = new String();
        String message_id = UUID.randomUUID().toString();
        message = "registo ; " + message_id +" ; " + username + " ; " + password;
        MulticastConnection N = new MulticastConnection(message);
        message = N.GetResponse();

        String[] process_message = message.split(" ; ");
        ArrayList<String> message_processed = new ArrayList<String>();
        String[] aux;

        for(String s : process_message){
            aux = s.split(" : ");
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

    public void ping() throws RemoteException {

    }

    public String[] RegistUser(String username, String password) throws RemoteException {
        return new String[0];
    }

    public void NewUser(ClientInterface client, String username) throws RemoteException {

    }
}