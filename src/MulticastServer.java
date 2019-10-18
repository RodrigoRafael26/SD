import java.io.*;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class MulticastServer extends Thread{
    private static String MULTICAST_ADDRESS = "224.3.2.1";
    private static int PORT_SEND = 4321;
    //private static int PORT_MANAGE = 4324;
    //private int PORT_RECEIVE = 4322;
    private long SLEEP_TIME = 5000;

    public static void main(String[] args) {
        ManageRequests manageRequests = new ManageRequests();
        /*m.registerUser("admin", "admin", 1);
        m.registerUser("user", "user", 2);*/


    }


}

