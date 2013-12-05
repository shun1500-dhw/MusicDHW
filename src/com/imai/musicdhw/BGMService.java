package com.imai.musicdhw;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.util.Log;

public class BGMService extends Service implements OnCompletionListener {
	private MediaPlayer player;
	//今実行中の曲のID。初期値は0であること
	private int currentMusicId = 0;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent.getAction().equals("start")) {
			//intentで選択されている曲のidを受け取る。入ってなかったらnc20349(ありえないけど)
			int id = intent.getIntExtra("id", R.raw.nc20349);

			//今実行中の曲のidと次に実行したい曲のidを比較
			if (id != currentMusicId) {
				//違う曲の場合

				//MediaPlayerを準備済みか判定
				if (player != null) {
					//MediaPlayerを準備済みならリリース
					player.release();
					player = null;
				}

				//MediaPlayerを準備
				player = MediaPlayer.create(this, id);
				currentMusicId = id;
			}

			//MediaPlayerを再生
			//同じ曲なら続きから再生
			player.start();
			player.setOnCompletionListener(this);

		} else if (intent.getAction().equals("pause") && player != null) {

			//一時停止
			player.pause();

		} else if (intent.getAction().equals("stop") && player != null) {

			//停止
			player.release();
			player = null;
			currentMusicId = 0;

		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO 自動生成されたメソッド・スタブ
		super.onDestroy();
		player.release();
		player = null;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO 自動生成されたメソッド・スタブ
//		Log.i("debug", "曲が終わった");

		Intent intent = new Intent();
		intent.setAction("NEXT_MUSIC");
		sendBroadcast(intent);
	}

}
