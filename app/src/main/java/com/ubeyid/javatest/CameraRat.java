package com.ubeyid.javatest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static android.content.Context.CAMERA_SERVICE;

public class CameraRat implements TextureView.SurfaceTextureListener {

    TextureView textureView;
    CameraManager cameraManager;
    String cameraId = String.valueOf(CameraMetadata.LENS_FACING_FRONT);
    CameraDevice cameraDevice;
    Surface surface;
    CaptureRequest.Builder builder;
    SurfaceTexture surfaceTexture;
    int width;
    int height;
    CaptureRequest request;
    CameraCaptureSession cameraCaptureSession;
    Context  context;
    Activity activity;
    int cameraİndex=0;
    public CameraRat(){

    }
    //Add constructor for communicate with main activity
    public CameraRat(TextureView textureView,Activity activity,Context context) {
        this.textureView = textureView;
        this.activity=activity;
        this.context =context;
        textureView.setSurfaceTextureListener(this);
    }

    //When surface Texture fisrt created then open camera
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d("CAMERA", "surface Texture Available...");
        height=i;
        width=i1;
        openCamera(cameraId);

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d("CAMERA","surface Texture Size Changed...");

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.d("CAMERA","surface Texture Destroyed...");

        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        Log.d("CAMERA","surface Texture Updated...");

    }
    //calback for capture session
    CameraCaptureSession.CaptureCallback cameraCaptureSessionCaptureCallback=new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            Log.d("CAMERA","ONCAPTURE STARTED..");

        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            Log.d("CAMERA","ONCAPTURE PROGRESSED..");
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.d("CAMERA","ONCAPTURE COMPLETED..");
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Log.d("CAMERA","ONCAPTURE FAİLED..");
        }

        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            Log.d("CAMERA","ONCAPTURE SEQUENCE COMPLEATED..");
        }

        @Override
        public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
            Log.d("CAMERA","onCaptureSequenceAborted..");
        }

        @Override
        public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
            Log.d("CAMERA","onCaptureBufferLos..");
        }
    };
    CameraCaptureSession.StateCallback camptureSessionStateCallback=new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            //if configuration is successfull then get capture session
            CameraRat.this.cameraCaptureSession=cameraCaptureSession;
            Log.d("CAMERA","CAMERA ON CONFİGURED..");
            try {
                //and here we take multiple request from camera
                cameraCaptureSession.setRepeatingRequest(request,cameraCaptureSessionCaptureCallback,null);
                Log.d("CAMERA","STARTİNG CAPTURE..");
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
            Log.d("CAMERA","CAMERA ON CONFİGURE FAİLED..");
        }
    };
    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            CameraRat.this.cameraDevice=cameraDevice;
            Log.d("CAMERA","CAMERA OPENED..");
            try {
                //if camera opened start preview the camera
                startPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {

            Log.d("CAMERA","CAMERA DISCONNECTED..");
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {

            Log.d("CAMERA","CAMERA ON ERROR..");
        }
    };

    public void startPreview() throws CameraAccessException {
        //for previewing the camera set flag CameraDevice.TEMPLATE_PREVIEW
        builder=cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        Log.d("CAMERA","STARTİNG PREVİEW.");
        //get surface texture from texture view
        surfaceTexture=textureView.getSurfaceTexture();
        //get surface from surface texture
        surface=new Surface(surfaceTexture);
        /*builder.set(CaptureRequest.SCALER_CROP_REGION,new Rect(0,0,width,height));*/
        //seting up capture request
        builder.set(CaptureRequest.JPEG_THUMBNAIL_SIZE,new Size(width,height));
        builder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE,CameraMetadata.STATISTICS_FACE_DETECT_MODE_FULL);
        //for preview camera ad surface to builder
        builder.addTarget(surface);
        //geting request from bulder
        request=builder.build();
        //create the capture request
        cameraDevice.createCaptureSession(Arrays.asList(surface),camptureSessionStateCallback,null);



    }
    /*public void openFlash(){

    }*/
    //when user clicked change camera button we changing the camera with another
    public void changeCamera(){
        //check for permission camera
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            cameraManager=null;
            //close the camera
            cameraDevice.close();
            cameraDevice=null;
            /*find default camera and change to another*/

            if(cameraİndex%2==0){
                cameraId=String.valueOf(CameraMetadata.LENS_FACING_BACK);

            }else{
                cameraId=String.valueOf(CameraMetadata.LENS_FACING_FRONT);
            }
            //reopen camera after setting up settings
            openCamera(cameraId);
            cameraİndex++;

        }else {
            Toast.makeText(context,"Could not changed camera!!",Toast.LENGTH_LONG).show();
        }

    }
    public void openCamera(String cameraId)  {
         //get camera manager instance
        cameraManager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        //check camera permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.CAMERA},1995);
            return;
            //if user once clicked cancel button and did not give the permission
        }if(ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.CAMERA)){
            createAlertDialog(context,R.string.message_open_camera,false,R.drawable.ic_warning_black_24dp,"Okey","Cancel");
        }
        try {
            cameraManager.openCamera(cameraId, stateCallback, null);

        } catch (CameraAccessException e) {
            Log.d("CAMERA","CAMERA ACCESS EXCEPTION CANOT OPEN CAMERA..");
            e.printStackTrace();
        }


    }
    public  void closeCamera(){
        //close camera and set it default
        cameraManager=null;
        cameraDevice.close();
        cameraDevice=null;
    }
    public Bitmap takePhoto(){
        //just take picture from texture view
        //and return it
        return textureView.getBitmap();
    }
    //saving image to device
    public boolean saveImage(Bitmap bitmap){
        boolean state=false;
        //check permission for Write external storage
        if(ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)
        {
              //don't change sdcard directory but you can change javatest and images directory
            //set new directory for our app
            File file=new File("sdcard/Javatest/Images/");
              //create new suffix randomly
            String suffix="JAVA-TEST-"+(int)Math.floor(Math.random()*100000000)+(int)Math.floor(Math.random()*100000000)+".jpg";
           //create file directory for save the image in it
            String fileDirectory=file.toString()+"/"+suffix;
            File file1=new File(fileDirectory);
            //check file exist or not
            if(!file.exists()){
                //check folder is created or not
                if(file.mkdirs()){
                    Log.d("IMAGE","PHOTO DİRECTORY CREATED");

                    {
                        //if file created then make state true;
                       if(saveCreatedFile(file1,bitmap)){
                           state=true;
                       }else{
                           state=false;
                       }
                    }
                }else{
                    state=false;
                }

            } else{
                {
                    if(saveCreatedFile(file1,bitmap)){
                        state=true;
                    }else{
                        state=false;
                    }
                }
            }


        }else if(ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
          createAlertDialog(context,R.string.message_save_storage,false,R.drawable.ic_warning_black_24dp,"Okey","Cancel");
        }

        return state;
    }
    private boolean saveCreatedFile(File file1,Bitmap bitmap) {
        boolean state=false;
        if(!file1.exists()){
            try {
                if(file1.createNewFile()){
                    Log.d("IMAGE","FILE CREATED..");
                    FileOutputStream fileOutputStream = null;
                    try {
                        fileOutputStream=new FileOutputStream(file1);
                        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
                        //turn bitmap to byte array
                        bitmap.compress(Bitmap.CompressFormat.JPEG,85,byteArrayOutputStream);
                        byte[] bytes=byteArrayOutputStream.toByteArray();
                        {
                            try {
                                //add all bytes from bitmap to output stream for given directory
                                fileOutputStream.write(bytes);
                                state=true;
                                Log.d("IMAGE","IMAGE SAVED"+file1.getAbsolutePath());

                            } catch (IOException e) {
                                e.printStackTrace();
                                state=false;
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        state=false;
                    }
                }else{
                    state=false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                state=false;
            }
        }
        return state;
    }

    public static void getPermissions() {
            String[] permissions=new String[3];
            permissions[0]=Manifest.permission.CAMERA;
            permissions[1]=Manifest.permission.READ_EXTERNAL_STORAGE;
            permissions[2]=Manifest.permission.WRITE_EXTERNAL_STORAGE;

            {
                    CameraRat cameraRat=new CameraRat();
                    for (int i=0;i<permissions.length;i++){
                        ActivityCompat.requestPermissions(cameraRat.activity,permissions,1995);
                    }

            }
        }
    public static void createAlertDialog(final Context context, final int messageId, boolean cancelable, int iconId, String positiveButtonName, String negativeButtonName){
        //create AlertDialog
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setMessage(messageId)
                .setCancelable(cancelable)
                .setIcon(iconId)
                .setPositiveButton(positiveButtonName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CameraRat.getPermissions();
                    }
                })
                .setNegativeButton(negativeButtonName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(context,messageId,Toast.LENGTH_LONG).show();

                    }
                })
                .create()
                .show();

     }

}
