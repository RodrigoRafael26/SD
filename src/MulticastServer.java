

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
        HashMap<String, HashSet<String>> searchIndex = new HashMap<String,HashSet<String>>();
        HashMap<String, HashSet<String>> referenceIndex = new HashMap<String,HashSet<String>>();


        WebCrawler wc = new WebCrawler();
        wc.getPageInfo("http://www.uc.pt/fctuc/dei/", searchIndex, referenceIndex);
        System.out.println(searchIndex);
    }


    public void run(){

    }

}

