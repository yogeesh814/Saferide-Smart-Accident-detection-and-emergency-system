package com.example.saferide.services;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;

import com.example.saferide.utils.PermissionHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.List;

public class LocationService {
    private static final long FUSED_LOCATION_TIMEOUT_MS = 5_000;
    private static final long NATIVE_LOCATION_TIMEOUT_MS = 4_000;

    public interface LocationCallback {
        void onLocation(Location location);

        void onError(String message);
    }

    private final Context context;
    private final FusedLocationProviderClient fusedClient;

    public LocationService(Context context) {
        this.context = context;
        this.fusedClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void getCurrentLocation(LocationCallback callback) {
        if (!PermissionHelper.hasLocationPermission(context)) {
            callback.onError("Location permission is required.");
            return;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onError("Location permission is required.");
            return;
        }

        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocation(location);
                    } else {
                        requestFreshLocation(callback);
                    }
                })
                .addOnFailureListener(e -> requestFreshLocation(callback));
    }

    private void requestFreshLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onError("Location permission is required.");
            return;
        }

        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(500)
                .setMaxUpdates(2)
                .setDurationMillis(FUSED_LOCATION_TIMEOUT_MS)
                .build();

        final boolean[] delivered = {false};
        com.google.android.gms.location.LocationCallback googleCallback = new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                Location location = result == null ? null : result.getLastLocation();
                if (location != null && !delivered[0]) {
                    delivered[0] = true;
                    fusedClient.removeLocationUpdates(this);
                    callback.onLocation(location);
                }
            }
        };

        fusedClient.requestLocationUpdates(request, googleCallback, Looper.getMainLooper())
                .addOnFailureListener(e -> getLastKnownLocation(callback));

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!delivered[0]) {
                fusedClient.removeLocationUpdates(googleCallback);
                requestNativeFreshLocation(callback);
            }
        }, FUSED_LOCATION_TIMEOUT_MS + 1_000);
    }

    private void getLastKnownLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onError("Location permission is required.");
            return;
        }
        fusedClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Location nativeLocation = getNativeLastKnownLocation();
                        if (nativeLocation != null) {
                            callback.onLocation(nativeLocation);
                        } else {
                            callback.onError("Unable to get GPS location. Turn on Location/GPS and try outdoors.");
                        }
                    } else {
                        callback.onLocation(location);
                    }
                })
                .addOnFailureListener(e -> {
                    Location nativeLocation = getNativeLastKnownLocation();
                    if (nativeLocation != null) {
                        callback.onLocation(nativeLocation);
                    } else {
                        callback.onError(e.getMessage());
                    }
                });
    }

    private void requestNativeFreshLocation(LocationCallback callback) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            getLastKnownLocation(callback);
            return;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onError("Location permission is required.");
            return;
        }

        String provider = chooseNativeProvider(manager);
        if (provider == null) {
            getLastKnownLocation(callback);
            return;
        }

        Handler handler = new Handler(Looper.getMainLooper());
        final boolean[] delivered = {false};
        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null && !delivered[0]) {
                    delivered[0] = true;
                    manager.removeUpdates(this);
                    callback.onLocation(location);
                }
            }
        };

        try {
            manager.requestLocationUpdates(provider, 1000, 0, listener, Looper.getMainLooper());
            handler.postDelayed(() -> {
                if (!delivered[0]) {
                    manager.removeUpdates(listener);
                    getLastKnownLocation(callback);
                }
            }, NATIVE_LOCATION_TIMEOUT_MS);
        } catch (Exception error) {
            getLastKnownLocation(callback);
        }
    }

    private String chooseNativeProvider(LocationManager manager) {
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        }
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        }

        List<String> providers = manager.getProviders(true);
        return providers.isEmpty() ? null : providers.get(0);
    }

    private Location getNativeLastKnownLocation() {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            return null;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        Location best = null;
        for (String provider : manager.getProviders(true)) {
            try {
                Location location = manager.getLastKnownLocation(provider);
                if (location != null && (best == null || location.getAccuracy() < best.getAccuracy())) {
                    best = location;
                }
            } catch (Exception ignored) {
            }
        }
        return best;
    }

    public static String mapsLink(double latitude, double longitude) {
        return "https://maps.google.com/?q=" + latitude + "," + longitude;
    }

    public void shareLocation(Activity activity, double latitude, double longitude) {
        String message = "SafeRide live location: " + mapsLink(latitude, longitude);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        activity.startActivity(Intent.createChooser(intent, "Share SafeRide location"));
    }

    public void openNearbyHospitals(Context context, double latitude, double longitude) {
        Uri uri = Uri.parse("geo:" + latitude + "," + longitude + "?q=hospitals");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException error) {
            Intent browserIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/hospitals/@" + latitude + "," + longitude + ",14z")
            );
            context.startActivity(browserIntent);
        }
    }
}
