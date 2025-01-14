package com.company;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Component
public class MyTelegramBot extends TelegramLongPollingBot {

	private HashMap<Long, String> userChatIds = new HashMap<>();
	private HashMap<Long, Long> adminToUserChatMap = new HashMap<>();

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasCallbackQuery()) {
			handleCallbackQuery(update);
		} else if (update.hasMessage() && update.getMessage().hasText()) {
			String messageText = update.getMessage().getText();
			long chatId = update.getMessage().getChatId();

			userChatIds.put(chatId, update.getMessage().getFrom().getUserName());
			
			 if (messageText.startsWith("/reply")) {
		            handleReplyCommand(chatId, messageText.split(" "));
		        }
			 else if (adminToUserChatMap.containsKey(chatId)) {
		            long userChatId = adminToUserChatMap.get(chatId);
		            sendMessageToUser(userChatId, messageText);
		        }
			 else if (messageText.startsWith("/endchat")) {
				    handleEndChatCommant(chatId);
				}


			if (messageText.startsWith("/start")) {
				sendGreetingMessage(chatId);
			} else if (messageText.startsWith("Заказать")) {
				sendHelpMessage(chatId);
			} else if (messageText.startsWith("Помощь")) {
				sendHelpMessage(chatId);
			} else if (messageText.startsWith("Каталог товаров")) {
				showAllCategory(chatId);
			} else if (messageText.startsWith("Купить")) {
				showAll(chatId);
			} else {
				sendUnknowMessage(chatId);
			}
		}
	}

	private void handleCallbackQuery(Update update) {
		String callbackData = update.getCallbackQuery().getData();
		long chatId = update.getCallbackQuery().getMessage().getChatId();

		switch (callbackData) {
		case "buy":
			showAllCategory(chatId);
			break;
		case "showAll":
			showAll(chatId);
		default:
			break;
		}
	}

	public void sendGreetingMessage(long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Добро пожаловать в бот! Выберите действие:");

		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		keyboardMarkup.setResizeKeyboard(true);

		KeyboardRow row = new KeyboardRow();
		row.add(new KeyboardButton("Купить"));
		row.add(new KeyboardButton("Помощь"));
		row.add(new KeyboardButton("Каталог товаров"));

		List<KeyboardRow> keyboard = new ArrayList<>();
		keyboard.add(row);
		keyboardMarkup.setKeyboard(keyboard);

		message.setReplyMarkup(keyboardMarkup);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public void showAllCategory(long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(String.valueOf(chatId));
		message.setText("Выберите действие");

		InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

		List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

		List<InlineKeyboardButton> row1 = new ArrayList<>();
		InlineKeyboardButton cableButton = new InlineKeyboardButton();
		cableButton.setText("Кабель");
		cableButton.setCallbackData("buy");
		sendProducts(chatId);

		row1.add(cableButton);

		buttons.add(row1);
		markup.setKeyboard(buttons);

		message.setReplyMarkup(markup);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public void showAll(long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Список что есть в наличии");

		InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

		List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

		List<InlineKeyboardButton> row1 = new ArrayList<>();
		InlineKeyboardButton cableButton = new InlineKeyboardButton();
		cableButton.setText("Кабель");
		cableButton.setCallbackData("buy");

		row1.add(cableButton);

		buttons.add(row1);
		markup.setKeyboard(buttons);

		message.setReplyMarkup(markup);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public void sendHelpMessage(long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("напишите свое сообщение здесь, свяжемся с Вами в ближащее время!");

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}

		getInTouchWithTeam(chatId);
	}

	private void handleReplyCommand(long adminChatId, String[] commnadsParts) {
		if (commnadsParts.length < 2) {
			sendMessageToUser(adminChatId, "User reply ");
			return;
		}

		try {
			long userChatId = Long.parseLong(commnadsParts[1]);
			adminToUserChatMap.put(adminChatId, userChatId);

	        sendMessage(adminChatId, "Используйте: /reply <ID пользователя>");
		} catch (NumberFormatException e) {
			sendMessage(adminChatId, "ID пользователя должен быть числом.");
		}
	}

	private void sendMessage(long adminChatId1, String text) {
		SendMessage message = new SendMessage();
		message.setChatId(adminChatId1);
		message.setText(text);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private void handleEndChatCommant(long adminChatId) {
		if (adminToUserChatMap.containsKey(adminChatId)) {
			long userChatId = adminToUserChatMap.remove(adminChatId);
			sendMessage(adminChatId, "Вы завершили общение с пользователем ID: " + userChatId);
		} else {
			sendMessage(adminChatId, "Вы не общаетесь ни с одним пользователем.");
		}
	}

	public void getInTouchWithTeam(long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Ожидайте");

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}

		SendMessage admin = new SendMessage();
		admin.setChatId("354497337");
		admin.setText("Запросили помощь" + chatId);

		try {
			execute(admin);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public void sendMessageToUser(long chatId, String text) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText(text);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public void sendUnknowMessage(long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Неопознанная команда! Нажмите кнопку 'Помощь'!");
		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public void sendProducts(long chatId) {
		String excelFilePath = "C:\\Users\\n0iko\\Desktop\\New folder\\Prodyx.xlsx";

		try (FileInputStream file = new FileInputStream(excelFilePath); Workbook workbook = new XSSFWorkbook(file)) {
			Sheet sheet = workbook.getSheetAt(0);
			for (Row row : sheet) {
				String productName = row.getCell(0).getStringCellValue();
				double productPrice = row.getCell(1).getNumericCellValue();
				String imageUrl = row.getCell(2).getStringCellValue(); // Считываем URL изображения

				// Отправка сообщения с информацией о товаре
				SendMessage message = new SendMessage();
				message.setChatId(String.valueOf(chatId));
				// Отправка фотографии по URL
				SendPhoto sendPhoto = new SendPhoto();
				sendPhoto.setChatId(String.valueOf(chatId));

				// Создаем InputFile из URL
				InputFile inputFile = new InputFile(imageUrl);
				sendPhoto.setPhoto(inputFile); // Указываем InputFile

				sendPhoto.setCaption("Товар: " + productName + "\nЦена: " + productPrice);

				try {
					execute(sendPhoto); // Отправка фото
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			SendMessage message = new SendMessage();
			message.setChatId(String.valueOf(chatId));
			message.setText("Ошибка при чтении файла Excel.");
			try {
				execute(message);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

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

}
