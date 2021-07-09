package com.XD.fitgain.views

import android.Manifest
import android.annotation.SuppressLint

import android.os.Bundle
import android.util.Log

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.XD.fitgain.R
import com.XD.fitgain.databinding.ActivityBusinessBinding
import com.XD.fitgain.model.Busines
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import kotlinx.android.synthetic.main.activity_map_view.*


class MapViewActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {

    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private lateinit var map: MapboxMap

    private lateinit var busines: Busines
    private lateinit var binding: ActivityBusinessBinding

    private var currentRoute: DirectionsRoute? = null
    private var navigationMapRoute: NavigationMapRoute? = null


    lateinit var originPoint: Point
    lateinit var destinationPoint: Point

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_map_view)

        getDataFromIntent()


        binding = ActivityBusinessBinding.inflate(layoutInflater)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        map = mapboxMap
        map.setStyle(Style.OUTDOORS) {


            // Map is set up and the style has loaded. Now you can add data or make other map adjustments
            enableLocationComponent(it)
            addDestinationIconSymbolLayer(it)


        }

    }

    private fun addDestinationIconSymbolLayer(it: Style) {
        if(busines.categoria == "Tecnologia") {
            val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_laptop_screen, null)
            val bitmapUtils = BitmapUtils.getBitmapFromDrawable(drawable)
            it.addImage(
                "destination-icon-id",
                bitmapUtils!!
            )
        }
        if (busines.categoria == "Restaurantes"){
            val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_cutlery, null)
            val bitmapUtils = BitmapUtils.getBitmapFromDrawable(drawable)
            it.addImage(
                "destination-icon-id",
                bitmapUtils!!
            )
        }
        var geoJsonSource: GeoJsonSource = GeoJsonSource("destination-source-id")
        it.addSource(geoJsonSource)

        var destinationSymbolLayer: SymbolLayer =
            SymbolLayer("destination-symbol-layer-id", "destination-source-id")
        destinationSymbolLayer.withProperties(
            iconImage("destination-icon-id"),
            iconAllowOverlap(true),
            iconIgnorePlacement(true),
            iconAnchor(com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM),
            iconOffset(arrayOf(0f, -16f)),
            textField(busines.nombre),
            textAnchor(com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_BOTTOM),
        )
        it.addLayer(destinationSymbolLayer)
        destinationPoint = Point.fromLngLat(busines.longitud.toDouble(), busines.latitud.toDouble())
        val source = map!!.style!!.getSourceAs<GeoJsonSource>("destination-source-id")
        source?.setGeoJson(Feature.fromGeometry(destinationPoint))
        map!!.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
            .target(LatLng(destinationPoint.latitude(),destinationPoint.longitude()))
            .zoom(16.0)
            .build()), 4000)

    }

    private fun getRoute(originPoint: Point, destinationPoint: Point) {
        NavigationRoute.builder(this)
            .accessToken(Mapbox.getAccessToken()!!)
            .origin(originPoint)
            .destination(destinationPoint)
            .build()
            .getRoute(object : retrofit2.Callback<DirectionsResponse> {
                override fun onResponse(
                    call: retrofit2.Call<DirectionsResponse>,
                    response: retrofit2.Response<DirectionsResponse>
                ) {
                    Log.d("Route", "Response code: " + response.body())
                    if (response.body() == null) {
                        Log.d(
                            "Route",
                            "No routes found, make sure you set the right user and access token"
                        )
                        return
                    } else if (response.body()!!.routes().size < 1) {
                        Log.e("Route", "No routes found")
                        return
                    }
                    currentRoute = response.body()!!.routes()[0]

                    //Draw the route on the map
                    if (navigationMapRoute != null) {
                        navigationMapRoute!!.updateRouteVisibilityTo(false)
                    } else {
                        navigationMapRoute =
                            NavigationMapRoute(null, mapView, map!!, R.style.NavigationMapRoute)
                    }
                    navigationMapRoute!!.addRoute(currentRoute)
                }

                override fun onFailure(call: retrofit2.Call<DirectionsResponse>, t: Throwable) {
                    Log.e("Route", "Error: " + t.message)
                }


            })

    }

    private fun getDataFromIntent() {
        busines = intent.getParcelableExtra("Busines") as Busines

    }


    @SuppressLint("MissingPermission")
    fun enableLocationComponent(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.mapboxGreen))
                .build()

            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(this, loadedMapStyle)
                    .locationComponentOptions(customLocationComponentOptions)
                    .useDefaultLocationEngine(true)
                    .locationEngineRequest(
                        LocationEngineRequest.Builder(750)
                            .setFastestInterval(750)
                            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                            .build()
                    )
                    .build()

            // Get an instance of the LocationComponent and then adjust its settings
            map.locationComponent.apply {

                // Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

                // Enable to make the LocationComponent visible
                isLocationComponentEnabled = true

                // Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

                // Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS
            }

        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG)
            .show()
    }

    fun btnBackClick(v: View) {
        finish();
    }

    fun btnRouteClick(v: View) {
        originPoint = Point.fromLngLat(
            map.locationComponent!!.lastKnownLocation!!.longitude,
            map.locationComponent!!.lastKnownLocation!!.latitude
        )
        Log.d("lastLocation", "location..." + originPoint?.latitude().toString())
        getRoute(originPoint, destinationPoint)
        map!!.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(LatLng(originPoint.latitude(),originPoint.longitude()))
                    .zoom(16.0)
                    .build()), 20000)
        map.locationComponent.cameraMode = CameraMode.TRACKING
    }


    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(map.style!!)
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG)
                .show()
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


}


