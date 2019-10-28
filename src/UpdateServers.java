import java.util.concurrent.CopyOnWriteArrayList;

public class UpdateServers extends Thread {
    private Storage server_Storage;
    private CopyOnWriteArrayList<ServerConfig> onlineServers;
    public UpdateServers(Storage st){
        this.server_Storage = st;
        this.start();
    }

    public void run() {
        while (true) {
            System.out.println("started Update Servers");
             this.onlineServers = server_Storage.getOnlineServers();

            //create TCP Client connecting to every online server
            System.out.println("ONLINE SERVERS: " + server_Storage.getOnlineServers().size());
            for (ServerConfig tempList : onlineServers) {

                if (tempList.getServer_ID() != server_Storage.getServerConfig().getServer_ID()) {

                    new ShareInfo(server_Storage, tempList.getHostname(), tempList.getTcp_port());
                }

            }

            try{
                this.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
