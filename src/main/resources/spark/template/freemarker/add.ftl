<#ftl output_format="HTML">
<#import "page.ftl" as page/>
<@page.page title="Add server for XMPP status checker" historical=false add=true>
<style>
    form > * {
        font-size: 1em;
    }
    label {
        display: inline-block;
        width: 80px;
        margin-right: 10px;
        text-align: right;
    }
    input {
        width: 300px;
        font-size: 0.95em;
        background: none;
        text-decoration: none;
        outline: none !important;
        border: none;
        border-bottom: solid 2px #43a047;
    }
    form > div {
        margin: 10px;
        padding: 10px;
    }
    #form_button {
        width: 200px;
        font-size: 1em;
        background: none;
        border: solid 2px #43a047;
        cursor: pointer;
        text-align: center;
        margin-left: 50px;
        padding: 10px;
    }
    #form_button:hover {
      background: #43a047;
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
