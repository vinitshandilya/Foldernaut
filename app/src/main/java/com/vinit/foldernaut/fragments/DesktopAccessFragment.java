package com.vinit.foldernaut.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.vinit.foldernaut.OnBackPressedListener;
import com.vinit.foldernaut.R;
import com.vinit.foldernaut.barcode.BarcodeCaptureActivity;
import com.vinit.foldernaut.objects.HttpRequestReceivedListener;
import com.vinit.foldernaut.objects.Webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;


public class DesktopAccessFragment extends Fragment implements OnBackPressedListener, HttpRequestReceivedListener {

    int BARCODE_READER_REQUEST_CODE = 1;
    String scanned_code="";
    String webUrl = "https://foldernaut.herokuapp.com/";
    static ViewGroup vg = null;

    public DesktopAccessFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_desktop_access, container, false);
        vg = container;

        try {
            Webserver server = new Webserver(this, getActivity().getApplicationContext());
            server.start();
            String mode = getWifiIp()!=null ? "WiFi" : "Mobile Data";
            System.out.println("Server started on: " + getDeviceIpAddress() + ":8080, " + "Mode: " + mode);

            TextView tv = (TextView)view.findViewById(R.id.desktopAccesstextView);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(15);
            tv.setText("Point your web browser to: " + "http://"+getDeviceIpAddress()+":8080");
            tv.append("\nOr just scan the QR code @ https://foldernaut@herokuapp.com");

        } catch (IOException e) {
            e.printStackTrace();
        }

        Button scan = (Button) view.findViewById(R.id.scanqrcode);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent scanIntent = new Intent(getActivity().getApplicationContext(), BarcodeCaptureActivity.class);
                startActivityForResult(scanIntent, BARCODE_READER_REQUEST_CODE);
            }
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if(data!=null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    scanned_code = barcode.rawValue;
                    System.out.println("Scanned code is: " + barcode.rawValue);

                    if(getWifiIp()!=null) {
                        //Send device IP as HTTP GET request to server
                        HttpConnectionTask httpConnectionTask = new HttpConnectionTask();
                        httpConnectionTask.execute(webUrl+scanned_code
                                + "?ip=" + getDeviceIpAddress());

                        animatePhoneIcon();

                    }
                    else {
                        //Device is on mobile data
                        //Code to send file tree as HTTP POST
                        HttpPostTask httpPostTask = new HttpPostTask();
                        httpPostTask.execute(webUrl+"testpost");
                    }
                }
            }
        }

    }

    private void animatePhoneIcon() {
        if(vg!=null) {
            ImageView avd_phone = (ImageView)vg.findViewById(R.id.desktopAccessimageView);
            Drawable drawable = avd_phone.getDrawable();
            avd_phone.setBackground(drawable);
            if (drawable instanceof Animatable) {
                ((Animatable) drawable).start();
            }
        }
    }

    @Override
    public void onBackPressed() {

        Toast.makeText(getActivity().getApplicationContext(),"Server will run in the background", Toast.LENGTH_SHORT).show();
        getActivity().finish();

    }

    @Override
    public void onHttpRequestReceived(String html) {

        System.out.println("Served page to browser: " + html);

    }

    private static class HttpConnectionTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            URL url;
            try {
                url = new URL(strings[0]);
                System.out.println(strings[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                int status_code = connection.getResponseCode();

                System.out.println("Server replied: " + status_code);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                //print result
                System.out.println(response.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }
    }

    private static class HttpPostTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            String sj = "filenames=" + getFileTreeStringUTF(Environment.getExternalStorageDirectory().toString()).get(0) + "&filepaths=" +
                    getFileTreeStringUTF(Environment.getExternalStorageDirectory().toString()).get(1);

            byte[] out = sj.getBytes(StandardCharsets.UTF_8);
            int length = out.length;


            try {
                URL url = new URL("https://foldernaut.herokuapp.com/testpost");
                URLConnection con = url.openConnection();
                HttpURLConnection http = (HttpURLConnection)con;
                http.setRequestMethod("POST"); // PUT is another valid option
                http.setDoOutput(true);

                http.setFixedLengthStreamingMode(length);
                http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                http.connect();


                try(OutputStream os = http.getOutputStream()) {
                    os.write(out);
                }

                System.out.println(http.getResponseCode());


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }

    private String getDeviceIpAddress() {
        String actualConnectedToNetwork = null;
        ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager != null) {
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWifi.isConnected()) {
                actualConnectedToNetwork = getWifiIp();
            }
        }
        if (TextUtils.isEmpty(actualConnectedToNetwork)) {
            actualConnectedToNetwork = getNetworkInterfaceIpAddress();
        }
        if (TextUtils.isEmpty(actualConnectedToNetwork)) {
            actualConnectedToNetwork = "127.0.0.1";
        }
        return actualConnectedToNetwork;
    }

    private String getWifiIp() {
        final WifiManager mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
            int ip = mWifiManager.getConnectionInfo().getIpAddress();
            return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
                    + ((ip >> 24) & 0xFF);
        }
        return null;
    }

    public String getNetworkInterfaceIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String host = inetAddress.getHostAddress();
                        if (!TextUtils.isEmpty(host)) {
                            return host;
                        }
                    }
                }

            }
        } catch (Exception ex) {
            Log.e("IP Address", "getLocalIpAddress", ex);
        }
        return null;
    }

    private static ArrayList<String> getFileTreeStringUTF(String rootdir) {

        ArrayList<String> bundle = new ArrayList<>();
        String names="", paths="";

        File rootFile = new File(rootdir);

        if(rootFile.isDirectory()) {
            for(File child : rootFile.listFiles()) {

                try {
                    names = names + URLEncoder.encode(child.getName(), "UTF-8") + "*";
                    paths = paths + URLEncoder.encode(child.getPath(), "UTF-8") + "*";
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    names = names + "Couldn't display file name" + "*";
                    paths = paths + "Couldn't display file name" + "*";
                }
            }
            bundle.add(names);
            bundle.add(paths);
            System.out.println("getFileTreeStringUTF: " + names);
            System.out.println("getFileTreeStringUTF: " + paths);
        }

        return bundle;
    }
}
