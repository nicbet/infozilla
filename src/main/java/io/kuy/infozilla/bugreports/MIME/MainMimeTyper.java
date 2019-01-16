/**
 * MainMimeTyper.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.bugreports.MIME;

import java.util.Map;

import io.kuy.infozilla.bugreports.BugReport;
import io.kuy.infozilla.datasources.bugzilladb.DatabaseConnector;

public class MainMimeTyper {

	/**
	 * @param args 
	 * args[0] database host
	 * args[1] database user
	 * args[2] database user password
	 * args[3] database name
	 * args[4] bug report id to start with
	 * args[5] amount of bug reports to process
	 */
	public static void main(String[] args) {
		
		if (args.length != 6) {
			usage();
			System.exit(-1);
		}
		
		// Setup connection to bug report database
		DatabaseConnector dbc = new DatabaseConnector(args[0], args[1], args[2], args[3]);
		dbc.initialize();
		if (!dbc.connect()) {
			System.out.println("Could not connect to database. Check host, port, username and password.");
			System.exit(1);
		}
		int startID = Integer.valueOf(args[4]);
		do {
			System.out.println("-------------------------------------------------------------------------------");
			// Fetch a number of Bug Report unique IDs belonging to the following query
			
			String setSelectionQuery = "SELECT DISTINCT bug_id FROM bugzilla_bugs WHERE bug_id > " + startID + " ORDER BY bug_id ASC LIMIT " + args[5];
			int[] bugIDs = dbc.getIdSetByQuery(setSelectionQuery);
			
			if (bugIDs.length == 0)
				break;
			
			int lastID = bugIDs[bugIDs.length -1];
			
			System.out.println("-- Attempting to fetch " + bugIDs.length + " reports starting with ID " + startID);
			System.out.println("-------------------------------------------------------------------------------");
	
			// Collect the bug reports from the database using up to 100 parallel threads.
			Map<Integer, BugReport> bugReports = dbc.getReportsConcurrent(bugIDs, true, false, false);
			System.out.println("-- Fetched " + bugReports.size() + " Reports");
			
			if (Integer.valueOf(startID) == 0) {
				System.out.println("-------------------------------------------------------------------------------");
				System.out.println("-- Empty metrics info, then write the table");
				System.out.println("DELETE FROM bugzilla_attachments_mime");
				System.out.println("-------------------------------------------------------------------------------");
			}
			
			for (int id : bugIDs) {				
				Map<String, String> mimeTypesOfAttachaments = MimeTyper.discoverMimeTypesOfBugReport(id, dbc);
				for (String attach_id : mimeTypesOfAttachaments.keySet()) {
					String mimeString = mimeTypesOfAttachaments.get(attach_id);
					String[] parts = mimeString.split("; ");
					String mimeType = parts[0];
					
					String encoding = "unknown";
					if (parts.length > 1) {
						if (parts[1].length() > 8) {
							encoding = parts[1].substring(8, parts[1].length());
						}
					}
					
					System.out.println("INSERT INTO bugzilla_attachments_mime (attach_id, mimetype, encoding, raw) VALUES ("
							+ attach_id + ", "
							+ "'" + escape(mimeType.trim()) + "', "
							+ "'" + escape(encoding.trim()) + "', "
							+ "'" + escape(mimeString.trim()) + "');");
				}
			}
			
			// Output last processed id for the user to he can pick up execution there
			System.out.println("-------------------------------------------------------------------------------");
			System.out.println("-- Last ID processed was " + lastID);
			System.out.println("-------------------------------------------------------------------------------");
			startID = lastID;
		} while (true);
		
		// Disconnect the DatabaseConnector
		dbc.disconnect();

	}
	
	private static void usage() {
		System.out.println("Usage: java -Xmx2000M -jar mimetyper.jar");
		System.out.println("* args[0] database host");
		System.out.println("* args[1] database user");
		System.out.println("* args[2] database user password");
		System.out.println("* args[3] database name");
		System.out.println("* args[4] bug report id to start with");
		System.out.println("* args[5] amount of bug reports to process");
	}

	public static String escape(String input) {
		// Escape all \ with \\
		String escaped = input.replaceAll("[\\\\]", "\\\\\\\\");
		// All ' become ''
		escaped = escaped.replaceAll("'", "''");
		return escaped;
	}

}
