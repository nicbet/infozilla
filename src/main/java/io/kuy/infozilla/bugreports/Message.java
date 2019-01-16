/**
 * Message.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.bugreports;

import java.sql.Timestamp;

/**
 * The Message Class describes one Message in the complete Discussion of a Bug Report.
 * @author Nicolas Bettenburg
 */
public class Message implements Comparable<Message>{
	
	private String contact;
	private Timestamp time;
	private String text;
	
	/**
	 * Overloaded Constructor
	 * @param contact	the person that submitted the message.
	 * @param time		the time and date, the message was submitted as java.util.Timestamp.
	 * @param text		the message text.
	 */
	public Message(String contact, Timestamp time, String text) {
		super();
		this.contact = contact;
		this.time = time;
		this.text = text;
	}

	/**
	 * This method is used to get the contact information of the person, who wrote the message.
	 * @return a String describing the author of the message.
	 */
	public String getContact() {
		return contact;
	}

	/**
	 * This method is used to set the name or email address of the message's author
	 * @param contact a String containing the name or the address of the author.
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}


	/**
	 * This method is used to get information about the time at which this message was submitted.
	 * @return a Timestamp of message creation in the bug database.
	 */
	public Timestamp getTime() {
		return time;
	}



	public void setTime(Timestamp time) {
		this.time = time;
	}


	
	public String getText() {
		return text;
	}



	public void setText(String text) {
		this.text = text;
	}



	public int compareTo(Message that) {
		if (this.getTime().after(that.getTime())) {
			return 1;
		} else if (this.getTime().before(that.getTime())) {
			return -1;
		} else {
			return 0;
		}
	}
	

}
