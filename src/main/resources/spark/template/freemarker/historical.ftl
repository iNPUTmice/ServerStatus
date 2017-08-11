<#ftl output_format="HTML">
<#import "page.ftl" as page/>
<@page.page title="Historical uptime data" historical=true>
<h1>Historical uptime data</h1>
<#if serverMap?size == 0>
    <p class="info">Calculating historical data</p>
<#else>
<table class="rightbound">
    <thead>
    <tr>
        <th></th>
        <#list durations as duration>
            <th><#if duration == 1>24 hours<#else>${duration} days</#if></th>
        </#list>
    </tr>
    </thead>
    <#list serverMap as server, historicalData>
        <tr>
            <td><a href="/<#if availableDomains?seq_index_of(server) != 0>${server}/</#if>">${server}</a></td>
            <#list durations as duration>
                <#if historicalData.isAvailableForDuration(duration)>
                    <#assign availability=historicalData.getForDuration(duration)>
                    <td class="<#if 99.5 < availability>successful<#else>unsuccessful</#if>">${availability?string["0.##"]}&percnt;</td>
                <#else>
                    <td class="info">N/A</td>
                </#if>
            </#list>
        </tr>
    </#list>
</table>
</#if>
</@page.page>