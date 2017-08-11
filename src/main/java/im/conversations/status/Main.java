package im.conversations.status;

import im.conversations.status.persistence.ServerStatusStore;
import im.conversations.status.pojo.Configuration;
import im.conversations.status.pojo.Credentials;
import im.conversations.status.web.Controller;
import im.conversations.status.xmpp.ServerStatusChecker;
import spark.TemplateEngine;
import spark.template.freemarker.FreeMarkerEngine;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;

public class Main {

    public static void main(String... args) {
        ipAddress("127.0.0.1");
        port(4567);
        final TemplateEngine templateEngine = new FreeMarkerEngine();
        before((request, response) -> {
            if (!request.pathInfo().endsWith("/")) {
                response.redirect(request.pathInfo() + "/");
            }
        });

        get("/", Controller.getStatus, templateEngine);
        get("/historical/", Controller.getHistorical,templateEngine);
        get("/:domain/", Controller.getStatus, templateEngine);
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5);
        for (Credentials credentials : Configuration.getInstance().getCredentials()) {
            scheduledThreadPoolExecutor.scheduleAtFixedRate(new ServerStatusChecker(credentials, Configuration.getInstance().getPingTargets()), 0, 2, TimeUnit.MINUTES);
        }
        scheduledThreadPoolExecutor.scheduleAtFixedRate(new ServerStatusStore.HistoricalDataUpdater(),0,10,TimeUnit.MINUTES);
    }
}
