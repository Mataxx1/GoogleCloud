package gcm.ejemplo.com.googlecloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends ActionBarActivity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    String regid;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "97998365892";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCMDemo";
    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId =  new AtomicInteger();
    SharedPreferences prefs;
    Context context;
    CheckBox recibir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkPlayServices()) {
            Toast.makeText(getApplicationContext(), "Sí tengo Play Services", Toast.LENGTH_SHORT).show();
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            Log.i("GCMCuvalles", "Registro: " + regid);
            //registrarDispositivoEnServidorWeb(MainActivity.this,regid);
            if (regid.isEmpty()) {
                registerInBackground();
                Log.i("GCMCuvalles", "Registro: " + regid);

            }
        }
    }

    private boolean checkPlayServices(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }else{
                Toast.makeText(getApplicationContext(),"El dispositivo no está soportado",Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void registrarDispositivoEnServidorWeb(Context context, final String regId){


        new AsyncTask<String, Void, Void>(){
            @Override
            protected Void doInBackground(String... parametros) {
                String serverUrl = "http://148.202.37.19/android/registrar.php";
                //String serverUrl = constantes.SERVER_REGISTRO;
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(serverUrl);
                ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                try {


                    params.add(new BasicNameValuePair("iddevice", regId));
                    params.add(new BasicNameValuePair("carrera", "ADMON"));
                    params.add(new BasicNameValuePair("idapp", SENDER_ID));

                    HttpParams parametrosHTTP = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(parametrosHTTP, 15000);
                    HttpConnectionParams.setSoTimeout(parametrosHTTP, 15000);

                    httppost.setEntity(new UrlEncodedFormEntity(params));
                    HttpResponse response = httpclient.execute(httppost);

                    Log.i("GCMCuvalles", "Registrado exitosamente");

                } catch (IOException e) {
                    Log.e("GCMCuvalles", "Error en el registro" + e);

                }
                return null;
            }
        }.execute(null, null, null);


    }

    private void desregistrarDispositivoEnServidor(Context context, final String regId){
        new AsyncTask<String, Void, Void>(){
            @Override
            protected Void doInBackground(String... params) {
                String serverUrl ="http://148.202.37.19/android/desregistrar.php";
                //String serverUrl =constantes.SERVER_DESREGISTRO;
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(serverUrl);
                ArrayList<NameValuePair> parametross = new ArrayList<NameValuePair>();

                parametross.add(new BasicNameValuePair("iddevice",regId));
                HttpParams parametrosHttp = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(parametrosHttp, 15000);
                HttpConnectionParams.setSoTimeout(parametrosHttp, 15000);

                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(parametross));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                try {
                    HttpResponse response = httpClient.execute(httpPost);
                    Log.i(TAG,"desregistrado exitosamente.");
                } catch (IOException e) {
                    e.printStackTrace();
                }


                return null;
            }
        }.execute(null,null,null);

    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void,String,String>(){


            @Override
            protected String doInBackground(Void... params) {
                String msg="";
                try{
                    if(gcm==null){
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg="Dispositivo registrado, id de Registro="+regid;

                    sendRegistrationIdToBackend();

                    storeRegistrationId(context,regid);

                }catch(IOException e){
                    msg = "Error: "+e.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String s) {
                // mDisplay.append("\n"+s+"\n");
            }
        }.execute(null,null,null);

    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }
    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
