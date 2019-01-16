/**
 * StackTrace.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.elements.stacktrace.java;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a Stack Trace (in Java 1.4, 1.5, 1.6)
 * @author Nicolas Bettenburg
 */
public class StackTrace {

	private String exception;
	private String reason;
	private List<String> frames;
	private boolean isCause;
	private int traceStart;
	private int traceEnd;
	
	public int getTraceStart() {
		return traceStart;
	}

	public void setTraceStart(int traceStart) {
		this.traceStart = traceStart;
	}

	public int getTraceEnd() {
		return traceEnd;
	}

	public void setTraceEnd(int traceEnd) {
		this.traceEnd = traceEnd;
	}

	/**
	 * Default Constructor - sets attributes to predefined values.
	 */
	public StackTrace() {
		this.traceStart = 0;
		this.traceEnd = 0;
		this.exception = "Not specified";
		this.reason = "No reason given";
		this.frames = new ArrayList<String>();
		this.isCause = false;
	}
	
	/**
	 * Overloaded Constructor
	 * @param exception The Exception resulting in this Stack Trace.
	 * @param reason The Reason given in the Exception.
	 * @param frames The list of locations the trace originated from.
	 */
	public StackTrace(String exception, String reason, List<String> frames) {
		this.traceStart = 0;
		this.traceEnd = 0;
		this.exception = exception;
		this.reason = reason;
		this.frames = frames;
		this.isCause = false;
	}
	
	
	/**
	 * Overloaded Constructor
	 * @param exception The Reason given in the Exception.
	 * @param reason The list of locations the trace originated from.
	 */
	public StackTrace(String exception, String reason) {
		this.traceStart = 0;
		this.traceEnd = 0;
		this.exception = exception;
		this.reason = reason;
		this.frames = new ArrayList<String>();
		this.isCause = false;
	}

	/**
	 * Getter for the Exception
	 * @return the exception this Stack Trace originated from
	 */
	public String getException() {
		return exception;
	}

	/**
	 * Getter for the Reason
	 * @return the reason for this Stack Trace stated in the first Exception. (removed trailing :)
	 */
	public String getReason() {
		if (reason.startsWith(": ") || reason.startsWith(" :"))
			return reason.substring(2);
		else
			return reason;
	}

	/**
	 * Getter for the Trace Locations
	 * @return A list of locations where the trace originated from.
	 */
	public List<String> getFrames() {
		return(frames);
	}
	
	/**
	 * Set the Stack Trace to be a cause or not
	 * @param isCause A boolean value if this stack trace is a cause
	 */
	public void setCause(boolean isCause) {
		this.isCause = isCause;
	}
	
	/**
	 * Check whether this Stack Trace is a cause or not
	 * @return A boolean value indicating if the Stack Trace is a cause.
	 */
	public boolean isCause() {
		return isCause;
	}
	
	/**
	 * Joins the text of all Frames in frames
	 * @return a single String with all frames concattenated
	 */
	public String getFramesText() {
		String framesText = "";
		for (String frame : frames) {
			framesText = framesText + "at " + frame + System.getProperty("line.separator");
		}
		return framesText;
	}
}
