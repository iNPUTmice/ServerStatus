package im.conversations.status.pojo;

import rocks.xmpp.addr.Jid;

public class PingResult {
    private Jid server;
    private boolean successful;

    public PingResult(String server, boolean successful) {
        this(Jid.ofDomain(server),successful);
    }

    public PingResult(Jid server, boolean successful) {
        this.server = server;
        this.successful = successful;
    }

    public Jid getServer() {
        return server;
    }

    public boolean isSuccessful() {
        return successful;
    }
}
