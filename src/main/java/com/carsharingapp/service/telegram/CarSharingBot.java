package com.carsharingapp.service.telegram;

import com.carsharingapp.exception.TelegramBotSendMessageException;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class CarSharingBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(CarSharingBot.class);

    @Value("${telegram.bot.username}")
    private String botName;

    @Value("${bot.token}")
    private String token;

    private final Set<String> activeChatIds = new HashSet<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = String.valueOf(update.getMessage().getChatId());
            addChatId(chatId);

            String messageText = update.getMessage().getText();
            if (messageText.equals("/start")) {
                sendOnStartCommand(update.getMessage().getChat().getFirstName());
            }
        }
    }

    private void addChatId(String chatId) {
        activeChatIds.add(chatId);
    }

    private void sendOnStartCommand(String name) {
        String message = """
                Hi, %s! 🌟 Welcome aboard! We're excited to have you here! 😄
                                 
                In this bot, you'll receive timely notifications about:
                🔹 Newly created rentals
                🔹 Successful payments
                🔹 Overdue rentals (we hope you won't encounter any! 😉)
                🔹 Reminders for any overdue rentals
                                 
                We wish you a fantastic journey ahead! 🌍 Enjoy your trip!
                """.formatted(name);
        logger.info("Sending start command message to {}: {}", name, message);
        sendMessage(message);
    }

    public void sendMessage(String text) {
        String chatId = getChatId();
        SendMessage message = new SendMessage(chatId, text);
        try {
            execute(message);
            logger.info("Message sent successfully to chat ID {}: {}", chatId, text);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message to chat ID {}: {}", chatId, text, e);
            throw new TelegramBotSendMessageException("Failed to send a message: " + text, e);
        }
    }

    private String getChatId() {
        return activeChatIds.stream()
                .findFirst()
                .orElseThrow(null);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
