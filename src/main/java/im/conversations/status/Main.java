package im.conversations.status;

import rocks.xmpp.addr.Jid;
import spark.*;
import spark.template.freemarker.FreeMarkerEngine;

import java.util.HashMap;
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
            "draugr.de",
            "jabber.hot-chilli.net",
            "riseup.net",
            "jabjab.de",
            "pimux.de").map(Jid::of).sorted().collect(Collectors.toList());

    private static List<String> AVAILABLE_DOMAINS = Credentials.LIST.stream().map(credentials -> credentials.getJid().getDomain()).collect(Collectors.toList());

    public static void main(String... args) {
        ipAddress("127.0.0.1");
        port(4567);
        final TemplateEngine templateEngine = new FreeMarkerEngine();
        get("/",getStatus,templateEngine);
        get("/:domain/",getStatus,templateEngine);
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(Math.max(Credentials.LIST.size(),5));
        for(Credentials credentials : Credentials.LIST) {
            scheduledThreadPoolExecutor.scheduleAtFixedRate(new ServerStatusChecker(credentials, SERVERS), 0, 5, TimeUnit.MINUTES);
        }
    }

    private static TemplateViewRoute getStatus = (request, response) -> {
        if (Credentials.LIST.size() == 0) {
            halt(500,"You have to configure at least on server");
            return null;
        }
        String param = request.params("domain");
        String domain = param == null ? Credentials.LIST.get(0).getJid().getDomain() : Jid.ofDomain(param).getDomain();
        ServerStatus serverStatus = ServerStatusStore.INSTANCE.get(domain);
        HashMap<String,Object> model = new HashMap<>();
        model.put("domain",domain);
        if (serverStatus != null) {
            model.put("isLoggedIn", serverStatus.isLoggedIn());
            model.put("pingResults", serverStatus.getPingResults());
            model.put("lastUpdated", serverStatus.getDate());
            model.put("availableDomains", AVAILABLE_DOMAINS);
        }
        return new ModelAndView(model,"status.ftl");
    };

}
