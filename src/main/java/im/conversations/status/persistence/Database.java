package im.conversations.status.persistence;

import com.zaxxer.hikari.HikariDataSource;
import im.conversations.status.Main;
import im.conversations.status.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;
import rocks.xmpp.addr.Jid;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Database {

    private static final String CREATE_LOGIN_STATUS = "CREATE TABLE IF NOT EXISTS login_status (server VARCHAR(255), timestamp DATETIME, status INTEGER, index server_index(server))";
    private static final String CREATE_CREDENTIALS = "CREATE TABLE IF NOT EXISTS credentials (username VARCHAR(255), domain VARCHAR(255), password VARCHAR(255))";

    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class.getName());
    private static final Database INSTANCE = new Database();
    private final Sql2o database;
    private final HashMap<String, ServerStatus> serverStatusMap = new HashMap<>();
    private final HashMap<String, HistoricalLoginStatus> serverHistoricalLoginStatusMap = new LinkedHashMap<>();

    private Database() {
        final Configuration config = Configuration.getInstance();
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(config.getDbUrl());
        dataSource.setUsername(config.getDbUsername());
        dataSource.setPassword(config.getDbPassword());
        dataSource.setMaximumPoolSize(3);
        this.database = new Sql2o(dataSource);
        try (Connection connection = this.database.open()) {
            connection.createQuery(CREATE_LOGIN_STATUS).executeUpdate();
            connection.createQuery(CREATE_CREDENTIALS).executeUpdate();
        } catch (Exception e) {
            LOGGER.error("unable initialize database", e);
        }
    }

    public static Database getInstance() {
        return INSTANCE;
    }

    public void put(String server, ServerStatus serverStatus) {
        synchronized (serverStatusMap) {
            serverStatusMap.put(server, serverStatus);
        }
        try (Connection connection = this.database.beginTransaction(java.sql.Connection.TRANSACTION_SERIALIZABLE)) {
            connection.createQuery("INSERT INTO login_status(server,timestamp,status) VALUES(:server,:timestamp,:status)")
                    .bind(serverStatus.getLoginStatus())
                    .addParameter("server", server)
                    .executeUpdate();
            connection.commit();
        } catch (final Exception e) {
            LOGGER.warn("unable to write server status to database", e);
        }
    }

    public boolean put(Credentials credentials) {
        try (Connection connection = this.database.open()) {
            connection.createQuery("INSERT into credentials(username,domain,password) VALUES(:username,:domain,:password)")
                    .addParameter("username", credentials.getJid().getEscapedLocal())
                    .addParameter("domain", credentials.getJid().getDomain())
                    .addParameter("password", credentials.getPassword())
                    .executeUpdate();
        } catch (Exception ex) {
            return false;
        }
        Main.scheduleStatusCheck();
        return true;
    }

    private void put(String server, HistoricalLoginStatus historicalLoginStatuus) {
        synchronized (serverHistoricalLoginStatusMap) {
            serverHistoricalLoginStatusMap.put(server, historicalLoginStatuus);
        }
    }

    private double getHistoricalLoginStatus(String server, Duration duration) throws HistoricalDataNotAvailableException {
        try (Connection connection = this.database.open()) {
            final Instant start = Instant.now().minus(duration);
            final int total = connection.createQuery("SELECT count(status) FROM login_status WHERE server=:server and timestamp < :start")
                    .addParameter("server", server)
                    .addParameter("start", start)
                    .executeScalar(Integer.class);
            if (total == 0) {
                throw new HistoricalDataNotAvailableException("Historical data does not reach back to " + start.toString());
            }
            final List<Boolean> statuus = connection.createQuery("SELECT status FROM login_status WHERE server=:server and timestamp >= :start")
                    .addParameter("server", server)
                    .addParameter("start", start)
                    .executeAndFetch(Boolean.class);
            final int count = statuus.size();
            if (count == 0) {
                throw new HistoricalDataNotAvailableException("No information available for time span");
            }
            final long maxDataPoints = duration.getSeconds() / 120; //every 2 minutes
            final long needed = maxDataPoints / 2;
            if (count < needed) {
                throw new HistoricalDataNotAvailableException("Not enough data points. " + count + " / " + needed);
            }
            final double successes = statuus.stream().filter(s -> s).count();
            return (successes / count) * 100;
        } catch (Sql2oException e) {
            throw new HistoricalDataNotAvailableException(e);
        }
    }

    private void discardExpired() {
        try (Connection connection = this.database.open()) {
            connection.createQuery("delete from login_status where timestamp < :timestamp")
                    .addParameter("timestamp", Instant.now().minus(Duration.ofDays(366)))
                    .executeUpdate();
        }
    }

    public List<Jid> getPingTargets() {
        return Stream.concat(
                getDomains().stream().map(Jid::ofDomain),
                Configuration.getInstance().getAdditionalDomains().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getDomains() {
        try (Connection connection = this.database.open()) {
            return connection.createQuery("select domain from credentials")
                    .executeAndFetch(String.class);
        } catch (Exception e) {
            LOGGER.error("unable to load domains from database", e);
            return Collections.emptyList();
        }
    }

    public List<Credentials> getCredentials() {
        try (Connection connection = this.database.open()) {
            return connection
                    .createQuery("SELECT concat(username,\"@\",domain) as jid,password from credentials")
                    .executeAndFetch(Credentials.class);
        } catch (Exception ex) {
            LOGGER.error("Unable to load credentials from database", ex);
            return Collections.emptyList();
        }
    }

    public boolean exists(String domain) {
        try (Connection connection = this.database.open()) {
            return connection.createQuery("select exists (select domain from credentials where domain=:domain)")
                    .addParameter("domain", domain)
                    .executeScalar(Boolean.class);
        }
    }

    public boolean delete(Credentials credentials) {
        try (Connection connection = this.database.open()) {
            final String SQL = "DELETE FROM credentials WHERE username=:username AND domain=:domain AND password = :password";
            int numRows = connection.createQuery(SQL)
                    .addParameter("username", credentials.getJid().getEscapedLocal())
                    .addParameter("domain", credentials.getJid().getDomain())
                    .addParameter("password", credentials.getPassword())
                    .executeUpdate().getResult();
            if (numRows == 0) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        Main.scheduleStatusCheck();
        return true;
    }

    public ServerStatus getServerStatus(String server) {
        synchronized (serverStatusMap) {
            return serverStatusMap.get(server);
        }
    }

    public Map<String, HistoricalLoginStatus> getStringHistoricalLoginStatusMap() {
        synchronized (serverHistoricalLoginStatusMap) {
            return Collections.unmodifiableMap(new TreeMap<>(serverHistoricalLoginStatusMap));
        }
    }

    public Collection<PingResult> getReverseStatusMap(final String server) {
        synchronized (serverStatusMap) {
            return serverStatusMap.entrySet().stream()
                    .flatMap(e -> {
                        Optional<PingResult> pr = e.getValue().getPingResult(server);
                        return pr.map(pingResult -> Stream.of(new PingResult(e.getKey(), pingResult.isSuccessful()))).orElseGet(Stream::empty);
                    })
                    .sorted(Comparator.comparing(PingResult::getServer))
                    .collect(Collectors.toList());
        }
    }

    public static class HistoricalDataUpdater implements Runnable {

        @Override
        public void run() {
            Database.getInstance().discardExpired();
            for (String domain : Database.getInstance().getDomains()) {
                final HistoricalLoginStatus status = create(domain);
                INSTANCE.put(domain, status);
            }
        }

        private HistoricalLoginStatus create(String server) {
            final Map<Duration, Double> map = new HashMap<>();
            for (int d : HistoricalLoginStatus.DURATIONS) {
                try {
                    final Duration duration = Duration.of(d, HistoricalLoginStatus.UNIT);
                    map.put(duration, INSTANCE.getHistoricalLoginStatus(server, duration));
                } catch (HistoricalDataNotAvailableException e) {
                    //ignore information not available
                }
            }
            return new HistoricalLoginStatus(map);
        }
    }

}
