/**
 * Copyright (c) 2014-2015 Digi International Inc.,
 * All rights not expressly granted are reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Digi International Inc. 11001 Bren Road East, Minnetonka, MN 55343
 * =======================================================================
 */
package mainApp;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IModemStatusReceiveListener;
import com.digi.xbee.api.models.ModemStatusEvent;

public class ModemStatusReceiveListener implements IModemStatusReceiveListener {

	String clientId = null;

	@Override
	public void modemStatusEventReceived(ModemStatusEvent modemStatusEvent) {
		System.out.format("Modem Status event received: %s%n", modemStatusEvent.toString());

		MqttMessage mqttMessage = new MqttMessage();
		mqttMessage.setPayload(modemStatusEvent.toString().getBytes());

		try {
			MainApp.mqttClient.publish("maykot/" + clientId + "/commandResult", mqttMessage);
		} catch (MqttPersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void modemReset(String clientId) {
		this.clientId = clientId;

		try {
			MainApp.myDevice.reset();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XBeeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
