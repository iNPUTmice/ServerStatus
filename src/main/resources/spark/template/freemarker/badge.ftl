<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
         width="190" height="20">
    <linearGradient id="b" x2="0" y2="100%">
        <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
        <stop offset="1" stop-opacity=".1"/>
    </linearGradient>
    <clipPath id="a">
        <rect width="190" height="20" rx="3" fill="#fff"/>
    </clipPath>
        <g clip-path="url(#a)">
            <path fill="#555" d="M0 0h140v20H0z"/>
            <path fill="<#if availability?exists><#if 99.5 < availability>#43A047<#else>#e53935</#if><#else>#9D9D9D</#if>" d="M140 0h50v20H140z"/>
            <path fill="url(#b)" d="M0 0h190v20H0z"/>
        </g>

    <g fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,Geneva,sans-serif" font-size="110">
        <text x="720" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)">Up time (&#216; 30 days)</text>
        <text x="720" y="140" transform="scale(.1)">Up time (&#216; 30 days)</text>
        <#if availability?exists>
            <text x="1650" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)">${availability?string["0.##"]}&percnt;</text>
            <text x="1650" y="140" transform="scale(.1)">${availability?string["0.##"]}&percnt;</text>
        <#else>
            <text x="1650" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)">n/a</text>
            <text x="1650" y="140" transform="scale(.1)">n/a</text>
        </#if>
    </g>
</svg>