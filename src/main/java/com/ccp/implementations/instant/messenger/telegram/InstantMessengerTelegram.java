package com.ccp.implementations.instant.messenger.telegram;

import com.ccp.constantes.CcpConstants;
import com.ccp.decorators.CcpMapDecorator;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInject;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.exceptions.http.CcpHttpError;
import com.ccp.exceptions.instant.messenger.ThisBotWasBlockedByThisUser;
import com.ccp.exceptions.instant.messenger.TooManyRequests;
import com.ccp.process.ThrowException;
enum X{
	instantMessenger
}
class InstantMessengerTelegram implements CcpInstantMessenger {
	
	private CcpMapDecorator properties;

	public InstantMessengerTelegram() {
		this.properties = new CcpStringDecorator("application.properties").propertiesFileFromClassLoader();
	}
	
	@CcpDependencyInject
	private CcpHttpRequester ccpHttp;
	
	@Override
	public Long getMembersCount(CcpMapDecorator parameters) {

		Long chatId = parameters.getAsLongNumber("chatId");
		String botTokenKey = parameters.getAsString("botTokenKey");
		String url = this.getBotToken(botTokenKey);
		this.ccpHttp.executeHttpRequest(url + "/getChatMemberCount?chat_id=" + chatId, "GET", CcpConstants.EMPTY_JSON, "");
		CcpHttpHandler ccpHttpHandler = new CcpHttpHandler(200, this.ccpHttp);
		try {
			CcpMapDecorator response = ccpHttpHandler.executeHttpSimplifiedGet(url, CcpHttpResponseType.singleRecord, X.instantMessenger);
			if(response.getAsBoolean("ok") == false) {
				throw new RuntimeException("Erro ao contar membros do grupo " + chatId);
			}
			Long result = response.getAsLongNumber("result");
			return result;
			
		} catch (CcpHttpError e) {
			throw new RuntimeException("Erro ao contar membros do grupo " + chatId + ". Detalhes: " + e.entity);
		}
		
		
	}

	private String getBotToken(String botTokenKey) {
		
		if(botTokenKey.trim().isEmpty()) {
			throw new RuntimeException("The parameter 'botTokenKey' is missing");
		}
		
		String botToken = this.properties.getAsString(botTokenKey);
		
		if(botToken.trim().isEmpty()) {
			throw new RuntimeException("The property '" + botTokenKey + "' is missing");
		}
		
		String url = "https://api.telegram.org/bot" + botToken;
		return url;
	}


	@Override
	public Long sendMessage(CcpMapDecorator parameters) {
	
		Long chatId = parameters.getAsLongNumber("chatId");
		
		String message = parameters.getAsString("message");
		Long replyTo = parameters.containsAllKeys("replyTo") ? parameters.getAsLongNumber("replyTo") : 0L;

		if(message.trim().isEmpty()) {
			return 0L;
		}

		String mensagem = message.replace("<p>", "\n").replace("</p>", " ").replace(",<br/>", " ");
		
		if(mensagem.length() > 4096) {
			mensagem = mensagem.substring(0, 4096);
		}
		String botTokenKey = parameters.getAsString("botTokenKey");

		String botToken = this.getBotToken(botTokenKey);
		String url = botToken + "/sendMessage";
		
		CcpMapDecorator handlers = new CcpMapDecorator()
				.put("403", new ThrowException(new ThisBotWasBlockedByThisUser()))
				.put("429", new ThrowException(new TooManyRequests()))
				.put("200", CcpConstants.DO_NOTHING)
				;
		
		CcpHttpHandler ccpHttpHandler = new CcpHttpHandler(handlers, this.ccpHttp);
		
		CcpMapDecorator body = new CcpMapDecorator()
		.put("reply_to_message_id", replyTo)
		.put("parse_mode", "html")
		.put("chat_id", chatId)
		.put("text", mensagem);
		CcpMapDecorator response;
		
		response = ccpHttpHandler.executeHttpRequest(url, "POST", new CcpMapDecorator(), body, CcpHttpResponseType.singleRecord, X.instantMessenger);

		boolean nOk = response.getAsBoolean("ok") == false;
		
		if(nOk) {
//			throw new CcpHttpError(response, url, method, expectedStatusList);
		}

		CcpMapDecorator result = response.getInternalMap("result");
		
		Long newMessageId = result.getAsLongNumber("message_id");
		
		return newMessageId;
	}


	@Override
	public String getFileName(CcpMapDecorator parameters) {
		
//		CcpMapDecorator messageData = parameters.getInternalMap("messageData");
//		String botToken = parameters.getAsString("botToken");
		
		return null;
	}

	@Override
	public String extractTextFromMessage(CcpMapDecorator parameters) {
//		CcpMapDecorator messageData = parameters.getInternalMap("messageData");
//		String botToken = parameters.getAsString("botToken");

		return null;
	}

	
}
