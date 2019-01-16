/**
 * MBox.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.datasources.mbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class MBox {

	String filename;
	List<Message> messages = null;
	
	/**
	 * Nested class describing a Message in the MBOX file
	 * @author nicbet
	 *
	 */
	public class Message {
		private String from;
		private String text;
		public Message(String from, String text) {
			super();
			this.from = from;
			this.text = text;
		}
		public String getFrom() {
			return from;
		}
		public void setFrom(String from) {
			this.from = from;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
	}
	
	/**
	 * Read the messages from the MBOX file described by this class
	 * @return a List of Messages
	 */
	public List<Message> readMessages() {
		messages = new ArrayList<Message>();
		
		String mbox_text = "";
		
		// Read the file
		try {
			File f = new File(filename);
			BufferedReader reader = new BufferedReader(new FileReader(f));
			StringBuilder pb = new StringBuilder();
			String buffer = "";
			while ((buffer = reader.readLine()) != null) {
				pb.append(buffer + System.getProperty("line.separator"));
			}
			
			mbox_text = pb.toString();
			reader.close();
		} catch (IOException e) {
			System.out.println("Error opening the MBOX file: " + e.getMessage());
		}
		
		// Split the mbox text into messages
		String[] pMessages = mbox_text.split("[\n\r]{2}From");
		
		
		// Make messages
		for (String pMessage : pMessages) {
			Message aMessage = new Message("",pMessage);
			messages.add(aMessage);
		}
		return messages;
	}

	/**
	 * Standard Constructor
	 * @param filename the path and filename of the MBOX file
	 */
	public MBox(String filename) {
		super();
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
}
