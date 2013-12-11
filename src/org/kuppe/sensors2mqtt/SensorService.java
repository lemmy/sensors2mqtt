/*******************************************************************************
 * Copyright (c) 2013 Markus Alexander Kuppe and others. All rights reserved. 
 * This program and the accompanying materials are made available under the terms 
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Markus Alexander Kuppe - initial API and implementation
 ******************************************************************************/
package org.kuppe.sensors2mqtt;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.provider.Settings.Secure;

public class SensorService extends Service {

	static final String HOSTNAME = "HOSTNAME";

	static final String WINDOW_SIZE = "WINDOW_SIZE";

	static final String TOPIC_EXTRA = "TOPIC_EXTRA";

	static final String PORT = "PORT";
	
	private final List<SensorEventListener> listeners = new ArrayList<SensorEventListener>();
	
	private MqttClient client;

	/* (non-Javadoc)
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	public void onStart(Intent i, int startId) {
		final String hostname = i.getStringExtra(HOSTNAME);
		final String port = i.getStringExtra(PORT);
		final String topicPrefix = i.getStringExtra(TOPIC_EXTRA);
		final String windowSize = i.getStringExtra(WINDOW_SIZE);
		
		try {
			final String deviceId = Secure.getString(getContentResolver(),
					Secure.ANDROID_ID);

			final String url = String.format(Locale.getDefault(),
					"tcp://%s:%s", hostname, port);

			client = new MqttClient(url, deviceId,
					new MemoryPersistence());
			client.connect();

			final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			final List<Sensor> sensors = sensorManager
					.getSensorList(Sensor.TYPE_LIGHT);

			for (Sensor sensor : sensors) {
				final String topic = sensor.getName().replaceAll("\\s+", "");
				final SensorEventListener mySensorEventListener = new MySensorEventListener(client,
						topicPrefix + topic, Integer.parseInt(windowSize));
				listeners.add(mySensorEventListener);
				sensorManager.registerListener(
						mySensorEventListener, sensor,
						SensorManager.SENSOR_DELAY_FASTEST);
			}
		} catch (MqttException e) {
			e.printStackTrace();
			return;
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	public void onDestroy() {
		final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		for (SensorEventListener listener : listeners) {
			sensorManager.unregisterListener(listener);
		}
		
		if (client != null) {
			try {
				client.disconnect();
				client = null;
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
