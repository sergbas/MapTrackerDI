package demo.apps.maptracker.presenter;

import com.google.android.gms.maps.model.LatLng;
import com.octo.android.robospice.SpiceManager;

import java.util.List;

import demo.apps.maptracker.common.BaseFragmentPresenter;
import demo.apps.maptracker.view.IMapFragmentView;

public interface IMapFragmentPresenter extends BaseFragmentPresenter<IMapFragmentView> {
    void onResume(SpiceManager spiceManager);
    void onPause();

    void onLoadWay(LatLng loc);
    void addWayToMap(List<LatLng> points);
}
