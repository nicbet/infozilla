/**
 * PatchHunk.java
  * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.elements.patch;

public class PatchHunk {
	private String text;
	
	public PatchHunk() {
		text = "";
	}
	
	public PatchHunk(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
