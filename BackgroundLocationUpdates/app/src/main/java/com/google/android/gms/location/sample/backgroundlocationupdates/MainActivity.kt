package com.google.android.gms.location.sample.backgroundlocationupdates

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

/**
 * The only activity in this sample. Displays UI widgets for requesting and removing location
 * updates, and for the batched location updates that are reported.
 *
 * Location updates requested through this activity continue even when the activity is not in the
 * foreground. Note: apps running on "O" devices (regardless of targetSdkVersion) may receive
 * updates less frequently than the interval specified in the [LocationRequest] when the app
 * is no longer in the foreground.
 */
class MainActivity : FragmentActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private var mLocationRequest: LocationRequest? = null

    /**
     * The entry point to Google Play Services.
     */
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    // UI Widgets.
    private var mRequestUpdatesButton: Button? = null
    private var mRemoveUpdatesButton: Button? = null
    private var mLocationUpdatesResultView: TextView? = null

    private val pendingIntent: PendingIntent
        get() {
            val intent = Intent(this, LocationUpdatesBroadcastReceiver::class.java)
            intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
            return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRequestUpdatesButton = findViewById<Button>(R.id.request_updates_button)
        mRemoveUpdatesButton = findViewById<Button>(R.id.remove_updates_button)
        mLocationUpdatesResultView = findViewById<TextView>(R.id.location_updates_result)

        // Check if the user revoked runtime permissions.
        if (!checkPermissions()) {
            requestPermissions()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
    }

    override fun onStart() {
        super.onStart()
        /*PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)*/
    }


    override fun onResume() {
        super.onResume()
        updateButtonsState(LocationRequestHelper.getRequesting(this))
        mLocationUpdatesResultView!!.text = LocationResultHelper.getSavedLocationResult(this)
    }

    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION`. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     *
     *
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     *
     *
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()

        mLocationRequest!!.interval = UPDATE_INTERVAL

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest!!.maxWaitTime = MAX_WAIT_TIME
    }


    override fun onConnected(bundle: Bundle?) {
        Log.i(TAG, "GoogleApiClient connected")
    }

    override fun onConnectionSuspended(i: Int) {
        val text = "Connection suspended"
        Log.w(TAG, "$text: Error code: $i")
        showSnackbar("Connection suspended")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        val text = "Exception while connecting to Google Play services"
        Log.w(TAG, text + ": " + connectionResult.errorMessage)
        showSnackbar(text)
    }

    /**
     * Shows a [Snackbar] using `text`.
     *
     * @param text The Snackbar text.
     */
    private fun showSnackbar(text: String) {
        val container = findViewById<View>(R.id.activity_main)
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show()
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok) {
                        // Request permission
                        ActivityCompat.requestPermissions(this@MainActivity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                REQUEST_PERMISSIONS_REQUEST_CODE)
                    }
                    .show()
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                requestLocationUpdates(null)

            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null)
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if (s == LocationResultHelper.KEY_LOCATION_UPDATES_RESULT) {
            mLocationUpdatesResultView!!.text = LocationResultHelper.getSavedLocationResult(this)
        } else if (s == LocationRequestHelper.KEY_LOCATION_UPDATES_REQUESTED) {
            updateButtonsState(LocationRequestHelper.getRequesting(this))
        }
    }

    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    fun requestLocationUpdates(view: View?) {
        try {
            Log.i(TAG, "Starting location updates")
            LocationRequestHelper.setRequesting(this, true)
            mFusedLocationClient!!.requestLocationUpdates(
                    mLocationRequest, pendingIntent)
        } catch (e: SecurityException) {
            LocationRequestHelper.setRequesting(this, false)
            e.printStackTrace()
        }

    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    fun removeLocationUpdates(view: View) {
        Log.i(TAG, "Removing location updates")
        LocationRequestHelper.setRequesting(this, false)
        mFusedLocationClient!!.removeLocationUpdates(pendingIntent)
    }

    /**
     * Ensures that only one button is enabled at any time. The Start Updates button is enabled
     * if the user is not requesting location updates. The Stop Updates button is enabled if the
     * user is requesting location updates.
     */
    private fun updateButtonsState(requestingLocationUpdates: Boolean) {
        if (requestingLocationUpdates) {
            mRequestUpdatesButton!!.isEnabled = false
            mRemoveUpdatesButton!!.isEnabled = true
        } else {
            mRequestUpdatesButton!!.isEnabled = true
            mRemoveUpdatesButton!!.isEnabled = false
        }
    }

    companion object {

        private val TAG = MainActivity::class.java.simpleName
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        // FIXME: 5/16/17
        private const val UPDATE_INTERVAL = (10 * 1000).toLong()

        /**
         * The fastest rate for active location updates. Updates will never be more frequent
         * than this value, but they may be less frequent.
         */
        // FIXME: 5/14/17
        private const val FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2

        /**
         * The max time before batched results are delivered by location services. Results may be
         * delivered sooner than this interval.
         */
        private const val MAX_WAIT_TIME = UPDATE_INTERVAL * 3
    }
}
