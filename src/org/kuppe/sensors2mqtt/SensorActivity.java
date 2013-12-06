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

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SensorActivity extends Activity {

	private final List<SensorEventListener> listeners = new ArrayList<SensorEventListener>();
	private MqttClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor);
	}

	// "start" method is wired from the activity_sensors.xml button onClick
	// attribute
	public void start(View view) {
		flipEnablement();
		
		final EditText hostname = (EditText) findViewById(R.id.hostname);
		final EditText port = (EditText) findViewById(R.id.port);
		final EditText topicPrefix = (EditText) findViewById(R.id.topic);
		final EditText windowSize = (EditText) findViewById(R.id.windowSize);

		try {
			final String deviceId = Secure.getString(getContentResolver(),
					Secure.ANDROID_ID);

			final String url = String.format(Locale.getDefault(),
					"tcp://%s:%s", hostname.getText(), port.getText());

			client = new MqttClient(url, deviceId,
					new MemoryPersistence());
			client.connect();

			final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			final List<Sensor> sensors = sensorManager
					.getSensorList(Sensor.TYPE_LIGHT);

			for (Sensor sensor : sensors) {
				final String topic = sensor.getName().replaceAll("\\s+", "");
				final SensorEventListener mySensorEventListener = new MySensorEventListener(client,
						topicPrefix.getText() + topic, Integer.parseInt(windowSize.getText().toString()));
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

	private void flipEnablement() {
		final Button start = (Button) findViewById(R.id.button1);
		start.setEnabled(!start.isEnabled());
		
		final Button stop = (Button) findViewById(R.id.button2);
		stop.setEnabled(!stop.isEnabled());

		final EditText hostname = (EditText) findViewById(R.id.hostname);
		hostname.setEnabled(!hostname.isEnabled());
		
		final EditText port = (EditText) findViewById(R.id.port);
		port.setEnabled(!port.isEnabled());
		
		final EditText topicPrefix = (EditText) findViewById(R.id.topic);
		topicPrefix.setEnabled(!topicPrefix.isEnabled());
		
		final EditText windowSize = (EditText) findViewById(R.id.windowSize);
		windowSize.setEnabled(!windowSize.isEnabled());
	}

	// "stop" method is wired from the activity_sensors.xml button onClick
	// attribute
	public void stop(View view) {
		flipEnablement();
		
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
}
