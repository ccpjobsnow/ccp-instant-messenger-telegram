package com.ccp.implementations.instant.messages.telegram;

import com.ccp.dependency.injection.CcpEspecification.DefaultImplementationProvider;

public class ImplementationProvider extends DefaultImplementationProvider {

	@Override
	public Object getImplementation() {
		return new InstantMessengerTelegram();
	}

}
