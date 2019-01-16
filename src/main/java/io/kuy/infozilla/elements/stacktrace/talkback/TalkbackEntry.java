/**
 * TalkbackEntry.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.elements.stacktrace.talkback;

public class TalkbackEntry {
	// Types definitions
	public final static int CLASSMETHODLINE = 1;
	public final static int METHODCALLLINE = 2;
	public final static int METHODLINE = 3;
	public final static int LIBRARYLINE = 4;
	public final static int ADDRESSLINE = 5;

	// The name
	private String name;

	// The location
	private String location;

	// The type
	private int type = 0;

	public TalkbackEntry(String name, String location, int type) {
		super();
		this.name = name;
		this.location = location;
		this.type = type;
	}

	public String getName() {
		return name.trim();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location.trim();
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public String toString() {
		return (name.trim() + " (" + location.trim() + ")");
	}
}
