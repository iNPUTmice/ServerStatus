package im.conversations.status.persistence;

import im.conversations.status.Main;
import im.conversations.status.pojo.Configuration;
import im.conversations.status.pojo.Credentials;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import rocks.xmpp.addr.Jid;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CredentialStore {
    public static final CredentialStore INSTANCE = new CredentialStore();
    private final Sql2o database;
    private List<Credentials> credentialsList;
    private List<Jid> additionalDomains;

    private CredentialStore() {
        additionalDomains = Configuration.getInstance().getAdditionalDomains();
        final String dbFilename = Configuration.getInstance().getStoragePath() + getClass().getSimpleName().toLowerCase(Locale.US) + ".db";
        this.database = new Sql2o("jdbc:sqlite:" + dbFilename, null, null);
        synchronized (this.database) {
            try (Connection connection = this.database.open()) {
                final String createTable = "create table if not exists credentials (jid TEXT, password TEXT)";
                connection.createQuery(createTable).executeUpdate();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public boolean put(Credentials credentials) {
        synchronized (this.database) {
            try (Connection connection = this.database.open()) {
                final String addCreds = "INSERT into credentials(jid,password) VALUES(:jid,:password)";
                connection.createQuery(addCreds).bind(credentials).executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            fetchCredentials();
            Main.scheduleStatusCheck();
        }
        return true;
    }

    private void fetchCredentials() {
        synchronized (this.database) {
            try (Connection connection = this.database.open()) {
                final String selectCreds = "SELECT jid,password from credentials";
                this.credentialsList = connection.createQuery(selectCreds).executeAndFetch(Credentials.class);
            } catch (Exception ex) {
                System.out.println("Could not get credentials from database");
            }
        }
    }

    public List<Credentials> getCredentialsList() {
        if (credentialsList == null) {
            fetchCredentials();
        }
        return Collections.unmodifiableList(credentialsList);
    }

    public List<Jid> getDomains() {
        return getCredentialsList().stream()
                .map(c -> Jid.of(c.getJid().getDomain()))
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Jid> getPingTargets() {
        return Stream.concat(
                getDomains().stream(),
                additionalDomains.stream())
                .sorted()
                .collect(Collectors.toList());
    }
}
