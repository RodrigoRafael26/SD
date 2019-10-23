import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

public class HandleFiles{

    public HandleFiles(){}

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

    public void writeUsers(ArrayList<User> usersList){
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

    public  void writeReferenceIndex(HashMap<String, HashSet<String>> referenceMap){
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

    public HashMap<String, HashSet<String>> getReferenceIndex(){
        File f = new File("ReferenceIndex");
        HashMap<String, HashSet<String>> referenceMap = null;
        try {
            FileInputStream fos = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fos);
            referenceMap = (HashMap<String, HashSet<String>>) ois.readObject();
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

    public  void writeSearchIndex(HashMap<String, HashSet<String>> searchMap){
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

    public HashMap<String, HashSet<String>> getSearchIndex(){
        File f = new File("SearchIndex");
        HashMap<String, HashSet<String>> searchMap = null;
        try {
            FileInputStream fos = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fos);
            searchMap = (HashMap<String, HashSet<String>>) ois.readObject();
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

    public ServerConfig readConfig(){
        String multicast_address;
        int port;
        int serverID;

        File config_file = new File("config_1.txt");
        String[] st = new String[4];
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
        port = Integer.parseInt(st[0]);
        multicast_address = st[1];
        serverID = Integer.parseInt(st[2]);

        ServerConfig s = new ServerConfig(port,multicast_address,serverID);
        return s;
    }

    //escrever funcoes para ler e escrever o array de links a indexar




}
