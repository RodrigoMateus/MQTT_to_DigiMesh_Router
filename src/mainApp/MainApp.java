package mainApp;

/***************************
 * MQTT to DigiMesh Router *
 ***************************/

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import com.digi.xbee.api.DigiMeshDevice;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.APIOutputMode;
import com.digi.xbee.api.utils.SerialPorts;
import com.maykot.utils.DeviceConfig;
import com.maykot.utils.LogRecord;

public class MainApp {

	/* XTends */
	static DigiMeshDevice myDevice;
	static RemoteXBeeDevice remoteDevice;
	static DeviceConfig deviceConfig;
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
			deviceConfig = new DeviceConfig();

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
		modemStatusReceiveListener = new ModemStatusReceiveListener();

		openDevice();

		try {
			discoverDevice();
		} catch (XBeeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		new RouterMqtt();

		// try {
		// mqttClient = new MqttClient(BROKER_URL, CLIENT_ID, null);
		// mqttClient.setCallback(new RouterMqtt());
		// mqttClient.connect();
		// mqttClient.subscribe(SUBSCRIBED_TOPIC, QoS);
		// } catch (MqttException e) {
		// e.printStackTrace();
		// }

		new XTendMonitor().run();
	}

	public static void openDevice() {
		try {
			XTEND_PORT = deviceConfig.getXTendPort();
			myDevice = openDevice(XTEND_PORT, XTEND_BAUD_RATE);
			System.out.println("Was found LOCAL radio " + myDevice.getNodeID() + " (PowerLevel "
					+ myDevice.getPowerLevel() + ").");
			return;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String port : SerialPorts.getSerialPortList()) {
			try {
				System.out.println("Try " + port);
				myDevice = openDevice(port, XTEND_BAUD_RATE);
				System.out.println("Was found LOCAL radio " + myDevice.getNodeID() + " (PowerLevel: "
						+ myDevice.getPowerLevel() + ").");
				return;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("openDevice() ERROR");
			}
		}
		System.out.println("LOCAL Radio not found! Try openDevice() again.");
		openDevice();

	}

	public static DigiMeshDevice openDevice(String port, int bd) throws Exception {
		DigiMeshDevice device = new DigiMeshDevice(port, bd);
		device.open();
		device.setAPIOutputMode(APIOutputMode.MODE_EXPLICIT);
		// myDevice.setReceiveTimeout(TIMEOUT_FOR_SYNC_OPERATIONS);
		device.addModemStatusListener(modemStatusReceiveListener);
		device.addExplicitDataListener(new ExplicitDataReceiveListener());
		return device;
	}

	public static void discoverDevice() throws XBeeException {
		// Obtain the remote XBee device from the XBee network.
		XBeeNetwork xbeeNetwork = myDevice.getNetwork();

		do {
			remoteDevice = xbeeNetwork.discoverDevice(REMOTE_NODE_IDENTIFIER);
			if (remoteDevice == null) {
				System.out.println("Couldn't find the Radio " + REMOTE_NODE_IDENTIFIER + ".");
			}
		} while (remoteDevice == null);

		System.out.println("Was found REMOTE radio " + REMOTE_NODE_IDENTIFIER + " (PowerLevel "
				+ remoteDevice.getPowerLevel() + ").");
	}
}
