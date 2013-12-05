package com.imai.musicdhw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnItemClickListener, OnClickListener {
	private HashMap<String, Integer> map;
	private List<String> sequentialList;
	private List<String> shuffleList;

	private static final String MUSIC_TITLE[] = {
			"オルゴールBGM", "捨てられた雪原", "ループ用BGM008",
			"ループ用BGM026", "春の陽気", "亡き王女の為のセプテット",
			"おてんば恋娘", "お茶の時間", "Starting Japan"
	};

	private static final int MUSIC_ID[] = {
			R.raw.nc2422, R.raw.nc7400, R.raw.nc10100,
			R.raw.nc10812, R.raw.nc11577, R.raw.nc13447,
			R.raw.nc20349, R.raw.nc20612, R.raw.nc29204
	};

	//今選んでいる音楽。初期値は先頭の曲だよ
	private int currentMusicId = R.raw.nc2422;

	private int musicIndex = 0;
	private boolean shuffleMode = false;

	private ListView musicListView;
	private ImageView shuffleImage;
	private TextView tiltlText;
	private TextView modeText;
	private ToggleButton operationButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		musicListView = (ListView) findViewById(R.id.musicListView);

		tiltlText = (TextView) findViewById(R.id.tiltlText);
		modeText = (TextView) findViewById(R.id.modeText);
		shuffleImage = (ImageView) findViewById(R.id.shuffleImage);

		ImageView previousImage = (ImageView) findViewById(R.id.previousImage);
		ImageView stopImage = (ImageView) findViewById(R.id.stopImage);
		operationButton = (ToggleButton) findViewById(R.id.operationButton);
		ImageView nextImage = (ImageView) findViewById(R.id.nextImage);

		sequentialList = new ArrayList<String>();
		map = new HashMap<String, Integer>();
		for (int i = 0; i < MUSIC_TITLE.length; i++) {
			//sequentialListは単純に配列順
			sequentialList.add(MUSIC_TITLE[i]);
			//タイトル名からidを引き出し用のmap
			map.put(MUSIC_TITLE[i], MUSIC_ID[i]);
		}

		//shuffleListはsequentialListをコピーしておく
		//シャッフルはシャッフルボタンが押されるたびに
		shuffleList = new ArrayList<String>(sequentialList);

		ArrayAdapter<String> adapter =
				new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, MUSIC_TITLE);

		musicListView.setAdapter(adapter);
		musicListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		//ラジオボタンの先頭をチェックしておきましょう
		musicListView.setItemChecked(0, true);
		musicListView.setOnItemClickListener(this);

		//再生と一時停止のトグルボタンの処理
		operationButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO 自動生成されたメソッド・スタブ

				if (isChecked == true) {
					//こっちが一時停止になる(一時停止(false)を押して再生(true)になった。変化したあとの値でのチェックになる)
					Intent intent =
							new Intent(MainActivity.this, BGMService.class);
					intent.setAction("pause");
					startService(intent);
				}
				else {
					//こっちが再生になる(再生(true)を押して一時停止(false)になった)

					startMusic();
				}

			}
		});
		operationButton.setChecked(true);

		shuffleImage.setOnClickListener(this);
		previousImage.setOnClickListener(this);
		stopImage.setOnClickListener(this);
		nextImage.setOnClickListener(this);

		//BroadcastReceiverの設定
		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO 自動生成されたメソッド・スタブ

				//serviceからintentを受けとった時の処理
				musicIndex++;
				if (musicIndex >= MUSIC_TITLE.length) {
					musicIndex = 0;
				}
//				Log.i("debug", "次の曲のindex" + musicIndex);

				startMusic();
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction("NEXT_MUSIC");
		registerReceiver(receiver, filter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO 自動生成されたメソッド・スタブ

		//押された場所のviewから曲名を取り出し
//		Log.i("debug", textView.getText() + "");

		if (shuffleMode) {
			//シャッフルモードの場合
			TextView textView = (TextView) view;
			musicIndex = shuffleList.indexOf(textView.getText());
		} else {
			//シーケンシャルモードの場合
			musicIndex = position;
		}
		startMusic();
	}

	@Override
	public void onClick(View v) {
		// TODO 自動生成されたメソッド・スタブ
		switch (v.getId()) {
		case R.id.shuffleImage:
			if (shuffleMode) {
				//シャッフルモードならば
				shuffleMode = false;
				//ボタンの画像をシャッフルモード(shuffle)にする(トグルになっている)
				shuffleImage.setImageResource(R.drawable.shuffle);

				modeText.setText("連続再生中");

			} else {
				//シーケンシャルモード(デフォ)ならば
				shuffleMode = true;
				//シャッフル用リストをシャッフル。押されるたびシャッフルするよ
				Collections.shuffle(shuffleList);

				//ボタンの画像をシーケンシャルモード(repeat)にする(トグルになっている)
				shuffleImage.setImageResource(R.drawable.repeat);

				modeText.setText("シャッフル再生中");
			}

			break;

		case R.id.previousImage:
			if (musicIndex != 0) {
				musicIndex--;
			} else {
				musicIndex = MUSIC_TITLE.length - 1;
			}
			startMusic();

			break;

		case R.id.stopImage:
			Intent intent = new Intent(MainActivity.this, BGMService.class);
			intent.setAction("stop");
			startService(intent);

			//停止したときはトグルボタンは再生表示にする
			operationButton.setChecked(true);

			break;

		case R.id.nextImage:
			musicIndex++;
			if (musicIndex >= MUSIC_TITLE.length) {
				musicIndex = 0;
			}
			startMusic();

			break;

		default:
			break;
		}
	}

	//音楽再生メソッド
	void startMusic() {
		String musicTitleTmp;
		int radioButtonPlace;
		if (shuffleMode) {
			//シャッフルモードの場合
			//shuffleListから曲名を抽出
			musicTitleTmp = shuffleList.get(musicIndex);
			//歪な処理だがsequentialListから表示リスト順の曲名の場所を抽出
			radioButtonPlace = sequentialList.indexOf(musicTitleTmp);
		} else {
			//シーケンシャルモードの場合
			//sequentialListから曲名を抽出
			musicTitleTmp = sequentialList.get(musicIndex);
			//シーケンシャルモードの場合はmusicIndexがそのまま表示リストの場所
			radioButtonPlace = musicIndex;
		}
		//曲名をキーにしてIDを取り出す
		currentMusicId = map.get(musicTitleTmp);
		//曲名をテキストビューにセット
		tiltlText.setText(musicTitleTmp);

		//ラジオボタン移動
		musicListView.setItemChecked(radioButtonPlace, true);
		musicListView.setSelection(radioButtonPlace);

		//再生するときはトグルボタンは一時停止表示にする
		operationButton.setChecked(false);

		Intent intent =
				new Intent(MainActivity.this, BGMService.class);
		intent.setAction("start");
		intent.putExtra("id", currentMusicId);
		startService(intent);
	}

}
