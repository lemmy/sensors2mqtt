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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import android.os.AsyncTask;

public class MyAsyncTask extends AsyncTask<List<Float>, Void, Void> {

	private static final int QoS0 = 0; // fire and forget (lowest QoS is good enough here)

	private final MqttClient client;
	private final String topic;
	private final Comparator<Float> comparator;

	public MyAsyncTask(MqttClient client, String topic) {
		this.client = client;
		this.topic = topic;
		this.comparator = new Comparator<Float>() {
			@Override
			public int compare(Float lhs, Float rhs) {
				return lhs.compareTo(rhs);
			}
		};
	}

	@Override
	protected Void doInBackground(List<Float>... params) {
		final List<Float> list = params[0];

		// Sort sensor readings
		Collections.sort(list, comparator);
		
		// Get median
		final Float[] floats = list.toArray(new Float[list.size()]);
		float f = floats[floats.length / 2];
		
		// Send (as string for easier readability on the consumer end)
		try {
			client.publish(topic, Float.toString(f)
					.getBytes(), QoS0, false);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		return null;
	}
}
