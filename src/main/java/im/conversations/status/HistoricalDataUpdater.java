package im.conversations.status;

import im.conversations.status.persistence.Database;
import im.conversations.status.persistence.HistoricalDataNotAvailableException;
import im.conversations.status.pojo.HistoricalLoginStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoricalDataUpdater implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoricalDataUpdater.class);

    @Override
    public void run() {
        final Instant start = Instant.now();
        Database.getInstance().discardExpired();
        final List<String> domains = Database.getInstance().getDomains();
        for (final String domain : domains) {
            final HistoricalLoginStatus status = create(domain);
            Database.getInstance().put(domain, status);
        }
        LOGGER.info("calculated historic data for " + domains.size() + " domains in " + Duration.between(start, Instant.now()));
    }

    private HistoricalLoginStatus create(String server) {
        final Map<Duration, Double> map = new HashMap<>();
        for (int d : HistoricalLoginStatus.DURATIONS) {
            try {
                final Duration duration = Duration.of(d, HistoricalLoginStatus.UNIT);
                map.put(duration, Database.getInstance().getHistoricalLoginStatus(server, duration));
            } catch (HistoricalDataNotAvailableException e) {
                //ignore information not available
            }
        }
        return new HistoricalLoginStatus(map);
    }
}
