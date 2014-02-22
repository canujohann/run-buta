package com.canujohann.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtil {
	
	// 自身のインスタンス
	private static SPUtil instance;

	// シングルトン
	public static synchronized SPUtil getInstance(Context context) {
		if (instance == null) {
			instance = new SPUtil(context);
		}
		return instance;
	}

	private static SharedPreferences settings;
	private static SharedPreferences.Editor editor;

	private SPUtil(Context context) {
		settings = context.getSharedPreferences("shared_preference_1.0", 0);
		editor = settings.edit();
	}

	public long getHighScore() {
		return settings.getLong("highScore", 0l);
	}

	public void setHighScore(long value) {
		editor.putLong("highScore", value);
		editor.commit();
	}
}
