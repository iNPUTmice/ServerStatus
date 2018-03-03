package im.conversations.status.web;

import im.conversations.status.persistence.CredentialStore;
import im.conversations.status.persistence.ServerStatusStore;
import im.conversations.status.pojo.*;
import rocks.xmpp.addr.Jid;
import spark.ModelAndView;
import spark.TemplateViewRoute;

import java.util.HashMap;
import java.util.List;

import static spark.Spark.halt;

public class Controller {

    public static TemplateViewRoute getStatus = (request, response) -> {
        final List<Jid> domains = CredentialStore.INSTANCE.getDomains();
        if (domains.size() == 0) {
            return new ModelAndView(null, "add.ftl");
        }
        final String param = request.params("domain");
        final String primaryDomain = Configuration.getInstance().getPrimaryCredentials().getJid().getDomain();
        final String domain = param == null ? primaryDomain : Jid.ofDomain(param).getDomain();
        if (param != null && domain.equals(primaryDomain)) {
            response.redirect("/");
            return new ModelAndView(null, "redirect.ftl");
        }
        ServerStatus serverStatus = ServerStatusStore.INSTANCE.getServerStatus(domain);
        HashMap<String, Object> model = new HashMap<>();
        model.put("domain", domain);
        if (serverStatus != null) {
            model.put("serverStatus", serverStatus);
            model.put("availableDomains", domains);
        } else if(domains.stream().map(d -> d.getDomain()).anyMatch(d -> d.equals(domain))) {
            // If domain is present in domain list but it's result is not present in server status
            halt(200,"Running tests on the server. Refresh after some time to see results");
        }
        return new ModelAndView(model, "status.ftl");
    };

    public static TemplateViewRoute getHistorical = (request, response) -> {
        HashMap<String, Object> model = new HashMap<>();
        model.put("serverMap", ServerStatusStore.INSTANCE.getStringHistoricalLoginStatuusMap());
        model.put("durations", HistoricalLoginStatuus.DURATIONS);
        model.put("availableDomains", CredentialStore.INSTANCE.getDomains());
        return new ModelAndView(model, "historical.ftl");
    };

    public static TemplateViewRoute getReverse = (request, response) -> {
        final String domain = request.params("domain");
        HashMap<String,Object> model = new HashMap<>();
        model.put("pingResults", ServerStatusStore.INSTANCE.getReverseStatusMap(domain));
        model.put("domain",domain);
        return new ModelAndView(model, "reverse.ftl");
    };

    public static TemplateViewRoute getAdd = (request, response) -> new ModelAndView(null,"add.ftl");

    public static TemplateViewRoute postAdd = (request, response) -> {
        String jid = request.queryParams("jid");
        String password = request.queryParams("password");
        Credentials credentials = new Credentials(jid,password);
        boolean status = CredentialStore.INSTANCE.put(credentials);
        if(status) {
            response.redirect("/" + credentials.getJid().getDomain());
        } else {
            halt(400,"ERROR: Could not add server with the provided credentials");
        }
        return null;
    };
}
