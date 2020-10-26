package com.ubeyid.javatest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;


/********************
 * @Author Ubeyid Koşsalun
 *
 *******************/

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
  TextureView textureView;
  CameraRat cameraRat=null;
  Bitmap bitmap=null;
    //declaring variables
  ImageView   imageView,takePhoto,cancel,save,changeCamera;
/*  int counterImageAnimate=0;*/

//when requesting for permission this method is called
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //check for permission if permission is Manifest.permission.CAMERA
        /*Toast.makeText(this,"Permissions"+permissions.length,Toast.LENGTH_LONG).show();*/
        if(requestCode==1995 && grantResults.length>0){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if(cameraRat!=null){
                    cameraRat.openCamera(cameraRat.cameraId);
                }
            }else if(grantResults[0]==PackageManager.PERMISSION_DENIED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.CAMERA)){
                 CameraRat.createAlertDialog(this,R.string.message_open_camera,false,R.drawable.ic_warning_black_24dp,"Okey","Cancel");
                }
            }
            if(grantResults[1]==PackageManager.PERMISSION_GRANTED){
                Log.d("IMAGE","READ EXTERNAL STORAGE PERMISSION İS GRANTED");
            }else if(grantResults[1]==PackageManager.PERMISSION_DENIED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    CameraRat.createAlertDialog(this,R.string.message_read_storage,false,R.drawable.ic_warning_black_24dp,"Okey","Cancel");
                }
            }
            if(grantResults[2]==PackageManager.PERMISSION_GRANTED){
                Log.d("IMAGE","WRITE EXTERNAL STORAGE PERMISSION İS GRANTED");
                if(bitmap!=null){
                    if(cameraRat.saveImage(bitmap)){
                        Toast.makeText(this,"Image Saved",Toast.LENGTH_LONG).show();
                        setViewsInvisible(imageView,cancel,save);
                    }
                }

            }else if(grantResults[2]==PackageManager.PERMISSION_DENIED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    CameraRat.createAlertDialog(this,R.string.message_save_storage,false,R.drawable.ic_warning_black_24dp,"Okey","Cancel");
                }
            }
        }

    }
    //change visibilty to visible  for given views
    private void setViewsVisible(View ... views){
        for(int i=0;i<views.length;i++){
            views[i].setVisibility(View.VISIBLE);
        }
    }
    //change visibilty to invisible  for given views
    private void setViewsInvisible(View ... views){
        for(int i=0;i<views.length;i++){
            views[i].setVisibility(View.INVISIBLE);
        }
    }

    //when activity first created

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPermissions();
        //make screen full screen
        Window window=getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //set content view our activty main layout
        setContentView(R.layout.activity_main);
        //getting  variables
        changeCamera=findViewById(R.id.changeCamera);
        textureView = findViewById(R.id.textureView);
        cameraRat=new CameraRat(textureView,MainActivity.this,this);
        takePhoto=findViewById(R.id.takePhoto);
        //set imageview  invisible
        imageView=findViewById(R.id.imageView);
        imageView.setVisibility(View.INVISIBLE);
        //set save image invisisible
        save=findViewById(R.id.save);
        save.setVisibility(View.INVISIBLE);
        //set cancel image invisible
        cancel=findViewById(R.id.cancel);
        cancel.setVisibility(View.INVISIBLE);
        //set onclick listeners for imageViews
        takePhoto.setOnClickListener(this);
        cancel.setOnClickListener(this);
        imageView.setOnClickListener(this);
        changeCamera.setOnClickListener(this);
        save.setOnClickListener(this);



    }



    //get view  clicked
    @Override
    public void onClick(View view) {
        // getting id for view
     switch (view.getId()){
         case R.id.save :{
             if(bitmap!=null){
                 if(ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                     if(cameraRat.saveImage(bitmap)){
                         Toast.makeText(this,"Image Saved",Toast.LENGTH_LONG).show();
                         setViewsInvisible(imageView,cancel,save);
                     }
                 }else{
                     getPermissions();
                 }


             }
         }
         break;
         case R.id.changeCamera: {
             setViewsInvisible(imageView,cancel,save);
             cameraRat.changeCamera();
             Toast.makeText(MainActivity.this,"Camera changed",Toast.LENGTH_LONG).show();

         }
         break;
         //if view ==cancel
         case R.id.cancel: {
             setViewsInvisible(imageView,cancel,save);

         }
         break;
         //if view==takephoto
         case R.id.takePhoto:{
             setViewsVisible(imageView,cancel,save);
             bitmap=cameraRat.takePhoto();
             imageView.setImageBitmap(bitmap);
         }
         break;
         //if view is none of of them
         default:{
             //show toast message for this issiue
             Toast.makeText(MainActivity.this,"Unresolved request",Toast.LENGTH_LONG).show();
         }


     }

    }
    //when activty restarted then reopen camera

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("CAMERA","ON RESTART");
        cameraRat.openCamera(cameraRat.cameraId);

    }


    //when activty stopped then relesae camera
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("CAMERA","ACTİVİTY DURDU...");
        if(cameraRat.cameraDevice!=null){
            cameraRat.closeCamera();
        }


    }


    // get defined permissions
   public void getPermissions(){
       String[] permissions=new String[3];
       permissions[0]=Manifest.permission.CAMERA;
       permissions[1]=Manifest.permission.READ_EXTERNAL_STORAGE;
       permissions[2]=Manifest.permission.WRITE_EXTERNAL_STORAGE;

       {
           //this loop is for every permission
           for (int i=0;i<permissions.length;i++){
               ActivityCompat.requestPermissions(MainActivity.this,permissions,1995);
           }


       }
   }



}



