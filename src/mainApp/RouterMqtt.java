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
import com.maykot.radiolibrary.ErrorCode;
import com.maykot.radiolibrary.ErrorMessage;
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
			dataToSend = new String("This is a message boot test!").getBytes();
			SendTextMessage.send(dataToSend, MainApp.ENDPOINT_TXT);
			System.out.println("Boot test SUCCESS!");
		} catch (Exception e) {
			System.out.println("Boot test FAILED!");
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

			if (proxyRequest.getBody() == null)
				body = "no body";
			else
				body = new String(proxyRequest.getBody());

			LogRecord.insertLog("ProxyRequestLog", clientId + ";" + new String(proxyRequest.getIdMessage()) + ";"
					+ new String(new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss:SSS").format(new Date())) + ";" + body);

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
								+ new String(new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss:SSS").format(new Date())) + ";"
								+ ErrorMessage.TRANSMIT_EXCEPTION.getDescription());
				System.out.println(ErrorMessage.TRANSMIT_EXCEPTION.getValue() + ": "
						+ ErrorMessage.TRANSMIT_EXCEPTION.getDescription());
				sendErrorMessage(ErrorMessage.TRANSMIT_EXCEPTION.getValue(), clientId, messageId,
						ErrorMessage.TRANSMIT_EXCEPTION.getDescription());

			} catch (TimeoutException e) {
				LogRecord.insertLog("ErrorLog",
						clientId + ";" + new String(proxyRequest.getIdMessage()) + ";"
								+ new String(new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss:SSS").format(new Date())) + ";"
								+ ErrorMessage.TIMEOUT_ERROR.getDescription());
				System.out
						.println("Erro " + ErrorMessage.TIMEOUT_ERROR.getValue() + ": " + ErrorMessage.TIMEOUT_ERROR.getDescription());
				sendErrorMessage(ErrorMessage.TIMEOUT_ERROR.getValue(), clientId, messageId,
						ErrorMessage.TIMEOUT_ERROR.getDescription());

			} catch (XBeeException e) {
				LogRecord.insertLog("ErrorLog",
						clientId + ";" + new String(proxyRequest.getIdMessage()) + ";"
								+ new String(new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss:SSS").format(new Date())) + ";"
								+ "XBeeException ERROR");
				e.printStackTrace();
				sendErrorMessage(606, clientId, messageId, ErrorCode.e606);

			} catch (Exception e) {
				LogRecord.insertLog("ErrorLog",
						clientId + ";" + new String(proxyRequest.getIdMessage()) + ";"
								+ new String(new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss:SSS").format(new Date())) + ";"
								+ "Exception ERROR");
				sendErrorMessage(607, clientId, messageId, ErrorCode.e607);
			}
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
			SendTextMessage.send(message.getPayload(), MainApp.ENDPOINT_TXT);
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
