/**
 * Most of the code in the Qalingo project is copyrighted Hoteia and licensed
 * under the Apache License Version 2.0 (release version 0.8.0)
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *                   Copyright (c) Hoteia, 2012-2014
 * http://www.hoteia.com - http://twitter.com/hoteia - contact@hoteia.com
 *
 */
package org.hoteia.qalingo.core.jms.cacheserveurmanagement.producer;

import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

@Component(value = "cacheServeurManagementMessageProducer")
public class CacheServeurManagementMessageProducer {

    protected final Log logger = LogFactory.getLog(getClass());

    @Resource(name="cacheServeurManagementJmsTemplate")
    private JmsTemplate jmsTemplate;

    /**
     * Generates JMS messages
     * 
     * @throws UnsupportedEncodingException
     */
    public void generateMessages() throws JMSException, UnsupportedEncodingException {
        try {
            final String valueJMSMessage = "message JMS";
            jmsTemplate.send(new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    TextMessage message = session.createTextMessage(valueJMSMessage);
                    if (logger.isDebugEnabled()) {
                        logger.info("Sending JMS message: " + valueJMSMessage);
                    }
                    return message;
                }
            });
        } catch (Exception e) {
            logger.error("Exception during create/send message process");
        }
    }

}
