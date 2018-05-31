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
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import com.jcraft.jsch.*;

import static com.stangassinger.mysync.Scp_to.executeRemoteCommand;


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

    /**
     * Builds a standard alert dialog,
     *    using setTitle to set its title, setMessage to set its message,
     *    and setPositiveButton and setNegativeButton to set its buttons.
     * Defines toast messages to appear depending on which alert
     *    button is clicked.
     * Shows the alert.
     * @param view  The activity's view in which the alert will appear.
     */
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

        File root = Environment.getExternalStorageDirectory();
        //String rootPath= root.getPath();

        output = this.getFilesOfDirectory(root, "pdf");

        for (File strArr : output) {
            Log.i(TAG, "------------------>" + strArr.getAbsolutePath() );
        }

        //scp_to.scp2();

        new AsyncTask<Integer, Void, Void>(){
            @Override
            protected Void doInBackground(Integer... params) {
                try {
                    executeRemoteCommand("usr", "pass","192.168.0.15", 22);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(1);




        /*
        FilenameFilter mp3Filter = new FilenameFilter() {
            File f;
            public boolean accept(File dir, String name) {

                if(name.endsWith("*.pdf")){
                    return true;
                }

                f = new File(dir.getAbsolutePath()+"/"+name);

                return f.isDirectory();
            }
        };

        String listOfFileNames[] = root.list(mp3Filter);

        for (String strArr : listOfFileNames) {
            Log.i(TAG, "------------------>" + strArr);
        }*/

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




