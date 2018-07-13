package im.conversations.status.persistence;

import com.zaxxer.hikari.HikariDataSource;
import im.conversations.status.Main;
import im.conversations.status.pojo.Configuration;
import im.conversations.status.pojo.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialStore.class);
    private final Sql2o database;
    private List<Credentials> credentialsList;
    private List<Jid> additionalDomains;

    private CredentialStore() {
        additionalDomains = Configuration.getInstance().getAdditionalDomains();
        final String dbFilename = Configuration.getInstance().getStoragePath() + getClass().getSimpleName().toLowerCase(Locale.US) + ".db";
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setMaximumPoolSize(1);
        dataSource.setJdbcUrl("jdbc:sqlite:" + dbFilename);
        this.database = new Sql2o(dataSource);
        try (Connection connection = this.database.open()) {
            final String createTable = "create table if not exists credentials (jid TEXT, password TEXT)";
            connection.createQuery(createTable).executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Unable to initialize database", e);
        }
    }

    public boolean put(Credentials credentials) {
        try (Connection connection = this.database.open()) {
            final String addCreds = "INSERT into credentials(jid,password) VALUES(:jid,:password)";
            connection.createQuery(addCreds).bind(credentials).executeUpdate();
        } catch (Exception ex) {
            return false;
        }
        fetchAndReschedule();
        return true;
    }

    private void fetchAndReschedule() {
        fetchCredentials();
        Main.scheduleStatusCheck();
    }

    private void fetchCredentials() {
        try (Connection connection = this.database.open()) {
            final String selectCreds = "SELECT jid,password from credentials";
            this.credentialsList = connection.createQuery(selectCreds).executeAndFetch(Credentials.class);
        } catch (Exception ex) {
            LOGGER.error("Unable to load credentials from database");
        }
    }

    public boolean delete(Credentials credentials) {
        try (Connection connection = this.database.open()) {
            final String SQL = "DELETE FROM credentials WHERE jid = :jid AND password = :password";
            int numRows = connection.createQuery(SQL).bind(credentials).executeUpdate().getResult();
            if (numRows == 0) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        fetchAndReschedule();
        return true;
    }

    public List<Jid> getPingTargets() {
        return Stream.concat(
                getDomains().stream(),
                additionalDomains.stream())
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Jid> getDomains() {
        return getCredentialsList().stream()
                .map(c -> Jid.of(c.getJid().getDomain()))
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Credentials> getCredentialsList() {
        if (credentialsList == null) {
            fetchCredentials();
        }
        return Collections.unmodifiableList(credentialsList);
    }
}
