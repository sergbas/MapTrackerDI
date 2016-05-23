package demo.apps.maptracker.view;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import demo.apps.maptracker.R;
import demo.apps.maptracker.di.IHasComponent;
import demo.apps.maptracker.di.components.DaggerIMainActivityComponent;
import demo.apps.maptracker.di.components.IMainActivityComponent;
import demo.apps.maptracker.di.components.IMapTrackerAppComponent;
import demo.apps.maptracker.common.BaseActivity;
import demo.apps.maptracker.di.modules.MainActivityModule;
import demo.apps.maptracker.presenter.MainActivityPresenterImpl;

import javax.inject.Inject;

public class MainActivity extends BaseActivity implements IMainActivityView, IHasComponent<IMainActivityComponent> {

    @Inject
    MainActivityPresenterImpl presenter;

    private IMainActivityComponent mainActivityComponent;
    private android.support.v4.app.FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        MapFragment listFragment = (MapFragment)fragmentManager.findFragmentByTag("MapFragment");
        if (listFragment == null){
            listFragment = new MapFragment();
        }
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, listFragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed(){
        if (fragmentManager.getBackStackEntryCount() > 0) {
            presenter.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void setupComponent(IMapTrackerAppComponent appComponent) {
        mainActivityComponent = DaggerIMainActivityComponent.builder()
                .iMapTrackerAppComponent(appComponent)
                .mainActivityModule(new MainActivityModule(this))
                .build();
        mainActivityComponent.inject(this);
    }

    @Override
    public IMainActivityComponent getComponent() {
        return mainActivityComponent;
    }

    // -----  IMainActivityView implement method

    @Override
    public void popFragmentFromStack() {
        fragmentManager.popBackStack();
    }
}
