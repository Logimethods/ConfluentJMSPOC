package com.logimethods;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import javax.jms.MapMessage;
import javax.naming.*;

import java.util.UUID;

public class Requestor {

	private Session session;
	private String replyQueue;
	private MessageProducer requestProducer;
	private MessageConsumer replyConsumer;
	private MessageProducer invalidProducer;

	protected Requestor() {
		super();
	}

	public static Requestor newRequestor(Session session, String requestQueue,
		String replyQueue, String invalidQueue, Context ctx)
		throws JMSException, NamingException {
			
		Requestor requestor = new Requestor();
		requestor.initialize(session, requestQueue, replyQueue, invalidQueue, ctx);
		return requestor;
	}

	protected void initialize(Session pSession, String pRequestQueue,
		String pReplyQueue, String pInvalidQueue, Context pCtx)
		throws NamingException, JMSException {
			
		//this.session = pConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        this.session = pSession;
        
		Destination requestQueue = (Destination)pCtx.lookup(pRequestQueue);
		this.replyQueue = pReplyQueue;
		Destination invalidQueue = (Destination)pCtx.lookup(pInvalidQueue);
		
		this.requestProducer = session.createProducer(requestQueue);
		Destination replyQueueDestination = (Destination)pCtx.lookup(pReplyQueue);
		System.out.println("replyqueue destination: "+replyQueueDestination);
		this.replyConsumer = session.createConsumer(replyQueueDestination);
		this.invalidProducer = session.createProducer(invalidQueue);
	}

	public void send() throws JMSException {
        MapMessage requestMessage = session.createMapMessage();
        requestMessage.setString("correlationID",UUID.randomUUID().toString());
		requestMessage.setString("content", "Hello world.");
		requestMessage.setString("replyQueue",replyQueue.toString());
		requestProducer.send(requestMessage);
		System.out.println("Sent request");
		System.out.println("\tTime:       " + System.currentTimeMillis() + " ms");
		System.out.println("\tMessage ID: " + requestMessage.getJMSMessageID());
		System.out.println("\tCorrel. ID: " + requestMessage.getString("correlationID"));
		System.out.println("\tReply to:   " + requestMessage.getString("replyQueue"));
		System.out.println("\tContents:   " + requestMessage.getString("content"));
	}

	public void receiveSync() throws JMSException {
		System.out.println("recieving Reply...");
		MapMessage msg = (MapMessage)replyConsumer.receive();
		if (msg instanceof MapMessage) {
			MapMessage replyMessage = (MapMessage) msg;
			System.out.println("Received reply ");
			System.out.println("\tTime:       " + System.currentTimeMillis() + " ms");
			System.out.println("\tMessage ID: " + replyMessage.getJMSMessageID());
			System.out.println("\tCorrel. ID: " + replyMessage.getString("correlationID"));
			System.out.println("\tReply to:   " + replyMessage.getString("replyQueue"));
			System.out.println("\tContents:   " + replyMessage.getString("content"));
		} else {
			System.out.println("Invalid message detected");
			System.out.println("\tType:       " + msg.getClass().getName());
			System.out.println("\tTime:       " + System.currentTimeMillis() + " ms");
			System.out.println("\tMessage ID: " + msg.getJMSMessageID());
			//System.out.println("\tCorrel. ID: " + msg.getJMSCorrelationID());
			//System.out.println("\tReply to:   " + msg.getJMSReplyTo());

			//msg.setJMSCorrelationID(msg.getJMSMessageID());
			invalidProducer.send(msg);

			System.out.println("Sent to invalid message queue");
			System.out.println("\tType:       " + msg.getClass().getName());
			System.out.println("\tTime:       " + System.currentTimeMillis() + " ms");
			System.out.println("\tMessage ID: " + msg.getJMSMessageID());
			//System.out.println("\tCorrel. ID: " + msg.getJMSCorrelationID());
			//System.out.println("\tReply to:   " + msg.getJMSReplyTo());
		}
	}
}