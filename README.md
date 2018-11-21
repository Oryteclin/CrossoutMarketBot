# Crossout Market Bot
Get CrossoutDB Data right into Discord.

#### configuration.json file
The configuration.json file contain two token (production & test).
Here's an example : 

```json
{
   "token": {
     "release": "<production token here>",
     "indev": "<test token here>"
   }
}
```

#### database.json file
This file contains all settings for the bot to be able to connect to a database.

 ```json
{
   "host": "localhost",
   "user": "mysqlUser",
   "pass": "mysqlPassword",
   "name": "mysqlDatabaseName",
   "port": 3306
}
 ```

#### Translate the bot
You can translate the bot in your language by making a pull request using one of the translation file and submitting your in the [languages folder](https://github.com/alexpado/CrossoutMarketBot/tree/master/src/main/languages).
Join the Discord server to receive your "Translator" role and get notified when translations need update.

#### Features Request & Bugs Report
You can request feature and report bugs on the Discord or by opening an issue.


#### Links
[CrossoutDB Github](https://github.com/Zicore/CrossoutMarket) - [Topic on Crossout Forum](https://forum.crossout.net/index.php?/topic/295123-crossout-market-discord-bot/) - [Discord Support Server](https://discord.gg/hxAx8yP) - [Invite The Bot](https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot)