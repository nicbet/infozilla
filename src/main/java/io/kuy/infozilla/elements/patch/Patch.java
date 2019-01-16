/**
 * Patch.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.elements.patch;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import io.kuy.infozilla.helpers.RegExHelper;

public class Patch {
	private String index = "";
	private String originalFile = "";
	private String modifiedFile = "";
	private String header = "";
	private int startPosition;
	private int endPosition;
	
	private List<PatchHunk> hunks;
	
	public Patch() {
		hunks = new ArrayList<PatchHunk>();
		startPosition = 0;
		endPosition = 0;
	}
	
	public Patch(int s, int e) {
		hunks = new ArrayList<PatchHunk>();
		startPosition = s;
		endPosition = e;
	}
	
	public Patch(int s) {
		hunks = new ArrayList<PatchHunk>();
		startPosition = s;
	}
	
	public void addHunk(PatchHunk hunk) {
		hunks.add(hunk);
	}

	public String getIndex() {
		if (index.length() > 7)
			return (index.substring(7, index.length()));
		else
			return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String PlusMinusLineToFilename(String input) {
		String temp = input;
		String pmreg = "([-]{3}|[+]{3})([ \\r\\n\\t](.*?)[ \\t])";
		for (MatchResult r : RegExHelper.findMatches(Pattern.compile(pmreg, Pattern.MULTILINE ), input)) {
			if (r.groupCount() > 1)
				temp = r.group(2).trim();
		}
		return temp;
	}
	
	public String getOriginalFile() {
		return PlusMinusLineToFilename(originalFile);
	}

	public void setOriginalFile(String originalFile) {
		this.originalFile = originalFile;
	}

	public String getModifiedFile() {
		return PlusMinusLineToFilename(modifiedFile);
	}

	public void setModifiedFile(String modifiedFile) {
		this.modifiedFile = modifiedFile;
	}

	public List<PatchHunk> getHunks() {
		return hunks;
	}
	
	@Override
	public String toString() {
		String s = "";
		String lineSep = System.getProperty("line.separator");
		s = s + index + lineSep;
		s = s + "ORIGINAL=" + getOriginalFile() + lineSep;
		s = s + "MODIFIED=" + getModifiedFile() + lineSep;
		s = s + "#HUNKS=" + Integer.valueOf(hunks.size()) + lineSep;
		return s;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}

	public int getEndPosition() {
		return endPosition;
	}

	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}
	
	
}
