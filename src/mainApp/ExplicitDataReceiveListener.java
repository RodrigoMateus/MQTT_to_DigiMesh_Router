package mainApp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.SerializationUtils;

import com.digi.xbee.api.listeners.IExplicitDataReceiveListener;
import com.digi.xbee.api.models.ExplicitXBeeMessage;
import com.digi.xbee.api.utils.LogRecord;
import com.maykot.radiolibrary.ProxyResponse;

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
				LogRecord.insertLog("ProxyResponseLog",
						new String(proxyResponse.getMqttClientId()) + ";" + new String(proxyResponse.getIdMessage())
								+ ";" + new String(new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss:SSS").format(new Date()))
								+ ";" + new String(proxyResponse.getBody()));

				System.out.println("Chegou a resposta!!!");

				new MQTTMonitor().sendMQTT(proxyResponse, payload);
				break;

			default:
				break;
			}
		}
	}
}
