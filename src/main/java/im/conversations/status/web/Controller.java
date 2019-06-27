package im.conversations.status.web;

import im.conversations.status.persistence.Database;
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
        final List<String> domains = Database.getInstance().getDomains();
        if (domains.size() == 0) {
            return new ModelAndView(null, "add.ftl");
        }
        final String param = request.params("domain");
        final String primaryDomain = Configuration.getInstance().getPrimaryDomain() == null ?
                domains.get(0): Configuration.getInstance().getPrimaryDomain();
        final String domain = param == null ? primaryDomain : Jid.ofDomain(param).getDomain();
        if (param != null && domain.equals(primaryDomain)) {
            response.redirect("/");
            return new ModelAndView(null, "redirect.ftl");
        }
        ServerStatus serverStatus = Database.getInstance().getServerStatus(domain);
        HashMap<String, Object> model = new HashMap<>();
        model.put("domain", domain);
        model.put("primaryDomain",primaryDomain);
        if (serverStatus != null) {
            model.put("serverStatus", serverStatus);
            model.put("availableDomains", domains);
        } else if(domains.contains(domain)) {
            // If domain is present in domain list but it's result is not present in server status
            response.redirect("/live/" + domain);
        }
        return new ModelAndView(model, "status.ftl");
    };

    public static TemplateViewRoute getBadge = (request, response) -> {
        final HashMap<String, Object> model = new HashMap<>();
        final String domain = request.params("domain");
        HistoricalLoginStatus statusMap = Database.getInstance().getStringHistoricalLoginStatusMap().get(domain);
        if (statusMap != null && statusMap.isAvailableForDuration(30)) {
            model.put("availability",statusMap.getForDuration(30));
        }
        return new ModelAndView(model, "badge.ftl");
    };

    public static TemplateViewRoute getHistorical = (request, response) -> {
        HashMap<String, Object> model = new HashMap<>();
        final String primaryDomain = Configuration.getInstance().getPrimaryDomain() == null ?
                Database.getInstance().getDomains().get(0) : Configuration.getInstance().getPrimaryDomain();
        model.put("primaryDomain",primaryDomain);
        model.put("serverMap", Database.getInstance().getStringHistoricalLoginStatusMap());
        model.put("durations", HistoricalLoginStatus.DURATIONS);
        model.put("availableDomains", Database.getInstance().getDomains());
        return new ModelAndView(model, "historical.ftl");
    };

    public static TemplateViewRoute getReverse = (request, response) -> {
        final String domain = request.params("domain");
        HashMap<String,Object> model = new HashMap<>();
        model.put("pingResults", Database.getInstance().getReverseStatusMap(domain));
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
        if(Database.getInstance().exists(domain)) {
            halt(400, "<p>ERROR: Domain already exists. Click <a href=\"/" + domain + "\">here</a> to check its result</p>");
        }

        // Verify credentials
        boolean verified = CredentialsVerifier.verifyCredentials(credentials);
        if(!verified) {
            halt(400,"ERROR: Invalid credentials provided");
        }

        // Add credentials to database
        boolean status = Database.getInstance().put(credentials);
        if(status) {
            response.redirect("/live/" + credentials.getJid().getDomain());
        } else {
            halt(400,"ERROR: Could not add server with the provided credentials to the database");
        }
        return null;
    };
    public static Route getAvailability = (request, response) -> {
        String domain = request.params("domain");
        ServerStatus serverStatus = Database.getInstance().getServerStatus(domain);
        if(serverStatus != null) {
            response.status(200);
            return "AVAILABLE";
        }
        else if(!Database.getInstance().exists(domain)) {
            response.status(404);
            return "INVALID";
        }
        else {
            response.status(200);
            return "UNAVAILABLE";
        }
    };
}
