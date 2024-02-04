package com.ccp.implementations.instant.messenger.telegram;

import com.ccp.dependency.injection.CcpInstanceProvider;;

public class CcpTelegramInstantMessenger implements CcpInstanceProvider {

	
	public Object getInstance() {
		return new TelegramInstantMessenger();
	}

}
