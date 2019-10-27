import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCP_Client extends Thread{
    private Storage st;
    private String host;
    private String message;
    private int serversocket;
    private Socket s;
    public TCP_Client (Storage st,String hostname, int port, String message) {
        this.st = st;
        this.host = hostname;
        this.message = message;
        Socket s = null;

        this.serversocket = port;
        this.start();


    }
    public void run(){
        try {
            // 1o passo
            s = new Socket(host,serversocket);

//            System.out.println("SOCKET=" + s);
            // 2o passo
//            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            out.writeUTF("message");

//            InputStreamReader input = new InputStreamReader(System.in);
//            BufferedReader reader = new BufferedReader(input);
//            System.out.println("Introduza texto:");
//            ReadAnswer t = new ReadAnswer(s);
            try {
                this.sleep(100);
                s.close();
                System.out.println("closed socket");
            } catch (IOException e) {
                System.out.println("close:" + e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            //remove host from online servers list
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } finally {
//            if (s != null)
//                try {
//                    s.close();
//                    System.out.println("closed socket");
//                } catch (IOException e) {
//                    System.out.println("close:" + e.getMessage());
//                }
        }
    }
}


