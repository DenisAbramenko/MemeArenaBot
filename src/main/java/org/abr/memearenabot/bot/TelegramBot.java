package org.abr.memearenabot.bot;

import org.abr.memearenabot.service.MemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class TelegramBot extends TelegramLongPollingBot {
    private MemeService memeService = new MemeService();

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText();

            if (text.equals("/start")) {
                sendMessage(chatId, "Привет! Я — Memify. Пришли мне идею, и я сделаю мем.");
            } else {
                String memeUrl = memeService.generateMeme(text);
                sendMessage(chatId, "Вот твой мем: " + memeUrl);
            }
        }
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}
