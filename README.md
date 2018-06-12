# Confluent JMS POC

### How to run:
1. Make sure both Confluent enterprise and ActiveMQ are both installed and running on default ports. ActiveMQ download: http://activemq.apache.org/getting-started.html, confluent download: https://www.confluent.io/product/confluent-enterprise/
2. Ensure the queues (topics in confluent) `reply-queue`, `request-queue`, and `invalid-queue` are created in both ActiveMQ and Confluent
3. In one terminal `cd confluentjms-replier` and `mvn clean package`
4. In a second terminal `cd confluentjms-requestor` and `mvn clean package`
5. In the first terminal, start the replier `mvn exec:java`
6. In the second terminal, start the requestor `mvn exec:java`
7. The request `Hello World` is sent to an activemq queue, which is picked up by the replier and sent back to the requestor. Notice the matching correlation Ids.
8. Now, you can run both the same demo in Confluent/Kafka by simply changing the JNDI properties file.
9. Stop both the requestor and replier with `ctrl+c`
9. In both confluentjms-replier confluentjms-requestor and  directories, cd into ./src/main/resources
10. rename `jndi.properties` to any other name, ex: `jndi.properties-activemq`
11. rename `jndi.properties-confluent` to `jndi.properties`
12. Now re-run both the requestor and the replier and notice the same result, the broker was changed without changing any code besides the `jndi.properties` file!

### Note on implementation:

For some reason, Kafka decides to drop some commonly used JMS Headers such as correlationID and replyTo fields (**CONFLUENT CONFIRMS THIS IS A BUG**). In order to work around this, the messages have been sent as MapMessages instead of typical TextMessages. Within my map, I have embedded my custom correlationID and replyTo fields. This way I can implement request reply semantics with confluent even though the fields are dropped. 

### Use a TextMessage instead of a MapMessage

To use a TextMessage instead of a MapMessage, go to `/confluentjms-requestor/src/main/java/com/logimethods/App.java` and comment out line 35 and uncomment line 36. TextMessages use the traditional JMS header fields for correlationID and replyTo instead of the custom ones I have created within the map message. This change will not effect the program at all if you are using the ActiveMQ properties file. If you are using the confluent properties file the replier will send the message to the invalid queue since both the correlation Id and reply-to fields will be null!
