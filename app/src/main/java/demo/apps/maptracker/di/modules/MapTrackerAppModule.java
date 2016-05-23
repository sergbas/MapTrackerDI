package demo.apps.maptracker.di.modules;

import android.app.Application;

import demo.apps.maptracker.app.MapTrackerApp;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MapTrackerAppModule {

    private final MapTrackerApp app;

    public MapTrackerAppModule(MapTrackerApp app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return app;
    }
}
