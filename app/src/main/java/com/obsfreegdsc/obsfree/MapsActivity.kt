package com.obsfreegdsc.obsfree

import android.Manifest
import android.app.AlertDialog
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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
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

        Places.initialize(applicationContext, getString(R.string.google_map_api_key))

        // SupportMapFragment를 획득하고 지도를 사용할 준비가 되면 알림 받기
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //AutocompleteSupportFragment 초기화
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // 선택된 장소 위치정보 가져오기
                place.latLng?.let {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 12.0f))
                }
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
            }
        })

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermission()

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
        //검색창 배경색 변경(투명 -> 흰색)
        val autocompleteFragmentView = findViewById<View>(R.id.autocomplete_fragment)
        autocompleteFragmentView?.setBackgroundColor(getResources().getColor(R.color.white))

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

        loadMultipleMarkers()
    }

    private fun loadMultipleMarkers() {
        db.collection("broken_blocks").get().addOnSuccessListener { documents ->
            for (document in documents) {
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0
                val markerOptions = MarkerOptions().position(LatLng(latitude, longitude)).title(document.id)
                val marker = mMap.addMarker(markerOptions)
                marker!!.tag = document.id
            }
            mMap.setOnMarkerClickListener { marker ->
                showMarkerDetails(marker)
                true
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMarkerDetails(marker: Marker) {
        val documentId = marker.tag as? String ?: return
        db.collection("broken_blocks").document(documentId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val brokenBlock = document.toObject(BrokenBlock::class.java)
                brokenBlock?.let {
                    showAlertDialogForMarker(brokenBlock, documentId)
                }
            } else {
                Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAlertDialogForMarker(brokenBlock: BrokenBlock, documentId: String) {
        val builder = AlertDialog.Builder(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_marker_details, null)

        val textViewLocation = dialogView.findViewById<TextView>(R.id.dialog_location)
        val imageView = dialogView.findViewById<ImageView>(R.id.dialog_image)
        val toggleButtonConfirmation = dialogView.findViewById<com.github.angads25.toggle.widget.LabeledSwitch>(R.id.dialog_toggle_confirmation)

        //경도+위도 위치 정보를 도로명 주소 혹은 지번 주소로 변경
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            // 주소 가져오기
            val addresses = geocoder.getFromLocation(brokenBlock.latitude, brokenBlock.longitude, 1)
            val addressText = addresses?.firstOrNull()?.getAddressLine(0) ?: "Can not find the address."
            textViewLocation.text = "$addressText"
        } catch (e: IOException) {
            textViewLocation.text = "${brokenBlock.latitude}, ${brokenBlock.longitude}"
        }

        //Firebase Storage에서 이미지 URL 가져오기
        brokenBlock.filename?.let { filename ->
            val imagePath = "images/$filename"
            val imageRef = storage.reference.child(imagePath)
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get().load(uri.toString()).into(imageView)
            }.addOnFailureListener {
                imageView.setImageResource(R.drawable.ic_launcher_background)
            }
        }

        // 토글 버튼 상태 설정
        toggleButtonConfirmation.isOn = brokenBlock.confirmation == "Resolved"
        toggleButtonConfirmation.setOnToggledListener { _, isOn ->
            val newState = if (isOn) "Resolved" else "Unresolved"
            // 파이어베이스 문서 업데이트
            db.collection("broken_blocks").document(documentId)
                .update("confirmation", newState)
                .addOnSuccessListener {
                    Log.d("Update", "DocumentSnapshot successfully updated!")
                }
                .addOnFailureListener { e ->
                    Log.w("Update", "Error updating document", e)
                }
        }

        builder.setView(dialogView)
        builder.setPositiveButton("OK", null)
        builder.show()
    }
}
