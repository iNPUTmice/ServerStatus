package im.conversations.status.persistence;

import im.conversations.status.pojo.Credentials;

import java.util.HashMap;

public class ThreeStrikesStore {

    public static final ThreeStrikesStore INSTANCE = new ThreeStrikesStore();

    private final HashMap<Credentials,StrikeCounter> store = new HashMap<>();

    private ThreeStrikesStore() {

    }

    public boolean strike(Credentials credentials) {
        synchronized (store) {
            return store.computeIfAbsent(credentials,k -> new StrikeCounter()).strike();
        }
    }

    private static class StrikeCounter {
        private int count = 0;

        private boolean strike() {
            count++;
            return count >= 3;
        }
    }

}
