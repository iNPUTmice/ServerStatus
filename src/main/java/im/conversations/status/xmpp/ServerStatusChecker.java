package im.conversations.status.xmpp;

import im.conversations.status.network.NetworkAvailability;
import im.conversations.status.persistence.CredentialStore;
import im.conversations.status.persistence.ServerStatusStore;
import im.conversations.status.persistence.ThreeStrikesStore;
import im.conversations.status.pojo.Credentials;
import im.conversations.status.pojo.PingResult;
import im.conversations.status.pojo.ServerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.sasl.AuthenticationException;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.ping.PingManager;
import rocks.xmpp.im.roster.RosterManager;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ServerStatusChecker implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerStatusChecker.class);

    private final Credentials credentials;
    private final List<Jid> serversToPing;

    public ServerStatusChecker(Credentials credentials, List<Jid> serversToPing) {
        this.credentials = credentials;
        this.serversToPing = serversToPing;
    }

    @Override
    public void run() {
        try {
            checkStatus().ifPresent(serverStatus -> ServerStatusStore.INSTANCE.put(credentials.getJid().getDomain(), serverStatus));
        } catch (Throwable t) {
            LOGGER.error("unexpected problem", t);
        }
    }

    private Optional<ServerStatus> checkStatus() {
        XmppSessionConfiguration xmppSessionConfiguration = XmppSessionConfiguration.builder()
                .defaultResponseTimeout(Duration.ofSeconds(10))
                .build();
        try (XmppClient xmppClient = XmppClient.create(credentials.getJid().getDomain(), xmppSessionConfiguration)) {
            xmppClient.connect();
            xmppClient.getManager(RosterManager.class).setRetrieveRosterOnLogin(false);
            xmppClient.getManager(ServiceDiscoveryManager.class).setEnabled(false);
            final PingManager pingManager = xmppClient.getManager(PingManager.class);
            xmppClient.login(credentials.getJid().getLocal(), credentials.getPassword());
            List<PingResult> results = serversToPing.parallelStream()
                    .filter(s -> !s.toString().equals(credentials.getJid().getDomain()))
                    .map(server -> pingManager.ping(server)
                            .toCompletableFuture()
                            .thenApply(result -> new PingResult(server, result))
                            .exceptionally(throwable -> new PingResult(server, false)))
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
            return Optional.of(ServerStatus.createWithPingResults(results));
        } catch (AuthenticationException e) {
            LOGGER.info("Authentication failure while testing " + credentials.getJid().getDomain());
            if (ThreeStrikesStore.INSTANCE.strike(credentials)) {
                if (CredentialStore.INSTANCE.delete(credentials)) {
                    LOGGER.info("successfully deleted credentials for " + credentials.getJid() + " after three strikes");
                }
            }
            return Optional.of(ServerStatus.createWithLoginFailure());
        } catch (XmppException e) {
            final String domain = credentials.getJid().getDomain();
            if (!NetworkAvailability.test()) {
                LOGGER.info("Network unavailable while testing " + domain);
                return Optional.empty();
            }
            LOGGER.info(e.getMessage() + " while testing " + domain);
            return Optional.of(ServerStatus.createWithLoginFailure());
        }
    }
}
