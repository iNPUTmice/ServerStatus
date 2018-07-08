package im.conversations.status.network;

import java.io.IOException;

public class NetworkAvailability {

    private static String[] WELL_KNOWN_PING_TARGETS = new String[]{"8.8.8.8","1.1.1.1"};


    public static boolean test() {
        for(String ip : WELL_KNOWN_PING_TARGETS) {
            if (ping(ip)) {
                return true;
            }
        }
        return false;
    }

    private static boolean ping(String ip) {
        try {
            Process ping = Runtime.getRuntime().exec("ping -c 1 "+ip);
            return ping.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

}
