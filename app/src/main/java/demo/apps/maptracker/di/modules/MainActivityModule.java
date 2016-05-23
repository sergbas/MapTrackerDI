package demo.apps.maptracker.di.modules;

import demo.apps.maptracker.presenter.MapFragmentPresenterImpl;
import demo.apps.maptracker.presenter.MainActivityPresenterImpl;
import demo.apps.maptracker.view.IMainActivityView;

import dagger.Module;
import dagger.Provides;

@Module
public class MainActivityModule {

    private IMainActivityView view;

    public MainActivityModule(IMainActivityView view) {
        this.view = view;
    }

    @Provides
    public IMainActivityView provideView() {
        return view;
    }

    @Provides
    public MainActivityPresenterImpl provideMainActivityPresenterImpl (IMainActivityView view){
        return  new MainActivityPresenterImpl(view);
    }

	@Provides
    public MapFragmentPresenterImpl provideMapFragmentPresenterImpl() {
        return new MapFragmentPresenterImpl();
    } 
}
