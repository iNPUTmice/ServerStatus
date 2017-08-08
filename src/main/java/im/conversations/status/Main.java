package im.conversations.status;

import im.conversations.status.persistence.ServerStatusStore;
import im.conversations.status.pojo.Credentials;
import im.conversations.status.web.Controller;
import im.conversations.status.xmpp.ServerStatusChecker;
import rocks.xmpp.addr.Jid;
import spark.TemplateEngine;
import spark.template.freemarker.FreeMarkerEngine;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static spark.Spark.*;

public class Main {

    private static List<Jid> SERVERS = Stream.of(
            "push.siacs.eu",
            "conversations.im",
            "conference.siacs.eu",
            "jabber.ccc.de",
            "jabber.ru",
            "yax.im",
            "jabber.at",
            "jabber.de",
            "trashserver.net",
            "jabber.fr",
            "5222.de",
            "wiuwiu.de",
            "mailbox.org",
            "simplewire.de",
            "xmpp.zone",
            "jabber.calyxinstitute.org",
            "jabbim.com",
            "jabber.org",
            "home.zom.im",
            "muc.xmpp.org",
            "draugr.de",
            "jabber.hot-chilli.net",
            "riseup.net",
            "jabjab.de",
            "pimux.de").map(Jid::of).sorted().collect(Collectors.toList());

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
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(Math.max(Credentials.LIST.size(), 5));
        for (Credentials credentials : Credentials.LIST) {
            scheduledThreadPoolExecutor.scheduleAtFixedRate(new ServerStatusChecker(credentials, SERVERS), 0, 2, TimeUnit.MINUTES);
        }
        scheduledThreadPoolExecutor.scheduleAtFixedRate(new ServerStatusStore.HistoricalDataUpdater(),0,10,TimeUnit.MINUTES);
    }
}
