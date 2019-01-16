/**
 * DatabaseConnector.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.datasources.bugzilladb;


import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.kuy.infozilla.bugreports.Attachment;
import io.kuy.infozilla.bugreports.BugReport;
import io.kuy.infozilla.bugreports.Discussion;
import io.kuy.infozilla.bugreports.Duplicate;
import io.kuy.infozilla.bugreports.Message;

/**
 * This class is used to connect to a BugZilla database, that was mined with APFEL.
 * For more information about the APFEL tool, please contact Dr. Thomas Zimmermann.
 * @author Nicolas Bettenburg
 *
 */
public class DatabaseConnector {
	
	// Nested Class for Evaluation Results
	public class EvaluationResult {
		public int bug_id;
		public boolean stacktrace;
		public boolean patch;
		public boolean source;
		public boolean enumeration;
		public EvaluationResult(int bug_id, boolean stacktrace, boolean patch,
				boolean source, boolean enumeration) {
			super();
			this.bug_id = bug_id;
			this.stacktrace = stacktrace;
			this.patch = patch;
			this.source = source;
			this.enumeration = enumeration;
		}
	}

	// Nested class for Duplicate Pairs
	public class DupeIDPair {
		public int dupeID;
		public int originalID;
		public DupeIDPair(int d, int o) {
			dupeID = d;
			originalID = o;
		}
	}
	
	private String dbHostname;
	private String dbUsername;
	private String dbPassword;
	private String dbName;
	private Connection dbConnection;

	/**
	 * Standard constructor of the Database Connector class
	 * @param host	The address or name of the server hosting the database.
	 * @param user	The name of a user who has permission to access the database.
	 * @param pw 	The password of this {@link user}.
	 * @param dbname	The name of the database.
	 */
	public DatabaseConnector(String host, String user, String pw, String dbname) {
		dbHostname = host;
		dbUsername = user;
		dbPassword = pw;
		dbName = dbname;
	}

