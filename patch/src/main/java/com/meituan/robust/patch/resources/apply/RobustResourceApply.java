package com.meituan.robust.patch.resources.apply;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;

/**
 * Created by hedingxu on 17/6/7.
 */
public class RobustResourceApply {

    private static final String BAIDU_ASSET_MANAGER = "android.content.res.BAIDU_ASSET_MANAGER";

    public static boolean patchExistingResourcesOnUiThread(final Context context, final String resourcesApkFilePath) {
        final List<Boolean> results = new ArrayList<>(1);
        boolean isUiThread = Looper.getMainLooper() == Looper.myLooper();
        if (isUiThread) {
            try {
                boolean result = patchExistingResources(context, resourcesApkFilePath);
                results.add(result);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } else {
            synchronized (RobustResourceApply.class) {
                Handler handler = new Handler(Looper.getMainLooper());
                final CountDownLatch syncDownLatch = new CountDownLatch(1);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean result = patchExistingResources(context, resourcesApkFilePath);
                            results.add(result);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                        syncDownLatch.countDown();
                    }
                });
                try {
                    syncDownLatch.await(8 * 1000L, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                }
            }
        }
        if (!results.isEmpty()) {
            return results.get(0);
        } else {
            return false;
        }
    }

    private static boolean patchExistingResources(Context context, String resourcesApkFilePath) throws Throwable {
        if (TextUtils.isEmpty(resourcesApkFilePath)) {
            return false;
        }
        //参考 https://android.googlesource.com/platform/tools/base/+/gradle_2.0.0/instant-run/instant-run-server/src/main/java/com/android/tools/fd/runtime/MonkeyPatcher.java

        //   - Replace mResDir to point to the external resource file instead of the .apk. This is
        //     used as the asset path for new Resources objects.

        /*
        (Note: the resource directory is *also* inserted into the loadedApk in
        monkeyPatchApplication)
        The code seems to perform this:
        File externalResourceFile = <path to resources.ap_ or extracted directory>
        AssetManager newAssetManager = new AssetManager();
        newAssetManager.addAssetPath(externalResourceFile)
        // Kitkat needs this method call, Lollipop doesn't. However, it doesn't seem to cause any harm
        // in L, so we do it unconditionally.
        newAssetManager.ensureStringBlocks();
        // Find the singleton instance of ResourcesManager
        ResourcesManager resourcesManager = ResourcesManager.getInstance();
        // Iterate over all known Resources objects
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            for (WeakReference<Resources> wr : resourcesManager.mActiveResources.values()) {
                Resources resources = wr.get();
                // Set the AssetManager of the Resources instance to our brand new one
                resources.mAssets = newAssetManager;
                resources.updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
            }
        }
        // Also, for each context, call getTheme() to get the current theme; null out its
        // mTheme field, then invoke initializeTheme() to force it to be recreated (with the
        // new asset manager!)
        */

//        // First get on ui thread
//        Object currentActivityThread = RobustResourceReflect.getCurrentActivityThread(context);
//        if (null == currentActivityThread) {
//            return false;
//        }

        // Find the ActivityThread instance for the current thread
        Class<?> activityThread = Class.forName("android.app.ActivityThread");

        // Figure out how loaded APKs are stored.
        // API version 8 has PackageInfo, 10 has LoadedApk. 9, I don't know.
        Class<?> loadedApkClass;
        try {
            loadedApkClass = Class.forName("android.app.LoadedApk");
        } catch (ClassNotFoundException e) {
            loadedApkClass = Class.forName("android.app.ActivityThread$PackageInfo");
        }
//        Field mApplication = loadedApkClass.getDeclaredField("mApplication");
//        mApplication.setAccessible(true);
        Field mResDir = loadedApkClass.getDeclaredField("mResDir");
        mResDir.setAccessible(true);


        //adapt hydra
        //getAssetPath: /system/framework/framework-res.apk;/data/app/com.meituan.robust.sample-1.apk;(有可能包含hydra的assetPath，需要保留）
        ArrayList<String> assets = getAssetPath(context.getAssets());
        ArrayList<String> assetsWithoutBaseApk = new ArrayList<>();//frame work path + hydra pathes
        String baseApkPath = context.getApplicationInfo().sourceDir;
        Log.d("robust", "context.getApplicationInfo().sourceDir 144: " + baseApkPath);
        for (String assetPath : assets) {
            if (!TextUtils.equals(baseApkPath, assetPath)) {
                String newAssetPath = new String(assetPath);
                Log.d("robust", "assetsWithoutBaseApk add newAssetPath 148: " + newAssetPath);
                assetsWithoutBaseApk.add(newAssetPath);
            }
        }

        // Enumerate all LoadedApk (or PackageInfo) fields in ActivityThread#mPackages and
        // ActivityThread#mResourcePackages and do two things:
        //   - Replace mResDir to point to the external resource file instead of the .apk. This is
        //     used as the asset path for new Resources objects.
        //   - Set Application#mLoadedApk to the found LoadedApk instance
        for (String fieldName : new String[]{"mPackages", "mResourcePackages"}) {
            Field field = activityThread.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(RobustResourceReflect.getCurrentActivityThread(context));
            for (Map.Entry<String, WeakReference<?>> entry :
                    ((Map<String, WeakReference<?>>) value).entrySet()) {
                Object loadedApk = entry.getValue().get();
                if (loadedApk == null) {
                    continue;
                }
                if (!TextUtils.isEmpty(resourcesApkFilePath)) {
                    mResDir.set(loadedApk, resourcesApkFilePath);
                }
            }
        }

        // Create a new AssetManager instance and point it to the robust patch resources
        AssetManager newAssetManager;

        AssetManager assetManager = context.getAssets();

        //adapt baiduAssetManager
        if (assetManager.getClass().getName().equals(BAIDU_ASSET_MANAGER)) {
            newAssetManager = (AssetManager) Class.forName(BAIDU_ASSET_MANAGER).getConstructor().newInstance();
        } else {
            newAssetManager = AssetManager.class.getConstructor().newInstance();
        }

        Method mAddAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
        mAddAssetPath.setAccessible(true);
        Log.d("robust", "newAssetManager add assetPath 192:" + resourcesApkFilePath);
        if (((Integer) mAddAssetPath.invoke(newAssetManager, resourcesApkFilePath)) == 0) {
            Log.e("robust","invoke newAssetManager 's mAddAssetPath method result : false");
            return false;
        }

        ArrayList<String> newAssets = getAssetPath(newAssetManager);
        for (String assetPath : assetsWithoutBaseApk) {
            if (!newAssets.contains(assetPath)) {
                Log.d("robust", "newAssetManager add assetPath 192:" + assetPath);
                if (((Integer) mAddAssetPath.invoke(newAssetManager, assetPath)) == 0) {
                    Log.e("robust","invoke newAssetManager 's mAddAssetPath method result : false");
                    return false;
                }
            }
        }

        // Kitkat needs this method call, Lollipop doesn't. However, it doesn't seem to cause any harm
        // in L, so we do it unconditionally.
        Method mEnsureStringBlocks = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
        mEnsureStringBlocks.setAccessible(true);
        mEnsureStringBlocks.invoke(newAssetManager);

        //ignore current activities 's mAssets , mTheme and caches
        //TODO: 17/6/6 hedex  support

        // Iterate over all known Resources objects
        Collection<WeakReference<Resources>> references;
        if (SDK_INT >= KITKAT) {
            // Find the singleton instance of ResourcesManager
            Class<?> resourcesManagerClass = Class.forName("android.app.ResourcesManager");
            Method mGetInstance = resourcesManagerClass.getDeclaredMethod("getInstance");
            mGetInstance.setAccessible(true);
            Object resourcesManager = mGetInstance.invoke(null);
            try {
                Field fMActiveResources = resourcesManagerClass.getDeclaredField("mActiveResources");
                fMActiveResources.setAccessible(true);
                @SuppressWarnings("unchecked")
                ArrayMap<?, WeakReference<Resources>> arrayMap =
                        (ArrayMap<?, WeakReference<Resources>>) fMActiveResources.get(resourcesManager);
                references = arrayMap.values();
            } catch (NoSuchFieldException ignore) {
                Field mResourceReferences = resourcesManagerClass.getDeclaredField("mResourceReferences");
                mResourceReferences.setAccessible(true);
                //noinspection unchecked
                references = (Collection<WeakReference<Resources>>) mResourceReferences.get(resourcesManager);
            }
        } else {
            Field fMActiveResources = activityThread.getDeclaredField("mActiveResources");
            fMActiveResources.setAccessible(true);
            Object thread = RobustResourceReflect.getCurrentActivityThread(context);
            @SuppressWarnings("unchecked")
            HashMap<?, WeakReference<Resources>> map =
                    (HashMap<?, WeakReference<Resources>>) fMActiveResources.get(thread);
            references = map.values();
        }
        if (null == references) {
            return false;
        }
        for (WeakReference<Resources> wr : references) {
            Resources resources = wr.get();
            if (resources != null) {
                pruneResourceCaches(resources);

                // Set the AssetManager of the Resources instance to our brand new one
                try {
                    Field mAssets = Resources.class.getDeclaredField("mAssets");
                    mAssets.setAccessible(true);
                    mAssets.set(resources, newAssetManager);
                } catch (Throwable ignore) {
                    Field mResourcesImpl = Resources.class.getDeclaredField("mResourcesImpl");
                    mResourcesImpl.setAccessible(true);
                    Object resourceImpl = mResourcesImpl.get(resources);
                    Field implAssets = resourceImpl.getClass().getDeclaredField("mAssets");
                    implAssets.setAccessible(true);
                    implAssets.set(resourceImpl, newAssetManager);
                }
                resources.updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
            }
        }

        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Field publicSourceDirField = ApplicationInfo.class.getDeclaredField("publicSourceDir");
                if (publicSourceDirField != null) {
                    publicSourceDirField.set(context.getApplicationInfo(), resourcesApkFilePath);
                }
            } catch (Throwable ignore) {
            }
        }

        return true;
    }


    private static void pruneResourceCaches(Object resources) {
        // Drain TypedArray instances from the typed array pool since these can hold on
        // to stale asset data
        if (SDK_INT >= LOLLIPOP) {
            try {
                Field typedArrayPoolField =
                        Resources.class.getDeclaredField("mTypedArrayPool");
                typedArrayPoolField.setAccessible(true);
                Object pool = typedArrayPoolField.get(resources);
                Class<?> poolClass = pool.getClass();
                Field mPoolSizeField = poolClass.getDeclaredField("mPoolSize");
                Object mPoolSize = mPoolSizeField.get(pool);
                int poolSize = (int) mPoolSize;

                Method acquireMethod = poolClass.getDeclaredMethod("acquire");
                acquireMethod.setAccessible(true);
                while (true) {
                    Object typedArray = acquireMethod.invoke(pool);
                    if (typedArray == null) {
                        break;
                    }
                }

                Constructor<?> typedArrayConstructor = poolClass.getConstructor(int.class);
                typedArrayConstructor.setAccessible(true);

                Object newPool = typedArrayConstructor.newInstance(poolSize);
                typedArrayPoolField.set(resources, newPool);

            } catch (Throwable ignore) {
                return;
            }
        }

        // TODO: hedex support
//        if (SDK_INT >= M) {
//            // Really should only be N; fix this as soon as it has its own API level
//            try {
//                Field mResourcesImpl = Resources.class.getDeclaredField("mResourcesImpl");
//                mResourcesImpl.setAccessible(true);
//                // For the remainder, use the ResourcesImpl instead, where all the fields
//                // now live
//                resources = mResourcesImpl.get(resources);
//            } catch (Throwable ignore) {
//            }
//        }
//        // Prune bitmap and color state lists etc caches
//        Object lock = null;
//        if (SDK_INT >= JELLY_BEAN_MR2) {
//            try {
//                Field field = resources.getClass().getDeclaredField("mAccessLock");
//                field.setAccessible(true);
//                lock = field.get(resources);
//            } catch (Throwable ignore) {
//            }
//        } else {
//            try {
//                Field field = Resources.class.getDeclaredField("mTmpValue");
//                field.setAccessible(true);
//                lock = field.get(resources);
//            } catch (Throwable ignore) {
//            }
//        }
//        if (lock == null) {
//            lock = RobustResourceReflect.class;
//        }
//        //noinspection SynchronizationOnLocalVariableOrMethodParameter
//        synchronized (lock) {
//            // Prune bitmap and color caches
//            pruneResourceCache(resources, "mDrawableCache");
//            pruneResourceCache(resources, "mColorDrawableCache");
//            pruneResourceCache(resources, "mColorStateListCache");
//            if (SDK_INT >= M) {
//                pruneResourceCache(resources, "mAnimatorCache");
//                pruneResourceCache(resources, "mStateListAnimatorCache");
//            }
//        }
        return;
    }

    private static boolean pruneResourceCache(Object resources, String fieldName) {
        try {
            Class<?> resourcesClass = resources.getClass();
            Field cacheField;
            try {
                cacheField = resourcesClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignore) {
                cacheField = Resources.class.getDeclaredField(fieldName);
            }
            cacheField.setAccessible(true);
            Object cache = cacheField.get(resources);
            // Find the class which defines the onConfigurationChange method
            Class<?> type = cacheField.getType();
            if (SDK_INT < JELLY_BEAN) {
                if (cache instanceof SparseArray) {
                    ((SparseArray) cache).clear();
                    return true;
                } else if (SDK_INT >= ICE_CREAM_SANDWICH && cache instanceof LongSparseArray) {
                    // LongSparseArray has API level 16 but was private (and available inside
                    // the framework) in 15 and is used for this cache.
                    //noinspection AndroidLintNewApi
                    ((LongSparseArray) cache).clear();
                    return true;
                }
            } else if (SDK_INT < M) {
                // JellyBean, KitKat, Lollipop
                if ("mColorStateListCache".equals(fieldName)) {
                    // For some reason framework doesn't call clearDrawableCachesLocked on
                    // this field
                    if (cache instanceof LongSparseArray) {
                        //noinspection AndroidLintNewApi
                        ((LongSparseArray) cache).clear();
                    }
                } else if (type.isAssignableFrom(ArrayMap.class)) {
                    Method clearArrayMap = Resources.class.getDeclaredMethod(
                            "clearDrawableCachesLocked", ArrayMap.class, Integer.TYPE);
                    clearArrayMap.setAccessible(true);
                    clearArrayMap.invoke(resources, cache, -1);
                    return true;
                } else if (type.isAssignableFrom(LongSparseArray.class)) {
                    Method clearSparseMap = Resources.class.getDeclaredMethod(
                            "clearDrawableCachesLocked", LongSparseArray.class, Integer.TYPE);
                    clearSparseMap.setAccessible(true);
                    clearSparseMap.invoke(resources, cache, -1);
                    return true;
                }
            } else {
                // Marshmallow: DrawableCache class
                while (type != null) {
                    try {
                        Method configChangeMethod = type.getDeclaredMethod(
                                "onConfigurationChange", Integer.TYPE);
                        configChangeMethod.setAccessible(true);
                        configChangeMethod.invoke(cache, -1);
                        return true;
                    } catch (Throwable ignore) {
                    }
                    type = type.getSuperclass();
                }
            }
        } catch (Throwable ignore) {
            // Not logging these; while there is some checking of SDK_INT here to avoid
            // doing a lot of unnecessary field lookups, it's not entirely accurate and
            // errs on the side of caution (since different devices may have picked up
            // different snapshots of the framework); therefore, it's normal for this
            // to attempt to look up a field for a cache that isn't there; only if it's
            // really there will it continue to flush that particular cache.
        }
        return false;
    }

    public static ArrayList<String> getAssetPath(AssetManager manager) {
        ArrayList<String> assetPaths = new ArrayList<String>();
        try {
            Method method = manager.getClass().getDeclaredMethod("getStringBlockCount");
            method.setAccessible(true);
            int assetsPathCount = (Integer) method.invoke(manager);
            for (int index = 0; index < assetsPathCount; index++) {
                // Cookies map to string blocks starting at 1
                String assetsPath = (String) manager.getClass().getMethod("getCookieName", int.class).invoke(manager, index + 1);
                if (!TextUtils.isEmpty(assetsPath)) {
                    assetPaths.add(assetsPath);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return assetPaths;
    }
}
