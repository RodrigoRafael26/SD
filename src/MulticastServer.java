

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

    public static void main(String[] args) {
        MulticastServer server = new MulticastServer();
        server.start();
    }

    public  MulticastServer(){
        super("Server is Running");


    }


    public void run(){
        HashMap<String, HashSet<String>> searchIndex = new HashMap<String,HashSet<String>>();
        HashMap<String, HashSet<String>> referenceIndex = new HashMap<String,HashSet<String>>();


        WebCrawler wc = new WebCrawler("http://www.uc.pt/fctuc/dei/");
        //wc.indexLinks("http://www.uc.pt/fctuc/dei/", searchIndex, referenceIndex);
        //System.out.println(wc.searchIndex);
        //System.out.println(wc.referenceIndex);
        try {
            sleep(10000);
            searchIndex = wc.getReferenceIndex();
            referenceIndex = wc.getReferenceIndex();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}

