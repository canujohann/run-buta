package com.canujohann.scenes;

import java.io.IOException;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.modifier.DelayModifier;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.util.modifier.ease.EaseBackInOut;

import android.content.Intent;
import android.view.KeyEvent;

import com.canujohann.activities.MultiSceneActivity;
import com.canujohann.utils.Consts;
import com.canujohann.utils.ResourceUtil;
import com.swarmconnect.Swarm;
import com.swarmconnect.SwarmLeaderboard;

/**
 * 
 * @author johann
 *
 */
public class InitialScene extends KeyListenScene implements
		ButtonSprite.OnClickListener , ITimerCallback {

	
	//button tags
	private static final int INITIAL_START = 1;
	private static final int INITIAL_RANKING = 2;
	private static final int DISTANCE_BETWEEN_TWO = 100;
	

	// ボタンが押された時の効果音
	private Sound btnPressedSound;
	
	private AnimatedSprite cochon ;
	private AnimatedSprite cuisinier ;

	public InitialScene(MultiSceneActivity context) {
		super(context);
		init();
	}

	
	@Override
	public void init() {
		
		//背景画像を設定
		Sprite bg = getBaseActivity().getResourceUtil().getSprite("initial_bg.png");
		bg.setPosition(0, 0);
		attachChild(bg);

		//タイトル設定
		Sprite titleSprite = getBaseActivity().getResourceUtil().getSprite(	"initial_title.png");
		placeToCenterX(titleSprite, 10);
		float titleX = titleSprite.getX();
		attachChild(titleSprite);

		/*
		 * タイトルのアニメーション
		 * 画面外から、中に入る
		 */
		titleSprite.setPosition(titleSprite.getX()
				+ getBaseActivity().getEngine().getCamera().getWidth(),
				titleSprite.getY() - 200);

		titleSprite.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(0.5f), new MoveModifier(1.0f, titleSprite
						.getX(), titleX, titleSprite.getY(), 10, EaseBackInOut
						.getInstance())));
		
		

		// STARTボタン
		ButtonSprite btnStart = getBaseActivity().getResourceUtil().getButtonSprite("button1.png", "button1_p.png");
		placeToCenterX(btnStart, 200);
		btnStart.setTag(INITIAL_START);
		btnStart.setOnClickListener(this);
		attachChild(btnStart);
		registerTouchArea(btnStart);

		//STARTボタンのアニメーション
		float btnX = btnStart.getX();
		btnStart.setPosition(btnStart.getX()
				+ getBaseActivity().getEngine().getCamera().getWidth(),
				btnStart.getY());
		btnStart.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(1.4f), new MoveModifier(1.0f,
						btnStart.getX(), btnX, btnStart.getY(),
						btnStart.getY(), EaseBackInOut.getInstance())));

		
		//RANKボタン
		ButtonSprite btnRanking = getBaseActivity().getResourceUtil()
				.getButtonSprite("button2.png", "button2_p.png");
		placeToCenterX(btnRanking, 280);
		btnRanking.setTag(INITIAL_RANKING);
		btnRanking.setOnClickListener(this);
		attachChild(btnRanking);
		registerTouchArea(btnRanking);

		//RANKボタンのアニメーション
		btnX = btnRanking.getX();
		btnRanking.setPosition(btnRanking.getX()
				+ getBaseActivity().getEngine().getCamera().getWidth(),
				btnRanking.getY());
		btnRanking.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(1.6f), new MoveModifier(1.0f, btnRanking
						.getX(), btnX, btnRanking.getY(), btnRanking.getY(),
						EaseBackInOut.getInstance())));
		
		/*
		 * 豚を追いかけるやつの処理
		 */
		
		float cameraY = getBaseActivity().getEngine().getCamera().getHeight();	
		float cameraX = getBaseActivity().getEngine().getCamera().getWidth();

		cochon = getBaseActivity().getResourceUtil().getAnimatedSprite("buta2.png", 1, 2);
		cochon.setPosition( cameraX , cameraY - cochon.getHeight() - 30f) ;
		cochon.setZIndex(3);
		attachChild(cochon);

		cuisinier = getBaseActivity().getResourceUtil().getAnimatedSprite("boucher.png", 1, 2);
		cuisinier.setPosition(cameraX + cuisinier.getWidth() + DISTANCE_BETWEEN_TWO , cochon.getY());
		cuisinier.setZIndex(3);
		attachChild(cuisinier);

		//update after 3 seconds
		registerUpdateHandler(new TimerHandler(5f, false, (ITimerCallback) this));
		
	}

	
	@Override
	public void prepareSoundAndMusic() {
		
		// 効果音をロード
		try {
			btnPressedSound = SoundFactory.createSoundFromAsset(
					getBaseActivity().getSoundManager(), getBaseActivity(),
					"door03.wav");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		return false;
	}

	/*
	 * ボタンのクリック処理 
	 */
	public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
			float pTouchAreaLocalY) {
		
		// 効果音を再生
		btnPressedSound.play();
		
		switch (pButtonSprite.getTag()) {
		
		case INITIAL_START:
			
			// リソースの解放
			ResourceUtil.getInstance(getBaseActivity()).resetAllTexture();
			
			//Sceneを取得し、移動
			KeyListenScene scene = new MainScene(getBaseActivity());
			getBaseActivity().getEngine().setScene(scene);
			
			// 遷移管理用配列に追加
			getBaseActivity().appendScene(scene);
			break;
			
		
		case INITIAL_RANKING:		//ランキングを表示			
			//SwarmLeaderboard.submitScore(14032, 200);
			Swarm.enableAlternativeMarketCompatability();
			Swarm.init(getBaseActivity(), Consts.SWARM_APP_ID, Consts.SWARM_APP_KEY);	
			SwarmLeaderboard.showLeaderboard(Consts.LEADERBOARD_ID);
			break;				
		}
	}
	
	
	/**
	* method pour les timercallback
	*/
	@Override
	public void onTimePassed(TimerHandler pTimerHandler) {	
		
		MoveXModifier moveUpModifier  = new MoveXModifier(5, cochon.getX(), - cochon.getWidth() - DISTANCE_BETWEEN_TWO);	
		MoveXModifier moveUpModifier2 = new MoveXModifier(5, cuisinier.getX(), -cuisinier.getWidth());
		
		cochon.animate(200);
		cuisinier.animate(200);
		
		cochon.registerEntityModifier(moveUpModifier);
		cuisinier.registerEntityModifier(moveUpModifier2);
	}

}
