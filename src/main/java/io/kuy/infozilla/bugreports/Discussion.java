/**
 * Discussion.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.bugreports;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;


public class Discussion {

	private PriorityQueue<Message> messageQueue = null;

	public Discussion() {
		messageQueue = new PriorityQueue<Message>();
	}
	
	public void addMessage(Message m) {
		messageQueue.add(m);
	}
	
	public List<Message> getMessages() {
		// Copy Messages
		PriorityQueue<Message> tmpQueue = new PriorityQueue<Message>(messageQueue);
		
		List<Message> msgList = new ArrayList<Message>();
		while(! tmpQueue.isEmpty()) {
			Message cmd = tmpQueue.poll();
			msgList.add(cmd);
		}
		return msgList;
	}
	
	public List<Message> getMessages(Timestamp untilDate) {
		// Copy Messages
		PriorityQueue<Message> tmpQueue = new PriorityQueue<Message>(messageQueue);
		
		List<Message> msgList = new ArrayList<Message>();
		while(! tmpQueue.isEmpty()) {
			Message m = tmpQueue.poll();
			if (m.getTime().before(untilDate)) {
				msgList.add(m);
			}
		}
		return msgList;
	}
	
	public Message getDescription() {
		return messageQueue.peek();
	}
	
}
