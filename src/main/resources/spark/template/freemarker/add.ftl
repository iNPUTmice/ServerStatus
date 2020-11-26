<#ftl output_format="HTML">
<#import "page.ftl" as page/>
<@page.page title="Add server for XMPP status checker" historical=false>
<#assign accent= "#3f51b5">
<style>
    form > * {
        font-size: 0.9em;
    }
    label {
        display: inline-block;
        width: 80px;
        margin-right: 10px;
        text-align: right;
    }
    input {
        width: 300px;
        font-size: 0.9em;
        background: none;
        color: black;
        text-decoration: none;
        outline: none !important;
        border: none;
        border-bottom: solid 2px ${accent};
    }
    form > div {
        margin: 5px;
        padding: 5px;
    }
    #form_button {
        width: 150px;
        font-size: 0.95em;
        background: none;
        border: solid 2px ${accent};
        cursor: pointer;
        text-align: center;
        margin-left: 100px;
        margin-top: 10px;
        padding: 6px;
    }
    #form_button:hover {
      background: ${accent};
      color: #F2F3EB;
      -webkit-transition: all 0.3s;
      -transition: all 0.3s;
      -ms-transition: all 0.3s;
      -o-transition: all 0.3s;
      transition: all 0.3s;
    }
</style>
<h1>Add credentials for your server</h1>
<form action="/add/" method="post">
    <div>
        <label for="jid">JID</label>
        <input type="text" name="jid"/>
    </div>
    <div>
        <label for="password">Password</label>
        <input type="password" name="password"/>
    </div>
    <div>
        <input type="submit" id="form_button" value="Submit"/>
    </div>
</form>
</@page.page>
