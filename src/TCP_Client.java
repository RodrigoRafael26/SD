import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCP_Client {
    Storage st;
    String host;
    String message;
    public TCP_Client (Storage st,String hostname, int port, String message) {
        this.st = st;
        this.host = hostname;
        System.out.println("tcp client criado");
        this.message = message;
        Socket s = null;
        int serversocket = port;

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
                s.close();
                System.out.println("closed socket");
            } catch (IOException e) {
                System.out.println("close:" + e.getMessage());
            }
        } catch (UnknownHostException e) {
            //remove host from online servers list
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } finally {
            if (s != null)
                try {
                    s.close();
                    System.out.println("closed socket");
                } catch (IOException e) {
                    System.out.println("close:" + e.getMessage());
                }
        }
    }
}


