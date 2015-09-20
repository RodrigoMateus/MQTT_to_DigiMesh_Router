package mainApp;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.digi.xbee.api.DigiMeshDevice;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.utils.LogRecord;
import com.digi.xbee.api.utils.Statistic;

import mainApp.MainApp;

public class SendFile {

	public static void send(DigiMeshDevice myDevice, byte[] dataToSend, int ENDPOINT, String REMOTE_NODE_IDENTIFIER) {

		try {
			if (!myDevice.isOpen()) {
				myDevice.open();
			}
			// Obtain the remote XBee device from the XBee network.
			XBeeNetwork xbeeNetwork = myDevice.getNetwork();
			RemoteXBeeDevice remoteDevice = xbeeNetwork.discoverDevice(REMOTE_NODE_IDENTIFIER);
			if (remoteDevice == null) {
				System.out.println(
						"Couldn't find the remote XBee device with '" + REMOTE_NODE_IDENTIFIER + "' Node Identifier.");
				Statistic.incrementCountNoModem();
				System.exit(1);
			}

			switch (ENDPOINT) {

			case MainApp.ENDPOINT_FILENEW:
				myDevice.sendExplicitData(remoteDevice, ENDPOINT, ENDPOINT, MainApp.CLUSTER_ID, MainApp.PROFILE_ID,
						dataToSend);

				LogRecord.insertLog((new String(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date())))
						+ " : Inicio " + dataToSend.toString());

				break;

			case MainApp.ENDPOINT_FILEDATA:

				int dataSize = dataToSend.length;
				int first = 0;
				int last = MainApp.PAYLOAD;

				do {
					byte[] partOfData = Arrays.copyOfRange(dataToSend, first, last);

					myDevice.sendExplicitData(remoteDevice, MainApp.ENDPOINT_FILEDATA, MainApp.ENDPOINT_FILEDATA,
							MainApp.CLUSTER_ID, MainApp.PROFILE_ID, partOfData);
					Statistic.incrementCountOK();

					first = last;
					last = last + MainApp.PAYLOAD;
					if (last > dataSize)
						last = dataSize;
				} while (first < dataSize);
				break;

			case MainApp.ENDPOINT_FILECLOSE:
				myDevice.sendExplicitData(remoteDevice, ENDPOINT, ENDPOINT, MainApp.CLUSTER_ID, MainApp.PROFILE_ID,
						dataToSend);
				LogRecord.insertLog(
						(new String(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()))) + " : Fim");
				break;
			}

		} catch (XBeeException e) {
			Statistic.incrementCountBadPack();
			e.printStackTrace();
			System.exit(1);
		} finally {
			// myDevice.close();
		}
	}
}
