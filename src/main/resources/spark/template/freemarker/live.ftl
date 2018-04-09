<#ftl output_format="HTML">
<#import "page.ftl" as page/>
<@page.page title="Checking status of ${domain}" historical=false>
<script>
    xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4) {
            if(this.responseText == "AVAILABLE") {
                var redirectUrl = window.location.protocol + "//" + location.hostname + ":" + location.port + "/${domain}";
                window.location.replace(redirectUrl);
            } else if(this.status != 404) {
                setTimeout(function() {
                    this.open("GET", "/availability/${domain}", true);
                    this.send();
                }.bind(this),500);
            }
        }
    };
    xhttp.open("GET", "/availability/${domain}", true);
    xhttp.send();
</script>
<style>
    .status > *,.status {
        font-size: 1.2em;
        display: inline;
        vertical-align: middle;
        line-height: 116px;
        margin: 10px;
    }
.loader {
    border: 16px solid #f3f3f3; /* Light grey */
    border-top: 16px solid #43a047; /* Blue */
    border-radius: 50%;
    display: inline-block;
    width: 60px;
    height: 60px;
    animation: spin 2s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}
</style>
<div class = "status">
    <div class = "loader"></div>
    <div>Checking current status for ${domain}</div>
</div>
</@page.page>
