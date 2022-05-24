package com.ccp.implementations.instant.messages.telegram;

import com.ccp.constantes.CcpConstants;
import com.ccp.decorators.CcpMapDecorator;
import com.ccp.dependency.injection.CcpEspecification;
import com.ccp.dependency.injection.CcpImplementation;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.exceptions.db.UnexpectedHttpStatus;

@CcpImplementation
public class CcpInstantMessengerTelegram implements CcpInstantMessenger {
	@CcpEspecification
	private CcpHttpRequester ccpHttp;

	@Override
	public void sendMessageToSupport(String botToken, String message) {
		// TODO Auto-generated method stub
		
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
	public void sendSlowlyMessage(String botToken, String message, Long chatId, Long replyTo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessage(String botToken, String message, Long chatId, Long replyTo) {
		// TODO Auto-generated method stub
		
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
