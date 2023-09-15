package com.ccp.implementations.instant.messenger.telegram;

import java.util.ArrayList;
import java.util.List;

import com.ccp.constantes.CcpConstants;
import com.ccp.decorators.CcpMapDecorator;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.exceptions.commons.ThrowException;
import com.ccp.exceptions.http.CcpHttpError;
import com.ccp.exceptions.instant.messenger.ThisBotWasBlockedByThisUser;
import com.ccp.exceptions.instant.messenger.TooManyRequests;

class InstantMessengerTelegram implements CcpInstantMessenger {
	
	private CcpMapDecorator properties;

	public InstantMessengerTelegram() {
		this.properties = new CcpStringDecorator("application.properties").propertiesFileFromFile();
	}
	
	@Override
	public Long getMembersCount(CcpMapDecorator parameters) {
		CcpHttpRequester ccpHttp = CcpDependencyInjection.getDependency(CcpHttpRequester.class);

		Long chatId = parameters.getAsLongNumber("chatId");
		String url = this.getCompleteUrl(parameters);
		ccpHttp.executeHttpRequest(url + "/getChatMemberCount?chat_id=" + chatId, "GET", CcpConstants.EMPTY_JSON, "", 200);
		CcpHttpHandler ccpHttpHandler = new CcpHttpHandler(200);
		try {
			CcpMapDecorator response = ccpHttpHandler.executeHttpSimplifiedGet(url, CcpHttpResponseType.singleRecord);
			if(response.getAsBoolean("ok") == false) {
				throw new RuntimeException("Erro ao contar membros do grupo " + chatId);
			}
			Long result = response.getAsLongNumber("result");
			return result;
			
		} catch (CcpHttpError e) {
			throw new RuntimeException("Erro ao contar membros do grupo " + chatId + ". Detalhes: " + e.getMessage());
		}
	}
	void throwThisBotWasBlockedByThisUser(String token) {
		throw new ThisBotWasBlockedByThisUser(token);
	}
	
	void throwTooManyRequests() {
		throw new TooManyRequests();
	}
	public CcpMapDecorator sendMessage(CcpMapDecorator parameters) {
		String token = this.getToken(parameters);

//		this.throwTooManyRequests();
//		this.throwThisBotWasBlockedByThisUser(token);
		Long chatId = parameters.getAsLongNumber("recipient");
		
		String message = parameters.getAsString("message");
		Long replyTo = parameters.containsAllKeys("replyTo") ? parameters.getAsLongNumber("replyTo") : 0L;

		if(message.trim().isEmpty()) {
			return new CcpMapDecorator();
		}

		String mensagem = message.replace("<p>", "\n").replace("</p>", " ").replace(",<br/>", " ");
		List<String> texts = new ArrayList<>();
		int length = mensagem.length();
		int pieces = length / 4096;
		
		for(int k = 0; k <= pieces; k++) {
			int nextBound = (k + 1) * 4096;
			int currentBound = k * 4096;
			String text = mensagem.substring(currentBound, nextBound > length ? length : nextBound);
			texts.add(text);
		}
		String botUrl = this.getCompleteUrl(parameters);
		String url = botUrl + "/sendMessage";
		String method = parameters.getAsString("method");
		
		CcpMapDecorator handlers = new CcpMapDecorator()
				.put("403", new ThrowException(new ThisBotWasBlockedByThisUser(token)))
				.put("429", new ThrowException(new TooManyRequests()))
				.put("200", CcpConstants.DO_NOTHING)
				;
		
		CcpHttpHandler ccpHttpHandler = new CcpHttpHandler(handlers);
		
		for (String text : texts) {
			CcpMapDecorator body = new CcpMapDecorator()
					.put("reply_to_message_id", replyTo)
					.put("parse_mode", "html")
					.put("chat_id", chatId)
					.put("text", text);
			
			CcpMapDecorator response = ccpHttpHandler.executeHttpRequest(url, method, new CcpMapDecorator(), body, CcpHttpResponseType.singleRecord);
			
			CcpMapDecorator result = response.getInternalMap("result");
			
			replyTo = result.getAsLongNumber("message_id");
		}
		
		return new CcpMapDecorator().put("token", token);
	}


	private String getCompleteUrl(CcpMapDecorator parameters) {
		
		String tokenValue = this.getToken(parameters);
		
		String urlKey = parameters.getAsString("url");
		String urlValue = this.properties.getAsString(urlKey);
		
		return urlValue + tokenValue;
	}

	public String getToken(CcpMapDecorator parameters) {
		String tokenKey = parameters.getAsString("token");
		String tokenValue = this.properties.getAsString(tokenKey);
		return tokenValue;
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
