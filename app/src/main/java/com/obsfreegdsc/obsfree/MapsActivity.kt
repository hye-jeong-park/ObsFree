package com.obsfreegdsc.obsfree

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.IOException
import java.util.Locale


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val LOCATION_PERMISSION_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermission()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<ImageButton>(R.id.myLocationButton).setOnClickListener {
            moveToCurrentLocation()
        }
    }


    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mMap.isMyLocationEnabled = true
                    moveToCurrentLocation()
                }
            } else {
                Toast.makeText(this, "Location permission needed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun moveToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            // 현재 위치로 지도 이동
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = false

            moveToCurrentLocation()
        } else {
            checkLocationPermission()
        }

        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this))
        loadMultipleMarkers()
    }

    private fun loadMultipleMarkers() {
        db.collection("broken_blocks").get().addOnSuccessListener { documents ->
            for (document in documents) {
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0
                val markerOptions =
                    MarkerOptions().position(LatLng(latitude, longitude)).title(document.id)
                val marker = mMap.addMarker(markerOptions)
                marker?.tag = BrokenBlock(
                    latitude = latitude,
                    longitude = longitude,
                    filename = document.getString("filename"),
                    confirmation = document.getString("confirmation")
                )
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
        }
    }

    inner class CustomInfoWindowAdapter(private val context: Context) :
        GoogleMap.InfoWindowAdapter {
        override fun getInfoWindow(marker: Marker): View? {
            val infoWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)

            val brokenBlock = marker.tag as? BrokenBlock ?: return null

            val textViewAddress = infoWindow.findViewById<TextView>(R.id.textViewAddress)
            val imageViewPhoto = infoWindow.findViewById<ImageView>(R.id.imageViewPhoto)
            val toggleButtonStatus = infoWindow.findViewById<ToggleButton>(R.id.toggleButtonStatus)

            return infoWindow
        }

        override fun getInfoContents(marker: Marker): View? {
            // 기본 정보 창 컨텐츠를 사용하지 않는 경우
            return null
        }
    }
}
