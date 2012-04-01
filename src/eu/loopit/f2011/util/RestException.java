package eu.loopit.f2011.util;

public class RestException extends RuntimeException {
	
	private int statusCode;

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
	
	public RestException(int statusCode) {
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}
}
