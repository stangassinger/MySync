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
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.*;


import com.jcraft.jsch.*;

import static android.os.Environment.DIRECTORY_DCIM;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.stangassinger.mysync.Scp_to.executeRemoteCommand;
import static com.stangassinger.mysync.Scp_to.executeRemoteSCP;


/**
 * This app shows a button to trigger a standard alert dialog.
 */
public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private List<File>  output;

    Scp_to scp_to = new Scp_to();

    /**
     * Creates the view.
     * @param savedInstanceState    The saved instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





    }


    public void onClickShowAlert(View view) {
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


        final File testFile = new File(  this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "pics.zip");



        File root = Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM);
        output = this.getFilesOfDirectory(root, "jpg");

        for (File strArr : output) {
            Log.i(TAG, "------------------>" + strArr.getAbsolutePath() );
        }



        try {
            zip(output);
        }catch (Exception e) {
            e.printStackTrace();
        }





        //scp_to.scp2();

        new AsyncTask<Integer, Void, Void>(){
            @Override
            protected Void doInBackground(Integer... params) {

                try {
                    //executeRemoteCommand("usr", "pass","192.168.0.15", 22);
                    executeRemoteSCP(Conf.USERNAME, Conf.PASSWORD,Conf.HOSTNAME, 22,
                            testFile.getAbsolutePath(), "MyFile.zip");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }


        }.execute(1);

    }



    private void zip( List<File> files ) throws Exception {
        final int BUFFER_SIZE = 2048;
        File zipFile = null;

        try {
            zipFile = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "pics.zip");
            if (!zipFile.exists())
                zipFile.createNewFile();

        } catch (Exception e) {
            Log.e(TAG, "Unable to create pics.zip file.");
        }



        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream( new FileOutputStream(zipFile) ));

        try {
            byte data[] = new byte[BUFFER_SIZE];

            for ( File file : files ) {
                FileInputStream fileInputStream = new FileInputStream( file );

                origin = new BufferedInputStream(fileInputStream, BUFFER_SIZE);

                String filePath = file.getAbsolutePath();

                try {
                    ZipEntry entry = new ZipEntry( filePath.substring( filePath.lastIndexOf("/") + 1 ) );

                    out.putNextEntry(entry);

                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


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




