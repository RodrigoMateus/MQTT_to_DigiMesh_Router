package mainApp;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.TransmitException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.utils.LogRecord;
import com.digi.xbee.api.utils.Statistic;
import com.maykot.maykottracker.radio.ErrorCode;
import com.maykot.maykottracker.radio.ProxyResponse;

public class RouterMqtt implements MqttCallback {

	public RouterMqtt() {
		super();
		try {
			Thread.sleep(1000);
			testeSendMessage();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void testeSendMessage() {
		try {
			byte[] dataToSend = null;
			dataToSend = new String("Teste: Servidor On-line").getBytes();
			SendTextMessage.send(MainApp.myDevice, dataToSend, MainApp.ENDPOINT_TXT, MainApp.REMOTE_NODE_IDENTIFIER);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {

		// Se a mensagem contém um HTTP POST
		if (topic.toLowerCase().contains("http_post")) {

			String[] topicWords = topic.split("/");
			String clientId = topicWords[2];
			byte[] mqttClientId = clientId.getBytes();
			String messageId = topicWords[3];

			byte[] noMessage = new String("noMessage").getBytes();

			try {
				SendHttpPost.send(MainApp.myDevice, mqttClientId, MainApp.ENDPOINT_HTTP_POST_INIT,
						MainApp.remoteDevice);
				SendHttpPost.send(MainApp.myDevice, message.getPayload(), MainApp.ENDPOINT_HTTP_POST_DATA,
						MainApp.remoteDevice);
				SendHttpPost.send(MainApp.myDevice, noMessage, MainApp.ENDPOINT_HTTP_POST_SEND, MainApp.remoteDevice);
			} catch (TransmitException e) {
				LogRecord.insertLog("log", new String("TransmitException ERROR"));
				System.out.println("604: TransmitException ERROR");
				Statistic.incrementCountBadPack();
				sendErrorMessage(604, clientId, messageId, ErrorCode.e604);

			} catch (TimeoutException e) {
				LogRecord.insertLog("log", new String("TimeOut ERROR"));
				System.out.println("605: TimeOut ERROR");
				Statistic.incrementCountBadPack();
				sendErrorMessage(605, clientId, messageId, ErrorCode.e605);

			} catch (XBeeException e) {
				Statistic.incrementCountBadPack();
				e.printStackTrace();
				sendErrorMessage(606, clientId, messageId, ErrorCode.e606);

			} catch (Exception e) {
				sendErrorMessage(607, clientId, messageId, ErrorCode.e607);
			}

			// System.out.println("Total Success " + Statistic.getCountOK());
			// System.out.println("Total Conect Error " +
			// Statistic.getCountNoModem());
			// System.out.println("Total Send Error " +
			// Statistic.getCountBadPack());
		}

		// Se a mensagem contém um comando JAVA
		if (topic.toLowerCase().contains("command")) {
			String command = new String(message.getPayload());

			switch (command) {

			case "reset":
				String[] topicWords = topic.split("/");
				String clientId = topicWords[1];
				MainApp.modemStatusReceiveListener.modemReset(clientId);
				break;

			default:
				break;
			}
		}

		// Se a mensagem contém apenas texto
		if (topic.toLowerCase().contains("text")) {
			SendTextMessage.send(MainApp.myDevice, message.getPayload(), MainApp.ENDPOINT_TXT,
					MainApp.REMOTE_NODE_IDENTIFIER);
		}
	}

	public void sendErrorMessage(int statusCode, String clientId, String messageId, String errorCode) {
		ProxyResponse errorResponse = new ProxyResponse(statusCode, "application/json", errorCode.getBytes());
		errorResponse.setMqttClientId(clientId);
		errorResponse.setIdMessage(messageId);

		byte[] payload = SerializationUtils.serialize(errorResponse);

		new MQTTMonitor().sendMQTT(errorResponse, payload);
	}
}
