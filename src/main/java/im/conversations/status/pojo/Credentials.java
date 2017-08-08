package im.conversations.status.pojo;

import rocks.xmpp.addr.Jid;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Credentials {

    public static final List<Credentials> LIST = Arrays.asList(
            new Credentials("dummy@example.com", "password")
    );

    public static List<String> AVAILABLE_DOMAINS = Credentials.LIST.stream().map(credentials -> credentials.getJid().getDomain()).collect(Collectors.toList());

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
