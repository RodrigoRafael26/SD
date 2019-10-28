import java.io.*;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

//This class is only used to read and write files recieves as a parameter the path for the config file
public class HandleFiles{
    String configFile;
    public HandleFiles(String config){
        this.configFile = config;
    }

    //function to get users from object file
    //returns CopyOnWriteArrayList<User> with all saved users
    public CopyOnWriteArrayList<User> readUsers(){

        File f = new File("Users");
        CopyOnWriteArrayList<User> userList = null;
        try {
            FileInputStream fos = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fos);
            userList = (CopyOnWriteArrayList<User>) ois.readObject();
            ois.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return null;
        }catch (StreamCorruptedException e){
            System.out.println("File corrupted restart system");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("User not defined");
            e.printStackTrace();
        }
        return userList;
    }

    //function to write users to object file
    //gets as parameter one CopyOnWriteArrayList<User> e escreve no ficheiro de objetos "Users" no path do projeto
    public void writeUsers(CopyOnWriteArrayList<User> usersList){
        File f = new File("Users");

        try{
            FileOutputStream fos = new FileOutputStream(f, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(usersList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Recieves as parameter ConcurrentHashMap<String, CopyOnWriteArrayList<String>> containing all urls as keys
    // and a CopyOnWriteArrayList<String> off all links that reference that URL
    //Writes the content of that hash map to the object file "ReferenceIndex" in the project path
    public void writeReferenceIndex(ConcurrentHashMap<String, CopyOnWriteArrayList<String>> referenceMap){
        File f = new File("ReferenceIndex");

        try{
            FileOutputStream fos = new FileOutputStream(f, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(referenceMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //Goes to project path and tries to open the object file "ReferenceIndex"
    //If the file exists and is not corrupted returns a hash map of all the URLs and the websites that reference them previously indexed else returns null
    public ConcurrentHashMap<String, CopyOnWriteArrayList<String>> getReferenceIndex(){
        File f = new File("ReferenceIndex");
        ConcurrentHashMap<String, CopyOnWriteArrayList<String>> referenceMap = null;
        try {
            FileInputStream fos = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fos);
            referenceMap = (ConcurrentHashMap<String, CopyOnWriteArrayList<String>>) ois.readObject();
            ois.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return null;
        } catch (StreamCorruptedException e) {
            System.out.println("File corrupted restart system");
            return null;
        }catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return referenceMap;
    }

    //Recieves as parameter ConcurrentHashMap<String, CopyOnWriteArrayList<String>> containing all indexed words as keys
    //and a CopyOnWriteArrayList<String> where that word was found
    //Writes the content of that hash map to the object file "ReferenceIndex" in the project path
    public  void writeSearchIndex(ConcurrentHashMap<String, CopyOnWriteArrayList<String>> searchMap){
        File f = new File("SearchIndex");

        try{
            FileOutputStream fos = new FileOutputStream(f, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(searchMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Goes to project path and tries to open the object file "SearchIndex"
    //If the file exists and is not corrupted returns a hash map of all the words previously indexed else returns null
    public ConcurrentHashMap<String, CopyOnWriteArrayList<String>> getSearchIndex(){
        File f = new File("SearchIndex");
        ConcurrentHashMap<String, CopyOnWriteArrayList<String>> searchMap = null;
        try {
            FileInputStream fos = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fos);
            searchMap = (ConcurrentHashMap<String, CopyOnWriteArrayList<String>>) ois.readObject();
            ois.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return null;
        }catch (StreamCorruptedException e) {
            System.out.println("File corrupted restart system");
            return null;
        }catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return searchMap;
    }

    //Reads the config file passed in as parameter, returns an object ServerConfig that contain all the server info
    public ServerConfig readConfig(){
        String multicast_address;
        int multicast_port;
        int tcp_port;
        int serverID;

        File config_file = new File(configFile);
        String[] st = new String[5];
        try {
            BufferedReader br = new BufferedReader(new FileReader(config_file));

            int i = 0;
            while((st[i] = br.readLine()) != null){
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (StreamCorruptedException e){
            System.out.println("File corrupted restart system");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        multicast_port = Integer.parseInt(st[0]);
        multicast_address = st[1];
        tcp_port = Integer.parseInt(st[2]);
        serverID = Integer.parseInt(st[3]);

        ServerConfig s = null;
        try {
            s = new ServerConfig(multicast_port,multicast_address,tcp_port, serverID);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return s;
    }

    //escrever funcoes para ler e escrever o array de links a indexar
    public void writeUrlList(CopyOnWriteArrayList<String> linkList){
        File f = new File("linkList");

        try{
            FileOutputStream fos = new FileOutputStream(f, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(linkList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CopyOnWriteArrayList<String> getLinkList(){
        //each server has a different file
        File f = new File("linkList");
        CopyOnWriteArrayList<String> linkList = null;
        try {
            FileInputStream fos = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fos);
            linkList = (CopyOnWriteArrayList<String>) ois.readObject();
            ois.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return null;
        }catch (StreamCorruptedException e) {
            System.out.println("File corrupted restart system");
            return null;
        }catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return linkList;
    }

}
