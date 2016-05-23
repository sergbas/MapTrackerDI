package demo.apps.maptracker.di.components;

import demo.apps.maptracker.di.ActivityScope;
import demo.apps.maptracker.di.modules.MainActivityModule;
import demo.apps.maptracker.view.MapFragment;
import demo.apps.maptracker.view.MainActivity;

import dagger.Component;

@ActivityScope
@Component(
        dependencies = IMapTrackerAppComponent.class,
        modules = MainActivityModule.class
)
public interface IMainActivityComponent {
    void inject(MainActivity activity);
    void inject(MapFragment talksMapFragment);
}
