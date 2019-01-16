/**
 * DataExportUtility.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.helpers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

import io.kuy.infozilla.elements.enumeration.Enumeration;
import io.kuy.infozilla.elements.patch.Patch;
import io.kuy.infozilla.elements.patch.PatchHunk;
import io.kuy.infozilla.elements.sourcecode.java.CodeRegion;
import io.kuy.infozilla.elements.stacktrace.java.StackTrace;

public class DataExportUtility {
	
	
	/**
	 * Get an XML Export of a list of Stack Traces
	 * @param traces a List of {@link StackTrace} that should be exported as a new XML node
	 * @return an Element "Stack Traces" containing an XML Export of the given stack traces.
	 */
	public static final Element getXMLExportOfStackTraces(List<StackTrace> traces, boolean withFrames, Timestamp ts) {
		Element rootE = new Element("Stacktraces");
		rootE.setAttribute(new Attribute("amount", Integer.toString(traces.size())));

		// Add each Stack Traces to the JDOM Tree
		for (StackTrace trace : traces) {
			
			// Distinguish between trace and cause
			Element traceE;
			if (trace.isCause()) 
				traceE = new Element("Cause");
			else 
				traceE = new Element("Stacktrace");
			
			traceE.setAttribute(new Attribute("timestamp", Long.toString(ts.getTime())));
			// Add the Originating Exception
			Element exceptionE = new Element("Exception");
			exceptionE.setText(trace.getException());
			traceE.addContent(exceptionE);
			
			// As well as the Reason
			Element reasonE = new Element("Reason");
			reasonE.setText(trace.getReason());
			traceE.addContent(reasonE);
			
			if (withFrames) {
				// And last the Stack Frames (call stack)
				Element framesE = new Element("Frames");
				int depth = 0;
				for (String frame : trace.getFrames()) {
					Element frameE = new Element("Frame");
					frameE.setAttribute(new Attribute("depth", (Integer.toString(depth))));
					frameE.setText(frame);
					framesE.addContent(frameE);
					depth++;
				}
				traceE.addContent(framesE);
			}
			
			// Add this Stack Trace to the Root Element
			rootE.addContent(traceE);
		}
		
		// Return the set of Stack Traces
		return rootE;
	}
	
	
	public static final Element getXMLExportOfStackTrace(StackTrace trace, boolean withFrames, Timestamp ts) {
		Element rootE = new Element("Stacktrace");
					
		// Distinguish between trace and cause
		Element traceE;
		if (trace.isCause()) 
			traceE = new Element("Cause");
		else 
			traceE = new Element("Stacktrace");
		
		traceE.setAttribute(new Attribute("timestamp", Long.toString(ts.getTime())));
		// Add the Originating Exception
		Element exceptionE = new Element("Exception");
		exceptionE.setText(trace.getException());
		traceE.addContent(exceptionE);
		
		// As well as the Reason
		Element reasonE = new Element("Reason");
		reasonE.setText(trace.getReason());
		traceE.addContent(reasonE);
		
		if (withFrames) {
			// And last the Stack Frames (call stack)
			Element framesE = new Element("Frames");
			int depth = 0;
			for (String frame : trace.getFrames()) {
				Element frameE = new Element("Frame");
				frameE.setAttribute(new Attribute("depth", (Integer.toString(depth))));
				frameE.setText(frame);
				framesE.addContent(frameE);
				depth++;
			}
			traceE.addContent(framesE);
		}
		
		// Add this Stack Trace to the Root Element
		rootE.addContent(traceE);
		
		
		// Return the set of Stack Traces
		return rootE;
	}
	
	
	/**
	 * Get an XML Export of a list of Patches
	 * @param patches a list of {@link Patch} that should be exported as new XML node
	 * @param withHunks a boolean value, true if we want complete Hunk output, false if this is not desired.
	 * @return an Element "Patches" containing an XML Export of the given patches.
	 */
	public static final Element getXMLExportOfPatches(List<Patch> patches, boolean withHunks) {
		Element rootE = new Element("Patches");
		rootE.setAttribute(new Attribute("amount", Integer.toString(patches.size())));
		
		// Add each Patch to the JDOM Tree
		for (Patch patch : patches) {
			Element patchE = new Element("Patch");
			
			// Add the index
			Element indexE = new Element("index");
			indexE.setText(patch.getIndex());
			patchE.addContent(indexE);
			
			// Add the original File
			Element origE = new Element("original_file");
			origE.setText(patch.getOriginalFile());
			patchE.addContent(origE);
			
			// Add the modified File
			Element modE = new Element("modified_file");
			modE.setText(patch.getModifiedFile());
			patchE.addContent(modE);
			
			// Add the original header   <---- This is meant for Debugging
			//Element oheadE = new Element("original_header");
			//oheadE.setText(patch.getHeader());
			//patchE.addContent(oheadE);
			
			// Add the list of Patch Hunks if we want so
			if (withHunks) {
				Element hunksE = new Element("Hunks");
				for (PatchHunk hunk : patch.getHunks()) {
					Element hunkE = new Element("hunk");
					hunkE.setText(hunk.getText());
					hunksE.addContent(hunkE);
				}
				patchE.addContent(hunksE);
			}
			
			// Finally add the patch to the root Element
			rootE.addContent(patchE);
		}
		
		// Return the export
		return rootE;
	}
	
	
	/**
	 * Get an XML Export of a list of Source Code Regions (as provided by FilterSourceCode classes)
	 * @param coderegions a list of {@CodeRegion}s that should be exported as new XML node
	 * @param withCode a boolean value, true if we want complete source code text, false if this is not desired.
	 * @return an Element "Source Code Regions" containing an XML Export of the given Code Regions.
	 */
	public static final Element getXMLExportOfSourceCode(List<CodeRegion> coderegions, boolean withCode) {
		Element rootE = new Element("SourceCodeRegions");
		rootE.setAttribute(new Attribute("amount", Integer.toString(coderegions.size())));
		
		for (CodeRegion region : coderegions) {
			Element regionE = new Element("source_code");
			regionE.setAttribute(new Attribute("type", region.keyword));
			
			Element locationE = new Element("location");
			locationE.setAttribute(new Attribute("start", Integer.toString(region.start)));
			locationE.setAttribute(new Attribute("end", Integer.toString(region.end)));
			
			regionE.addContent(locationE);
			
			if (withCode) {
				Element codeE = new Element("code");
				codeE.setText(region.text);
				regionE.addContent(codeE);
			}
			
			rootE.addContent(regionE);
		}
		return rootE;
	}

	
	/**
	 * Get an XML Export of a list of Enumeration (as provided by the FilterEnumeration classes)
	 * @param enumerations a list of {@link Enumeration}s that should be exported as new XML node
	 * @param withLines a boolean value, true if we want complete enumeration lines 
	 * @return an Element "Enumerations" containing an XML Export of the given Enumerations
	 */
	public static final Element getXMLExportOfEnumerations(List<Enumeration> enumerations, boolean withLines) {
		Element rootE = new Element("Enumerations");
		rootE.setAttribute(new Attribute("amount", Integer.toString(enumerations.size())));
		
		for (Enumeration enu : enumerations) {
			Element enumE = new Element("Enumeration");
			enumE.setAttribute(new Attribute("lines", Integer.toString(enu.getEnumeration_items().size())));
			
			if (withLines) {
				Element linesE = new Element("Lines");
				for (String line : enu.getEnumeration_items()) {
					Element lineE = new Element("Line");
					lineE.setText(line);
					linesE.addContent(lineE);
				}
				enumE.addContent(linesE);
			}
			
			rootE.addContent(enumE);
		}
		
		return rootE;
	}
	
	
	/**
	 * Write a CSV line to a BufferedWriter stream
	 * @param writer	The writer to write to
	 * @param bug_id	the bug report id
	 * @param foundStackTrace	if a stacktrace was found
	 * @param foundPatch		if a patch was found
	 * @param foundSource		if source code was found
	 * @param foundEnum			if enumerations were found
	 */
	public static void writeCSV(BufferedWriter writer,
								int bug_id, 
								boolean foundStackTrace,
								boolean foundPatch,
								boolean foundSource,
								boolean foundEnum) {
		try {
			
			
			writer.write(Integer.toString(bug_id) + "," 
					+ getIntFromBoolean(foundStackTrace) + ","
					+ getIntFromBoolean(foundPatch) + ","
					+ getIntFromBoolean(foundSource) + ","
					+ getIntFromBoolean(foundEnum) + System.getProperty("line.separator"));
		} catch (IOException e) {
			System.out.println("Could not write CSV file!");
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Write the Results of the Duplicates analysis to a BufferedWriter Stream
	 * @param writer the writer to write to
	 * @param dupe_id the id of the duplicate report
	 * @param original_id the id of the coupled original report
	 * @param opa the amount of patches in the original report
	 * @param ost the amount of stack traces in the original report
	 * @param oso the amount of source code in the original report
	 * @param oen the amount of enumerations in the original report
	 * @param dpa the amount of patches in the duplicate report
	 * @param dst the amount of stack traces in the duplicate report
	 * @param dso the amount of source code in the duplicate report
	 * @param den the amount of enumerations in the duplicate report
	 */
	public static void writeCSVDuplicateAnalysis(BufferedWriter writer,
			int dupe_id, int original_id, int opa, int ost, int oso, int oen,
										  int dpa, int dst, int dso, int den) {
		try {
			
			writer.write(dupe_id + "," 
					+ original_id + ","
					+ opa + ","
					+ ost + ","
					+ oso + ","
					+ oen + ","
					+ dpa + ","
					+ dst + ","
					+ dso + ","
					+ den 
					+ System.getProperty("line.separator"));
			
		} catch (IOException e) {
			System.out.println("Could not write CSV file!");
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
	
	
	/**
	 * Helper Function to convert boolean to int
	 * @param b a boolean value
	 * @return 0 if false, 1 if true
	 */
	private static int getIntFromBoolean(boolean b) {
		if (b)
			return 1;
		else
			return 0;
	}
	
}
