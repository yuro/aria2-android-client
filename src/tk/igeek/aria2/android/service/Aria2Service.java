/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tk.igeek.aria2.android.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import tk.igeek.aria2.android.Aria2Manager;
import tk.igeek.aria2.android.IIncomingHandler;
import tk.igeek.aria2.android.IncomingHandler;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

/**
 * This is an example of implementing an application service that uses the
 * {@link Messenger} class for communicating with clients.  This allows for
 * remote interaction with a service, without needing to define an AIDL
 * interface.
 *
 * <p>Notice the use of the {@link NotificationManager} when interesting things
 * happen in the service.  This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 */

public class Aria2Service extends Service implements IIncomingHandler {
    
    /** Keeps track of all current registered clients. */
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    public Handler _mHandler = null;
    HandlerThread _aria2APIHandlerThread = null;
    /**
     * Command to the service to register a client, receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client where callbacks should be sent.
     */
    public static final int MSG_REGISTER_CLIENT = 1;
    
    /**
     * Command to the service to unregister a client, ot stop receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client as previously given with MSG_REGISTER_CLIENT.
     */
    public static final int MSG_UNREGISTER_CLIENT = 2;
    
    private Aria2Manager _aria2Manager = null;
    
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    Messenger mMessenger = null;
    
    @Override
    public void onCreate() {
    	_aria2APIHandlerThread = new HandlerThread("Aria2 API Handler Thread"); 
		_aria2APIHandlerThread.start();
		Looper mLooper = _aria2APIHandlerThread.getLooper();
    	_mHandler = new IncomingHandler(mLooper,this);
    	mMessenger = new Messenger(_mHandler);
    	_aria2Manager = new Aria2Manager();
    	
    }

    @Override
    public void onDestroy() {
    	
        // Tell the user we stopped.
        Toast.makeText(this, "Aria2 service has stopped",Toast.LENGTH_SHORT).show();
    }
    
    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
    	 Toast.makeText(this, "Aria2 service has Bind",Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }

	@Override
	public void handleMessage(Message msg, Handler handler) {
		switch (msg.what) {
		case MSG_REGISTER_CLIENT:
			mClients.add(msg.replyTo);
			break;
		case MSG_UNREGISTER_CLIENT:
			mClients.remove(msg.replyTo);
			break;
		default:
			for (int i=mClients.size()-1; i>=0; i--) {
				try {

					Messenger messenger = mClients.get(i);
					_aria2Manager.setCurretnRefreshHandler(messenger);
					_aria2Manager.handleMessage(msg,handler);
				} catch (RemoteException e) {
					// The client is dead.  Remove it from the list;
					// we are going through the list from back to front
					// so this is safe to do inside the loop.
					mClients.remove(i);
				}
			}
		}
	}
}

