package com.example.seo.buddy;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.DialogPreference;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * Created by Seo on 2016-12-21.
 */
public class AddlogActivity extends AppCompatActivity { // 로그 다이어리를 추가하기 위한 AddlogActivity

    private static final int REQUEST_TAKE_PHOTO = 0; // 사진촬영 시그널 변수
    private static final int REQUEST_PHOTO_ALBUM = 1; // 사진앨범 시그널 변수
    private static final int REQUEST_CROP = 2; // 사진을 크롭하기 위한 시그널 변수

    private Uri ImageCaptureUri; // 사진 uri 변수
    private String absolutePath = null; // 사진이 저장 될 path 변수

    LogDBHelper logdbhelper; // logdbhelper 변수 (로그 다이어리 데이터베이스를 위한 헬퍼)

    LogbookActivity logbookActivity = (LogbookActivity)LogbookActivity.currentfragment; // LogbookActivity에 접근하기 위한 변수

    int meal = 0, cmpmeal = 0; // 식사 종류 변수(meal은 현재 식사, cmpmeal은 현재 식사의 비교 값)
    String table = null, cmptable = null; // 식사 종류에 다른 데이터베이스 테이블(table은 현재 식사, cmptable은 현재 식사의 비교 테이블)
    String strPhotoName; // 사진 이름

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addlog);

        java.util.Calendar cal = java.util.Calendar.getInstance(); // 시간을 받기위한 변수, 함수

        // 현재 시간을 각 변수에 저장
        final int year = cal.get(cal.YEAR);
        final int month = cal.get(cal.MONTH) + 1;
        final int date = cal.get(cal.DATE);
        final int hour = cal.get(cal.HOUR);
        final int min = cal.get(cal.MINUTE);
        final String ampm;

        // AM, PM을 나누기 위한 알고리즘
        if(cal.get(cal.AM_PM)==0)
            ampm = "AM";
        else
            ampm = "PM";

        logdbhelper = new LogDBHelper(this); // logdbhelper 새롭게 할당

        ImageView addlog_backbtn = (ImageView)findViewById(R.id.Buddy_Addlog_Backbtn); // back 버튼

        // 시간 정보를 출력하기 위한 textview들
        TextView addlog_year = (TextView)findViewById(R.id.Buddy_Addlog_Year);
        TextView addlog_month = (TextView)findViewById(R.id.Buddy_Addlog_Month);
        final TextView addlog_day = (TextView)findViewById(R.id.Buddy_Addlog_Day);
        TextView addlog_hour = (TextView)findViewById(R.id.Buddy_Addlog_Hour);
        TextView addlog_min = (TextView)findViewById(R.id.Buddy_Addlog_Min);
        TextView addlog_ampm = (TextView)findViewById(R.id.Buddy_Addlog_Ampm);

        LinearLayout addlog_nophoto = (LinearLayout) findViewById(R.id.Buddy_Addlog_Nophoto);
        ImageView addlog_photo = (ImageView)findViewById(R.id.Buddy_Addlog_Photo); // 사진 imageview

        // 장소, 혈당, 다이어리 edittext들
        final EditText addlog_location = (EditText)findViewById(R.id.Buddy_Addlog_Location);
        final EditText addlog_glucose = (EditText)findViewById(R.id.Buddy_Addlog_Glucose);
        final EditText addlog_diary = (EditText)findViewById(R.id.Buddy_Addlog_Diary);

        Button addlog_savebtn = (Button)findViewById(R.id.Buddy_Addlog_Savebtn); // save 버튼

        TextView addlog_meal = (TextView)findViewById(R.id.Buddy_Addlog_Meal); // 식사 정보 textview

        // 시간 정보를 시간 정보들에 대한 textview들에 출력
        addlog_year.setText(""+year);
        addlog_month.setText(""+month);
        addlog_day.setText(""+date);
        if(hour == 0)
            addlog_hour.setText("12");
        else
            addlog_hour.setText(""+hour);

        // 분이 한자리 수인 경우 0을 붙이고 아니면 안 붙인다
        if(String.valueOf(min).length() == 1)
            addlog_min.setText("0"+min);
        else
            addlog_min.setText(""+min);

        // AM, PM 정보 출력
        addlog_ampm.setText(ampm);

        registerForContextMenu(addlog_meal); // 컨텍스트 메뉴 버튼에 식사 정보 textview 출력

        addlog_backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // Back 버튼에 대한 클릭 리스너
                AlertDialog.Builder builder = new AlertDialog.Builder(AddlogActivity.this, R.style.MyAlertDialogTheme); // 다이얼로그 출력
                builder.setTitle("다이어리 입력을 취소하시겠습니까?");
                builder.setCancelable(false);

                builder.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { // 뒤로 이동
                        finish();
                    }
                });

                builder.setNegativeButton("계속", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { // 동작 계속 진행
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        addlog_nophoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener CameraListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { // 사진촬영
                        takeCamera();
                    }
                };
                DialogInterface.OnClickListener AlbumListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { // 앨범으로 이동
                        takeAlbum();
                    }
                };
                DialogInterface.OnClickListener CancelListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { // 취소
                        dialogInterface.dismiss();
                    }
                };

                new AlertDialog.Builder(AddlogActivity.this, R.style.MyAlertDialogTheme) // 사진을 눌렀을 경우 다이얼로그가 출력
                        .setTitle("업로드할 이미지 선택                    ")
                        .setPositiveButton("사진촬영", CameraListener)
                        .setNeutralButton("취소", CancelListener)
                        .setNegativeButton("앨범선택", AlbumListener)
                        .show(); // 다이얼로그 출력
            }
        });

        addlog_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 사진에 대한 클릭 리스너
                DialogInterface.OnClickListener CameraListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { // 사진촬영
                        takeCamera();
                    }
                };
                DialogInterface.OnClickListener AlbumListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { // 앨범으로 이동
                        takeAlbum();
                    }
                };
                DialogInterface.OnClickListener CancelListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { // 취소
                        dialogInterface.dismiss();
                    }
                };

                new AlertDialog.Builder(AddlogActivity.this, R.style.MyAlertDialogTheme) // 사진을 눌렀을 경우 다이얼로그가 출력
                        .setTitle("업로드할 이미지 선택                    ")
                        .setPositiveButton("사진촬영", CameraListener)
                        .setNeutralButton("취소", CancelListener)
                        .setNegativeButton("앨범선택", AlbumListener)
                        .show(); // 다이얼로그 출력
            }
        });

        addlog_savebtn.setOnClickListener(new View.OnClickListener() { // Save 버튼에 대한 클릭 리스너
            int alert = 0; // 내부 시그널 변수
            @Override
            public void onClick(View view) {
                String location = addlog_location.getText().toString(); // 장소 정보 불러와서 저장
                String glucose = addlog_glucose.getText().toString(); // 혈당 정보 불러와서 저장
                if (location.getBytes().length <= 0 && meal == 0) { // 장소를 입력하지 않은 경우 토스트 메시지 출력
                    Toast toast = Toast.makeText(getApplicationContext(), "장소가 입력되지 않았습니다", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    // 각 항목을 입력하지 않은 경우 토스트 메시지 출력
                    if (meal != 0) { // 식사 정보가 있는 경우
                        if (location.getBytes().length <= 0 && glucose.getBytes().length <= 0) {
                            Toast toast = Toast.makeText(getApplicationContext(), "장소와 혈당이 입력되지 않았습니다", Toast.LENGTH_LONG);
                            toast.show();
                        } else if (location.getBytes().length <= 0 && glucose.getBytes().length > 0) {
                            Toast toast = Toast.makeText(getApplicationContext(), "장소가 입력되지 않았습니다", Toast.LENGTH_LONG);
                            toast.show();
                        } else if (location.getBytes().length > 0 && glucose.getBytes().length <= 0) {
                            Toast toast = Toast.makeText(getApplicationContext(), "혈당이 입력되지 않았습니다", Toast.LENGTH_LONG);
                            toast.show();
                        } else { // 모두 입력한 경우 여기로 이동
                                if (logdbhelper.Searchbydate(table, year, month, date) == 0) { // Searchbydate 함수 호출, 기존 혈당 데이터가 있는 경우
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddlogActivity.this, R.style.MyAlertDialogTheme);
                                    // 다이얼로그 출력
                                   alertDialogBuilder.setTitle("기존 혈당 데이터를 수정하시겠습니까?");
                                    alertDialogBuilder.setCancelable(false);
                                    alertDialogBuilder.setPositiveButton("수정", // 수정 버튼, Updatetableforgraph 함수 호출, 해당 데이터 수정
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface dialog, int id) {
                                                    logdbhelper.Updatetableforgraph(table, year, month, date, hour, min, ampm, addlog_location.getText().toString(),
                                                            absolutePath, meal, addlog_glucose.getText().toString(), addlog_diary.getText().toString());
                                                    Toast toast = Toast.makeText(getApplicationContext(), " 수정 되었습니다 ", Toast.LENGTH_LONG);
                                                    toast.show();
                                                    logdbhelper.close();
                                                    logbookActivity.getActivity().finish(); // 상위에 있는 logbookactivity 종료
                                                    Intent intent = new Intent(AddlogActivity.this, MainActivity.class); // MainActivity 새로 출력
                                                    startActivity(intent);
                                                    finish(); // 현재 Activity 종료
                                                }
                                            });
                                    alertDialogBuilder.setNegativeButton("취소", // 취소 버튼
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                                    // 다이얼로그 출력
                                    AlertDialog alert = alertDialogBuilder.create();
                                    alert.show();
                                } else if (logdbhelper.Searchbydate(table, year, month, date) == 1) { // Searchbydate 함수 호출, 해당 날짜에 대한 데이터가 맨 처음 들어가는 경우
                                    logdbhelper.Insert(year, month, date, hour, min, ampm, addlog_location.getText().toString(), // 데이터 입력
                                            absolutePath, meal, addlog_glucose.getText().toString(), addlog_diary.getText().toString());
                                    if(meal != 7) // 취침 시 혈당 정보가 아닌 경우
                                        logdbhelper.Insert(year, month, date, 0, 0, null, null, absolutePath, cmpmeal, "0", null); // 해당 데이터에 대한 반대 식사 정보의 임시 데이터 입력
                                    alert = 1; // 시그널 1로 변환
                                } else if (logdbhelper.Searchbydate(table, year, month, date) == 2) { // Searchbydate 함수 호출, 해당 날짜에 대한 반대 식사 정보의 데이터가 있는 경우
                                    int index = logdbhelper.getIndex(year, month, date, hour, min, ampm, addlog_location.getText().toString(),
                                            absolutePath, meal, addlog_glucose.getText().toString(), addlog_diary.getText().toString());
                                    logdbhelper.Delete(index, meal, year, month, date);
                                    logdbhelper.Insert(year, month, date, hour, min, ampm, addlog_location.getText().toString(), // 데이터 입력
                                            absolutePath, meal, addlog_glucose.getText().toString(), addlog_diary.getText().toString());
                                    //logdbhelper.Updatetableforgraph(table, year, month, date, hour, min, ampm, addlog_location.getText().toString(),
                                            //absolutePath, meal, addlog_glucose.getText().toString(), addlog_diary.getText().toString()); // 해당 데이터 수정
                                    //logdbhelper.Insert(year, month, date, hour, min, ampm, addlog_location.getText().toString(), // 데이터 입력
                                            //absolutePath, 0, addlog_glucose.getText().toString(), addlog_diary.getText().toString());
                                    alert = 1; // 시그널 1로 변환
                                }
                            //logdbhelper.Insert(year, month, date, hour, min, ampm, addlog_location.getText().toString(),
                            //absolutePath, meal, addlog_glucose.getText().toString(), addlog_diary.getText().toString());
                            /*else {
                                logdbhelper.Insert(year, month, date, hour, min, ampm, addlog_location.getText().toString(),
                                        absolutePath, meal, "1", addlog_diary.getText().toString());
                                alert = 1;
                            }*/
                            if (alert == 1) { // 시그널이 1인 경우
                                logdbhelper.close();
                                logbookActivity.getActivity().finish(); // 상위에 있는 logbookactivity 종료
                                Intent intent = new Intent(AddlogActivity.this, MainActivity.class); // MainActivity 새로 출력
                                startActivity(intent);
                                Toast toast = Toast.makeText(getApplicationContext(), "저장 되었습니다", Toast.LENGTH_LONG);
                                toast.show();
                                finish();
                            }
                        }
                    } else { // 식사 정보가 없는 경우
                        logdbhelper.Insert(year, month, date, hour, min, ampm, addlog_location.getText().toString(),
                                absolutePath, meal, "1", addlog_diary.getText().toString()); // 해당 데이터 입력, 혈당 정보는 필요 없으므로 1을 저장
                        logdbhelper.close();
                        logbookActivity.getActivity().finish(); // 상위에 있는 logbookactivity 종료
                        Intent intent = new Intent(AddlogActivity.this, MainActivity.class); // MainActivity 새로 출력
                        startActivity(intent);
                        Toast toast = Toast.makeText(getApplicationContext(), "저장 되었습니다", Toast.LENGTH_LONG);
                        toast.show();
                        finish();
                    }
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) { // 컨텍스트 메뉴를 만들기 위한 메소드

        super.onCreateContextMenu(menu, v, menuInfo);

        // 메뉴 아이템들
        menu.add(0, 1, 200, "선택 안함");
        menu.add(0, 2, 200, "아침 식사 전");
        menu.add(0, 3, 200, "아침 식사 후");
        menu.add(0, 4, 200, "점심 식사 전");
        menu.add(0, 5, 200, "점심 식사 후");
        menu.add(0, 6, 200, "저녁 식사 전");
        menu.add(0, 7, 200, "저녁 식사 후");
        menu.add(0, 8, 200, "취침 전");

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) { // 컨텍스트 메뉴의 아이템들에 대한 메소드
        TextView addlog_meal = (TextView)findViewById(R.id.Buddy_Addlog_Meal); // 식사 정보에 대한 textview
        LinearLayout addlog_glucoselayout = (LinearLayout)findViewById(R.id.Buddy_Addlog_Glucoselayout); // 혈당을 입력할 경우 필요한 layout
        View addlog_glucoseline = (View)findViewById(R.id.Buddy_Addlog_Glucoseline); // 항목에 대한 구분선
        switch (item.getItemId()){ // 각 항목에 대한 switch 문, 항목을 어떻게 고르느냐에 따라 table, cmptable, meal, cmpmeal을 모두 변경
            case 1:
                addlog_meal.setText("선택 시 길게 클릭");
                addlog_glucoselayout.setVisibility(View.GONE);
                addlog_glucoseline.setVisibility(View.GONE);
                meal = 0;
                return true;
            case 2:
                addlog_meal.setText("아침 식사 전");
                addlog_glucoselayout.setVisibility(View.VISIBLE);
                addlog_glucoseline.setVisibility(View.VISIBLE);
                table = "bbtable";
                cmptable = "batable";
                meal = 1;
                cmpmeal = 2;
                return true;
            case 3:
                addlog_meal.setText("아침 식사 후");
                addlog_glucoselayout.setVisibility(View.VISIBLE);
                addlog_glucoseline.setVisibility(View.VISIBLE);
                table = "batable";
                cmptable = "bbtable";
                meal = 2;
                cmpmeal = 1;
                return true;
            case 4:
                addlog_meal.setText("점심 식사 전");
                addlog_glucoselayout.setVisibility(View.VISIBLE);
                addlog_glucoseline.setVisibility(View.VISIBLE);
                table = "lbtable";
                cmptable = "latable";
                meal = 3;
                cmpmeal = 4;
                return true;
            case 5:
                addlog_meal.setText("점심 식사 후");
                addlog_glucoselayout.setVisibility(View.VISIBLE);
                addlog_glucoseline.setVisibility(View.VISIBLE);
                table = "latable";
                cmptable = "lbtable";
                meal = 4;
                cmpmeal = 3;
                return true;
            case 6:
                addlog_meal.setText("저녁 식사 전");
                addlog_glucoselayout.setVisibility(View.VISIBLE);
                addlog_glucoseline.setVisibility(View.VISIBLE);
                table = "dbtable";
                cmptable = "datable";
                meal = 5;
                cmpmeal = 6;
                return true;
            case 7:
                addlog_meal.setText("저녁 식사 후");
                addlog_glucoselayout.setVisibility(View.VISIBLE);
                addlog_glucoseline.setVisibility(View.VISIBLE);
                table = "datable";
                cmptable = "dbtable";
                meal = 6;
                cmpmeal = 5;
                return true;
            case 8:
                addlog_meal.setText("취침 전");
                addlog_glucoselayout.setVisibility(View.VISIBLE);
                addlog_glucoseline.setVisibility(View.VISIBLE);
                meal = 7;
                table = "sleeptable";
                return true;
        }
        return super.onContextItemSelected(item);
    }

    public void takeCamera() { // 카메라 촬영에 대한 메소드
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // 카메라 촬영 화면으로 이동

        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg"; // 현재 시간을 이름으로 하여 url 지정
        ImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Buddy", url)); // uri 지정

        intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageCaptureUri); // 사진 찍은 후 결과 데이터 처리
        startActivityForResult(intent, REQUEST_TAKE_PHOTO); // 시그널과 함께 result 메소드로 이동
    }

    public void takeAlbum() { // 앨범 선택에 대한 메소드
        Intent intent = new Intent(Intent.ACTION_PICK); // 앨범 선택 화면으로 이동
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE); // 이미지 타입으로 지정
        startActivityForResult(intent, REQUEST_PHOTO_ALBUM); // 시그널과 함께 result 메소드로 이동
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { // result를 받기 위한 메소드
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK) // 제대로 된 result를 받지 못한 경우, 메소드 종료
            return;

        switch (requestCode)
        {
            case REQUEST_PHOTO_ALBUM : // 앨범에 대한 시그널
                ImageCaptureUri = data.getData(); // uri 받음

                File original_file = getImageFile(ImageCaptureUri); // 원래 파일을 미리 저장

                ImageCaptureUri = createSaveCropFile(); // 크롭한 이미지 파일에 대한 uri를 새로 저장
                File cpoy_file = new File(ImageCaptureUri.getPath()); // 새로운 uri에 대해서 파일 할당

                copyFile(original_file, cpoy_file); // copyFile 메소드로 원래 이미지를 copy

                // 사진을 crop하기 위해 request_crop으로 이동
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(ImageCaptureUri, "image/*");
                intent.putExtra( "output", ImageCaptureUri);
                startActivityForResult(intent, REQUEST_CROP);

                break;

            case REQUEST_CROP :
                String full_path = ImageCaptureUri.getPath(); // 받은 uri path 저장

                // bitmap으로 변환 후 해당 imageview에 출력
                Bitmap bmp = BitmapFactory.decodeFile(full_path);
                LinearLayout addlog_nophoto = (LinearLayout)findViewById(R.id.Buddy_Addlog_Nophoto);
                ImageView addlog_photo = (ImageView)findViewById(R.id.Buddy_Addlog_Photo);
                addlog_nophoto.setVisibility(View.GONE);
                addlog_photo.setImageBitmap(bmp);
                addlog_photo.setVisibility(View.VISIBLE);

                absolutePath = full_path; // 데이터베이스에 저장하기 위해 uri path는 따로 한번 더 저장

                break;

            case REQUEST_TAKE_PHOTO :
                // 사진을 찍은 후 crop하기 위해 request_crop으로 이동
                Intent intent1 = new Intent("com.android.camera.action.CROP");
                intent1.setDataAndType(ImageCaptureUri, "image/*");
                intent1.putExtra( "output", ImageCaptureUri);
                startActivityForResult(intent1, REQUEST_CROP);
                break;
        }
    }

    private Uri createSaveCropFile(){ // crop한 파일의 uri를 만들기 위한 메소드
        Uri uri;
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        strPhotoName = url;
        uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Buddy", url));
        return uri;
    }

    public static boolean copyFile(File srcFile, File destFile) { // 사진을 앨범에서 선택한 경우 원본 이미지에서 카피하기 위한 메소드
        boolean result = false;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally  {
                in.close();
            }
            result = true;
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    private File getImageFile(Uri uri) { // uri를 통해 이미지 파일로 만들기 위한 메소드
        String[] projection = { MediaStore.Images.Media.DATA };
        if (uri == null) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        Cursor mCursor = getContentResolver().query(uri, projection, null, null,
                MediaStore.Images.Media.DATE_MODIFIED + " desc");
        if(mCursor == null || mCursor.getCount() < 1) { // 데이터가 없는 경우
            return null;
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

    private static boolean copyToFile(InputStream inputStream, File destFile) { // 새로운 파일로 스트림에 있는 데이터를 저장하기 위한 메소드
        try {
            OutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096]; // 바이트 단위로 저장
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) { // 읽을 데이터가 있을 때까지 반복
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
    public boolean onKeyDown(int keyCode, KeyEvent event) { // 스마트폰 버튼에 대한 메소드

        if(keyCode == KeyEvent.KEYCODE_BACK) { // 종료 버튼
            AlertDialog.Builder builder = new AlertDialog.Builder(AddlogActivity.this, R.style.MyAlertDialogTheme); // 종료 다이얼로그 호출
            builder.setTitle("다이어리 입력을 취소하시겠습니까?");
            builder.setCancelable(false);

            builder.setPositiveButton("취소", new DialogInterface.OnClickListener() { // 취소버튼
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            builder.setNegativeButton("계속", new DialogInterface.OnClickListener() { // 동작 계속 진행
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

