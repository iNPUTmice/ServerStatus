package im.conversations.status.xmpp;

import im.conversations.status.persistence.ServerStatusStore;
import im.conversations.status.pojo.Credentials;
import im.conversations.status.pojo.PingResult;
import im.conversations.status.pojo.ServerStatus;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.ping.PingManager;
import rocks.xmpp.im.roster.RosterManager;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ServerStatusChecker implements Runnable {

    private final Jid jid;
    private final String password;
    private final List<Jid> serversToPing;

    public ServerStatusChecker(Credentials credentials, List<Jid> serversToPing) {
        this.jid = credentials.getJid();
        this.password = credentials.getPassword();
        this.serversToPing = serversToPing;
    }

    @Override
    public void run() {
        try {
            ServerStatusStore.INSTANCE.put(jid.getDomain(), checkStatus());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private ServerStatus checkStatus() {
        XmppSessionConfiguration xmppSessionConfiguration = XmppSessionConfiguration.builder()
                .defaultResponseTimeout(Duration.ofSeconds(10))
                .build();
        try (XmppClient xmppClient = XmppClient.create(jid.getDomain(), xmppSessionConfiguration)) {
            xmppClient.connect();
            xmppClient.getManager(RosterManager.class).setRetrieveRosterOnLogin(false);
            xmppClient.getManager(ServiceDiscoveryManager.class).setEnabled(false);
            final PingManager pingManager = xmppClient.getManager(PingManager.class);
            xmppClient.login(jid.getLocal(), password);
            List<PingResult> results = serversToPing.parallelStream()
                    .filter(s -> !s.toString().equals(jid.getDomain()))
                    .map(server -> pingManager.ping(server)
                            .toCompletableFuture()
                            .thenApply(result -> new PingResult(server, result))
                            .exceptionally(throwable -> {
                                throwable.printStackTrace();
                                return new PingResult(server, false);
                            }))
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
            return ServerStatus.createWithPingResults(results);
        } catch (XmppException e) {
            System.err.println(jid.getDomain() + ": " + e.getMessage());
            return ServerStatus.createWithLoginFailure();
        }
    }
}
