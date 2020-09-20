package ru.ivan.telegram;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

class MyAmazingBot extends TelegramLongPollingBot {

    private final YamlVariables yaml = new YamlVariables("variables.yaml");
    private final YamlVariables credentials = new YamlVariables("credentials.yaml");

    @Override
    public void onUpdateReceived(Update update) {
        if (update != null && update.hasMessage()) {

            Message telegramMsg = update.getMessage();

            // We check if the update has a message and the message has text
            if (telegramMsg.hasText()) {
                String message_text = telegramMsg.getText();
                long chat_id = telegramMsg.getChatId();

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

            // We check if the update has a message and the message has document
            if (telegramMsg.hasDocument()) {
                //Set variables

                long chat_id = telegramMsg.getChatId();
                String responseMessage = getResponseMessage("document");
                SendMessage message = new SendMessage()
                        .setChatId(chat_id)
                        .setText(responseMessage); // Create a message object object
                try {
                    saveFileFromMessage(telegramMsg); //saving file
                    execute(message); // Sending our message object to user
                } catch (TelegramApiException | IOException | NullPointerException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getResponseMessage(String message_text) {
        switch (message_text) {
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
            case "document":
                return getAnswerForQuestion(("document"));
            default:
                return getAnswer("error", "splitter", "questions");
        }
    }

    private String getAnswerForQuestion(String question) {
        return getAnswer(question, "splitter", "after", "write", "line", "questions");
    }

    private String getAnswer(String... strings) {
        StringBuilder text = new StringBuilder();
        for (String var : strings) {
            text.append(yaml.getVariable(var));
            text.append(System.lineSeparator());
        }
        return text.toString();
    }

    /**
     * @param telegramMsg message from telegram
     * @throws IOException          from String, FileOutputStream
     * @throws NullPointerException from libs
     * @throws JSONException        from JSONObject
     */

    private void saveFileFromMessage(Message telegramMsg) throws IOException, NullPointerException, JSONException {
        URL url = new URL("https://api.telegram.org/bot" +
                credentials.getVariable("BotToken") +
                "/getFile?file_id=" + telegramMsg.getDocument().getFileId());
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String res = in.readLine();
        JSONObject jresult = new JSONObject(res);
        JSONObject path = jresult.getJSONObject("result");
        String file_name = telegramMsg.getDocument().getFileName();
        String file_path = path.getString("file_path");
        URL download = new URL("https://api.telegram.org/file/bot" +
                credentials.getVariable("BotToken") + "/" + file_path);
        FileOutputStream fos = new FileOutputStream(file_name);
        //System.out.println("Start upload");
        ReadableByteChannel rbc = Channels.newChannel(download.openStream());
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
        //System.out.println("Uploaded!");
    }

    @Override
    public String getBotUsername() {
        return credentials.getVariable("BotUsername");
    }

    @Override
    public String getBotToken() {
        return credentials.getVariable("BotToken");
    }
}