import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
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
        serverID = Integer.parseInt(st[2]); //talvez mude isto para ser passado em parametro

        ServerConfig s = new ServerConfig(port,multicast_address,serverID);
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

    //Write and read undelivered messages
    public void writeUndeliveredMessages(CopyOnWriteArrayList<String> messages){
        //each server has a different file

    }


}
