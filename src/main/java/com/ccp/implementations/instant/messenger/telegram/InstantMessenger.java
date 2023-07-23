package com.ccp.implementations.instant.messenger.telegram;

import com.ccp.dependency.injection.CcpModuleExporter;;

public class InstantMessenger implements CcpModuleExporter {

	@Override
	public Object export() {
		return new InstantMessengerTelegram();
	}

}
