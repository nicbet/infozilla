/**
 * FilterSourceCodeJAVA.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.filters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.Ostermiller.util.CSVParser;

import io.kuy.infozilla.elements.sourcecode.java.CodeRegion;
import io.kuy.infozilla.helpers.RegExHelper;

/**
 * The FilterSourceCodeJAVA class implements the IFilter interface for
 * JAVA source code structural elements. 
 * @author Nicolas Bettenburg
 *
 */
public class FilterSourceCodeJAVA implements IFilter{
	
	/** Stores the codePatterns read from Java_CodeDB.txt */
	private HashMap<String, Pattern> codePatterns;
	
	/** Stores the code pattern options, read from Java_CodeDB.txt */
	private HashMap<String, String> codePatternOptions;
	
	/** The classes own textRemover */
	private FilterTextRemover textRemover;
	
	/**
	 * Standard Constructor
	 */
	public FilterSourceCodeJAVA() {
		codePatterns = new HashMap<String, Pattern>();
		codePatternOptions = new HashMap<String, String>();
	}
	
	/**
	 * Overloaded Constructor
	 * @param filename the name of the file to read Code Patterns from.
	 */
	public FilterSourceCodeJAVA(String filename) {
		codePatterns = new HashMap<String, Pattern>();
		codePatternOptions = new HashMap<String, String>();
		try {
			readCodePatterns(filename);
		} catch(Exception e) {
			System.err.println("Error while reading Java Source Code Patterns!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Overloaded Constructor
	 * @param fileurl a URL to a file to read Code Patterns from.
	 */
	public FilterSourceCodeJAVA(URL fileurl) {
		codePatterns = new HashMap<String, Pattern>();
		codePatternOptions = new HashMap<String, String>();
		try {
			readCodePatterns(fileurl.openStream());
		} catch(Exception e) {
			System.err.println("Error while reading Java Source Code Patterns!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Get a List of Source Code Regions contained in a given Text {@link s}
	 * @param s the Text we shall look inside for Source Code
	 * @return a List of Source Code Occurences as {@link CodeRegion}s
	 */
	private List<CodeRegion> getCodeRegions(final String s, boolean minimalSet) {
		List<CodeRegion> codeRegions = new ArrayList<CodeRegion>();
		// for each keyword-pattern pair find the corresponding occurences!
		for (String keyword : codePatterns.keySet()) {
			Pattern p = codePatterns.get(keyword);
			String patternOptions = codePatternOptions.get(keyword);
			if (patternOptions.contains("MATCH")) {
				for (MatchResult r : RegExHelper.findMatches(p, s)) {
					int offset = findMatch(s,'{', '}', r.end());
					CodeRegion foundRegion = new CodeRegion(r.start(),r.end() + offset, keyword, s.substring(r.start(), r.end() + offset));
					codeRegions.add(foundRegion);
				}
			}
			else {
				for (MatchResult r : RegExHelper.findMatches(p, s)) {
					CodeRegion foundRegion = new CodeRegion(r.start(),r.end(), keyword, r.group());
					codeRegions.add(foundRegion);
				}
			}
			
		}
		
		if (minimalSet)
			return makeMinimalSet(codeRegions);
		else 
			return codeRegions;
	}
	
	
	/**
	 * findMatch() returns the offset where the next closing is found. If not found return 0
	 */
	private int findMatch(String where, char opening, char closing, int start) {
		String region = where.substring(start);
		int level = 0;
		int position = 0;
		for (char c : region.toCharArray()) {
			position = position +1;
			if (c == opening) level=level+1;
			if (c == closing) {
				if (level == 0) { return position; }
				else {level = level -1;}
			}
		}
		return 0;
	}
	
	/**
	 * Read in some Code Patterns from a file named {@link filename}
	 * @param filename the full qualified filename from which to read the code patterns.
	 * @throws Exception if there did something go wrong with I/O
	 */
	private void readCodePatterns(final String filename) throws Exception {
		BufferedReader fileInput = new BufferedReader(new FileReader(filename));
		// Read patterns from the file
		String inputLine = null;
		while ( (inputLine = fileInput.readLine())  != null ) {
			// Input comes in the format: "keyword","PATTERN","OPTIONS"
			// A line can be commented out by using //
			if (!inputLine.substring(0, 2).equalsIgnoreCase("//")) {
				// we use ostermillers CSV Parser for sake of ease
				String[][] parsedLine = CSVParser.parse(inputLine);
				String keyword = parsedLine[0][0];
				String pattern = parsedLine[0][1];
				// Check if we have some options
				if (parsedLine[0].length == 3) {
					String options = parsedLine[0][2];
					codePatternOptions.put(keyword, options);
				} else {
					codePatternOptions.put(keyword, "");
				}
				Pattern somePattern = Pattern.compile(pattern);
				codePatterns.put(keyword, somePattern);
			}
		}
		fileInput.close();
	}
	
	/**
	 * Read in some Code Patterns from an input stream name {@link instream}
	 * @param instream the input stream to read the code patterns from
	 * @throws Exception if something goes wrong with I/O
	 */
	private void readCodePatterns(final InputStream instream) throws Exception {
		BufferedReader fileInput = new BufferedReader(new InputStreamReader(instream));
		// Read patterns from the file
		String inputLine = null;
		while ( (inputLine = fileInput.readLine())  != null ) {
			// Input comes in the format: "keyword","PATTERN","OPTIONS"
			// A line can be commented out by using //
			if (!inputLine.substring(0, 2).equalsIgnoreCase("//")) {
				// we use Ostermillers CSV Parser for sake of ease
				String[][] parsedLine = CSVParser.parse(inputLine);
				String keyword = parsedLine[0][0];
				String pattern = parsedLine[0][1];
				// Check if we have some options
				if (parsedLine[0].length == 3) {
					String options = parsedLine[0][2];
					codePatternOptions.put(keyword, options);
				} else {
					codePatternOptions.put(keyword, "");
				}
				Pattern somePattern = Pattern.compile(pattern);
				codePatterns.put(keyword, somePattern);
			}
		}
	}
	
	
	/**
	 * Given a List of Code Regions transform that list to a minimal including set
	 * @param regionList a List of Code Regions that should be minimized
	 * @return a minimal inclusion set of Code Regions.
	 */
	public static List<CodeRegion> makeMinimalSet(List<CodeRegion> regionList) {
		// Create a copy of the Code Region List
		List<CodeRegion> sortedRegionList = new ArrayList<CodeRegion>(regionList);
		// Sort it Ascending (by start position)
		java.util.Collections.sort(sortedRegionList);
		// This will hold the minimal set
		List<CodeRegion> minimalSet = new ArrayList<CodeRegion>();
		
		// For each Element, see if it is contained in any previous element
		for (int i=0; i < sortedRegionList.size(); i++ ) {
			CodeRegion thisRegion = sortedRegionList.get(i);
			boolean contained = false;
			for (int j=0; j < i; j++) {
				CodeRegion thatRegion = sortedRegionList.get(j);
				if (thatRegion.end >= thisRegion.end) {
					contained = true;
				}
			}
			if (! contained) {
				minimalSet.add(thisRegion);
			}
		}
		return minimalSet;
	}

	public String getOutputText() {
		return textRemover.doDelete();
	}

	public List<CodeRegion> runFilter(String inputText) {
		// Initialize a TextRemover
		textRemover = new FilterTextRemover(inputText);
		
		// Find all Code Regions in the given Text inputText - by default we want the minimal set
		// which means the outer most syntactical elements spanning all the discovered code.
		List<CodeRegion> codeRegions = getCodeRegions(inputText, true);
		
		// Mark the found Regions for deletion
		for (CodeRegion region : codeRegions)
			textRemover.markForDeletion(region.start, region.end);
		
		// Return the found source code regions
		return codeRegions;
	}
	
}

	