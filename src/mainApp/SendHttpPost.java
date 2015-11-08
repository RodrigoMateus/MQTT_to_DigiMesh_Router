package mainApp;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.digi.xbee.api.DigiMeshDevice;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.utils.LogRecord;
import com.digi.xbee.api.utils.Statistic;

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

			do {
				try {
					byte[] partOfData = Arrays.copyOfRange(dataToSend, first, last);
					myDevice.sendExplicitData(remoteDevice, ENDPOINT, ENDPOINT, MainApp.CLUSTER_ID, MainApp.PROFILE_ID,
							partOfData);
					first = last;
					last = last + MainApp.PAYLOAD;
					if (last > dataSize)
						last = dataSize;
					Statistic.incrementCountOK();
				} catch (TimeoutException e) {
					System.out.println("TimeOut ERROR");
				}
			} while (first < dataSize);
			break;

		case MainApp.ENDPOINT_HTTP_POST_SEND:
			myDevice.sendExplicitData(remoteDevice, ENDPOINT, ENDPOINT, MainApp.CLUSTER_ID, MainApp.PROFILE_ID,
					dataToSend);
			break;
		}
	}
}
