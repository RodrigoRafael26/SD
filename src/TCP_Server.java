import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TCP_Server extends Thread{
    private Storage st;
    private int serverPort;

    public TCP_Server(Storage st, int tcp_port){
        this.st = st;
        this.serverPort = tcp_port;
        this.start();
    }
    public void run(){
        System.out.println("started tcp server");
        try{
            ServerSocket listenSocket = new ServerSocket(serverPort);

            CopyOnWriteArrayList<Socket> socketList = new CopyOnWriteArrayList<Socket>();
            while(true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())="+clientSocket);
                socketList.add(clientSocket);
                new Connection(st,clientSocket, socketList);

            }
        }catch(IOException e) {
            System.out.println("Listen:" + e.getMessage());
        }
    }

}
//TCP server only recieves information and updates server Storage
class Connection extends Thread{
    DataInputStream in;
    Storage st;
    Socket clientSocket;
    CopyOnWriteArrayList<Socket> socketList;
    ConcurrentHashMap<String, CopyOnWriteArrayList<String>> addSearch;
    ConcurrentHashMap<String, CopyOnWriteArrayList<String>> addReference;
    CopyOnWriteArrayList<String> addLinks;

    public Connection (Storage st,Socket aClientSocket, CopyOnWriteArrayList<Socket> socketList) {
        this.socketList = socketList;
        this.addSearch = new ConcurrentHashMap<>();
        this.addReference = new ConcurrentHashMap<>();
        this.addLinks = new CopyOnWriteArrayList<>();
        this.st = st;

        try{
            clientSocket = aClientSocket;
            in = new DataInputStream(clientSocket.getInputStream());

            this.start();
        }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
    }



    public void run(){


        try{

            System.out.println("chegou aqui/ socketList size: " +socketList.size());

            for (Socket clientSocket: socketList) {
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream());

                //posso escrever respostas entre cada um
                addSearch = (ConcurrentHashMap<String, CopyOnWriteArrayList<String>>) is.readObject();
                addReference = (ConcurrentHashMap<String, CopyOnWriteArrayList<String>>) is.readObject();
                addLinks = (CopyOnWriteArrayList<String>) is.readObject();

                System.out.println("SEARCH UPDATES: " + addSearch.toString());
                System.out.println("REFERENCE UPDATES: "+ addReference.toString());
                System.out.println("Link Updates: " + addLinks.toString());

               this.merge();

            }
            System.out.println("TERMINA O FOR");
            socketList.clear();


        }catch(EOFException e){
            e.getMessage();
            System.out.println("EOF:" + e);
        }catch(IOException e){
            System.out.println("IO:" + e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void merge(){
        //merge searchList
        //go through all words that were changed in the other server
        for(String s: addSearch.keySet()){
            //if this server already has that word
            if(st.getSearchHash().keySet().contains(s)){
                for(String temp : addSearch.get(s)){
                    //check if the references on updates sent by the other server are already in this server hash
                    if(!st.getSearchHash().get(s).contains(temp)){
                        //if not add them
                        st.getSearchHash().get(s).add(temp);
                    }
                }
            }else{
                //if the server doesnt have the words yet add the entire hash row
                st.getSearchHash().put(s,addSearch.get(s));
            }
        }

        //merge references
        //go through all urls that were changed in the other server
        for(String s: addReference.keySet()){
            //if this server already has that url
            if(st.getReferenceHash().keySet().contains(s)){
                for(String temp : addReference.get(s)){
                    //check if the references on updates sent by the other server are already in this server hash
                    if(!st.getReferenceHash().get(s).contains(temp)){
                        //if not add them
                        st.getReferenceHash().get(s).add(temp);
                    }
                }
            }else{
                //if the server doesnt have the url yet add the entire hash row
                st.getReferenceHash().put(s,addReference.get(s));
            }
        }

        //add all the new URLs to queue so they can be indexed
        for(String url : addLinks){
            st.addLinkToQueue(url);
        }

        //after all updates save server status
        st.updateFiles();
    }
}



