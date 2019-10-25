import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.registry.Registry;
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServidorRMI extends UnicastRemoteObject implements ServerInterface {
    private boolean[] servers = {false, false, false};
    int replyServer = 0;
    private static ServerInterface serverInterface;
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4320;
    private String name = "RMIServer";
    private CopyOnWriteArrayList<ClientInterface> clientsList = new CopyOnWriteArrayList<>();
    private int clientPort = 7000;
    private int i;
    String request;

    private ServidorRMI() throws RemoteException {
        MulticastConnection connect = new MulticastConnection(this);
        connect.start();
    }

    public static void main(String[] args) throws RemoteException {
        try {
            serverInterface = new ServidorRMI();
            createRegistry();
        } catch (RemoteException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void createRegistry() throws RemoteException, InterruptedException {
        /*Creates registry of new RMI server on port 7000
        If AccessException happens => prints message
        If ExportException happens => There is already a RMI server, then changes to backup RMI server*/
        int port = 7000;
        try {
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("Sporting", serverInterface);
            System.out.println("Main RMI ready!");
        } catch (AccessException e) {
            System.out.println("main.AccessException: " + e.getMessage());
        } catch (ExportException e) {
            System.out.println("There is already a RMI Server. Changing to backup...");
            secondaryRMI();
        }
    }

    private static void secondaryRMI() throws RemoteException, InterruptedException {
        /*This function is executed when a new RMI server is created but there's already a main one*/

        try {
            serverInterface = (ServerInterface) LocateRegistry.getRegistry(7000).lookup("Sporting"); // liga-se ao RMI Primário
            System.out.println("Backup RMI ready!");
        } catch (NotBoundException e) {
            System.out.println("Attempting to become primary RMI server...");
            createRegistry(); //se não der, é porque entretanto deu cagada no primário e tenta ser ele o primário
        }
        int timer = 1;
        //iniciam os pings: ao fim de 5 pings, se não tiver obtido resposta do RMI primário, torna-se primário
        while (true){
            try {
                Thread.sleep(500);
                serverInterface.ping();
            } catch (Exception e) {
                if(timer>=6){
                    System.out.println("Timeout exceeded. Attempting to be Main Server");
                    serverInterface = new ServidorRMI();
                    createRegistry();
                    break;
                }
                else {
                    try {
                        serverInterface = (ServerInterface) LocateRegistry.getRegistry(7000).lookup("Sporting"); // liga-se ao RMI Primário
                    } catch (NotBoundException exception) {
                        System.out.println("No ping received: " + timer);
                        timer++;
                    }
                }
            }
        }
    }

    public boolean[] getServers(){
        return this.servers;
    }

    public void ping() throws java.rmi.RemoteException {
    }

    public String newURL(String url) throws RemoteException {
        request = "type | getOnlineServer";
        String answer = dealWithRequest(request);



        request = "type | newURL ; url | " + url + " ; id_server | " /*+ id_server*/;
        answer = dealWithRequest(request);
        return answer.split(" ; ")[1].split(" | ")[1];
    }

    public int hello() throws RemoteException {
        clientPort++;
        return clientPort;
    }

    private String dealWithRequest(String request) {
        MulticastSocket socket = null;
        String tipo_request = request.split(" ; ")[0].split(" \\| ")[1];
        //String message = "type | " + tipo_request + " ; operation | failed";
        String message = "";
        int count = 0;

        while(count < 6){
            try {
                socket = new MulticastSocket(PORT);
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                socket.joinGroup(group);
                socket.setLoopbackMode(false);

                String length = "" + request.length();
                byte[] buffer = length.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);

                try {
                   Thread.sleep(1000);
                } catch (InterruptedException e) {}

                buffer = request.getBytes();
                //buffer = request.getBytes();
                packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);
//                System.out.println("Sent to multicast address: " + request);

                buffer = new byte[8];
                packet = new DatagramPacket(buffer, buffer.length);
                socket = new MulticastSocket(4324);
                socket.joinGroup(group);
                socket.setSoTimeout(5000);
                socket.receive(packet);

                message = new String(packet.getData(), 0, packet.getLength());
                int bufferLength = Integer.parseInt(message.trim());
                buffer = new byte[bufferLength];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                //System.out.println( packet.getData().length);
//                System.out.println("Received packet from " + packet.getAddress().getHostName() + ":" + packet.getPort() + " with message: " + message);
                break;
            } catch (SocketTimeoutException e) {
                if(count++ < 0) {
                    request = "type | resend ; " + request;
                }
                continue;
            }catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                socket.close();
            }
        }
        return message;
    }

    public boolean logout(String user) throws java.rmi.RemoteException {
        request = "type | logout ; username | " + user;
        dealWithRequest(request);
        for (ClientInterface client : clientsList) {
            try {
                if(client.getUser().equals(user))
                    clientsList.remove(client);
            } catch (RemoteException e) {
                clientsList.remove(client);
            }
        }
        return true;
    }

    public int register(String username, String password) throws RemoteException {
        request = "type | register ; username | " + username + " ; password | " + password;
        String resposta = dealWithRequest(request);
        System.out.println(resposta);
        if(resposta.equals("type | register ; operation | failed"))
            return 4;

        if (Integer.parseInt(resposta) == 1) return 1;
        else return 3;
    }

    public int login(String username, String password) throws RemoteException {
        request = "type | login ; username | " + username + " ; password | " + password;
        String ans = dealWithRequest(request);

        String tokens[] = ans.split(" ; ");
        String mes[][] = new String[tokens.length][];
        for (int i = 0; i < tokens.length; i++) mes[i] = tokens[i].split(" | ");
        return Integer.parseInt(mes[2][1]);
    }

    public String historic(String user) throws RemoteException {
        request = "type | historico ; username | " + user;
        String resposta = dealWithRequest(request);

        String tokens[] = resposta.split(" ; ");
        String mes[][] = new String[tokens.length][];
        resposta = "";
        for(int i = 0; i < tokens.length; i++) mes[i] = tokens[i].split(" | ");
        for(i = 0; i < tokens.length; i++) resposta += mes[i][1] + "\n";
        return resposta;
    }

    public ArrayList<String> pagesList(String url) throws RemoteException {
        request = "type | url_references ; url | " + url;
        ArrayList<String> resposta_list = new ArrayList<>();
        String resposta = dealWithRequest(request);

        String tokens[] = resposta.split(" ; ");
        String mes[][] = new String[tokens.length][];
        for(int i = 0; i < tokens.length; i++) mes[i] = tokens[i].split(" | ");
        for(i = 0; i < tokens.length; i++) resposta_list.add(mes[i][1]);

        return resposta_list;
    }

    public String[] tenMostImportant() throws RemoteException {
        request = "type | 10MostImportant";
        return tenMost(request);
    }

    public String[] tenMostSearched() throws RemoteException {
        request = "type | 10MostSearched";
        return tenMost(request);
    }

    private String[] tenMost(String request) {
        String[] respostaList = new String[10];
        String resposta = dealWithRequest(request);

        String tokens[] = resposta.split(" ; ");
        String mes[][] = new String[tokens.length][];
        for(int i = 0; i < tokens.length; i++) mes[i] = tokens[i].split(" \\| ");
        for(i = 0; i < tokens.length; i++) respostaList[i] = mes[i][1];

        return respostaList;
    }

    public String searchWeb(String searchText) throws RemoteException {
        request = "type | search ; text | " + searchText;
        String resposta = dealWithRequest(request);

        if(resposta == null) {
            return "No " + searchText + "!";
        }

        String[] splitted = resposta.split(" ; ");
        return splitted[2].split(" \\| ")[1];
    }

    public void newClient(int port, String myHost) throws RemoteException {
        ClientInterface client;
        System.out.println("port: " + port + " | clientIP: " + myHost);

        while (true) {
            try {
                client = (ClientInterface) LocateRegistry.getRegistry(myHost, port).lookup("Benfica");
                System.out.println(client == null);
                break;
            } catch (NotBoundException e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(client.getUser() + " na lista de clientes");
        clientsList.add(client);
        String resposta = dealWithRequest("type | get_notifications ; username | " + client.getUser());
        System.out.println(resposta + "AQUI");
        String tokens[] = resposta.split(" ; ");
        String mes[][] = new String[tokens.length][];
        for (int i = 0; i < tokens.length; i++) mes[i] = tokens[i].split(" \\| ");

        int counter = Integer.parseInt(mes[1][1]);
        if(counter > 0){
            System.out.println(mes[2][1]);
            sendNotification(mes[2][1], client.getUser());
        }
    }

    private void sendNotification(String s, String user) {
        System.out.println("Notification: " + s + ": to user " + user);
        for (ClientInterface client : clientsList) {
            try {
                if (client.getUser() == null)
                    continue;
                if (client.getUser().equals(user)){
                    client.notification(s);
                    return;
                }
            } catch (RemoteException e) {
                clientsList.remove(client);
                System.out.println("Out of list");
            }
        }

        System.out.println("O cliente nao esta online");

        request = "type | notification ; username | " + user + " ; message | " + s;
        System.out.println(request);

        dealWithRequest(request);
    }

    public boolean givePrivileges(String usernameOldAdmin, String usernameFutureAdmin) throws RemoteException {
        request = "type | give_privilege ; username | " + usernameFutureAdmin;

        String resposta = dealWithRequest(request);
        if (resposta.equals("type | give_privilege ; operation | succeeded")) {
            String message = "You are admin now!";
            sendNotification(message, usernameFutureAdmin);
            return true;
        }
        return false;
    }
}


class MulticastConnection extends Thread {
    private int PORT = 4360;
    ServidorRMI serverRmi;
    int[] servers = {0, 0, 0};
    int serverCounter = 0;

    public MulticastConnection(ServidorRMI server) {
        super("RMIChecker");
        this.serverRmi = server;
    }

    public void run() {
        DatagramSocket socket = null;
        try {
            int currentRequest, serverNumber;
            socket = new DatagramSocket(PORT);
            byte[] buffer = new byte[32];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                try {
                    socket.setSoTimeout(1000);
                    socket.receive(packet);
                    System.out.println("asdasd");
                    String message = new String(packet.getData(), 0, packet.getLength());
                    System.out.println(message);
                    String[] aux = message.split(" ; ");
                    String[][] info = new String[2][];
                    info[0] = aux[0].split(" \\| ");
                    info[1] = aux[1].split(" \\| ");
                    serverNumber = Integer.parseInt(info[1][0].trim());
                    currentRequest = Integer.parseInt(info[1][1].trim());

                    if ((serverRmi.getServers()[serverNumber-1]) && this.servers[serverNumber-1] < 28){
                        serverRmi.getServers()[serverNumber-1] = false;
                    }

                    if (!serverRmi.getServers()[serverNumber - 1]) {
                        if(serverCounter++ > 0){
                            String request = "type | config ; currentRequest | " + currentRequest;
//                            request = serverRmi.setReplyServer(request, "config");
                            MulticastSocket multicastSocket = null;
                            try{
                                multicastSocket = new MulticastSocket();
                                InetAddress address = InetAddress.getByName("224.0.224.0");
                                String length = request.length() + "";
                                buffer = length.getBytes();
                                packet = new DatagramPacket(buffer, buffer.length, address, 4320);
                                multicastSocket.send(packet);
                                try{
                                    Thread.sleep(100);
                                }catch (InterruptedException e){

                                }
                                System.out.println("Sent to multicast: " + length);
                                buffer = request.getBytes();
                                packet = new DatagramPacket(buffer, buffer.length, address, 4323);
                                multicastSocket.send(packet);
                                System.out.println("Sent to multicast: " + request);
                            }catch(IOException e){
                                e.printStackTrace();
                            }finally {
                                multicastSocket.close();
                            }
                        }
                        serverRmi.getServers()[serverNumber-1] = true;
                        System.out.println("Server " + serverNumber + " is up!");
                    }
                    this.servers[serverNumber -1] = 0;
                }catch (SocketTimeoutException e) {
                    for (int i = 0; i < serverRmi.getServers().length; i++) {
                        if (serverRmi.getServers()[i]) {
                            if (++this.servers[i] > 35) {
                                System.out.println("Server " + (i + 1) + " is dead!");
                                serverRmi.getServers()[i] = false;
                                this.serverCounter--;
                            }
                        }
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (NullPointerException e1) {
            e1.printStackTrace();
        } finally {
            socket.close();
        }
    }
}