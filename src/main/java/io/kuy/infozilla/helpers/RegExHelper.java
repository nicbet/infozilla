/**
 * RegExHelper.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExHelper {

	public static Iterable<MatchResult> findMatches(final Pattern p,
			CharSequence s) {
		List<MatchResult> results = new ArrayList<MatchResult>();

		for (Matcher m = p.matcher(s); m.find();) {
			results.add(m.toMatchResult());
		}
		return results;
	}
	
	public static String makeLinuxNewlines(String input) {
		String output = input.replaceAll("(([\r][\n])|([\r]))", "\n");
		return output;
	}

}
