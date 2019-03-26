package com.jd.journalq.service.impl;

import com.alibaba.fastjson.JSON;
import com.jd.journalq.model.ListQuery;
import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.convert.CodeConverter;
import com.jd.journalq.model.domain.*;
import com.jd.journalq.model.query.QApplication;
import com.jd.journalq.model.query.QConsumer;
import com.jd.journalq.service.ApplicationService;
import com.jd.journalq.service.ConsumerService;
import com.jd.journalq.nsr.ConsumerNameServerService;
import com.jd.journalq.nsr.TopicNameServerService;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.util.LocalSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("consumerService")
public class ConsumerServiceImpl  implements ConsumerService {
    private final Logger logger = LoggerFactory.getLogger(ConsumerServiceImpl.class);

    @Autowired
    private ApplicationService applicationService;


    @Autowired
    private TopicNameServerService topicNameServerService;

    @Autowired
    private ConsumerNameServerService consumerNameServerService;

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public int add(Consumer consumer) {
        Preconditions.checkArgument(consumer!=null && consumer.getTopic()!=null, "invalid consumer arg");

        try {
            //Find topic
            Topic topic = topicNameServerService.findById(consumer.getTopic().getId());
            consumer.setTopic(topic);
            consumer.setNamespace(topic.getNamespace());

            return consumerNameServerService.add(consumer);
        }catch (Exception e){
            String errorMsg = String.format("add consumer with nameServer failed, consumer is %s.", JSON.toJSONString(consumer));
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);//回滚
        }
    }

    @Override
    public Consumer findById(String s) throws Exception {
        return consumerNameServerService.findById(s);
    }

    @Override
    public PageResult<Consumer> findByQuery(QPageQuery<QConsumer> query) throws Exception {
        User user = LocalSession.getSession().getUser();
        if (query.getQuery() != null && query.getQuery().getApp() != null){
            query.getQuery().setReferer(query.getQuery().getApp().getCode());
            query.getQuery().setApp(null);
        }
        if (user.getRole() == User.UserRole.NORMAL.value()) {
            QApplication qApplication = new QApplication();
            qApplication.setUserId(user.getId());
            qApplication.setAdmin(false);
            List<Application> applicationList = applicationService.findByQuery(new ListQuery<>(qApplication));
            if (applicationList == null || applicationList.size() <=0 ) return PageResult.empty();
            List<String> appCodes = applicationList.stream().map(application -> application.getCode()).collect(Collectors.toList());
            query.getQuery().setAppList(appCodes);
        }
        return consumerNameServerService.findByQuery(query);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public int delete(Consumer consumer) {
        //Validate
        checkArgument(consumer);
        try {
            consumerNameServerService.delete(consumer);
        }catch (Exception e){
            String errorMsg = String.format("remove consumer status by nameServer failed, consumer is %s.", JSON.toJSONString(consumer));
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);//回滚
        }
        return 1;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public int update(Consumer consumer) {
        //Validate
        checkArgument(consumer);
		try {
            consumerNameServerService.update(consumer);
        }catch (Exception e){
            String errorMsg = String.format("update consumer by nameServer failed, consumer is %s.", JSON.toJSONString(consumer));
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);//回滚
        }
        return 1;
    }

    @Override
    public List<Consumer> findByQuery(QConsumer query) throws Exception {
        return consumerNameServerService.findByQuery(query);
    }

    @Override
    public Consumer findByTopicAppGroup(String namespace, String topic, String app, String group) {
        try {
            QConsumer qConsumer = new QConsumer();
            qConsumer.setReferer(app);
            //consumer表没存group
            if (group !=null) {
                qConsumer.setApp(new Identity(CodeConverter.convertApp(new Identity(app), group)));
            }
            qConsumer.setNamespace(namespace);
            qConsumer.setTopic(new Topic(topic));
            List<Consumer> consumerList = consumerNameServerService.findByQuery(qConsumer);
            if (consumerList == null || consumerList.size() <= 0)return null;
            return consumerList.get(0);
        } catch (Exception e) {
            logger.error("findByTopicAppGroup error",e);
            throw new RuntimeException("findByTopicAppGroup error",e);
        }
    }

    @Override
    public List<String> findAllSubscribeGroups() {
        try {
            return consumerNameServerService.findAllSubscribeGroups();
        } catch (Exception e) {
            logger.error("findAllSubscribeGroups error",e);
            throw new RuntimeException("findAllSubscribeGroups error",e);
        }
    }


    private void checkArgument(Consumer consumer) {
        Preconditions.checkArgument(consumer != null && consumer.getId() != null, "invalidate consumer arg.");
    }

}