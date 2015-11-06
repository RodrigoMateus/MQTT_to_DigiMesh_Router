package mainApp;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.maykot.maykottracker.radio.ProxyResponse;

public class MQTTMonitor {

	public void sendMQTT(ProxyResponse proxyResponse, byte[] payload) {
		MqttMessage mqttMessage = new MqttMessage();
		String mqttClientId = proxyResponse.getMqttClientId();
		String idMessage = proxyResponse.getIdMessage();

		mqttMessage.setPayload(payload);

		new SendThread("maykot/" + mqttClientId + "/" + idMessage, mqttMessage).start();

	}

	class SendThread extends Thread {

		String url;
		MqttMessage mqttMessage;

		public SendThread(String url, MqttMessage mqttMessage) {
			this.url = url;
			this.mqttMessage = mqttMessage;
		}

		@Override
		public void run() {
			try {
				MainApp.mqttClient.publish(url, mqttMessage);
			} catch (MqttPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
