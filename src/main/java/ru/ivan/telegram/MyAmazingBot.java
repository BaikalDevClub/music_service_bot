package ru.ivan.telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

class MyAmazingBot extends TelegramLongPollingBot {

    private final YamlVariables yaml = new YamlVariables("variables.yaml");
    private final YamlVariables credentials = new YamlVariables("credentials.yaml");

    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables

            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            String responseMessage = getResponseMessage(message_text);

            SendMessage message = new SendMessage() // Create a message object object
                    .setChatId(chat_id)
                    .setText(responseMessage);
            try {
                execute(message); // Sending our message object to user
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private String getResponseMessage(String message_text) {
        switch (message_text){
            case "/start":
                return getAnswer("start", "line", "write", "line", "questions");
            case "1":
                return getAnswerForQuestion("q1");
            case "2":
                return getAnswerForQuestion("q2");
            case "3":
                return getAnswerForQuestion("q3");
            case "4":
                return getAnswerForQuestion("q4");
            case "5":
                return getAnswerForQuestion("q5");
            case "6":
                return getAnswerForQuestion("q6");
            case "7":
                return getAnswerForQuestion("q7");
            case "8":
                return getAnswerForQuestion("q8");
            case "0":
                return getAnswerForQuestion("q0");
            default:
                return getAnswer("error", "splitter", "questions");
        }
    }

    private String getAnswerForQuestion(String question){
        return getAnswer(question, "splitter", "after", "write", "line", "questions");
    }

    private String getAnswer(String ... strings ){
        StringBuilder text = new StringBuilder();
        for (String var : strings) {
            text.append(yaml.getVariable(var));
            text.append(System.lineSeparator());
        }
        return text.toString();
    }

    public String getBotUsername() {
        return credentials.getVariable("BotUsername");
    }

    public String getBotToken() {
        return credentials.getVariable("BotToken");
    }
}