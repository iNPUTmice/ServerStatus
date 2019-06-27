package im.conversations.status;

import im.conversations.status.persistence.Database;
import im.conversations.status.pojo.Configuration;
import im.conversations.status.pojo.Credentials;
import im.conversations.status.web.Controller;
import im.conversations.status.xmpp.ServerStatusChecker;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.addr.Jid;
import spark.TemplateEngine;
import spark.template.freemarker.FreeMarkerEngine;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static ScheduledThreadPoolExecutor statusCheckExecutor = new ScheduledThreadPoolExecutor(5);
    private static ScheduledThreadPoolExecutor historicDataExecutor = new ScheduledThreadPoolExecutor(1);

    public static void main(String... args) {
        Options options = new Options();
        options.addOption(new Option("c", "config", true, "Path to the config file"));
        try {
            CommandLine cmd = new DefaultParser().parse(options, args);
            String configPath = cmd.getOptionValue("c");
            if (configPath != null) {
                Configuration.setFilename(configPath);
            }
        } catch (ParseException e) {
            LOGGER.warn("unable to parse config. using default", e);
        }
        main(options);
    }

    private static void main(Options options) {


        if (Configuration.getInstance().getPrimaryDomain() == null) {
            LOGGER.info("Configuration does not have primary domain");
        }

        ipAddress(Configuration.getInstance().getIp());
        port(Configuration.getInstance().getPort());
        final TemplateEngine templateEngine = new FreeMarkerEngine();
        before((request, response) -> {
            if (!request.pathInfo().endsWith("/")) {
                response.redirect(request.pathInfo() + "/");
            }
        });

        get("/", Controller.getStatus, templateEngine);
        get("/historical/", Controller.getHistorical, templateEngine);
        post("/add/", Controller.postAdd, templateEngine);
        get("/add/", Controller.getAdd, templateEngine);
        get("/live/:domain/", Controller.getLive, templateEngine);
        get("/availability/:domain/", Controller.getAvailability);
        get("/reverse/:domain/", Controller.getReverse, templateEngine);
        get("/:domain/", Controller.getStatus, templateEngine);
        get("/badge/:domain/", Controller.getBadge, templateEngine);
        scheduleStatusCheck();
        historicDataExecutor.scheduleWithFixedDelay(new Database.HistoricalDataUpdater(), 0, 10, TimeUnit.MINUTES);

    }

    public static void scheduleStatusCheck() {
        statusCheckExecutor.getQueue().clear();
        List<Credentials> credentialsList = Database.getInstance().getCredentials();
        List<Jid> pingTargetList = Database.getInstance().getPingTargets();
        for (Credentials credentials : credentialsList) {
            statusCheckExecutor.scheduleAtFixedRate(new ServerStatusChecker(credentials, pingTargetList), 0, 2, TimeUnit.MINUTES);
        }
    }
}
