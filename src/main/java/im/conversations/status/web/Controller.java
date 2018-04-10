package im.conversations.status.web;

import im.conversations.status.persistence.CredentialStore;
import im.conversations.status.persistence.ServerStatusStore;
import im.conversations.status.pojo.*;
import im.conversations.status.xmpp.CredentialsVerifier;
import rocks.xmpp.addr.Jid;
import spark.ModelAndView;
import spark.Route;
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
        final String primaryDomain = Configuration.getInstance().getPrimaryDomain() == null ?
                domains.get(0).getDomain() : Configuration.getInstance().getPrimaryDomain();
        final String domain = param == null ? primaryDomain : Jid.ofDomain(param).getDomain();
        if (param != null && domain.equals(primaryDomain)) {
            response.redirect("/");
            return new ModelAndView(null, "redirect.ftl");
        }
        ServerStatus serverStatus = ServerStatusStore.INSTANCE.getServerStatus(domain);
        HashMap<String, Object> model = new HashMap<>();
        model.put("domain", domain);
        model.put("primaryDomain",primaryDomain);
        if (serverStatus != null) {
            model.put("serverStatus", serverStatus);
            model.put("availableDomains", domains);
        } else if(domains.stream().map(Jid::getDomain).anyMatch(d -> d.equals(domain))) {
            // If domain is present in domain list but it's result is not present in server status
            response.redirect("/live/" + domain);
        }
        return new ModelAndView(model, "status.ftl");
    };

    public static TemplateViewRoute getHistorical = (request, response) -> {
        HashMap<String, Object> model = new HashMap<>();
        final String primaryDomain = Configuration.getInstance().getPrimaryDomain() == null ?
                CredentialStore.INSTANCE.getDomains().get(0).getDomain() : Configuration.getInstance().getPrimaryDomain();
        model.put("primaryDomain",primaryDomain);
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

 public static TemplateViewRoute getLive = (request, response) -> {
         final String domain = request.params("domain");
         HashMap<String,Object> model = new HashMap<>();
         model.put("domain",domain);
        return new ModelAndView(model,"live.ftl");
    };

    public static TemplateViewRoute getAdd = (request, response) -> new ModelAndView(null,"add.ftl");

    public static TemplateViewRoute postAdd = (request, response) -> {
        String jid = request.queryParams("jid");
        String password = request.queryParams("password");
        Credentials credentials = new Credentials(jid,password);

        // Check for existence of domain in database
        String domain = credentials.getJid().getDomain();
        boolean domainExists = CredentialStore.INSTANCE.getCredentialsList().stream()
                .map(c -> c.getJid().getDomain())
                .anyMatch(c -> c.equals(domain));
        if(domainExists) {
            halt(400, "<p>ERROR: Domain already exists. Click <a href=\"/" + domain + "\">here</a> to check its result</p>");
        }

        // Verify credentials
        boolean verified = CredentialsVerifier.verifyCredentials(credentials);
        if(!verified) {
            halt(400,"ERROR: Invalid credentials provided");
        }

        // Add credentials to database
        boolean status = CredentialStore.INSTANCE.put(credentials);
        if(status) {
            response.redirect("/live/" + credentials.getJid().getDomain());
        } else {
            halt(400,"ERROR: Could not add server with the provided credentials to the database");
        }
        return null;
    };
    public static Route getAvailability = (request, response) -> {
        String domain = request.params("domain");
        ServerStatus serverStatus = ServerStatusStore.INSTANCE.getServerStatus(domain);
        if(serverStatus != null) {
            response.status(200);
            return "AVAILABLE";
        }
        else if(!CredentialStore.INSTANCE.getDomains().stream().anyMatch(s -> s.getDomain().equals(domain))) {
            response.status(404);
            return "INVALID";
        }
        else {
            response.status(200);
            return "UNAVAILABLE";
        }
    };
}
