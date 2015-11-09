package mainApp;

import com.digi.xbee.api.DigiMeshDevice;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.utils.Statistic;

public abstract class SendTextMessage {

	public static void send(DigiMeshDevice myDevice, byte[] dataToSend, int ENDPOINT, String REMOTE_NODE_IDENTIFIER) {

		try {
			if (!myDevice.isOpen()) {
				myDevice.open();
			}
			// Obtain the remote XBee device from the XBee network.
			XBeeNetwork xbeeNetwork = myDevice.getNetwork();
			RemoteXBeeDevice remoteDevice = xbeeNetwork.discoverDevice(REMOTE_NODE_IDENTIFIER);
			if (remoteDevice == null) {
				System.out.println("Couldn't find the remote XTend device named '");

				Statistic.incrementCountNoModem();
				System.out.println("Conect Error " + Statistic.getCountNoModem());

			} else {
				System.out.format("Sending data to %s >> %s\n", remoteDevice.getNodeID(), new String(dataToSend));

				myDevice.sendExplicitData(remoteDevice, ENDPOINT, ENDPOINT, MainApp.CLUSTER_ID, MainApp.PROFILE_ID,
						dataToSend);

				Statistic.incrementCountOK();
				System.out.println("Success " + Statistic.getCountOK());
			}

		} catch (XBeeException e) {
			Statistic.incrementCountBadPack();
			System.out.println("Error " + Statistic.getCountBadPack());

			e.printStackTrace();

		} finally {
			// myDevice.close();
		}
	}
}
