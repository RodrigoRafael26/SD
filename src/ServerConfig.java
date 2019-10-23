import java.io.Serializable;

public class ServerConfig implements Serializable {

    private int port;
    private int server_ID;
    private String address;

    public ServerConfig(int port, String address, int server_ID){
        this.port = port;
        this.address = address;
        this.server_ID = server_ID;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }

    public int getServer_ID() {
        return server_ID;
    }

    public void setServer_ID(int server_ID) {
        this.server_ID = server_ID;
    }
}
