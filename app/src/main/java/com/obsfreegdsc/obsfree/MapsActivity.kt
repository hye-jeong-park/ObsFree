package com.obsfreegdsc.obsfree

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val db = FirebaseFirestore.getInstance()
    private val LOCATION_PERMISSION_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermission()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.isMyLocationEnabled = true
                    moveToCurrentLocation()
                }
            } else {
                Toast.makeText(this, "Location permission needed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun moveToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLocation = LatLng(it.latitude, it.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f))
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
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
                val location = LatLng(latitude, longitude)
                mMap.addMarker(MarkerOptions().position(location).title(document.id))
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
        }
    }
}


//class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
//
//    private lateinit var mMap: GoogleMap
//    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
//    private val database = FirebaseDatabase.getInstance()
//    private val brokenBlocksRef = database.getReference("brokenBlocks")
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_maps)
//
//        // 위치 권한 요청
//        requestLocationPermission()
//        Log.d("test","************1실행됨*****************")
//
//        // FusedLocationProviderClient 초기화
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
//        Log.d("test","************2실행됨*****************")
//
//
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//        Log.d("test","************3실행됨*****************")
//
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//        Log.d("test","************4실행됨*****************")
//
//        // 현재 위치 표시
//        getDeviceLocation()
//        Log.d("test","************5실행됨*****************")
//
//        // 파이어베이스 데이터 읽어 마커 표시
//        brokenBlocksRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                Log.d("test","************6실행됨*****************")
//
//                for (snapshot in dataSnapshot.children) {
//                    Log.d("test","************7실행됨*****************")
//                    val brokenBlock = snapshot.getValue(BrokenBlock::class.java)
//                    Log.d("test","************8실행됨*****************")
//
//                    if (brokenBlock != null) {
//                        Log.d("test","************9실행됨*****************")
//                        Log.d("MapsActivity", "데이터베이스에서 가져온 위치: 위도=${brokenBlock.latitude}, 경도=${brokenBlock.longitude}")
//                        mMap.addMarker(
//                            MarkerOptions()
//                                .position(LatLng(brokenBlock.latitude, brokenBlock.longitude))
//                                .title("파손된 블록")
//                        )
//                    }
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.w("MapsActivity", "Firebase database error: ${databaseError.message}")
//            }
//        })
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun getDeviceLocation() {
//        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
//            if (location != null) {
//                val currentLatLng = LatLng(location.latitude, location.longitude)
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15.0f))
//            }
//        }
//    }
//
//    private fun requestLocationPermission() {
//        val requestPermissionLauncher =
//            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//                if (isGranted) {
//                    Log.d("MapsActivity", "위치 권한 허용됨")
//                } else {
//                    Log.d("MapsActivity", "위치 권한 거부됨")
//                }
//            }
//        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//    }
//}

// 현재 위치 버전
//class MapsActivity : AppCompatActivity(), OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
//
//    private lateinit var mMap: GoogleMap
//    private var currentMarker: Marker? = null
//
//    companion object {
//        private const val TAG = "googlemap"
//        private const val GPS_ENABLE_REQUEST_CODE = 2001
//        private const val UPDATE_INTERVAL_MS: Long = 1000 // 1초
//        private const val FASTEST_UPDATE_INTERVAL_MS: Long = 500 // 0.5초
//        private const val PERMISSION_REQUEST_CODE = 100
//    }
//
//    private var needRequest = false
//
//    private lateinit var REQUIRED_PERMISSION: Array<String>
//
//    private lateinit var mCurrentLocation: Location
//    private lateinit var currentPosition: LatLng
//
//    private lateinit var mFusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationRequest: LocationRequest
//    private lateinit var location: Location
//
//    private lateinit var mLayout: View // for using snackbar
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_maps)
//
//        REQUIRED_PERMISSION = arrayOf(
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        )
//
//        locationRequest = LocationRequest()
//            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//            .setInterval(UPDATE_INTERVAL_MS)
//            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS)
//
//        val builder = LocationSettingsRequest.Builder()
//        builder.addLocationRequest(locationRequest)
//
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
//        mapFragment?.getMapAsync(this)
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        Log.d(TAG, "onMapReady: 들어옴 ")
//        mMap = googleMap
//
//        // 지도의 초기위치 이동
//        setDefaultLocation()
//
//        // 런타임 퍼미션 처리
//        val hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//
//        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
//            startLocationUpdates()
//        } else {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSION[0])) {
//                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Snackbar.LENGTH_INDEFINITE)
//                    .setAction("확인") {
//                        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, PERMISSION_REQUEST_CODE)
//                    }.show()
//            } else {
//                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, PERMISSION_REQUEST_CODE)
//            }
//        }
//
//        mMap.uiSettings.isMyLocationButtonEnabled = true
//        mMap.setOnMapClickListener { latLng ->
//            Log.d(TAG, "onMapClick: ")
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun startLocationUpdates() {
//        if (!checkLocationServicesStatus()) {
//            showDiologForLocationServiceSetting()
//        } else {
//            val hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//            val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//
//            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
//                Log.d(TAG, "startLocationUpdates: 퍼미션 없음")
//                return
//            }
//
//            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
//            if (checkPermission()) {
//                mMap.isMyLocationEnabled = true
//            }
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                startLocationUpdates()
//            } else {
//                Toast.makeText(this, "위치 권한을 허용해야 합니다.", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private val locationCallback = object : LocationCallback() {
//        override fun onLocationResult(locationResult: LocationResult) {
//            super.onLocationResult(locationResult)
//            val locationList = locationResult.locations
//            if (locationList.isNotEmpty()) {
//                location = locationList[locationList.size - 1]
//                currentPosition = LatLng(location.latitude, location.longitude)
//                val markerTitle = getCurrentAddress(currentPosition)
//                val markerSnippet = "위도 : ${location.latitude} 경도 : ${location.longitude}"
//                setCurrentLocation(location, markerTitle, markerSnippet)
//                mCurrentLocation = location
//            }
//        }
//    }
//
//    private fun getCurrentAddress(currentPosition: LatLng): String {
//        val geocoder = Geocoder(this, Locale.getDefault())
//        val addresses: List<Address>
//        return try {
//            addresses =
//                geocoder.getFromLocation(currentPosition.latitude, currentPosition.longitude, 1) as List<Address>
//            if (addresses.isEmpty()) {
//                "주소 미발견"
//            } else {
//                addresses[0].getAddressLine(0) ?: "주소 미발견"
//            }
//        } catch (ioException: IOException) {
//            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show()
//            "지오코더 서비스 사용 불가"
//        } catch (illegalArgumentException: IllegalArgumentException) {
//            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show()
//            "잘못된 GPS 좌표"
//        }
//    }
//
//    private fun setCurrentLocation(location: Location, markerTitle: String, markerSnippet: String) {
//        currentMarker?.remove()
//        val currentLatLng = LatLng(location.latitude, location.longitude)
//        val markerOptions = MarkerOptions().position(currentLatLng).title(markerTitle).snippet(markerSnippet).draggable(true)
//        currentMarker = mMap.addMarker(markerOptions)
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
//    }
//
//    private fun checkPermission(): Boolean {
//        val hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//        return hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun showDiologForLocationServiceSetting() {
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("위치 서비스 비활성화")
//            .setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다. 위치설정을 수정하시겠습니까?")
//            .setCancelable(true)
//            .setPositiveButton("설정") { dialogInterface: DialogInterface, _: Int ->
//                val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE)
//                dialogInterface.dismiss()
//            }
//            .setNegativeButton("취소") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
//            .create().show()
//    }
//
//    private fun checkLocationServicesStatus(): Boolean {
//        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
//    }
//
//    private fun setDefaultLocation() {
//        val DEFAULT_LOCATION = LatLng(37.56, 126.97)
//        val markerTitle = "위치 정보 가져올 수 없음"
//        val markerSnippet = "위치 퍼미션과 GPS 활성 여부를 확인하세요"
//        currentMarker?.remove()
//        val markerOptions = MarkerOptions().position(DEFAULT_LOCATION).title(markerTitle).snippet(markerSnippet).draggable(true)
//        currentMarker = mMap.addMarker(markerOptions)
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15f))
//    }
//}









