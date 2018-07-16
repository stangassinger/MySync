/*
 * Copyright (C) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.stangassinger.mysync;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.zip.*;


import static android.os.Environment.DIRECTORY_DCIM;
import static com.stangassinger.mysync.Scp_to.checkHosts;
import static com.stangassinger.mysync.Scp_to.executeRemoteSCP;
import static com.stangassinger.mysync.Scp_to.zipPics;



public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private static String valid_hostname = "";
    private static boolean zipFile_ready      = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btn = (Button) findViewById(R.id.button1);
        btn.setText("Searching for host ...");


        new AsyncTask<Integer, Void, Void>(){
            @Override
            protected Void doInBackground(Integer... params) {
                String host = "";
                try {
                    host = checkHosts("192.168.0");
                    Log.i(TAG, "---  valid hostname  ------>" + host );
                    valid_hostname = host;
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }


            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (valid_hostname.length() == 0){
                    btn.setText("NO HOST FOUND !!");
                }else {
                    btn.setText("Sync with " + valid_hostname);
                }
            }


        }.execute(1);


    }



    public void onClickShowAlert(View view) {
        if (valid_hostname.length() == 0){
            return;
        }
        // Build the alert dialog.
        AlertDialog.Builder myAlertBuilder = new AlertDialog.Builder(MainActivity.this);
        // Set the dialog title.
        myAlertBuilder.setTitle(R.string.alert_title);
        // Set the dialog message.
        myAlertBuilder.setMessage(R.string.alert_message);
        // Add the buttons.
        myAlertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User clicked OK button.
                Toast.makeText(getApplicationContext(), R.string.pressed_ok,
                        Toast.LENGTH_SHORT).show();
            }
        });
        myAlertBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the CANCEL button.
                Toast.makeText(getApplicationContext(), R.string.pressed_cancel,
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Create and show the AlertDialog.
        myAlertBuilder.show();
        final File pics_zip_location = new File(  this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "pics.zip");
        final File DOWNLOAD_DIR = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        final List<File> all_pic_files;
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM  + "/Camera");
        all_pic_files = this.getFilesOfDirectory(root, "");


        new AsyncTask<Integer, Void, Void>(){
            @Override
            protected Void doInBackground(Integer... params) {
                try {
                        File zipFile = null;
                        zipFile = new File(DOWNLOAD_DIR, "pics.zip");
                        if (!zipFile.exists())
                            zipFile.createNewFile();

                    zipPics(all_pic_files, zipFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }


            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                zipFile_ready = true;
                return;
            }
        }.execute(1);




        new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... params) {
                while (zipFile_ready == false) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                    Log.i(TAG, "--------  executeRemoteSCP    ---------->");
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                    try {
                        //executeRemoteCommand("usr", "pass","192.168.0.15", 22);
                        executeRemoteSCP(Conf.USERNAME, valid_hostname, 22,
                                pics_zip_location.getAbsolutePath(), "pic_" + timeStamp + ".zip");
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;

            }
        }.execute(1);


    }








    private  List<File> getFilesOfDirectory(File dir, String fileNamePart) {

        List<File> result;

        if (dir.exists() && dir.isDirectory()
                && !dir.getAbsolutePath().contains("root")
                && dir.getAbsolutePath().split("/").length < 10) {
            Log.i("fileSearch", "search in directory " + dir.getAbsolutePath());
            File[] files = dir.listFiles();

            if (files != null)
                result = new ArrayList<File>(Arrays.asList(files));
            else
                result = new ArrayList<File>();
        }

        else
            result = Collections.emptyList();

        /* Filter for interesting files. */
        List<File> filteredResult = new ArrayList<File>();
        for (File file : result) {
            if (file.getAbsolutePath().toLowerCase()
                    .contains(fileNamePart.toLowerCase()))
                filteredResult.add(file);
            // no else.
        }

        /* Search recursively. */
        List<File> furtherResults = new ArrayList<File>();
        for (File file : result) {
            if (file.isDirectory())
                furtherResults.addAll(getFilesOfDirectory(file, fileNamePart));
            // no else.
        }
        // end for.

        filteredResult.addAll(furtherResults);

        return filteredResult;
    }
}




