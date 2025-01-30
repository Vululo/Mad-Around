package com.brunov.proyectointegrador;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.health.connect.datatypes.ExerciseRoute;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brunov.proyectointegrador.api.ApiClient;
import com.brunov.proyectointegrador.api.ApiService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import android.Manifest;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
//import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap Map;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ListView listview;
    private ArrayAdapter<String> adapter;
    private List<String> lista;
    private Set<String> barriosUnicos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ListView listView=findViewById(R.id.lista);
        lista=new ArrayList<>();
        barriosUnicos=new HashSet<>();

        SearchView searchView = findViewById(R.id.busqueda);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(Color.BLACK); // Color del texto
        searchEditText.setHintTextColor(Color.GRAY); // Color del hint

        searchView.setOnClickListener(v -> {
            barriosUnicos.clear(); // Limpiar el Set de barrios únicos
            lista.clear();

            searchView.setIconified(false);
            listView.setVisibility(View.VISIBLE);


            ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
            apiService.getFuentes().enqueue(new Callback<List<Fuentes>>() {
                @Override
                public void onResponse(Call<List<Fuentes>> call, Response<List<Fuentes>> response) {
                    List<Fuentes>fuente=response.body();
                    for(Fuentes fuentes:fuente){
                        barriosUnicos.add(fuentes.getBarrio());
                    }
                    for(String barrios:barriosUnicos){
                        lista.add(barrios);
                    }
                    adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, lista) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView textView = (TextView) view.findViewById(android.R.id.text1);
                            textView.setTextColor(Color.BLACK); // Cambia el color del texto
                            return view;
                        }
                    };
                    listView.setAdapter(adapter);
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            Map.clear();
                            // Acción cuando el usuario envía la búsqueda
                            for(Fuentes fuentes:fuente){
                                if (query != null && query.equalsIgnoreCase(fuentes.getBarrio())) {
                                    LatLng latLng = new LatLng(fuentes.getLatitud(), fuentes.getLongitud());
                                    Map.addMarker(new MarkerOptions()
                                            .position(latLng)
                                            .title(fuentes.getNomVia())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                }
                            }
                            // Ocultar el teclado
                            hideKeyboard(v);
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            // Acción mientras el usuario escribe
                            adapter.getFilter().filter(newText);
                            if (newText.isEmpty()) {
                                listView.setVisibility(View.GONE);
                            }else {
                                listView.setVisibility(View.VISIBLE);
                            }
                            return false;
                        }
                    });
                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        String selectedItem = adapter.getItem(position);
                        searchView.setQuery(selectedItem, false); // Mostrar selección en SearchView
                        // Ocultar el ListView
                        listView.setVisibility(View.GONE);
                        // Colapsar el SearchView
                        //searchView.setIconified(true);
                    });

                }
                @Override
                public void onFailure(Call<List<Fuentes>> call, Throwable t) {
                    t.printStackTrace(); // Manejo de errores
                }
            });
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Map = googleMap;
        LatLngBounds mapBounds = new LatLngBounds(
                new LatLng(40.3121,-3.8466),
                new LatLng(40.6437,-3.5702)
        );
        Map.setLatLngBoundsForCameraTarget(mapBounds);
        Map.setMinZoomPreference(7);
        Map.setMaxZoomPreference(17);
        // Solicitar permisos
        requestLocationPermission();


        /*ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        apiService.getFuentes().enqueue(new Callback<List<Fuentes>>() {
            @Override
            public void onResponse(Call<List<Fuentes>> call, Response<List<Fuentes>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API Response", "Datos recibidos: " + response.body().toString());
                    List<Fuentes> fuentes = response.body();
                    for (Fuentes fuente : fuentes) {
                        Log.d("API Response", "Fuente: " + fuente.getNomVia() + " - " + fuente.getLatitud() + ", " + fuente.getLongitud()+","+fuente.getEstado());
                    }
                    for (Fuentes fuente : response.body()) {
                        LatLng latLng = new LatLng(fuente.getLatitud(), fuente.getLongitud());
                        if(fuente.getUso() != null && fuente.getUso().equalsIgnoreCase("personas")){
                            if(fuente.getEstado() != null && fuente.getEstado().equalsIgnoreCase("operativo")){
                                Map.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(fuente.getNomVia())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            }
                            if(fuente.getEstado() != null && fuente.getEstado().equalsIgnoreCase("fuera_de_servicio")){
                                Map.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(fuente.getNomVia())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                            }
                            if(fuente.getEstado() != null && fuente.getEstado().equalsIgnoreCase("cerrada_temporalment")){
                                Map.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(fuente.getNomVia())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Fuentes>> call, Throwable t) {
                t.printStackTrace(); // Manejo de errores
            }
        });*/
    }

    private void getDeviceLocation(){
        try{
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this,task ->{
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Obtén la última ubicación conocida
                        Location location = task.getResult();
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        // Mueve la cámara al usuario
                        Map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    } else {
                        Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }

    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Map.setMyLocationEnabled(true);
            getDeviceLocation();
        }

    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            }
        }
    }

}