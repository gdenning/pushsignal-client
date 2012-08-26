package com.pushsignal;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.pushsignal.xml.simple.UserDTO;
import com.pushsignal.xml.simple.UserDeviceDTO;

public class AppUserDevice {
	private static AppUserDevice ourInstance = new AppUserDevice();
	private UserDeviceDTO userDevice;
	
	public static AppUserDevice getInstance() {
		return ourInstance;
	}
	
	private AppUserDevice() {
	}

	public void setUserDevice(UserDeviceDTO userDevice) {
		this.userDevice = userDevice;
	}

	public UserDeviceDTO getUserDevice() {
		return userDevice;
	}
	
	public UserDTO getUser() {
		if (userDevice == null) {
			return null;
		}
		return userDevice.getUser();
	}
	
	public long calculateMillisecondsSinceBootForServerDate(long dateMilliseconds) {
		if (userDevice == null) {
			return 0;
		}
		return userDevice.getClientMillisecondsSinceBoot() + (dateMilliseconds - userDevice.getServerMillisecondsSince1970());
	}

	public String getGoogleUsername(Context context) {
	    AccountManager manager = AccountManager.get(context); 
	    Account[] accounts = manager.getAccountsByType("com.google"); 
	    for (Account account : accounts) {
	    	return account.name;
	    }
	    return "";
	}
}
