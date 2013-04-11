package com.example.media.timedtexttest;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnTimedTextListener;
import android.media.MediaPlayer.TrackInfo;
import android.media.TimedText;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity implements OnTimedTextListener {
	private static final String TAG = "TimedTextTest";
	private TextView txtDisplay;
	private static Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		txtDisplay = (TextView) findViewById(R.id.txtDisplay);
		MediaPlayer player = MediaPlayer.create(this, R.raw.video);
		try {
			player.addTimedTextSource(getSubtitleFile(R.raw.sub),
					MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP);
			int textTrackIndex = findTrackIndexFor(
					TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT, player.getTrackInfo());
			if (textTrackIndex >= 0) {
				player.selectTrack(textTrackIndex);
			} else {
				Log.w(TAG, "Cannot find text track!");
			}
			player.setOnTimedTextListener(this);
			player.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int findTrackIndexFor(int mediaTrackType, TrackInfo[] trackInfo) {
		int index = -1;
		for (int i = 0; i < trackInfo.length; i++) {
			if (trackInfo[i].getTrackType() == mediaTrackType) {
				return i;
			}
		}
		return index;
	}

	private String getSubtitleFile(int resId) {
		String fileName = getResources().getResourceEntryName(resId);
		File subtitleFile = getFileStreamPath(fileName);
		if (subtitleFile.exists()) {
			Log.d(TAG, "Subtitle already exists");
			return subtitleFile.getAbsolutePath();
		}
		Log.d(TAG, "Subtitle does not exists, copy it from res/raw");

		// Copy the file from the res/raw folder to your app folder on the
		// device
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = getResources().openRawResource(resId);
			outputStream = new FileOutputStream(subtitleFile, false);
			copyFile(inputStream, outputStream);
			return subtitleFile.getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeStreams(inputStream, outputStream);
		}
		return "";
	}

	private void copyFile(InputStream inputStream, OutputStream outputStream)
			throws IOException {
		final int BUFFER_SIZE = 1024;
		byte[] buffer = new byte[BUFFER_SIZE];
		int length = -1;
		while ((length = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, length);
		}
	}

	// A handy method I use to close all the streams
	private void closeStreams(Closeable... closeables) {
		if (closeables != null) {
			for (Closeable stream : closeables) {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void onTimedText(final MediaPlayer mp, final TimedText text) {
		if (text != null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					int seconds = mp.getCurrentPosition() / 1000;

					txtDisplay.setText("[" + secondsToDuration(seconds) + "] "
							+ text.getText());
				}
			});
		}
	}

	// To display the seconds in the duration format 00:00:00
	public String secondsToDuration(int seconds) {
		return String.format("%02d:%02d:%02d", seconds / 3600,
				(seconds % 3600) / 60, (seconds % 60), Locale.US);
	}
}
