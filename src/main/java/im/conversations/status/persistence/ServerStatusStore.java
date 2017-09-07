package im.conversations.status.persistence;

import im.conversations.status.pojo.Configuration;
import im.conversations.status.pojo.HistoricalLoginStatuus;
import im.conversations.status.pojo.ServerStatus;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;
import rocks.xmpp.addr.Jid;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class ServerStatusStore {

    public static final ServerStatusStore INSTANCE = new ServerStatusStore();

    private final Sql2o database;
    private final HashMap<String, ServerStatus> serverStatusMap = new HashMap<>();
    private final HashMap<String, HistoricalLoginStatuus> serverHistoricalLoginStatuusMap = new LinkedHashMap<>();

    private ServerStatusStore() {
        final String dbFilename = Configuration.getInstance().getStoragePath()+getClass().getSimpleName().toLowerCase(Locale.US) + ".db";
        this.database = new Sql2o("jdbc:sqlite:" + dbFilename, null, null);
        synchronized (this.database) {
            try (Connection connection = this.database.open()) {
                final String createTable = "create table if not exists login_status (server TEXT, timestamp TEXT, status INTEGER)";
                final String createIndex = "create index if not exists server_index on login_status(server)";
                connection.createQuery(createTable).executeUpdate();
                connection.createQuery(createIndex).executeUpdate();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public void put(String server, ServerStatus serverStatus) {
        synchronized (serverStatusMap) {
            serverStatusMap.put(server, serverStatus);
        }
        synchronized (this.database) {
            try (Connection connection = this.database.open()) {
                connection.createQuery("INSERT INTO login_status(server,timestamp,status) VALUES(:server,:timestamp,:status)")
                        .bind(serverStatus.getLoginStatus())
                        .addParameter("server", server)
                        .executeUpdate();
            } catch (Exception e) {
                System.err.println("Error writing status to database");
            }
        }
    }

    private void put(String server, HistoricalLoginStatuus historicalLoginStatuus) {
        synchronized (serverHistoricalLoginStatuusMap) {
            serverHistoricalLoginStatuusMap.put(server,historicalLoginStatuus);
        }
    }

    private double getHistoricalLoginStatus(String server, Duration duration) throws HistoricalDataNotAvailableException {
        synchronized (this.database) {
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
                final double successes = statuus.stream().filter(s -> s).count();
                return (successes / count) * 100;
            } catch (Sql2oException e) {
                throw new HistoricalDataNotAvailableException(e);
            }
        }
    }

    public ServerStatus getServerStatus(String server) {
        synchronized (serverStatusMap) {
            return serverStatusMap.get(server);
        }
    }

    public Map<String,HistoricalLoginStatuus> getStringHistoricalLoginStatuusMap() {
        synchronized (serverHistoricalLoginStatuusMap) {
            return Collections.unmodifiableMap(new LinkedHashMap<>(serverHistoricalLoginStatuusMap));
        }
    }

    public static class HistoricalDataUpdater implements Runnable {

        @Override
        public void run() {
            final List<Jid> domains = new ArrayList<>(Configuration.getInstance().getDomains());
            Collections.sort(domains);
            for(Jid domain : domains) {
                final HistoricalLoginStatuus statuus = create(domain.getDomain());
                INSTANCE.put(domain.getDomain(),statuus);
            }
        }

        private HistoricalLoginStatuus create(String server) {
            final Map<Duration,Double> map = new HashMap<>();
            for(int d : HistoricalLoginStatuus.DURATIONS) {
                try {
                    final Duration duration = Duration.of(d,HistoricalLoginStatuus.UNIT);
                    map.put(duration,INSTANCE.getHistoricalLoginStatus(server,duration));
                } catch (HistoricalDataNotAvailableException e) {
                    //ignore information not available
                }
            }
            return new HistoricalLoginStatuus(map);
        }
    }

}
