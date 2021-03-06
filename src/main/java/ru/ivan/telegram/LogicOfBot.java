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

class LogicOfBot extends TelegramLongPollingBot {

    private final YamlVariables YAML_VARIABLES = new YamlVariables("variables.yaml");
    private final YamlVariables YAML_CREDENTIALS = new YamlVariables("credentials.yaml");

    private final static boolean IS_LOGGING = false;

    @Override
    public void onUpdateReceived(Update update) {
        if (update != null && update.hasMessage()) {

            try {
                Message telegramMsg = update.getMessage();
                long chatId = telegramMsg.getChatId();

                // If message has text - print response
                if (telegramMsg.hasText()) {
                    String message_text = telegramMsg.getText();

                    String responseMessage = getResponseMessage(message_text);
                    if (responseMessage != null) {
                        SendMessage message = new SendMessage()
                                .setChatId(chatId)
                                .setText(responseMessage);
                        execute(message); // Sending our message object to user
                    }
                }

                // if the message has document - save file
                if (telegramMsg.hasDocument()) {

                    String responseMessage = getResponseMessage("document");
                    if (responseMessage != null) {
                        SendMessage message = new SendMessage()
                                .setChatId(chatId)
                                .setText(responseMessage);
                        saveFileFromMessage(telegramMsg);
                        execute(message); // Sending our message object to user
                    }
                }

            } catch (TelegramApiException | IOException | NullPointerException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String getResponseMessage(String message_text) {
        switch (message_text) {
            case "/start":
                return getAnswer("start");
            case "document":
                return getAnswerForCommand("document");
            default:
                return null;
        }
    }

    private String getAnswerForCommand(String command) {
        return getAnswer(command, "splitter", "after", "write", "line", "commands");
    }

    private String getAnswer(String... strings) {
        StringBuilder text = new StringBuilder();
        for (String var : strings) {
            text.append(YAML_VARIABLES.getVariable(var));
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

        URL url = new URL(String.format("https://api.telegram.org/bot%s/getFile?file_id=%s",
                YAML_CREDENTIALS.getVariable("BotToken"), telegramMsg.getDocument().getFileId()));

        URL download = new URL(String.format("https://api.telegram.org/file/bot%s/%s",
                YAML_CREDENTIALS.getVariable("BotToken"), getPathToTelegramFile(url)));

        String fileName = telegramMsg.getDocument().getFileName();
        FileOutputStream fos = new FileOutputStream(fileName);

        if (IS_LOGGING) {
            System.out.println("Start upload");
        }

        ReadableByteChannel rbc = Channels.newChannel(download.openStream());
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();

        if (IS_LOGGING) {
            System.out.println("Uploaded!");
        }
    }

    private String getPathToTelegramFile(URL url) throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String res = in.readLine();
        JSONObject jresult = new JSONObject(res);
        JSONObject path = jresult.getJSONObject("result");

        return path.getString("file_path");
    }

    @Override
    public String getBotUsername() {
        return YAML_CREDENTIALS.getVariable("BotUsername");
    }

    @Override
    public String getBotToken() {
        return YAML_CREDENTIALS.getVariable("BotToken");
    }
}