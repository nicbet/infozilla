/**
 * BugReport.java
  * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.bugreports;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * This class models a Bug Report in the BugZilla database systems.
 * @author Nicolas Bettenburg
 *
 */
public class BugReport implements Comparable<BugReport> {

	private int bug_id = -1;
	private String product_id = "unknown";
	private String component_id = "unknown";
	private String priority = "unknown";
	private String assigned_to = "unknown";
	private String bug_severity = "unknown";
	private String bug_status = "unknown";
	private Timestamp creation_ts;
	private String short_desc = "unknown";
	private String resolution = "unknown";
	private Discussion discussion;  // have a priority queue of discussion articles ordered by Timestamp
	private List<Attachment> attachments; 	// have a list of attachements
	
	/**
	 * Standard Constructor for an instance of BugReport
	 * @param bug_id An integer value describing the unique ID of this Bug Report
	 * @param assigned_to The developer this Bug Report was assigned to
	 * @param bug_severity The severity of the Bug
	 * @param bug_status The status of the Bug (like OPEN, CLOSED, RESOLVED, ...)
	 * @param creation_ts The timestamp of when the Bug Report was created
	 * @param short_desc A short summary of the Bug described (heading)
	 * @param resolution The resolution of the Bug (like WONTFIX, WORKSFORME, ...)
	 * @param discussion The complete discussion of the Report. The first discussion message is the description.
	 */
	public BugReport(int bug_id, 
					String product_id, String component_id, String priority,
					String assigned_to, String bug_severity,
					String bug_status, Timestamp creation_ts, String short_desc,
					String resolution, Discussion discussion) {
		super();
		this.bug_id = bug_id;
		this.product_id = product_id;
		this.component_id = component_id;
		this.priority = priority;
		this.assigned_to = assigned_to;
		this.bug_severity = bug_severity;
		this.bug_status = bug_status;
		this.creation_ts = creation_ts;
		this.short_desc = short_desc;
		this.resolution = resolution;
		
		// Instanciate the Discussion Class
		this.discussion = discussion;
		
		// Instanciate an empty attachment list
		this.attachments = new ArrayList<Attachment>();
	}
	
	public int getBug_id() {
		return bug_id;
	}
	
	public String getAssigned_to() {
		return assigned_to;
	}
	
	public String getBug_severity() {
		return bug_severity;
	}

	public String getBug_status() {
		return bug_status;
	}
	
	public Timestamp getCreation_ts() {
		return creation_ts;
	}

	public String getShort_desc() {
		return short_desc;
	}

	public String getResolution() {
		if (resolution == null)
			return "NO RESOLUTION";
		else
			return resolution;
	}

	public Discussion getDiscussion() {
		return discussion;
	}
	
	public String getDescription() {
		if (discussion.getMessages().size() > 0)
			return discussion.getDescription().getText();
		else
			return "";
	}
	
	public String getDiscussionText() {
		String discussionText = "";
		for (Message m : discussion.getMessages()) {
			discussionText = discussionText + m.getText() + System.getProperty("line.separator") + System.getProperty("line.separator");
		}
		return discussionText;
	}
	
	/**
	 * The complete discussion until a specified date - useful when doing analysis for prior releases
	 * @param untilDate
	 * @return
	 */
	public String getDiscussionText(Timestamp untilDate) {
		String discussionText = "";
		for (Message m : discussion.getMessages()) {
			if (m.getTime().before(untilDate))
				discussionText = discussionText + m.getText() + System.getProperty("line.separator") + System.getProperty("line.separator");
		}
		return discussionText;
	}
	
	public String getInitialMessageText() {
		String messageText = "";
		if (discussion.getMessages().size() > 0)
			messageText = discussion.getMessages().get(0).getText();
		
		return messageText;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public int compareTo(BugReport that) {
		if (this.bug_id < that.bug_id)
			return -1;
		else if (this.bug_id > that.bug_id)
			return 1;
		else
			return 0;
	}

	public String getProduct_id() {
		return product_id;
	}

	public String getComponent_id() {
		return component_id;
	}

	public String getPriority() {
		return priority;
	}

	/**
	 * @param attachments the attachments to set
	 */
	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}
	
	
}
