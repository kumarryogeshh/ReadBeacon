/**
 * 
 */
package com.boskysoft.beaconscanner.modal;

/**
 * @author bosky
 * 
 * This is a sample modal class for iBeacon
 *
 */
public class Beacon {
	
	private final String proximityUUID;
	private final String name;
	private final String macAddress;
	private final int major;
	private final int minor;
	private final int measuredPower;
	private final int rssi;

	public Beacon(String proximityUUID, String name, String macAddress,
			int major, int minor, int measuredPower, int rssi) {
		this.proximityUUID = proximityUUID;
		this.name = name;
		this.macAddress = macAddress;
		this.major = major;
		this.minor = minor;
		this.measuredPower = measuredPower;
		this.rssi = rssi;
	}

	public String getProximityUUID() {
		return proximityUUID;
	}

	public String getName() {
		return name;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getMeasuredPower() {
		return measuredPower;
	}

	public int getRssi() {
		return rssi;
	}

}
