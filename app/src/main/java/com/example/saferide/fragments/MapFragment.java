package com.example.saferide.fragments;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.saferide.R;
import com.example.saferide.services.LocationService;
import com.example.saferide.utils.PermissionHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private ProgressBar loading;
    private TextView locationStatusText;
    private TextView coordinatesText;
    private View openMapsButton;
    private LocationService locationService;
    private double currentLat = Double.NaN;
    private double currentLng = Double.NaN;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        loading = view.findViewById(R.id.mapLoading);
        locationStatusText = view.findViewById(R.id.locationStatusText);
        coordinatesText = view.findViewById(R.id.coordinatesText);
        openMapsButton = view.findViewById(R.id.openMapsButton);
        locationService = new LocationService(requireContext());

        if (hasRealMapsKey()) {
            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, mapFragment)
                    .commitNow();
            mapFragment.getMapAsync(this);
        } else {
            view.findViewById(R.id.mapContainer).setVisibility(View.GONE);
            locationStatusText.setText("Live location mode");
            coordinatesText.setText("Google Maps API key not added, showing coordinates for demo.");
            loadCurrentLocation();
        }

        view.findViewById(R.id.shareLocationButton).setOnClickListener(v -> shareLocation());
        view.findViewById(R.id.hospitalsButton).setOnClickListener(v -> openHospitals());
        openMapsButton.setOnClickListener(v -> openCurrentInMaps());
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        loadCurrentLocation();
    }

    private void loadCurrentLocation() {
        if (!PermissionHelper.hasLocationPermission(requireContext())) {
            loading.setVisibility(View.GONE);
            PermissionHelper.requestLocationPermission(requireActivity());
            Toast.makeText(requireContext(), "Allow location permission to show live tracking.", Toast.LENGTH_LONG).show();
            return;
        }

        locationService.getCurrentLocation(new LocationService.LocationCallback() {
            @Override
            public void onLocation(Location location) {
                if (!isAdded()) {
                    return;
                }
                loading.setVisibility(View.GONE);
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                showCoordinates();
                if (googleMap != null) {
                    LatLng current = new LatLng(currentLat, currentLng);
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(current).title("You are here"));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 16f));
                    try {
                        googleMap.setMyLocationEnabled(true);
                    } catch (SecurityException ignored) {
                        // Permission was checked above.
                    }
                }
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                loading.setVisibility(View.GONE);
                locationStatusText.setText("Location not found");
                coordinatesText.setText(message + "\nTurn ON GPS, allow precise location, then tap Share location.");
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void shareLocation() {
        if (Double.isNaN(currentLat) || Double.isNaN(currentLng)) {
            Toast.makeText(requireContext(), "Getting location first...", Toast.LENGTH_SHORT).show();
            loadCurrentLocation();
            return;
        }
        locationService.shareLocation(requireActivity(), currentLat, currentLng);
    }

    private void openHospitals() {
        if (Double.isNaN(currentLat) || Double.isNaN(currentLng)) {
            Toast.makeText(requireContext(), "Getting location first...", Toast.LENGTH_SHORT).show();
            loadCurrentLocation();
            return;
        }
        locationService.openNearbyHospitals(requireContext(), currentLat, currentLng);
    }

    private boolean hasRealMapsKey() {
        String key = getString(R.string.google_maps_key);
        return key != null && !key.contains("TODO") && key.trim().length() > 20;
    }

    private void showCoordinates() {
        locationStatusText.setText("Location found");
        coordinatesText.setText("Latitude: " + currentLat + "\nLongitude: " + currentLng + "\n" + LocationService.mapsLink(currentLat, currentLng));
        openMapsButton.setVisibility(View.VISIBLE);
    }

    private void openCurrentInMaps() {
        if (Double.isNaN(currentLat) || Double.isNaN(currentLng)) {
            loadCurrentLocation();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + currentLat + "," + currentLng + "?q=" + currentLat + "," + currentLng + "(SafeRide location)"));
        startActivity(intent);
    }
}
