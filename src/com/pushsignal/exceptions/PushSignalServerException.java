package com.pushsignal.exceptions;

import com.pushsignal.xml.simple.ErrorResultDTO;

public class PushSignalServerException extends Exception {
	private static final long serialVersionUID = 1L;

	public PushSignalServerException(final ErrorResultDTO errorResult) {
		super(errorResult.getDescription());
	}

	public PushSignalServerException(final String errorDescription) {
		super(errorDescription);
	}
}
