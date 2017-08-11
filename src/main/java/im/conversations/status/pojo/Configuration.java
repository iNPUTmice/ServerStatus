package im.conversations.status.pojo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import im.conversations.status.json.JidDeserializer;
import rocks.xmpp.addr.Jid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Configuration {

    private static File FILE = new File("config.json");
    private static Configuration INSTANCE;

    private List<Credentials> credentials = Collections.emptyList();
    private List<Jid> additionalDomains = Collections.emptyList();
    private List<Jid> domains = Collections.emptyList();
    private List<Jid> pingTargets = new ArrayList<>();

    private Configuration() {

    }

    public List<Credentials> getCredentials() {
        return credentials;
    }

    public List<Jid> getDomains() {
        return domains;
    }

    public List<Jid> getPingTargets() {
        return pingTargets;
    }

    public synchronized static void setFilename(String filename) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Unable to set filename after instance has been created");
        }
        Configuration.FILE = new File(filename);
    }

    public synchronized  static  Configuration getInstance() {
        INSTANCE = load();
        return INSTANCE;
    }

    private static Configuration load() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Jid.class,new JidDeserializer());
        final Gson gson = gsonBuilder.create();
        try {
            final Configuration configuration = gson.fromJson(new FileReader(FILE),Configuration.class);
            configuration.domains = configuration.credentials.stream().map(c -> Jid.of(c.getJid().getDomain())).collect(Collectors.toList());
            configuration.pingTargets.addAll(configuration.domains);
            configuration.pingTargets.addAll(configuration.additionalDomains);
            Collections.sort(configuration.pingTargets);
            return configuration;
        } catch (FileNotFoundException e) {
            return new Configuration();
        }
    }
}
