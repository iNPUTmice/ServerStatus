<#ftl output_format="HTML">
<#import "page.ftl" as page/>
<@page.page title="XMPP Server Status for ${domain}" historical=false>
<#if serverStatus??>
    <#assign isLoggedIn = serverStatus.isLoggedIn()>
    <#assign pingResults = serverStatus.getPingResults()>
    <#assign lastUpdated = serverStatus.getDate()>
    <#assign linkReverse = 1 < availableDomains?size>
    <#if isLoggedIn>
    <h1>${domain} is up and running</h1>
    <table>
        <#list pingResults as result>
            <tr>
                <td>
                <#if availableDomains?seq_contains(result.getServer())>
                    <a href="/<#if availableDomains?seq_index_of(result.getServer()) != 0>${result.getServer()}/</#if>">${result.getServer()}</a>
                <#elseif linkReverse>
                    <a href="/reverse/${result.getServer()}">${result.getServer()}</a> <sup><small>R</small></sup>
                <#else>
                    ${result.getServer()}
                </#if>
                </td>
                <td class="<#if result.isSuccessful()>successful">reachable<#else>unsuccessful">unreachable</#if></td>
            </tr>
        </#list>
    </table>
    <#else>
    <h1>${domain} seems to be down</h1>
    </#if>
<p class="small info">Last updated: ${lastUpdated?datetime}</p>
<#else>
<p>No current information available on ${domain}</p>
</#if>
</@page.page>