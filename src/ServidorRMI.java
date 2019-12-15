import java.io.IOException;
import java.rmi.*;
import java.rmi.ConnectException;
import java.rmi.registry.Registry;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verifier;
import com.github.scribejava.core.oauth.OAuthService;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uc.sd.apis.FacebookApi2;

import static com.github.scribejava.core.model.OAuthConstants.EMPTY_TOKEN;

public class ServidorRMI extends UnicastRemoteObject implements ServerInterface {
    private static ServerInterface serverInterface;
    //passar parametros multicast por parametro / file
    private static String MULTICAST_ADDRESS = "224.1.224.1";
    private static int PORT = 4320;
    private CopyOnWriteArrayList<ClientInterface> clientsList = new CopyOnWriteArrayList<>();
    private int clientPort = 7000;
    private int i;
    private String request, confirmRequest;
    private UUID uuid;
    private String tenMostSearched = "", tenMostImportant = "";


    //web clients
    private CopyOnWriteArrayList<ClientInterface> browserUsers = new CopyOnWriteArrayList<>();


    public String apiKey = "437779406889234";
    public String apiSecret = "719d5061f341819d7ec6dec5f1ba17a0";
    public OAuthService service = new ServiceBuilder()
                .provider(FacebookApi2.class)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .callback("https://localhost:8443/ucBusca/loginFBSuccess") // Do not change this.
                .scope("public_profile")
                .build();

    private ServidorRMI() throws RemoteException {}

    public static void main(String[] args) throws RemoteException {
        PORT = Integer.parseInt(args[0]);
        MULTICAST_ADDRESS = args[1];

        try {

            serverInterface = new ServidorRMI();
            createRegistry();
        } catch (RemoteException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void createRegistry() throws RemoteException, InterruptedException {
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
        try {
            serverInterface = (ServerInterface) LocateRegistry.getRegistry(7000).lookup("Sporting"); // liga-se ao RMI Primário
            System.out.println("Backup RMI ready!");
        } catch (ConnectException | NotBoundException e) {
            System.out.println("Attempting to become primary RMI server...");
            createRegistry();
        }
        int timer = 1;
        //inicio dos pings: ao fim de 5 pings, se não tiver obtido resposta do RMI primário, torna-se primário
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

    //    1 - envia type | getOnlineServer ; uuid | uuid_example
//    2 - recebe type | getOnlineServer ; uuid | uuid_example ; server | id ; carga | carga
//    3 - envia type | newURL ; uuid | uuid_example ; url | url ; id_server | id
//    4 - recebe type | status ; uuid | uuid_example ; operation | succeed (ou failed)
    public String newURL(String url) throws RemoteException {
        uuid = UUID.randomUUID();
        request = "type | getOnlineServer ; uuid | " + uuid;
        String answer = dealWithRequest(request);
        while(!answer.contains(request)){
            answer = dealWithRequest(request);
        }

        int nrServers = (answer.split(" ; ").length - 2) / 2;
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
                System.out.println(aux[i][1]);
                menor = Integer.parseInt(aux[i][1]);
//                id_server = aux[i][0];
            }
            if(Integer.parseInt(aux[i][1]) < menor){
                menor = Integer.parseInt(aux[i][1]);
//                id_server = aux[i][0];
            }
        }
        id_server = ""+menor;
        uuid = UUID.randomUUID();
        confirmRequest = "type | status ; uuid | " + uuid;
        request = "type | newURL ; uuid | " +uuid+" ; url | " + url + " ; id_server | " + id_server;
        answer = dealWithRequest(request);
        while(!answer.contains(confirmRequest)){
            answer = dealWithRequest(request);
        }
        if (answer.contains("failed"))
            return "failed";
        if(tenMostImportant().compareTo(tenMostImportant) == 0){
            return "success";
        }else{
            this.updateTenMostImportant(tenMostImportant);
            return "success ;;; UPDATE";
        }
    }

    public int addPort() throws RemoteException {
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

////              Envia para multicast o tamanho do buffer
//                String length = "" + request.length();
//                byte[] buffer = length.getBytes();
//                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
//                socket.send(packet);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}

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
                socket.setSoTimeout(5000);
                socket.receive(packet);


                message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received packet from " + packet.getAddress().getHostName() + ":" + packet.getPort() + " with message: " + message);
                break;
            } catch (SocketTimeoutException e) {
                if(count++ < 0) {
                    request = "type | resend ; " + request;
                }
            }catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                socket.close();
            }
        }
        return message;
    }

    //    1 - envia type | logout ; uuid | uuid_example ; username | username
