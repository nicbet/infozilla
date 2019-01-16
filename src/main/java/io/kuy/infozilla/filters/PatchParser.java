/**
 * PatchParser.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.filters;

import java.util.ArrayList;
import java.util.List;

import io.kuy.infozilla.elements.patch.Patch;
import io.kuy.infozilla.elements.patch.PatchHunk;

public class PatchParser {
	// Change the following line if you need debug output 
	// Debug output is VERY verbose, so it's turned off by default!
	private final static boolean debug = false;
	
	/**
	 * Find the next line among the set of lines[] that looks like a Patch Index: line.
	 * @param lines	the lines in which to look for the patch start.
	 * @param start the starting line number from where to start the search.
	 * @return the linenumber where we found the next patch start from {@link start}.
	 */
	private int findNextIndex(String[] lines, int start) {
		int found = -1;
		for (int i=start; i < lines.length-1; i++) {
			// Find the next line that starts with "Index: "
			if (lines[i].startsWith("Index: ")) {
				// Check if the following line starts with "====="
				if (lines[i+1].startsWith("====")) {
					found = i;
					break;
				}
				
			}
		}
		return found;
	}
	
	/**
	 * Find the next Hunk start beginning from {@link start} in a set of {@link lines}.
	 * @param lines The lines to search for the next Hunk start.
	 * @param start The line from which to start looking for the next Hunk.
	 * @return The linenumber where the next Hunk start was found.
	 */
	private int findNextHunkHeader(String[] lines, int start) {
		int found = -1;
		for (int i=start; i < lines.length; i++) {
			// Find the next line that matches @@ -X,Y +XX,YY @@
			if (lines[i].matches("^@@\\s\\-\\d+,\\d+\\s\\+\\d+,\\d+\\s@@$")) {
				found = i;
				break;
			}
		}
		return found;
	}
	
	/**
	 * Splits a text into a List of possible Patches.
	 * @param text The text to split into patches.
	 * @return a List of possible Patches.
	 */
	private List<String> partitionByIndex(String text) {
		// This will be a list of all potential Patch Areas
		List<String> indexPartition = new ArrayList<String>();
		
		// Split the complete text into single lines to work with
		String[] lines = text.split("[\n\r]");
		
		// When we start we think there are more Patches inside ;)
		boolean hasMore = true;
		
		// We start at the very beginning of our text
		int idxStart = -1;
		
		// Find all areas
		while (hasMore) {
			idxStart = findNextIndex(lines, idxStart +1);
			if (idxStart == -1) {
				// if there is no next start we are done
				hasMore = false;
			} else {
				// otherwise see if there is another index 
				int idxEnd = findNextIndex(lines, idxStart +1);
				if (idxEnd == -1) {
					// add the whole range because there is no more next idx start
					String range = "";
					for (int i = idxStart; i < lines.length -1; i++) {
						range = range + lines[i] + System.getProperty("line.separator");
					}
					range = range + lines[lines.length-1];
					indexPartition.add(range);
				} else {
					// there is another index start so add the range to the partition
					String range = "";
					for (int i = idxStart; i < idxEnd; i++) {
						range = range + lines[i] + System.getProperty("line.separator");
					}
					indexPartition.add(range);
					
					// and set the new idxStart to end !
					idxStart = idxEnd -1;
				}
			}
		}
		
		return indexPartition;
	}
	
	/**
	 * Find the first line that starts with a given String
	 * @param text The text the line we look for starts with
	 * @param lines An Array of lines
	 * @param start The line number to start the search with
	 * @return The index of the first line starting at {@link start} or -1 if there is no such line
	 */
	private int findFirstLineBeginningWith(String text, String[] lines, int start) {
		int found = -1;
		for (int i=start; i < lines.length; i++) {
			if (lines[i].startsWith(text)) {
				found = i;
				break;
			}
		}
		return found;
	}
	
	
	/**
	 * Find the first line that starts with a given String
	 * @param text The text the line we look for starts with
	 * @param lines An Array of lines
	 * @param start The line number to start the search with
	 * @return The first line starting at {@link start} or an empty String if there is no such line
	 */
	private String findFirstLineBeginningWithS(String text, String[] lines, int start) {
		String found = "";
		for (int i=start; i < lines.length; i++) {
			if (lines[i].startsWith(text)) {
				found = lines[i];
				break;
			}
		}
		return found;
	}
	
	
	/**
	 * Checks whether the given line is a line that belongs to a hunk or not.
	 * @param line the line to check for being a Hunk Line.
	 * @return true if the {@link line} is a Hunk line, false otherwise.
	 */
	private boolean isHunkLine(String line) {
		boolean isHunkLine = ((line.startsWith("+")) || (line.startsWith("-")) || (line.startsWith(" ")));
		return isHunkLine;
	}
	
	/**
	 * Find and extract all Hunks in a Patch
	 * @param lines A set of Patch Lines
	 * @param start The line to start looking for Hunks
	 * @return a List<PatchHunk> of Hunks that were found
	 */
	private List<PatchHunk> findAllHunks(String[] lines, int start) {
		List<PatchHunk> foundHunks = new ArrayList<PatchHunk>();
		String lineSep = System.getProperty("line.separator");
		int hStart = start-1;
		boolean hasMore = true;
		while (hasMore) {
			hStart = findNextHunkHeader(lines, hStart+1);
			// Check if there are more Hunks
			if (hStart == -1) {
				// If there are no more Hunks then we are finished
				if (debug) System.out.println("<>>> No More Hunks found! Finished!");
				hasMore = false;
			} else {
				// If there are then look for the next Hunk start
				if (debug) System.out.println("<>>> Hunk Start is " + hStart);
				int nextHunkStart = findNextHunkHeader(lines, hStart + 1);
				int searchEnd = 0;
				if (nextHunkStart == -1) {
					if (debug) System.out.println("<>>> There are no more Hunks!");
					// If there is no next Hunk we can process until the end
					searchEnd = lines.length;
					hasMore = false;
				} else {
					if (debug) System.out.println("<>>> There are more Hunks left!");
					// Otherwise we will look only until the next Hunk beginning
					searchEnd = nextHunkStart -1;
				}
					if (debug) System.out.println("<>>> Will look for HunkLines from " + (hStart+1) + " to " + (searchEnd-1));
					String hunktext = "";
					for (int i = hStart +1; i < searchEnd; i++) {
						if (debug) System.out.println("<>>> Checking if Hunkline: " + lines[i]);
						if (isHunkLine(lines[i])) { 
							if (debug) System.out.println("<>>> Yes it is!");
							hunktext = hunktext + lines[i] + lineSep;
						} else {
							if (i < searchEnd -1) {
								if (isHunkLine(lines[i+1])) {
									if (debug) System.out.println("<>>> No But next line is!");
									hunktext = hunktext + lines[i] + lineSep;
								}
								else {
									// we are done
									if (debug) System.out.println("<>>> No it is not and niether is the next one! We should stop here!");
									searchEnd = i;
								}
							}
						}
					}
					// Kill last newline
					if (hunktext.length() > 1)
						hunktext = hunktext.substring(0, hunktext.length()-1);
					foundHunks.add(new PatchHunk(hunktext));	
					hStart = nextHunkStart -1;
				}
			}
		return foundHunks;
	}
	
	/**
	 * Parses a given text for all Patches inside using a 2 line lookahead Fuzzy Parser approach.
	 * @param text The text to extract Patches from.
	 * @return a list of {@link foundPatches}.
	 */
	public List<Patch> parseForPatches(String text) {
		// Start with an empty list of Patches
		List<Patch> foundPatches = new ArrayList<Patch>();

		// First Partition the whole given text into sections starting with Index:
		// The parts of the partition mark on potential patch
		List<String> indexPartition = partitionByIndex(text);
		
		// For each potential patch area split into header and a list of potential hunks
		for (String potentialPatch : indexPartition) {
			String[] lines = potentialPatch.split("[\n\r]");
			
			Patch patch = new Patch();
			// Gather Header Information of the Patch
			String pIndex = findFirstLineBeginningWithS("Index: ", lines, 0);
			patch.setIndex(pIndex);
			String pOrig  = findFirstLineBeginningWithS("--- ", lines, 0);
			patch.setOriginalFile(pOrig);
			String pModi  = findFirstLineBeginningWithS("+++ ", lines, 0);
			patch.setModifiedFile(pModi);
			
			// Find the first Hunk Header
			int    pModiNum = findFirstLineBeginningWith("+++ ", lines, 0);
			int firstHunkLine = findNextHunkHeader(lines, pModiNum + 1);
			
			// If there is no Hunk then the patch is invalid!
			if (firstHunkLine == -1)
				break;
			
			// Now we can add the complete Header
			String header = "";
			for (int i=0; i < firstHunkLine-1; i++) {
				header = header + lines[i] + System.getProperty("line.separator");
			}
			header = header + lines[firstHunkLine-1];
			patch.setHeader(header);
			
			// Discover all Hunks!
			List<PatchHunk> hunks = findAllHunks(lines, firstHunkLine);
			
			// And add the Hunks to the List of Hunks for this patch
			for (PatchHunk h : hunks) patch.addHunk(h);
			foundPatches.add(patch);
		}
		
		// Locate the Patches in the Source Code
		for (Patch p : foundPatches) {
			int patchStart = text.indexOf(p.getHeader());
			
			int patchEnd = text.lastIndexOf(p.getHunks().get(p.getHunks().size()-1).getText())
							+ p.getHunks().get(p.getHunks().size()-1).getText().length();
			
			p.setStartPosition(patchStart);
			p.setEndPosition(patchEnd);
		}
		
		// Here is the patch we found
		return foundPatches;
	}
}
