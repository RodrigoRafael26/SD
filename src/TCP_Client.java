import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCP_Client {
    String hostname;
    public TCP_Client (String hostname) {
        this.hostname = hostname;
        Socket s = null;
        int serversocket = 6000;
        try {
            // 1o passo
            s = new Socket(hostname,serversocket);

            System.out.println("SOCKET=" + s);
            // 2o passo
            //DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            String texto = "";
            InputStreamReader input = new InputStreamReader(System.in);
            BufferedReader reader = new BufferedReader(input);
            System.out.println("Introduza texto:");
            ReadAnswer t = new ReadAnswer(s);
            while (true) {
                // READ STRING FROM KEYBOARD

                try {
                    texto = reader.readLine();
                } catch (Exception e) {
                }
                // WRITE INTO THE SOCKET
                out.writeUTF(texto);
            }
            // 3o passo

        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } finally {
            if (s != null)
                try {
                    s.close();
                } catch (IOException e) {
                    System.out.println("close:" + e.getMessage());
                }
        }
    }
}

class ReadAnswer extends Thread{
    DataInputStream in;
    //DataOutputStream out;
    Socket s;
    public ReadAnswer(Socket s){
        this.s = s;
        try {
            in = new DataInputStream(s.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.start();
    }


    public void run(){

        // READ FROM SOCKET
        String data = null;
        try {
            data = in.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // DISPLAY WHAT WAS READ
        System.out.println("Received: " + data);
    }
}

