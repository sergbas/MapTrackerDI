package demo.apps.maptracker.app;

import android.app.Application;
import android.content.Context;

import demo.apps.maptracker.di.components.DaggerIMapTrackerAppComponent;
import demo.apps.maptracker.di.components.IMapTrackerAppComponent;
import demo.apps.maptracker.di.modules.MapTrackerAppModule;

public class MapTrackerApp extends Application {

    private IMapTrackerAppComponent appComponent;

    public static MapTrackerApp get(Context c) {
        return (MapTrackerApp) c.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildGraphAndInject();
    }

    public IMapTrackerAppComponent getAppComponent() {
        return appComponent;
    }

    public void buildGraphAndInject() {
        appComponent = DaggerIMapTrackerAppComponent.builder()
                .mapTrackerAppModule(new MapTrackerAppModule(this))
                .build();
        appComponent.inject(this);
    }
}
