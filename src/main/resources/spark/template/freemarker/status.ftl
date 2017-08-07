<#ftl output_format="HTML">
<!DOCTYPE html>
<html>
<head>
    <link href='https://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>XMPP Server Status for ${domain}</title>
    <style type="text/css">
        body {
            color: rgba(0, 0, 0, 0.87);
            font-family: 'Roboto', sans-serif;
            font-weight: 400;
            font-size: 13pt;
            background-color: #fafafa;
        }

        h1 {
            color: rgba(0, 0, 0, 0.87);
            font-family: 'Roboto', sans-serif;
            font-weight: 600;
            font-size: 20pt;
        }

        table {
            border-collapse: collapse;
        }

        td {
            padding-right: 6px;
            padding-left: 6px;
        }

        table tr:hover td {
            background-color: #e0e0e0;
        }

        table tbody tr td.successful {
            color: #43a047;
        }

        table tbody tr td.unsuccessful {
            color: #e53935;
        }

        a {
            color: #3f51b5;
        }

        p.small {
            font-size: 10pt;
        }
    </style>
</head>
<body>
<#if isLoggedIn??>
    <#if isLoggedIn>
    <h1>${domain} is up and running</h1>
    <table>
        <#list pingResults as result>
            <tr>
                <td>
                <#if availableDomains?seq_contains(result.getServer())>
                    <a href="/<#if availableDomains?seq_index_of(result.getServer()) != 0>${result.getServer()}/</#if>">${result.getServer()}</a>
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
<p class="small">Last updated: ${lastUpdated?datetime}</p>
<#else>
<p>No information available on ${domain}</p>
</#if>

</body>
</html>