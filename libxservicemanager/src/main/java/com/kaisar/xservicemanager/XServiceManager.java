package com.kaisar.xservicemanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class XServiceManager {

    private static final String TAG = "XServiceManager";
    private static final String DELEGATE_SERVICE = "clipboard";
    private static final Map<String, ServiceFetcher<? extends Binder>> SERVICE_FETCHERS = new ArrayMap<>();
    private static final HashMap<String, IBinder> sCache = new HashMap<>();

    private static final String DESCRIPTOR = XServiceManager.class.getName();
    private static final int TRANSACTION_getService = ('_'<<24)|('X'<<16)|('S'<<8)|'M';

    public interface ServiceFetcher<T extends Binder> {
        T createService(Context ctx);
    }

    /**
     * Init XServiceManager for system server.
     * Must be called from system_server!
     */
    public static void initForSystemServer() {
        if (!isSystemServerProcess()) return;
        try {
            @SuppressLint("PrivateApi") Class<?> ServiceManagerClass = Class.forName("android.os.ServiceManager");
            @SuppressLint("DiscouragedPrivateApi") Method getIServiceManagerMethod = ServiceManagerClass.getDeclaredMethod("getIServiceManager");
            getIServiceManagerMethod.setAccessible(true);
            final Object serviceManager = getIServiceManagerMethod.invoke(null);
            @SuppressLint("DiscouragedPrivateApi") Field sServiceManagerField = ServiceManagerClass.getDeclaredField("sServiceManager");
            sServiceManagerField.setAccessible(true);
            Class<?> IServiceManagerClass = sServiceManagerField.getType();
            Object serviceManagerDelegate = Proxy.newProxyInstance(IServiceManagerClass.getClassLoader(), new Class[]{IServiceManagerClass}, (proxy, method, args) -> {
                final String methodName = method.getName();
                if ("addService".equals(methodName) && DELEGATE_SERVICE.equals(args[0])) {
                    IBinder clipboardService = (IBinder) args[1];
                    IBinder xServiceManagerService = new XServiceManagerService();
                    args[1] = new BinderDelegateService(clipboardService, xServiceManagerService);
                    @SuppressLint("PrivateApi") Class<?> ActivityThreadClass = Class.forName("android.app.ActivityThread");
                    Method currentActivityThread = ActivityThreadClass.getMethod("currentActivityThread");
                    Method getSystemContext = ActivityThreadClass.getMethod("getSystemContext");
                    Context systemContext = (Context) getSystemContext.invoke(currentActivityThread.invoke(null));
                    for (Map.Entry<String, ServiceFetcher<?>> serviceFetcherEntry : SERVICE_FETCHERS.entrySet()) {
                        String name = serviceFetcherEntry.getKey();
                        try {
                            Binder service = serviceFetcherEntry.getValue().createService(systemContext);
                            addService(name, service);
                        } catch (Exception e) {
                            Log.e(TAG, String.format("create %s service fail", name), e);
                        }
                    }
                }
                return method.invoke(serviceManager, args);
            });
            sServiceManagerField.set(null, serviceManagerDelegate);
            Log.d(TAG, "inject success");
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            Log.e(TAG, "inject fail", e);
        }
    }

    private static boolean isSystemServerProcess() {
        if (Process.myUid() != Process.SYSTEM_UID) {
            return false;
        }
        try {
            try (BufferedReader r = new BufferedReader(new FileReader(String.format(Locale.getDefault(), "/proc/%d/cmdline", Process.myPid())))) {
                String processName = r.readLine().trim();
                return "system_server".equals(processName);
            }
        } catch (IOException ignore) {
            //ignore.printStackTrace();
        }
        return false;
    }

    private static final class BinderDelegateService extends Binder {

        private final IBinder systemService;
        private final IBinder customService;

        public BinderDelegateService(IBinder systemService, IBinder customService) {
            this.systemService = systemService;
            this.customService = customService;
        }

        @Override
        protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
            if(code == TRANSACTION_getService){
                return customService.transact(code, data, reply, flags);
            }
            return systemService.transact(code, data, reply, flags);
        }
    }

    private static final class XServiceManagerService extends Binder {

        @Override
        protected boolean onTransact(int code, @NonNull Parcel data, Parcel reply, int flags) throws RemoteException {
            String descriptor = DESCRIPTOR;
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(descriptor);
                    return true;
                }
                case TRANSACTION_getService: {
                    data.enforceInterface(descriptor);
                    String name = data.readString();
                    reply.writeNoException();
                    IBinder binder = getServiceInternal(name);
                    reply.writeStrongBinder(binder);
                    return true;
                }
                default: {
                    return super.onTransact(code, data, reply, flags);
                }
            }
        }

    }

    private static IBinder getServiceInternal(String name) {
        IBinder binder = sCache.get(name);
        Log.d(TAG, String.format("get service %s %s", name, binder));
        return binder;
    }

    /**
     * Register a new @a serviceFetcher called @a name into the service
     * Services registered differently from {@link #addService(String, IBinder)} will be delayed
     * until the clipboard service is created. If your service depends on the core service of the
     * system or the context should be added in this way.
     * Must be called from system_server!
     *
     * @param name           the name of the new service
     * @param serviceFetcher the service fetcher object
     */
    public static <T extends Binder> void registerService(String name, ServiceFetcher<T> serviceFetcher) {
        if (!isSystemServerProcess()) return;
        Log.d(TAG, String.format("register service %s %s", name, serviceFetcher));
        SERVICE_FETCHERS.put(name, serviceFetcher);
    }

    /**
     * Place a new @a service called @a name into the service
     * manager.
     * Must be called from system_server!
     *
     * @param name    the name of the new service
     * @param service the service object
     */
    public static void addService(String name, IBinder service) {
        if (!isSystemServerProcess()) return;
        Log.d(TAG, String.format("add service %s %s", name, service));
        sCache.put(name, service);
    }

    /**
     * Returns a reference to a service with the given name.
     *
     * @param name the name of the service to get
     * @return a reference to the service, or <code>null</code> if the service doesn't exist
     */
    public static IBinder getService(String name) {
        try {
            @SuppressLint("PrivateApi") Class<?> ServiceManagerClass = Class.forName("android.os.ServiceManager");
            Method checkService = ServiceManagerClass.getMethod("checkService", String.class);
            IBinder delegateService = (IBinder) checkService.invoke(null, DELEGATE_SERVICE);
            Objects.requireNonNull(delegateService, "can't not access delegate service");
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(name);
                delegateService.transact(TRANSACTION_getService, _data, _reply, 0);
                _reply.readException();
                return _reply.readStrongBinder();
            } finally {
                _data.recycle();
                _reply.recycle();
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("get %s service error", name), e instanceof InvocationTargetException ? e.getCause() : e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <I extends IInterface> I getServiceInterface(String name) {
        try {
            IBinder service = getService(name);
            Objects.requireNonNull(service, String.format("can't found %s service", name));
            String descriptor = service.getInterfaceDescriptor();
            Class<?> StubClass = XServiceManager.class.getClassLoader().loadClass(descriptor + "$Stub");
            return (I) StubClass.getMethod("asInterface", IBinder.class).invoke(null, service);
        } catch (Exception e) {
            Log.e(TAG, String.format("get %s service error", name), e instanceof InvocationTargetException ? e.getCause() : e);
            return null;
        }
    }

}
