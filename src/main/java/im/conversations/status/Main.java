package im.conversations.status;

import im.conversations.status.persistence.CredentialStore;
import im.conversations.status.persistence.ServerStatusStore;
import im.conversations.status.pojo.Configuration;
import im.conversations.status.pojo.Credentials;
import im.conversations.status.web.Controller;
import im.conversations.status.xmpp.ServerStatusChecker;
import org.apache.commons.cli.*;
import rocks.xmpp.addr.Jid;
import spark.TemplateEngine;
import spark.template.freemarker.FreeMarkerEngine;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;

public class Main {

    private static ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5);
    public static void main(String... args) {
        Options options = new Options();
        options.addOption(new Option("c","config",true,"Path to the config file"));
        try {
            CommandLine cmd = new DefaultParser().parse( options, args);
            String configPath = cmd.getOptionValue("c");
            if (configPath != null) {
                Configuration.setFilename(configPath);
            }
        } catch (ParseException e) {
            //ignore. Just start with default config
        }
        main(options);
    }

    private static void main(Options options) {
        try {
            if (CredentialStore.INSTANCE.getCredentialsList().size() == 0) {
                System.out.println("Database doesnot contain credentials.");
            }
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            return;
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
        post("/add/",Controller.postAdd, templateEngine);
        get("/add/", Controller.getAdd, templateEngine);
        get("/reverse/:domain/", Controller.getReverse, templateEngine);
        get("/:domain/", Controller.getStatus, templateEngine);
        scheduleStatusCheck();
    }

    public static void scheduleStatusCheck() {
        scheduledThreadPoolExecutor.getQueue().clear();
        List<Credentials> credentialsList = CredentialStore.INSTANCE.getCredentialsList();
        List<Jid> pingTargetList = CredentialStore.INSTANCE.getPingTargets();
        for (Credentials credentials : credentialsList) {
            scheduledThreadPoolExecutor.scheduleAtFixedRate(new ServerStatusChecker(credentials, pingTargetList), 0, 2, TimeUnit.MINUTES);
        }
        scheduledThreadPoolExecutor.scheduleAtFixedRate(new ServerStatusStore.HistoricalDataUpdater(),0,10,TimeUnit.MINUTES);
    }
}
