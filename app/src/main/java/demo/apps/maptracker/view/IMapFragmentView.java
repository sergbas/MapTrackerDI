package demo.apps.maptracker.view;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface IMapFragmentView {
    //void setGoogleMap();
    void addWayToMap(List<LatLng> points);

    void startService();
    void stopService();
}
