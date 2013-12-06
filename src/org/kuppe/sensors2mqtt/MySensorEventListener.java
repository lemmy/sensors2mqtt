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

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class MySensorEventListener implements SensorEventListener {

	private static final int WINDOW_SIZE = 30;
	
	private final MyAsyncTask denoiseAndPush;

	private List<Float> readings = new ArrayList<Float>(WINDOW_SIZE);

	public MySensorEventListener(MyAsyncTask myAsyncTask) {
		this.denoiseAndPush = myAsyncTask;
	}

	// send via mqtt
	@SuppressWarnings("unchecked")
	@Override
	public void onSensorChanged(SensorEvent event) {
		//TODO for temperature readings the first value is set
		readings.add(event.values[0]);
		
		// If this listener has received more than WINDOW_SIZE readings, lets
		// denoise/remove outliers.
		if (readings.size() > WINDOW_SIZE) {
			synchronized (readings) {
				if (readings.size() > WINDOW_SIZE) {
					final List<Float> c = readings;
					readings = new ArrayList<Float>(WINDOW_SIZE);
					denoiseAndPush.execute(c);
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// nop
	}
}
