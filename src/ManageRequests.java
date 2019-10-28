import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ManageRequests extends Thread {
    private Storage server_Storage;
    private String request;
    private String response;
    private String responseAddress;
    private HashMap<String, Date> lastPingSent;


    public ManageRequests(Storage st) {
        this.server_Storage = st;
        this.request = request;
        this.responseAddress = st.getServerConfig().getAddress();
        this.lastPingSent = new HashMap<>();
        this.start();
    }


    public void run() {
        System.out.println("started manage requests");
        while (true) {

            this.request = server_Storage.getRequest();
            String type = request.split(" ; ")[0];
            type = type.replace("type | ","");
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
                    boolean isAdmin = false;
                    int id = 1;
                    String resp;
                    if (server_Storage.getUserList().isEmpty()) {
                        isAdmin = true;
                        id = 1;
                        resp = "type | status ; operation | success ; isAdmin | true" ;

                    } else {
                        if (server_Storage.getUser(username) != null) {
                            resp = "type | status ; operation | failed";

                        } else {

                            isAdmin = false;
                            id = server_Storage.getUserList().size() + 1;
                            resp = "type | status ; operation | success ; isAdmin | false";
                        }
                    }

                    User u = new User(username, password, isAdmin, id);

                    //add user to list

                    server_Storage.addUser(u);

                    this.response = resp;

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
                    String newUrl = data[0].split(" ; ")[0].replace("url | ", "");
                    String server_ID = data[1].replace("id_server | ","");
                    System.out.println(server_ID);
                    //add link to queue
                    if(Integer.parseInt(server_ID)!=server_Storage.getServerConfig().getServer_ID()){
                        //ignore request and dont send response
                        response = null;
                        break;
                    }

                    response = "type | status ; operation | success";
                    try {
                        Document doc = Jsoup.connect(newUrl).get();
                    } catch (Exception e) {
                        // mandar notificacao para o admin a dizer que o url nao esta disponivel
                        System.out.println(e.getMessage());
                        System.out.println(newUrl);
                        response = "type | status ; operation | failed";
                        break;
                    }
                    server_Storage.addLinkToQueue(newUrl);
                    break;

                case "search":
                    //get all the search terms
                    username = data[0].replace("username | ", "");
                    String[] seachTerms = data[1].replace("text | ", "").split(" ");
                    String pesquisa ="";
                    CopyOnWriteArrayList<String> searchResults = server_Storage.getSearchHash().get(seachTerms[0]);
                    boolean opFailed = false;
                    for (String s : seachTerms) {
                        CopyOnWriteArrayList<String> merged = new CopyOnWriteArrayList<>();
                        CopyOnWriteArrayList<String> temp = server_Storage.getSearchHash().get(s);
                        pesquisa += s +" ";
                        if (temp == null){
                            opFailed = true;
                            resp = "type | search ; item_count | 0";
                            response = resp;
                            break;
                        }

                        for (String url : temp) {
                            if (searchResults.contains(url)) {
                                int i = 0;

                                merged.add(i, url);
                            }
                        }
                        searchResults = merged;
                    }

                    //order search results
                    if(!opFailed) {
                        if(username.compareTo("null")!=0) server_Storage.getUser(username).addToHist(pesquisa);
                        String[] array = listToArray(searchResults);
                        Arrays.sort(array, new URL_Comparator(server_Storage));

                        //convert search results to string and send response
                        resp = "type | search ; item_count | " + searchResults.size() + " ; ";
                        String title = "";
                        String citation = "";
                        String order_search;
                        if(array.length >= 10){

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
                        }else {
                            for (String search : array) {
                                order_search = search;
                                try {
                                    Document doc = Jsoup.connect(order_search).get();
                                    title = doc.title();
                                    citation = doc.text().substring(0, 20);
                                } catch (Exception e) {
                                    continue;
                                }
                                resp += "item_name | " + order_search + " ; " + "title | " + title + " ; " + "citation | " + citation + " ; ";
                            }
                        }
                        this.response = resp;
                    }
                    break;
                case "give_privilege":
                    username = data[0].replace("username | ", "");

                    //check if user exists
                    if (server_Storage.getUser(username) != null) {
                        //change access privilege
                        server_Storage.getUser(username).changeUserToAdmin();
                        //send message to admin saying operation successful

                        resp = "type | status ; operation | success";
                        //send notification to user saying he is now an admin
                        String notification = "You are now an admin";
                        server_Storage.getUser(username).addNotifications(notification);

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
                    String[] mostImportant =  keySetToArray(server_Storage.getReferenceHash().keySet());

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

                    response =resp;
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

                    String[] ordered_array = keySetToArray(termFrequency.keySet());
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

                    response = resp;
                    break;

                case "get_notifications":
                    //check if user has undelivered messages
                    username = data[0].replace("username | ", "");
                    User user = server_Storage.getUser(username);

                    String notifications = "type | notifications ; item_count | " + user.getNotifications().size() + " ;";
                    if (user.getNotifications() != null) {

                        for (String temp : user.getNotifications()) {
                            notifications += " item_name | " + temp + " ;";
                        }
                    }

                    //if information checks send notification
                    this.response = notifications;
                    user.getNotifications().clear();
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

                    String aux_id = ""+server_id;
                    ServerConfig temp_S = null;
                    try {
                        temp_S = new ServerConfig(server_port, address, tcp_port, server_id);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    temp_S.updateWorkload(Integer.parseInt(info[5].replace("workload ", "")));

                    //if server is already on list update information
                    Date date = new Date(System.currentTimeMillis());

                    //since lastPingSent is an hashmap if the key already exists it will just update
                    lastPingSent.put(aux_id, date);

                    for(String servers : lastPingSent.keySet()){
                        //if last ping was over 30s remove it from both lists
                        if(date.getTime() - lastPingSent.get(servers).getTime() > 30000){
                            lastPingSent.remove(servers);
                            server_Storage.removeServer(servers);
                        }
                    }
                    //if server is not on list add it
                    if(temp_S.getServer_ID() != server_Storage.getServerConfig().getServer_ID() && !server_Storage.isServerOnline(temp_S)){
                        server_Storage.addOnlineServer(temp_S);
                    }


                    break;

                case "getOnlineServer":
                    //this is for RMI server
                    resp = "type | getOnlineServer ; ";
                    if(lastPingSent.keySet().size()==1){
                        resp += "item_id | " + server_Storage.getServerConfig().getServer_ID() + " ; workload | " + server_Storage.getServerConfig().getWorkload() + " ; ";

                    }else{

                        for (ServerConfig temp : server_Storage.getOnlineServers()) {
                            resp += "item_id | " + temp.getServer_ID() + " ; workload | " + temp.getWorkload() + " ; ";
                        }
                    }
                    response = resp;
                    break;
                default:
            }
            if (type.equals("keepAlive") || response == null) {
                continue;
            }

            int resp_port = 4324;
            MulticastSocket resp_socket = null;
            try {
                resp_socket = new MulticastSocket();
                InetAddress group = InetAddress.getByName(responseAddress);

                //send response
                System.out.println(response);
                byte[] buffer = response.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, resp_port);
                //packet = new DatagramPacket(buffer, buffer.length, group, resp_port);

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


    //auxiliar method to convert CopyOnWriteArrayList to string array
    private String[] listToArray(CopyOnWriteArrayList<String> list){
        String[] array = new String[list.size()];
        int i = 0;
        for(String s : list){
            array[i] = s;
            i++;
        }
        return  array;

    }
    //auxiliar method to convert CopyOnWriteArrayList to string array
    private String[] keySetToArray(Set<String> list){
        String[] array = new String[list.size()];
        int i = 0;
        for(String s : list){
            array[i] = s;
            i++;
        }
        return  array;

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