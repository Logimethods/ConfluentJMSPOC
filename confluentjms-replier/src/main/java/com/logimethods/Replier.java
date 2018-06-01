package com.logimethods;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import javax.jms.MapMessage;
import javax.naming.*;

public class Replier{

	private Session session;
    private MessageProducer invalidProducer;
	private MessageConsumer requestConsumer;
	private Context ctx;

	protected Replier() {
		super();
	}

	public static Replier newReplier(Session session, String requestQueue, String invalidQueue, Context ctx)
		throws JMSException, NamingException {

		Replier replier = new Replier();
		replier.initialize(session, requestQueue, invalidQueue, ctx);
		return replier;
	}

	protected void initialize(Session pSession, String pRequestQueue, String pInvalidQueue, Context pCtx)
		throws NamingException, JMSException {
		
		this.ctx = pCtx;
		this.session = pSession;
		
	    
		Destination requestQueue = (Destination)pCtx.lookup(pRequestQueue);
		Destination invalidQueue = (Destination)pCtx.lookup(pInvalidQueue);

		this.requestConsumer = session.createConsumer(requestQueue);
		this.invalidProducer = session.createProducer(invalidQueue);
	}

	public void reply() {
        
		try {
            MapMessage requestMessage = (MapMessage)requestConsumer.receive();
			if (requestMessage.getString("replyQueue") != null) {
			
				System.out.println("Received request");
				System.out.println("\tTime:       " + System.currentTimeMillis() + " ms");
				System.out.println("\tMessage ID: " + requestMessage.getJMSMessageID());
				System.out.println("\tCorrel. ID: " + requestMessage.getString("correlationID"));
				System.out.println("\tReply to:   " + requestMessage.getString("replyQueue"));
				System.out.println("\tContents:   " + requestMessage.getString("content"));

				String contents = requestMessage.getString("content");
				/*change this to use jndi context*/
				Destination replyDestination = (Destination)ctx.lookup(requestMessage.getString("replyQueue"));
				System.out.println(replyDestination.toString());
				MessageProducer replyProducer = session.createProducer(replyDestination);

				MapMessage replyMessage = session.createMapMessage();
				replyMessage.setString("content", contents);
				replyMessage.setString("correlationID",requestMessage.getString("correlationID"));
				//replyMessage.setString("replyQueue",requestMessage.getString("replyQueue"));
				replyProducer.send(replyMessage);

				System.out.println("Sent reply");
				System.out.println("\tTime:       " + System.currentTimeMillis() + " ms");
				System.out.println("\tMessage ID: " + replyMessage.getJMSMessageID());
				System.out.println("\tCorrel. ID: " + replyMessage.getString("correlationID"));
				System.out.println("\tReply to:   " + replyMessage.getString("replyQueue"));
				System.out.println("\tContents:   " + replyMessage.getString("content"));
			} else {
				System.out.println("Invalid message detected");
				System.out.println("\tType:       " + requestMessage.getClass().getName());
				System.out.println("\tTime:       " + System.currentTimeMillis() + " ms");
				System.out.println("\tMessage ID: " + requestMessage.getJMSMessageID());
				//System.out.println("\tCorrel. ID: " + msg.getJMSCorrelationID());
				//System.out.println("\tReply to:   " + msg.getJMSReplyTo());

				//msg.setJMSCorrelationID(msg.getJMSMessageID());
				invalidProducer.send(requestMessage);

				System.out.println("Sent to invalid message queue");
				System.out.println("\tType:       " + requestMessage.getClass().getName());
				System.out.println("\tTime:       " + System.currentTimeMillis() + " ms");
				System.out.println("\tMessage ID: " + requestMessage.getJMSMessageID());
				//System.out.println("\tCorrel. ID: " + msg.getJMSCorrelationID());
				//System.out.println("\tReply to:   " + msg.getJMSReplyTo());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}