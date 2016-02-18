package com.permenko.testtaskapp.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.permenko.testtaskapp.R;
import com.permenko.testtaskapp.activities.AudioPagerActivity;
import com.permenko.testtaskapp.eventbus.MediaPlayerActionEvent;
import com.permenko.testtaskapp.eventbus.SearchEvent;
import com.permenko.testtaskapp.models.Playlist;
import com.permenko.testtaskapp.models.Song;
import com.permenko.testtaskapp.eventbus.MediaPlayerAction;
import com.permenko.testtaskapp.services.StreamService;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Locale;

public class AudioListAdapter extends BaseAdapter {

    private Context context;
    private View view;
    ImageButton playPauseButton;
    private ArrayList<Song> songs;
    private ArrayList<Song> mainSongsList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    public AudioListAdapter(Context context, ArrayList<Song> songs) {
        this.context = context;
        this.songs = songs;
        this.mainSongsList.addAll(songs);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.audio_list_item, parent, false);

        sharedPreferences = view.getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyDataSetChanged();
                context.startActivity(new Intent(context, AudioPagerActivity.class).putExtra("position", position));
            }
        });

        ((TextView) view.findViewById(R.id.artist)).setText(songs.get(position).getArtist());
        ((TextView) view.findViewById(R.id.title)).setText(songs.get(position).getTitle());

        playPauseButton = (ImageButton) view.findViewById(R.id.play_pause_button);

        try {
            if (sharedPreferences.getInt("playing_track_id", -1) == songs.get(position).getId()) {

                if (StreamService.mediaPlayer.getPlayWhenReady()) {
                    playPauseButton.setImageResource(R.drawable.pause_listview);
                    view.setBackgroundColor(view.getResources().getColor(R.color.audio_list_item_selected));
                } else {
                    playPauseButton.setImageResource(R.drawable.play_listview);
                    view.setBackgroundColor(view.getResources().getColor(android.R.color.transparent));
                }
            } else {
                playPauseButton.setImageResource(R.drawable.play_listview);
                view.setBackgroundColor(view.getResources().getColor(android.R.color.transparent));
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            //too bad(( possible catch when searching
        }

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyDataSetChanged();
                try {
                    if (sharedPreferences.getInt("playing_track_id", -1) == songs.get(position).getId() && // is clicked on right song
                            StreamService.mediaPlayer.getPlayWhenReady()) { // is song playing
                        playPauseButton.setImageResource(R.drawable.play_listview);
                        EventBus.getDefault().post(new MediaPlayerActionEvent(MediaPlayerAction.STOP));
                    } else {
                        sharedPreferencesEditor = sharedPreferences.edit();
                        sharedPreferencesEditor.putInt("playing_track_id", songs.get(position).getId()).apply();
                        playPauseButton.setImageResource(R.drawable.pause_listview);
                        EventBus.getDefault().post(new MediaPlayerActionEvent(MediaPlayerAction.PLAY, songs.get(position).getUrl()));
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }

    public void filter(CharSequence charText) {
        charText = charText.toString().toLowerCase(Locale.getDefault());
        songs.clear();

        if (charText.length() == 0) {
            songs.addAll(mainSongsList);
        } else {
            for (Song song : mainSongsList) {
                if (song.getArtist().toLowerCase(Locale.getDefault()).contains(charText) || song.getTitle().toLowerCase(Locale.getDefault()).contains(charText)) {
                    songs.add(song);
                }
            }
        }

        Playlist playlist = new Playlist();
        playlist.setCount(songs.size());
        playlist.setSongs(songs);
        EventBus.getDefault().post(new SearchEvent(playlist));

        notifyDataSetChanged();
    }
}
