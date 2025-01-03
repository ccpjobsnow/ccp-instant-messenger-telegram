package com.ccp.implementations.instant.messenger.telegram;

import java.util.ArrayList;
import java.util.List;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.exceptions.http.CcpHttpError;
import com.ccp.exceptions.instant.messenger.CcpThisBotWasBlockedByThisUser;
import com.ccp.exceptions.instant.messenger.CcpTooManyRequests;
import com.ccp.exceptions.process.CcpThrowException;

class TelegramInstantMessenger implements CcpInstantMessenger {
	
	public Long getMembersCount(CcpJsonRepresentation parameters) {
		CcpHttpRequester ccpHttp = CcpDependencyInjection.getDependency(CcpHttpRequester.class);

		Long chatId = parameters.getAsLongNumber("chatId");
		String url = this.getCompleteUrl(parameters);
		ccpHttp.executeHttpRequest(url + "/getChatMemberCount?chat_id=" + chatId, "GET", CcpOtherConstants.EMPTY_JSON, "", 200);
		CcpHttpHandler ccpHttpHandler = new CcpHttpHandler(200);
		try {
			CcpJsonRepresentation response = ccpHttpHandler.executeHttpSimplifiedGet("getMembersCount", url, CcpHttpResponseType.singleRecord);
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
		throw new CcpThisBotWasBlockedByThisUser(token);
	}
	
	void throwTooManyRequests() {
		throw new CcpTooManyRequests();
	}
	public CcpJsonRepresentation sendMessage(CcpJsonRepresentation parameters) {
		String token = this.getToken(parameters);

//		this.throwTooManyRequests();
//		this.throwThisBotWasBlockedByThisUser(token);
		Long chatId = parameters.getAsLongNumber("recipient");
		
		String message = parameters.getAsString("message");
		Long replyTo = parameters.containsAllFields("replyTo") ? parameters.getAsLongNumber("replyTo") : 0L;

		if(message.trim().isEmpty()) {
			return CcpOtherConstants.EMPTY_JSON;
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
		
		CcpJsonRepresentation handlers = CcpOtherConstants.EMPTY_JSON
				.addJsonTransformer("403", new CcpThrowException(new CcpThisBotWasBlockedByThisUser(token)))
				.addJsonTransformer("429", new CcpThrowException(new CcpTooManyRequests()))
				.addJsonTransformer("200", CcpOtherConstants.DO_NOTHING)
				;
		
		CcpHttpHandler ccpHttpHandler = new CcpHttpHandler(handlers);
		
		for (String text : texts) {
			CcpJsonRepresentation body = CcpOtherConstants.EMPTY_JSON
					.put("reply_to_message_id", replyTo)
					.put("parse_mode", "html")
					.put("chat_id", chatId)
					.put("text", text);
			
			CcpJsonRepresentation response = ccpHttpHandler.executeHttpRequest("sendInstantMessage", url, method, CcpOtherConstants.EMPTY_JSON, body, CcpHttpResponseType.singleRecord);
			
			CcpJsonRepresentation result = response.getInnerJson("result");
			
			replyTo = result.getAsLongNumber("message_id");
		}
		
		return CcpOtherConstants.EMPTY_JSON.put("token", token);
	}


	private String getCompleteUrl(CcpJsonRepresentation parameters) {
		CcpJsonRepresentation properties = new CcpStringDecorator("application_properties").propertiesFrom().environmentVariablesOrClassLoaderOrFile();	
		String tokenValue = this.getToken(parameters);
		
		String urlKey = parameters.getAsString("url");
		String urlValue = properties.getAsString(urlKey);
		
		return urlValue + tokenValue;
	}

	public String getToken(CcpJsonRepresentation parameters) {
		CcpJsonRepresentation properties = new CcpStringDecorator("application_properties").propertiesFrom().environmentVariablesOrClassLoaderOrFile();	
		String tokenKey = parameters.getAsString("token");
		String tokenValue = properties.getAsString(tokenKey);
		if(tokenValue.trim().isEmpty()) {
			return tokenKey;
		}
		return tokenValue;
	}


	
	public String getFileName(CcpJsonRepresentation parameters) {
		
//		CcpMapDecorator messageData = parameters.getInternalMap("messageData");
//		String botToken = parameters.getAsString("botToken");
		
		return "";
	}

	
	public String extractTextFromMessage(CcpJsonRepresentation parameters) {
//		CcpMapDecorator messageData = parameters.getInternalMap("messageData");
//		String botToken = parameters.getAsString("botToken");

		return "";
	}

	
}
