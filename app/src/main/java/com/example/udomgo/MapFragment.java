
package com.example.udomgo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.maplibre.android.MapLibre;
import org.maplibre.android.WellKnownTileServer;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.location.LocationComponent;
import org.maplibre.android.location.LocationComponentActivationOptions;
import org.maplibre.android.location.LocationComponentOptions;
import org.maplibre.android.location.modes.CameraMode;
import org.maplibre.android.location.modes.RenderMode;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private LocationComponent locationComponent;

    private EditText mapSearch;
    private ListView searchSuggestions;
    private ImageButton clearSearchButton;

    private ArrayAdapter<String> suggestionAdapter;

    private final List<MapLocation> locations = new ArrayList<>();


    // ============================================================
    // FRAGMENT CREATION
    // ============================================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        /*
         * IMPORTANT:
         *
         * MapLibre MUST be initialized before the XML containing
         * MapView is inflated.
         *
         * MapLibre 13.6.0 supports this three-argument form.
         */
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


    // ============================================================
    // VIEW CREATED
    // ============================================================

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // --------------------------------------------------------
        // FIND VIEWS
        // --------------------------------------------------------

        mapView = view.findViewById(R.id.mapView);
        mapSearch = view.findViewById(R.id.mapSearch);
        searchSuggestions = view.findViewById(R.id.searchSuggestions);
        clearSearchButton = view.findViewById(R.id.clearSearchButton);

        ImageButton myLocationButton =
                view.findViewById(R.id.myLocationButton);


        // --------------------------------------------------------
        // MAPVIEW LIFECYCLE
        // --------------------------------------------------------

        mapView.onCreate(savedInstanceState);


        // --------------------------------------------------------
        // LOAD UDOM LOCATIONS
        // --------------------------------------------------------

        loadUDOMLocations();


        // --------------------------------------------------------
        // SEARCH
        // --------------------------------------------------------

        setupSearch();


        // --------------------------------------------------------
        // MY LOCATION BUTTON
        // --------------------------------------------------------

        myLocationButton.setOnClickListener(
                v -> {

                    /*
                     * IMPORTANT:
                     *
                     * Check permission BEFORE touching
                     * LocationComponent.
                     */
                    if (!hasLocationPermission()) {

                        requestLocationPermission();

                        return;
                    }

                    returnToMyLocation();
                }
        );


        // --------------------------------------------------------
        // CLEAR SEARCH
        // --------------------------------------------------------

        clearSearchButton.setOnClickListener(v -> {

            mapSearch.setText("");

            mapSearch.requestFocus();

            showKeyboard();
        });


        // --------------------------------------------------------
        // START MAP
        // --------------------------------------------------------

        initializeMap();

        /*
         * Request permission separately.
         *
         * The map itself does NOT depend on location permission.
         */
        checkLocationPermission();
    }


    // ============================================================
    // UDOM LOCATIONS
    // ============================================================

    private void loadUDOMLocations() {

        locations.clear();

        /*
         * ========================================================
         * CIVE
         * ========================================================
         *
         * These are the detailed CIVE destinations.
         *
         * Coordinates will be filled with the real coordinates
         * when we reach the coordinate stage.
         */

        locations.add(
                new MapLocation(
                        "Lecture Room A",
                        "CIVE",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "Lecture Room B",
                        "CIVE",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "Auditorium",
                        "CIVE",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "CIVE Block 1",
                        "CIVE",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "CIVE Block 2",
                        "CIVE",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "CIVE Block 3",
                        "CIVE",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "CIVE Block 4",
                        "CIVE",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "CIVE Block 5",
                        "CIVE",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "CIVE Block 6",
                        "CIVE",
                        0.0,
                        0.0
                )
        );


        /*
         * ========================================================
         * OTHER UDOM COLLEGES
         * ========================================================
         *
         * These are destination-level locations only.
         *
         * We are NOT adding internal buildings for them yet.
         */

        locations.add(
                new MapLocation(
                        "College of Informatics and Virtual Education",
                        "CIVE",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "College of Business Education",
                        "CoBE",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "College of Engineering and Technology",
                        "CoET",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "College of Education",
                        "CoED",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "College of Health Sciences",
                        "CHS",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "College of Natural and Mathematical Sciences",
                        "CNMS",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "College of Humanities and Social Sciences",
                        "CHSS",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "Institute of Development Studies",
                        "IDS",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "School of Law",
                        "School of Law",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "School of Medicine",
                        "School of Medicine",
                        0.0,
                        0.0
                )
        );

        locations.add(
                new MapLocation(
                        "School of Nursing and Public Health",
                        "School of Nursing and Public Health",
                        0.0,
                        0.0
                )
        );
    }


    // ============================================================
    // SEARCH
    // ============================================================

    private void setupSearch() {

        List<String> searchNames = new ArrayList<>();

        for (MapLocation location : locations) {
            searchNames.add(location.getName());
        }

        suggestionAdapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        new ArrayList<>()
                );

        searchSuggestions.setAdapter(suggestionAdapter);


        // --------------------------------------------------------
        // USER TYPES
        // --------------------------------------------------------

        mapSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }


                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        String query =
                                s.toString()
                                        .trim()
                                        .toLowerCase(Locale.ROOT);

                        suggestionAdapter.clear();


                        if (query.isEmpty()) {

                            searchSuggestions.setVisibility(
                                    View.GONE
                            );

                            clearSearchButton.setVisibility(
                                    View.GONE
                            );

                            suggestionAdapter.notifyDataSetChanged();

                            return;
                        }


                        clearSearchButton.setVisibility(
                                View.VISIBLE
                        );


                        for (String name : searchNames) {

                            if (name
                                    .toLowerCase(Locale.ROOT)
                                    .contains(query)) {

                                suggestionAdapter.add(name);
                            }
                        }


                        suggestionAdapter.notifyDataSetChanged();


                        if (suggestionAdapter.getCount() > 0) {

                            searchSuggestions.setVisibility(
                                    View.VISIBLE
                            );

                        } else {

                            searchSuggestions.setVisibility(
                                    View.GONE
                            );
                        }
                    }


                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );


        // --------------------------------------------------------
        // USER SELECTS RESULT
        // --------------------------------------------------------

        searchSuggestions.setOnItemClickListener(
                (parent, view, position, id) -> {

                    String selectedLocation =
                            suggestionAdapter.getItem(position);

                    if (selectedLocation == null) {
                        return;
                    }


                    mapSearch.setText(
                            selectedLocation
                    );


                    searchSuggestions.setVisibility(
                            View.GONE
                    );


                    hideKeyboard();


                    moveToUDOMLocation(
                            selectedLocation
                    );
                }
        );
    }


    // ============================================================
    // LOCATION PERMISSION
    // ============================================================

    private boolean hasLocationPermission() {

        Context context = requireContext();

        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;
    }


    private void checkLocationPermission() {

        if (hasLocationPermission()) {

            /*
             * Permission already exists.
             *
             * If the map is ready, activate location now.
             */
            if (mapLibreMap != null) {

                Style style =
                        mapLibreMap.getStyle();

                if (style != null) {
                    setupLocationComponent(style);
                }
            }

        } else {

            requestLocationPermission();
        }
    }


    private void requestLocationPermission() {

        if (!isAdded()) {
            return;
        }

        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST
        );
    }


    // ============================================================
    // PERMISSION RESULT
    // ============================================================

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );


        if (requestCode != LOCATION_PERMISSION_REQUEST) {
            return;
        }


        if (hasLocationPermission()) {

            /*
             * Map is already initialized.
             *
             * Activate the location component if
             * the style is ready.
             */
            if (mapLibreMap != null) {

                Style style =
                        mapLibreMap.getStyle();

                if (style != null) {

                    setupLocationComponent(style);
                }
            }

        } else {

            Toast.makeText(
                    requireContext(),
                    "Location permission is needed for live location.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // ============================================================
    // INITIALIZE MAP
    // ============================================================

    private void initializeMap() {

        if (mapView == null) {
            return;
        }


        mapView.getMapAsync(
                map -> {

                    mapLibreMap = map;


                    /*
                     * OpenFreeMap Liberty style.
                     *
                     * This gives us the detailed streets,
                     * buildings and map information.
                     */
                    map.setStyle(
                            "https://tiles.openfreemap.org/styles/liberty",
                            style -> {

                                /*
                                 * Set the initial camera first.
                                 *
                                 * This does NOT require location
                                 * permission.
                                 */
                                CameraPosition initialPosition =
                                        new CameraPosition.Builder()
                                                .target(
                                                        new LatLng(
                                                                -6.1649,
                                                                35.7516
                                                        )
                                                )
                                                .zoom(15.0)
                                                .build();


                                mapLibreMap.setCameraPosition(
                                        initialPosition
                                );


                                /*
                                 * Only activate live location
                                 * when permission actually exists.
                                 */
                                if (hasLocationPermission()) {

                                    setupLocationComponent(
                                            style
                                    );
                                }
                            }
                    );
                }
        );
    }


    // ============================================================
    // LIVE LOCATION
    // ============================================================

    private void setupLocationComponent(
            @NonNull Style style) {

        /*
         * NEVER activate the location component without
         * permission.
         */
        if (!hasLocationPermission()) {
            return;
        }


        if (mapLibreMap == null) {
            return;
        }


        /*
         * Prevent activating the component multiple times.
         */
        if (locationComponent != null
                && locationComponent.isLocationComponentActivated()) {

            locationComponent.setLocationComponentEnabled(
                    true
            );

            return;
        }


        locationComponent =
                mapLibreMap.getLocationComponent();


        LocationComponentOptions options =
                LocationComponentOptions
                        .builder(requireContext())
                        .pulseEnabled(true)
                        .trackingGesturesManagement(true)
                        .build();


        LocationComponentActivationOptions activationOptions =
                LocationComponentActivationOptions
                        .builder(
                                requireContext(),
                                style
                        )
                        .locationComponentOptions(
                                options
                        )
                        .useDefaultLocationEngine(true)
                        .build();


        locationComponent.activateLocationComponent(
                activationOptions
        );


        /*
         * Permission has already been checked above,
         * so it is safe to enable the component.
         */
        locationComponent.setLocationComponentEnabled(
                true
        );


        /*
         * Normal mode shows the user's current location
         * without forcing the camera to follow them forever.
         */
        locationComponent.setRenderMode(
                RenderMode.NORMAL
        );
    }


    // ============================================================
    // RETURN TO LIVE LOCATION
    // ============================================================

    private void returnToMyLocation() {

        /*
         * Explicit permission check.
         *
         * This is important because Android Studio's
         * "Call requires permission" warning comes from
         * location-related methods.
         */
        if (!hasLocationPermission()) {

            requestLocationPermission();

            return;
        }


        if (locationComponent == null ||
                mapLibreMap == null) {

            return;
        }


        /*
         * Get the last known location only after
         * permission has been confirmed.
         */
        Location userLocation =
                locationComponent.getLastKnownLocation();


        if (userLocation == null) {

            Toast.makeText(
                    requireContext(),
                    "Waiting for your location...",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        LatLng currentPosition =
                new LatLng(
                        userLocation.getLatitude(),
                        userLocation.getLongitude()
                );


        /*
         * Stop camera tracking temporarily so we can
         * explicitly move the camera.
         */
        locationComponent.setCameraMode(
                CameraMode.NONE
        );


        mapLibreMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        currentPosition,
                        17.0
                ),
                700
        );
    }


    // ============================================================
    // MOVE TO SEARCHED UDOM LOCATION
    // ============================================================

    private void moveToUDOMLocation(
            String locationName) {

        if (mapLibreMap == null) {
            return;
        }


        MapLocation selectedLocation = null;


        for (MapLocation location : locations) {

            if (location
                    .getName()
                    .equalsIgnoreCase(locationName)) {

                selectedLocation = location;

                break;
            }
        }


        if (selectedLocation == null) {
            return;
        }


        /*
         * We deliberately do NOT use coordinates that are
         * 0.0 / 0.0.
         *
         * Real UDOM coordinates will be inserted later.
         */
        if (selectedLocation.getLatitude() == 0.0
                && selectedLocation.getLongitude() == 0.0) {

            Toast.makeText(
                    requireContext(),
                    selectedLocation.getName()
                            + " coordinates have not been added yet.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        LatLng destination =
                new LatLng(
                        selectedLocation.getLatitude(),
                        selectedLocation.getLongitude()
                );


        /*
         * Searching for a destination means the camera
         * should stop following the user's location.
         */
        if (locationComponent != null
                && locationComponent.isLocationComponentActivated()) {

            locationComponent.setCameraMode(
                    CameraMode.NONE
            );
        }


        mapLibreMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        destination,
                        18.0
                ),
                700
        );
    }


    // ============================================================
    // KEYBOARD
    // ============================================================

    private void showKeyboard() {

        InputMethodManager keyboard =
                (InputMethodManager)
                        requireContext()
                                .getSystemService(
                                        Context.INPUT_METHOD_SERVICE
                                );


        if (keyboard != null) {

            keyboard.showSoftInput(
                    mapSearch,
                    InputMethodManager.SHOW_IMPLICIT
            );
        }
    }


    private void hideKeyboard() {

        InputMethodManager keyboard =
                (InputMethodManager)
                        requireContext()
                                .getSystemService(
                                        Context.INPUT_METHOD_SERVICE
                                );


        if (keyboard != null) {

            keyboard.hideSoftInputFromWindow(
                    mapSearch.getWindowToken(),
                    0
            );
        }


        mapSearch.clearFocus();
    }


    // ============================================================
    // MAPVIEW LIFECYCLE
    // ============================================================

    @Override
    public void onStart() {

        super.onStart();

        if (mapView != null) {
            mapView.onStart();
        }
    }


    @Override
    public void onResume() {

        super.onResume();

        if (mapView != null) {
            mapView.onResume();
        }
    }


    @Override
    public void onPause() {

        if (mapView != null) {
            mapView.onPause();
        }

        super.onPause();
    }


    @Override
    public void onStop() {

        if (mapView != null) {
            mapView.onStop();
        }

        super.onStop();
    }


    @Override
    public void onLowMemory() {

        super.onLowMemory();

        if (mapView != null) {
            mapView.onLowMemory();
        }
    }


    @Override
    public void onSaveInstanceState(
            @NonNull Bundle outState) {

        super.onSaveInstanceState(outState);

        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }


    @Override
    public void onDestroyView() {

        if (mapView != null) {
            mapView.onDestroy();
        }

        mapView = null;
        mapLibreMap = null;
        locationComponent = null;

        super.onDestroyView();
    }
}

