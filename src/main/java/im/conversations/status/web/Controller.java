package im.conversations.status.web;

import im.conversations.status.persistence.ServerStatusStore;
import im.conversations.status.pojo.Credentials;
import im.conversations.status.pojo.HistoricalLoginStatuus;
import im.conversations.status.pojo.ServerStatus;
import rocks.xmpp.addr.Jid;
import spark.ModelAndView;
import spark.TemplateViewRoute;

import java.util.HashMap;

import static spark.Spark.halt;

public class Controller {

    public static TemplateViewRoute getStatus = (request, response) -> {
        if (Credentials.AVAILABLE_DOMAINS.size() == 0) {
            halt(500, "You have to configure at least on server");
            response.redirect("/");
            return new ModelAndView(null, "redirect.ftl");
        }
        final String param = request.params("domain");
        final String domain = param == null ? Credentials.AVAILABLE_DOMAINS.get(0) : Jid.ofDomain(param).getDomain();
        if (param != null && Credentials.AVAILABLE_DOMAINS.get(0).equals(domain)) {
            response.redirect("/");
            return new ModelAndView(null, "redirect.ftl");
        }
        ServerStatus serverStatus = ServerStatusStore.INSTANCE.getServerStatus(domain);
        HashMap<String, Object> model = new HashMap<>();
        model.put("domain", domain);
        if (serverStatus != null) {
            model.put("serverStatus", serverStatus);
            model.put("availableDomains", Credentials.AVAILABLE_DOMAINS);
        }
        return new ModelAndView(model, "status.ftl");
    };

    public static TemplateViewRoute getHistorical = (request, response) -> {
        HashMap<String, Object> model = new HashMap<>();
        model.put("serverMap", ServerStatusStore.INSTANCE.getStringHistoricalLoginStatuusMap());
        model.put("durations", HistoricalLoginStatuus.DURATIONS);
        model.put("availableDomains", Credentials.AVAILABLE_DOMAINS);
        return new ModelAndView(model, "historical.ftl");
    };
}
