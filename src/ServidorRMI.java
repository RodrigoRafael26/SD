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
}