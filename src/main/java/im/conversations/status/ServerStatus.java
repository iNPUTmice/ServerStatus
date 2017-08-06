package im.conversations.status;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ServerStatus {

    private final boolean loggedIn;
    private final List<PingResult> pingResults;
    private final Date date =  new Date();

    private ServerStatus(boolean loggedIn, List<PingResult> pingResults) {
        this.loggedIn = loggedIn;
        this.pingResults = pingResults;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public List<PingResult> getPingResults() {
        return pingResults;
    }

    public Date getDate() {
        return date;
    }

    public static ServerStatus createWithLoginFailure() {
        return new ServerStatus(false, Collections.emptyList());
    }

    public static ServerStatus createWithPingResults(List<PingResult> pingResults) {
        return new ServerStatus(true,pingResults);
    }

}
