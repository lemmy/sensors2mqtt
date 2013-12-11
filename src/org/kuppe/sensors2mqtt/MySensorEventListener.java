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

import org.eclipse.paho.client.mqttv3.MqttClient;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class MySensorEventListener implements SensorEventListener {

	private static final String TAG = "org.kuppe.sensors2mqtt";
	
	private List<Float> readings = new ArrayList<Float>();

	private final MqttClient client;
	private final String topic;
	private final int windowSize;

	public MySensorEventListener(MqttClient client, String topic, int windowSize) {
		this.client = client;
		this.topic = topic;
		this.windowSize = windowSize;
	}

	// send via mqtt
	@SuppressWarnings("unchecked")
	@Override
	public void onSensorChanged(SensorEvent event) {
		//TODO for temperature readings the first value is set
		readings.add(event.values[0]);
		Log.d(TAG, "Sensorreading");
		
		// If this listener has received more than WINDOW_SIZE readings, lets
		// denoise/remove outliers.
		// TODO Not sure if the event listener is indeed called concurrently by
		// the underlying OS. But better be safe than sorry.
		if (readings.size() > windowSize) {
			synchronized (readings) {
				if (readings.size() > windowSize) {
					Log.d(TAG, "DenoiseAndPush");
					final List<Float> c = readings;
					readings = new ArrayList<Float>(windowSize);
					new MyAsyncTask(client, topic).execute(c);
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// nop
	}
}
