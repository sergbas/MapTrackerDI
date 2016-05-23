package demo.apps.maptracker.presenter;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.simple.SimpleTextRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import demo.apps.maptracker.common.Utils;
import demo.apps.maptracker.model.Tags;
import demo.apps.maptracker.view.IMapFragmentView;

public class MapFragmentPresenterImpl implements IMapFragmentPresenter {

    private static final String TAG = "MapFrPresenterImpl";
    private static final double REGION_SIZE_X = 0.04;
    private static final double REGION_SIZE_Y = 0.04;

    private IMapFragmentView view;
    LatLng currentLocation = new LatLng(55, 38);
    private SpiceManager spiceManager;

    @Inject
    public MapFragmentPresenterImpl() {
    }

    @Override
    public void init(IMapFragmentView view) {
        this.view = view;
    }

    @Override
    public void onResume(SpiceManager spiceManager) {
        view.startService();
        this.spiceManager = spiceManager;
        onLoadWay(currentLocation);
    }

    @Override
    public void onPause() {
        view.stopService();
    }

    @Override
    public void onLoadWay(LatLng loc) {
        Log.i(TAG, "onLoadWay:" + loc);
        Random r = new Random();
        String url = Utils.makeURL(
                loc.latitude,
                loc.longitude,
                loc.latitude + r.nextDouble() * REGION_SIZE_X - REGION_SIZE_X/2,
                loc.longitude + r.nextDouble() * REGION_SIZE_Y - REGION_SIZE_Y/2);
        sendRequest(url, spiceManager);
    }

    @Override
    public void addWayToMap(List<LatLng> points) {
        view.addWayToMap(points);
    }

    private void sendRequest(String url, SpiceManager spiceManager) {
        SimpleTextRequest request = new SimpleTextRequest(url);
        spiceManager.execute(request, "jsonMap", DurationInMillis.ONE_SECOND, new MapApiJsonRequestListener());
    }

    private final class MapApiJsonRequestListener implements RequestListener<String> {

        private List<LatLng> points;

        @Override
        public void onRequestFailure(SpiceException spiceException) {

        }

        @Override
        public void onRequestSuccess(String result) {
            addWayToMap(getListFromJson(result));
        }

        //парсим и декодируем json
        private List<LatLng> getListFromJson(String jsonString) {
            Log.i(TAG, "getListFromJson");
            try {
                final JSONObject json = new JSONObject(jsonString);
                JSONArray routeArray = json.getJSONArray(Tags.TAG_ROUTES);
                JSONObject routes = routeArray.getJSONObject(0);
                JSONObject overviewPolylines = routes.getJSONObject(Tags.TAG_OVERVIEW_POLYLINE);
                String encodedString = overviewPolylines.getString(Tags.TAG_POINTS);

                points = Utils.decodePoly(encodedString);
            } catch (JSONException e) {

            }

            return points;
        }
    }
}
