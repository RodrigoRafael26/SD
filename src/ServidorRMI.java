import java.rmi.*;
import java.io.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

class MulticastConnection extends Thread {
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT_SEND = 1000;
    private int PORT_RECEIVE = 1000;

    public MulticastConnection() {
        this.start();
        try {
            this.join();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
    }

    public void run() {
    }
}

public class ServidorRMI extends UnicastRemoteObject {

    protected ServidorRMI() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws RemoteException {
        MulticastConnection t = new MulticastConnection();
    }

    public String[] registUser(String username, String password, boolean isAdmin) throws RemoteException{
        String message = new String();
        String proto_id = UUID.randomUUID().toString();
        message = "type | registo ; message_id | " + proto_id +" ; username | " + username + " ; password | " + password;
        MulticastConnection N = new MulticastConnection(message);
        message = N.GetResponse();

        String[] processar = message.split(Pattern.quote(" ; "));
        ArrayList<String> processa = new ArrayList<String>();
        String[] aux;
        for(String s : processar){
            aux = s.split(Pattern.quote(" | "));
            processa.add(aux[0]);
            processa.add(aux[1]);
        }

        if(processa.get(5).compareTo("true")==0){
            String[] Info = new String[3];
            Info[0] = processa.get(5);
            Info[1] = processa.get(7);
            Info[2] = processa.get(9);
            return Info;
        }
        else{
            String[] Info = new String[1];
            Info[0] = "false";
            return Info;
        }
    }
}