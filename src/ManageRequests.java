import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class ManageRequests extends Thread{
    private Storage server_Storage;
    private String request;
    private String response;


    public ManageRequests(Storage st, String request/*, InetAddress group, int port, String address*/){
        this.server_Storage = st;
        this.request = request;
//        this.response_port = port;
//        this.responseAddress = address;
        this.start();
    };


    public void run(){
        String type = request.substring(7).split(" ; ")[0];

        String parameters = request.replace("type | "+ type + " ; ","");
        //System.out.println(parameters);

        String[] data = parameters.split(" ; ");
        //send ack packet
        switch (type){
            case "register":
                //parse the request

                String username = data[0].replace("username | ","");
                String password = data[1].replace("password | ", "");

                //create user
                //System.out.println(username + "||"+password);
                //do not allow duplicate users
                boolean isAdmin;
                int id;
                if(server_Storage.getUserList().isEmpty()) {
                    isAdmin = true;
                    id = 1;
                }else{
                    isAdmin = false;
                    id = server_Storage.getUserList().size() +1;
                }

                response = "1";

                User u = new User(username, password, isAdmin, id);

                //add user to list

                server_Storage.addUser(u);
                System.out.println("user added");
                break;

            case "login":

                username = data[0].replace("username | ","");
                password = data[1].replace("password | ", "");

                //check if user is registered and confirm passowrd
                if(server_Storage.getUser(username) !=null){
                    User user = server_Storage.getUser(username);
                    if(user.getUsername().compareTo(username) == 0 && user.getPassword().compareTo(password)==0){
                        //add user to online users

                        String resp = "type | status ; operation | success";

                    }
                }else{

                    //send message saying user doesnt exist
                    String resp = "type | status ; operation | failed";
                    this.response = resp;
                }

                break;

            case "historico":
                username = data[0].replace("username | ", "");
                //get search info from personal info
                server_Storage.getUser(username).getSearchHistory();

                //send message with all information
                String resp = "type | historico ; ";
                u = server_Storage.getUser(username);
                for(String temp : u.getSearchHistory()){
                    resp += "item_name | "+ temp + " ; ";
                }

                this.response = resp;

                break;

            case "url_references":
                String find_url = data[0].replace("url | ", "");
                //go to hashmap to find the url
                if(server_Storage.getReferenceHash().get(find_url)!=null){
                    response="type | url_references ; ";
                    for (String url : server_Storage.getReferenceHash().get(find_url)) {
                        response +="item_name | " + url +" ; ";
                    }

                }else{
                    //send message saying the URL doesnt exist
                    response = "type | status ; operation | failed";
                }

                break;

            case "add_URL":
                String newUrl = data[0].replace("url | ", "");

                //add link to queue
                server_Storage.addLinkToQueue(newUrl);

                response = "type | status ; operation | success";

                response = "type | status ; operation | failed";

                break;

            case "search":
                //get all the search terms
                String[] seachTerms = data[0].replace("text | ", "").split(" ");

                CopyOnWriteArrayList<String> searchResults =  server_Storage.getSearchHash().get(seachTerms[0]);

                for (String s : seachTerms){
                    CopyOnWriteArrayList<String> merged = new CopyOnWriteArrayList<>();
                    CopyOnWriteArrayList<String> temp = server_Storage.getSearchHash().get(s);

                    for(String url : temp){
                        if(searchResults.contains(url)){
                            merged.add(url);
                        }
                    }
                    searchResults = merged;
                }
                //order search


                //convert search results to string and send response
                resp = "type | search ; item_count | "+ searchResults.size() + " ; ";
                String title = "";
                String citation = "";
                for(String temp : searchResults){
                    try{
                        Document doc = Jsoup.connect(temp).get();
                        title = doc.title();
                        citation = doc.text().substring(0,20);
                    } catch (Exception e) {
                        continue;
                    }
                    resp += "item_name | "+temp+ " ; "+ "title | "+ title + " ; "+ "citation | "+ citation + " ; ";
                }
                this.response = resp;

                break;


            case "give_privilege":
                username = data[0].replace("username | ", "");

                //check if user exists
                if(server_Storage.getUser(username) != null){
                    //change access privilege
                    server_Storage.getUser(username).changeUserToAdmin();
                    //send message to admin saying operation successful

                    //send notification to user saying he is now an admin
                    resp = "type | status ; operation | failed";
                }else{
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

                break;

            case "10MostSearched":

                break;

            case "newURL":

                break;

            case "get_notifications":
                //check if user has undelivered messages
                username = data[0].replace("username | ", "");
                User user = server_Storage.getUser(username);

                String notifications = "type | notifications ; ";
                if(user.getNotifications()!=null){

                    for(String temp : user.getNotifications() ){
                        notifications += "item_name | "+ temp + " ; ";
                    }
                }

                //if information checks send notification
                this.response = notifications;
                break;

            case "keepAlive":
                //save online servers
                System.out.println("server is alive");
                break;

            case "getOnlineServer":
                //this is for RMI server
                resp = "type | getOnlineServer ; ";

                for (ServerConfig s : server_Storage.getOnlineServers()){
                    resp += "item_id | " + s.getServer_ID() + " ; workload | " + s.getWorkload() + " ; ";
                }
                break;
            default:
        }
        if(type.equals("keepAlive")){
            return;
        }
        String resp_address = "224.0.224.0";
        int resp_port = 4324;
        MulticastSocket resp_socket = null;
        try{
            resp_socket = new MulticastSocket();
            InetAddress group = InetAddress.getByName("224.0.224.0");

            //send buffer length
            String length = "" + response.length();
            byte[] buffer = length.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, resp_port);
            resp_socket.send(packet);

            //send response
            packet = new DatagramPacket(buffer, buffer.length, group, resp_port);
            try{
                Thread.sleep(100);
            }catch (InterruptedException e){}
            resp_socket.send(packet);


        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            resp_socket.close();
        }

    }



}
