package mainApp;

import java.util.Arrays;

import com.digi.xbee.api.DigiMeshDevice;
import com.digi.xbee.api.RemoteXBeeDevice;

public class SendHttpPost {

	public static void send(DigiMeshDevice myDevice, byte[] dataToSend, int ENDPOINT, RemoteXBeeDevice remoteDevice)
			throws Exception {

		if (!myDevice.isOpen()) {
			myDevice.open();
			System.out.println("Device is open now!");
		}

		switch (ENDPOINT) {

		case MainApp.ENDPOINT_HTTP_POST_INIT:
			myDevice.sendExplicitData(remoteDevice, ENDPOINT, ENDPOINT, MainApp.CLUSTER_ID, MainApp.PROFILE_ID,
					dataToSend);
			break;

		case MainApp.ENDPOINT_HTTP_POST_DATA:
			int dataSize = dataToSend.length;
			int first = 0;
			int last = MainApp.PAYLOAD;
			int count=0;
			long init = System.currentTimeMillis();

			do {
				byte[] partOfData = Arrays.copyOfRange(dataToSend, first, last);
				myDevice.sendExplicitData(remoteDevice, ENDPOINT, ENDPOINT, MainApp.CLUSTER_ID, count,
						partOfData);
				first = last;
				last = last + MainApp.PAYLOAD;
				if (last > dataSize) {
					last = dataSize;
				}
				//Thread.sleep(50);
				count++;
			} while (first < dataSize);
			System.out.println(System.currentTimeMillis() - init + "milles");

			break;

		case MainApp.ENDPOINT_HTTP_POST_SEND:
			myDevice.sendExplicitData(remoteDevice, ENDPOINT, ENDPOINT, MainApp.CLUSTER_ID, MainApp.PROFILE_ID,
					dataToSend);
			break;
		}
	}
}
