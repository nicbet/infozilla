/**
 * FilterTextRemover.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.filters;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>FilterTextRemover</code> class is used to cut out the structural element
 * from the complete input text. Given a list of (possibly overlapping) cut points,
 * the class calculates the longest consecutive parts and marks them for deletion
 * through a character-wise bit mask.
 * @author Nicolas Bettenburg.
 *
 */
public class FilterTextRemover {
	
	public class TextCutPoint {
		private int start;
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
		private int end;
		public TextCutPoint(int start, int end) {
			super();
			this.start = start;
			this.end = end;
		}
	}
	
	private String originalText = "";
	private boolean[] deletionMask;
	private List<TextCutPoint> cutPoints;
	
	public FilterTextRemover(String originalText) {
		// Set the Text
		this.originalText = originalText;
		
		// Create a deletion mask of appropriate size
		deletionMask = new boolean[originalText.length()];
		
		// Initialize the Deletion Mask with false (=do not delete)
		for (int i=0; i < deletionMask.length; i++) {
			deletionMask[i] = false;
		}
		
		cutPoints = new ArrayList<TextCutPoint>();
	}
	
	public void markForDeletion(int start, int end) {
		cutPoints.add(new TextCutPoint(start, end));
		if (start >= 0 && end <= deletionMask.length) {
			for (int i = start; i < end; i++) {
				deletionMask[i] = true;
			}
		} else {
			System.err.println("Warning! Trying to Delete out of Bounds: " + start + " until " + end + " but bounds are 0:"  + deletionMask.length);
			System.err.println("Will not mark for deletion!");
		}
	}
	
	
	public String doDelete() {
		StringBuilder myStringBuilder = new StringBuilder();
		
		for (int i = 0; i < originalText.length(); i++) {
			if (!deletionMask[i])
				myStringBuilder.append(originalText.charAt(i));
		}
		return myStringBuilder.toString();
	}
	
	public String getText() {
		return originalText;
	}
	
}
