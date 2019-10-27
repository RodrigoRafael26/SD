import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
                new Connection(clientSocket, socketList);

            }
        }catch(IOException e) {
            System.out.println("Listen:" + e.getMessage());
        }
    }

}
//TCP server only recieves information and updates server Storage
class Connection extends Thread{
    DataInputStream in;
    Socket clientSocket;
    CopyOnWriteArrayList<Socket> socketList;

    public Connection (Socket aClientSocket, CopyOnWriteArrayList<Socket> socketList) {
        this.socketList = socketList;

        try{
            clientSocket = aClientSocket;
            in = new DataInputStream(clientSocket.getInputStream());

            this.start();
        }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
    }



    public void run(){

        try{

                System.out.println("chegou aqui/ socketList size: " +socketList.size());
                int i = 0;
                for (Socket clientSocket: socketList) {
                    //update hashmaps and recieve workload if needed
                    in = new DataInputStream(clientSocket.getInputStream());
                    //out.writeUTF(resposta);
                    String synchro = in.readUTF();

                    System.out.println(synchro);
                }
                socketList.clear();
                System.out.println("saiu do for");



        }catch(EOFException e){
            e.getMessage();
            System.out.println("EOF:" + e);
        }catch(IOException e){
            System.out.println("IO:" + e);
        }
    }
}
