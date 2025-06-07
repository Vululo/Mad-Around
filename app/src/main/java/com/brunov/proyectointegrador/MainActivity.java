package com.brunov.proyectointegrador;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import com.google.android.gms.maps.CameraUpdate;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;

import android.Manifest;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap Map;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int RADIUS_METERS = 500; // Radio en metros (500 m)
    private static final int RADIUS_METERS_BANCOS = 150; // Radio en metros (150 m)
    private int estadoActual = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private HashMap<String, Marker> currentMarkersFuentes = new HashMap<>();
    private HashMap<String, Marker> currentMarkersBancos = new HashMap<>();
    private HashMap<String, Marker> currentMarkersPuntosLimpios = new HashMap<>();
    private double lat,lng;

    private ArrayAdapter<String> adapter;
    private List<String> lista = new ArrayList<>();
    private Set<String> barriosUnicos = new HashSet<>();
    private final Set<String> estadosSeleccionados = new HashSet<>();
    private final Set<String> categoriasSeleccionadas = new HashSet<>();
    private List<Fuentes> fuentesBusqueda = new ArrayList<>();
    private List<Fuentes> fuentesCercanas = new ArrayList<>();

    boolean isSearching=false;

    //private LinearLayout linearLayoutItems;
    private SearchView searchView;
    private CardView cardView;
    private Button exit;
    private TextView tipocard, localizacioncard, distanciacard;
    private boolean cargado = false;
    private Location location;
    Button puntosLimpios_btn;
    Button fuentes_btn;
    Button bancos_btn;
    Button comollegar;
    ProgressBar progressBar;
    View overlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BarraDeBusqueda();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button available;
        Button maintenance;
        Button disabled;

        progressBar = findViewById(R.id.progressBar);
        overlay = findViewById(R.id.overlay);

        puntosLimpios_btn = findViewById(R.id.puntos_limpios_btn);
        fuentes_btn = findViewById(R.id.fuentes_btn);
        bancos_btn = findViewById(R.id.bancos_btn);

        cardView = findViewById(R.id.carddata);
        exit = findViewById(R.id.salir);
        tipocard = findViewById(R.id.tipo);
        localizacioncard = findViewById(R.id.localizacion);
        distanciacard = findViewById(R.id.distancia);
        comollegar = findViewById(R.id.comollegar);

        Button center = findViewById(R.id.center);

        if (!isConnectedToInternet()) {
            showNoInternetDialog();
        }

        actualizarBotones();
        // Button click listeners
        center.setOnClickListener(view -> {getDeviceLocation(false);});

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardView.setVisibility(View.GONE);
            }
        });

        comollegar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // URI de dirección de Google Maps
                String uri = "google.navigation:q=" + lat + "," + lng + "&mode=w";

                // Intent para abrir Google Maps
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");

                // Comprobar si existe la app de Google Maps
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Google Maps no está instalado", Toast.LENGTH_LONG).show();
                }
            }
        });

        fuentes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                cambiarEstado(1);
                actualizarBotones();
                getDeviceLocation(true);
                ajustarCamara(currentMarkersFuentes);
                cardView.setVisibility(View.INVISIBLE);
                cargado=false;
                getDeviceLocation(true);

            }
        });
        bancos_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                cambiarEstado(2);
                actualizarBotones();
                getDeviceLocation(true);
                ajustarCamara(currentMarkersBancos);
                cardView.setVisibility(View.INVISIBLE);
                cargado=false;

            }
        });
        puntosLimpios_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                cambiarEstado(3);
                actualizarBotones();
                getDeviceLocation(true);
                ajustarCamara(currentMarkersPuntosLimpios);
                cardView.setVisibility(View.INVISIBLE);
                cargado=false;
                getDeviceLocation(true);

            }
        });

        /*
        available.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFiltro("OPERATIVO", available, R.drawable.enabled2tag, R.drawable.enabled1tag,"Estado");
            }
        });
        maintenance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFiltro("CERRADA_TEMPORALMENT", maintenance, R.drawable.maintenance2tag, R.drawable.maintenance1tag,"Estado");
            }
        });
        disabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFiltro("FUERA_DE_SERVICIO", disabled, R.drawable.disabled2tag, R.drawable.disabled1tag,"Estado");
            }
        });
        */
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void actualizarCardConMarcadorMasCercano(HashMap<String,Marker> marcadoresMap,String tipo) {
        if (marcadoresMap == null || marcadoresMap.isEmpty()) return;

        Marker marcadorMasCercano = null;
        Location markerLocation;
        int results=1000000;
        int distanceInMeters;
        for (Marker marker : marcadoresMap.values()) {
            markerLocation = new Location("");
            markerLocation.setLatitude(marker.getPosition().latitude);
            markerLocation.setLongitude(marker.getPosition().longitude);


            distanceInMeters = (int) location.distanceTo(markerLocation);
            if (results > distanceInMeters) {
                lat=marker.getPosition().latitude;
                lng=marker.getPosition().longitude;
                results=distanceInMeters;
                marcadorMasCercano = marker;
            }
        }

        if (marcadorMasCercano != null) {
            // Actualizar el CardView
            cardView.setVisibility(View.VISIBLE);
            tipocard.setText(tipo);
            distanciacard.setText("Distancia: "+results+"m");
            localizacioncard.setText("Localización: "+marcadorMasCercano.getTitle()); // o cualquier info asociada
        }
    }


    public boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }


    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sin conexión a Internet")
                .setMessage("Esta aplicación requiere conexión a Internet. ¿Deseas intentar conectarte?")
                .setCancelable(false)
                .setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Reinicia la actividad para volver a comprobar la conexión
                        recreate();
                    }
                })
                .setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity(); // Cierra la app completamente
                    }
                })
                .show();
    }
    private void cambiarEstado(int nuevoEstado) {
        estadoActual = nuevoEstado;
        actualizarBotones();
    }
    private void actualizarBotones() {
        fuentes_btn.setBackgroundResource(estadoActual == 1 ? R.drawable.fuentes : R.drawable.fuentes_seleccionada);
        bancos_btn.setBackgroundResource(estadoActual == 2 ? R.drawable.bancos_asientos : R.drawable.bancos_asientos_seleccionado);
        puntosLimpios_btn.setBackgroundResource(estadoActual == 3 ? R.drawable.puntos_limpios : R.drawable.puntos_limpios_seleccionado);
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

        searchView = findViewById(R.id.busqueda);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setHint("Busqueda por Barrio");
        searchEditText.setTextColor(Color.BLACK); // Color del texto
        searchEditText.setHintTextColor(Color.GRAY); // Color del hint

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

        cargarDatos();

        searchView.setOnQueryTextFocusChangeListener((v,hasfocus)->{
            if(!hasfocus){
                searchView.setQuery("",false);
                listView.setVisibility(View.GONE);
            }
            else{
                searchView.clearFocus();
                listView.setVisibility(View.GONE);
            }
        });
        searchView.setOnClickListener(v -> {
            searchView.setIconified(false);
            Log.e("CurrentMarkerClear","Limpiado de marcadores del Buscador");

            isSearching=true;
            barriosUnicos.clear();
            fuentesBusqueda.clear();
            currentMarkersFuentes.clear();
            lista.clear();
            searchView.setIconified(false);
            listView.setVisibility(View.VISIBLE);
            VaciarItems();

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
                            VaciarItems();
                            fuentesDelMapa(fuente, query);
                            isSearching=true;
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
                                VaciarItems();
                                fuentesBusqueda.clear();
                                currentMarkersFuentes.clear();
                                configureLocationUpdates();
                                getDeviceLocation(false);
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
                        isSearching=true;
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

    private void cargarDatos() {
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        apiService.getFuentes().enqueue(new Callback<List<Fuentes>>() {
            @Override
            public void onResponse(Call<List<Fuentes>> call, Response<List<Fuentes>> response) {
                List<Fuentes> fuente = response.body();
                if (fuente != null) {
                    for (Fuentes fuentes : fuente) {
                        barriosUnicos.add(fuentes.getBarrio());
                    }
                    lista.addAll(barriosUnicos);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Fuentes>> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    private void fuentesDelMapa(List<Fuentes> fuente, String selectedItem) {
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder(); // Para ajustar la cámara
        VaciarItems();
        boolean found = false; // Para saber si se encontraron fuentes en el barrio

        // Agregar todos los marcadores del barrio seleccionado
        for (Fuentes fuentes : fuente) {
            if (selectedItem.equalsIgnoreCase(fuentes.getBarrio())) {
                LatLng latLng = new LatLng(fuentes.getLatitud(), fuentes.getLongitud());
                String key = fuentes.getLatitud()+" "+fuentes.getLongitud();
                fuentesBusqueda.add(fuentes);
                currentMarkersFuentes.put(key, addMarker(fuentes,fuentes.getEstado()));
                boundsBuilder.include(latLng);
                found = true;
                InsertarItem(fuentes);
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
        currentMarkersFuentes.clear();
        Map.clear();
        currentMarkersFuentes = listaDeFuentes(fuentesBusqueda);
    }

    private void actualizarMarcadoresLocalizacion(){
        Log.e("CurrentMarkerClear","Limpiado de marcadores Cercanos");
        currentMarkersFuentes.clear();
        Map.clear();
        currentMarkersFuentes = listaDeFuentes(fuentesCercanas);
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

    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.i("Ready", "onMapReady");
        Map = googleMap;
        LatLngBounds mapBounds = new LatLngBounds(
                new LatLng(40.3121,-3.8466),
                new LatLng(40.6437,-3.5702)
        );
        Map.setLatLngBoundsForCameraTarget(mapBounds);
        Map.setMinZoomPreference(10);

        // Deshabilitar el botón de ubicación del usuario
        Map.getUiSettings().setMyLocationButtonEnabled(false);
        Map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @SuppressLint("PotentialBehaviorOverride")
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                String tag= (String) marker.getTag();
                Location markerLocation;
                int distanceInMeters;

                cardView.setVisibility(View.VISIBLE);
                markerLocation = new Location("");
                lat=marker.getPosition().latitude;
                lng=marker.getPosition().longitude;
                markerLocation.setLatitude(lat);
                markerLocation.setLongitude(lng);
                distanceInMeters = (int) location.distanceTo(markerLocation);

                localizacioncard.setText("Localización: "+marker.getTitle());
                distanciacard.setText("Distancia: "+distanceInMeters+"m");

                switch (tag){
                    case "fuente":
                        tipocard.setText("Fuente de agua");
                        break;

                    case "banco":
                        tipocard.setText("Banco / Asiento");
                        break;

                    case "punto":
                        tipocard.setText("Punto Limpio");
                        break;
                }

                return true;
            }
        });
        // Solicitar permisos
        requestLocationPermission();
        configureLocationUpdates();


    }

    private Marker addMarker(Fuentes fuente, String estado){
        LatLng latLng = new LatLng(fuente.getLatitud(), fuente.getLongitud());
        Marker marker = null;
        switch(estado){
            case "OPERATIVO":
                marker = Map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(fuente.getNomVia()+"\nEstado: "+fuente.getEstado())
                        .icon(getCustomMarker(R.drawable.markeroperative)));
                break;
            case "CERRADA_TEMPORALMENT":
                marker = Map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(fuente.getNomVia()+"\nEstado: "+fuente.getEstado())
                        .icon(getCustomMarker(R.drawable.markermaintenance)));
                break;
            case "FUERA_DE_SERVICIO":
                marker = Map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(fuente.getNomVia()+"\nEstado: "+fuente.getEstado())
                        .icon(getCustomMarker(R.drawable.markerclosed)));
                break;
        }
        marker.setTag("fuente");
        return marker;
    }
    private Marker addMarker(Bancos banco){
        LatLng latLng = new LatLng(banco.getLatitud(),banco.getLongitud());
        Marker marker = Map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(banco.getNomVia())
                .icon(getCustomMarker(R.drawable.markerbench)));
        marker.setTag("banco");
        return marker;
    }
    private Marker addMarker(PuntosLimpios punto) {
        LatLng latLng = new LatLng(punto.location.latitude, punto.location.longitude);
        Marker marker = Map.addMarker(new MarkerOptions()
                .position(latLng)
                .title(punto.title)
                .icon(getCustomMarker(R.drawable.markerpuntolimpio))); // Usa el ícono que prefieras
        marker.setTag("punto");
        return marker;
    }


    private void getDeviceLocation(boolean ajustarCam){
        try{
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this,task ->{
                    isSearching=false;
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Obtén la última ubicación conocida

                        location = task.getResult();
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        Log.i("DeviceLocation", "getDeviceLocation");
                        // Mueve la cámara al usuario
                        Map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                        updateMapWithUserLocation(location, ajustarCam);
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
            Log.i("LocationEnabled", "enableUserLocation");
            getDeviceLocation(false);
        }

    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            Log.i("RequestLocation", "requestLocationPermission:Yep");
            enableUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
                getDeviceLocation(false);
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
        view.clearFocus();
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
        if(fuente.getEstado().equalsIgnoreCase("OPERATIVO")) {
            imageView.setImageResource(R.drawable.enabled1);
        }
        else if(fuente.getEstado().equalsIgnoreCase("CERRADA_TEMPORALMENT")) {
            imageView.setImageResource(R.drawable.maintenance1);
        }
        else if(fuente.getEstado().equalsIgnoreCase("FUERA_DE_SERVICIO")) {
            imageView.setImageResource(R.drawable.disabled1);
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
            hideKeyboard(v);
        });

        // Añadir el contenedor al LinearLayout principal
        //linearLayoutItems.addView(newItemContainer);
    }

    public void VaciarItems(){
//        linearLayoutItems.removeAllViews();
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
        Log.e("ConfigureLocation", "configureLocation: Updating");
        LocationRequest locationRequest = new LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY,1000)
            .setMinUpdateIntervalMillis(2000)
            .setMaxUpdateDelayMillis(5000)
            .build();
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location userLocation = locationResult.getLastLocation();
                if (userLocation != null) {
                    updateMapWithUserLocation(userLocation,false);
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }

    }

    private void updateMapWithUserLocation(Location userLocation,boolean ajustarCam) {
        if(isSearching) return;

        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);


        if(estadoActual == 1){

            ocultar_marcadores(currentMarkersBancos);
            ocultar_marcadores(currentMarkersPuntosLimpios);
            mostrar_marcadores(currentMarkersFuentes);

            apiService.getFuentes().enqueue(new Callback<List<Fuentes>>() {
                @Override
                public void onResponse(Call<List<Fuentes>> call, Response<List<Fuentes>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.e("Marcadores", "Cargando Localizacion" );
                        List<Fuentes> fuentes = response.body();
                        VaciarItems();
                        for (Fuentes fuente : fuentes) {
                            Location fuenteLocation = new Location("");
                            fuenteLocation.setLatitude(fuente.getLatitud());
                            fuenteLocation.setLongitude(fuente.getLongitud());
                            String key = fuente.getLongitud()+" "+fuente.getLatitud();
                            // Calcula la distancia entre el usuario y la fuente
                            float distancia = userLocation.distanceTo(fuenteLocation);
                            if (distancia <= RADIUS_METERS && cumpleFiltros(fuente)) {
                                // Si ya existe el marcador, no lo agrega de nuevo
                                if (!currentMarkersFuentes.containsKey(key)) {
                                    currentMarkersFuentes.put(key, addMarker(fuente, fuente.getEstado()));
                                }
                                fuentesCercanas.add(fuente);
                                InsertarItem(fuente);
                            }
                        }

                        if(!cargado) {actualizarCardConMarcadorMasCercano(currentMarkersFuentes,"Fuente de agua");cargado = true;}

                        if(ajustarCam){
                            ajustarCamara(currentMarkersFuentes);
                        }
                        overlay.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }
                }
                @Override
                public void onFailure(Call<List<Fuentes>> call, Throwable t) {
                    t.printStackTrace(); // Manejo de errores
                }
            });

        } else if (estadoActual == 2) {

            ocultar_marcadores(currentMarkersPuntosLimpios);
            ocultar_marcadores(currentMarkersFuentes);
            mostrar_marcadores(currentMarkersBancos);
            apiService.getBancos().enqueue(new Callback<List<Bancos>>() {
                @Override
                public void onResponse(Call<List<Bancos>> call, Response<List<Bancos>> response) {
                    if (response.isSuccessful() && response.body() != null) {

                        Log.e("Bancos", "Cargando Localizacion" );
                        List<Bancos> bancos = response.body();
                        Log.e("Bancos", "Cantidad recibida: " + bancos.size());


                        for (Bancos banco : bancos) {
                            Location bancoLocation = new Location("");
                            bancoLocation.setLatitude(banco.getLatitud());
                            bancoLocation.setLongitude(banco.getLongitud());
                            String key = banco.getLongitud()+" "+banco.getLatitud();
                            // Calcula la distancia entre el usuario y la fuente
                            float distancia = userLocation.distanceTo(bancoLocation);
                            if (distancia <= RADIUS_METERS_BANCOS) {
                                // Si ya existe el marcador, no lo agrega de nuevo
                                if (!currentMarkersBancos.containsKey(key)) {
                                    currentMarkersBancos.put(key, addMarker(banco));
                                }
                            }
                        }
                        if(ajustarCam){
                            ajustarCamara(currentMarkersBancos);
                        }
                    }
                    if(!cargado) { actualizarCardConMarcadorMasCercano(currentMarkersBancos,"Banco / Asiento"); cargado = true;;}
                    overlay.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<List<Bancos>> call, Throwable t) {

                }
            });
        } else if (estadoActual == 3) {
            ocultar_marcadores(currentMarkersBancos);
            ocultar_marcadores(currentMarkersFuentes);
            mostrar_marcadores(currentMarkersPuntosLimpios);
            if(!cargado) {
                apiService.getPuntosLimpios().enqueue(new Callback<PuntosLimpiosResponse>() {
                    @Override
                    public void onResponse(Call<PuntosLimpiosResponse> call, Response<PuntosLimpiosResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {

                            Log.e("PuntosLimpios", "Cargando Localización");
                            List<PuntosLimpios> puntos = response.body().puntos;
                            Log.e("PuntosLimpios", "Cantidad recibida: " + puntos.size());

                            VaciarItems(); // Si compartes función, asegúrate que no borra los bancos

                            for (PuntosLimpios punto : puntos) {
                                if (punto.location == null) continue;

                                Location puntoLocation = new Location("");
                                puntoLocation.setLatitude(punto.location.latitude);
                                puntoLocation.setLongitude(punto.location.longitude);

                                String key = punto.location.longitude + " " + punto.location.latitude;
                                currentMarkersPuntosLimpios.put(key, addMarker(punto));
                            }
                            cargado = true;
                            actualizarCardConMarcadorMasCercano(currentMarkersPuntosLimpios,"Punto Limpio");
                        }
                        if(ajustarCam){
                            ajustarCamara(currentMarkersPuntosLimpios);
                        }
                        overlay.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Call<PuntosLimpiosResponse> call, Throwable t) {
                        Log.e("PuntosLimpios", "Error al cargar puntos limpios: " + t.getMessage());
                    }
                });

            }
            ajustarCamara(currentMarkersPuntosLimpios);


        }

    }

    public void ocultar_marcadores(HashMap<String, Marker> markerMap){
        for (Marker marker : markerMap.values()) {
            marker.setVisible(false);
        }
    }
    public void mostrar_marcadores(HashMap<String, Marker> markerMap){
        for (Marker marker : markerMap.values()) {
            marker.setVisible(true);
        }
    }

    private void ajustarCamara(HashMap<String, Marker> markerMap) {
        if (markerMap == null || markerMap.isEmpty()) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (Marker marker : markerMap.values()) {
            builder.include(marker.getPosition());
        }

        try {
            LatLngBounds bounds = builder.build();
            int padding = 100; // margen en píxeles

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            Map.animateCamera(cameraUpdate);
        } catch (IllegalStateException e) {
            // Esto puede pasar si solo hay un marcador o todos están en la misma posición exacta
            Marker unico = markerMap.values().iterator().next();
            Map.animateCamera(CameraUpdateFactory.newLatLngZoom(unico.getPosition(), 15));
        }
    }

}