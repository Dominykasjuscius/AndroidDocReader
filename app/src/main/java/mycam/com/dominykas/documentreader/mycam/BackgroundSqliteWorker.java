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
import android.view.View;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.security.auth.callback.Callback;

public class BackgroundSqliteWorker extends AsyncTask<Bitmap, Void, String> {
    public static File directory;
    public static File file;
    Bitmap bit;
    Bitmap bitmap;
    Context context;
    DatabaseHelper databaseHelper;
    String date;
    private long num;
    String result;

    BackgroundSqliteWorker(Context ctx) {
        this.context = ctx;
    }

    protected String doInBackground(Bitmap... bitmaps) {
        Date Objdate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = format.format(Objdate);
        String time = format2.format(Objdate);
        this.bit = bitmaps[0];
        directory = new File(this.context.getFilesDir() + "/images");
        if (!directory.exists()) {
            directory.mkdir();
        }
        file = new File(directory, date + ".png");
        try {
            if (file.createNewFile()) {
                FileOutputStream fileOutputStream = new FileOutputStream(file.getPath());
                if (bitmaps[0].compress(CompressFormat.PNG, 100, fileOutputStream)) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.databaseHelper = new DatabaseHelper(this.context);
        this.databaseHelper.InsertData(file.getPath(), date);
        if (!isURLReachable(this.context)) {
            return "Cant connect to the server";
        }
        try {
            MainActivity.cursor = this.databaseHelper.showData();
            MainActivity.cursor.moveToFirst();
            if (MainActivity.cursor.getPosition() < MainActivity.cursor.getCount()) {
                MainActivity.path = MainActivity.cursor.getString(2);
                this.date = MainActivity.cursor.getString(1);
            }
            if (BitmapFactory.decodeFile(file.getPath()) != null) {
                this.bitmap = BitmapFactory.decodeFile(file.getPath());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                this.bitmap.compress(CompressFormat.PNG, 100, baos);
                String temp = Base64.encodeToString(baos.toByteArray(), 0);
                String username = MainActivity.sharedPreferences.getString("user", "");
                String pass = MainActivity.sharedPreferences.getString("pass", "");
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(MainActivity.sharedPreferences.getString("web", "http://tlist.eu/demo") + "/sendimage.php").openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                bufferedWriter.write(URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(temp, "UTF-8") + "&" + URLEncoder.encode("date", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8") + "&" + URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8") + "&" + URLEncoder.encode("pass", "UTF-8") + "=" + URLEncoder.encode(pass, "UTF-8"));
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                this.result = "";
                String str = "";
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
            }
            if (this.result != null && this.result.equals("New record created successfully") && new File(MainActivity.path).delete() && MainActivity.cursor.getPosition() > -1 && MainActivity.cursor.getPosition() != MainActivity.cursor.getCount()) {
                this.num = (long) this.databaseHelper.deleteRow(MainActivity.cursor.getString(0));
            }
            MainActivity.cursor.moveToNext();
            return this.result;
        } catch (MalformedURLException e2) {
            e2.printStackTrace();
            return null;
        } catch (IOException e3) {
            e3.printStackTrace();
            return null;
        }
    }

    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(this.context, "Sending photo to database...", Toast.LENGTH_SHORT).show();
        MainActivity.progressBar.setVisibility(View.VISIBLE);
        MainActivity.PHOTOS_BEING_SENT++;
    }

    protected void onPostExecute(String s) {
        int i = 0;
        super.onPostExecute(s);
        MainActivity.menu.add(file.getName());
        MainActivity.progressBar.setVisibility(View.INVISIBLE);
        if (s != null && s.equals("New record created successfully")) {
            Toast.makeText(this.context, "photo successfully sent to database", Toast.LENGTH_SHORT).show();
        }
        if (s != null && s.equals("incorrect user")) {
            Toast.makeText(this.context, "Check username and password", Toast.LENGTH_SHORT).show();
        }
        if (s != null && s.equals("Cant connect to the server")) {
            Toast.makeText(this.context, "Cant connect to the server", Toast.LENGTH_SHORT).show();
        }
        if (MainActivity.files == null || MainActivity.files.length <= 0) {
            MainActivity.send.setVisibility(View.INVISIBLE);
        } else {
            MainActivity.send.setVisibility(View.VISIBLE);
        }
        MainActivity.PHOTOS_BEING_SENT--;
        MainActivity.menu.clear();
        File directory = new File(this.context.getFilesDir() + "/images");
        if (directory.exists()) {
            MainActivity.files = directory.listFiles();
            if (MainActivity.files != null && MainActivity.files.length < 1) {
                MainActivity.send.setVisibility(View.INVISIBLE);
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

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(maxImageSize / ((float) realImage.getWidth()), maxImageSize / ((float) realImage.getHeight()));
        return Bitmap.createScaledBitmap(realImage, Math.round(((float) realImage.getWidth()) * ratio), Math.round(((float) realImage.getHeight()) * ratio), filter);
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
            Toast.makeText(context, "MalformedURLException", Toast.LENGTH_LONG).show();
            return false;
        } catch (IOException e2) {
            return false;
        }
    }
}
