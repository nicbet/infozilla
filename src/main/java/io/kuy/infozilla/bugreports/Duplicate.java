/**
 * Duplicate.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.bugreports;


public class Duplicate {

	private BugReport duplicateReport;
	private BugReport originalReport;
		
	public Duplicate(BugReport duplicateReport, BugReport originalReport) {
		super();
		this.duplicateReport = duplicateReport;
		this.originalReport = originalReport;
	}
	
	public BugReport getDuplicateReport() {
		return duplicateReport;
	}
	
	public BugReport getOriginalReport() {
		return originalReport;
	}
	
	
}
