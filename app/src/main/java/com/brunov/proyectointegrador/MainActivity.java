package com.brunov.proyectointegrador;

import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brunov.proyectointegrador.api.ApiClient;
import com.brunov.proyectointegrador.api.ApiService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import android.Manifest;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
//import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
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
    private static final int RADIUS_METERS = 500; // Radio en metros (500 m)
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> lista;
    private Set<String> barriosUnicos;

    private LocationCallback locationCallback;
    private HashMap<String, Marker> currentMarkers = new HashMap<>();
    private LinearLayout linearLayoutItems;
    private Button botonsheet;
    private GestureDetector gestureDetector;
    BottomSheetDialog dialog;
    private boolean buscando,found=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //Configuracion y funcionalidad de la barra de busqueda
        barrabusqueda();

        //Configuracion del BottomSheet
        botonsheet=findViewById(R.id.botonsheet);
        View vista = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_dialog, null);
        linearLayoutItems = vista.findViewById(R.id.linearLayoutItems);
        dialog = new BottomSheetDialog(MainActivity.this);
        bottomSheet(dialog,vista);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void barrabusqueda(){
        listView=findViewById(R.id.lista);
        lista=new ArrayList<>();
        barriosUnicos=new HashSet<>();

        SearchView searchView = findViewById(R.id.busqueda);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(Color.BLACK); // Color del texto
        searchEditText.setHintTextColor(Color.GRAY); // Color del hint

        searchView.setOnClickListener(v -> {
            barriosUnicos.clear(); // Limpiar el Set de barrios únicos
            lista.clear();
            currentMarkers.clear();
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

                            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                            found =false;

                            // Acción cuando el usuario envía la búsqueda
                            for(Fuentes fuentes:fuente){
                                if (query != null && query.equalsIgnoreCase(fuentes.getBarrio())) {
                                    LatLng latLng = new LatLng(fuentes.getLatitud(), fuentes.getLongitud());
                                    Map.addMarker(new MarkerOptions()
                                            .position(latLng)
                                            .title(fuentes.getNomVia())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                    boundsBuilder.include(latLng);
                                    buscando=true;
                                    found=true;
                                }
                            }
                            if (found) {
                                // Ajustar la cámara para que muestre todos los marcadores
                                Map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
                            } else {
                                Toast.makeText(MainActivity.this, "No se encontraron fuentes en esta zona", Toast.LENGTH_SHORT).show();
                            }
                            // Ocultar el teclado
                            hideKeyboard(v);
                            return found;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            // Acción mientras el usuario escribe
                            adapter.getFilter().filter(newText);
                            if (newText.isEmpty()) {
                                listView.setVisibility(View.GONE);
                                Map.clear();
                                currentMarkers.clear();
                                buscando=false;
                            }else {
                                listView.setVisibility(View.VISIBLE);
                            }
                            return false;
                        }
                    });
                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        // Ocultar el ListView
                        listView.setVisibility(View.GONE);
                        String selectedItem = adapter.getItem(position); // Barrio seleccionado
                        searchView.setQuery(selectedItem, false); // Mostrar en SearchView

                        // Limpiar los marcadores actuales
                        Map.clear();
                        VaciarItems();

                        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder(); // Para ajustar la cámara

                        boolean found = false; // Para saber si se encontraron fuentes en el barrio

                        // Agregar todos los marcadores del barrio seleccionado
                        for (Fuentes fuentes : fuente) {
                            if (selectedItem.equalsIgnoreCase(fuentes.getBarrio())) {
                                LatLng latLng = new LatLng(fuentes.getLatitud(), fuentes.getLongitud());
                                Map.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(fuentes.getNomVia())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                InsertarItem(fuentes);
                                boundsBuilder.include(latLng);
                                found=true;
                                buscando=true;
                            }
                        }

                        if (found) {
                            // Ajustar la cámara para que muestre todos los marcadores
                            Map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
                        } else {
                            Toast.makeText(MainActivity.this, "No se encontraron fuentes en esta zona", Toast.LENGTH_SHORT).show();
                        }

                        hideKeyboard(searchView);
                        // Ocultar el ListView después de la selección
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

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        view.clearFocus();
    }

    public void bottomSheet(BottomSheetDialog dialog,View vista){
        dialog.setCancelable(true);
        dialog.setContentView(vista);
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // Detectar deslizamiento hacia arriba (swipe up)
                if (e2.getY() < e1.getY()) { // Si el deslizamiento es hacia arriba
                    dialog.show(); // Realizar acción
                    return true;
                }
                return false;
            }
        });
        View touchListenerView = findViewById(R.id.botonsheet);  // Este es un contenedor fuera del mapa
        touchListenerView.setOnTouchListener((v, event) -> {
            // Pasar el evento al GestureDetector para que maneje el deslizamiento
            gestureDetector.onTouchEvent(event);
            return true;
        });
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

        configureLocationUpdates();
    }
    private void configureLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(10000) // Cada 10 segundos
                .setFastestInterval(5000) // Intervalo más rápido posible
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
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

        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        apiService.getFuentes().enqueue(new Callback<List<Fuentes>>() {
            @Override
            public void onResponse(Call<List<Fuentes>> call, Response<List<Fuentes>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Fuentes> fuentesCercanas = filtrarFuentesCercanas(response.body(), userLocation);
                    Log.d("DEBUG","buscando "+buscando);
                    if(!buscando) {
                        actualizarMarcadores(fuentesCercanas);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Fuentes>> call, Throwable t) {
                t.printStackTrace(); // Manejo de errores
            }
        });

    }
    private void actualizarMarcadores(List<Fuentes> fuentesCercanas) {
        HashMap<String, Marker> updatedMarkers = new HashMap<>();

        //Borrar todos los item del menu deslizable
        VaciarItems();

        for (Fuentes fuente : fuentesCercanas) {
            String key = fuente.getLatitud() + "," + fuente.getLongitud();

            if (currentMarkers.containsKey(key)) {
                // Mantener marcador existente
                updatedMarkers.put(key, currentMarkers.get(key));
            } else {
                // Crear nuevo marcador
                LatLng latLng = new LatLng(fuente.getLatitud(), fuente.getLongitud());
                Marker marker = Map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(fuente.getNomVia()));
                updatedMarkers.put(key, marker);
            }

            //Añadir items al menu deslizable
            InsertarItem(fuente);
        }

        // Eliminar marcadores que ya no están cerca
        for (String key : currentMarkers.keySet()) {
            if (!updatedMarkers.containsKey(key)) {
                currentMarkers.get(key).remove();
            }
        }

        currentMarkers = updatedMarkers; // Actualizar lista de marcadores
    }

    private void InsertarItem(Fuentes fuente){
        LinearLayout newItemContainer = new LinearLayout(MainActivity.this);
        newItemContainer.setOrientation(LinearLayout.HORIZONTAL); // Los elementos se organizan horizontalmente (imagen + texto)
        newItemContainer.setPadding(16, 16, 16, 16);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        newItemContainer.setFocusable(false);
        newItemContainer.setFocusableInTouchMode(false);

        layoutParams.setMargins(0,0,0,15);
        newItemContainer.setLayoutParams(layoutParams);

        newItemContainer.setBackgroundResource(R.drawable.item_border);

        // Crear la imagen
       // String estado=fuente.getEstado();

        ImageView imageView = new ImageView(MainActivity.this);
        imageView.setImageResource(R.drawable.icono_ubi);// Aquí puedes cambiarlo por la imagen que desees
        Drawable drawable = imageView.getDrawable();
        if(fuente.getEstado().equalsIgnoreCase("OPERATIVO")) {
            DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.azulito));
            imageView.setImageDrawable(drawable);
        }
        else if(fuente.getEstado().equalsIgnoreCase("CERRADA_TEMPORALMENT")) {
            DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.naranja));
            imageView.setImageDrawable(drawable);
        }
        else if(fuente.getEstado().equalsIgnoreCase("FUERA_DE_SERVICIO")) {
            DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.gris_claro));
            imageView.setImageDrawable(drawable);
        }
        imageView.setLayoutParams(new LinearLayout.LayoutParams(120, 120));  // Tamaño de la imagen

        // Crear el TextView para el texto
        TextView textView = new TextView(MainActivity.this);
        textView.setText(fuente.getNomVia());
        textView.setTextSize(18);
        textView.setTextColor(getResources().getColor(R.color.black));
        textView.setPadding(16, 0, 0, 0);  // Espaciado entre la imagen y el texto
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Añadir la imagen y el texto al contenedor
        newItemContainer.addView(imageView);
        newItemContainer.addView(textView);

        newItemContainer.setOnClickListener(v -> {
            // Aquí moverás el mapa al marcador con la latitud y longitud asociada
            LatLng location = new LatLng(fuente.getLatitud(),fuente.getLongitud());
            Map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 80));
            dialog.dismiss();
            hideKeyboard(v);
        });

        // Añadir el contenedor al LinearLayout principal
        linearLayoutItems.addView(newItemContainer);
    }

    public void VaciarItems(){
        linearLayoutItems.removeAllViews();
    }

    // Método para filtrar fuentes cercanas
    private List<Fuentes> filtrarFuentesCercanas(List<Fuentes> fuentes, Location userLocation) {
        List<Fuentes> fuentesCercanas = new ArrayList<>();
        for (Fuentes fuente : fuentes) {
            Location fuenteLocation = new Location("");
            fuenteLocation.setLatitude(fuente.getLatitud());
            fuenteLocation.setLongitude(fuente.getLongitud());

            // Calcula la distancia entre el usuario y la fuente
            float distancia = userLocation.distanceTo(fuenteLocation);
            if (distancia <= RADIUS_METERS) { // Radio de 2 km
                fuentesCercanas.add(fuente);
            }
        }
        return fuentesCercanas;
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