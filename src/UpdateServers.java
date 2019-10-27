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
            //create message to send to other servers
            //                String updateMessage = "searchIndex|";
            //                for(String searchTerm : server_Storage.getSearchUpdates().keySet()){
            //                    updateMessage += searchTerm + " | " + server_Storage.getSearchUpdates().get(searchTerm).toString().replace("[","").replace(",",";".replace("]",""));
            //                }
            //                updateMessage += "referenceIndex|";
            //                for(String indexRef : server_Storage.getReferenceUpdates().keySet()){
            //                    updateMessage += indexRef + " | " + server_Storage.getSearchUpdates().get(indexRef).toString().replace("[","").replace(",",";".replace("]",""));
            //                }

            //create TCP Client connecting to every online server
//                    TCP_Client[] client_List = new TCP_Client[server_Storage.getOnlineServers().size()];



            System.out.println("ONLINE SERVERS: " + server_Storage.getOnlineServers().size());
            for (ServerConfig tempList :onlineServers) {

                if (tempList.getServer_ID() != server_Storage.getServerConfig().getServer_ID()) {

                    new TCP_Client(server_Storage, tempList.getHostname(), tempList.getTcp_port(), "hello");
                }

            }
            try{
                this.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
