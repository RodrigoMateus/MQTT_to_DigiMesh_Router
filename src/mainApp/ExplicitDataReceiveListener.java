package mainApp;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.digi.xbee.api.listeners.IExplicitDataReceiveListener;
import com.digi.xbee.api.models.ExplicitXBeeMessage;

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
			System.out.println("Chegou a resposta!!!");

			MqttMessage mqttMessage = new MqttMessage();
			String resposta = "Resposta";
			mqttMessage.setPayload(resposta.getBytes());

			try {
				MainApp.mqttClient.publish("maykot/teste", mqttMessage);
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
