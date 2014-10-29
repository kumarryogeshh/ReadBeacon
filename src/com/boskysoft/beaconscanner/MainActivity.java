package com.boskysoft.beaconscanner;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.boskysoft.beaconscanner.ble.BleWrapper;
import com.boskysoft.beaconscanner.ble.BleWrapperUiCallbacks;
import com.boskysoft.beaconscanner.core.HashCode;
import com.boskysoft.beaconscanner.modal.Beacon;

public class MainActivity extends Activity {
	
	public static final String LOGTAG="boskysoft";
	private BleWrapper mBleWrapper = null;
	private boolean mScanning = false;
	
	TextView mStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mStatus = (TextView)findViewById(R.id.scan_status);
		
		mBleWrapper = new BleWrapper(this, new BleWrapperUiCallbacks.Null() {
			
			@Override
			public void uiDeviceFound(final BluetoothDevice device, final int rssi, final byte[] record) {
        		handleFoundDevice(device, rssi, record);
        	}
		});
		
		// Check if device supports BLE
		
		if (mBleWrapper.checkBleHardwareAvailable() == false) {
			Toast.makeText(this, "No BLE-compatible hardware detected",
					Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	protected void handleFoundDevice(BluetoothDevice device, int rssi,
			byte[] scanRecord) {
		
		String scanRecordAsHex = HashCode.fromBytes(scanRecord)
				.toString();
		for (int i = 0; i < scanRecord.length; i++) {
			int payloadLength = unsignedByteToInt(scanRecord[i]);
			if ((payloadLength == 0) || (i + 1 >= scanRecord.length)) {
				break;
			}
			if (unsignedByteToInt(scanRecord[(i + 1)]) != 255) {
				i += payloadLength;
			} else {
				if (payloadLength == 26) {
					if ((unsignedByteToInt(scanRecord[(i + 2)]) == 76)
							&& (unsignedByteToInt(scanRecord[(i + 3)]) == 0)
							&& (unsignedByteToInt(scanRecord[(i + 4)]) == 2)
							&& (unsignedByteToInt(scanRecord[(i + 5)]) == 21)) {
						String proximityUUID = String.format(
								"%s-%s-%s-%s-%s",
								new Object[] {
										scanRecordAsHex.substring(18,
												26),
										scanRecordAsHex.substring(26,
												30),
										scanRecordAsHex.substring(30,
												34),
										scanRecordAsHex.substring(34,
												38),
										scanRecordAsHex.substring(38,
												50) });
						int major = unsignedByteToInt(scanRecord[(i + 22)])
								* 256
								+ unsignedByteToInt(scanRecord[(i + 23)]);
						int minor = unsignedByteToInt(scanRecord[(i + 24)])
								* 256
								+ unsignedByteToInt(scanRecord[(i + 25)]);
						int measuredPower = scanRecord[(i + 26)];
						

						String msg = "uiDeviceFound: "
								+ device.getName() + ", rssi:" + rssi
								+ ", Mac Address:"
								+ device.getAddress()
								+ " PROXIMITYUUID:" + proximityUUID
								+ " MAJOR:" + major + " MINOR:" + minor
								+ " MEASURED POWER:" + measuredPower;

						Log.d(LOGTAG, msg);
						
						// use myBeacon as per your need
						
						Beacon myBeacon = new Beacon(proximityUUID,
						 device.getName(), device.getAddress(), major,
						 minor, measuredPower, rssi);


					}
				}
			}

		}
		
	}
	
	private int unsignedByteToInt(byte value) {
		return value & 0xff;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scanning, menu);

        if (mScanning) {
            menu.findItem(R.id.scanning_start).setVisible(false);
            menu.findItem(R.id.scanning_stop).setVisible(true);
            menu.findItem(R.id.scanning_indicator)
                .setActionView(R.layout.progress_indicator);

        } else {
            menu.findItem(R.id.scanning_start).setVisible(true);
            menu.findItem(R.id.scanning_stop).setVisible(false);
            menu.findItem(R.id.scanning_indicator).setActionView(null);
        }
        return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
        case R.id.scanning_start:
        	mScanning = true;
        	mBleWrapper.startScanning();
        	mStatus.setText("Status : Scanning..");
            break;
        case R.id.scanning_stop:
        	mScanning = false;
        	mBleWrapper.stopScanning();
        	mStatus.setText("Status : Scanning Stop");
            break;
        
    }
    
    invalidateOptionsMenu();
    return true;
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();

		if (mBleWrapper.isBtEnabled() == false) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(enableBtIntent);
			finish();
		}
		mBleWrapper.initialize();

	}

	@Override
	protected void onPause() {
		super.onPause();
		mBleWrapper.diconnect();
		mBleWrapper.close();

	}

}
