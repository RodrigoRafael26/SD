import org.jsoup.select.Elements;

import java.io.*;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;

public class MulticastServer extends Thread{
    private static String MULTICAST_ADDRESS = "224.3.2.1";
    private static int PORT_SEND = 4321;
    //private static int PORT_MANAGE = 4324;
    //private int PORT_RECEIVE = 4322;
    private long SLEEP_TIME = 5000;
    public HashMap<String, HashSet<String>> searchIndex = new HashMap<String,HashSet<String>>();
    public HashMap<String, HashSet<String>> referenceIndex = new HashMap<String,HashSet<String>>();
    public static boolean reading;
    public static boolean writing;
    public static boolean update;

    public static void main(String[] args) {
        MulticastServer server = new MulticastServer();
        server.start();
    }

    public  MulticastServer(){
        super("Server is Running");
    }


    public void run(){

        //Isto só serve de teste, esta parte não é feita no multicast Server mas sim no ManageRequests (admin)
        WebCrawler wc = new WebCrawler("http://www.uc.pt/fctuc/dei/", searchIndex, referenceIndex);

        try {
            sleep(SLEEP_TIME*2);
            System.out.println(referenceIndex);
            System.out.println(searchIndex);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return;
    }

}

//this class is used to synch the hashmaps of the diferent multicast servers
class ConnectServersMulti {
    //Needs 2 TCP connections
}





