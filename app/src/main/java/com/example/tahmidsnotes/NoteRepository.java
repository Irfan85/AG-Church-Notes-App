package com.example.tahmidsnotes;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NoteRepository {
    private final String LOG_TAG = NoteRepository.class.getSimpleName();

    public static final String BASE_URL = "https://agcs.com.bd/login/";
    private final String USERNAME = "<AG Church Student ID>";
    private final String PASSWORD = "<Password>";

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    public void fetchNotes(final UiUpdateCallback callback) {

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                String htmlResponse = null;

                try {
                    OkHttpClient httpClient = new OkHttpClient.Builder()
                            .cookieJar(new MyCookieJar())
                            .build();

                    FormBody loginFormBody = new FormBody.Builder()
                            .add("username", USERNAME)
                            .add("password", PASSWORD)
                            .build();

                    Request request = new Request.Builder()
                            .url(BASE_URL)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .post(loginFormBody)
                            .build();

                    Response response = httpClient.newCall(request).execute();

                    if (response.isSuccessful()) {
                        htmlResponse = response.body().string();
                    } else {
                        Log.e(LOG_TAG, "Unexpected response code: " + response.code());
                    }

                } catch (IOException e) {
                    Log.e(LOG_TAG, "IO Exception Occurred", e);
                }

                List<Note> notes;
                if (htmlResponse != null) {
                    notes= extractNotesFromHtml(htmlResponse);
                    callback.onComplete(notes);
                }else{
                    notes = new ArrayList<>();
                    callback.onComplete(notes);
                }
            }
        });
    }

    private List<Note> extractNotesFromHtml(String html) {
        List<Note> notes = new ArrayList<>();

        if (html != null) {
            Document htmlDocument = Jsoup.parse(html);
            Element notesTableRoot = htmlDocument.getElementById("example1");
            Elements noteTables = notesTableRoot.children();
            for (int i = 1; i < noteTables.size(); i++) {
                Elements noteColumns = noteTables.get(i).getElementsByTag("tr").get(0).children();

                String issueDate = noteColumns.get(1).text();
                String noteDescription = noteColumns.get(2).text();
                String noteUrlExtension = noteColumns.get(5).children().get(1).attr("href");

                notes.add(new Note(noteDescription, issueDate, noteUrlExtension));
            }
        }

        return notes;
    }


    // Cookie jar for storing cookies. It is required in some websites for authentication
    private static class MyCookieJar implements CookieJar {

        private List<Cookie> cookies;

        @NotNull
        @Override
        public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
            if (cookies == null) {
                return new ArrayList<>();
            } else {
                return cookies;
            }
        }

        @Override
        public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> cookies) {
            this.cookies = cookies;
        }
    }

    // Callback for telling the main thread to update the UI with new data
    public interface UiUpdateCallback {
        void onComplete(List<Note> notes);
    }
}
