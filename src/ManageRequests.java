import java.io.*;

public class ManageRequests {

    public ManageRequests(){}
    public void registerUser(String userName, String password, int id){

        File f = new File("Users");
        System.out.println(f.exists());
        boolean isAdmin = !f.exists();
        try {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            User u = createUser(userName, password, isAdmin,id);
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public User createUser(String userName, String password, boolean isAdmin, int id){
        User u = new User(userName, password, isAdmin, id);
        return u;
    }

    //manage login

    //manage research


}
