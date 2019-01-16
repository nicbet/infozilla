/**
 * TalkbackTrace.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.elements.stacktrace.talkback;

import java.util.ArrayList;
import java.util.List;

/**
 * A Mozilla TALKBACK� Stack Trace. Warning: the Talkback format was reverse engineered
 * and might not fully comply with the full specifications!
 * @author Nicolas Bettenburg
 *
 */
public class TalkbackTrace {
	private List<TalkbackEntry> entries;

	public TalkbackTrace(List<TalkbackEntry> entries) {
		super();
		this.entries = entries;
	}
	
	public TalkbackTrace() {
		super();
		this.entries = new ArrayList<TalkbackEntry>();
	}

	public List<TalkbackEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<TalkbackEntry> entries) {
		this.entries = entries;
	}
	
	public String toString() {
		if (entries != null) {
			StringBuilder sb = new StringBuilder();
			for (TalkbackEntry entry : entries ) {
				sb.append(entry.toString() + System.getProperty("line.separator"));
			}
			return (sb.toString());
		} else {
			return (this.getClass().getName() + " " + this.hashCode());
		}
	}
}
