package mercubuana.android.tugas;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AudioListener {
    private static final int PERMISSION = 0;

    private RecyclerView recyclerView;
    private PlayerView playerView;
    private AppCompatTextView txvPlayerName;
    private LinearLayoutCompat layoutPlayer;

    private LinearLayoutManager layoutManager;
    private AudioAdapter adapter;
    private List<AudioModel> audioModelEntities;
    private AudioModel audioSelected;

    private SimpleExoPlayer player;

    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    private PlaybackStateListener playbackStateListener;

    {
        audioModelEntities = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playerView = findViewById(R.id.video_player);
        txvPlayerName = findViewById(R.id.video_name);
        layoutPlayer = findViewById(R.id.video_view);
        recyclerView = findViewById(R.id.list_item);

        layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);

        playbackStateListener = new PlaybackStateListener();

        initializePermission();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24 && audioSelected != null) {
            initializePlayer(audioSelected);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT < 24 || player == null) && audioSelected != null) {
            initializePlayer(audioSelected);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getAllAudioFromDevice(getApplicationContext());
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION);
                }
                return;
            }
        }
    }

    @Override
    public void onAudioClick(AudioModel audioModel) {
        audioSelected = audioModel;
        initializePlayer(audioModel);
    }

    private void initializePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION);
        } else {
            getAllAudioFromDevice(getApplicationContext());
        }
    }

    private void initializePlayer(AudioModel audioModel) {
        if (player == null) {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd());
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
            player.addListener(playbackStateListener);
        }

        playerView.setPlayer(player);

        Uri uri = Uri.fromFile(new File(audioModel.getPath()));
        MediaSource mediaSource = buildMediaSource(uri);

        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.prepare(mediaSource, false, false);

        txvPlayerName.setText(audioModel.getName());
        layoutPlayer.setVisibility(View.VISIBLE);
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, "mercubuana-tugas");
        ProgressiveMediaSource.Factory mediaSourceFactory =
                new ProgressiveMediaSource.Factory(dataSourceFactory);

        MediaSource mediaSource1 = mediaSourceFactory.createMediaSource(uri);

        MediaSource mediaSource2 = mediaSourceFactory.createMediaSource(uri);

        return new ConcatenatingMediaSource(mediaSource1, mediaSource2);
    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();

            player.removeListener(playbackStateListener);
            player.release();
            player = null;

            layoutPlayer.setVisibility(View.GONE);
        }
    }

    public void getAllAudioFromDevice(final Context context) {
        Uri uri = MediaStore.Files.getContentUri("external");
        Cursor c = context.getContentResolver().query(uri,
                null,
                MediaStore.Files.FileColumns.MIME_TYPE + "=?",
                new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3")},
                null);

        if (c != null) {
            while (c.moveToNext()) {

                AudioModel audioModel = new AudioModel();
                String path = c.getString(1);
                String name = c.getString(8);

                audioModel.setPath(path);
                audioModel.setName(name);

                audioModelEntities.add(audioModel);
            }
            c.close();
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(this.layoutManager);
        adapter = new AudioAdapter(audioModelEntities, this);
        recyclerView.setAdapter(adapter);
    }
}
