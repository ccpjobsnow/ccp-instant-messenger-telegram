package com.ccp.implementations.instant.messages.telegram;

import com.ccp.dependency.injection.CcpImplementationProvider;

public class ImplementationProvider implements CcpImplementationProvider {

	@Override
	public Object getImplementation() {
		return new InstantMessengerTelegram();
	}

}
