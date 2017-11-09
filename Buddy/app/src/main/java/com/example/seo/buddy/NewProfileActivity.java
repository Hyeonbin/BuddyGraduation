package com.example.seo.buddy;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Seo on 2016-12-21.
 */
public class NewProfileActivity extends AppCompatActivity { // 새로운 프로필을 저장하기 위한 NewProfileActivity

    ProfileActivity currentActivity = (ProfileActivity)ProfileActivity.CurrentActivity; // ProfileActivity에 접근하기 위한 변수 선언

    // 사진에 관련된 시그널 정의
    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_PHOTO_ALBUM = 1;
    private static final int REQUEST_CROP = 2;

    // 사진의 uri, path를 저장하기 위한 변수
    private Uri ImageCaptureUri;
    private String absolutePath;

    // profile 데이터베이스에 접근하기 위한 변수들
    private ProfileDBHelper profiledbhelper;
    private SQLiteDatabase profiledb;
    private Cursor profilecursor = null;
    private String profilesql;

    // 성별 변수
    int men = 0;
    int women = 0;

    String gender;

    String strPhotoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newprofile);

        ImageView newprofile_backbtn = (ImageView)findViewById(R.id.Buddy_Newprofile_Backbtn); // 백버튼 정의

        // 이름과 생년월일 edittext 정의
        final EditText newprofile_name = (EditText)findViewById(R.id.Buddy_Newprofile_Name);
        final EditText newprofile_old = (EditText)findViewById(R.id.Buddy_Newprofile_Old);

        // 성별 버튼 정의
        final Button newprofile_men = (Button)findViewById(R.id.Buddy_Newprofile_Menbtn);
        final Button newprofile_women = (Button)findViewById(R.id.Buddy_Newprofile_Womenbtn);

        CircleImageView newprofile_photo = (CircleImageView) findViewById(R.id.Buddy_Newprofile_Photo); // 프로필 사진 정의

        Button newprofile_savebtn = (Button)findViewById(R.id.Buddy_Newprofile_Savebtn); // 저장 버튼 정의

        // profile 데이터베이스에 접근하기 위한 변수들 셋팅
        profiledbhelper = new ProfileDBHelper(this);
        profiledb = profiledbhelper.getReadableDatabase();
        profilesql = "SELECT * FROM profiletable";
        profilecursor = profiledb.rawQuery(profilesql, null);
        profilecursor.moveToFirst();

        newprofile_backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 백버튼에 대한 클릭 리스너
                AlertDialog.Builder builder = new AlertDialog.Builder(NewProfileActivity.this, R.style.MyAlertDialogTheme); // 다이얼로그 Build
                builder.setTitle("프로필 입력을 취소하시겠습니까?");
                builder.setCancelable(false);

                builder.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });

                builder.setNegativeButton("계속", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        newprofile_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 사진에 대한 클릭 리스너
                DialogInterface.OnClickListener CameraListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { // 사진 촬영
                        takeCamera();
                    }
                };
                DialogInterface.OnClickListener AlbumListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { // 앨범에서 불러오기
                        takeAlbum();
                    }
                };
                DialogInterface.OnClickListener CancelListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { // 취소
                        dialogInterface.dismiss();
                    }
                };

                new AlertDialog.Builder(NewProfileActivity.this, R.style.MyAlertDialogTheme) // 다이얼로그 Build
                        .setTitle("업로드할 이미지 선택                    ")
                        .setPositiveButton("사진촬영", CameraListener)
                        .setNeutralButton("취소", CancelListener)
                        .setNegativeButton("앨범선택", AlbumListener)
                        .show();
            }
        });

        newprofile_men.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 남성 버튼 클릭 리스너, 선택에 따라 시그널과 색이 변한다
                if(men==0) {
                    newprofile_men.setBackgroundColor(Color.parseColor("#93DAFF"));
                    newprofile_women.setBackgroundColor(Color.parseColor("#e0e0e0"));
                    men = 1;
                    women = 0;
                    gender = "Man";
                }
                else {
                    newprofile_men.setBackgroundColor(Color.parseColor("#e0e0e0"));
                    men = 0;
                    gender = null;
                }
            }
        });

        newprofile_women.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 여성 버튼 클릭 리스너, 선택에 따라 시그널과 색이 변한다
                if(women==0) {
                    newprofile_women.setBackgroundColor(Color.parseColor("#FF98A3"));
                    newprofile_men.setBackgroundColor(Color.parseColor("#e0e0e0"));
                    women = 1;
                    men = 0;
                    gender = "Woman";
                }
                else {
                    newprofile_women.setBackgroundColor(Color.parseColor("#e0e0e0"));
                    women = 0;
                    gender = null;
                }
            }
        });

        newprofile_savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 저장 버튼에 대한 클릭 리스너
                // edittext를 통해 이름과 생년월일을 받아온다
                String name = newprofile_name.getText().toString();
                String old = newprofile_old.getText().toString();
                name = name.trim();
                old = old.trim();
                if(name.getBytes().length <= 0 || old.getBytes().length <=0 || gender == null) { // 입력 다 될 수 있도록 조건문 작성
                    Toast toast = Toast.makeText(getApplicationContext(), "입력되지 않은 항목이 있습니다", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    if(profilecursor.getCount() == 0) { // 프로필 첫 작성인 경우, 데이터베이스에 프로필 데이터 저장
                        profiledbhelper.delete();
                        profiledbhelper.Insert(absolutePath, newprofile_name.getText().toString(), newprofile_old.getText().toString(), gender);
                        profiledbhelper.close();
                        Intent intent = new Intent(NewProfileActivity.this, ProfileActivity.class); // 저장 후 ProfileActivity로 이동
                        currentActivity.finish();
                        finish();
                        Toast toast = Toast.makeText(getApplicationContext(), "저장 되었습니다", Toast.LENGTH_LONG);
                        toast.show();
                        startActivity(intent);
                    } else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewProfileActivity.this, R.style.MyAlertDialogTheme); // 다이얼로그 Build

                        alertDialogBuilder.setTitle("기존 프로필을 수정하시겠습니까?");
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setPositiveButton("수정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) { // 수정버튼 누르면 수정됨
                                profiledbhelper.delete(); // 데이터 삭제 후 다시 저장
                                profiledbhelper.Insert(absolutePath, newprofile_name.getText().toString(), newprofile_old.getText().toString(), gender);
                                profiledbhelper.close();
                                Intent intent = new Intent(NewProfileActivity.this, ProfileActivity.class);
                                currentActivity.finish();
                                finish();
                                Toast toast = Toast.makeText(getApplicationContext(), "저장 되었습니다", Toast.LENGTH_LONG);
                                toast.show();
                                startActivity(intent);
                            }
                        });
                        alertDialogBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) { // 취소버튼
                                dialogInterface.dismiss();
                            }
                        });

                        AlertDialog alert = alertDialogBuilder.create();
                        alert.show();
                    }
                }
            }
        });
    }

    // 이후 사진 관련 메소드들 -> AddlogActivity 참조 (같은 맥락)
    public void takeCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        ImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Buddy", url));

        intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageCaptureUri);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    public void takeAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_PHOTO_ALBUM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK)
            return;

        switch (requestCode)
        {
            case REQUEST_PHOTO_ALBUM :
                ImageCaptureUri = data.getData();

                File original_file = getImageFile(ImageCaptureUri);

                ImageCaptureUri = createSaveCropFile();
                File cpoy_file = new File(ImageCaptureUri.getPath());

                copyFile(original_file, cpoy_file);

                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(ImageCaptureUri, "image/*");
                intent.putExtra( "output", ImageCaptureUri);
                startActivityForResult(intent, REQUEST_CROP);

                break;

            case REQUEST_CROP :
                String full_path = ImageCaptureUri.getPath();

                Bitmap bmp = BitmapFactory.decodeFile(full_path);
                CircleImageView newprofile_photo = (CircleImageView) findViewById(R.id.Buddy_Newprofile_Photo);
                newprofile_photo.setImageBitmap(bmp);

                absolutePath = full_path;

                break;

            case REQUEST_TAKE_PHOTO :
                Intent intent1 = new Intent("com.android.camera.action.CROP");
                intent1.setDataAndType(ImageCaptureUri, "image/*");
                intent1.putExtra( "output", ImageCaptureUri);
                startActivityForResult(intent1, REQUEST_CROP);
                break;
        }
    }

    private Uri createSaveCropFile(){
        Uri uri;
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        strPhotoName = url;
        uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Buddy", url));
        return uri;
    }

    public static boolean copyFile(File srcFile, File destFile) {
        boolean result = false;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally  {
                in.close();
            }
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    private File getImageFile(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        if (uri == null) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        Cursor mCursor = getContentResolver().query(uri, projection, null, null,
                MediaStore.Images.Media.DATE_MODIFIED + " desc");
        if(mCursor == null || mCursor.getCount() < 1) {
            return null; // no cursor or no record
        }
        int column_index = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        mCursor.moveToFirst();

        String path = mCursor.getString(column_index);

        if (mCursor !=null ) {
            mCursor.close();
            mCursor = null;
        }

        return new File(path);
    }

    private static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            OutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(NewProfileActivity.this, R.style.MyAlertDialogTheme);
            builder.setTitle("프로필 입력을 취소하시겠습니까?");
            builder.setCancelable(false);

            builder.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            builder.setNegativeButton("계속", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}
