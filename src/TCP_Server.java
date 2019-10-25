import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class TCP_Server {
    Storage st;
    public TCP_Server(Storage st){
        this.st = st;
        try{
            int serverPort = 6000; // de onde vem este server Port?

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

class Connection extends Thread{
    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    int thread_number;
    CopyOnWriteArrayList<Socket> socketList;

    public Connection (Socket aClientSocket, CopyOnWriteArrayList<Socket> socketList) {
        this.socketList = socketList;

        try{
            clientSocket = aClientSocket;
            in = new DataInputStream(clientSocket.getInputStream()); //isto é para mudar
            //out = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
    }
    //=============================
    public void run(){

        String resposta;
        try{
            while(true){

                int i = 0;
                for (Socket clientSocket: socketList) {
                    //make response hashmaps changes and workload if needed
                    resposta = "";
                    out = new DataOutputStream(clientSocket.getOutputStream());
                    out.writeUTF(resposta);
                }


            }
        }catch(EOFException e){
            System.out.println("EOF:" + e);
        }catch(IOException e){
            System.out.println("IO:" + e);
        }
    }
}
