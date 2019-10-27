import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ManageRequests extends Thread {
    private Storage server_Storage;
    private String request;
    private String response;
    private String responseAddress;


    public ManageRequests(Storage st) {
        this.server_Storage = st;
        this.request = request;
        this.responseAddress = st.getServerConfig().getAddress();
        this.start();
    }

    ;


    public void run() {
        System.out.println("started manage requests");
        while (true) {

            this.request = server_Storage.getRequest();
            String type = request.substring(7).split(" ; ")[0];

            String parameters = request.replace("type | " + type + " ; ", "");
            //System.out.println(parameters);

            String[] data = parameters.split(" ; ");
            //send ack packet
            System.out.println(type);
            switch (type) {
                case "register":
                    //parse the request

                    String username = data[0].replace("username | ", "");
                    String password = data[1].replace("password | ", "");

                    //create user
                    //System.out.println(username + "||"+password);
                    //do not allow duplicate users
                    boolean isAdmin;
                    int id;
                    String resp;
                    if (server_Storage.getUserList().isEmpty()) {
                        isAdmin = true;
                        id = 1;

                    } else {
                        if (server_Storage.getUser(username) != null) {
                            resp = "type | status ; operation | failed";
                            break;
                        } else {

                            isAdmin = false;
                            id = server_Storage.getUserList().size() + 1;
                        }
                    }

                    response = "1";

                    User u = new User(username, password, isAdmin, id);

                    //add user to list

                    server_Storage.addUser(u);

                    resp = "type | status ; operation | success ; isAdmin | " + u.isAdmin();
                    this.response = resp;
                    System.out.println("user added");
                    break;

                case "login":

                    username = data[0].replace("username | ", "");
                    password = data[1].replace("password | ", "");

                    //check if user is registered and confirm passowrd
                    if (server_Storage.getUser(username) != null) {
                        User user = server_Storage.getUser(username);
                        if (user.getUsername().compareTo(username) == 0 && user.getPassword().compareTo(password) == 0) {
                            //add user to online users

                            resp = "type | status ; operation | success ; isAdmin | " + user.isAdmin();

                        } else {
                            resp = "type | status ; operation | failed";
                        }
                    } else {

                        //send message saying user doesnt exist
                        resp = "type | status ; operation | failed";

                    }

                    this.response = resp;

                    break;

                case "historico":
                    username = data[0].replace("username | ", "");
                    //get search info from personal info
                    server_Storage.getUser(username).getSearchHistory();

                    //send message with all information
                    resp = "type | historico ; ";
                    u = server_Storage.getUser(username);
                    for (String temp : u.getSearchHistory()) {
                        resp += "item_name | " + temp + " ; ";
                    }

                    this.response = resp;

                    break;

                case "url_references":
                    String find_url = data[0].replace("url | ", "");
                    //go to hashmap to find the url
                    if (server_Storage.getReferenceHash().get(find_url) != null) {
                        response = "type | url_references ; ";
                        for (String url : server_Storage.getReferenceHash().get(find_url)) {
                            response += "item_name | " + url + " ; ";
                        }

                    } else {
                        //send message saying the URL doesnt exist
                        response = "type | status ; operation | failed";
                    }

                    break;

                case "newURL":
                    String newUrl = data[0].replace("url | ", "");

                    //add link to queue
                    server_Storage.addLinkToQueue(newUrl);

                    response = "type | status ; operation | success";
                    try {
                        Document doc = Jsoup.connect(newUrl).get();
                    } catch (Exception e) {
                        // mandar notificacao para o admin a dizer que o url nao esta disponivel
                        response = "type | status ; operation | failed";
                    }
                    break;

                case "search":
                    //get all the search terms
                    String[] seachTerms = data[0].replace("text | ", "").split(" ");

                    CopyOnWriteArrayList<String> searchResults = server_Storage.getSearchHash().get(seachTerms[0]);

                    for (String s : seachTerms) {
                        CopyOnWriteArrayList<String> merged = new CopyOnWriteArrayList<>();
                        CopyOnWriteArrayList<String> temp = server_Storage.getSearchHash().get(s);
                        System.out.println(temp);
                        for (String url : temp) {
                            if (searchResults.contains(url)) {
                                int i = 0;

                                merged.add(i, url);
                            }
                        }
                        searchResults = merged;
                    }

                    //order search results
                    String[] array = (String[]) searchResults.toArray();
                    Arrays.sort(array, new URL_Comparator(server_Storage));

                    //convert search results to string and send response
                    resp = "type | search ; item_count | " + searchResults.size() + " ; ";
                    String title = "";
                    String citation = "";
                    String order_search;

                    for (int i = 0; i < 10; i++) {
                        order_search = array[i];
                        try {
                            Document doc = Jsoup.connect(order_search).get();
                            title = doc.title();
                            citation = doc.text().substring(0, 20);
                        } catch (Exception e) {
                            continue;
                        }
                        resp += "item_name | " + order_search + " ; " + "title | " + title + " ; " + "citation | " + citation + " ; ";
                    }
                    this.response = resp;

                    break;


                case "give_privilege":
                    username = data[0].replace("username | ", "");

                    //check if user exists
                    if (server_Storage.getUser(username) != null) {
                        //change access privilege
                        server_Storage.getUser(username).changeUserToAdmin();
                        //send message to admin saying operation successful

                        //send notification to user saying he is now an admin
                        resp = "type | status ; operation | failed";
                    } else {
                        //send message saying user doesnt exist
                        resp = "type | status ; operation | failed";
                    }
                    this.response = resp;

                    break;

                case "logout":
                    username = data[0].replace("username | ", "");
                    server_Storage.disconnectUser(username);
                    resp = "type | status ; operation | success";
                    this.response = resp;
                    break;

                case "10MostImportant":
                    //get all the indexed websites
                    String[] mostImportant = (String[]) server_Storage.getReferenceHash().keySet().toArray();

                    Arrays.sort(mostImportant, new URL_Comparator(server_Storage));

                    resp = "type | 10MostImportant ; ";

                    if (mostImportant.length >= 10) {
                        for (int i = 0; i < 10; i++) {
                            resp += "item_name | " + mostImportant[10] + " ; ";
                        }
                    } else {
                        for (String s : mostImportant) {
    //                    System.out.println(s);
                            resp += "item_name | " + s + " ; ";
                        }

                    }


                    //type | 10MostImportant ; item_name | url ; item_name | url ;

                    break;

                case "10MostSearched":
                    //get all searched terms;
                    //go through all users and get all searches
                    HashMap<String, Integer> termFrequency = new HashMap<String, Integer>();
                    CopyOnWriteArrayList<User> userList = server_Storage.getUserList();

                    for (User user_temp : userList) {
                        for (String s : user_temp.getSearchHistory()) {
                            if (termFrequency.get(s) != null) {
                                termFrequency.put(s, termFrequency.get(s) + 1);
                            } else {
                                termFrequency.put(s, 1);
                            }
                        }
                    }

                    String[] ordered_array = (String[]) termFrequency.keySet().toArray();
                    Arrays.sort(ordered_array, new terms_Comparator(termFrequency));

                    resp = "type | 10MostSearched ; ";
                    if (ordered_array.length >= 10) {
                        for (int i = 0; i < 10; i++) {
                            resp += "item_name | " + ordered_array[i] + " ; ";
                        }
                    } else {
                        for (String s : ordered_array) {
                            resp += "item_name | " + s + " ; ";
                        }
                    }

                    break;

                case "get_notifications":
                    //check if user has undelivered messages
                    username = data[0].replace("username | ", "");
                    User user = server_Storage.getUser(username);

                    String notifications = "type | notifications ; item_count | " + user.getNotifications().size();
                    if (user.getNotifications() != null) {

                        for (String temp : user.getNotifications()) {
                            notifications += "item_name | " + temp + " ; ";
                        }
                    }

                    //if information checks send notification
                    this.response = notifications;
                    break;

                case "keepAlive":
                    //save online servers

                    //server_Storage.getOnlineServers().clear();
                    String[] serverInfo = data[0].split(" ; ");
                    String s = serverInfo[0];
                    String[] info = s.split("~");

                    int server_id = Integer.parseInt(info[0].replace("serverID ", ""));
                    String address = info[1].replace("address ", "");
                    int server_port = Integer.parseInt(info[2].replace("port ", ""));
                    int tcp_port = Integer.parseInt(info[4].replace("TCPport ", ""));
//                    System.out.println(server_id);


                    ServerConfig temp_S = null;
                    try {
                        temp_S = new ServerConfig(server_port, address, tcp_port, server_id);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    temp_S.updateWorkload(Integer.parseInt(info[5].replace("workload ", "")));

                    //if server is already on list update information
                    boolean serverIsOn = false;

//                    for (ServerConfig server : server_Storage.getOnlineServers()) {
//                        if (server.getServer_ID() == temp_S.getServer_ID()) serverIsOn = true;
//                    }
                    if(temp_S.getServer_ID() != server_Storage.getServerConfig().getServer_ID() && !server_Storage.isServerOnline(temp_S)){
                        server_Storage.addOnlineServer(temp_S);
                    }


//                    System.out.println("servers online " + server_Storage.getOnlineServers().size());

                    //System.out.println("server is alive");
                    break;

                case "getOnlineServer":
                    //this is for RMI server
                    resp = "type | getOnlineServer ; ";

                    for (ServerConfig temp : server_Storage.getOnlineServers()) {
                        resp += "item_id | " + temp.getServer_ID() + " ; workload | " + temp.getWorkload() + " ; ";
                    }
                    break;
                default:
            }
            if (type.equals("keepAlive")) {
                continue;
            }

            int resp_port = 4324;
            MulticastSocket resp_socket = null;
            try {
                resp_socket = new MulticastSocket();
                InetAddress group = InetAddress.getByName(responseAddress);

    //            //send buffer length
    //            String length = "" + response.length();
    //            byte[] buffer = length.getBytes();
    //            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, resp_port);
    //            resp_socket.send(packet);

                //send response
                System.out.println(response);
                byte[] buffer = response.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, resp_port);
                packet = new DatagramPacket(buffer, buffer.length, group, resp_port);
    //            try{
    //                Thread.sleep(100);
    //            }catch (InterruptedException e){}
                resp_socket.send(packet);


            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                resp_socket.close();
            }

        }
    }
}

class URL_Comparator implements Comparator<String> {
    private Storage st;

    public URL_Comparator(Storage st){
        this.st = st;
    }

    @Override
    public int compare(String o1, String o2) {
        return st.getReferenceHash().get(o1).size() - st.getReferenceHash().get(o2).size();
    }
}

class terms_Comparator implements Comparator<String> {
    private HashMap<String, Integer> map;
    public terms_Comparator(HashMap<String, Integer> map){
        this.map = map;
    }
    @Override
    public int compare(String o1, String o2) {
        return map.get(o1) - map.get(o2);
    }
}