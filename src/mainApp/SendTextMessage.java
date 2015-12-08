package mainApp;

import com.digi.xbee.api.exceptions.XBeeException;

public abstract class SendTextMessage {

	public static void send(byte[] dataToSend, int ENDPOINT) {

		try {
				System.out.format("Sending text message to %s >> %s\n", MainApp.remoteDevice.getNodeID(), new String(dataToSend));

				MainApp.myDevice.sendExplicitData(MainApp.remoteDevice, ENDPOINT, ENDPOINT, MainApp.CLUSTER_ID, MainApp.PROFILE_ID,
						dataToSend);
		} catch (XBeeException e) {
			e.printStackTrace();
		} finally {
			// myDevice.close();
		}
	}
}
