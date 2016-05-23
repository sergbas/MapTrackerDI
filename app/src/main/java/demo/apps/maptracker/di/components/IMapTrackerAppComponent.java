package demo.apps.maptracker.di.components;

import demo.apps.maptracker.app.MapTrackerApp;
import demo.apps.maptracker.di.modules.MapTrackerAppModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(
        modules = {
                MapTrackerAppModule.class
        }
)
public interface IMapTrackerAppComponent {
    void inject(MapTrackerApp app);
}
