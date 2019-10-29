import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLOutput;

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
    //connect with other servers through tcp
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
            String temp;
            //check if is needed to share URLs
            int sumURLs = 0;
            for(ServerConfig s : st.getOnlineServers())  sumURLs+= s.getWorkload();

            //if it has over 20% more URLs than it would have if the system was perfectly balanced/ completly balance the servers workload

            if(st.getServerConfig().getWorkload() > sumURLs*0.7){
                //Discover the x amount needed to send each server balance workload
//                System.out.println("CHEGOU AO SHARE WORKLOAD");
                int x = (int) (sumURLs*0.3);
                //remove X urls from queue
                for(int i = 0; i < x;i++){
                    //always one and add it to the array that will be sent to other server
                    temp = st.getLink();
//                    System.out.println("queue size: "+ st.getLinkList().size());
//                    System.out.println("SHOW TEMP "+ temp);
                    st.getShareUrls().add(temp);
                }
            }
            System.out.println(st.getShareUrls().toString());
            os.writeObject(st.getShareUrls());

            try {
                this.sleep(1000);
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


