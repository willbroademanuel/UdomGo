package com.example.udomgo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;
import org.maplibre.android.MapLibre;
import org.maplibre.android.WellKnownTileServer;
import org.maplibre.android.annotations.Marker;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.annotations.Polyline;
import org.maplibre.android.annotations.PolylineOptions;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.location.LocationComponent;
import org.maplibre.android.location.LocationComponentActivationOptions;
import org.maplibre.android.location.LocationComponentOptions;
import org.maplibre.android.location.modes.CameraMode;
import org.maplibre.android.location.modes.RenderMode;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    // Default UDOM Campus Center coordinates
    private static final LatLng UDOM_CENTER = new LatLng(-6.21650, 35.81150);
    private static final LatLng UDOM_DEFAULT_START = new LatLng(-6.21620, 35.80620); // CIVE Main Gate

    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private LocationComponent locationComponent;

    private EditText mapSearch;
    private ListView searchSuggestions;
    private ImageButton clearSearchButton;
    private ArrayAdapter<String> suggestionAdapter;

    // Route Card UI
    private LinearLayout routeCard;
    private TextView routeDestinationTitle;
    private TextView routeDistanceDetails;
    private Button startRouteButton;
    private ImageButton closeRouteButton;

    // Active Route & Markers
    private Polyline currentRouteLine;
    private Marker destinationMarker;
    private Marker startMarker;
    private MapLocation currentSelectedLocation;

    private final List<MapLocation> locations = new ArrayList<>();

    public MapFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        MapLibre.getInstance(
                requireContext(),
                null,
                WellKnownTileServer.MapLibre
        );

        return inflater.inflate(
                R.layout.fragment_map,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Find views
        mapView = view.findViewById(R.id.mapView);
        mapSearch = view.findViewById(R.id.mapSearch);
        searchSuggestions = view.findViewById(R.id.searchSuggestions);
        clearSearchButton = view.findViewById(R.id.clearSearchButton);

        ImageButton myLocationButton = view.findViewById(R.id.myLocationButton);

        routeCard = view.findViewById(R.id.routeCard);
        routeDestinationTitle = view.findViewById(R.id.routeDestinationTitle);
        routeDistanceDetails = view.findViewById(R.id.routeDistanceDetails);
        startRouteButton = view.findViewById(R.id.startRouteButton);
        closeRouteButton = view.findViewById(R.id.closeRouteButton);

        // Initialize MapView
        mapView.onCreate(savedInstanceState);

        // Load UDOM destinations with real coordinates
        loadUDOMLocations();

        // Setup search & autocomplete
        setupSearch();

        // Setup Route Card actions
        if (startRouteButton != null) {
            startRouteButton.setOnClickListener(v -> {
                if (currentSelectedLocation != null) {
                    calculateAndDrawRoute(currentSelectedLocation);
                }
            });
        }

        if (closeRouteButton != null) {
            closeRouteButton.setOnClickListener(v -> clearActiveRoute());
        }

        // My Location Button
        myLocationButton.setOnClickListener(v -> {
            if (!hasLocationPermission()) {
                requestLocationPermission();
                return;
            }
            returnToMyLocation();
        });

        // Clear Search text
        clearSearchButton.setOnClickListener(v -> {
            mapSearch.setText("");
            mapSearch.requestFocus();
            showKeyboard();
        });

        // Initialize map
        initializeMap();

        // Check location permission
        checkLocationPermission();
    }

    private void loadUDOMLocations() {
        locations.clear();

        // CIVE Buildings & Lecture Rooms
        locations.add(new MapLocation("Lecture Room A", "CIVE", -6.21728, 35.80805));
        locations.add(new MapLocation("Lecture Room B", "CIVE", -6.21745, 35.80852));
        locations.add(new MapLocation("CIVE Auditorium", "CIVE", -6.21693, 35.81109));
        locations.add(new MapLocation("CIVE Block 1", "CIVE", -6.21650, 35.80705));
        locations.add(new MapLocation("CIVE Block 2", "CIVE", -6.21672, 35.80755));
        locations.add(new MapLocation("CIVE Block 3", "CIVE", -6.21685, 35.80812));
        locations.add(new MapLocation("CIVE Block 4", "CIVE", -6.21705, 35.80860));
        locations.add(new MapLocation("CIVE Block 5", "CIVE", -6.21720, 35.80915));
        locations.add(new MapLocation("CIVE Block 6", "CIVE", -6.21740, 35.80965));
        locations.add(new MapLocation("CIVE Cafeteria", "CIVE", -6.21690, 35.80830));
        locations.add(new MapLocation("CIVE Main Gate", "CIVE", -6.21620, 35.80620));

        // UDOM Colleges
        locations.add(new MapLocation("CIVE – Informatics & Virtual Education", "CIVE", -6.21708, 35.80744));
        locations.add(new MapLocation("CoBE – Business and Economics", "CoBE", -6.21405, 35.82405));
        locations.add(new MapLocation("CoED – College of Education", "CoED", -6.21050, 35.81810));
        locations.add(new MapLocation("CHSS – Humanities & Social Sciences", "CHSS", -6.20810, 35.81220));
        locations.add(new MapLocation("CNMS – Natural & Mathematical Sciences", "CNMS", -6.21350, 35.81520));
        locations.add(new MapLocation("CoESE – Earth Sciences & Engineering", "CoESE", -6.21900, 35.80510));

        // Institutes & Schools
        locations.add(new MapLocation("School of Law (SoL)", "SoL", -6.21100, 35.82110));
        locations.add(new MapLocation("School of Medicine (SoMD)", "SoMD", -6.22300, 35.82910));
        locations.add(new MapLocation("School of Nursing & Public Health", "SoNPH", -6.22410, 35.82820));
        locations.add(new MapLocation("Institute of Development Studies (IDS)", "IDS", -6.20950, 35.81350));
        locations.add(new MapLocation("Confucius Institute (CI)", "CI", -6.20880, 35.81420));

        // Central Facilities
        locations.add(new MapLocation("UDOM Central Library", "Central", -6.21250, 35.81950));
        locations.add(new MapLocation("UDOM Administration Block", "Central", -6.21470, 35.82470));
        locations.add(new MapLocation("UDOM Health Centre", "Health", -6.22050, 35.82510));
        locations.add(new MapLocation("UDOM Roundabout", "Transit", -6.21550, 35.81600));
        locations.add(new MapLocation("Student Hostels", "Accommodation", -6.21850, 35.81050));
    }

    private void setupSearch() {
        List<String> searchNames = new ArrayList<>();
        for (MapLocation location : locations) {
            searchNames.add(location.getName());
        }

        suggestionAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        );

        searchSuggestions.setAdapter(suggestionAdapter);

        mapSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase(Locale.ROOT);
                suggestionAdapter.clear();

                if (query.isEmpty()) {
                    searchSuggestions.setVisibility(View.GONE);
                    clearSearchButton.setVisibility(View.GONE);
                    suggestionAdapter.notifyDataSetChanged();
                    return;
                }

                clearSearchButton.setVisibility(View.VISIBLE);

                for (String name : searchNames) {
                    if (name.toLowerCase(Locale.ROOT).contains(query)) {
                        suggestionAdapter.add(name);
                    }
                }

                suggestionAdapter.notifyDataSetChanged();
                searchSuggestions.setVisibility(suggestionAdapter.getCount() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchSuggestions.setOnItemClickListener((parent, view, position, id) -> {
            String selected = suggestionAdapter.getItem(position);
            if (selected == null) return;

            mapSearch.setText(selected);
            searchSuggestions.setVisibility(View.GONE);
            hideKeyboard();

            moveToUDOMLocation(selected);
        });
    }

    private void initializeMap() {
        if (mapView == null) return;

        mapView.getMapAsync(map -> {
            mapLibreMap = map;

            // OpenFreeMap Liberty style
            map.setStyle("https://tiles.openfreemap.org/styles/liberty", style -> {
                // Focus on UDOM Campus
                CameraPosition initialPosition = new CameraPosition.Builder()
                        .target(UDOM_CENTER)
                        .zoom(15.5)
                        .build();

                mapLibreMap.setCameraPosition(initialPosition);

                if (hasLocationPermission()) {
                    setupLocationComponent(style);
                }
            });
        });
    }

    private void setupLocationComponent(@NonNull Style style) {
        if (!hasLocationPermission() || mapLibreMap == null) return;

        if (locationComponent != null && locationComponent.isLocationComponentActivated()) {
            locationComponent.setLocationComponentEnabled(true);
            return;
        }

        locationComponent = mapLibreMap.getLocationComponent();

        LocationComponentOptions options = LocationComponentOptions.builder(requireContext())
                .pulseEnabled(true)
                .trackingGesturesManagement(true)
                .build();

        LocationComponentActivationOptions activationOptions = LocationComponentActivationOptions
                .builder(requireContext(), style)
                .locationComponentOptions(options)
                .useDefaultLocationEngine(true)
                .build();

        locationComponent.activateLocationComponent(activationOptions);
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setRenderMode(RenderMode.NORMAL);
    }

    private void moveToUDOMLocation(String locationName) {
        if (mapLibreMap == null) return;

        MapLocation selectedLocation = null;
        for (MapLocation location : locations) {
            if (location.getName().equalsIgnoreCase(locationName)) {
                selectedLocation = location;
                break;
            }
        }

        if (selectedLocation == null) return;
        currentSelectedLocation = selectedLocation;

        LatLng destination = new LatLng(selectedLocation.getLatitude(), selectedLocation.getLongitude());

        // Stop live tracking
        if (locationComponent != null && locationComponent.isLocationComponentActivated()) {
            locationComponent.setCameraMode(CameraMode.NONE);
        }

        // Animate to location
        mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 17.5), 700);

        // Place Destination Pin
        if (destinationMarker != null) {
            mapLibreMap.removeMarker(destinationMarker);
        }
        destinationMarker = mapLibreMap.addMarker(
                new MarkerOptions()
                        .position(destination)
                        .title("📍 " + selectedLocation.getName())
                        .snippet(selectedLocation.getCollege())
        );

        // Display Route Card
        displayRouteCard(selectedLocation);
    }

    private void displayRouteCard(MapLocation location) {
        if (routeCard == null) return;

        routeDestinationTitle.setText(location.getName());

        LatLng start = getRouteStartPoint();
        float[] distResults = new float[1];
        Location.distanceBetween(
                start.getLatitude(), start.getLongitude(),
                location.getLatitude(), location.getLongitude(),
                distResults
        );

        int distance = Math.round(distResults[0]);
        int minutes = Math.max(1, Math.round(distance / 75.0f)); // ~75m/min walking speed

        routeDistanceDetails.setText("~" + distance + " m • " + minutes + " min walk from "
                + (isLiveLocationActive() ? "Your Location" : "Campus Entrance"));

        startRouteButton.setText("🚶 Calculate Route Line");
        routeCard.setVisibility(View.VISIBLE);
    }

    private LatLng getRouteStartPoint() {
        if (isLiveLocationActive()) {
            Location userLocation = locationComponent.getLastKnownLocation();
            if (userLocation != null) {
                return new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            }
        }
        return UDOM_DEFAULT_START;
    }

    private boolean isLiveLocationActive() {
        return hasLocationPermission()
                && locationComponent != null
                && locationComponent.isLocationComponentActivated()
                && locationComponent.getLastKnownLocation() != null;
    }

    private static class RouteData {
        final List<LatLng> points;
        final int distanceMeters;
        final int durationMinutes;

        RouteData(List<LatLng> points, int distanceMeters, int durationMinutes) {
            this.points = points;
            this.distanceMeters = distanceMeters;
            this.durationMinutes = durationMinutes;
        }
    }

    private void calculateAndDrawRoute(MapLocation destinationLoc) {
        LatLng start = getRouteStartPoint();
        LatLng destination = new LatLng(destinationLoc.getLatitude(), destinationLoc.getLongitude());

        startRouteButton.setText("⏳ Routing along roads...");
        startRouteButton.setEnabled(false);

        // Fetch walking route via OSRM in background thread
        new Thread(() -> {
            RouteData routeData = fetchOsrmWalkingRoute(start, destination);

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (!isAdded() || mapLibreMap == null) return;

                startRouteButton.setEnabled(true);
                startRouteButton.setText("✓ Route Active (" + routeData.distanceMeters + " m)");

                if (routeDistanceDetails != null) {
                    routeDistanceDetails.setText("~" + routeData.distanceMeters + " m • " + routeData.durationMinutes + " min walk from "
                            + (isLiveLocationActive() ? "Your Location" : "Campus Entrance"));
                }

                // Remove existing route and markers
                if (currentRouteLine != null) {
                    mapLibreMap.removePolyline(currentRouteLine);
                }
                if (startMarker != null) {
                    mapLibreMap.removeMarker(startMarker);
                }

                // Add Polyline to map following roads
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(routeData.points)
                        .color(Color.parseColor("#0284C7"))
                        .width(6.5f);

                currentRouteLine = mapLibreMap.addPolyline(polylineOptions);

                // Add Start marker
                startMarker = mapLibreMap.addMarker(
                        new MarkerOptions()
                                .position(start)
                                .title("🟢 Start Point (" + (isLiveLocationActive() ? "My Location" : "CIVE Gate") + ")")
                );

                // Fit entire route on screen
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                for (LatLng point : routeData.points) {
                    boundsBuilder.include(point);
                }

                try {
                    int padding = (int) (110 * getResources().getDisplayMetrics().density);
                    mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), padding), 800);
                } catch (Exception e) {
                    mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 16.5), 700);
                }
            });
        }).start();
    }

    private RouteData fetchOsrmWalkingRoute(LatLng start, LatLng destination) {
        // 1. Primary: OpenStreetMap dedicated pedestrian/foot router (tracks all campus footpaths and walkways)
        String osmFootUrl = String.format(
                Locale.US,
                "https://routing.openstreetmap.de/routed-foot/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                start.getLongitude(), start.getLatitude(),
                destination.getLongitude(), destination.getLatitude()
        );
        RouteData data = requestRouteFromUrl(osmFootUrl, start, destination);

        // 2. Secondary: OSRM foot profile
        if (data == null || data.points.isEmpty()) {
            String osrmFootUrl = String.format(
                    Locale.US,
                    "https://router.project-osrm.org/route/v1/foot/%f,%f;%f,%f?overview=full&geometries=geojson",
                    start.getLongitude(), start.getLatitude(),
                    destination.getLongitude(), destination.getLatitude()
            );
            data = requestRouteFromUrl(osrmFootUrl, start, destination);
        }

        // 3. Tertiary: OSRM driving profile (for major campus vehicle roads)
        if (data == null || data.points.isEmpty()) {
            String osrmDrivingUrl = String.format(
                    Locale.US,
                    "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                    start.getLongitude(), start.getLatitude(),
                    destination.getLongitude(), destination.getLatitude()
            );
            data = requestRouteFromUrl(osrmDrivingUrl, start, destination);
        }

        if (data != null && !data.points.isEmpty()) {
            return data;
        }

        // Fallback if device is offline
        List<LatLng> fallback = new ArrayList<>();
        fallback.add(start);
        fallback.add(destination);

        float[] distResults = new float[1];
        Location.distanceBetween(
                start.getLatitude(), start.getLongitude(),
                destination.getLatitude(), destination.getLongitude(),
                distResults
        );
        int dist = Math.round(distResults[0]);
        int minutes = Math.max(1, Math.round(dist / 75.0f));

        return new RouteData(fallback, dist, minutes);
    }

    private RouteData requestRouteFromUrl(String urlString, LatLng start, LatLng destination) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "UDOMGo-CampusApp/1.0 (Android; Mobile) AppleWebKit/537.36");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                if ("Ok".equalsIgnoreCase(json.optString("code"))) {
                    JSONArray routes = json.getJSONArray("routes");
                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        double distance = route.optDouble("distance", 0.0);

                        JSONObject geometry = route.getJSONObject("geometry");
                        JSONArray coordinates = geometry.getJSONArray("coordinates");
                        List<LatLng> points = new ArrayList<>();

                        // Connect user start position to the nearest road node
                        points.add(start);

                        for (int i = 0; i < coordinates.length(); i++) {
                            JSONArray coord = coordinates.getJSONArray(i);
                            double lng = coord.getDouble(0);
                            double lat = coord.getDouble(1);
                            points.add(new LatLng(lat, lng));
                        }

                        // Connect the road node to the destination building pin
                        points.add(destination);

                        int distMeters = (int) Math.round(distance);
                        int durMinutes = Math.max(1, (int) Math.round(distMeters / 75.0)); // ~75m/min walking speed
                        return new RouteData(points, distMeters, durMinutes);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void clearActiveRoute() {
        if (mapLibreMap != null) {
            if (currentRouteLine != null) {
                mapLibreMap.removePolyline(currentRouteLine);
                currentRouteLine = null;
            }
            if (destinationMarker != null) {
                mapLibreMap.removeMarker(destinationMarker);
                destinationMarker = null;
            }
            if (startMarker != null) {
                mapLibreMap.removeMarker(startMarker);
                startMarker = null;
            }
        }

        if (routeCard != null) {
            routeCard.setVisibility(View.GONE);
        }

        currentSelectedLocation = null;
    }

    private void returnToMyLocation() {
        if (!hasLocationPermission() || locationComponent == null || mapLibreMap == null) return;

        Location userLocation = locationComponent.getLastKnownLocation();
        if (userLocation == null) {
            Toast.makeText(requireContext(), "Waiting for GPS signal...", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng currentPosition = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        locationComponent.setCameraMode(CameraMode.NONE);
        mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 17.0), 700);
    }

    private boolean hasLocationPermission() {
        Context context = getContext();
        if (context == null) return false;

        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void checkLocationPermission() {
        if (hasLocationPermission()) {
            if (mapLibreMap != null) {
                Style style = mapLibreMap.getStyle();
                if (style != null) {
                    setupLocationComponent(style);
                }
            }
        } else {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        if (!isAdded()) return;
        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST) return;

        if (hasLocationPermission() && mapLibreMap != null) {
            Style style = mapLibreMap.getStyle();
            if (style != null) {
                setupLocationComponent(style);
            }
        }
    }

    private void showKeyboard() {
        InputMethodManager keyboard = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) {
            keyboard.showSoftInput(mapSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        if (mapSearch == null) return;
        InputMethodManager keyboard = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) {
            keyboard.hideSoftInputFromWindow(mapSearch.getWindowToken(), 0);
        }
        mapSearch.clearFocus();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mapView != null) mapView.onStop();
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        if (mapView != null) mapView.onDestroy();
        mapView = null;
        mapLibreMap = null;
        locationComponent = null;
        super.onDestroyView();
    }
}
