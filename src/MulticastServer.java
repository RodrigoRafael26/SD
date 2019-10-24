import org.jsoup.select.Elements;

import java.io.*;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.FileHandler;

public class MulticastServer extends Thread{
    private String multicast_address;
    private int port;
    private int server_id;
    private long SLEEP_TIME = 5000;
    Storage st;




    public static void main(String[] args) {
        MulticastServer server = new MulticastServer();

        server.start();
    }

    public  MulticastServer(){

        super("Server is Running");
        try {
            FileHandler fileHandler = new FileHandler();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.st = new Storage();
        this.port = st.getServerConfig().getPort();
        this.multicast_address = st.getServerConfig().getAddress();
        this.server_id = st.getServerConfig().getServer_ID();
    }


    public void run(){

        //Isto só serve de teste, esta parte não é feita no multicast Server mas sim no ManageRequests (admin)
//        WebCrawler wc = new WebCrawler(st);
        //st.addLinkToQueue("https://pt.wikipedia.org/wiki/Engenharia_Inform%C3%A1tica");

        MulticastSocket socket = null;
        try{
            socket = new MulticastSocket(port);
            InetAddress group = InetAddress.getByName(multicast_address);
            socket.joinGroup(group);
            socket.setLoopbackMode(false);

            while(true){
                byte[] socketBuffer = new byte[8];
                DatagramPacket packet = new DatagramPacket(socketBuffer, socketBuffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                int length = Integer.parseInt(message.trim());
                socketBuffer = new byte[length];
                packet = new DatagramPacket(socketBuffer, length);
                socket.receive(packet);

                byte[] data = packet.getData();
                String s = new String(data,0,data.length);
                System.out.println(s);

                //chamar manage requests aqui
                System.out.println("Port " + packet.getPort() + " on " + packet.getAddress().getHostAddress() +" sent this message:" + s);
                ManageRequests mr = new ManageRequests(st, s);

            }


        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            //guardar mensagens que estiverem por enviar
            //
            socket.close();

        }

        //create thread to save state from time to time? or do it in keepAlive Thread

        //ManageRequests mr = new ManageRequests(st, "type | register ; username | admin ; password | worked");
        //TCP_Server tcp = new TCP_Server();
        //TCP_Client client = new TCP_Client("localhost");

        //tem de mandar as configs do socket TCP por multicast

        //send keepAlives from time to time

    }

}

class Storage{
    private ConcurrentHashMap<String, CopyOnWriteArrayList<String>> searchIndex;
    private ConcurrentHashMap<String, CopyOnWriteArrayList<String>> referenceIndex;
    private ConcurrentHashMap<String, CopyOnWriteArrayList<String>> searchIndex_changes;
    private ConcurrentHashMap<String, CopyOnWriteArrayList<String>> referenceIndex_changes;

    private CopyOnWriteArrayList<User> users;
    private CopyOnWriteArrayList<String> responses;
    private CopyOnWriteArrayList<User> onlineUsers;

    private CopyOnWriteArrayList<ServerConfig> onlineServers;
    private CopyOnWriteArrayList<String> linkList;
    private HandleFiles fileHandler;
    private ServerConfig serverConfig;


    public Storage(){
        this.fileHandler = new HandleFiles();
        this.searchIndex = new ConcurrentHashMap<>();
        this.referenceIndex =  new ConcurrentHashMap<>();
        this.linkList = new CopyOnWriteArrayList<>();
        this.users = new CopyOnWriteArrayList<>();
        this.searchIndex_changes = new ConcurrentHashMap<>();
        this.referenceIndex_changes = new ConcurrentHashMap<>();
        this.onlineServers = new CopyOnWriteArrayList<>();
        this.onlineUsers = new CopyOnWriteArrayList<>();
        fillInfo();

    }
    private void fillInfo(){
        if(fileHandler.getReferenceIndex()!=null){
            referenceIndex = fileHandler.getReferenceIndex();
        }
        if (fileHandler.getSearchIndex()!=null){
            searchIndex = fileHandler.getSearchIndex();
        }
        if(fileHandler.readUsers()!=null){
            users = fileHandler.readUsers();
        }
        if (fileHandler.readConfig()!=null){
            serverConfig = fileHandler.readConfig();
            //System.out.println(serverConfig.getPort());
            //System.out.println(serverConfig.getAddress());
        }
    }

    public synchronized void addLinkToQueue(String ws){
        linkList.add(ws);
        notify();
    }

    // add a word to hashmap
    public void addWordToHash(String word, String ws){
        //if the word already exists
        if(searchIndex.get(word)!=null){
            //if the ws is not in word index add it
            if(!searchIndex.get(word).contains(ws)){
                //if this word doesnt exist in changes add it
                if(searchIndex_changes.get(word) == null){
                    searchIndex_changes.put(word, new CopyOnWriteArrayList<>());
                }

                //add ws to search hash and changes hash
                searchIndex.get(word).add(ws);
                searchIndex_changes.get(word).add(ws);
            }

        }else{//if the word doesnt exist create a key in both hashmaps (search and changes)
            searchIndex.put(word, new CopyOnWriteArrayList<>());
            searchIndex_changes.put(word, new CopyOnWriteArrayList<>());
            searchIndex.get(word).add(ws);
            searchIndex_changes.get(word).add(ws);
        }
    }

    public void addReferenceToHash(String ws, String s){
        if (referenceIndex.get(s) != null) {
            if(!referenceIndex.get(s).contains(ws)){

                referenceIndex.get(s).add(ws);
            }
        }else {
            referenceIndex.put(s, new CopyOnWriteArrayList<>());
            referenceIndex.get(s).add(ws);
        }
    }

    public ConcurrentHashMap<String, CopyOnWriteArrayList<String>> getSearchHash(){
        return searchIndex;
    }

    public ConcurrentHashMap<String, CopyOnWriteArrayList<String>> getReferenceHash(){
        return referenceIndex;
    }

    public CopyOnWriteArrayList<String> getLinkList(){
        return linkList;
    }

    public void addUser(User user){
        users.add(user);
    }

    public User getUser(String username){
        for(User user : users){
            if(user.getUsername().compareTo(username) == 0){
                return user;
            }
        }
        return null;
    }

    public CopyOnWriteArrayList<User> getUserList() {
        return users;
    }

    public synchronized String getLink(){
        while(linkList.isEmpty()){
            try{
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        String ws = linkList.get(0);
        linkList.remove(0);
        System.out.println("removed");
        return ws;
    }

    public ServerConfig getServerConfig(){
        return serverConfig;
    }

    public CopyOnWriteArrayList<User> getOnlineUsers(){
        return onlineUsers;
    }

    public void disconnectUser(String username){
        for(User u : onlineUsers){
            if(u.getUsername().equals(username)){
                onlineUsers.remove(u);
            }
        }
    }

    public CopyOnWriteArrayList<ServerConfig> getOnlineServers(){
        return onlineServers;
    }
    public void updateFiles(){
        fileHandler.writeReferenceIndex(referenceIndex);
        fileHandler.writeSearchIndex(searchIndex);
        fileHandler.writeUsers(users);
        fileHandler.writeUndeliveredMessages(responses);
    }
}



