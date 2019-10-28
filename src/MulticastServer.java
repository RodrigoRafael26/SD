import com.sun.security.ntlm.Server;
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

        //starts needed threads
        TCP_Server server = new TCP_Server(st, st.getServerConfig().getTcp_port());
        WebCrawler wc = new WebCrawler(st);
        ManageRequests mr1 = new ManageRequests(st);
        UpdateServers us = new UpdateServers(st);

        //start connection between server and multicast group
        try{
            socket = new MulticastSocket(port);
            System.out.println(multicast_address);
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

                st.addRequestToQueue(request);

            }


        } catch (IOException e) {
            e.printStackTrace();
        }finally{

            socket.close();

        }

    }

}

//this class has all server information, it is also used to synchronize accesss to files and queues
class Storage{
    private ConcurrentHashMap<String, CopyOnWriteArrayList<String>> searchIndex;
    private ConcurrentHashMap<String, CopyOnWriteArrayList<String>> referenceIndex;
    private ConcurrentHashMap<String, CopyOnWriteArrayList<String>> searchIndex_changes;
    private ConcurrentHashMap<String, CopyOnWriteArrayList<String>> referenceIndex_changes;

    private CopyOnWriteArrayList<User> users;
    private CopyOnWriteArrayList<String> requestQueue;
    private CopyOnWriteArrayList<User> onlineUsers;
    private CopyOnWriteArrayList<String> shareUrls;

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
        this.shareUrls = new CopyOnWriteArrayList<>();
        fillInfo();

    }
    private synchronized void fillInfo(){

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
        onlineServers.add(serverConfig);
    }

    //every time a link is added to queue notify waiting thread
    public synchronized void addLinkToQueue(String ws){
        linkList.add(ws);
        notify();
    }

    //every time a request is added to queue notify waiting thread
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


    public synchronized CopyOnWriteArrayList<ServerConfig> getOnlineServers(){
        //if onlineServers is empty wait
        while (onlineServers.size()<2){
            try{
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return onlineServers;
    }

    public boolean isServerOnline(ServerConfig s){
        for(ServerConfig temp : onlineServers) {
            if(temp.getServer_ID() == s.getServer_ID()) return true;
        }

        return false;
    }

    //notify waiting thread when another multicast server starts
    public synchronized void addOnlineServer(ServerConfig s){
        onlineServers.add(s);
        notify();
    }

    public ConcurrentHashMap<String, CopyOnWriteArrayList<String>> getSearchUpdates(){
        return searchIndex_changes;
    }

    public ConcurrentHashMap<String, CopyOnWriteArrayList<String>> getReferenceUpdates() {
        return referenceIndex_changes;
    }

    public CopyOnWriteArrayList<String> getShareUrls(){
        return shareUrls;
    }

    public synchronized void updateFiles(){
        fileHandler.writeReferenceIndex(referenceIndex);
        fileHandler.writeSearchIndex(searchIndex);
        fileHandler.writeUsers(users);
        serverConfig.updateWorkload(linkList.size());
    }

    public void removeServer(String id){
        for(ServerConfig s : onlineServers){
            if(s.getServer_ID() == Integer.parseInt(id)){
                onlineServers.remove(s);
            }
        }
    }
}

//this thread is only in charge of sending keepAlives to multicast group
class KeepAlive extends Thread{
    private Storage st;
    private int counter;
    public KeepAlive(Storage st){
        this.st = st;
        this.counter = 0;
    }

    public void run(){
        System.out.println("started keepAlive");
        DatagramSocket socket = null;
        try{
            socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(st.getServerConfig().getAddress());



            while(true){

                String message = "type | keepAlive ; serverID " + st.getServerConfig().getServer_ID() + "~address "+ st.getServerConfig().getAddress() + "~port "+ st.getServerConfig().getPort()+"~hostname "+ st.getServerConfig().getHostname() + "~TCPport "+ st.getServerConfig().getTcp_port()+"~workload " + st.getServerConfig().getWorkload();

                byte[] buffer =message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, st.getServerConfig().getPort());
                socket.send(packet);
                counter ++;
                if(counter == 60){
                    //update files every 60 seconds
                    st.updateFiles();
                }

                try{
                    this.sleep(1000);


                }catch(InterruptedException e){}
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            socket.close();
        }
    }
}



