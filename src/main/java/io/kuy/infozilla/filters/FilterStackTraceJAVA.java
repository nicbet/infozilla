/**
 * FilterStackTraceJAVA.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.kuy.infozilla.elements.stacktrace.java.StackTrace;
import io.kuy.infozilla.helpers.RegExHelper;

public class FilterStackTraceJAVA implements IFilter{
	
	private FilterTextRemover textRemover;
	
	// Define a reusable Regular Expression for finding Java Stack Traces
	public static String JAVA_EXCEPTION = "^(([\\w<>\\$_]+\\.)+[\\w<>\\$_]+(Error|Exception){1}(\\s|:))";
	public static String JAVA_REASON = "(:?.*?)(at\\s+([\\w<>\\$_]+\\.)+[\\w<>\\$_]+\\s*\\(.+?\\.java(:)?(\\d+)?\\)";
	public static String JAVA_TRACE = "(\\s*?at\\s+([\\w<>\\$_\\s]+\\.)+[\\w<>\\$_\\s]+\\s*\\(.+?\\.java(:)?(\\d+)?\\))*)";
	public static String JAVA_CAUSE = "(Caused by:).*?(Exception|Error)(.*?)(\\s+at.*?\\(.*?:\\d+\\))+";
	public static String JAVA_STACKTRACE = JAVA_EXCEPTION + JAVA_REASON + JAVA_TRACE;
	private static Pattern pattern_stacktrace_java = Pattern.compile(JAVA_STACKTRACE,  Pattern.DOTALL | Pattern.MULTILINE);
	private static Pattern pattern_cause_java = Pattern.compile(JAVA_CAUSE, Pattern.DOTALL | Pattern.MULTILINE);
	
	
	/**
	 * Find StackTraces or Causes that match against our exhaustive patterns
	 * @param s The CharSequence to look in for Stack Traces or Causes
	 * @return A list of Matches
	 */
	private List<MatchResult> findStackTraces(CharSequence s) {
		List<MatchResult> stacktraces = new ArrayList<MatchResult>();
		
		for (MatchResult r : RegExHelper.findMatches(pattern_stacktrace_java, s)) {
			stacktraces.add(r);			
		}
		
		for (MatchResult r: RegExHelper.findMatches(pattern_cause_java, s)) {
			stacktraces.add(r);
		}
		return stacktraces;
	}
	
	/**
	 * This method is used to create an StackTrace object given its String representation.
	 * @param stackTraceMatchGroup the String representing a StrackTrace Cause.
	 * 		This usually comes from a RegExHelper matches' group() operation! 
	 * @return a StackTrace as represented by the given String 
	 */
	private StackTrace createCause(String stackTraceMatchGroup) {
		String exception = "";
		String reason = "";
		List<String> foundFrames = new ArrayList<String>();
		
		// This Pattern has:       GROUP 1     GROUP 2   GROUP 3
		String causeException = "(Caused by:)(.*?(Error|Exception){1})(.*?)(at\\s+([\\w<>\\$_\n\r]+\\.)+[\\w<>\\$_\n\r]+\\s*\\(.+?\\.java(:)?(\\d+)?\\)(\\s*?at\\s+([\\w<>\\$_\\s]+\\.)+[\\w<>\\$_\\s]+\\s*\\(.+?\\.java(:)?(\\d+)?\\))*)";
		Pattern causeEPattern = Pattern.compile(causeException, Pattern.DOTALL | Pattern.MULTILINE);
		
		// Find the Exception of this cause (which is group 2 of causeEPattern)
		Matcher exceptionMatcher = causeEPattern.matcher(stackTraceMatchGroup);
		if (exceptionMatcher.find()) {
			MatchResult matchResult = exceptionMatcher.toMatchResult();
			
			exception = matchResult.group(2).trim();
			reason = matchResult.group(4).trim();
			
			// look at the frames
			String regexFrames = "(^\\s*?at\\s+(([\\w<>\\$_\n\r]+\\.)+[\\w<>\\$_\n\r]+\\s*\\(.*?\\)$))";
			Pattern patternFrames = Pattern.compile(regexFrames, Pattern.DOTALL | Pattern.MULTILINE);
			
			// Find all frames (without the preceeding "at" )
			for (MatchResult framesMatch : RegExHelper.findMatches(patternFrames, matchResult.group(5)))
				foundFrames.add(framesMatch.group(2).replaceAll("[\n\r]", ""));				
		}
		// create a Stacktrace
		StackTrace trace = new StackTrace(exception, reason, foundFrames);
		trace.setCause(true);
		
		return(trace);
	}
	
	/**
	 * This method is used to create an StackTrace object given its String representation.
	 * @param stackTraceMatchGroup the String representing a StrackTrace.
	 * 		This usually comes from a RegExHelper matches' group() operation! 
	 * @return a StackTrace as represented by the given String 
	 */
	private StackTrace createTrace(String stackTraceMatchGroup) {
		String exception = "";
		String reason = "";
		List<String> foundFrames = new ArrayList<String>();
		
		// This Pattern has:       GROUP 1 = exception  GROUP 4 = reason  GROUP 5 = frames
		String traceException = "(([\\w<>\\$_]+\\.)+[\\w<>\\$_]+(Error|Exception){1})(.*?)(at\\s+([\\w<>\\$_\n\r]+\\.)+[\\w<>\\$_\n\r]+\\s*\\(.+?\\.java(:)?(\\d+)?\\)(\\s*?at\\s+([\\w<>\\$_\\s]+\\.)+[\\w<>\\$_\\s]+\\s*\\(.+?\\.java(:)?(\\d+)?\\))*)";
		Pattern tracePattern = Pattern.compile(traceException, Pattern.DOTALL | Pattern.MULTILINE);
		
		
		// Find the Exception of this cause (which is group 2 of causeEPattern)
		Matcher exceptionMatcher = tracePattern.matcher(stackTraceMatchGroup);
		if (exceptionMatcher.find()) {
			MatchResult matchResult = exceptionMatcher.toMatchResult();
		
			exception = matchResult.group(1).trim();
			reason = matchResult.group(4).trim();
		
		// look at the frames
		String regexFrames = "(^\\s*?at\\s+(([\\w<>\\$_\\s]+\\.)+[\\w<>\\$_\\s]+\\s*\\(.*?\\)$))";
		Pattern patternFrames = Pattern.compile(regexFrames, Pattern.DOTALL | Pattern.MULTILINE);
		
		// Find all frames (without the preceeding "at" )
		for (MatchResult framesMatch : RegExHelper.findMatches(patternFrames, matchResult.group(5)))
			foundFrames.add(framesMatch.group(2).replaceAll("[\n\r]", ""));				
		}
		// create a Stacktrace
		StackTrace trace = new StackTrace(exception, reason, foundFrames);
		trace.setCause(false);
		
		return(trace); 
	}
	
	/**
	 * Get a List of StackTraces that are inside the Text s
	 * @param inputSequence A CharSequence containing the Text to look for Stack Traces in
	 * @return A List of StackTraces
	 */
	private List<StackTrace> getStackTraces(CharSequence inputSequence) {
		List<StackTrace> stackTraces = new ArrayList<StackTrace>();
		
		// Split the text sequence first by possible exception start otherwise
		// multiline patterns will run FOREVER!
		
		int[] possibleStart = findExceptions(inputSequence);
		
		for (int i=0; i < possibleStart.length -1; i++) {
			
			CharSequence region = inputSequence.subSequence(possibleStart[i], possibleStart[i+1] -1);
			List<MatchResult> matches = findStackTraces(region);
			
			for (MatchResult match : matches) {
				String matchText = match.group();
				
				// Mark this Stack Trace match for deletion
				int traceStart = inputSequence.toString().indexOf(matchText);
				int traceEnd = traceStart + matchText.length() + 1;
				textRemover.markForDeletion(traceStart, traceEnd);
				if (traceStart == 0 && traceEnd == 0)
					System.out.println("Critical Error in Stacktrace Filter! Could not find start and End!");
				// Check if it is a cause or not
				if (matchText.trim().startsWith("Caused by:")) {
					// Create a cause
					StackTrace cause = createCause(matchText);
					// Add it to the List of Stack Traces
					cause.setTraceStart(traceStart);
					cause.setTraceEnd(traceEnd);
					stackTraces.add(cause);
					
				} else {
					// Create a trace
					StackTrace trace = createTrace(matchText);
					// Add it to the List of Stack Traces
					trace.setTraceStart(traceStart);
					trace.setTraceEnd(traceEnd);
					stackTraces.add(trace);
				}
			}
		}
		
		// And for the last region, too !!!!!!!!!!!!!!!!!!!!!!!!!!!!
		if (possibleStart.length > 0) {
			
			CharSequence region = inputSequence.subSequence(possibleStart[possibleStart.length-1], inputSequence.length());
			List<MatchResult> matches = findStackTraces(region);
			
			for (MatchResult match : matches) {
				String matchText = match.group();
				
				// Mark this Stack Trace match for deletion
				int traceStart = inputSequence.toString().lastIndexOf(matchText);
				int traceEnd = traceStart + matchText.length();
				textRemover.markForDeletion(traceStart, traceEnd);
				if (traceStart == 0 && traceEnd == 0)
					System.out.println("Critical Error in Stacktrace Filter! Could not find start and End!");
				
				// Check if it is a cause or not
				if (matchText.trim().startsWith("Caused by:")) {
					// Create a cause
					StackTrace cause = createCause(matchText);
					// Add it to the List of Stack Traces
					cause.setTraceStart(traceStart);
					cause.setTraceEnd(traceEnd);
					stackTraces.add(cause);
					
				} else {
					// Create a trace
					StackTrace trace = createTrace(matchText);
					// Add it to the List of Stack Traces
					trace.setTraceStart(traceStart);
					trace.setTraceEnd(traceEnd);
					stackTraces.add(trace);
				}
			}
		}
		
		return stackTraces;
	}
	
	
	/**
	 * Find a list of starting points of x.y.zException or x.y.zError
	 * @param s The CharSequence to look inside for such starting points
	 * @return an array of possible starting points
	 */
	private final int[] findExceptions(CharSequence s) {
		List<Integer> exceptionList = new ArrayList<Integer>();
		
		// We match against our well known JAVA_EXCEPTION Pattern that denotes a start of an exception or error
		Pattern exceptionPattern = Pattern.compile(JAVA_EXCEPTION, Pattern.DOTALL | Pattern.MULTILINE);
		
		// For every match we want to add the start of that match to the list of possible starting points
		for (MatchResult r : RegExHelper.findMatches(exceptionPattern, s)) {
			
			// If there have previously been some starting points
			if (exceptionList.size() > 0){
				// See if this new starting points is at least 20 lines away from the old one
				// Sometimes the reason contains another Exception in the first 5 lines
				// In this case we would otherwise omit the root exception but take the exception stated in Reason as root!
				String newRegion = s.subSequence(exceptionList.get(exceptionList.size()-1), r.start()).toString();
				if ( newRegion.split("[\n\r]").length >= 20)
					exceptionList.add(Integer.valueOf(r.start()));
			} else {
				// If there had been no starting points before just add one to start with
				exceptionList.add(Integer.valueOf(r.start()));
			}
		}
		
		// If no region is found then go and try the whole text for exhaustive search
		if (exceptionList.size() == 0)
			exceptionList.add(Integer.valueOf(0));
		
		// Convert the List<Integer> to an array
		int[] results = new int[exceptionList.size()];
		for (int i=0; i < exceptionList.size(); i++)
			results[i] = exceptionList.get(i).intValue();
	
		return results;
	}

	// Auto-generated Message from IFilter interface
	public String getOutputText() {
		return textRemover.doDelete();
	}

	// Auto-generated Message from IFilter interface
	public List<StackTrace> runFilter(String inputText) {
		// Initialize TextRemover
		textRemover = new FilterTextRemover(inputText);
		// Get a Bunch of Stack Traces
		List<StackTrace> foundStackTraces = getStackTraces(inputText);
		
		// Do the removal in the textRemover
		// ==> This is already done in getStackTraces when a MatchResult is present!
		
		// And return the found Stack Traces
		return foundStackTraces;
	}
}
