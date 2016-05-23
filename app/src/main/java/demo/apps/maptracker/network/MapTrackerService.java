package demo.apps.maptracker.network;

import android.app.Application;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.string.InFileStringObjectPersister;

public class MapTrackerService extends SpiceService {
    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        CacheManager cacheManager = new CacheManager();
        InFileStringObjectPersister mapPersister = new InFileStringObjectPersister(application);
        cacheManager.addPersister(mapPersister);
        return cacheManager;
    }

    @Override
    public int getThreadCount() {
        return 3;
    }
}
