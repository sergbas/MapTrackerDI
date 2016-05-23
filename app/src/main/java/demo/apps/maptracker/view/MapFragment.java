package demo.apps.maptracker.view;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.octo.android.robospice.SpiceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import demo.apps.maptracker.R;
import demo.apps.maptracker.app.Constants;
import demo.apps.maptracker.common.BaseFragment;
import demo.apps.maptracker.common.Segment;
import demo.apps.maptracker.common.Utils;
import demo.apps.maptracker.di.components.IMainActivityComponent;
import demo.apps.maptracker.network.MapTrackerService;
import demo.apps.maptracker.presenter.MapFragmentPresenterImpl;

public class MapFragment extends BaseFragment implements IMapFragmentView, OnMapReadyCallback {

    @Inject
    MapFragmentPresenterImpl presenter;
    protected SpiceManager spiceManager = new SpiceManager(MapTrackerService.class);

    private static final String TAG = "MapFragment";

    private Activity activity;
    private ListView listView;
    private View rootView;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;

    private LocationRequest lr;
    private double latitude;
    private double longitude;

    private List<LatLng> myWay = new ArrayList<>();
    private List<Segment> edges = new ArrayList<>();

    private TextView tvLoc;
    private int nearest = 0;

    private Button btn;
    private List<LatLng> points;

    public MapFragment() {
    }

    // ----- Lifecycle override method

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.getComponent(IMainActivityComponent.class).inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.init(this);
        presenter.onResume(spiceManager);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_map, container, false);
            listView = (ListView) rootView.findViewById(R.id.talkListView);

            mMapFragment = new SupportMapFragment() {
                @Override
                public void onActivityCreated(Bundle savedInstanceState) {
                    super.onActivityCreated(savedInstanceState);
                    mMap = mMapFragment.getMap();
                    if (mMap != null) {
                        onMapReady(mMap);
                    }
                }
            };

            getChildFragmentManager().beginTransaction().add(R.id.framelayout_location_container, mMapFragment).commit();

            tvLoc = (TextView) rootView.findViewById(R.id.tvLocation);
            btn = (Button) rootView.findViewById(R.id.btnChangePath);

            mMapFragment.getMapAsync(this);

            LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            LocationListener ll = new MyLocationListener();
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.MIN_TIME_UPDATE_MS, Constants.MIN_DISTANCE, ll);
            Location loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
            Log.i(TAG, String.format("Last location: %s, %s", latitude, longitude));

            lr = LocationRequest.create();
            lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        }
        return rootView;
    }

    @Override
    public void onDestroyView() {
        if (rootView.getParent() != null) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onLoadWay(new LatLng(latitude, longitude));
            }
        });

        //mMapFragment.onClick

    }

    @Override
    public void onPause() {
        presenter.onPause();
        super.onPause();
    }

    // -----  IMapFragmentView implement method

    @Override
    public void addWayToMap(List<LatLng> pts) {
        this.points = pts;
        edges = Utils.pointsToEdges(points);
        drawEdges();
    }

    @Override
    public void startService() {
        if (!spiceManager.isStarted()) {
            spiceManager.start(activity);
        }
    }

    @Override
    public void stopService() {
        if (spiceManager.isStarted()) {
            spiceManager.shouldStop();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        if (Constants.DEBUG)
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng arg0) {
                    Log.d("arg0", arg0.latitude + "-" + arg0.longitude);
                    latitude = arg0.latitude;
                    longitude = arg0.longitude;

                    MoveTo(latitude, longitude);
                }
            });
        updateLocation();
    }

    private void MoveTo(double latitude, double longitude) {
        drawEdges();
        updatePath(new LatLng(latitude, longitude));
        updateLocation();
    }

    private void updatePath(LatLng loc) {
        if(edges.size()>0) {
            nearest = Utils.findNearestSegment(edges, loc);
            Log.i(TAG, "FindNearestSegment:" + nearest);

            int distTo = nearest + 1;
            if (distTo > edges.size() - 1) distTo = edges.size() - 1;

            double lenEdge = edges.get(nearest).lengthInMeters;
            double distToNext = Utils.calculationByDistance(new LatLng(latitude, longitude), edges.get(nearest).finish);
            double df = getDistanceToFinishFrom(nearest);
            double metersLeft = df + distToNext;

            tvLoc.setText(String.format("до финиша: %.0f метров", metersLeft));
        }

        drawEdges();
    }

    private double getDistanceToFinishFrom(int from) {
        double x = 0;
        for(int i=from+1; i<edges.size(); i++) {
            x += edges.get(i).lengthInMeters;
        }
        return x;
    }

    private void updateLocation() {
        if (mMap != null) {
            LatLng loc = new LatLng(latitude, longitude);

            myWay.add(loc);

            CircleOptions co = new CircleOptions();
            co.center(loc);
            co.radius(9);
            co.fillColor(Color.RED);
            co.strokeColor(Color.RED);

            Circle c = mMap.addCircle(co);

            CameraUpdate cam = CameraUpdateFactory.newLatLngZoom(loc, 15);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
            mMap.animateCamera(zoom);
        }
    }

    //убрать
    public void drawPath(String result) {
        Log.i(TAG, "drawPath");
        try {
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");

            points = Utils.decodePoly(encodedString);

            double pathDist = 0;
            edges.clear();

            for (int z = 0; z < points.size()-1; z++) {
                LatLng src = points.get(z);
                LatLng dest = points.get(z + 1);

                double len = Utils.calculationByDistance(src, dest);

                edges.add(new Segment(src, dest, len));
                //pathDist += len;
            }

            drawEdges();

        } catch (JSONException e) {

        }
    }

    //оставить здесь
    private void drawEdges() {
        Log.i(TAG, "drawEdges:" + edges.size() + ":" + edges);

        if(mMap == null) return;

        mMap.clear();

        if(points != null)
            mMap.addPolyline(new PolylineOptions()
                    .addAll(points)
                    .width(12)
                    .color(Color.parseColor("#05b1fb"))
                    .geodesic(true)
            );

        for (int z = 0; z < edges.size(); z++) {
            LatLng src = edges.get(z).start;
            LatLng dest = edges.get(z).finish;

            if (z == nearest)
                mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
                        .width(12)
                        .color(Color.YELLOW).geodesic(true));

            float r = 5;
            if (z == edges.size()-1) r = 25;

            CircleOptions co = new CircleOptions();
            co.center(dest);
            co.radius(r);
            co.fillColor(Color.YELLOW);
            co.strokeColor(Color.MAGENTA);
            mMap.addCircle(co);
        }
    }

    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location arg0) {
            Log.i(TAG, "onLocationChanged");

            latitude = arg0.getLatitude();
            longitude = arg0.getLongitude();

            MoveTo(latitude, longitude);
        }

        @Override
        public void onProviderDisabled(String arg0) {
        }

        @Override
        public void onProviderEnabled(String arg0) {
        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        }
    }


}
