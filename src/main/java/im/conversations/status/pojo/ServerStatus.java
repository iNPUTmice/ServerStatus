package im.conversations.status.pojo;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ServerStatus {

    private final List<PingResult> pingResults;
    private final LoginStatus loginStatus;

    private ServerStatus(boolean loggedIn, List<PingResult> pingResults) {
        this.loginStatus = LoginStatus.create(loggedIn);
        this.pingResults = pingResults;
    }

    public static ServerStatus createWithLoginFailure() {
        return new ServerStatus(false, Collections.emptyList());
    }

    public static ServerStatus createWithPingResults(List<PingResult> pingResults) {
        return new ServerStatus(true, pingResults);
    }

    public boolean isLoggedIn() {
        return loginStatus.getStatus();
    }

    public List<PingResult> getPingResults() {
        return pingResults;
    }

    public Date getDate() {
        return Date.from(loginStatus.getTimestamp());
    }

    public LoginStatus getLoginStatus() {
        return loginStatus;
    }
}
