package im.conversations.status.pojo;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HistoricalLoginStatuus {

    public static List<Integer> DURATIONS = Arrays.asList(1,7,30,365);
    public static ChronoUnit UNIT = ChronoUnit.DAYS;

    private final Map<Duration,Double> durationLoginStatusMap;

    public HistoricalLoginStatuus(Map<Duration,Double> map) {
        this.durationLoginStatusMap = map;
    }

    public double getForDuration(int days) {
        return this.durationLoginStatusMap.getOrDefault(Duration.of(days,UNIT),0d);
    }

    public boolean isAvailableForDuration(int days) {
        return this.durationLoginStatusMap.containsKey(Duration.of(days,UNIT));
    }

}
