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
            //check if is needed to share URLs
            int sumURLs = 0;
            for(ServerConfig s : st.getOnlineServers())  sumURLs+= s.getWorkload();

            //if it has over 20% more URLs than it would have if the system was perfectly balanced/ completly balance the servers workload
            if(st.getServerConfig().getWorkload() > sumURLs / st.getOnlineServers().size() + ((sumURLs / st.getOnlineServers().size())*0.2)){
                //Discover the x amount needed to send each server balance workload
                int x = (sumURLs - st.getServerConfig().getWorkload()) / st.getOnlineServers().size();
                //remove last X urls from queue
                for(int i = 0; i < x;i++){
                    //always remove last one and add it to the array that will be sent to other server

                    st.getShareUrls().add(st.getLinkList().remove(st.getLinkList().size()-1));
                }
            }

            os.writeObject(st.getShareUrls());

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


