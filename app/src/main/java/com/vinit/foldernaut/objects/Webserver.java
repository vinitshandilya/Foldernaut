package com.vinit.foldernaut.objects;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.vinit.foldernaut.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class Webserver extends NanoHTTPD {

    private HttpRequestReceivedListener httpRequestReceivedListener;
    private Context ctx;

    public Webserver(HttpRequestReceivedListener httpRequestReceivedListener, Context ctx) throws IOException {
        super(8080);
        this.httpRequestReceivedListener = httpRequestReceivedListener;
        this.ctx = ctx;
    }


    @Override
    public Response serve(String uri, Method method,
                          Map<String, String> header, Map<String, String> parameters,
                          Map<String, String> files) {

        String msg = "<html><head><h1>Foldernaut</h1></head><body><ul>";
        String mimetype;


        // http://10.92.184.118:8080/

        if(uri.trim().equals("/")) { //Test HTML page serving :)
            System.out.println("URI received: " + uri);
            System.out.println("Method: " + method.name());
            String pathParam = "";
            for(Map.Entry m:parameters.entrySet()){
                System.out.println("Parameter KVP: " + m.getKey()+" "+m.getValue());
                if(m.getKey().equals("root")) {
                    pathParam = m.getValue().toString();
                }
            }
            System.out.println("Path parameter: " + pathParam);

            Resources resources = ctx.getResources();
            InputStream html = resources.openRawResource(R.raw.index);

            if(pathParam.isEmpty()) {
                return new NanoHTTPD.Response(Response.Status.OK, "text/html", html);
            }
            else { // http://10.92.184.118:8080/?root=pathParam
                String file_names = "\"";
                String file_paths = "\"";
                String file_types = "\"";

                if(pathParam.equals("sdcard")) {
                    File root = Environment.getExternalStorageDirectory();
                    for(File file : root.listFiles()) {
                        file_names = file_names + file.getName() + "*";
                        file_paths = file_paths + file.getAbsolutePath() + "*";
                        file_types = file_types + ((file.isDirectory())?"d":"f") + "*";
                    }
                    file_names = file_names + "\"";
                    file_paths = file_paths + "\"";
                    file_types = file_types + "\"";
                    String JSON_response = "{ \"filenames\":" + file_names + ", \"filepaths\":" + file_paths + ", \"filetypes\":" + file_types + " }";
                    System.out.println("JSON response: " + JSON_response);
                    return new NanoHTTPD.Response(Response.Status.OK, "application/json", JSON_response);

                } else {

                    File root = new File(pathParam);

                    if(root.isDirectory()) {
                        for(File file : root.listFiles()) {
                            file_names = file_names + file.getName() + "*";
                            file_paths = file_paths + file.getAbsolutePath() + "*";
                            file_types = file_types + ((file.isDirectory())?"d":"f") + "*";
                        }
                        file_names = file_names + "\"";
                        file_paths = file_paths + "\"";
                        file_types = file_types + "\"";
                        String JSON_response = "{ \"filenames\":" + file_names + ", \"filepaths\":" + file_paths + ", \"filetypes\":" + file_types + " }";
                        System.out.println("JSON response: " + JSON_response);
                        return new NanoHTTPD.Response(Response.Status.OK, "application/json", JSON_response);

                    }
                    else { // A file, not a folder
                        FileInputStream fis = null;
                        try {
                            System.out.println("File download stream: " + root.getAbsolutePath());
                            fis = new FileInputStream(root);
                        }
                        catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        //"application/octet-stream" mimetype downloads files. Use other mimetypes to stream files
                        mimetype = "application/octet-stream";
                        //Guess mime type
                        //MimeTypeMap myMime = MimeTypeMap.getSingleton();
                        //mimetype = myMime.getMimeTypeFromExtension(fileExt(filepath));

                        return new NanoHTTPD.Response(Response.Status.OK, mimetype, fis);
                    }
                }

            }
        }
        else if(uri.trim().equals("/upload") && (Method.POST.equals(method) || Method.PUT.equals(method))) {
            System.out.println("URI received: " + uri);
            System.out.println("Method: " + method.name());

            String filename="";
            String tempPath = "";
            String destinationPath = Environment.getExternalStorageDirectory().toString();

            // Received Map<String, String> files
            for(Map.Entry m : files.entrySet()){ // 'filearray' is defined in index.html form
                if(m.getKey().equals("filearray")) {
                    tempPath = m.getValue().toString();
                    System.out.println("Temp path: " + tempPath);
                }
            }
            for(Map.Entry m : parameters.entrySet()){
                if(m.getKey().equals("filearray")) {
                    filename = m.getValue().toString();
                    System.out.println("Filename: " + filename);
                }
                if(m.getKey().equals("servpath")) {
                    if(!m.getValue().toString().equals("sdcard"))
                        destinationPath = m.getValue().toString();
                    System.out.println("Destination to save file: " + destinationPath);
                }
            }

            File dst = new File(destinationPath, filename);
            if (dst.exists()) {
                // Response for confirm to overwrite
            }
            File src = new File(tempPath);
            try {
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dst);
                byte[] buf = new byte[65536];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (IOException ioe) {
                // Response for failed
            }

            Response r = new Response(Response.Status.REDIRECT, "application/json", "");
            r.addHeader("Location", "http://" + getDeviceIpAddress() + ":8080/?redir=" + destinationPath);
            return r;

        }

        else { // Serve filetree as HTML string! Ugh!
            File rootDir = Environment.getExternalStorageDirectory();
            File[] filesList;

            String filepath = "";
            if (uri.trim().equals("/")) {
                filepath = rootDir.getPath().trim();
                filesList = rootDir.listFiles();
            } else {
                filepath = uri.trim();
                filesList = new File(filepath).listFiles();
            }

            if(new File(filepath).isDirectory()) {
                for (File detailsOfFiles : filesList) {
                    msg += "<li><a href=\"" + detailsOfFiles.getAbsolutePath()+ "\" alt = \"\">"+ detailsOfFiles.getName() + "</a></li><br>";
                }
                mimetype = "text/html";
                msg = msg + "</ul></body></html>";
                httpRequestReceivedListener.onHttpRequestReceived(msg);
                return new NanoHTTPD.Response(Response.Status.OK, mimetype, msg);
            }
            else {

                FileInputStream fis = null;
                try {
                    System.out.println("File download stream: " + filepath);
                    fis = new FileInputStream(filepath);
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //"application/octet-stream" mimetype downloads files. Use other mimetypes to stream files
                mimetype = "application/octet-stream";
                //Guess mime type
                //MimeTypeMap myMime = MimeTypeMap.getSingleton();
                //mimetype = myMime.getMimeTypeFromExtension(fileExt(filepath));

                return new NanoHTTPD.Response(Response.Status.OK, mimetype, fis);

            }
        }

    }

    private String getDeviceIpAddress() {
        String actualConnectedToNetwork = null;
        ConnectivityManager connManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
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
        final WifiManager mWifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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

}

