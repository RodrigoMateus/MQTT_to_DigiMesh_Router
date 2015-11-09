package mainApp;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.TransmitException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.utils.LogRecord;
import com.digi.xbee.api.utils.Statistic;
import com.maykot.radiolibrary.ErrorCode;
import com.maykot.radiolibrary.ProxyRequest;
import com.maykot.radiolibrary.ProxyResponse;

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
			String body = null;
			
			ProxyRequest proxyRequest = (ProxyRequest) SerializationUtils.deserialize(message.getPayload());

			if(proxyRequest.getBody() == null)
				body = "no body";
			else
				new String(proxyRequest.getBody());

			LogRecord.insertLog("ProxyRequestLog",
					clientId + ";" + new String(proxyRequest.getIdMessage()) + ";"
							+ new String(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date())) + ";"
							+ body);

			byte[] noMessage = new String("noMessage").getBytes();

			try {
				SendHttpPost.send(MainApp.myDevice, mqttClientId, MainApp.ENDPOINT_HTTP_POST_INIT,
						MainApp.remoteDevice);
				SendHttpPost.send(MainApp.myDevice, message.getPayload(), MainApp.ENDPOINT_HTTP_POST_DATA,
						MainApp.remoteDevice);
				SendHttpPost.send(MainApp.myDevice, noMessage, MainApp.ENDPOINT_HTTP_POST_SEND, MainApp.remoteDevice);
			} catch (TransmitException e) {
				LogRecord.insertLog("ErrorLog",
						clientId + ";" + new String(proxyRequest.getIdMessage()) + ";"
								+ new String(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date())) + ";"
								+ "TransmitException ERROR");
				System.out.println("604: TransmitException ERROR");
				Statistic.incrementCountBadPack();
				sendErrorMessage(604, clientId, messageId, ErrorCode.e604);

			} catch (TimeoutException e) {
				LogRecord.insertLog("ErrorLog",
						clientId + ";" + new String(proxyRequest.getIdMessage()) + ";"
								+ new String(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date())) + ";"
								+ "TimeOut ERROR");
				System.out.println("605: TimeOut ERROR");
				Statistic.incrementCountBadPack();
				sendErrorMessage(605, clientId, messageId, ErrorCode.e605);

			} catch (XBeeException e) {
				LogRecord.insertLog("ErrorLog",
						clientId + ";" + new String(proxyRequest.getIdMessage()) + ";"
								+ new String(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date())) + ";"
								+ "XBeeException ERROR");
				Statistic.incrementCountBadPack();
				e.printStackTrace();
				sendErrorMessage(606, clientId, messageId, ErrorCode.e606);

			} catch (Exception e) {
				LogRecord.insertLog("ErrorLog",
						clientId + ";" + new String(proxyRequest.getIdMessage()) + ";"
								+ new String(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date())) + ";"
								+ "Exception ERROR");
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