//    2 - recebe type | logout ; uuid | uuid_example ; status | succeed (ou failed)
    public boolean logout(String user) throws java.rmi.RemoteException {
        uuid = UUID.randomUUID();
        request = "type | logout ; uuid | " + uuid + " ; username | " + user;
        confirmRequest = "type | status ; uuid | " + uuid;
        String answer = dealWithRequest(request);
        while(!answer.contains(confirmRequest) || answer.contains("failed")){
            answer = dealWithRequest(request);
        }
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

    //    1 - envia type | register ; uuid | uuid_example ; username | username ; password | password ; facebookID | facebookID
//    2 - recebe type | status ; uuid | uuid_example ; operation | failed ou entao type | status ; uuid | uuid_example ; operation | succeeded ; isAdmin | true (ou false)
    public int register(String username, String password, String facebookID) throws RemoteException {
        uuid = UUID.randomUUID();
        confirmRequest = "type | status ; uuid | "+ uuid;
        request = "type | register ; uuid | " + uuid + " ; username | " + username + " ; password | " + password + " ; facebookID | "+facebookID  ;
        String answer = dealWithRequest(request);
        while(!answer.contains(confirmRequest)){
            answer = dealWithRequest(request);
        }
        if(answer.contains("failed")) return 3;
        else if (answer.contains("true")) return 1;
        else return 2;
    }

    public int register(String username, String password, String facebookID, ClientInterface tomcat) throws RemoteException {
        uuid = UUID.randomUUID();
        confirmRequest = "type | status ; uuid | "+ uuid;
        request = "type | register ; uuid | " + uuid + " ; username | " + username + " ; password | " + password + " ; facebookID | "+facebookID  ;
        String answer = dealWithRequest(request);
        while(!answer.contains(confirmRequest)){
            answer = dealWithRequest(request);
        }
        if(answer.contains("failed")) return 3;
        else if (answer.contains("true")) {
            clientsList.add(tomcat);
            return 1;
        }
        else{
            clientsList.add(tomcat);
            return 2;
        }
    }

    //    1 - envia type | login ; uuid | uuid_example ; username | username ; password | password;
//    2 - recebe type | status ; uuid | uuid_example ; operation | failed ou entao type | status ; uuid | uuid_example ; operation | succeeded ; isAdmin | true (ou false)
    public int login(String username, String password) throws RemoteException {
        uuid = UUID.randomUUID();
        confirmRequest = "type | status ; uuid | " + uuid;
        request = "type | login ; uuid | "+uuid+" ; username | " + username + " ; password | " + password;
        String answer = dealWithRequest(request);
        while(!answer.contains(confirmRequest)){
            answer = dealWithRequest(request);
        }
        System.out.println(clientsList.size());
        if(answer.contains("failed")) return 3;
        else if (answer.contains("true")) return 1;
        else return 2;
    }

//    public int login(String username, String password, ClientInterface tomcat) throws RemoteException {
//        uuid = UUID.randomUUID();
//        confirmRequest = "type | status ; uuid | " + uuid;
//        request = "type | login ; uuid | "+uuid+" ; username | " + username + " ; password | " + password;
//        String answer = dealWithRequest(request);
//        while(!answer.contains(confirmRequest)){
//            answer = dealWithRequest(request);
//        }
//        System.out.println(clientsList.size());
//        if(answer.contains("failed")) return 3;
//        else if (answer.contains("true")) {
//            clientsList.add(tomcat);
//            return 1;
//        }else{
//            clientsList.add(tomcat);
//            return 2;
//        }
//    }

    //    1 - envia type | historico ; uuid | uuid_example ; username | username
//    2 - recebe type | historico ; uuid | uuid_example ; value | addas ; value | asfdsa
    public String historic(String user) throws RemoteException {
        uuid = UUID.randomUUID();
        confirmRequest = "type | historico ; uuid | " + uuid;
        request = confirmRequest + " ; username | " + user;
        String answer = dealWithRequest(request);
        while(!answer.contains(confirmRequest)){
            answer = dealWithRequest(request);
        }

        String[] tokens = answer.split(" ; ");
        if (tokens.length > 2){
            String[][] aux = new String[tokens.length-2][];
            String ans = "";

            for(i = 2; i < tokens.length; i++) aux[i-2] = tokens[i].split(" \\| ");
            for(i = 0; i < aux.length; i++) ans += aux[i][1] + "\n";
            return ans;
        }
        return "Your historic is clear";
    }

    //    1 - envia type | url_references ; uuid | uuid_example ; url | url
//    2 - recebe type | url_references ; uuid | uuid_example ; item_count | 123 ; url | dsaf ; url | asdfa ...
    public String pagesList(String url) throws RemoteException {
        uuid = UUID.randomUUID();
        confirmRequest = "type | status ; uuid | " + uuid;
        request = "type | url_references ; uuid | " + uuid + " ; url | " + url;
        String answer = dealWithRequest(request);
        while(!answer.contains(confirmRequest)){
            answer = dealWithRequest(request);
        }

        if (answer.contains("failed")) return "Any result for " + url;

        String[] tokens = answer.split(" ; ");
        int size = Integer.parseInt(tokens[2].split(" \\| ")[1]);
        String[][] aux = new String[size][];
        String list = "";

        for(int i = 3; i < tokens.length; i++) aux[i - 3] = tokens[i].split(" \\| ");
        for(i = 0; i < aux.length; i++) list += aux[i][1] + "\n";
        System.out.println(list);
        return list;
    }

    //    1 - envia type | 10MostImportant ; uuid | uuid_example
//    2 - recebe type | 10MostImportant ; uuid | uuid_example ; url | dsaf ; url | asdfa ...
    public String tenMostImportant() throws RemoteException {
        String tenMostRequest;
        uuid = UUID.randomUUID();
        request = "type | 10MostImportant ; uuid | " + uuid;
        tenMostRequest = tenMost(request);
        if (tenMostImportant.compareTo(tenMostRequest) == 0){
            tenMostImportant = tenMostRequest;
            return tenMostImportant;
        }else{
            tenMostImportant = tenMostRequest;
            return tenMostRequest + " ;;; UPDATE";
        }
    }

    //    1 - envia type | 10MostSearched ; uuid | uuid_example
//    2 - recebe type | 10MostSearched ; uuid | uuid_example ; url | dsaf ; url | asdfa ...
    public String tenMostSearched() throws RemoteException {
        String tenMostRequest;
        uuid = UUID.randomUUID();
        request = "type | 10MostSearched ; uuid | " + uuid;
        tenMostRequest = tenMost(request);
        if (tenMostSearched.compareTo(tenMostRequest) == 0){
            tenMostSearched = tenMostRequest;
            return tenMostSearched;
        }else{
//            tenMostSearched = tenMostRequest;
            this.updateTenMostSearched(tenMostRequest);
            return tenMostRequest + " ;;; UPDATE";
        }
    }

    private String tenMost(String request) {
        String answer = dealWithRequest(request);
        while(!answer.contains(request)){
            answer = dealWithRequest(request);
        }

        String[] tokens = answer.split(" ; ");
        String[][] aux = new String[tokens.length-2][];
        String ans = "";

        for(int i = 2; i < tokens.length; i++) aux[i-2] = tokens[i].split(" \\| ");
        for(i = 0; i < aux.length; i++) ans += aux[i][1] + "\n";
        if (ans.length() == 0) return "No searches done";
        return ans;
    }

    //    1 - envia type | search ; uuid | uuid_example ; text | text
//    2 - recebe type | search ; uuid | uuid_example ; item_count | 13241 ; url | adad
    public String searchWeb(String searchText, String username) throws RemoteException {
        uuid = UUID.randomUUID();
        confirmRequest = "type | search ; uuid | " + uuid;
        request = confirmRequest + " ; username | "+ username +" ; text | " + searchText;

        String answer = dealWithRequest(request);

        while(!answer.contains(confirmRequest)) {
            answer = dealWithRequest(request);
        }

        String[] tokens = answer.split(" ; ");
        String[][] aux = new String[tokens.length-2][2];

        int size = Integer.parseInt(tokens[2].split(" \\| ")[1]);
        String ans = "";

        if(size == 0) {
            return "Any result for your search";
        }

        for(i = 3; i < tokens.length; i++) aux[i-3] = tokens[i].split(" \\| ");
        for(i = 0; i < aux.length; i++) {
            if(aux[i][1]!=null){

                ans += aux[i][1] + "\n";
            }
        }
        if(tenMostSearched().contains("UPDATE"))
            ans += " ;;; UPDATE";
        //ans += "\nExistem no total " + size + " resultados para a tua pesquisa";
        return ans;
    }

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
    }

    //    usa o get_notifications
