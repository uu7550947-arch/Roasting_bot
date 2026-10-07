package com.example.tw_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Locale;

@Component
public class TwitterStatusBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final String botToken;
    private final TelegramClient telegramClient;
    private final FunRoastEngine roastEngine = new FunRoastEngine();

    public TwitterStatusBot(@Value("${telegram.bot.token}") String botToken) {
        this.botToken = botToken;
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public String getBotToken() { return botToken; }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() { return this; }

    @Override
    public void consume(Update update) {
        try {
            if (update.hasCallbackQuery()) {
                long chatId = update.getCallbackQuery().getMessage().getChatId();
                String senderName = getSenderName(update);
                String data = update.getCallbackQuery().getData();
                if ("ROAST".equals(data)) {
                    sendText(chatId, roastEngine.reply("button dabaya", senderName));
                } else if ("HELP".equals(data)) {
                    sendHelp(chatId);
                }
                return;
            }
            if (!update.hasMessage() || !update.getMessage().hasText()) return;

            long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText().trim();
            String senderName = getSenderName(update);
            String command = text.split("\\s+")[0].toLowerCase(Locale.ROOT);
            int at = command.indexOf('@');
            if (at > 0) command = command.substring(0, at);

            if ("/start".equals(command) || "/menu".equals(command)) {
                sendMenu(chatId);
                return;
            }
            if ("/help".equals(command)) {
                sendHelp(chatId);
                return;
            }
            if ("/roast".equals(command)) {
                String rest = text.length() > command.length() ? text.substring(command.length()).trim() : "";
                sendText(chatId, rest.isBlank() ? roastEngine.reply("bakchodi", senderName) : roastEngine.reply(rest, senderName));
                return;
            }

            // Every normal text message gets a playful roast reply.
            sendText(chatId, roastEngine.reply(text, senderName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSenderName(Update update) {
        if (update.hasMessage() && update.getMessage().getFrom() != null) {
            var user = update.getMessage().getFrom();
            if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
                String name = user.getFirstName();
                if (user.getLastName() != null && !user.getLastName().isBlank()) {
                    name += " " + user.getLastName();
                }
                return name;
            }
            if (user.getUserName() != null && !user.getUserName().isBlank()) return "@" + user.getUserName();
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getFrom() != null) {
            var user = update.getCallbackQuery().getFrom();
            if (user.getFirstName() != null && !user.getFirstName().isBlank()) return user.getFirstName();
            if (user.getUserName() != null && !user.getUserName().isBlank()) return "@" + user.getUserName();
        }
        return "Bhai";
    }

    private void sendMenu(long chatId) {
        String text = "🤖 *FUN ROAST AI BOT*\n\n" +
                "Har normal message ka context/time ke hisaab se random roast milega. 😂\n\n" +
                "🔥 `/roast text` — custom roast\n" +
                "📖 `/help` — help\n\n" +
                "⚠️ Ye bot playful gaali/roast ke liye hai; hate slurs, threats aur protected-group abuse generate nahi karta.";

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("🔥 Roast Me").callbackData("ROAST").build()))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("📖 Help").callbackData("HELP").build()))
                .build();
        try {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .parseMode("Markdown")
                    .replyMarkup(keyboard)
                    .build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendHelp(long chatId) {
        sendText(chatId, "📖 *Fun Roast Bot*\n\n" +
                "• Normal message bhejo → automatic roast\n" +
                "• `/roast tumhara message` → custom roast\n" +
                "• `/menu` → menu\n\n" +
                "Group me har message receive karne ke liye BotFather me `/setprivacy` → *Disable* karo, ya bot ko admin banao.");
    }

    private void sendText(long chatId, String text) {
        try {
            telegramClient.execute(SendMessage.builder().chatId(chatId).text(text).build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
