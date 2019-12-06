# AltBeacon

This is an Integration sample for Android AltBeacon Library.

The Project Can use for the following purposes

* Monitor beacons
* Range beacons
* find nearby beacons
* Stop ranging and monitoring

### Authorization

You can add Beacon Library Gradle dependency to the Project build Gradle file and Sync. After that, you can use Beacon API inside Project.
`````
implementation 'org.altbeacon:android-beacon-library:2.12'

`````

For Permission Handling, you can use the below library or you can create permission handling manually.

`````
implementation 'com.nabinbhandari.android:permissions:3.8'

```````

```kotlin
private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
...
	private fun checkLocationPermission() {
            val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            Permissions.check(this/*context*/, permissions, null/*options*/, null, object : PermissionHandler() {
                override fun onGranted() {
                    initializeGeoFence()
                    initializeBeacon()
                    startScan()
                    setAdapter()
                }
            })
        }
```

### About
Nithinjith Pushpakaran
