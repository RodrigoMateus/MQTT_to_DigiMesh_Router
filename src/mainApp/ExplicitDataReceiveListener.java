package mainApp;

import java.io.ByteArrayOutputStream;
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
import com.maykot.maykottracker.models.ProxyResponse;

public class ExplicitDataReceiveListener implements IExplicitDataReceiveListener {

	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	FileChannel fileChannel;
	ByteBuffer buffer;
	boolean fileExist = false;

	@Override
	public void explicitDataReceived(ExplicitXBeeMessage explicitXBeeMessage) {
		ExecutorService executor = Executors.newFixedThreadPool(20);
		executor.execute(new TrataRequisao(explicitXBeeMessage));
		executor.shutdown();
	}

	class TrataRequisao extends Thread {

		ExplicitXBeeMessage explicitXBeeMessage;

		public TrataRequisao(ExplicitXBeeMessage explicitXBeeMessage) {
			this.explicitXBeeMessage = explicitXBeeMessage;
			byte[] payload = explicitXBeeMessage.getData();
			ProxyResponse proxyResponse = (ProxyResponse) SerializationUtils.deserialize(payload);
			
			System.out.println("Chegou a resposta!!!");

			MqttMessage mqttMessage = new MqttMessage();
			String resposta = new String(proxyResponse.getBody());
			String mqttClientId = proxyResponse.getMqttClientId();
			mqttMessage.setPayload(resposta.getBytes());

			try {
				MainApp.mqttClient.publish("maykot/" + mqttClientId, mqttMessage);
			} catch (MqttPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void run() {

		}
	}
}
