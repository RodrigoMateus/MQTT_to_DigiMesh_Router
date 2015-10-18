package mainApp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.digi.xbee.api.listeners.IExplicitDataReceiveListener;
import com.digi.xbee.api.models.ExplicitXBeeMessage;
import com.maykot.maykottracker.radio.ProxyResponse;

public class ExplicitDataReceiveListener implements IExplicitDataReceiveListener {

	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	FileChannel fileChannel;
	ByteBuffer buffer;
	boolean fileExist = false;
	String mqttClientId;

	@Override
	public void explicitDataReceived(ExplicitXBeeMessage explicitXBeeMessage) {
		ExecutorService executor = Executors.newFixedThreadPool(20);
		executor.execute(new TreatRequest(explicitXBeeMessage));
		executor.shutdown();
	}

	class TreatRequest extends Thread {

		ExplicitXBeeMessage explicitXBeeMessage;

		public TreatRequest(ExplicitXBeeMessage explicitXBeeMessage) {
			this.explicitXBeeMessage = explicitXBeeMessage;
		}

		@Override
		public void run() {
			int endPoint = explicitXBeeMessage.getDestinationEndpoint();
			switch (endPoint) {

			case MainApp.ENDPOINT_RESPONSE_INIT:

				mqttClientId = new String(explicitXBeeMessage.getData());
				System.out.println("MQTT Client ID = " + mqttClientId);
				break;

			case MainApp.ENDPOINT_RESPONSE_DATA:

				try {
					byteArrayOutputStream.write(explicitXBeeMessage.getData());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;

			case MainApp.ENDPOINT_RESPONSE_SEND:

				byte[] payload = byteArrayOutputStream.toByteArray();
				byteArrayOutputStream.reset();

				ProxyResponse proxyResponse = (ProxyResponse) SerializationUtils.deserialize(payload);
				System.out.println("Chegou a resposta!!!");

				MqttMessage mqttMessage = new MqttMessage();
				String mqttClientId = proxyResponse.getMqttClientId();
				String idMessage = proxyResponse.getIdMessage();

				mqttMessage.setPayload(payload);

				try {
					MainApp.mqttClient.publish("maykot/" + mqttClientId + "/" + idMessage, mqttMessage);
				} catch (MqttPersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
		}
	}
}
