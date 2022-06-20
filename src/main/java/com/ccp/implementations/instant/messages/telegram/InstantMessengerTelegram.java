package com.ccp.implementations.instant.messages.telegram;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.ccp.constantes.CcpConstants;
import com.ccp.decorators.CcpMapDecorator;
import com.ccp.dependency.injection.CcpEspecification;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.exceptions.http.UnexpectedHttpStatus;
import com.ccp.utils.Utils;

class InstantMessengerTelegram implements CcpInstantMessenger {
	@CcpEspecification
	private CcpHttpRequester ccpHttp;
	private Set<Long> locks = Collections.synchronizedSet(new HashSet<>());

	@Override
	public void sendMessageToSupport(String botToken, String message) {
		Long supportTelegram = Long.valueOf(System.getenv("SUPPORT_TELEGRAM"));
		long naoResponderPraNinguem = 0L;

		this.sendMessage(botToken, message, supportTelegram, naoResponderPraNinguem, CcpConstants.doNotEvaluate, CcpConstants.byPass);
	}

	@Override
	public Long getMembersCount(String botToken, Long chatId) {
		String url = this.getBotToken(botToken);
		this.ccpHttp.executeHttpRequest(url + "/getChatMemberCount?chat_id=" + chatId, "GET", CcpConstants.emptyJson, "");
		CcpHttpHandler ccpHttpHandler = new CcpHttpHandler(200, this.ccpHttp);
		try {
			CcpMapDecorator response = ccpHttpHandler.executeHttpSimplifiedGet(url, CcpHttpResponseType.singleRecord);
			if(response.getAsBoolean("ok") == false) {
				throw new RuntimeException("Erro ao contar membros do grupo " + chatId);
			}
			Long result = response.getAsLongNumber("result");
			return result;
			
		} catch (UnexpectedHttpStatus e) {
			throw new RuntimeException("Erro ao contar membros do grupo " + chatId + ". Detalhes: " + e.response.httpResponse);
		}
		
		
	}

	private String getBotToken(String botToken) {
		String url = "https://api.telegram.org/bot" + botToken;
		return url;
	}

	@Override
	public Long sendSlowlyMessage(String botToken, String message, Long chatId, Long replyTo, Predicate<String> isLockedUser, Consumer<String> lockUser) {
		
		while(this.locks.contains(chatId)) {
			Utils.sleep(1);
		}
		
		long firstTime = System.currentTimeMillis();
		this.locks.add(chatId);
		Long sendMessage = this.sendMessage(botToken, message, chatId, replyTo, isLockedUser, lockUser);
		Utils.sleep(3000 - (int)(System.currentTimeMillis() - firstTime));
		return sendMessage;
	}

	@Override
	public Long sendMessage(String botToken, String message, Long chatId, Long replyTo, Predicate<String> isLockedUser, Consumer<String> lockUser) {
		if(message.trim().isEmpty()) {
			return 0L;
		}
		String id = botToken + "_" + chatId;

		if(isLockedUser.test(id)) {
			return 0L;
		}
		String mensagem = message.replace("<p>", "\n").replace("</p>", " ").replace(",<br/>", " ");
		
		if(mensagem.length() > 4096) {
			mensagem = mensagem.substring(0, 4096);
		}
		
		String url = this.getBotToken(botToken)
				+ "/sendMessage";
		CcpHttpHandler ccpHttpHandler = new CcpHttpHandler(200, this.ccpHttp);
		CcpMapDecorator body = new CcpMapDecorator()
		.put("reply_to_message_id", replyTo)
		.put("parse_mode", "html")
		.put("chat_id", chatId)
		.put("text", mensagem);
		CcpMapDecorator response = ccpHttpHandler.executeHttpRequest(url, "POST", new CcpMapDecorator(), body, CcpHttpResponseType.singleRecord);

		boolean ok = response.getAsBoolean("ok");
		if(ok) {
			Long newMessageId = response.getInternalMap("result").getAsLongNumber("message_id");
			
			return newMessageId;
		}
		Integer errorCode = response.getAsIntegerNumber("error_code");
		boolean esteBotFoiBloqueado = Integer.valueOf(403).equals(errorCode);
		if(esteBotFoiBloqueado) {
			lockUser.accept(id);
		}
		return 0L;
		
	}

	@Override
	public String getFileName(String botToken, CcpMapDecorator messageData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String extractTextFromMessage(String botToken, CcpMapDecorator messageData) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
