<#ftl output_format="HTML">
<#import "page.ftl" as page/>
<@page.page>
<table>
    <thead>
    <tr>
        <th></th>
    <#list durationInDays as days>
        <th><#if days == 1>24 hours<#else>${days} days</#if></th>
    </#list>
    </tr>
    </thead>
<#list serverMap as server, historicalData>
    <tr>
        <td>${server}</td>
        <#list durationInDays as days>
            <td><#if historicalData.isAvailableForDuration(days)>${historicalData.getForDuration(days)}<#else>
                N/A</#if></td>
        </#list>
    </tr>
</#list>
</table>
</@page.page>