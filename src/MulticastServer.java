import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
        
    }

    public void registerUser(String userName, String password, int id){
        File f = new File("Users");
        try {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
