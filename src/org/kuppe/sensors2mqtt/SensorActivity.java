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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
		flipEnablement();
		
		final EditText hostname = (EditText) findViewById(R.id.hostname);
		final EditText port = (EditText) findViewById(R.id.port);
		final EditText topicPrefix = (EditText) findViewById(R.id.topic);
		final EditText windowSize = (EditText) findViewById(R.id.windowSize);
		
		final Intent i= new Intent(this, SensorService.class);
		i.putExtra(SensorService.HOSTNAME, hostname.getText().toString());
		i.putExtra(SensorService.PORT, port.getText().toString());
		i.putExtra(SensorService.TOPIC_EXTRA, topicPrefix.getText().toString());
		i.putExtra(SensorService.WINDOW_SIZE, windowSize.getText().toString());
		this.startService(i); 
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
		
		this.stopService(new Intent(this, SensorService.class));
	}
}
