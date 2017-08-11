XMPP Server Status
==================

This project aims to provide an all-in-one solution for monitoring the uptime of XMPP of one or many servers.

It’s primary job is to answer the “is X down or is it just me?” question and also to display the status of some (common) S2S connections. Furthermore it will record historic data and display the average uptime over the last 24 hours, 7 days, 30 days and a year.

Configuration happens in a file called `config.json`.


```
git clone https://github.com/iNPUTmice/ServerStatus.git
cd ServerStatus
mvn package
cp config.example.json config.json
java -jar target/ServerStatus.jar
```

