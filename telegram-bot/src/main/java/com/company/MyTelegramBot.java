package com.company;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MyTelegramBot extends TelegramLongPollingBot {
	
	@Value("${bot.name}")
	private String botName;
	
	@Value("${bot.token}")
	private String botToken;
	
	@Override
	public String getBotUsername() {
		return botName;
	}

	public String getBotToken() {
		return botToken;
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageText = update.getMessage().getText();
			String chatId = update.getMessage().getChatId().toString();
			
//			if(messageText.equals("/start")) {
//				sendStartMenu(chatId);
//			}else {
//				sendResponse(chatId, "jnvdu");
//			}
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("Добро пожаловать в бот");

			try {
				execute(message);
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}
		
		
	}

//	private void sendStartMenu(String chatId) {
//		
//		
//	}
//
//	private void sendResponse(String chatId, String string) {
//		SendMessage message = new SendMessage();
//		message.setChatId(String.valueOf(chatId));
//		message.setText(string);
//		
//		try {
//			execute(message);
//		}catch (TelegramApiException e) {
//			e.printStackTrace();
//		}
//	}
	
	

}
