package mycam.com.dominykas.documentreader.mycam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.util.Base64;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackgroundWorker extends AsyncTask<String, Void, String> {
    public static int number;
    public String TYPE;
    private Bitmap bitmap;
    Context context;
    String date;
    int failed;
    File file;
    long num;
    String pass;
    String path;
    String result;
    String site;
    int sk;
    int skaicius;
    int successful;
    String time;
    String user;

    BackgroundWorker(Context ctx) {
        this.context = ctx;
    }

    protected String doInBackground(String... params) {
        if (!isURLReachable(this.context)) {
            return "no internet";
        }
        Date Objdate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        this.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Objdate);
        this.TYPE = params[0];
        this.sk = 0;
        if (!this.TYPE.equals("internet")) {
            String username;
            HttpURLConnection httpURLConnection;
            OutputStream outputStream;
            BufferedWriter bufferedWriter;
            InputStream inputStream;
            BufferedReader bufferedReader;
            String str;
            if (this.TYPE.equals("send_Data")) {
                try {
                    MainActivity.cursor = MainActivity.databaseHelper.showData();
                    MainActivity.cursor.moveToFirst();
                    this.successful = 0;
                    this.failed = 0;
                    this.skaicius = MainActivity.cursor.getCount();
                    if (MainActivity.cursor.getPosition() < MainActivity.cursor.getCount()) {
                        this.path = MainActivity.cursor.getString(2);
                        this.date = MainActivity.cursor.getString(1);
                    }
                    if (BitmapFactory.decodeFile(this.path) != null) {
                        this.bitmap = BitmapFactory.decodeFile(this.path);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        this.bitmap.compress(CompressFormat.PNG, 100, baos);
                        String temp = Base64.encodeToString(baos.toByteArray(), 0);
                        username = MainActivity.sharedPreferences.getString("user", "");
                        String pass = MainActivity.sharedPreferences.getString("pass", "");
                        httpURLConnection = (HttpURLConnection) new URL(MainActivity.sharedPreferences.getString("web", "http://tlist.eu/demo") + "/sendimage.php").openConnection();
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setDoInput(true);
                        outputStream = httpURLConnection.getOutputStream();
                        bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                        bufferedWriter.write(URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(temp, "UTF-8") + "&" + URLEncoder.encode("date", "UTF-8") + "=" + URLEncoder.encode(this.date, "UTF-8") + "&" + URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8") + "&" + URLEncoder.encode("pass", "UTF-8") + "=" + URLEncoder.encode(pass, "UTF-8"));
                        bufferedWriter.flush();
                        bufferedWriter.close();
                        outputStream.close();
                        inputStream = httpURLConnection.getInputStream();
                        bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                        this.result = "";
                        str = "";
                        while (true) {
                            str = bufferedReader.readLine();
                            if (str == null) {
                                break;
                            }
                            this.result += str;
                        }
                        bufferedReader.close();
                        inputStream.close();
                        httpURLConnection.disconnect();
                    } else {
                        if (this.path != null) {
                            File file = new File(this.path);
                        }
                        if (this.file != null && this.file.exists()) {
                            this.file.delete();
                        }
                    }
                    if (this.result != null && this.result.equals("New record created successfully") && new File(this.path).delete() && MainActivity.cursor.getPosition() > -1 && MainActivity.cursor.getPosition() != MainActivity.cursor.getCount()) {
                        this.num = (long) MainActivity.databaseHelper.deleteRow(MainActivity.cursor.getString(0));
                    }
                    MainActivity.cursor.moveToNext();
                    return this.result;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            if (!this.TYPE.equals("start") && !this.TYPE.equals("settings")) {
                return null;
            }
            try {
                username = params[1];
                String password = params[2];
                this.site = params[3];
                httpURLConnection = (HttpURLConnection) new URL(this.site + "/login.php").openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                outputStream = httpURLConnection.getOutputStream();
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                bufferedWriter.write(URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8") + "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8"));
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                inputStream = httpURLConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                this.result = "";
                str = "";
                while (true) {
                    str = bufferedReader.readLine();
                    if (str == null) {
                        break;
                    }
                    this.result += str;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                this.user = username;
                this.pass = password;
            } catch (UnsupportedEncodingException e3) {
                e3.printStackTrace();
            } catch (ProtocolException e4) {
                e4.printStackTrace();
            } catch (MalformedURLException e5) {
                e5.printStackTrace();
            } catch (IOException e22) {
                e22.printStackTrace();
            }
            return this.result;
        } else if (isURLReachable(this.context)) {
            return "true";
        } else {
            return "false";
        }
    }

    protected void onPostExecute(String result) {
        int i = 0;
        super.onPostExecute(result);
        MainActivity.progressBar.setVisibility(4);
        MainActivity.progressBar2.setVisibility(4);
        MainActivity.btn.setVisibility(0);
        MainActivity.cancel.setVisibility(4);
        if (Settings.save != null) {
            Settings.save.setEnabled(true);
        }
        if (this.TYPE != null && this.TYPE.equals("internet")) {
            MainActivity.send.setVisibility(4);
        }
        if (result != null && !result.equals("no internet") && this.TYPE.equals("start") && result.equals("Successful login")) {
            MainActivity.isLoggedIn = true;
            MainActivity.btn.setVisibility(0);
        }
        if (this.TYPE != null && this.TYPE.equals("send_Data") && result == null) {
            Toast.makeText(this.context, "no files found", 1).show();
            MainActivity.btn.setVisibility(0);
        }
        if (result == null) {
            Toast.makeText(this.context, "Cant connect to the server", 0).show();
            MainActivity.btn.setVisibility(0);
        }
        if (result == null || !result.equals("New record created successfully") || !this.TYPE.equals("send_Data") || MainActivity.isCanceled) {
            MainActivity.progressBar2.setVisibility(4);
            MainActivity.progressBar.setVisibility(4);
            Toast.makeText(this.context, "Something went wrong " + result, 0).show();
            return;
        }
        MainActivity.cursor = MainActivity.databaseHelper.showData();
        if (MainActivity.cursor.getCount() > 0) {
            new BackgroundWorker(this.context).execute(new String[]{"send_Data"});
            MainActivity.progressBar2.setProgress(MainActivity.progressCount);
            MainActivity.progressCount++;
        } else {
            MainActivity.progressBar2.setVisibility(4);
            MainActivity.btn.setVisibility(0);
            Toast.makeText(this.context, "Successfully uploaded all images", 1).show();
        }
        MainActivity.menu.clear();
        File directory = new File(this.context.getFilesDir() + "/images");
        if (directory.exists()) {
            MainActivity.files = directory.listFiles();
            if (MainActivity.files.length < 1) {
                MainActivity.send.setVisibility(4);
            }
            File[] fileArr = MainActivity.files;
            int length = fileArr.length;
            while (i < length) {
                File f = fileArr[i];
                if (f.exists()) {
                    MainActivity.menu.add(f.getName());
                }
                i++;
            }
        }
    }

    public static boolean isURLReachable(Context context) {
        NetworkInfo netInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected()) {
            return false;
        }
        try {
            HttpURLConnection urlc = (HttpURLConnection) new URL(MainActivity.sharedPreferences.getString("web", "http://tlist.eu/demo")).openConnection();
            urlc.setConnectTimeout(1000);
            urlc.connect();
            if (urlc.getResponseCode() == Callback.DEFAULT_DRAG_ANIMATION_DURATION) {
                return true;
            }
            return false;
        } catch (MalformedURLException e) {
            Toast.makeText(context, "MalformedURLException", 1).show();
            return false;
        } catch (IOException e2) {
            return false;
        }
    }

    protected void onPreExecute() {
        super.onPreExecute();
        MainActivity.progressBar.setVisibility(0);
        MainActivity.progressBar2.setVisibility(0);
        MainActivity.cancel.setVisibility(0);
        MainActivity.btn.setVisibility(4);
        MainActivity.isRunning = true;
    }

    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
