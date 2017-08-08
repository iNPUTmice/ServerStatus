package im.conversations.status.pojo;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HistoricalLoginStatuus {

    public static List<Integer> DURATION_IN_DAYS = Arrays.asList(1,7,30,365);

    private final HashMap<Duration,Double> durationLoginStatusMap;

    public HistoricalLoginStatuus(HashMap<Duration,Double> map) {
        this.durationLoginStatusMap = map;
    }

    public double getForDuration(int days) {
        return this.durationLoginStatusMap.getOrDefault(Duration.ofDays(days),0d);
    }

    public boolean isAvailableForDuration(int days) {
        return this.durationLoginStatusMap.containsKey(Duration.ofDays(days));
    }

}
