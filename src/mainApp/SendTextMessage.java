package mainApp;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.digi.xbee.api.DigiMeshDevice;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.utils.LogRecord;
import com.digi.xbee.api.utils.Statistic;

public abstract class SendTextMessage {

	@SuppressWarnings("null")
	public static void send(DigiMeshDevice myDevice, byte[] dataToSend, int ENDPOINT, String REMOTE_NODE_IDENTIFIER) {

		try {
			if (!myDevice.isOpen()) {
				myDevice.open();
			}
			// Obtain the remote XBee device from the XBee network.
			XBeeNetwork xbeeNetwork = myDevice.getNetwork();
			RemoteXBeeDevice remoteDevice = xbeeNetwork.discoverDevice(REMOTE_NODE_IDENTIFIER);
			if (remoteDevice == null) {
				System.out.println("Couldn't find the remote XTend device named '" + remoteDevice.getNodeID());

				Statistic.incrementCountNoModem();
				System.out.println("Conect Error " + Statistic.getCountNoModem());

				LogRecord.insertLog((new String(new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss").format(new Date())))
						+ ";Conect Error " + Statistic.getCountNoModem() + ";" + new String(dataToSend));

			} else {
				System.out.format("Sending data to %s >> %s\n", remoteDevice.getNodeID(), new String(dataToSend));

				myDevice.sendExplicitData(remoteDevice, ENDPOINT, ENDPOINT, MainApp.CLUSTER_ID, MainApp.PROFILE_ID,
						dataToSend);

				Statistic.incrementCountOK();
				System.out.println("Success " + Statistic.getCountOK());

				LogRecord.insertLog((new String(new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss").format(new Date())))
						+ ";Success " + Statistic.getCountOK() + ";" + new String(dataToSend));
			}

		} catch (XBeeException e) {
			Statistic.incrementCountBadPack();
			System.out.println("Error " + Statistic.getCountBadPack());

			LogRecord.insertLog((new String(new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss").format(new Date())))
					+ ";Send Error " + Statistic.getCountBadPack() + ";" + new String(dataToSend));

			e.printStackTrace();

		} finally {
			// myDevice.close();
		}
	}
}
