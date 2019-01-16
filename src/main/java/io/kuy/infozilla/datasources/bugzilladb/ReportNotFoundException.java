/**
 * ReportNotFoundException.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.datasources.bugzilladb;

public class ReportNotFoundException extends Exception {

	/**
	 * Automatic generated serialization id
	 */
	private static final long serialVersionUID = 8766823313226301540L;

	public ReportNotFoundException() {
	}

	public ReportNotFoundException(String message) {
		super(message);
	}

	public ReportNotFoundException(Throwable cause) {
		super(cause);
	}

	public ReportNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
