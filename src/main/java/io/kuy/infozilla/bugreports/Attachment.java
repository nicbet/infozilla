/**
 * Attachment.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.bugreports;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

public class Attachment {
	
	private String attachmentID;
	/**
	 * @return the attachmentID
	 */
	public String getAttachmentID() {
		return attachmentID;
	}

	/**
	 * @param attachmentID the attachmentID to set
	 */
	public void setAttachmentID(String attachmentID) {
		this.attachmentID = attachmentID;
	}

	private String description;
	private String mimetype;
	private String filename;
	private boolean isObsolete = false;
	private Timestamp creation_ts;
	private String type = null;
	private String magictype = null;
	private String encoding = null;
	private byte[] data;
	private int filesize = 0;
	
	/**
	 * Mime-Types believed to contain text
	 */
	private static Set<String> texttypes = new LinkedHashSet<String>(
		java.util.Arrays.asList("application/text" ,"text/*" ,"text/bash-script" ,"text/css" ,"text/csv" ,"text/csv file" ,"text/diff" ,"text/html" ,"text/java" ,"text/java source" ,"text/js" ,"text/log" ,"text/plain" ,"text/plain, text/file" ,"text/x-csrc" ,"text/x-csv" ,"text/x-diff" ,"text/x-java" ,"text/x-java-source" ,"text/x-log" ,"text/xml" ,"text/xml " ,"text/x-patch")	
	);
	
	// Need Magic Types from UNIX file util since values returned by Database tend to be wrong!
	private static Set<String> magictexttypes = new LinkedHashSet<String>(
			java.util.Arrays.asList("text/html" ,"text/plain" ,"text/plain\0118bit" ,"text/troff" ,"text/x-asm" ,"text/x-c" ,"text/x-c++" ,"text/x-java" ,"text/x-mail" ,"text/x-makefile" ,"text/x-news")	
		);
	
	public static boolean isTextType(String mimetype, String magictype) {
		return (Attachment.texttypes.contains(mimetype) && Attachment.magictexttypes.contains(magictype));
	}
	
	public boolean isText() {
		return (Attachment.texttypes.contains(mimetype) && Attachment.magictexttypes.contains(magictype));
		//return magictype.contains("text");
	}
	
	/**
	 * Mime-Types believed to be binary/image
	 */
	private static Set<String> imagetypes = new LinkedHashSet<String>(
		java.util.Arrays.asList("application/gif" ,"application/jpg" ,"application/pdf" ,"application/png" ,"application/postscript" ,"application/x-shockwave-flash" ,"image/bar" ,"image/bitmap" ,"image/bmp" ,"image/gif" ,"image/ico" ,"image/icon" ,"image/jpeg" ,"image/jpeg " ,"image/pjpeg" ,"image/png" ,"image/svg+xml" ,"image/tiff" ,"image/x-bmp" ,"image/x-emf" ,"image/x-icon" ,"image/x-png" ,"image/x-portable-bitmap" ,"image/x-psd" ,"image/x-windows-bmp" ,"image/zip" ,"video/avi" ,"video/quicktime")
	);
	
	private static Set<String> magicimagetypes = new LinkedHashSet<String>(
			java.util.Arrays.asList("application/x-shockwave-flash" ,"image/gif" ,"image/jpeg" ,"image/png" ,"image/tiff" ,"image/x-ms-bmp" ,"image/x-photoshop" ,"image/x-portable-bitmap" ,"image/x-portable-bitmap\0117bit" ,"image/x-xpmi" ,"video/quicktime" ,"video/unknown" ,"video/x-msvideo")
		);
	
	public static boolean isImageType(String mimetype, String magictype) {
		return (Attachment.imagetypes.contains(mimetype) && Attachment.magicimagetypes.contains(magictype));
	}
	
	public boolean isImage() {
		return (Attachment.imagetypes.contains(mimetype) && Attachment.magicimagetypes.contains(magictype));
		//return magictype.contains("image");
	}
	
	/** Overloaded Constructor
	 * @param description	The attachment's descriptive text
	 * @param mimetype		The mime-type of the attachment
	 * @param filename		The original filename
	 * @param creation_ts	The timestamp of the attachment's submission
	 * @param type			The attachments TYPE: SCREENSHOT, PATCH, STACKTRACE, SOURCECODE
	 * @param data			The binary Data of the attachment
	 */
	public Attachment(String description, String mimetype, String filename,
			Timestamp creation_ts, String type, byte[] data, int filesize) {
		super();
		this.description = description;
		this.mimetype = mimetype;
		this.filename = filename;
		this.creation_ts = creation_ts;
		this.type = type;
		this.data = data;
		this.filesize = filesize;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @return the mimetype
	 */
	public String getMimetype() {
		return mimetype;
	}
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}
	/**
	 * @return the creation_ts
	 */
	public Timestamp getCreation_ts() {
		return creation_ts;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		if (type ==null)
			guessType();
		return type;
	}
	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @param data the data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
	}
	
	/**
	 * Try to guess the type of the attachment using some basic heuristics on mimetype and description
	 * @return true if the type was guessed, false otherwise
	 */
	public boolean guessType() {
		boolean wasAbleToGuess = false;
		String guessedType = "";
		
		// Rules for Screenshot
		if (mimetype.contains("image") 
				|| mimetype.contains("jpg") 
				|| mimetype.contains("gif") 
				|| mimetype.contains("jpeg")
				|| mimetype.contains("bmp")
				|| mimetype.contains("png")
				|| mimetype.contains("video")
				|| description.toLowerCase().contains("screenshot")
				|| filename.toLowerCase().contains("screenshot"))
			guessedType = "SCREENSHOT";
		
		// Rules for Patches
		if (mimetype.contains("patch") 
				|| mimetype.contains("diff")
				|| description.toLowerCase().contains("patch")
				|| description.toLowerCase().contains("diff")
				|| filename.toLowerCase().contains("patch")
				|| filename.toLowerCase().contains("diff"))
			guessedType = "PATCH";
		
		// Rules for Source Code
		if (mimetype.contains("text/java")
				|| mimetype.contains("text/java source")
				|| mimetype.contains("text/x-java")
				|| mimetype.contains("text/x-java source")
				|| (description.toLowerCase().contains("source") && description.toLowerCase().contains("code"))
				|| filename.toLowerCase().contains(".java"))
			guessedType = "SOURCECODE";
		
		// Rules for Stack Traces
		if (mimetype.contains("text/log") 
				|| mimetype.contains("text/x-log")
				|| (description.toLowerCase().contains("stack") && description.toLowerCase().contains("trace"))
				|| filename.toLowerCase().contains("stacktrace"))
			guessedType = "STACKTRACE";
		
		if (guessedType != "") {
			this.type = guessedType;
			wasAbleToGuess = true;
		}
		
		return wasAbleToGuess;
	}
	
	public String toString() {
		try {
			String output = new String(data, "UTF-8");
			return output;
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	/**
	 * @return the filesize
	 */
	public int getFilesize() {
		return filesize;
	}

	/**
	 * @return the magictype
	 */
	public String getMagictype() {
		return magictype;
	}

	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @param magictype the magictype to set
	 */
	public void setMagictype(String magictype) {
		this.magictype = magictype;
	}

	/**
	 * @param encoding the encoding to set
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public boolean isObsolete() {
		return isObsolete;
	}

	public void setObsolete(boolean isObsolete) {
		this.isObsolete = isObsolete;
	}
	
}
