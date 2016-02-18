package com.permenko.testtaskapp.views.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.permenko.testtaskapp.R;
import com.permenko.testtaskapp.eventbus.MediaPlayerActionEvent;
import com.permenko.testtaskapp.eventbus.SlideAudioPagerEvent;
import com.permenko.testtaskapp.models.Song;
import com.permenko.testtaskapp.eventbus.MediaPlayerAction;
import com.permenko.testtaskapp.services.StreamService;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AudioPagerAdapter extends PagerAdapter {

    private ArrayList<Song> songs;
    private LayoutInflater inflater;
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    private View rootView;

    @Bind(R.id.play_pause) ImageButton playPause;
    @Bind(R.id.next) ImageButton next;
    @Bind(R.id.previous) ImageButton prev;
    @Bind(R.id.artist) TextView artist;
    @Bind(R.id.title) TextView title;

    public AudioPagerAdapter(Context context, ArrayList<Song> songs) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.songs = songs;
        this.context = context;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object instantiateItem(final ViewGroup view, final int position) {
        rootView = inflater.inflate(R.layout.audio_pager_item, view, false);
        ButterKnife.bind(this, rootView);
        artist.setText(StreamService.playlist.getSongs().get(position).getArtist());
        title.setText(StreamService.playlist.getSongs().get(position).getTitle());

        sharedPreferences = view.getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        try {
            if (sharedPreferences.getInt("playing_track_id", -1) == songs.get(position).getId()) {
                if (StreamService.mediaPlayer.getPlayWhenReady()) {
                    playPause.setImageResource(R.drawable.pause);
                } else {
                    playPause.setImageResource(R.drawable.play);
                }
            } else {
                playPause.setImageResource(R.drawable.play);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            //too bad(( possible catch when searching
        }

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (sharedPreferences.getInt("playing_track_id", -1) == songs.get(position).getId() && // is clicked on right song
                            StreamService.mediaPlayer.getPlayWhenReady()) { // is song playing
                        playPause.setImageResource(R.drawable.play);
                        EventBus.getDefault().post(new MediaPlayerActionEvent(MediaPlayerAction.STOP));
                    } else {
                        sharedPreferencesEditor = sharedPreferences.edit();
                        sharedPreferencesEditor.putInt("playing_track_id", songs.get(position).getId()).apply();
                        playPause.setImageResource(R.drawable.pause);
                        EventBus.getDefault().post(new MediaPlayerActionEvent(MediaPlayerAction.PLAY, songs.get(position).getUrl()));
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                notifyDataSetChanged();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new SlideAudioPagerEvent(position, SlideAudioPagerEvent.TO_NEXT_PAGE));
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new SlideAudioPagerEvent(position, SlideAudioPagerEvent.TO_PREVIOUS_PAGE));
            }
        });

        view.addView(rootView);

        return rootView;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        ((ViewPager) container).removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
}
