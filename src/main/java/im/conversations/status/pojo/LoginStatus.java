package im.conversations.status.pojo;

import java.time.Instant;

public class LoginStatus {

    private final Instant timestamp;
    private final boolean status;


    private LoginStatus(Instant timestamp, boolean status) {
        this.timestamp = timestamp;
        this.status = status;
    }

    public static LoginStatus create(boolean status) {
        return new LoginStatus(Instant.now(), status);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean getStatus() {
        return status;
    }
}
