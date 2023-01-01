package com.ccp.implementations.instant.messages.telegram;

import com.ccp.constantes.CcpConstants;
import com.ccp.decorators.CcpMapDecorator;
import com.ccp.dependency.injection.CcpDependencyInject;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.exceptions.http.UnexpectedHttpStatus;
import com.ccp.process.ThrowException;

class InstantMessengerTelegram implements CcpInstantMessenger {
	
	@CcpDependencyInject
	private CcpHttpRequester ccpHttp;
	
	@Override
	public Long getMembersCount(String botToken, Long chatId) {
		String url = this.getBotToken(botToken);
		this.ccpHttp.executeHttpRequest(url + "/getChatMemberCount?chat_id=" + chatId, "GET", CcpConstants.EMPTY_JSON, "");
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
	public Long sendMessage(String botToken, String message, Long chatId, Long replyTo) {
		if(message.trim().isEmpty()) {
			return 0L;
		}

		String mensagem = message.replace("<p>", "\n").replace("</p>", " ").replace(",<br/>", " ");
		
		if(mensagem.length() > 4096) {
			mensagem = mensagem.substring(0, 4096);
		}
		
		String url = this.getBotToken(botToken)	+ "/sendMessage";
		
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
		
		try {
			response = ccpHttpHandler.executeHttpRequest(url, "POST", new CcpMapDecorator(), body, CcpHttpResponseType.singleRecord);

			boolean nOk = response.getAsBoolean("ok") == false;
			
			if(nOk) {
				throw new InstantMessageApiIsUnavailable();
			}

			CcpMapDecorator result = response.getInternalMap("result");
			
			Long newMessageId = result.getAsLongNumber("message_id");
			
			return newMessageId;
		} catch (UnexpectedHttpStatus e) {
			throw new InstantMessageApiIsUnavailable();
		}
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
