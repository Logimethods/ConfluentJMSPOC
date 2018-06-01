package com.logimethods; /**
 * Created by William Campoli on 2018-05-30.
 */
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.MapMessage;
import javax.naming.*;


public class App {

    public static void main(String[] args) throws JMSException, NamingException{
        Context ctx = new InitialContext();

        ConnectionFactory connectionFactory = (ConnectionFactory)ctx.lookup("ConnectionFactory");
        Connection connection = connectionFactory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        
        String requestQueue = "request-queue";
        String replyQueue = "reply-queue";
        String invalidQueue = "invalid-queue";
        connection.start();
        try {
            Requestor myRequestor = Requestor.newRequestor(session, requestQueue, replyQueue, invalidQueue, ctx);
          
            myRequestor.send();
            

            while(true){
                myRequestor.receiveSync();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    
    }
}
