package im.conversations.status;

import java.util.HashMap;

public class ServerStatusStore {

    public static final ServerStatusStore INSTANCE = new ServerStatusStore();

    private HashMap<String,ServerStatus> serverStatusMap = new HashMap<>();

    public void put(String server, ServerStatus serverStatus) {
        synchronized (serverStatusMap) {
            serverStatusMap.put(server, serverStatus);
        }
    }

    public ServerStatus get(String server) {
        synchronized (serverStatusMap) {
            return serverStatusMap.get(server);
        }
    }

}
