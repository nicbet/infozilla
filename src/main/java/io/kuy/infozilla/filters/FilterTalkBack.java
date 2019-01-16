/**
 * FilterTalkBack.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.kuy.infozilla.elements.stacktrace.talkback.TalkbackEntry;
import io.kuy.infozilla.elements.stacktrace.talkback.TalkbackTrace;
import io.kuy.infozilla.helpers.RegExHelper;

public class FilterTalkBack implements IFilter {

	private FilterTextRemover textRemover;
	
	public String getOutputText() {
		return textRemover.doDelete();
	}

	public List<TalkbackTrace> runFilter(String inputText) {
		textRemover = new FilterTextRemover(inputText);
		
		List<TalkbackTrace> foundTraces = new ArrayList<TalkbackTrace>();
		List<String> talkbackLines = new ArrayList<String>();
		
		/* This regular expression can be used to filter TalkBack expressions
		 * (
			([ \\n\\r]*(?:.*)(?:::)(?:.*)[ \\n\\r]*\\[.*?,?[ \\n\\r]*line[ \\n\\r]*[0-9]+\\])
			(?:[ \\n\\r]*.*[ \\n\\r]*\\[.*?,?[ \\n\\r]*line[ \\n\\r]*[0-9]+\\])
			(?:[ \\n\\r]*.*?\\(\\))
			(?:[ \\n\\r]*.*?\\+[ \\n\\r]*[0-9]x[0-9a-zA-Z]+[ \\n\\r]*\\([0-9]x[0-9a-zA-Z]+\\))
			(?:[ \\n\\r]*[0-9]x[0-9a-zA-Z]+)
		   ){2,}
		 */
		
		String classmethodline = "([ \\n\\r]*(?:.*)(?:::)(?:.*)[ \\n\\r]*\\[.*?,?[ \\n\\r]*line[ \\n\\r]*[0-9]+\\])";
		String methodline = "(?:[ \\n\\r]*.*[ \\n\\r]*\\[.*?,?[ \\n\\r]*line[ \\n\\r]*[0-9]+\\])";
		String methodcallline = "([ \\n\\r]*[^ ]*?\\(\\)[ ]*[\\n\\r])";
		String libraryline = "(?:[ \\n\\r]*.*?\\+[ \\n\\r]*[0-9]x[0-9a-zA-Z]+[ \\n\\r]*\\([0-9]x[0-9a-zA-Z]+\\))";
		String addressline = "(?:[ \\n\\r]*[0-9]x[0-9a-zA-Z]+)";

		String trace = "^(" + classmethodline + "|" + methodcallline + "|" + methodline + "|" + libraryline + "|" + addressline + "){2,}";
		
		// Compile the patterns for reuse
		Pattern p_cml = Pattern.compile("^(" + classmethodline + ")", Pattern.MULTILINE);
		Pattern p_mcl = Pattern.compile("^(" + methodcallline + ")", Pattern.MULTILINE);
		Pattern p_ml = Pattern.compile("^(" + methodline + ")", Pattern.MULTILINE);
		Pattern p_ll = Pattern.compile("^(" + libraryline + ")", Pattern.MULTILINE);
		Pattern p_al = Pattern.compile("^(" + addressline + ")", Pattern.MULTILINE);
		Pattern ptl1 = Pattern.compile(trace, Pattern.MULTILINE);
		
		// Find all talkback lines
		for (MatchResult r : RegExHelper.findMatches(ptl1, inputText)) {
			talkbackLines.add(r.group().trim());
			textRemover.markForDeletion(r.start(), r.end());
		}
		
		// From each set of talkback lines create a talkback trace
		for (String line : talkbackLines){
			String tmp = line;
			boolean hasMore = true;
			Matcher m = null;
			
			List<TalkbackEntry> entries = new ArrayList<TalkbackEntry>();
			while (hasMore) {
				// We assume there are no more talkback lines. If there are matches this will be set to true.
				hasMore = false;
				
				// Check line for class method line
				m = p_cml.matcher(tmp);
				if (m.find())
					if (m.start() == 0) {
						// Format the line
						String tbline = m.group().replaceAll("[\\n\\r]", "").trim();
						// Split into name and location
						String[] info = tbline.split("\\[");
						TalkbackEntry anEntry = new TalkbackEntry(info[0],info[1].replaceAll("\\]",""),TalkbackEntry.CLASSMETHODLINE);
						
						// Add the entry to the talkback entries list
						entries.add(anEntry);
						
						// Remove this talback entry line from the string that is to be processed
						tmp = tmp.substring(m.end());
	
						// Continue looking for further lines
						hasMore = true;
						continue;
					}
				// Check line for address line
				m = p_al.matcher(tmp);
				if (m.find())
					if (m.start() == 0) {
						// Format the line
						String tbline = m.group().replaceAll("[\\n\\r]", "").trim();
			
						TalkbackEntry anEntry = new TalkbackEntry(tbline, tbline, TalkbackEntry.ADDRESSLINE);
						
						// Add the entry to the talkback entries list
						entries.add(anEntry);
						
						// Remove this talback entry line from the string that is to be processed
						tmp = tmp.substring(m.end());
	
						// Continue looking for further lines
						hasMore = true;
						continue;
					}
				// Check line for method call line
				m = p_mcl.matcher(tmp);
				if (m.find())
					if (m.start() == 0) {
						// Format the line
						String tbline = m.group().replaceAll("[\\n\\r]", "").trim();
						
						TalkbackEntry anEntry = new TalkbackEntry(tbline,"",TalkbackEntry.METHODCALLLINE);
						
						// Add the entry to the talkback entries list
						entries.add(anEntry);
						
						// Remove this talback entry line from the string that is to be processed
						tmp = tmp.substring(m.end());
	
						// Continue looking for further lines
						hasMore = true;
						continue;
					}
				// Check line for method line
				m = p_ml.matcher(tmp);
				if (m.find())
					if (m.start() == 0) {
						// Format the line
						String tbline = m.group().replaceAll("[\\n\\r]", "").trim();
						
						// Split into name and location
						String[] info = tbline.split("\\[");
						TalkbackEntry anEntry = new TalkbackEntry(info[0],info[1].replaceAll("\\]",""),TalkbackEntry.METHODLINE);
						
						// Add the entry to the talkback entries list
						entries.add(anEntry);
						
						// Remove this talback entry line from the string that is to be processed
						tmp = tmp.substring(m.end());
	
						// Continue looking for further lines
						hasMore = true;
						continue;
					}
				// Check line for library line
				m = p_ll.matcher(tmp);
				if (m.find())
					if (m.start() == 0) {
						// Format the line
						String tbline = m.group().replaceAll("[\\n\\r]", "").trim();
						
						// Split into name and location
						String[] info = tbline.split("\\(");
						TalkbackEntry anEntry = new TalkbackEntry(info[0],info[1].replaceAll("\\)",""),TalkbackEntry.LIBRARYLINE);
						
						// Add the entry to the talkback entries list
						entries.add(anEntry);
						
						// Remove this talback entry line from the string that is to be processed
						tmp = tmp.substring(m.end());
	
						// Continue looking for further lines
						hasMore = true;
						continue;
					}
				}
				if (entries.size() > 0) {
					TalkbackTrace tbTrace = new TalkbackTrace(entries);
					foundTraces.add(tbTrace);
				}
			}
		
		return foundTraces;
	}

}

