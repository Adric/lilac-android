/*
 * Copyright (C) 2014 The Android Open Source Project
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

package qesst.asu.edu.lilac;

import java.util.UUID;

/**
 * Defines several constants used between {@link BluetoothService} and the UI.
 */
public interface Constants
{

    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

	// App tag
    public static final String TAG = "Lilac";
    // SPP UUID service
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Default bluetooth device name. Should be "DogBlue" but use all lowercase just in, well... case
    public static final String BT_DEVICE_NAME = "dogblue";
    // Passed to startActivityForResult(). Must be >= 0
    // See requestCode at:
    // http://developer.android.com/reference/android/app/Activity.html#startActivityForResult%28android.content.Intent,%20int%29
    public static final int REQUEST_ENABLE_BT = 1;

}
