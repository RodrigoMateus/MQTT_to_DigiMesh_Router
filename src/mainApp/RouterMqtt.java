package mainApp;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.digi.xbee.api.utils.Statistic;

public class RouterMqtt implements MqttCallback {

	public RouterMqtt() {
		super();
		runRouter();
	}

	public void runRouter() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		testeSendMessage();
	}

	public void testeSendMessage() {
		byte[] dataToSend = null;
		dataToSend = new String("Teste: Servidor On-line").getBytes();
		SendTextMessage.send(MainApp.myDevice, dataToSend, MainApp.ENDPOINT_TXT, MainApp.REMOTE_NODE_IDENTIFIER);
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

		// Se a mensagem contém apenas texto
		if (topic.toLowerCase().contains("text")) {
			SendTextMessage.send(MainApp.myDevice, message.getPayload(), MainApp.ENDPOINT_TXT,
					MainApp.REMOTE_NODE_IDENTIFIER);
		}

		// Se a mensagem contém um HTTP POST
		if (topic.toLowerCase().contains("http_post")) {

			byte[] noMessage = new String("noMessage").getBytes();

			SendHttpPost.send(MainApp.myDevice, noMessage, MainApp.ENDPOINT_HTTP_POST_INIT,
					MainApp.REMOTE_NODE_IDENTIFIER);
			SendHttpPost.send(MainApp.myDevice, message.getPayload(), MainApp.ENDPOINT_HTTP_POST_DATA,
					MainApp.REMOTE_NODE_IDENTIFIER);
			SendHttpPost.send(MainApp.myDevice, noMessage, MainApp.ENDPOINT_HTTP_POST_SEND,
					MainApp.REMOTE_NODE_IDENTIFIER);

			System.out.println("Total Success " + Statistic.getCountOK());
			System.out.println("Total Conect Error " + Statistic.getCountNoModem());
			System.out.println("Total Send Error " + Statistic.getCountBadPack());
		}
	}
}
