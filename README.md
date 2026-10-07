<<<<<<< HEAD
# Fun Roast Telegram Bot

Java 17 + Spring Boot 3.2.3 + Telegram Long Polling.

## Features
- Replies to every normal text message with a playful Hindi/Hinglish roast.
- Reply varies by message content and time of day.
- `/roast <text>` for a direct roast.
- `/menu` and `/help`.
- `/health` endpoint remains available for Render.
- No external AI API is required; roast generation is local and deterministic-by-context with random variations.

## Group chats
Telegram group bots do not receive every normal message while Privacy Mode is enabled. In BotFather use `/setprivacy` and disable it, or make the bot an administrator, if you want the bot to see ordinary group messages.

## Safety
The roast engine is intended for playful banter. It avoids hate slurs, threats, and abuse targeting protected groups.

## Run
```bash
mvnw.cmd clean package -DskipTests
mvnw.cmd spring-boot:run
```

Set `TELEGRAM_BOT_TOKEN` in the environment. Do not commit the real token to GitHub.


## Fun Roast v2
- Reads sender first name/username from Telegram updates.
- Includes the sender name in every automatic roast.
- Uses message keywords, length, and time of day for varied Hinglish friend-style roasts.
- `/roast` and menu button are supported.
- No external AI API required.
=======
# Roasting_bot
>>>>>>> 545088e8bc8882423084be18c9698ce7fca763b6
