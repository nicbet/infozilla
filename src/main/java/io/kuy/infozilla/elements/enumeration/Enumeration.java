/**
 * Enumeration.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.elements.enumeration;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates an Enumeration as created by FilterEnumerations filter.
 * Although we are using this for now to discover "Steps to Reproduce", a user should
 * probably subclass from Enumeration and add custom heuristics.
 * @author Nicolas Bettenburg
 *
 */
public class Enumeration {
	
	private List<String> enumeration_items;
	private int start;
	private int end;
	private int enumStart;
	private int enumEnd;
	
	public int getEnumStart() {
		return enumStart;
	}

	public void setEnumStart(int enumStart) {
		this.enumStart = enumStart;
	}

	public int getEnumEnd() {
		return enumEnd;
	}

	public void setEnumEnd(int enumEnd) {
		this.enumEnd = enumEnd;
	}

	public Enumeration( List<String> items, int start, int end) {
		this.start = start;
		this.end = end;
		this.enumeration_items = items;
	}
	
	public Enumeration() {
		this.enumeration_items = new ArrayList<String>();
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public List<String> getEnumeration_items() {
		return enumeration_items;
	}

	public void setEnumeration_items(List<String> enumeration_items) {
		this.enumeration_items = enumeration_items;
	}
	
}
