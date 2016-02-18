package com.permenko.testtaskapp.activities;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer.ExoPlayer;
import com.permenko.testtaskapp.R;
import com.permenko.testtaskapp.eventbus.MediaPlayerStateEvent;
import com.permenko.testtaskapp.eventbus.SlideAudioPagerEvent;
import com.permenko.testtaskapp.services.StreamService;
import com.permenko.testtaskapp.views.adapters.AudioPagerAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AudioPagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_pager);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new AudioPagerFragment()).commit();
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

    public static class AudioPagerFragment extends Fragment {

        //private final String TAG = AudioPagerFragment.class.getSimpleName();

        private View rootView;
        private ViewPager viewPager;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_audio_pager, container, false);

            viewPager = (ViewPager) rootView.findViewById(R.id.pager);

            viewPager.setAdapter(new AudioPagerAdapter(rootView.getContext(), StreamService.playlist.getSongs()));

            viewPager.setCurrentItem(getActivity().getIntent().getIntExtra("position", 0));

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
                viewPager.getAdapter().notifyDataSetChanged();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStop() {
            EventBus.getDefault().unregister(this);
            super.onStop();
        }

        @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
        public void onEventMainThread(MediaPlayerStateEvent event) {
            switch (event.getState()) {
                case ExoPlayer.STATE_BUFFERING:
                    //ignore
                    break;
                case ExoPlayer.STATE_ENDED:
                    viewPager.getAdapter().notifyDataSetChanged(); //updating viewpager view
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

        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThread(SlideAudioPagerEvent event) {
            try {
                viewPager.setCurrentItem(event.getPosition() + event.getDirection());
                viewPager.getAdapter().notifyDataSetChanged();
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }
}