//완전 전 버전
// class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
//
//    private lateinit var binding: ActivityMapsBinding
//    private lateinit var locationManager: LocationManager
//    private lateinit var currentLocation: LatLng
//    private lateinit var googleMap: GoogleMap
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMapsBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//
//        // 위치 권한 확인
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // 권한이 부여되지 않은 경우 권한을 요청
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
//        } else {
//            // 권한이 부여된 경우 GPS 활성화 확인
//            checkGPSStatus()
//        }
//    }
//
//    private fun checkGPSStatus() {
//        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            showGPSDisabledAlert()
//        } else {
//            initMap()
//        }
//    }
//
//    private fun initMap() {
//        // 구글 맵 설정
//        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        // 내 위치 가져오기
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
//        }
//
//        // 구글 맵 설정
//        this.googleMap = googleMap
//
//        // 내 위치의 마커 설정하기 (초기에는 위치를 가져오기 전이므로 임시 위치 사용)
//        currentLocation = LatLng(37.53040854433893, 126.8458642208867)
//        googleMap.addMarker(MarkerOptions().position(currentLocation).title("내 위치"))
//
//        // 마커 색상 변경
//        googleMap.addMarker(MarkerOptions().position(LatLng(37.53062664472608, 126.84556396236947))
//            .title("마커2").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)))
//
//        // 마커의 인덱스 설정, onMarkerClick 메소드에서 기능 정의
//        googleMap.addMarker(MarkerOptions().position(LatLng(37.53064536620612, 126.84610131866438))
//            .title("마커3"))?.tag = 0
//
//        // onMarkerClick 메소드 호출
//        googleMap.setOnMarkerClickListener(this)
//    }
//
//    override fun onMarkerClick(marker: Marker): Boolean {
//        val index = marker.tag as Int?
//
//        if (index == 0) {
//            Toast.makeText(applicationContext, "marker click", Toast.LENGTH_LONG).show()
//        } else {
//            Toast.makeText(applicationContext, "else", Toast.LENGTH_LONG).show()
//        }
//        return false
//    }
//
//    private val locationListener: LocationListener = object : LocationListener {
//        override fun onLocationChanged(location: Location) {
//            currentLocation = LatLng(location.latitude, location.longitude)
//            // 지도가 준비된 후에 내 위치를 기준으로 지도를 확대하도록 호출
//            if (::googleMap.isInitialized) {
//                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17f))
//            }
//        }
//
//        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
//        override fun onProviderEnabled(provider: String) {}
//        override fun onProviderDisabled(provider: String) {}
//    }
//
//    private fun showGPSDisabledAlert() {
//        val alertDialogBuilder = AlertDialog.Builder(this)
//        alertDialogBuilder.setMessage("GPS가 비활성화되어 있습니다. 설정으로 이동하시겠습니까?")
//            .setCancelable(false)
//            .setPositiveButton("설정으로 이동",
//                DialogInterface.OnClickListener { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
//            .setNegativeButton("취소",
//                DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
//        val alert = alertDialogBuilder.create()
//        alert.show()
//    }
//
//    companion object {
//        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
//    }
//}
