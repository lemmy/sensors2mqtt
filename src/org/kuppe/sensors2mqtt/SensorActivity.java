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

import java.util.List;
import java.util.Locale;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.View;
import android.widget.EditText;

public class SensorActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor);
	}

	// "start" method is wired from the activity_sensors.xml button onClick
	// attribute
	public void start(View view) {
		final EditText hostname = (EditText) findViewById(R.id.hostname);
		final EditText port = (EditText) findViewById(R.id.port);
		final EditText topicPrefix = (EditText) findViewById(R.id.topic);

		try {
			final String deviceId = Secure.getString(getContentResolver(),
					Secure.ANDROID_ID);

			final String url = String.format(Locale.getDefault(),
					"tcp://%s:%s", hostname.getText(), port.getText());

			final MqttClient client = new MqttClient(url, deviceId,
					new MemoryPersistence());
			client.connect();

			final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			final List<Sensor> sensors = sensorManager
					.getSensorList(Sensor.TYPE_LIGHT);

			// This only expects a single sensor
			for (Sensor sensor : sensors) {
				final String topic = sensor.getName().replaceAll("\\s+", "");
				sensorManager.registerListener(
						new MySensorEventListener(new MyAsyncTask(client,
								topicPrefix.getText() + topic)), sensor,
						SensorManager.SENSOR_DELAY_FASTEST);
			}
		} catch (MqttException e) {
			e.printStackTrace();
			return;
		}
	}
}
