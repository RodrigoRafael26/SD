import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.ConnectException;
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
    private boolean[] servers = {false, false};
    int replyServer = 0;
    private static ServerInterface serverInterface;
    //passar parametros multicast por parametro / file
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4320;
    private String name = "RMIServer";
    private CopyOnWriteArrayList<ClientInterface> clientsList = new CopyOnWriteArrayList<>();
    private int clientPort = 7000;
    private int i;
    String request;

    private ServidorRMI() throws RemoteException {
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
        } catch (ConnectException | NotBoundException e) {
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
                    } catch (ConnectException | NotBoundException exception) {
                        System.out.println("No ping received: " + timer);
                        timer++;
                    }
                }
            }
        }
    }

    public void ping() throws java.rmi.RemoteException {
    }

//    1 - envia type | getOnlineServer
//    2 - recebe type | getOnlineServer ; server | id ; carga | carga
//    3 - envia type | newURL ; url | url ; id_server | id
//    4 - recebe type | status ; operation | succeed (ou failed)
    public String newURL(String url) throws RemoteException {
        request = "type | getOnlineServer";
        String answer = dealWithRequest(request);

        int nrServers = (answer.split(" ; ").length - 1) / 2;
        String[] tokens = answer.split(" ; ");
        String[][] aux = new String[nrServers][2];
        int menor = -1;
        String id_server = null;

        for (i = 1; i <= nrServers; i++) {
            aux[i-1][0] = tokens[2*i - 1].split(" \\| ")[1];
            aux[i-1][1] = tokens[2*i].split(" \\| ")[1];
        }

        for(i = 0; i < aux.length; i++){
            if (menor == -1){
                menor = Integer.parseInt(aux[i][1]);
                id_server = aux[i][0];
            }
            if(Integer.parseInt(aux[i][1]) < menor){
                menor = Integer.parseInt(aux[i][1]);
                id_server = aux[i][0];
            }
        }

        request = "type | newURL ; url | " + url + " ; id_server | " + id_server;
        answer = dealWithRequest(request);

        return answer.split(" ; ")[1].split(" \\| ")[1];
    }

    public int hello() throws RemoteException {
        clientPort++;
        return clientPort;
    }

    private String dealWithRequest(String request) {
        MulticastSocket socket = null;
        String message = "";
        int count = 0;

        while(count < 6){
            try {
                socket = new MulticastSocket(PORT);
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                socket.joinGroup(group);
                socket.setLoopbackMode(false);

////              Envia para multicast o tamanha do buffer
//                String length = "" + request.length();
//                byte[] buffer = length.getBytes();
//                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
//                socket.send(packet);

                try {
                   Thread.sleep(1000);
                } catch (InterruptedException e) {}

//              envia para multicast o request
                byte[] buffer = request.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                buffer = request.getBytes();
                packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);
                System.out.println("Sent to multicast address: " + request);


                buffer = new byte[10000];
                packet = new DatagramPacket(buffer, buffer.length);
                socket = new MulticastSocket(4324);
                socket.joinGroup(group);
                //socket.setSoTimeout(5000);
                socket.receive(packet);

////                ja recebeu o tamanho do buffer e agora vai receber a resposta ao request
//                message = new String(packet.getData(), 0, packet.getLength());
//                int bufferLength = Integer.parseInt(message.trim());
//                buffer = new byte[bufferLength];
//                packet = new DatagramPacket(buffer, buffer.length);
//                socket.receive(packet);

                message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received packet from " + packet.getAddress().getHostName() + ":" + packet.getPort() + " with message: " + message);
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

//    1 - envia type | logout ; username | username
//    2 - pode receber qualquer coisa
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

//    1 - envia type | register ; username | username ; password | password;
//    2 - recebe type | status ; operation | failed ou entao type | status ; operation | succeeded ; isAdmin | true (ou false)
    public int register(String username, String password) throws RemoteException {
        request = "type | register ; username | " + username + " ; password | " + password;
        String resposta = dealWithRequest(request);
        if(resposta.equals("type | status ; operation | failed")) return 3;
        else if (resposta.equals("type | status ; operation | success ; isAdmin | true")) return 1;
        else return 2;
    }

//    1 - envia type | login ; username | username ; password | password;
//    2 - recebe type | status ; operation | failed ou entao type | status ; operation | succeeded ; isAdmin | true (ou false)
    public int login(String username, String password) throws RemoteException {
        request = "type | login ; username | " + username + " ; password | " + password;
        String resposta = dealWithRequest(request);

        if(resposta.equals("type | status ; operation | failed")) return 3;
        else if (resposta.equals("type | status ; operation | success ; isAdmin | true")) return 1;
        else return 2;
    }

//    1 - envia type | historico ; username | username
//    2 - recebe type | historico ; value | addas ; value | asfdsa
    public String historic(String user) throws RemoteException {
        request = "type | historico ; username | " + user;
        String resposta = dealWithRequest(request);
        String[][] aux = null;

        String[] tokens = resposta.split(" ; ");
        if (tokens.length > 1){
            aux = new String[tokens.length-1][];
            String ans = "";

            for(i = 1; i < tokens.length; i++) aux[i-1] = tokens[i].split(" \\| ");
            for(i = 0; i < aux.length; i++) ans += aux[i][1] + "\n";
            return ans;
        }
        return "Your historic is clear";
    }

//    1 - envia type | url_references ; url | url
//    2 - recebe type | url_references ; item_count | 123 ; url | dsaf ; url | asdfa ...
    public String pagesList(String url) throws RemoteException {
        request = "type | url_references ; url | " + url;
        String resposta = dealWithRequest(request);

        if (resposta.compareTo("type | status ; operation | failed") == 0) return "Any result for " + url;

        String[] tokens = resposta.split(" ; ");
        String[][] aux = new String[tokens.length][];
        int size = Integer.parseInt(tokens[1].split(" \\| ")[1]);
        String list = null;

        for(int i = 2; i < size; i++) aux[i - 2] = tokens[i].split(" \\| ");
        for(i = 0; i < aux.length; i++) list += " " + aux[i][1] + " ;";

        return list;
    }

//    1 - envia type | 10MostImportant
//    2 - recebe type | 10MostImportant ; url | dsaf ; url | asdfa ...
    public String tenMostImportant() throws RemoteException {
        request = "type | 10MostImportant";
        return tenMost(request);
    }
//    1 - envia type | 10MostSearched
//    2 - recebe type | 10MostSearched ; url | dsaf ; url | asdfa ...
    public String tenMostSearched() throws RemoteException {
        request = "type | 10MostSearched";
        return tenMost(request);
    }

    private String tenMost(String request) {
        String resposta = dealWithRequest(request);

        String[] tokens = resposta.split(" ; ");
        String[][] aux = new String[tokens.length-1][];
        String ans = "";

        for(int i = 1; i < tokens.length; i++) aux[i-1] = tokens[i].split(" \\| ");
        for(i = 0; i < aux.length; i++) ans += " " + aux[i][1] + " ;";
        if (ans.length() == 0) return "No searches done";
        return ans;
    }

//    1 - envia type | search ; text | text
//    2 - recebe type | search ; item_count | 13241 ; url | adad
    public String searchWeb(String searchText, String username) throws RemoteException {
        request = "type | search ; username | "+ username +" ; text | " + searchText;
        String resposta = dealWithRequest(request);

        String[] tokens = resposta.split(" ; ");
        String[][] aux = new String[tokens.length-2][2];
        int size = Integer.parseInt(tokens[1].split(" \\| ")[1]);
        String ans = "";

        if(size == 0) {
            return "Any result for your search";
        }

        for(i = 2; i < tokens.length; i++) aux[i-2] = tokens[i].split(" \\| ");
        for(i = 0; i < aux.length; i++) ans += " " + aux[i][1] + " ;";
        ans += "\nExistem no total " + size + " resultados para a tua pesquisa";
        return ans;
    }

//    usa o get_notifications
//    1 - envia type | get_notifications ; username | afaf
//    2 - recebe type | get_notifications ; item_count | 123 ; not | text ...
    public void newClient(int port, String clientIP) throws RemoteException {
        ClientInterface client;
        System.out.println("port: " + port + " | clientIP: " + clientIP);

        while (true) {
            try {
                client = (ClientInterface) LocateRegistry.getRegistry(clientIP, port).lookup("Benfica");
                System.out.println(client == null);
                break;
            } catch (ConnectException | NotBoundException e) {
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
        System.out.println(resposta);
        String[] tokens = resposta.split(" ; ");
        int size = Integer.parseInt(tokens[1].split(" \\| ")[1]);
        String[][] aux = new String[tokens.length-2][];
        if(size > 0) {
            for (i = 2; i < tokens.length; i++) aux[i] = tokens[i].split(" \\| ");
        }
        for (i = 0; i < aux.length; i++) {
            sendNotification(aux[i][1], client.getUser());
        }
    }

//    caso o user nao esteja online
//    1 - envia type | notification ; username | user ; message | sdaads
//    2 - recebe qualquer coisa
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
        dealWithRequest(request);
    }

//    1 - envia type | give_privilege ; username | futureAdmin
//    2 - recebe type | give_privilege ; operation | succeeded (ou failed)
    public boolean givePrivileges(String usernameOldAdmin, String usernameFutureAdmin) throws RemoteException {
        request = "type | give_privilege ; username | " + usernameFutureAdmin;

        String resposta = dealWithRequest(request);
        if (resposta.equals("type | status ; operation | success")) {
            String message = "You are admin now!";
            sendNotification(message, usernameFutureAdmin);
            return true;
        }
        return false;
    }
}

