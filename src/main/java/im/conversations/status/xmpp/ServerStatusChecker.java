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
        ServerStatusStore.INSTANCE.put(jid.getDomain(), checkStatus());
    }

    private ServerStatus checkStatus() {
        XmppSessionConfiguration xmppSessionConfiguration = XmppSessionConfiguration.builder()
                //.debugger(ConsoleDebugger.class)
                .defaultResponseTimeout(Duration.ofSeconds(20))
                .build();
        System.out.println("Run status check for " + jid.getDomain());
        try (XmppClient xmppClient = XmppClient.create(jid.getDomain(), xmppSessionConfiguration)) {
            xmppClient.connect();
            xmppClient.getManager(RosterManager.class).setRetrieveRosterOnLogin(false);
            xmppClient.getManager(ServiceDiscoveryManager.class).setEnabled(false);
            xmppClient.login(jid.getLocal(), password);
            PingManager pingManager = xmppClient.getManager(PingManager.class);
            List<PingResult> results = serversToPing.parallelStream()
                    .filter(s -> !s.toString().equals(jid.getDomain()))
                    .map(server -> pingManager.ping(server)
                            .toCompletableFuture()
                            .thenApply(result -> new PingResult(server, result))
                            .exceptionally(throwable -> new PingResult(server, false)))
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
            return ServerStatus.createWithPingResults(results);
        } catch (XmppException e) {
            return ServerStatus.createWithLoginFailure();
        }
    }
}
