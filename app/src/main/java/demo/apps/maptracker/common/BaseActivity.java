package demo.apps.maptracker.common;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import demo.apps.maptracker.di.components.IMapTrackerAppComponent;
import demo.apps.maptracker.app.MapTrackerApp;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupComponent(MapTrackerApp.get(this).getAppComponent());
    }

    protected abstract void setupComponent(IMapTrackerAppComponent appComponent);

}
