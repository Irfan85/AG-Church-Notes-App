package com.example.tahmidsnotes;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NotesViewModel viewModel;
    private NoteListAdapter noteListAdapter;
    private static DownloadManager downloadManager;
    private static long downloadID;
    private static String downloadedFilePath;
    private boolean isNetworkAvailable;

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the id of the download
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            // Verify the downloaded file belong to this app
            if (downloadID == id) {
                File noteFile = new File(downloadedFilePath);
                Uri noteFileUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", noteFile);
                Intent openNoteIntent = new Intent(Intent.ACTION_VIEW);
                openNoteIntent.setDataAndType(noteFileUri, "application/pdf");
                openNoteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                openNoteIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(openNoteIntent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Listen for any download completion
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        // Check whether internet connection exists or not
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    isNetworkAvailable = true;
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    isNetworkAvailable = false;
                }
            });
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            isNetworkAvailable = (networkInfo != null) && networkInfo.isAvailable();
        }

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        final ListView noteListView = findViewById(R.id.noteListView);
        final TextView noteListEmptyTextView = findViewById(R.id.noteListEmptyTextView);
        final ProgressBar noteListProgressBar = findViewById(R.id.noteListProgressBar);

        viewModel.getIsNoteListLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (!isLoading) {
                    noteListProgressBar.setVisibility(View.GONE);

                    if (isNetworkAvailable){
                        noteListEmptyTextView.setText(R.string.empty_note_list_text);
                    }else{
                        noteListEmptyTextView.setText(R.string.no_connection_text);
                    }
                    noteListView.setEmptyView(noteListEmptyTextView);
                }
            }
        });

        viewModel.getNoteListLiveData().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                if (notes != null) {
                    noteListAdapter = new NoteListAdapter(MainActivity.this, notes);
                    noteListView.setAdapter(noteListAdapter);
                }

                noteListAdapter.notifyDataSetChanged();
            }
        });

        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Note note = noteListAdapter.getItem(i);
                String filePath;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // 0th item is internal storage. 1st item is sd card.
                    filePath = getExternalMediaDirs()[0].getAbsolutePath();
                } else {
                    filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                }

//                Log.d("Main Activity", filePath);

                if (note != null) {
                    downloadNote(note.getNoteUrl(), filePath);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }

    public static void downloadNote(String noteUrl, String filePath) {
        String noteFileName = noteUrl.substring(noteUrl.indexOf("/") + 1);

        String encodedFileName = noteFileName.replace(" ", "%20");
        String noteDownloadUrl = "https://agcs.com.bd/login/upload/" + encodedFileName;

        File file = new File(filePath, noteFileName);
//        Log.d("Main", file.getAbsolutePath());
        downloadedFilePath = file.getAbsolutePath();

        // Make Download Request
        DownloadManager.Request noteDownloadRequest = new DownloadManager.Request(Uri.parse(noteDownloadUrl))
                .setTitle("Downloading")
                .setDescription(noteFileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationUri(Uri.fromFile(file))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true);

        downloadID = downloadManager.enqueue(noteDownloadRequest);
    }
}
