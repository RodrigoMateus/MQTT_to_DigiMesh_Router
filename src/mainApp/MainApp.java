package mainApp;

/***************************
 * MQTT to DigiMesh Router *
 ***************************/

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.digi.xbee.api.DigiMeshDevice;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.APIOutputMode;
import com.digi.xbee.api.utils.DeviceConfig;
import com.digi.xbee.api.utils.LogRecord;
import com.digi.xbee.api.utils.Statistic;

public class MainApp {

	/* XTends */
	static DigiMeshDevice myDevice;
	static RemoteXBeeDevice remoteDevice;
	static String XTEND_PORT = null;
	static int XTEND_BAUD_RATE;
	static int TIMEOUT_FOR_SYNC_OPERATIONS = 10000; // 10 seconds
	static String REMOTE_NODE_IDENTIFIER = null;

	// Payload é a quantidade de bytes que cabe em uma mensagem
	// ou MTU (Maximum Transmission Unit)
	static int PAYLOAD = 250;

	/* MQTT */
	static String BROKER_URL = null;
	static String CLIENT_ID = null;
	static String SUBSCRIBED_TOPIC = null;
	static int QoS = -1;
	static MqttClient mqttClient;
	static ModemStatusReceiveListener modemStatusReceiveListener;

	/* Endpoints, clusterID and profileID */
	static final int ENDPOINT_TXT = 11;
	static final int ENDPOINT_HTTP_POST_INIT = 31;
	static final int ENDPOINT_HTTP_POST_DATA = 32;
	static final int ENDPOINT_HTTP_POST_SEND = 33;
	static final int ENDPOINT_RESPONSE_INIT = 41;
	static final int ENDPOINT_RESPONSE_DATA = 42;
	static final int ENDPOINT_RESPONSE_SEND = 43;
	static final int CLUSTER_ID = 1;
	static final int PROFILE_ID = 1;

	public static void main(String[] args) {
		System.out.println(" +---------------------------+");
		System.out.println(" |  MQTT to DigiMesh Router  |");
		System.out.println(" +---------------------------+\n");

		try {
			DeviceConfig deviceConfig = new DeviceConfig();

			XTEND_PORT = deviceConfig.getXTendPort();
			XTEND_BAUD_RATE = deviceConfig.getXTendBaudRate();
			REMOTE_NODE_IDENTIFIER = deviceConfig.getRemoteNodeID();
			BROKER_URL = deviceConfig.getBrokerURL();
			CLIENT_ID = deviceConfig.getClientId();
			SUBSCRIBED_TOPIC = deviceConfig.getSubscribedTopic();
			QoS = deviceConfig.getQoS();

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		new LogRecord();
		new Statistic();

		myDevice = new DigiMeshDevice(XTEND_PORT, XTEND_BAUD_RATE);

		modemStatusReceiveListener = new ModemStatusReceiveListener();

		openDevice();

		try {
			mqttClient = new MqttClient(BROKER_URL, CLIENT_ID);
			mqttClient.setCallback(new RouterMqtt());
			mqttClient.connect();
			mqttClient.subscribe(SUBSCRIBED_TOPIC, QoS);
		} catch (MqttException e) {
			e.printStackTrace();
		}


	}

	public static void openDevice() {
		try {
			myDevice.open();
			myDevice.setAPIOutputMode(APIOutputMode.MODE_EXPLICIT);
			myDevice.setReceiveTimeout(TIMEOUT_FOR_SYNC_OPERATIONS);

			System.out.println("ReceiveTimeout: " + myDevice.getReceiveTimeout());

			myDevice.addModemStatusListener(modemStatusReceiveListener);
			myDevice.addExplicitDataListener(new ExplicitDataReceiveListener());

			discoverDevice();
		} catch (XBeeException e) {
			e.printStackTrace();
			// System.exit(1);
		}
	}

	public static void discoverDevice() throws XBeeException {
		// Obtain the remote XBee device from the XBee network.
		XBeeNetwork xbeeNetwork = myDevice.getNetwork();
		do{
			remoteDevice = xbeeNetwork.discoverDevice(REMOTE_NODE_IDENTIFIER);
			System.out.println(remoteDevice.getPowerLevel());
			if (remoteDevice == null) {
				System.out.println("Couldn't find the radio modem '" + REMOTE_NODE_IDENTIFIER + ".");
				Statistic.incrementCountNoModem();
			}
		}while(remoteDevice == null);
	}
}
