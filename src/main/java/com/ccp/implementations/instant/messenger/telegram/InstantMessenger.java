package com.ccp.implementations.instant.messenger.telegram;

import com.ccp.dependency.injection.CcpInstanceProvider;;

public class InstantMessenger implements CcpInstanceProvider {

	@Override
	public Object getInstance() {
		return new InstantMessengerTelegram();
	}

}
