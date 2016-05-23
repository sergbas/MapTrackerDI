package demo.apps.maptracker.common;

import android.support.v4.app.Fragment;

import demo.apps.maptracker.di.IHasComponent;

public abstract class BaseFragment extends Fragment {
    @SuppressWarnings("unchecked")
    protected <T> T getComponent(Class<T> componentType) {
        return componentType.cast(((IHasComponent<T>)getActivity()).getComponent());
    }
}