	/**
	 * This method initializes the JDBC Driver and Database Connection.
	 * It has to be called at least once before actually trying to use the DBC.
	 */
	public void initialize() {
		// try to load the Postgres JDBC driver
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException E) {
			System.err.println("Error in DatabaseConnector::initialize() while loading postgresql driver");
			System.err.println(E.getMessage());
		}
	}

	/**
	 * This method connects to the database.
	 * @return true if the connection could be established, false otherwise.
	 */
	public boolean connect() {
		String connectionString = "jdbc:postgresql://" + dbHostname + "/"
				+ dbName;

		//System.out.println("Trying to connect to " + dbHostname + " with User " + dbUsername);
		try {
			dbConnection = DriverManager.getConnection(connectionString, dbUsername, dbPassword);
		} catch (SQLException e) {
			System.err.println("Error in DatabaseConnector::connect()");
			System.err.println(e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * This method disconnects the database
	 * @return true if the database was successfully disconnects, false otherwise.
	 */
	public boolean disconnect() {
		try {
			dbConnection.close();
			return true;
		} catch (SQLException e) {
			System.out.println("Error in DatabaseConnector::disconnect()");
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	/**
	 * Receive the complete discussion for a Bug Report
	 * @param bug_id	The unique id of the Bug Report for which the discussion should be received.
	 * @return A {@link Discussion} of the Bug Report.
	 * @throws SQLException if there was an error retrieving the discussion.
	 */
	private Discussion createDiscussion(int bug_id) throws SQLException {
		Statement statement = dbConnection.createStatement();
		String discquery = "SELECT * FROM bugzilla_longdescs WHERE bug_id='"+ Integer.toString(bug_id) +"'";
		// Run the Query - we SHOULD get at least ONE record
		ResultSet result = statement.executeQuery(discquery);
		
		Discussion theDiscussion = new Discussion();
		
		while (result.next()) {
			String who = result.getString("who");
			Timestamp when = result.getTimestamp("bug_when");
			String thetext = result.getString("thetext");
			
			Message aMessage = new Message(who, when, thetext);
			theDiscussion.addMessage(aMessage);
		}
		
		return theDiscussion;
	}
	
	/**
	 * This method is used to create an instance of {@link BugReport} using a result from the database.
	 * @param result A ResultSet that links to a successful query of a Report.
	 * @return An instance of {@link BugReport}. 
	 * @throws SQLException If there was any error retrieving data.
	 */
	private BugReport createReportFromResultSet(int id, ResultSet result, boolean discussions, boolean attachments) throws Exception{
		BugReport areport = null;
		// If we have a result
		if (result.next()) {
			// Retrieve all relevant bug report data from the result set.
			int bug_id = result.getInt("bug_id");
			String product_id = result.getString("product_id");
			String component_id = result.getString("component_id");
			String priority = result.getString("priority");
			String assigned_to = result.getString("assigned_to");
			String bug_severity = result.getString("bug_severity");
			String bug_status = result.getString("bug_status");
			Timestamp creation_ts = result.getTimestamp("creation_ts");
			String short_desc = result.getString("short_desc");
			String resolution = result.getString("resolution");
			
			// Query a discussion! If discussions is true, run a query and generate a discussion out of that
			// otherwise just create an empty discussion.
			Discussion discussion;
			if (discussions)
				discussion = createDiscussion(bug_id);
			else
				discussion = new Discussion();
					
			// Create the bug report
			areport = new BugReport(bug_id, product_id, component_id, priority, assigned_to, bug_severity, bug_status, creation_ts, short_desc, resolution, discussion);
			
			// Query the attachments! If attachments is true, run a query and generate a list of attachments out of that
			// By default a newly created bug report has an empty list of attachments.
			if (attachments) {
				List<Attachment> attachmentlist = getAttachmentsFor(bug_id);
				// Add the attachments
				areport.setAttachments(attachmentlist);
			}
		} else {
			// If something went wrong
			// throw new SQLException("Error creating a Bug Report from the result set - ResultSet was empty!");
			throw new ReportNotFoundException(Integer.toString(id));
		}
		
		// return the bug report
		return areport;
	}

	/**
	 * Use this method to retrieve a {@link BugReport} from the database. This method
	 * takes care of all the steps to gather needed information. However it will only 
	 * include the discussion but not the attachments!
	 * @param bug_id The unique ID of the bug report you want to retrieve.
	 * @return An instance of the {@BugReport} that belongs to {@link bug_id}.
	 * @throws Exception If there were any problems with the database.
	 */
	public BugReport getReport(int bug_id) throws Exception {
		BugReport areport = null;
		// Read the bug report with ID bug_id from the database
		// and create an instance of the class BugReport.
		
		Statement statement = dbConnection.createStatement();
		String repquery = "SELECT * FROM bugzilla_bugs WHERE bug_id='"
				+ Integer.toString(bug_id) + "'";
		ResultSet result = statement.executeQuery(repquery);
		
		areport = createReportFromResultSet(bug_id, result, true, false);
		
		// Clean up
		result.close();
		statement.close();
		
		// Return the report
		return areport;
	}
	
	/**
	 * Overloaded method. Let's you explicitly specify whether discussion and attachments are desired or not.
	 * See {@link getReport(int bug_id)} for more information.
	 * @param bug_id	The unique ID of the bug report you want to retrieve.
	 * @param discussions	A boolean value. True if you want discussions included, false otherwise.
	 * @param attachments	A boolean value. True if you want attachments included, false otherwise.
	 * @return				an instance of the {@link BugReport} that belongs to {@link bug_id}.
	 * @throws Exception	If there were any problems with the database.
	 */
	public BugReport getReport(int bug_id, boolean discussions, boolean attachments) throws Exception {
		BugReport areport = null;
		// Read the bug report with ID bug_id from the database
		// and create an instance of the class BugReport.

		Statement statement = dbConnection.createStatement();
		String repquery = "SELECT * FROM bugzilla_bugs WHERE bug_id='"
				+ Integer.toString(bug_id) + "'";
		ResultSet result = statement.executeQuery(repquery);
		
		areport = createReportFromResultSet(bug_id, result, discussions, attachments);
		
		// Clean up
		result.close();
		statement.close();
		
		// Return the report
		return areport;
	}
	
	public BugReport getReportFAST(int bug_id, boolean discussions, boolean attachments) throws Exception {
		BugReport areport = null;
		Statement statement = dbConnection.createStatement();
		String repquery = "select *, ARRAY(SELECT thetext FROM bugzilla_longdescs WHERE bug_id='" + bug_id 
							+ "' ORDER BY bug_when) as discussiontext FROM bugzilla_bugs WHERE bug_id = '" +
							+ bug_id + "'";
		ResultSet result = statement.executeQuery(repquery);
		
		// If we have a result
		if (result.next()) {
			// Retrieve all relevant bug report data from the result set.
			int rbug_id = result.getInt("bug_id");
			String product_id = result.getString("product_id");
			String component_id = result.getString("component_id");
			String priority = result.getString("priority");
			String assigned_to = result.getString("assigned_to");
			String bug_severity = result.getString("bug_severity");
			String bug_status = result.getString("bug_status");
			Timestamp creation_ts = result.getTimestamp("creation_ts");
			String short_desc = result.getString("short_desc");
			String resolution = result.getString("resolution");
			Array discussiontext = result.getArray("discussiontext");
			
			// Query a discussion! If discussions is true, run a query and generate a discussion out of that
			// otherwise just create an empty discussion.
			Discussion discussion;
			if (discussions) {
				String[] discussionsmessages = (String[]) discussiontext.getArray();
				discussion = new Discussion();
				for (String msg : discussionsmessages) {
					Message aMessage = new Message("unkown", creation_ts,msg);
					discussion.addMessage(aMessage);
				}
			}
			else
				discussion = new Discussion();
					
			// Create the bug report
			areport = new BugReport(rbug_id, product_id, component_id, priority, assigned_to, bug_severity, bug_status, creation_ts, short_desc, resolution, discussion);
			
			// Query the attachments! If attachments is true, run a query and generate a list of attachments out of that
			// By default a newly created bug report has an empty list of attachments.
			if (attachments) {
				List<Attachment> attachmentlist = getAttachmentsFor(bug_id);
				// Add the attachments
				areport.setAttachments(attachmentlist);
			}
		} else {
			// If something went wrong
			// throw new SQLException("Error creating a Bug Report from the result set - ResultSet was empty!");
			throw new ReportNotFoundException(Integer.toString(bug_id));
		}
		return areport;
	}
	
	
	/**
	 * Retrieve a Report using a prepared statement. useful for concurrent fetches!
	 * @param bug_id	The unique ID of the bug report you want to retrieve.
	 * @param ps		The prepared statement that should be used
	 * @param discussions	A boolean value. True if you want discussions included, false otherwise.
	 * @param attachments	A boolean value. True if you want attachments included, false otherwise.
	 * @return				an instance of the {@link BugReport} that belongs to {@link bug_id}.
	 * @throws Exception	If there were any problems with the database. 
	 */
	public BugReport getReportPrepared(int bug_id, PreparedStatement ps, boolean discussions, boolean attachments) throws Exception {
		// Read the db result using the prepared statement
		BugReport areport = null;
		ps.setString(1, "" + bug_id);
		ResultSet result = ps.executeQuery();
		areport = createReportFromResultSet(bug_id, result, discussions, attachments);
		// Clean up
		result.close();
		// Return the report
		return areport;
	}
	
	/**
	 * Return the IDs of the next {@link amount} bug reports starting from ID {@link start}.
	 * @param start The id to start from getting the next IDs
	 * @param amount the amount of IDs to try fetching
	 * @return An array of the next {@link amount} bug reports starting from ID {@link start}.
	 * @throws SQLException If there were any problems with the database.
	 */
	public int[] getNextIDs(int start, int amount) throws SQLException {
		// Create a new Array of amount size
		List<Integer> idBuffer = new ArrayList<Integer>();
		
		// Run the query to fetch the next IDs
		String query = "SELECT bug_id FROM bugzilla_bugs WHERE bug_id >='" 
							+ Integer.toString(start) + "' AND bug_id <'" 
							+ Integer.toString(start+amount) + "'";
		
		Statement statement = dbConnection.createStatement();
		ResultSet result = statement.executeQuery(query);
		
		// Get the results from the query also counting the amount of results.
		int numresults = 0;
		while (result.next()) {
			idBuffer.add(Integer.valueOf(result.getInt(1)));
			numresults++;
		}
		
		// Copy the results into an array of the size of results we had
		// this is because the amount of results could be smaller than the desired amount.
		int[] nextIDs = new int[numresults];
		for (int i=0; i < numresults; i++) nextIDs[i] = idBuffer.get(i).intValue();
		
		// Return our findings
		return nextIDs;
	}
	
	/**
	 * Use this method to retrieve a List of {@link BugReport} from the database at once.
	 * It takes care of all the steps to gather needed information and discussion.
	 * @param start The id to start from getting the next IDs
	 * @param amount the amount of IDs to try fetching
	 * @return An List of instances of{@BugReport} starting from {@link start}
	 * @throws SQLException If there were any problems with the database.
	 */
	public List<BugReport> getNextReports(int amount, int start) throws SQLException {
		
		// Initialize a new list of reports
		List<BugReport> nextReports = new ArrayList<BugReport>();
		
		// Read the bug report with ID bug_id from the database
		// and create an instance of the class BugReport.
		
		Statement statement = dbConnection.createStatement();
		String repquery = "SELECT * FROM bugzilla_bugs WHERE bug_id >='"
				+ Integer.toString(start) + "'" + " AND bug_id <'" + Integer.toString(start + amount) + "'";
		
		ResultSet result = statement.executeQuery(repquery);
		
		while(result.next()) {
			// Get the data ...
			int bug_id = result.getInt("bug_id");
			String product_id = result.getString("product_id");
			String component_id = result.getString("component_id");
			String priority = result.getString("priority");
			String assigned_to = result.getString("assigned_to");
			String bug_severity = result.getString("bug_severity");
			String bug_status = result.getString("bug_status");
			Timestamp creation_ts = result.getTimestamp("creation_ts");
			String short_desc = result.getString("short_desc");
			String resolution = result.getString("resolution");
			
			// Query a discussion here!
			Discussion discussion = createDiscussion(bug_id);
			
			BugReport aReport = new BugReport(bug_id, product_id, component_id, priority, assigned_to, bug_severity, bug_status, creation_ts, short_desc, resolution, discussion);
			nextReports.add(aReport);
		}
		
		// Clean up
		result.close();
		statement.close();
		return nextReports;
	}
	
	/**
	 * Use to discover the duplicate IDs of a given Bug Report.
	 * @param bug_id The ID of the Bug Report we want to know it's duplicates for
	 * @return An array of ID numbers of duplicate Bug Reports.
	 * @throws SQLException when an error occurred in the database.
	 */
	public int[] getDuplicatesOf(int bug_id) throws SQLException {
		List<Integer> dupesBuffer = new ArrayList<Integer>();
		
		// Get Duplicates from Database
		String query = "SELECT dupe FROM bugzilla_duplicates WHERE dupe_of='" + bug_id + "'";
		Statement statement = dbConnection.createStatement();
		ResultSet result = statement.executeQuery(query);
		
		// Put all duplicates to a temporary buffer
		while (result.next()) {
			dupesBuffer.add(Integer.valueOf(result.getInt(1)));
		}
		
		// Convert Buffer to int array
		int[] dupes = new int[dupesBuffer.size()];
		for (int i=0; i < dupesBuffer.size(); i++)
			dupes[i] = dupesBuffer.get(i).intValue();
		
		// return the int array
		return dupes;
	}
	
	
	
	/**
	 * This method will retrieve a list of duplicate/original bug report id pairs.
	 * @param startID the duplicate id to start with
	 * @param amount the amount of duplicates to list
	 * @return a list of duplicate/original bug report id pairs.
	 * @throws SQLException if something is wrong with the sql connection.
	 */
	public List<DupeIDPair> getDupePairs(int startID, int amount) throws SQLException {
		List<DupeIDPair> dupesList = new ArrayList<DupeIDPair>();
		
		// Get all Duplicates 
		String query = "SELECT dupe_of, dupe FROM bugzilla_duplicates WHERE dupe > '" 
				+ Integer.toString(startID) + "' ORDER BY dupe LIMIT " + Integer.toString(amount);
		Statement statement = dbConnection.createStatement();
		ResultSet result = statement.executeQuery(query);
		
		// Put all duplicates to a temporary buffer
		while (result.next()) {
			dupesList.add(new DupeIDPair(result.getInt("dupe"), result.getInt("dupe_of")));
		}
		
		return dupesList;
	}
	
	
	
	/**
	 * Use this function to feed in a list of dupliate/original id pairs and get the duplicate reports
	 * for further processing.
	 * @param dupeIDs a list of duplicate/original id pairs
	 * @return an array of Duplicate Reports.
	 * @throws SQLException if something is wrong with the sql connection!
	 */
	public Duplicate[] getSomeDuplicates(List<DupeIDPair> dupeIDs) throws SQLException {
		
		List<Duplicate> dupesBuffer = new ArrayList<Duplicate>();
		List<Integer> listOfIdsToBeFetched = new ArrayList<Integer>();
		
		// Add all duplicate ids and the original ids into the list of reports
		// that shall be downloaded later concurrently - this saves us a lot of time!
		for (int i=0; i < dupeIDs.size(); i++) {
			listOfIdsToBeFetched.add(dupeIDs.get(i).dupeID);
			listOfIdsToBeFetched.add(dupeIDs.get(i).originalID);
		}
		
		// Convert to an int array that we can feed in to the concurrent report fetcher!
		int[] idsToBeFetched = new int[listOfIdsToBeFetched.size()];
		for (int i=0; i < listOfIdsToBeFetched.size(); i++) {
			idsToBeFetched[i] = listOfIdsToBeFetched.get(i);
		}
		
		// Feed that int array into the concurrent bug Fetcher
		Map<Integer, BugReport> fetchedReports = getReportsConcurrent(idsToBeFetched, true, false);
		
		// Create a lot of duplicates:
		for (DupeIDPair dupeIdPair : dupeIDs) {
			Duplicate someDuplicate = 
				new Duplicate(fetchedReports.get(dupeIdPair.dupeID), 
							  fetchedReports.get(dupeIdPair.originalID));
			dupesBuffer.add(someDuplicate);
		}
		
		// Convert Buffer to Array
		Duplicate[] dupes = new Duplicate[dupesBuffer.size()];
		for (int i=0; i < dupes.length; i++)
			dupes[i] = dupesBuffer.get(i);
				
		// return the Array
		return dupes;
	}
	
	
	/**
	 * Retrieve a number of {@link amount} BugReports starting from {@link start} in parallel from database.
	 * This will put a heavy load on your network traffic and the database if using large values of {@amount}.
	 * @param start The Bug Report ID to start with.
	 * @param amount The number of Bug Reports to collect.
	 * @return A list of Bug Reports.
	 */
	public List<BugReport> getNextReportsConcurrent(int amount, int start) {
		
		// A nested class that handles the conccurent fetching of a report and returns a complete BugReport
		class CallableCollector implements Callable<BugReport>{
			private final int id;
			private DatabaseConnector dbC;
			public CallableCollector(int id) {
				this.id = id;
			}
			public BugReport call() throws Exception {
				dbC = new DatabaseConnector(dbHostname, dbUsername, dbPassword, dbName);
				dbC.initialize();
				dbC.connect();
				//System.out.println("Fetching Report " + id);
				BugReport r = dbC.getReport(id);
				dbC.disconnect();
				return r;
			}
		}
		
		List<BugReport> fetchedReports = new ArrayList<BugReport>();
		
		try {
			int[] nextIds = getNextIDs(start, amount);
			
			// Maybe we want a threadpool that is limited to 100 threads - the database cannot cope
			// with more anyways!
			//ExecutorService threadPool = Executors.newCachedThreadPool();
			ExecutorService threadPool = Executors.newFixedThreadPool(100);
			
			CompletionService<BugReport> completor = new ExecutorCompletionService<BugReport>(threadPool);
			for (int Id : nextIds) {
				completor.submit(new CallableCollector(Id));
			}
			
			// Take all results one by another
			for (int i=0; i < nextIds.length; i++) {
				Future<BugReport> someResult = completor.take();
				BugReport aReport = someResult.get();
				//System.out.println("Received Report " + aReport.getBug_id());
				fetchedReports.add(aReport);
			}
			threadPool.shutdownNow();
			
		} catch(Exception E) {
			System.out.println("Error while concurrently fetching Bug Reports");
			System.out.println(E.getMessage());
			E.printStackTrace();
		}
		
		return fetchedReports;
	}
	
	/**
	 * Retrieve a number of {@link amount} BugReports starting from {@link start} consecutively from database.
	 * @param ids A list of ids to fetch concurrently into a list of bugreports
	 * @return A list of Bug Reports.
	 */
	public Map<Integer, BugReport> getReportsConsecutive(int[] ids, boolean discussions, boolean attachments) {
		int currentID = -1;
		try {
			Map<Integer, BugReport> fetchedReports = new HashMap<Integer, BugReport>(); 
			for (int i=0; i < ids.length; i++) {
				currentID = ids[i];
				System.out.print(currentID + " ");
				BugReport aReport = getReport(ids[i], discussions, attachments);
				fetchedReports.put(ids[i], aReport);
				
			}
			return fetchedReports;
		} catch (Exception E) {
			System.err.println("Error while fetching Bug Report " + currentID);
			System.err.println(E.getMessage());
			throw new RuntimeException("Error in getReportsConsecutive");
		}
	}
	
	/**
	 * Retrieve a number of {@link amount} BugReports starting from {@link start} in parallel from database.
	 * This will put a heavy load on your network traffic and the database if using large values of {@amount}.
	 * @param ids A list of ids to fetch concurrently into a list of bugreports
	 * @return A list of Bug Reports.
	 */
	public Map<Integer, BugReport> getReportsConcurrent(int[] ids, boolean discussions, boolean attachments) {
		
		// A nested class that handles the conccurent fetching of a report and returns a complete BugReport
		class CallableCollector implements Callable<BugReport>{
			private final int id;
			private final boolean discussions;
			private final boolean attachments;
			private DatabaseConnector dbC;

			public CallableCollector(int id, PreparedStatement ps, boolean discussions, boolean attachments) {
				this.id = id;
				this.discussions = discussions;
				this.attachments = attachments;

			}
			public BugReport call() throws Exception {
				dbC = new DatabaseConnector(dbHostname, dbUsername, dbPassword, dbName);
				dbC.initialize();
				dbC.connect();
				//System.out.println("Fetching Report " + id);
				BugReport r = dbC.getReport(id, discussions, attachments);
				dbC.disconnect();
				return r;
			}
		}
		
		Map<Integer, BugReport> fetchedReports = new HashMap<Integer, BugReport>();
		
		try {
			int[] nextIDs = ids;
			
			// Maybe we want a threadpool that is limited to 100 threads - the database cannot cope
			// with more anyways!
			//ExecutorService threadPool = Executors.newCachedThreadPool();
			ExecutorService threadPool = Executors.newFixedThreadPool(10);
			PreparedStatement pst = dbConnection.prepareStatement("SELECT * FROM bugzilla_bugs WHERE bug_id=?");
			CompletionService<BugReport> completor = new ExecutorCompletionService<BugReport>(threadPool);
			for (int Id : nextIDs) {
				completor.submit(new CallableCollector(Id, pst, discussions, attachments));
			}
			
			// Take all results one by another
			for (int i=0; i < nextIDs.length; i++) {
				Future<BugReport> someResult = completor.take();
				BugReport aReport = someResult.get();
				//System.out.println("Received Report " + aReport.getBug_id());
				fetchedReports.put(aReport.getBug_id(), aReport);
			}
			threadPool.shutdownNow();
			
		} catch(Exception E) {
			System.out.println("Error while concurrently fetching Bug Reports");
			System.out.println(E.getMessage());
			E.printStackTrace();
			throw new RuntimeException("Some Reports could not be collected!");
		}
		return fetchedReports;
	}
	
	/**
	 * Retrieve a number of {@link amount} BugReports starting from {@link start} in parallel from database using fast method (no discussion details except for text).
	 * This will put a heavy load on your network traffic and the database if using large values of {@amount}.
	 * @param ids A list of ids to fetch concurrently into a list of bugreports
	 * @return A list of Bug Reports.
	 */
	public Map<Integer, BugReport> getReportsConcurrent(int[] ids, boolean discussions, boolean attachments, boolean fast) {
		
		// A nested class that handles the conccurent fetching of a report and returns a complete BugReport
		class CallableCollector implements Callable<BugReport>{
			private final int id;
			private final boolean discussions;
			private final boolean attachments;
			private DatabaseConnector dbC;
			private boolean fast;
			public CallableCollector(int id, boolean fast, boolean discussions, boolean attachments) {
				this.id = id;
				this.discussions = discussions;
				this.attachments = attachments;
				this.fast = fast;
			}
			public BugReport call() throws Exception {
				dbC = new DatabaseConnector(dbHostname, dbUsername, dbPassword, dbName);
				dbC.initialize();
				dbC.connect();
				//System.out.println("Fetching Report " + id);
				if (fast) {
					BugReport r = dbC.getReportFAST(id, discussions, attachments);
					dbC.disconnect();
					return r;
				} else {
					BugReport r = dbC.getReport(id, discussions, attachments);
					dbC.disconnect();
					return r;
				}
				
			}
		}
		
		Map<Integer, BugReport> fetchedReports = new HashMap<Integer, BugReport>();
		
		try {
			int[] nextIDs = ids;
			
			// Maybe we want a threadpool that is limited to 100 threads - the database cannot cope
			// with more anyways!
			//ExecutorService threadPool = Executors.newCachedThreadPool();
			ExecutorService threadPool = Executors.newFixedThreadPool(3);
			CompletionService<BugReport> completor = new ExecutorCompletionService<BugReport>(threadPool);
			for (int Id : nextIDs) {
				completor.submit(new CallableCollector(Id, fast, discussions, attachments));
			}
			
			// Take all results one by another
			for (int i=0; i < nextIDs.length; i++) {
				Future<BugReport> someResult = completor.take();
				BugReport aReport = someResult.get();
				//System.out.println("Received Report " + aReport.getBug_id());
				fetchedReports.put(aReport.getBug_id(), aReport);
			}
			threadPool.shutdownNow();
			
		} catch(Exception E) {
			System.out.println("Error while concurrently fetching Bug Reports");
			System.out.println(E.getMessage());
			E.printStackTrace();
		}
		return fetchedReports;
	}
	
	/**
	 * This function is only used for our Evaluation of the tool
	 * @param bug_id		The Bug_ID evaluated
	 * @param predicted		if we are inserting for a prediction or not
	 * @param stacktrace	boolean
	 * @param patch			boolean
	 * @param source		boolean
	 * @param enumeration	boolean
	 * @throws Exception	if there is something wrong with the database
	 */
	public void saveEvaluationResults(int bug_id, String which,
			boolean predicted, boolean real) throws Exception {
		
		// For Stacktraces
		if (which.equalsIgnoreCase("stacktrace")) {
			String query = "BEGIN;"
					+ "DELETE FROM results_stacktrace WHERE bug_id='"
					+ Integer.toString(bug_id) + "'; "
					+ "INSERT INTO results_stacktrace VALUES (" + "'" + bug_id
					+ "'," + "'" + predicted + "'," + "'" + real + "'"
					+ "); END;";
			Statement stmt = dbConnection.createStatement();
			stmt.executeUpdate(query);
		}
		
		// For Patches
		if (which.equalsIgnoreCase("patch")) {
			String query = "BEGIN;"
					+ "DELETE FROM results_patch WHERE bug_id='"
					+ Integer.toString(bug_id) + "'; "
					+ "INSERT INTO results_patch VALUES (" + "'" + bug_id
					+ "'," + "'" + predicted + "'," + "'" + real + "'"
					+ "); END;";
			Statement stmt = dbConnection.createStatement();
			stmt.executeUpdate(query);
		}
		
		// For Enumerations
		if (which.equalsIgnoreCase("enum")) {
			String query = "BEGIN;"
					+ "DELETE FROM results_enum WHERE bug_id='"
					+ Integer.toString(bug_id) + "'; "
					+ "INSERT INTO results_enum VALUES (" + "'" + bug_id
					+ "'," + "'" + predicted + "'," + "'" + real + "'"
					+ "); END;";
			Statement stmt = dbConnection.createStatement();
			stmt.executeUpdate(query);
		}
		
		// For Source Code
		if (which.equalsIgnoreCase("source")) {
			String query = "BEGIN;"
					+ "DELETE FROM results_source WHERE bug_id='"
					+ Integer.toString(bug_id) + "'; "
					+ "INSERT INTO results_source VALUES (" + "'" + bug_id
					+ "'," + "'" + predicted + "'," + "'" + real + "'"
					+ "); END;";
			Statement stmt = dbConnection.createStatement();
			stmt.executeUpdate(query);
		}
	}
	
	/**
	 * Return the evaluation result for a specific bug id.
	 * @param bug_id the bug id.
	 * @param predicted whether this is a predicted or a real result (chose table).
	 * @return a generated Evaluation Result.
	 * @throws Exception if something goes wrong.
	 */
	public EvaluationResult getEvalResultFor(int bug_id, boolean predicted) throws Exception {
		// Determine the table name depending on whether we want to get predicted or evaluated results
		String table = "";
		if (predicted)
			table = "predicted_features";
		else
			table = "real_features";
		
		// Build the Query and run the Query
		String query = "SELECT * FROM " +  table + " WHERE bug_id='" + bug_id + "'";
		Statement statement = dbConnection.createStatement();
		ResultSet result = statement.executeQuery(query);
		
		// if there is at least on result available, fetch it into an EvaluationResult and return it
		while (result.next()) {
			boolean st = result.getBoolean(2);
			boolean pt = result.getBoolean(3);
			boolean sr = result.getBoolean(4);
			boolean en = result.getBoolean(5);
			EvaluationResult evalResult = new EvaluationResult(bug_id, st, pt, sr, en);
			return evalResult;
		}
		return null;
	}
	
	/**
	 * Return a List of Attachments associated with a bug_id
	 * @param bug_id	the unique id of the bug report to fetch the attachments for
	 * @return			a list of {@link Attachment}s for the specified bug report
	 * @throws Exception	if anything goes wrong with the database connection.
	 */
	public List<Attachment> getAttachmentsFor(int bug_id) throws Exception {
		List<Attachment> attachments = new ArrayList<Attachment>();
		
		// Build the query and run it - we only want to download contents of text file types!
		// String query = "SELECT * FROM bugzilla_attachments WHERE bug_id = '" + bug_id + "'";
		// deleted WHEN 'text/html' THEN thedata because mozilla testcases destroy everything yay!
		
		String query = "SELECT bugzilla_attachments.attach_id, bug_id, creation_ts, isobsolete, description, bugzilla_attachments.mimetype, bugzilla_attachments_mime.mimetype AS magictype, bugzilla_attachments_mime.encoding as encoding ,submitter_id, filename, CASE bugzilla_attachments.mimetype WHEN 'application/text' THEN thedata WHEN 'text/*' THEN thedata WHEN 'text/bash-script' THEN thedata WHEN 'text/css' THEN thedata WHEN 'text/csv' THEN thedata WHEN 'text/csv file' THEN thedata WHEN 'text/diff' THEN thedata WHEN 'text/java' THEN thedata WHEN 'text/java source' THEN thedata WHEN 'text/js' THEN thedata WHEN 'text/log' THEN thedata WHEN 'text/php' THEN thedata WHEN 'text/plain' THEN thedata WHEN 'text/plain, text/file' THEN thedata WHEN 'text/x-csrc' THEN thedata WHEN 'text/x-csv' THEN thedata WHEN 'text/x-diff' THEN thedata WHEN 'text/x-java' THEN thedata WHEN 'text/x-java-source' THEN thedata WHEN 'text/x-log' THEN thedata WHEN 'text/xml' THEN thedata WHEN 'text/xml ' THEN thedata WHEN 'text/x-patch' THEN thedata WHEN 'text/x-sh' THEN thedata ELSE '' END as thedata, octet_length(thedata) as filesize FROM bugzilla_attachments JOIN bugzilla_attachments_mime ON bugzilla_attachments.attach_id = bugzilla_attachments_mime.attach_id WHERE bug_id = '" + bug_id + "'"; 
		
		//String query = "SELECT bugzilla_attachments.attach_id, bug_id, creation_ts, isobsolete, description, bugzilla_attachments.mimetype," +
		//		" bugzilla_attachments_mime.mimetype AS magictype, bugzilla_attachments_mime.encoding as encoding ,submitter_id, filename, thedata," +
		//		" octet_length(thedata) as filesize FROM bugzilla_attachments LEFT JOIN bugzilla_attachments_mime USING(attach_id) WHERE bug_id = '" + bug_id + "';";
		
		Statement statement = dbConnection.createStatement();
		ResultSet result = statement.executeQuery(query);
		
		// For every attachment
		while (result.next()) {
			String attach_id = result.getString("attach_id");
			Timestamp creation_ts = result.getTimestamp("creation_ts");
			String description = result.getString("description");
			boolean isObsolete = result.getBoolean("isobsolete");
			String mimetype = result.getString("mimetype");
			String magictype = result.getString("magictype");
			String encoding = result.getString("encoding");
			String filename = result.getString("filename");
			byte[] thedata = result.getBytes("thedata");
			int filesize = result.getInt("filesize");
			Attachment someAttachment = new Attachment(description, mimetype, filename, creation_ts, "", thedata, filesize);
			someAttachment.setAttachmentID(attach_id);
			someAttachment.setObsolete(isObsolete);
			someAttachment.setMagictype(magictype);
			someAttachment.setEncoding(encoding);
			someAttachment.guessType();
			attachments.add(someAttachment);
		}
		
		return attachments;
	}
	
	public List<Attachment> getRAWAttachmentsFor(int bug_id) throws Exception {
		List<Attachment> attachments = new ArrayList<Attachment>();
		
		// Build the query and run it - we only want to download contents of text file types!
		// String query = "SELECT * FROM bugzilla_attachments WHERE bug_id = '" + bug_id + "'";
		// deleted WHEN 'text/html' THEN thedata because mozilla testcases destroy everything yay!
		String query = "SELECT bugzilla_attachments.attach_id, bug_id, creation_ts, isobsolete ,description, submitter_id, mimetype, filename, thedata, octet_length(thedata) as filesize FROM bugzilla_attachments WHERE bug_id = '" + bug_id + "'"; 
		Statement statement = dbConnection.createStatement();
		ResultSet result = statement.executeQuery(query);
		
		// For every attachment
		while (result.next()) {
			String attach_id = result.getString("attach_id");
			Timestamp creation_ts = result.getTimestamp("creation_ts");
			String description = result.getString("description");
			boolean isObsolete = result.getBoolean("isobsolete");
			String mimetype = result.getString("mimetype");
			String filename = result.getString("filename");
			byte[] thedata = result.getBytes("thedata");
			int filesize = result.getInt("filesize");

			Attachment someAttachment = new Attachment(description, mimetype, filename, creation_ts, "", thedata, filesize);
			someAttachment.setObsolete(isObsolete);
			someAttachment.guessType();
			someAttachment.setAttachmentID(attach_id);
			attachments.add(someAttachment);
		}
		
		return attachments;
	}
	
	
	/**
	 * Get a List of Bug IDs using a custom query String.
	 * You should NOT USE this method unless you are REALLY SURE about what you are doing!
	 * {@link getNextReports()}, {@link getDuplicatesOf()} and alike methods are probably what you are looking for.
	 * @param query	The query to run to generate a list of bug_ids. first field of each result MUST be an integer value
	 * describing a unique bug id
	 * @return	an integer list of bug_ids associated with your custom query. This can be fed into {@link getReportsConcurrent()}
	 */
	public int[] getIdSetByQuery(String query) {
		// Create a new Array of amount size
		List<Integer> idBuffer = new ArrayList<Integer>();
		
		// Run the query to fetch the next IDs
		try {
			Statement statement = dbConnection.createStatement();
			ResultSet result = statement.executeQuery(query);
			
			// Get the results from the query also counting the amount of results.
			int numresults = 0;
			while (result.next()) {
				idBuffer.add(Integer.valueOf(result.getInt(1)));
				numresults++;
			}
			
			// Copy the results into an array of the size of results we had
			// this is because the amount of results could be smaller than the desired amount.
			int[] nextIDs = new int[numresults];
			for (int i=0; i < numresults; i++) nextIDs[i] = idBuffer.get(i).intValue();
			
			// Return our findings
			return nextIDs;
		} catch (SQLException e) {
			System.out.println("Error while fetching IDs using custom query!");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		// If there was an error with the query we will return an empty list of IDs
		return new int[0];
	}
	
	public ResultSet runArbitraryQyery(String Query) throws Exception {
		Statement stmt = dbConnection.createStatement();
		ResultSet rs = stmt.executeQuery(Query);
		return rs;
	}
}
