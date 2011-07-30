package eu.loopit.f2011.util;

public class RestException extends RuntimeException {

	public RestException() {
		super();
	}

	public RestException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public RestException(String detailMessage) {
		super(detailMessage);
	}

	public RestException(Throwable throwable) {
		super(throwable);
	}

}
