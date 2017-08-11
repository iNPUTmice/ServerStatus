package im.conversations.status.pojo;

import rocks.xmpp.addr.Jid;

public class Credentials {

    private final Jid jid;
    private final String password;

    private Credentials(String jid, String password) {
        this.jid = Jid.of(jid);
        this.password = password;
    }

    public Jid getJid() {
        return jid;
    }

    public String getPassword() {
        return password;
    }
}
