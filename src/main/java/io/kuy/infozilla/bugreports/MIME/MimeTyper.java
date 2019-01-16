/**
 * MimeTyper.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.bugreports.MIME;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.kuy.infozilla.bugreports.Attachment;
import io.kuy.infozilla.datasources.bugzilladb.DatabaseConnector;

public class MimeTyper {
	
	public static Map<String, String> discoverMimeTypesOfBugReport(int bug_id, DatabaseConnector dbc) {
		Map<String, String> mimeTypes = new HashMap<String, String>();
		//System.out.println("-- Processing Bug Report " + bug_id);
		try {
			List<Attachment> attachments = dbc.getRAWAttachmentsFor(bug_id);
			for (Attachment attachment : attachments) {
				String attmtFileName = attachment.getFilename();
				int lastSeparator = attmtFileName.lastIndexOf(".");
				String prefix = "";
				String suffix = "";
				if (lastSeparator > 0) {
					prefix = attmtFileName.substring(0, lastSeparator);
					suffix = attmtFileName.substring(lastSeparator, attmtFileName.length());
				} else  {
					prefix = attmtFileName;
				}
				
				prefix = prefix.replaceAll("[ \\t\\n\\r]", "_");
				suffix = suffix.replaceAll("[ \\t\\n\\r]", "_");
				
				File attmtFile = File.createTempFile(attachment.getAttachmentID() + "_" + prefix, suffix);
				//System.out.println("-- Creating Temporary File: " + attmtFile.getCanonicalPath());
				FileOutputStream fos = new FileOutputStream(attmtFile);
				fos.write(attachment.getData());
				fos.close();
				String command = "file -b -i -n ";
				if (System.getProperty("os.name").contains("Mac OS"))
					command = "file -b -I -n ";
				System.out.println("-- " + command + attmtFile.getCanonicalPath());
				String xx = invokeCommand(command + attmtFile.getCanonicalPath());
				//System.out.println("--" + xx);
				mimeTypes.put(attachment.getAttachmentID(), xx);	
				attmtFile.delete();
			}
		} catch (Exception e) {
			System.err.println("Error Guessing Mime Types for Attachments of bug " + bug_id);
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		return mimeTypes;
	}
	
	public static String invokeCommand(String command) {
		String s;
		StringBuilder results = new StringBuilder();
		try {
            
		    // run the Unix "ps -ef" command
	            // using the Runtime exec method:
	            Process p = Runtime.getRuntime().exec(command);
	            
	            BufferedReader stdInput = new BufferedReader(new 
	                 InputStreamReader(p.getInputStream()));

	            BufferedReader stdError = new BufferedReader(new 
	                 InputStreamReader(p.getErrorStream()));

	            // read the output from the command
	            while ((s = stdInput.readLine()) != null) {
	                results.append(s);
	                results.append(System.getProperty("line.separator"));
	            }
	            
	            // read any errors from the attempted command
	            while ((s = stdError.readLine()) != null) {
	            	   results.append(s);
		                results.append(System.getProperty("line.separator"));
	            }
	            
	            p.destroy();
	        }
	        catch (IOException e) {
	            System.out.println("Error executing command " + command + " :");
	            e.printStackTrace();
	        }
	        return results.toString();
	}
	
	
	
}
