<#macro page title="" historical=false>
<!DOCTYPE html>
<html>
<head>
    <link href='https://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title}</title>
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

        table.rightbound tr td:not(:first-child) {
            text-align: right;
        }

        th {
            padding-right: 6px;
            padding-left: 6px;
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

        p.info {
            color: rgba(0, 0, 0, 0.54);
        }
    </style>
</head>
<body>
    <#nested />
<p class="small">Copyright 2017 <a href="https://gultsch.de">Daniel Gultsch</a> <#if !historical>&middot; <a href="/historical/">Historical data</a> </#if>&middot; <a href="https://github.com/iNPUTmice/ServerStatus">Source code</a></p>
</body>
</html>
</#macro>