package mainApp;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.digi.xbee.api.utils.Statistic;

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
			// String messageId = topicWords[2];
			// byte[] mqttMessageId = messageId.getBytes();

			byte[] noMessage = new String("noMessage").getBytes();

			SendHttpPost.send(MainApp.myDevice, mqttClientId, MainApp.ENDPOINT_HTTP_POST_INIT, MainApp.remoteDevice);
			SendHttpPost.send(MainApp.myDevice, message.getPayload(), MainApp.ENDPOINT_HTTP_POST_DATA,
					MainApp.remoteDevice);
			SendHttpPost.send(MainApp.myDevice, noMessage, MainApp.ENDPOINT_HTTP_POST_SEND, MainApp.remoteDevice);

			System.out.println("Total Success " + Statistic.getCountOK());
			System.out.println("Total Conect Error " + Statistic.getCountNoModem());
			System.out.println("Total Send Error " + Statistic.getCountBadPack());
		}

		// Se a mensagem contém um comando JAVA
		if (topic.toLowerCase().contains("command")) {
			String command = message.getPayload().toString();

			switch (command) {

			case "reset":
				MainApp.myDevice.reset();
				System.out.println("Radio RESET.");
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
}
