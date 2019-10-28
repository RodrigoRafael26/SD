import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class ShareInfo extends Thread{
    private Storage st;
    private String host;

    private int serversocket;
    private Socket s;

    public ShareInfo (Storage st,String hostname, int port) {
        this.st = st;
        this.host = hostname;

        Socket s = null;

        this.serversocket = port;
        this.start();


    }
    public void run(){
        try {
            // 1o passo
            s = new Socket(host,serversocket);

//            DataInputStream in = new DataInputStream(s.getInputStream());
            ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream is = new ObjectInputStream(s.getInputStream());

            //send search differences
            os.writeObject(st.getSearchUpdates());
            os.writeObject(st.getReferenceUpdates());
            os.writeObject(st.getShareUrls());

//            InputStreamReader input = new InputStreamReader(System.in);
//            BufferedReader reader = new BufferedReader(input);
//            System.out.println("Introduza texto:");
//            ReadAnswer t = new ReadAnswer(s);
            try {
                this.sleep(100);
                s.close();
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
        }

    }
}


