package mycam.com.dominykas.documentreader.mycam;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.List;

public class AndroidCamera extends Activity implements Callback {
    public static Camera camera;
    PictureCallback jpegCallback;
    SurfaceHolder surfaceHolder;

    AndroidCamera(Context context, SurfaceView surfaceView) {
        this.surfaceHolder = surfaceView.getHolder();
        this.surfaceHolder.addCallback(this);
        this.surfaceHolder.setType(3);
    }

    public void startCamera() {
        if (camera != null) {
            Parameters params = camera.getParameters();
            params.setFocusMode("continuous-picture");
            if (MainActivity.isLandscape) {
                camera.setDisplayOrientation(0);
            } else {
                camera.setDisplayOrientation(90);
            }
            if (MainActivity.usesFlash) {
                params.setFlashMode("on");
            } else {
                params.setFlashMode("off");
            }
            List<Size> sizes = params.getSupportedPictureSizes();
            int width = 0;
            int height = 0;
            for (int i = 1; i < sizes.size(); i++) {
                if (((Size) sizes.get(i)).height * ((Size) sizes.get(i)).width > width * height) {
                    width = ((Size) sizes.get(i)).width;
                    height = ((Size) sizes.get(i)).height;
                }
            }
            params.setPictureSize(width, height);
            camera.setParameters(params);
        }
    }

    public void startPreview() {
        if (camera != null) {
            if (MainActivity.isLandscape) {
                camera.setDisplayOrientation(0);
            } else {
                camera.setDisplayOrientation(90);
            }
            if (MainActivity.previewing) {
                camera.stopPreview();
                MainActivity.previewing = false;
            }
            if (camera != null) {
                try {
                    camera.setPreviewDisplay(this.surfaceHolder);
                    camera.startPreview();
                    MainActivity.previewing = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        camera = Camera.open();
        startCamera();
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        startPreview();
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.stopPreview();
        camera.release();
        camera = null;
        MainActivity.previewing = false;
    }
}
