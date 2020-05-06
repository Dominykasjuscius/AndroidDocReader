package mycam.com.dominykas.documentreader.mycam;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.File;

public class Settings extends Activity {
    public static Button delete;
    public static Button save;
    BackgroundWorker backgroundWorker;
    Cursor cursor;
    EditText password;
    EditText username;
    EditText website;

    /* renamed from: documentreader.dominykas.com.mycam.Settings$1 */
    class C03541 implements OnClickListener {
        C03541() {
        }

        public void onClick(View view) {
            String user = Settings.this.username.getText().toString();
            String pass = Settings.this.password.getText().toString();
            String site = Settings.this.website.getText().toString();
            Editor editor = MainActivity.sharedPreferences.edit();
            editor.putString("user", user);
            editor.putString("pass", pass);
            editor.putString("web", site);
            editor.commit();
            Intent intent = new Intent(Settings.this.getApplicationContext(), MainActivity.class);
            if (user != null) {
                intent.putExtra("USER", user);
            }
            intent.setFlags(268435456);
            Settings.this.startActivity(intent);
        }
    }

    /* renamed from: documentreader.dominykas.com.mycam.Settings$2 */
    class C03552 implements OnClickListener {
        C03552() {
        }

        public void onClick(View view) {
            File directory = new File(Settings.this.getFilesDir() + "/images");
            if (directory.exists()) {
                for (File f : directory.listFiles()) {
                    if (f.exists()) {
                        f.delete();
                    }
                }
                MainActivity.databaseHelper.deleteAll();
                Toast.makeText(Settings.this, "Deleted all files", 0).show();
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0353R.layout.activity_settings);
        this.username = (EditText) findViewById(C0353R.id.user);
        this.password = (EditText) findViewById(C0353R.id.password);
        save = (Button) findViewById(C0353R.id.save);
        delete = (Button) findViewById(C0353R.id.delete);
        this.website = (EditText) findViewById(C0353R.id.website);
        this.cursor = MainActivity.databaseHelper.showData();
        this.cursor.moveToFirst();
        if (MainActivity.sharedPreferences != null) {
            this.username.setText(MainActivity.sharedPreferences.getString("user", "enter username here"));
            this.password.setText(MainActivity.sharedPreferences.getString("pass", ""));
            this.website.setText(MainActivity.sharedPreferences.getString("web", "http://tlist.eu/demo"));
        }
        save.setOnClickListener(new C03541());
        delete.setOnClickListener(new C03552());
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        startActivity(new Intent(this, MainActivity.class));
        return true;
    }
}
