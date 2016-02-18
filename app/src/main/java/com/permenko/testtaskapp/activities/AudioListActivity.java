package com.permenko.testtaskapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlayer;
import com.permenko.testtaskapp.eventbus.MediaPlayerStateEvent;
import com.permenko.testtaskapp.eventbus.PlaylistEvent;
import com.permenko.testtaskapp.models.Playlist;
import com.permenko.testtaskapp.services.StreamService;
import com.permenko.testtaskapp.views.adapters.AudioListAdapter;
import com.permenko.testtaskapp.R;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

public class AudioListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_list);

        if (getSupportActionBar() != null && getIntent().getBooleanExtra("show_menu_home", true)) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new AudioListFragment()).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class AudioListFragment extends Fragment {

        //private final String TAG = AudioListFragment.class.getSimpleName();

        EditText searchView;
        ImageButton clear;

        private Playlist playlist;
        private View rootView;
        private AudioListAdapter audioListAdapter;
        private ListView songsList;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            final VKRequest request = VKApi.audio().get();
            request.executeWithListener(vkRequestListener);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_audio_list, container, false);

            searchView = (EditText) rootView.findViewById(R.id.search_field);
            searchView.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3) {
                    audioListAdapter.filter(charSequence); //filter by charSequence in AudioListView
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            clear = (ImageButton) rootView.findViewById(R.id.clear);
            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchView.setText("");
                }
            });

            return rootView;
        }

        @Override
        public void onStart() {
            EventBus.getDefault().register(this);
            super.onStart();
        }

        @Override
        public void onResume() {
            super.onResume();
            try {
                audioListAdapter.notifyDataSetChanged();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStop() {
            EventBus.getDefault().unregister(this);
            super.onStop();
        }

        @Override
        public void onDestroy() {
            try {
                if (!StreamService.mediaPlayer.getPlayWhenReady()) {
                    //if music not playing stop service
                    rootView.getContext().stopService(new Intent(getContext(), StreamService.class));
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            super.onDestroy();
        }

        VKRequest.VKRequestListener vkRequestListener = new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                try {
                    playlist = new Playlist(response.json.getJSONObject("response").getJSONArray("items").toString()); // deserializing json array of songs
                    getContext().startService(new Intent(getContext(), StreamService.class));
                    EventBus.getDefault().postSticky(new PlaylistEvent(playlist)); //sending playlist to StreamService
                    fillAudioListView();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "JSON Exception", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(VKError error) {
                showError(error);
            }
        };

        private void showError(VKError error) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(error.toString())
                    .setPositiveButton("OK", null)
                    .show();
            if (error.httpError != null) {
                Log.w("VKError", "Error in request", error.httpError);
            }
        }
        
        private void fillAudioListView() {
            songsList = (ListView) rootView.findViewById(R.id.audio_list);
            songsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            audioListAdapter = new AudioListAdapter(getActivity(), playlist.getSongs());
            songsList.setAdapter(audioListAdapter);
        }

        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThread(MediaPlayerStateEvent event) {
            switch(event.getState()) {
                case ExoPlayer.STATE_BUFFERING:
                    //ignore
                    break;
                case ExoPlayer.STATE_ENDED:
                    audioListAdapter.notifyDataSetChanged();
                    break;
                case ExoPlayer.STATE_IDLE:
                    //ignore
                    break;
                case ExoPlayer.STATE_PREPARING:
                    //ignore
                    break;
                case ExoPlayer.STATE_READY:
                    //ignore
                    break;
                default:
                    //ignore
                    break;
            }
        }
    }
}