//    1 - envia type | get_notifications ; uuid | uuid_example ; username | afaf
//    2 - recebe type | get_notifications ; uuid | uuid_example ; item_count | 123 ; not | text ...
    public String verifyNotification(String username) {
        String resposta = "";
        int size = 0;
        uuid = UUID.randomUUID();
        confirmRequest = "type | notifications ; uuid | " + uuid;
        if(username == null){
            System.out.println("FDS");
            return "";
        }
        request = "type | get_notifications ; uuid | " + uuid + " ; username | " + username;
        String answer = dealWithRequest(request);

        while(!answer.contains(confirmRequest)) {
            answer = dealWithRequest(request);
        }

        String[] tokens = answer.split(" ; ");
        try {
            size = Integer.parseInt(tokens[2].split(" \\| ")[1]);
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
        String[][] aux = new String[tokens.length-2][];
        if (size == 0){
            return "";
        }
        for (i = 3; i < tokens.length; i++) aux[i-3] = tokens[i].split(" \\| ");
        for (i = 0; i < (aux.length/2); i++) resposta += aux[i][1]+"\n";
        return resposta;
    }


    //    caso o user nao esteja online
//    1 - envia type | notification ; uuid | uuid_example ; username | user ; message | sdaads
//    2 - recebe type | notification ; uuid | uuid_example
    public void sendNotification(String s, String user) {
        System.out.println("AQUIIIIIIII");
        for (ClientInterface c : clientsList) {
            try {
                if (c.getUser() == null)
                    continue;
                if (c.getUser().equals(user)) {
                    c.notification(s);
                    return;
                }
            } catch (RemoteException e) {
                clientsList.remove(c);
                System.out.println("saiu da lista");
            }
        }
        uuid = UUID.randomUUID();
        confirmRequest = "type | notification ; uuid | " + uuid;
        request = confirmRequest + " ; username | " + user + " ; message | " + s;
        String answer = dealWithRequest(request);
        while(!answer.contains(confirmRequest) && !answer.contains("success")) {
            answer = dealWithRequest(confirmRequest);
        }
    }

    @Override
    public int newTomcat(ClientInterface rmiBean) throws RemoteException {
        //ClientInterface client;
        //System.out.println("port: " + this.port + " | clientIP: " + clientIP);

        browserUsers.add(rmiBean);
        return 0;
    }

    public void updateTenMostSearched(String tenMostSearched){
        System.out.println("ENTROU NO UPDATE");
        for(ClientInterface c : clientsList){
            try {
                if(c.getPerk()==1){
                    c.writeTenMostSearch(tenMostSearched);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        for(ClientInterface c : browserUsers){

            try {
                System.out.println("DEBUG TEN MOST");
                c.writeTenMostSearch(tenMostSearched);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }

    public void updateTenMostImportant(String tenMost){
        for(ClientInterface c : clientsList){
            try {
                if(c.getPerk()==1){
                    c.writeTenMostImportant(this.tenMostImportant);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        for(ClientInterface c : browserUsers){

            try {
                c.writeTenMostSearch(this.tenMostImportant);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }


    //    1 - envia type | give_privilege ; uuid | uuid_example ; username | futureAdmin
//    2 - recebe type | give_privilege ; uuid | uuid_example ; operation | succeeded (ou failed)
    public boolean givePrivileges(String usernameOldAdmin, String usernameFutureAdmin) throws RemoteException {
        uuid = UUID.randomUUID();
        confirmRequest = "type | status ; uuid | " + uuid;
        request = "type | give_privilege ; uuid | " + uuid + " ; username | " + usernameFutureAdmin;

        String answer = dealWithRequest(request);
        while(!answer.contains(confirmRequest) && !answer.contains("success")) {
            answer = dealWithRequest(confirmRequest);
        }

        if (answer.contains("success")) {
            String message = "You are admin now!";
            sendNotification(message, usernameFutureAdmin);
            return true;
        }
        return false;
    }

    @Override
    public String loginFacebook() throws RemoteException {
//        String NETWORK_NAME = "Facebook";

        String authorizationUrl =  this.service.getAuthorizationUrl(null);

        System.out.println(authorizationUrl);

        return authorizationUrl;

    }

    @Override
    public String facebookSucccess(String code, String user) throws RemoteException {
        String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/me";
        Verifier verifier = new Verifier(code);
        // Trade the Request Token and Verfier for the Access Token
        Token accessToken = service.getAccessToken(null, verifier);

//        System.out.println("(if your curious it looks like this: " + accessToken + " )");
        System.out.println("USER=====");
        System.out.println(user);
        OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL, service);
        service.signRequest(accessToken, request);
        Response response = request.send();
        String resp="";
        Object obj = null;
        try {
            obj = new JSONParser().parse(response.getBody());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // typecasting obj to JSONObject
        JSONObject jo = (JSONObject) obj;
        System.out.println(jo.get("id"));
        String fb_id = (String) jo.get("id");
        System.out.println(fb_id);
        if(user==null) {


            int perk = 0;
            //check if user already exists
            //type | get_fb_user ; uuid | uuid ; fb_id | fb_id;
            //recebe type | get_fb_user ; uuid | uuid ; username | username ; perk | perk ;
            uuid = UUID.randomUUID();
            String requestMulticast = "type | get_fb_user ; uuid | " + uuid + " ; fb_id | " + fb_id;
            System.out.println("AQUI");
            confirmRequest = "type | get_fb_user ; uuid | " + uuid;
            String answer = dealWithRequest(requestMulticast);

            while (!answer.contains(confirmRequest)) {
                answer = dealWithRequest(requestMulticast);
            }
            if (answer.contains("failed")) {
                perk = 3;
            } else {
                String[] tokens = answer.split(" ; ");
                String[][] aux = new String[tokens.length - 2][2];


                for (i = 2; i < tokens.length; i++) aux[i - 2] = tokens[i].split(" \\| ");

                String username = aux[0][1];
                perk = Integer.parseInt(aux[1][1]);
                resp = "username | " + username + " ; perk | " + perk;
            }

            if (perk == 1 || perk == 2) {

            } else {
                //se nao existir regista e manda
                perk = this.register(fb_id, fb_id, fb_id);
                resp = "username | " + fb_id + " ; perk | " + perk;
                System.out.println(resp);

            }
        }else{
             resp = this.linkFacebook(user, fb_id);

        }
        return resp;
    }

    public String linkFacebook(String username, String fb_id){
        uuid = UUID.randomUUID();
        String requestMulticast = "type | associate_account ; uuid | "+ uuid +" ; username | "+username+" ; fb_id | "+fb_id;
        System.out.println("AQUI");
        confirmRequest = "type | associate_account ; uuid | " + uuid;
        String answer = dealWithRequest(requestMulticast);

        while(!answer.contains(confirmRequest)) {
            answer = dealWithRequest(requestMulticast);
        }
        String resp = "";
        if(answer.contains("failed")){

        }
        return "";
    }

}

