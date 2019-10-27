import org.jsoup.select.Elements;

import java.io.*;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.FileHandler;

public class MulticastServer extends Thread{
    private String multicast_address ;
    private int port;
    private int server_id;
    Storage st;




    public static void main(String[] args) {
        MulticastServer server = new MulticastServer(args[0]);
        server.start();

    }

    public  MulticastServer(String configFile){

        super("Server is Running");

        try {
            FileHandler fileHandler = new FileHandler();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.st = new Storage(configFile);
        this.port = st.getServerConfig().getPort();
        this.multicast_address = st.getServerConfig().getAddress();
        this.server_id = st.getServerConfig().getServer_ID();
        KeepAlive ping = new KeepAlive(st);
        ping.start();

    }


    public void run(){

        MulticastSocket socket = null;
        TCP_Server server = new TCP_Server(st, st.getServerConfig().getTcp_port());

        WebCrawler wc = new WebCrawler(st);
        ManageRequests mr = new ManageRequests(st);
        try{
            socket = new MulticastSocket(port);
            InetAddress group = InetAddress.getByName(multicast_address);
            socket.joinGroup(group);
            //socket.setLoopbackMode(false);

            while(true){
                byte[] socketBuffer = new byte[10000];
                DatagramPacket packet = new DatagramPacket(socketBuffer, socketBuffer.length);
                socket.receive(packet);
                socket.setLoopbackMode(false);
                String request = new String(packet.getData(), 0, packet.getLength());
                request = request.trim();
//                socketBuffer = new byte[length];
//                packet = new DatagramPacket(socketBuffer, length);
//                socket.receive(packet);
//
//                byte[] data = packet.getData();
//                String s = new String(data,0,data.length);
//                System.out.println(request);
                System.out.println("recieved request");
                st.addRequestToQueue(request);

                //chamar manage requests aqui
               //fazer uma queue de requests



            }


        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            //guardar mensagens que estiverem por enviar
            //
            socket.close();

        }

    }

}

class Storage{
    private ConcurrentHashMap<String, CopyOnWriteArrayList<String>> searchIndex;
    private ConcurrentHashMap<String, CopyOnWriteArrayList<String>> referenceIndex;
    private ConcurrentHashMap<String, CopyOnWriteArrayList<String>> searchIndex_changes;
    private ConcurrentHashMap<String, CopyOnWriteArrayList<String>> referenceIndex_changes;

    private CopyOnWriteArrayList<User> users;
    private CopyOnWriteArrayList<String> requestQueue;
    private CopyOnWriteArrayList<User> onlineUsers;

    private CopyOnWriteArrayList<ServerConfig> onlineServers;
    private CopyOnWriteArrayList<String> linkList;
    private HandleFiles fileHandler;
    private ServerConfig serverConfig;


    public Storage(String configFile){
        this.fileHandler = new HandleFiles(configFile);
        this.searchIndex = new ConcurrentHashMap<>();
        this.referenceIndex =  new ConcurrentHashMap<>();
        this.linkList = new CopyOnWriteArrayList<>();
        this.users = new CopyOnWriteArrayList<>();
        this.searchIndex_changes = new ConcurrentHashMap<>();
        this.referenceIndex_changes = new ConcurrentHashMap<>();
        this.onlineServers = new CopyOnWriteArrayList<>();
        this.onlineUsers = new CopyOnWriteArrayList<>();
        this.requestQueue = new CopyOnWriteArrayList<>();
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
    public synchronized void addRequestToQueue(String ws){
        requestQueue.add(ws);
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
                //if this url doesnt exist in changes add it
                if(referenceIndex_changes.get(s) == null){
                    referenceIndex_changes.put(s, new CopyOnWriteArrayList<>());
                }
                referenceIndex.get(s).add(ws);
                referenceIndex_changes.get(s).add(ws);

            }
        }else {
            referenceIndex.put(s, new CopyOnWriteArrayList<>());
            referenceIndex_changes.put(s, new CopyOnWriteArrayList<>());
            referenceIndex.get(s).add(ws);
            referenceIndex_changes.get(s).add(ws);
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

    public synchronized String getRequest(){
        while(requestQueue.isEmpty()){
            try{
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        String ws = requestQueue.get(0);
        requestQueue.remove(0);
        return ws;
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

    public void addOnlineServer(ServerConfig s){
        onlineServers.add(s);
    }

    public ConcurrentHashMap<String, CopyOnWriteArrayList<String>> getSearchUpdates(){
        return searchIndex_changes;
    }

    public ConcurrentHashMap<String, CopyOnWriteArrayList<String>> getReferenceUpdates() {
        return referenceIndex_changes;
    }

    public void updateFiles(){
        fileHandler.writeReferenceIndex(referenceIndex);
        fileHandler.writeSearchIndex(searchIndex);
        fileHandler.writeUsers(users);
        serverConfig.updateWorkload(linkList.size());
    }
}


class KeepAlive extends Thread{
    Storage st;
    public KeepAlive(Storage st){
        //send keepAlives to multicast group
        this.st = st;
    }

    public void run(){
        DatagramSocket socket = null;
        try{
            socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(st.getServerConfig().getAddress());



            while(true){
                st.updateFiles();

                String message = "type | keepAlive ; serverID " + st.getServerConfig().getServer_ID() + "~address "+ st.getServerConfig().getAddress() + "~port "+ st.getServerConfig().getPort()+"~hostname "+ st.getServerConfig().getHostname() + "~TCPport "+ st.getServerConfig().getTcp_port()+"~workload " + st.getServerConfig().getWorkload();

                //String length = "" + message.length();
                byte[] buffer =message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, st.getServerConfig().getPort());
                socket.send(packet);


//                buffer = message.getBytes();
//                packet = new DatagramPacket(buffer, buffer.length, address, st.getServerConfig().getPort());
                System.out.println("Multicast server " + st.getServerConfig().getServer_ID() + " sent heartbeat!");
//                socket.send(packet);
                try{
                    this.sleep(10000);
                    //clear online servers list

                }catch(InterruptedException e){}
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            socket.close();
        }
    }
}



