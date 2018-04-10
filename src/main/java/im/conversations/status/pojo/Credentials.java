package im.conversations.status.pojo;

import rocks.xmpp.addr.Jid;

public class Credentials {

    private final String jid;
    private final String password;

    public Credentials(String jid, String password) {
        this.jid = jid;
        this.password = password;
    }

    public Jid getJid() {
        return Jid.of(jid);
    }

    public String getPassword() {
        return password;
    }
}
