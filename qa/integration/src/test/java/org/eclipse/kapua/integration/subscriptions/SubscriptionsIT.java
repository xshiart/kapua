/*******************************************************************************
 * Copyright (c) 2019 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.integration.subscriptions;

import org.eclipse.kapua.qa.common.DBHelper;
import org.eclipse.kapua.qa.common.utils.EmbeddedBroker;
import org.eclipse.kapua.qa.common.utils.EmbeddedDatastore;
import org.eclipse.kapua.qa.common.utils.EmbeddedEventBroker;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionsIT {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionsIT.class);

    private String brokerUrl = "tcp://localhost:1883";
    private String usernameAdmin = "kapua-sys";
    private String passwordAdmin = "kapua-password";
    private String accountName = "kapua-sys";
    private String username = "kapua-broker";
    private String password = "kapua-password";
    private String qosTestClientId = "qos-client";
    private String qosTestClientIdAdmin = "qos-client-admin";

    private String[][] topicsAndSubscriptions = new String[][]{
        new String[]{"$EDC/" + accountName + "/" + qosTestClientId + "/topic1", "$EDC/" + accountName + "/" + qosTestClientId + "/topic1"},
        new String[]{"$EDC/" + accountName + "/" + qosTestClientId + "/topic1", "$EDC/" + accountName + "/#"},
        new String[]{"$EDC/" + accountName + "/" + qosTestClientId + "/topic1", "$EDC/" + accountName + "/+/+"},
        new String[]{"$EDC/" + accountName + "/" + qosTestClientId + "/topic1", "$EDC/" + accountName + "/" + qosTestClientId + "/+"},
        new String[]{"$EDC/" + accountName + "/" + qosTestClientId + "/topic1", "$EDC/" + accountName + "/" + qosTestClientId + "/#"}
    };

    protected MqttClient mqttClient;
    protected MqttClient mqttClientAdmin;

    //env
    private DBHelper db;
    private EmbeddedDatastore es;
    private EmbeddedBroker broker;
    private EmbeddedEventBroker eventBroker;

    public SubscriptionsIT() {
        db = new DBHelper();
        es = new EmbeddedDatastore();
        broker = new EmbeddedBroker();
        eventBroker = new EmbeddedEventBroker(db);
        es.setup();
        eventBroker.start();
        broker.start();
    }

    @After
    public void cleanUp() throws Exception {
        if (broker != null) {
            broker.stop();
        }
        if (eventBroker != null) {
            eventBroker.stop();
        }
    }

    @Test
    public void testDifferentQoS() throws Exception {
        checkSubscriptions();
        String willTopic = "$EDC/" + accountName + "/" + qosTestClientId + "/MQTT/LWT";
        String willMessage = "Will message from client " + qosTestClientId;
        mqttClient = new MqttClient(brokerUrl, qosTestClientId);
        MqttCallback clientCallback = new MqttCallback(qosTestClientId);
        mqttClient.setCallback(clientCallback);
        mqttClient.connect(getOptions(true, 10, MqttConnectOptions.MQTT_VERSION_3_1_1, username, password, willTopic, willMessage));

        String willTopicAdmin = "$EDC/" + accountName + "/" + qosTestClientIdAdmin + "/MQTT/LWT";
        String willMessageAdmin = "Will message from client " + qosTestClientIdAdmin;
        mqttClientAdmin = new MqttClient(brokerUrl, qosTestClientIdAdmin);
        MqttCallback clientAdminCallback = new MqttCallback(qosTestClientIdAdmin);
        mqttClientAdmin.setCallback(clientAdminCallback);
        mqttClientAdmin.connect(getOptions(true, 10, MqttConnectOptions.MQTT_VERSION_3_1_1, usernameAdmin, passwordAdmin, willTopicAdmin, willMessageAdmin));
        //
        for (String topic[] : topicsAndSubscriptions) {
            publishLoop(topic[1], topic[0], 0, qosTestClientId, clientAdminCallback);
        }
        for (String topic[] : topicsAndSubscriptions) {
            publishLoop(topic[1], topic[0], 1, qosTestClientId, clientAdminCallback);
        }
        for (String topic[] : topicsAndSubscriptions) {
            publishLoop(topic[1], topic[0], 2, qosTestClientId, clientAdminCallback);
        }
        //reconnect clients with clean session false
        mqttClient.disconnect();
        mqttClient = new MqttClient(brokerUrl, qosTestClientId);
        clientCallback.clean();
        mqttClient.setCallback(clientCallback);
        mqttClient.connect(getOptions(false, 10, MqttConnectOptions.MQTT_VERSION_3_1_1, username, password, willTopic, willMessage));
        mqttClientAdmin.disconnect();
        mqttClientAdmin = new MqttClient(brokerUrl, qosTestClientIdAdmin);
        clientAdminCallback.clean();
        mqttClientAdmin.setCallback(clientAdminCallback);
        mqttClientAdmin.connect(getOptions(false, 10, MqttConnectOptions.MQTT_VERSION_3_1_1, usernameAdmin, passwordAdmin, willTopicAdmin, willMessageAdmin));
        for (String topic[] : topicsAndSubscriptions) {
            publishLoop(topic[1], topic[0], 0, qosTestClientId, clientAdminCallback);
        }
        for (String topic[] : topicsAndSubscriptions) {
            publishLoop(topic[1], topic[0], 1, qosTestClientId, clientAdminCallback);
        }
        for (String topic[] : topicsAndSubscriptions) {
            publishLoop(topic[1], topic[0], 2, qosTestClientId, clientAdminCallback);
        }
        clientAdminCallback.printWrongTopicCount();
        logger.info("Topic sent count {} / wrong {}", topicsAndSubscriptions.length * 6, clientAdminCallback.getWrongTopicCount());
    }

    private void checkSubscriptions() throws Exception {
        String willTopic = "$EDC/" + accountName + "/" + qosTestClientId + "/MQTT/LWT";
        String willMessage = "Will message from client " + qosTestClientId;
        mqttClient = new MqttClient(brokerUrl, qosTestClientId);
        MqttCallback clientCallback = new MqttCallback(qosTestClientId);
        mqttClient.setCallback(clientCallback);
        mqttClient.connect(getOptions(true, 10, MqttConnectOptions.MQTT_VERSION_3_1_1, username, password, willTopic, willMessage));

        String willTopicAdmin = "$EDC/" + accountName + "/" + qosTestClientIdAdmin + "/MQTT/LWT";
        String willMessageAdmin = "Will message from client " + qosTestClientIdAdmin;
        mqttClientAdmin = new MqttClient(brokerUrl, qosTestClientIdAdmin);
        MqttCallback clientAdminCallback = new MqttCallback(qosTestClientIdAdmin);
        mqttClientAdmin.setCallback(clientAdminCallback);
        mqttClientAdmin.connect(getOptions(true, 10, MqttConnectOptions.MQTT_VERSION_3_1_1, usernameAdmin, passwordAdmin, willTopicAdmin, willMessageAdmin));
        //just subscription
        for (String topic[] : topicsAndSubscriptions) {
            subscriptionLoop(topic[1], 0);
        }
        for (String topic[] : topicsAndSubscriptions) {
            subscriptionLoop(topic[1], 1);
        }
        for (String topic[] : topicsAndSubscriptions) {
            subscriptionLoop(topic[1], 2);
        }
        //reconnect clients with clean session false
        mqttClient.disconnect();
        mqttClient = new MqttClient(brokerUrl, qosTestClientId);
        clientCallback.clean();
        mqttClient.setCallback(clientCallback);
        mqttClient.connect(getOptions(false, 10, MqttConnectOptions.MQTT_VERSION_3_1_1, username, password, willTopic, willMessage));

        mqttClientAdmin.disconnect();
        mqttClientAdmin = new MqttClient(brokerUrl, qosTestClientIdAdmin);
        clientAdminCallback.clean();
        mqttClientAdmin.setCallback(clientAdminCallback);
        mqttClientAdmin.connect(getOptions(false, 10, MqttConnectOptions.MQTT_VERSION_3_1_1, usernameAdmin, passwordAdmin, willTopicAdmin, willMessageAdmin));
        for (String topic[] : topicsAndSubscriptions) {
            subscriptionLoop(topic[1], 0);
        }
        for (String topic[] : topicsAndSubscriptions) {
            subscriptionLoop(topic[1], 1);
        }
        for (String topic[] : topicsAndSubscriptions) {
            subscriptionLoop(topic[1], 2);
        }
        mqttClientAdmin.disconnect();
        mqttClient.disconnect();
    }

    private void subscriptionLoop(String subscription, int qos) throws Exception {
        logger.info("Testing subscription {} (QoS {})", subscription, qos);
        try {
            mqttClientAdmin.subscribe(subscription, qos);
        }
        catch (Exception e) {
            logger.error("{}", e);
        }
        Thread.sleep(1000);
        try {
            mqttClientAdmin.unsubscribe(subscription);
        }
        catch (Exception e) {
            logger.error("{}", e);
        }
        Thread.sleep(1000);
    }

    private void publishLoop(String subscription, String topic, int qos, String clientId, MqttCallback clientAdminCallback) throws Exception {
        logger.info("Testing topic {} with subscription {} (QoS {})", topic, subscription, qos);
        try {
            mqttClientAdmin.subscribe(subscription, qos);
        }
        catch (Exception e) {
            logger.error("{}", e);
        }
        Thread.sleep(1000);
        clientAdminCallback.clean();
        String message = "Message from client " + clientId + " - " + System.currentTimeMillis();
        try {
            mqttClient.publish(topic, message.getBytes(), 1, false);
        }
        catch (Exception e) {
            logger.error("{}", e);
        }
        Thread.sleep(1000);
        clientAdminCallback.check(topic, message, 1);
        try {
            mqttClientAdmin.unsubscribe(subscription);
        }
        catch (Exception e) {
            logger.error("{}", e);
        }
        Thread.sleep(1000);
        clientAdminCallback.clean();
        try {
            mqttClient.publish(topic, message.getBytes(), 1, false);
        }
        catch (Exception e) {
            logger.error("{}", e);
        }
        Thread.sleep(1000);
        clientAdminCallback.check(null, null, 0);
    }

    private MqttConnectOptions getOptions(boolean cleanSession, int keepAliveInterval, int mqttVersion, String userName, String password, String willTopic, String willMessage) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(cleanSession);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setMqttVersion(mqttVersion);
        options.setPassword(password.toCharArray());
        options.setUserName(userName);
        options.setWill(willTopic, willMessage.getBytes(), 1, false);
        return options;
    }
}

class MqttCallback implements org.eclipse.paho.client.mqttv3.MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(MqttCallback.class);
    private String result;
    private byte[] byteResult;
    private int resultCounter;
    private String arrivedTopic;
    private String clientId;
    private int wrongTopic;

    public MqttCallback(String clientId) {
        this.clientId = clientId;
    }

    public void resetCounter() {
        resultCounter = 0;
    }

    public String getArrivedTopic() {
        return arrivedTopic;
    }

    public int getResultCounter() {
        return resultCounter;
    }

    public String getResult() {
        return result;
    }

    public void cleanResult() {
        result = null;
    }

    public byte[] getByteResult() {
        return byteResult;
    }

    public int getWrongTopicCount() {
        return wrongTopic;
    }

    public void clean() {
        result = null;
        resultCounter = 0;
        byteResult = null;
        arrivedTopic = null;
    }

    public void check(String topic, String message, int count) {
        if (topic!=null) {
            if (!topic.equals(arrivedTopic)) {
                wrongTopic++;
            }
        }
        else if (arrivedTopic!=null) {
            wrongTopic++;
        }
        logger.info("### Expected topic is: {} - received message topic is: {}", topic, arrivedTopic);
        Assert.assertEquals("Wrong received message", message, result);
        Assert.assertEquals("Wrong received messages count", count, resultCounter);
    }

    public void connectionLost(Throwable cause) {
        logger.error("Connection Lost");
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        logger.info(clientId + " - Message arrived on \"" + topic + "\" - \"");// + new String(message.getPayload()) + "\"");
        arrivedTopic = topic;
        result = new String(message.getPayload());
        byteResult = message.getPayload();
        resultCounter++;
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.info("Delivery Complete: " + token.getMessageId());
    }

    public void printWrongTopicCount() {
        logger.info("===> wrong topic count: {}", wrongTopic);
    }

}