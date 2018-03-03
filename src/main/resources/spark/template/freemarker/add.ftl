<#ftl output_format="HTML">
<#import "page.ftl" as page/>
<@page.page title="Add server for XMPP status checker" historical=false add=true>
<h1>Add credentials for your server</h1>
<form action="/add/" method="post">
    JID:<br>
    <input type="text" name="jid"/><br>
    Password:<br>
    <input type="password" name="password"/><br>
    <input type="submit" value="Submit"/>
</form>
</@page.page>
