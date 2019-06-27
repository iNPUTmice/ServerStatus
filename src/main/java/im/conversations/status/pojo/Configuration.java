package im.conversations.status.pojo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import im.conversations.status.json.JidDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.addr.Jid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;

public class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private static File FILE = new File("config.json");
    private static Configuration INSTANCE;

    private List<Jid> additionalDomains = Collections.emptyList();
    private String primaryDomain;
    private String ip = "127.0.0.1";
    private int port = 4567;

    private String dbUrl;
    private String dbUsername;
    private String dbPassword;

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    private Configuration() {

    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getPrimaryDomain() {
        return primaryDomain;
    }

    public List<Jid> getAdditionalDomains() {
        return additionalDomains;
    }

    public synchronized static void setFilename(String filename) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Unable to set filename after instance has been created");
        }
        Configuration.FILE = new File(filename);
    }

    public synchronized static  Configuration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static Configuration load() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Jid.class,new JidDeserializer());
        final Gson gson = gsonBuilder.create();
        try {
            LOGGER.debug("Reading configuration from "+FILE.getAbsolutePath());
            final Configuration configuration = gson.fromJson(new FileReader(FILE),Configuration.class);
            return configuration;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Configuration file not found");
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Invalid syntax in config file");
        }
    }
}
