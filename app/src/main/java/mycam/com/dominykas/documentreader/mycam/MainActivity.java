package mycam.com.dominykas.documentreader.mycam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.*;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  {
    public static int COUNT;
    public static int MENU_ITEM_COUNT = 0;
    public static int PHOTOS_BEING_SENT = 0;
    public static boolean SINGLE_MODE;
    public static String USERNAME;
    public static ImageButton btn;
    public static ImageButton btnMenu;
    public static Button cancel;
    public static Cursor cursor;
    public static DatabaseHelper databaseHelper;
    public static Editor editor;
    public static File[] files;
    public static ImageView img;
    public static boolean isCanceled;
    public static boolean isConnected;
    public static boolean isLandscape;
    public static boolean isLoggedIn;
    public static boolean isPermissionGranted = false;
    public static boolean isRunning = false;
    public static Menu kmenu;
    public static String mCurrentPhotoPath;
    public static Menu menu;
    public static Button orientation;
    public static String path;
    public static int permission;
    public static boolean previewing = true;
    public static ProgressBar progressBar;
    public static ProgressBar progressBar2;
    public static int progressCount;
    public static Button send;
    public static SharedPreferences sharedPreferences;
    public static boolean usesFlash;
    public static int width;
    AndroidCamera androidCamera;
    Bundle bundle;
    DrawerLayout drawerLayout;
    PictureCallback jpegCallback;
    MenuItem landscape;
    NavigationView navigationView;
    MenuItem portrait;
    float posX;
    ShutterCallback shutterCallback;
    SurfaceView surfaceView;
    BackgroundWorker worker;

    /* renamed from: documentreader.dominykas.com.mycam.MainActivity$1 */
    class C03471 implements ShutterCallback {
        C03471() {
        }

        public void onShutter() {
        }
    }

    /* renamed from: documentreader.dominykas.com.mycam.MainActivity$2 */
    class C03482 implements PictureCallback {
        C03482() {
        }

        public void onPictureTaken(byte[] data, Camera camera) {
            if (data != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                if (MainActivity.isLandscape) {
                    new BackgroundSqliteWorker(MainActivity.this.getApplicationContext()).execute(new Bitmap[]{MainActivity.scaleDown(bitmap, 1500.0f, true)});
                } else {
                    new BackgroundSqliteWorker(MainActivity.this.getApplicationContext()).execute(new Bitmap[]{MainActivity.RotateBitmap(MainActivity.scaleDown(bitmap, 1500.0f, true), 90.0f)});
                }
                camera.startPreview();
                MainActivity.previewing = true;
            }
        }
    }

    /* renamed from: documentreader.dominykas.com.mycam.MainActivity$3 */
    class C03493 implements OnClickListener {
        C03493() {
        }

        public void onClick(View view) {
            if (MainActivity.this.bundle != null) {
                MainActivity.isLoggedIn = MainActivity.this.bundle.getBoolean("log");
                if (MainActivity.this.bundle.getString("USER") != null) {
                    MainActivity.USERNAME = MainActivity.this.bundle.getString("USER");
                }
            }
            if (MainActivity.send.getVisibility() == 4) {
                MainActivity.progressBar2.setVisibility(4);
                MainActivity.cancel.setVisibility(4);
            }
            if (!MainActivity.previewing) {
                return;
            }
            if (MainActivity.PHOTOS_BEING_SENT >= 5) {
                Toast.makeText(MainActivity.this, "Too many requests, wait for photos to be sent", 0).show();
            } else if (AndroidCamera.camera != null) {
                AndroidCamera.camera.takePicture(MainActivity.this.shutterCallback, null, MainActivity.this.jpegCallback);
                MainActivity.previewing = false;
            } else {
                Toast.makeText(MainActivity.this, "Camera null", 0).show();
            }
        }
    }

    /* renamed from: documentreader.dominykas.com.mycam.MainActivity$4 */
    class C03504 implements OnClickListener {
        C03504() {
        }

        public void onClick(View view) {
            MainActivity.cancel.setVisibility(0);
            MainActivity.isCanceled = false;
            MainActivity.files = new File(MainActivity.this.getApplicationContext().getFilesDir() + "/images").listFiles();
            if (MainActivity.files.length > 0) {
                MainActivity.progressCount = 1;
                MainActivity.progressBar2.setProgress(MainActivity.progressCount);
                MainActivity.progressBar2.setMax(MainActivity.files.length);
                MainActivity.this.worker = new BackgroundWorker(MainActivity.this.getBaseContext());
                MainActivity.this.worker.execute(new String[]{"send_Data"});
                return;
            }
            Toast.makeText(MainActivity.this, "no files to upload", 0).show();
        }
    }

    /* renamed from: documentreader.dominykas.com.mycam.MainActivity$5 */
    class C03515 implements OnClickListener {
        C03515() {
        }

        public void onClick(View view) {
            MainActivity.this.worker.cancel(true);
            MainActivity.progressBar2.setVisibility(4);
            MainActivity.btn.setVisibility(0);
            MainActivity.progressBar.setVisibility(4);
            MainActivity.isCanceled = true;
            MainActivity.cancel.setVisibility(4);
        }
    }

    /* renamed from: documentreader.dominykas.com.mycam.MainActivity$6 */
    class C03526 implements OnClickListener {
        C03526() {
        }

        public void onClick(View view) {
            MainActivity.this.drawerLayout.openDrawer(8388611);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) C0353R.layout.activity_main);
        permission = ContextCompat.checkSelfPermission(this, "android.permission.CAMERA");
        if (permission == 0) {
            btn = (ImageButton) findViewById(C0353R.id.button);
            send = (Button) findViewById(C0353R.id.send);
            progressBar = (ProgressBar) findViewById(C0353R.id.progressBar);
            progressBar2 = (ProgressBar) findViewById(C0353R.id.progressBar2);
            this.surfaceView = (SurfaceView) findViewById(C0353R.id.surfaceView);
            btnMenu = (ImageButton) findViewById(C0353R.id.menu);
            cancel = (Button) findViewById(C0353R.id.cancel);
            cancel.setVisibility(4);
            usesFlash = false;
            isLandscape = true;
            sharedPreferences = getPreferences(0);
            getWindow().setFormat(0);
            this.bundle = getIntent().getExtras();
            setNavigationViewListener();
            databaseHelper = new DatabaseHelper(this);
            this.navigationView = (NavigationView) findViewById(C0353R.id.sidebar);
            kmenu = this.navigationView.getMenu();
            menu = kmenu.addSubMenu("FILES THAT ARE NOT SENT");
            this.portrait = kmenu.findItem(C0353R.id.portrait);
            this.landscape = kmenu.findItem(C0353R.id.landscape);
            progressBar.setVisibility(4);
            this.drawerLayout = (DrawerLayout) findViewById(C0353R.id.drawer);
            this.posX = btn.getX();
            progressBar2.setScaleY(5.0f);
            progressBar2.setVisibility(4);
            this.androidCamera = new AndroidCamera(this, this.surfaceView);
            File directory = new File(getFilesDir() + "/images");
            if (directory.exists()) {
                files = directory.listFiles();
                for (File f : files) {
                    if (f.exists()) {
                        menu.add(f.getName());
                    }
                }
            } else {
                directory.mkdir();
            }
            if (files == null || files.length <= 1) {
                send.setVisibility(4);
            } else {
                send.setVisibility(0);
            }
            this.shutterCallback = new C03471();
            this.jpegCallback = new C03482();
            generateButtonListeners();
        }
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case C0353R.id.flash:
                break;
            case C0353R.id.settings:
                startActivity(new Intent(this, Settings.class));
                break;
        }
        if (usesFlash) {
            usesFlash = false;
            item.setIcon(C0353R.drawable.flash_on);
            item.setTitle("TURN ON FLASH");
            this.androidCamera.startCamera();
            this.androidCamera.startPreview();
        } else {
            usesFlash = true;
            item.setIcon(C0353R.drawable.flash_off);
            item.setTitle("TURN OFF FLASH");
            this.androidCamera.startCamera();
            this.androidCamera.startPreview();
        }
        if (item.getItemId() == C0353R.id.portrait || item.getItemId() == C0353R.id.landscape) {
            if (isLandscape) {
                if (item.getItemId() == C0353R.id.portrait) {
                    isLandscape = false;
                    item.setIcon(C0353R.drawable.checked);
                    this.landscape.setIcon(C0353R.drawable.unchecked);
                    setRequestedOrientation(1);
                }
            } else if (item.getItemId() == C0353R.id.landscape) {
                isLandscape = true;
                item.setIcon(C0353R.drawable.checked);
                this.portrait.setIcon(C0353R.drawable.unchecked);
                setRequestedOrientation(0);
            }
        }
        this.drawerLayout.closeDrawer(8388611);
        return true;
    }

    private void setNavigationViewListener() {
        ((NavigationView) findViewById(C0353R.id.sidebar)).setNavigationItemSelectedListener(this);
    }

    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "fuck u ", 0).show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                this.drawerLayout.openDrawer(8388611);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(maxImageSize / ((float) realImage.getWidth()), maxImageSize / ((float) realImage.getHeight()));
        return Bitmap.createScaledBitmap(realImage, Math.round(((float) realImage.getWidth()) * ratio), Math.round(((float) realImage.getHeight()) * ratio), filter);
    }

    protected void onStart() {
        super.onStart();
        if (VERSION.SDK_INT >= 23) {
            int hasCameraPermission = checkSelfPermission("android.permission.CAMERA");
            List<String> permissions = new ArrayList();
            if (hasCameraPermission != 0) {
                permissions.add("android.permission.CAMERA");
            }
            if (!permissions.isEmpty()) {
                requestPermissions((String[]) permissions.toArray(new String[permissions.size()]), 111);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 111:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == 0) {
                        startActivity(new Intent(this, MainActivity.class));
                    } else if (grantResults[i] == -1) {
                        System.out.println("Permissions --> Permission Denied: " + permissions[i]);
                        isPermissionGranted = false;
                    }
                }
                return;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return;
        }
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void generateButtonListeners() {
        btn.setOnClickListener(new C03493());
        send.setOnClickListener(new C03504());
        cancel.setOnClickListener(new C03515());
        btnMenu.setOnClickListener(new C03526());
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == 2) {
            Toast.makeText(this, "landscape", 1).show();
            AndroidCamera.camera.setDisplayOrientation(0);
        } else if (newConfig.orientation == 1) {
            AndroidCamera.camera.setDisplayOrientation(90);
            Toast.makeText(this, "portrait", 1).show();
        }
    }
}
