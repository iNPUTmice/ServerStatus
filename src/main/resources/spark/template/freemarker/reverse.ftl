<#ftl output_format="HTML">
<#import "page.ftl" as page/>
<#assign title="Reverse reachability for ${domain}">
<@page.page title=$title historical=false>
<#if 1 < pingResults?size>
<h1>${title}</h1>
<table class="rightbound">
    <#list pingResults as result>
        <tr>
            <td><a href="/${result.getServer()}/">${result.getServer()}</a></td>
            <td class="<#if result.isSuccessful()>successful">reachable<#else>unsuccessful">unreachable</#if></td>
        </tr>
    </#list>
</table>
<#else>
    <p>No current information available on ${domain}</p>
</#if>
</@page.page>