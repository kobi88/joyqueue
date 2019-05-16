/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openmessaging.journalq;

import com.jd.journalq.client.internal.MessageAccessPoint;
import com.jd.journalq.client.internal.MessageAccessPointFactory;
import com.jd.journalq.client.internal.consumer.MessageConsumer;
import com.jd.journalq.client.internal.consumer.config.ConsumerConfig;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.producer.MessageProducer;
import com.jd.journalq.client.internal.producer.config.ProducerConfig;
import com.jd.journalq.client.internal.producer.feedback.config.TxFeedbackConfig;
import com.jd.journalq.client.internal.transport.config.TransportConfig;
import com.jd.journalq.exception.JournalqCode;
import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.consumer.Consumer;
import io.openmessaging.exception.OMSUnsupportException;
import io.openmessaging.journalq.config.ExceptionConverter;
import io.openmessaging.journalq.config.KeyValueConverter;
import io.openmessaging.journalq.consumer.support.ConsumerImpl;
import io.openmessaging.journalq.producer.message.MessageFactoryAdapter;
import io.openmessaging.journalq.producer.support.ProducerImpl;
import io.openmessaging.journalq.producer.support.TransactionProducerImpl;
import io.openmessaging.journalq.support.ConsumerWrapper;
import io.openmessaging.journalq.support.MessageAccessPointHolder;
import io.openmessaging.journalq.support.ProducerWrapper;
import io.openmessaging.manager.ResourceManager;
import io.openmessaging.message.MessageFactory;
import io.openmessaging.producer.Producer;
import io.openmessaging.producer.TransactionStateCheckListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MessagingAccessPointImpl
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/18
 */
public class MessagingAccessPointImpl implements MessagingAccessPoint {

    protected final Logger logger = LoggerFactory.getLogger(MessagingAccessPointImpl.class);

    private KeyValue attributes;

    private NameServerConfig nameServerConfig;
    private TransportConfig transportConfig;
    private ProducerConfig producerConfig;
    private ConsumerConfig consumerConfig;
    private TxFeedbackConfig txFeedbackConfig;
    private MessageFactory messageFactory;
    private MessageAccessPointHolder messageAccessPointHolder;

    public MessagingAccessPointImpl(KeyValue attributes) {
        this.attributes = attributes;
        this.nameServerConfig = KeyValueConverter.convertNameServerConfig(attributes);
        this.transportConfig = KeyValueConverter.convertTransportConfig(attributes);
        this.producerConfig = KeyValueConverter.convertProducerConfig(nameServerConfig, attributes);
        this.consumerConfig = KeyValueConverter.convertConsumerConfig(nameServerConfig, attributes);
        this.txFeedbackConfig = KeyValueConverter.convertFeedbackConfig(nameServerConfig, attributes);
        this.messageFactory = createMessageFactory();
    }

    protected MessageFactory createMessageFactory() {
        return new MessageFactoryAdapter();
    }

    @Override
    public synchronized Producer createProducer() {
        MessageAccessPointHolder messageAccessPointHolder = getOrCreateMessageAccessPointHolder();
        MessageAccessPoint messageAccessPoint = messageAccessPointHolder.getMessageAccessPoint();
        MessageProducer messageProducer = messageAccessPoint.createProducer(producerConfig);
        ProducerImpl producer = new ProducerImpl(messageProducer, messageFactory);
        return new ProducerWrapper(producer, messageAccessPointHolder);
    }

    @Override
    public synchronized Producer createProducer(TransactionStateCheckListener transactionStateCheckListener) {
        MessageAccessPointHolder messageAccessPointHolder = getOrCreateMessageAccessPointHolder();
        MessageAccessPoint messageAccessPoint = messageAccessPointHolder.getMessageAccessPoint();
        MessageProducer messageProducer = messageAccessPointHolder.getMessageAccessPoint().createProducer(producerConfig);
        ProducerImpl producer = new ProducerImpl(messageProducer, messageFactory);
        TransactionProducerImpl transactionProducer = new TransactionProducerImpl(producer, transactionStateCheckListener, messageProducer, messageAccessPoint, txFeedbackConfig);
        return new ProducerWrapper(transactionProducer, messageAccessPointHolder);
    }

    @Override
    public synchronized Consumer createConsumer() {
        MessageAccessPointHolder messageAccessPointHolder = getOrCreateMessageAccessPointHolder();
        MessageAccessPoint messageAccessPoint = messageAccessPointHolder.getMessageAccessPoint();
        MessageConsumer messageConsumer = messageAccessPoint.createConsumer(consumerConfig);
        ConsumerImpl consumer = new ConsumerImpl(messageConsumer);
        return new ConsumerWrapper(consumer, messageAccessPointHolder);
    }

    protected MessageAccessPointHolder getOrCreateMessageAccessPointHolder() {
        if (messageAccessPointHolder != null && messageAccessPointHolder.getMessageAccessPoint().isStarted()) {
            return messageAccessPointHolder;
        }

        try {
            MessageAccessPoint messageAccessPoint = MessageAccessPointFactory.create(nameServerConfig, transportConfig);
            messageAccessPoint.start();
            messageAccessPointHolder = new MessageAccessPointHolder(messageAccessPoint);
        } catch (Exception e) {
            logger.error("create messagingAccessPoint exception", e);
            throw ExceptionConverter.convertRuntimeException(e);
        }
        return messageAccessPointHolder;
    }

    @Override
    public ResourceManager resourceManager() {
        throw new OMSUnsupportException(JournalqCode.CN_COMMAND_UNSUPPORTED.getCode(), "resourceManager is not supported");
    }

    @Override
    public MessageFactory messageFactory() {
        return messageFactory;
    }

    @Override
    public KeyValue attributes() {
        return attributes;
    }

    @Override
    public String version() {
        return JournalQOMSConsts.VERSION;
    }
}