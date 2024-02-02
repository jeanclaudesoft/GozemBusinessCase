package com.claudylab.gozem

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.claudylab.gozem.model.Content
import com.claudylab.gozem.model.Section
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.io.InputStream

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var gogleMap: GoogleMap

    private var webSocket: WebSocket? = null

    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private var latitude = 0.0
    private var longitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val json = loadJsonFromResources()
        if (json.isNotBlank()) {
            val gson = Gson()
            val listType = object : TypeToken<List<Section>>() {}.type
            val homePageData: List<Section> = gson.fromJson(json, listType)
            displaySections(homePageData)

        }


    }

    private fun displaySections(sections: List<Section>) {
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)

        for (section in sections) {
            when (section.type) {
                "profile" -> {
                    val profileView = createProfileView(section.content)
                    linearLayout.addView(profileView)
                }

                "map" -> {
                    val mapView = createMapView(section.content)
                    linearLayout.addView(mapView)
                }

                "data" -> {
                    val dataView = createDataView(section.content)
                    linearLayout.addView(dataView)
                }
            }
        }
    }

    private fun createProfileView(content: Content): View {

        val profileView = LayoutInflater.from(this).inflate(R.layout.profile_section_layout, null)


        val imageView = profileView.findViewById<CircleImageView>(R.id.profileImageView)
        val nameTextView = profileView.findViewById<TextView>(R.id.nameTextView)
        val emailTextView = profileView.findViewById<TextView>(R.id.emailTextView)

        Picasso.get().load(content.image).into(imageView)
        nameTextView.text = content.name
        emailTextView.text = content.email



        return profileView
    }

    private fun createMapView(content: Content): View {
        val mapView = LayoutInflater.from(this).inflate(R.layout.map_section_layout, null)


        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        latitude = content.lat!!
        longitude = content.lng!!
        return mapView
    }


    private fun connectToWebSocket(homePageData: Content, text: TextView) {
        val dataSection = homePageData

        val webSocketUrl = dataSection.source

        val request = Request.Builder().url(webSocketUrl!!).build()
        val listener = WebSocketListenerImpl(dataSection, text)

        val client = OkHttpClient()
        webSocket = client.newWebSocket(request, listener)
    }


    private fun createDataView(content: Content): View {
        val dataView = LayoutInflater.from(this).inflate(R.layout.data_section, null)

        val titleTextView = dataView.findViewById<TextView>(R.id.textTitle)
        val source = dataView.findViewById<TextView>(R.id.textSource)
        val valueTextView = dataView.findViewById<TextView>(R.id.textValue)

        titleTextView.text = content.title
        source.text = content.source
        valueTextView.text = content.value
        connectToWebSocket(content, valueTextView)

        return dataView
    }

    private fun loadJsonFromResources(): String {
        val resourceId = R.raw.mock_api
        val inputStream: InputStream = resources.openRawResource(resourceId)
        return inputStream.bufferedReader().use { it.readText() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                     requestLocationUpdates()
                } else {

                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.map_pin)

        val width = 150
        val height = 150
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(resizedBitmap)

        gogleMap = googleMap;
        if (gogleMap != null) {
            gogleMap.addMarker(
                MarkerOptions().position(LatLng(latitude, longitude)).icon(bitmapDescriptor)
                    .title("")
            )
        }

        checkLocationPermission()
    }

    class WebSocketListenerImpl(private val dataContent: Content, val text: TextView) :
        WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            updateDataSection(text)


        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        }

        private fun updateDataSection(value: String) {
            this.text.text = value
        }
    }


    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            requestLocationUpdates()
        }
    }

    private fun requestLocationUpdates() {
        val locationRequest =
            LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000).setFastestInterval(500)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateMap(location)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    private fun updateMap(location: Location) {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.map_pin)

        val width = 150
        val height = 150
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(resizedBitmap)

        val currentLatLng = LatLng(location.latitude, location.longitude)
        gogleMap.clear()
        gogleMap.addMarker(
            MarkerOptions().position(currentLatLng).icon(bitmapDescriptor).anchor(0.5f, 0.5f)

        )
        gogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
    }




}