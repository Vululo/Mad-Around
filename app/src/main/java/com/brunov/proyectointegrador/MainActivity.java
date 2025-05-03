package com.brunov.proyectointegrador;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
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

import com.brunov.proyectointegrador.api.ApiClient;
import com.brunov.proyectointegrador.api.ApiService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import android.Manifest;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
//import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap Map;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int RADIUS_METERS = 500; // Radio en metros (500 m)
    private FusedLocationProviderClient fusedLocationProviderClient;

    private HashMap<String, Marker> currentMarkers = new HashMap<>();

    private ArrayAdapter<String> adapter;
    private List<String> lista = new ArrayList<>();
    private Set<String> barriosUnicos = new HashSet<>();
    private final Set<String> estadosSeleccionados = new HashSet<>();
    private final Set<String> categoriasSeleccionadas = new HashSet<>();
    private List<Fuentes> fuentesBusqueda = new ArrayList<>();
    private List<Fuentes> fuentesCercanas = new ArrayList<>();

    boolean isSearching=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BarraDeBusqueda();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Button pet = findViewById(R.id.pet);
        Button people = findViewById(R.id.people);
        Button available = findViewById(R.id.available);
        Button maintenance = findViewById(R.id.maintenance);
        Button disabled = findViewById(R.id.disabled);
        Button center = findViewById(R.id.center);

        // Button click listeners
        center.setOnClickListener(view -> {getDeviceLocation();});

        pet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFiltro("MASCOTAS", pet, R.drawable.paw2, R.drawable.paw1,"Categoria");
            }
        });

        people.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFiltro("PERSONAS", people, R.drawable.people2, R.drawable.people1,"Categoria");
            }
        });

        available.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFiltro("OPERATIVO", available, R.drawable.enabled2, R.drawable.enabled1,"Estado");
            }
        });
        maintenance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFiltro("CERRADA_TEMPORALMENT", maintenance, R.drawable.maintenance2, R.drawable.maintenance1,"Estado");
            }
        });
        disabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFiltro("FUERA_DE_SERVICIO", disabled, R.drawable.disabled2, R.drawable.disabled1,"Estado");
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Método para manejar los estados de las fuentes
    private void toggleFiltro(String filtro, Button button, int activeDrawable, int inactiveDrawable,String tipo) {
        switch(tipo){
            case "Estado":
                if (estadosSeleccionados.contains(filtro)) {
                    estadosSeleccionados.remove(filtro);
                    button.setBackground(getDrawable(inactiveDrawable));
                } else {
                    estadosSeleccionados.add(filtro);
                    button.setBackground(getDrawable(activeDrawable));
                }
                break;
            case "Categoria":
                if (categoriasSeleccionadas.contains(filtro)) {
                    categoriasSeleccionadas.remove(filtro);
                    button.setBackground(getDrawable(inactiveDrawable));
                } else {
                    categoriasSeleccionadas.add(filtro);
                    button.setBackground(getDrawable(activeDrawable));
                }
                break;
        }
        if(isSearching){
            actualizarMarcadoresBusqueda();
        }else{
            actualizarMarcadoresLocalizacion();
        }

    }

    private void BarraDeBusqueda() {
        ListView listView=findViewById(R.id.lista);

        SearchView searchView = findViewById(R.id.busqueda);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setHint("Busqueda por Barrio");
        searchEditText.setTextColor(Color.BLACK); // Color del texto
        searchEditText.setHintTextColor(Color.GRAY); // Color del hint

        searchView.setOnQueryTextFocusChangeListener((v,hasfocus)->{
            if(!hasfocus){
                searchView.setQuery("",false);
                listView.setVisibility(View.GONE);
            }
        });
        searchView.setOnClickListener(v -> {
            Log.e("CurrentMarkerClear","Limpiado de marcadores del Buscador");

            isSearching=true;
            barriosUnicos.clear();
            fuentesBusqueda.clear();
            currentMarkers.clear();
            lista.clear();
            searchView.setIconified(false);
            listView.setVisibility(View.VISIBLE);

            ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
            apiService.getFuentes().enqueue(new Callback<List<Fuentes>>() {
                @Override
                public void onResponse(Call<List<Fuentes>> call, Response<List<Fuentes>> response) {
                    List<Fuentes>fuente=response.body();
                    for(Fuentes fuentes : fuente){
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
                            fuentesDelMapa(fuente, query);

                            hideKeyboard(searchView);
                            // Ocultar el ListView después de la selección
                            listView.setVisibility(View.GONE);
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {

                            adapter.getFilter().filter(newText);
                            if (newText.isEmpty()) {
                                Log.e("CurrentMarkerClear","Limpiado de marcadores salir Buscador");

                                isSearching=false;
                                listView.setVisibility(View.GONE);
                                Map.clear();
                                fuentesBusqueda.clear();
                                currentMarkers.clear();
                                configureLocationUpdates();
                                getDeviceLocation();
                            } else {
                                listView.setVisibility(View.VISIBLE);
                            }
                            return false;
                        }
                    });

                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        String selectedItem = adapter.getItem(position); // Barrio seleccionado
                        searchView.setQuery(selectedItem, false); // Mostrar en SearchView

                        // Limpiar los marcadores actuales
                        Map.clear();
                        fuentesDelMapa(fuente, selectedItem);

                        hideKeyboard(searchView);
                        listView.setVisibility(View.GONE);
                    });
                }
                @Override
                public void onFailure(Call<List<Fuentes>> call, Throwable t) {
                    t.printStackTrace(); // Manejo de errores
                }
            });
        });
    }

    private void fuentesDelMapa(List<Fuentes> fuente, String selectedItem) {
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder(); // Para ajustar la cámara

        boolean found = false; // Para saber si se encontraron fuentes en el barrio

        // Agregar todos los marcadores del barrio seleccionado
        for (Fuentes fuentes : fuente) {
            if (selectedItem.equalsIgnoreCase(fuentes.getBarrio())) {
                LatLng latLng = new LatLng(fuentes.getLatitud(), fuentes.getLongitud());
                String key = fuentes.getLatitud()+" "+fuentes.getLongitud();
                fuentesBusqueda.add(fuentes);
                currentMarkers.put(key, addMarker(fuentes,fuentes.getEstado()));
                boundsBuilder.include(latLng);
                found = true;

            }
        }

        if (found) {
            // Ajustar la cámara para que muestre todos los marcadores
            Map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
        } else {
            Toast.makeText(MainActivity.this, "No se encontraron fuentes en esta zona", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarMarcadoresBusqueda() {
        Log.e("CurrentMarkerClear","Limpiado de marcadores Buscados");
        currentMarkers.clear();
        Map.clear();
        currentMarkers = listaDeFuentes(fuentesBusqueda);
    }

    private void actualizarMarcadoresLocalizacion(){
        Log.e("CurrentMarkerClear","Limpiado de marcadores Cercanos");
        currentMarkers.clear();
        Map.clear();
        currentMarkers = listaDeFuentes(fuentesCercanas);
    }

    private HashMap<String, Marker> listaDeFuentes(List<Fuentes> fuente){
        Log.e("NewMarcadores","Nueva Lista de Marcadores");
        HashMap<String, Marker> markers = new HashMap<>();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        for (Fuentes fuentes : fuente){

            String key = fuentes.getLongitud()+" "+fuentes.getLatitud();

            if(cumpleFiltros(fuentes)){
                markers.put(key,addMarker(fuentes,fuentes.getEstado()));
            }
            boundsBuilder.include(new LatLng(fuentes.getLatitud(), fuentes.getLongitud()));
        }

        Map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));

        return markers;
    }


    private boolean cumpleFiltros(Fuentes fuente) {
        boolean estadoCoincide = estadosSeleccionados.isEmpty() || estadosSeleccionados.contains(fuente.getEstado());
        boolean categoriaCoincide = categoriasSeleccionadas.isEmpty() ||
                (fuente.getUso().equals("MASCOTAS") && categoriasSeleccionadas.contains("MASCOTAS")) ||
                (fuente.getUso().equals("PERSONAS") && categoriasSeleccionadas.contains("PERSONAS")) ||
                (fuente.getUso().contains("PERSONAS_Y_MASCOTAS"));

        return estadoCoincide && categoriaCoincide;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Map = googleMap;
        LatLngBounds mapBounds = new LatLngBounds(
                new LatLng(40.3121,-3.8466),
                new LatLng(40.6437,-3.5702)
        );
        Map.setLatLngBoundsForCameraTarget(mapBounds);
        Map.setMinZoomPreference(10);
        Map.setMaxZoomPreference(17);

        // Deshabilitar el botón de ubicación del usuario
        Map.getUiSettings().setMyLocationButtonEnabled(false);

        // Solicitar permisos
        requestLocationPermission();

        configureLocationUpdates();
    }

    private Marker addMarker(Fuentes fuente,String estado){
        LatLng latLng = new LatLng(fuente.getLatitud(), fuente.getLongitud());
        Marker marker = null;
        switch(estado){
            case "OPERATIVO":
                marker = Map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(fuente.getNomVia())
                        .icon(getCustomMarker(R.drawable.markeroperative)));
                break;
            case "CERRADA_TEMPORALMENT":
                marker = Map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(fuente.getNomVia())
                        .icon(getCustomMarker(R.drawable.markermaintenance)));
                break;
            case "FUERA_DE_SERVICIO":
                marker = Map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(fuente.getNomVia())
                        .icon(getCustomMarker(R.drawable.markerclosed)));
                break;
        }
        return marker;
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
                        Map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
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

    private BitmapDescriptor getCustomMarker(int drawableRes) {
        Drawable drawable = ContextCompat.getDrawable(this, drawableRes);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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

    private void configureLocationUpdates() {

        LocationRequest locationRequest = new LocationRequest
                .Builder(Priority.PRIORITY_HIGH_ACCURACY,10000)
                .build();

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location userLocation = locationResult.getLastLocation();
                if (userLocation != null) {
                    updateMapWithUserLocation(userLocation);
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }

    }

    private void updateMapWithUserLocation(Location userLocation) {
        if(isSearching) return;
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        apiService.getFuentes().enqueue(new Callback<List<Fuentes>>() {
            @Override
            public void onResponse(Call<List<Fuentes>> call, Response<List<Fuentes>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Fuentes> fuentes = response.body();
                    for (Fuentes fuente : fuentes) {
                        Location fuenteLocation = new Location("");
                        fuenteLocation.setLatitude(fuente.getLatitud());
                        fuenteLocation.setLongitude(fuente.getLongitud());
                        String key = fuente.getLongitud()+" "+fuente.getLatitud();
                        // Calcula la distancia entre el usuario y la fuente
                        float distancia = userLocation.distanceTo(fuenteLocation);
                        if (distancia <= RADIUS_METERS && cumpleFiltros(fuente)) {
                            // Si ya existe el marcador, no lo agrega de nuevo
                            if (!currentMarkers.containsKey(key)) {
                                currentMarkers.put(key, addMarker(fuente, fuente.getEstado()));
                            }
                            fuentesCercanas.add(fuente);
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Fuentes>> call, Throwable t) {
                t.printStackTrace(); // Manejo de errores
            }
        });
    }
}