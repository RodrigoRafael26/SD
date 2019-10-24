import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class ManageRequests extends Thread{
    private Storage server_Storage;
    private String request;
    private String response;
    private int response_port;
    private String responseAddress;

    public ManageRequests(Storage st, String request){
        this.server_Storage = st;
        this.request = request;
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
                boolean isAdmin;
                int id;
                if(server_Storage.getUserList().isEmpty()) {
                    isAdmin = true;
                    id = 1;
                }else{
                    isAdmin = false;
                    id = server_Storage.getUserList().size() +1;
                }

                User u = new User(username, password, isAdmin, id);

                //add user to list
                server_Storage.addUser(u);
                break;
            case "login":

                username = data[0].replace("username | ","");
                password = data[1].replace("password | ", "");

                //check if user is registered and confirm passowrd
                if(server_Storage.getUser(username) !=null){

                    if(server_Storage.getUser(username).getUsername().compareTo(username) == 0 && server_Storage.getUser(username).getPassword().compareTo(password)==0){

                        //if information checks send notification

                    }else{

                        //send message saying wrong login info

                    }
                }else{

                    //send message saying user doesnt exist
                }

                //check if user has undelivered messages if so send them

                break;

            case "historico":
                username = data[0].replace("username | ", "");
                //get search info from personal info
                server_Storage.getUser(username).getSearchHistory();

                //send message with all information
                break;

            case "url_references":
                String find_url = data[0].replace("url | ", "");
                response="";
                //go to hashmap to find the url
                if(server_Storage.getReferenceHash().get(find_url)!=null){
                    for (String url : server_Storage.getReferenceHash().get(find_url)) {
                        response += url +" ; ";
                    }

                }else{
                    //send message saying the URL doesnt exist
                }

                break;

            case "add_URL":
                //falta esta opcao no rmi server
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

                //need to find a way to show pages info
                break;

            case "status":
                //nao sei bem o que por aqui
                break;

            case "give_privilege":
                username = data[0].replace("username | ", "");

                //check if user exists
                if(server_Storage.getUser(username) != null){
                    //change access privilege
                    server_Storage.getUser(username).changeUserToAdmin();
                    //send message to admin saying operation successful
                    //send notification to user saying he is now an admin

                }else{
                    //send message saying user doesnt exist
                }

                break;

            case "logout":
                //nao sei o que e suposto fazer no logout aqui
                break;

            case "10MostImportant":
                //faz parte do status
                break;

            case "10MostSearched":
                //faz parte do status
                break;

            case "get_notifications":
                //pode ser feito no login acho
                break;
            case "keepAlive":
                //save online servers
                break;
            case "getOnlineServers":
                break;
            default:
        }

    }



}
