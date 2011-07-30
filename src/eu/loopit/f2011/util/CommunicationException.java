package eu.loopit.f2011.util;

public class CommunicationException extends RuntimeException {

	public CommunicationException() {
	}

	public CommunicationException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public CommunicationException(String detailMessage) {
		super(detailMessage);
	}

	public CommunicationException(Throwable throwable) {
		super(throwable);
	}

}
