package im.conversations.status.persistence;

public class HistoricalDataNotAvailableException extends Exception {

    public HistoricalDataNotAvailableException(String message) {
        super(message);
    }

    public HistoricalDataNotAvailableException(Exception e) {
        super(e);
    }
}
