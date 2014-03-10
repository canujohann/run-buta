package com.canujohann.scenes;

import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.DelayModifier;
import org.andengine.entity.modifier.FadeInModifier;
import org.andengine.entity.modifier.FadeOutModifier;
import org.andengine.entity.modifier.IEntityModifier.IEntityModifierMatcher;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.modifier.ParallelEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.font.BitmapFont;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.ease.EaseBackIn;
import org.andengine.util.modifier.ease.EaseQuadOut;

import com.canujohann.activities.MultiSceneActivity;
import com.canujohann.utils.Consts;
import com.canujohann.utils.ResourceUtil;
import com.canujohann.utils.SPUtil;
import com.swarmconnect.Swarm;
import com.swarmconnect.SwarmLeaderboard;

import android.content.Intent;
import android.graphics.Color;
import android.view.KeyEvent;

public class MainScene extends KeyListenScene implements
		ButtonSprite.OnClickListener {

	// ゾンビ用タグ
	private static final int TAG_ZOMBIE_01 = 1;
	private static final int TAG_ZOMBIE_02 = 2;
	private static final int TAG_ZOMBIE_FIX_01 = -1;
	private static final int TAG_ZOMBIE_FIX_02 = -2;
	
	private static final int WALL_NB = 2;



	// アイテム用タグ
	private static final int TAG_ITEM_01 = 11;
	private static final int TAG_ITEM_02 = 12;

	// ボタン用タグ
	private static final int MENU_MENU = 21;
	private static final int MENU_RANKING = 23;
	private static final int MENU_RETRY = 24;
	private static final int MENU_RESUME = 25;

	// 主人公
	private AnimatedSprite boySprite, boyFirst;
	// 主人公のz-index
	private int zIndexBoy = 2;
	// 攻撃アイテムのz-index
	private int zIndexItem = 1;
	// ゲームオーバー画面のz-index
	private int zIndexGameOverLayer = 3;
	// 主人公が移動する先の座標を保持
	private float boyNewX;
	private float boyNewY;

	// ゾンビ出現穴の配列
	//private ArrayList<Sprite> holeArray;
	// 出現済みゾンビの配列
	private ArrayList<AnimatedSprite> zombieArray;
	private AnimatedSprite zombieFirst ;
	// 攻撃アイテムの配列
	private ArrayList<AnimatedSprite> itemArray;
	//wall配列
	private ArrayList<Sprite> wallArray;
	// 攻撃エフェクトのTextureRegionの配列
	private TextureRegion[] weaponTextureArray;
	// コンボ表示用フォント
	private BitmapFont[] comboBMFontArray;

	// ゲームオーバーか否かのフラグ
	private boolean isGameOver;
	// ポーズ中か否かのフラグ
	private boolean isPaused;
	// ポーズ画面の背景
	private Rectangle pauseBg;

	// 現在のスコアを表示するテキスト
	private Text currentScoreText;
	// 過去最高のスコアを表示するテキスト
	private Text highScoreText;
	// 現在のスコア
	private long currentScore;

	// 遊び方画面
	private Sprite instructionSprite;
	// 遊び方画面のボタン
	private ButtonSprite instructionBtn;
	// 遊び方画面が出ているかどうか
	private boolean isHelpVisible;
	
	boolean hasContactWithWall;
	float[] previousPosition = new float[2];

	// 効果音
	private Sound btnPressedSound;
	private Sound weapon01Sound;
	private Sound weapon02Sound;
	private Sound weapon03Sound;
	private Sound zombieAppearSound;
	private Sound gameoverSound;
	private Sound comboSound;
	
	protected Font mFont ;

	public MainScene(MultiSceneActivity baseActivity) {
		super(baseActivity);
		init();
	}

	public void init() {
		

		wallArray = new ArrayList<Sprite>();
		
		attachChild(getBaseActivity().getResourceUtil().getSprite("herbe.png"));

		//pig first
		boySprite = getBaseActivity().getResourceUtil().getAnimatedSprite("buta2.png", 1, 2);
		
		boyFirst = getBaseActivity().getResourceUtil().getAnimatedSprite("buta2.png", 1, 2);
		boyFirst.setVisible(false);
		
		placeToCenter(boySprite);
		attachChild(boySprite);
		boySprite.animate(200);
		boySprite.setZIndex(zIndexBoy);
		
		
		zombieFirst = getBaseActivity().getResourceUtil().getAnimatedSprite("boucher2.png", 1, 2);
		zombieFirst.setVisible(false);
		
		// スコア表示
		currentScore = 0;
				
		final ITexture fontTexture = new BitmapTextureAtlas(getBaseActivity().getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		mFont = FontFactory.createFromAsset(getBaseActivity().getFontManager(), fontTexture, getBaseActivity().getAssets(),"font/chicken.ttf" , 48f, true, Color.WHITE);
		mFont.load();

		//スコアの表示
		currentScoreText = new Text(20, 20, mFont,
				"Score " + currentScore, 20, new TextOptions(
						HorizontalAlign.LEFT), getBaseActivity()
						.getVertexBufferObjectManager());
		attachChild(currentScoreText);
		
		//HIGH SCOREの表示
		highScoreText = new Text(20, 60, mFont, "HighScore "
				+ SPUtil.getInstance(getBaseActivity()).getHighScore(), 20,
				new TextOptions(HorizontalAlign.LEFT), getBaseActivity()
						.getVertexBufferObjectManager());
		attachChild(highScoreText);

		// 配列の初期化
		zombieArray = new ArrayList<AnimatedSprite>();
		itemArray = new ArrayList<AnimatedSprite>();

		
		/*
		 * 処理が重くならないように
		 * 攻撃エフェクトのSpriteを初期化
		 */
		weaponTextureArray = new TextureRegion[5];
		for (int i = 0; i < 3; i++) {
			weaponTextureArray[i] = (TextureRegion) getBaseActivity().getResourceUtil().getSprite("weapon_0" + (i + 1) + ".png").getTextureRegion();
		}
		
		
		// コンボ表示用フォントを初期化
		comboBMFontArray = new BitmapFont[3];
		for (int i = 0; i < 3; i++) {
			comboBMFontArray[i] = new BitmapFont(getBaseActivity()
					.getTextureManager(), getBaseActivity().getAssets(),
					"font/combo_0" + (i + 1) + ".fnt");
		}

		
		/*
		 * ハイスコアが500以下の時のみヘルプ画面を出す
		 */
		if (SPUtil.getInstance(getBaseActivity()).getHighScore() > 500) {
			showNewZombie();	// 適を出現させる
			showNewWeapon();	// 攻撃アイテムを出現させる
			showWalls(WALL_NB);
			
			isHelpVisible = false;
		} else {
			isHelpVisible = true;
			showHelp();
		}
	}

	
	/*
	 * 遊び方画面を出現させる
	 */
	public void showHelp() {
		
		//main image for help
		instructionSprite = ResourceUtil.getInstance(getBaseActivity()).getSprite("instruction.png");
		placeToCenter(instructionSprite);
		attachChild(instructionSprite);

		// help button to click
		instructionBtn = ResourceUtil.getInstance(getBaseActivity()).getButtonSprite("instruction_btn.png", "instruction_btn_p.png");
		placeToCenterX(instructionBtn, 380);
		attachChild(instructionBtn);
		registerTouchArea(instructionBtn);
		
		instructionBtn.setOnClickListener(new ButtonSprite.OnClickListener() {
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				
				isHelpVisible = false;
				
				//on supprime les elements inutiles
				instructionSprite.detachSelf();
				instructionBtn.detachSelf();
				unregisterTouchArea(instructionBtn);

				// ゾンビを出現させる
				showNewZombie();
				
				// 攻撃アイテムを出現させる
				showNewWeapon();
				
				showWalls(WALL_NB);
			
			}
		});
	}

	
	@Override
	public void prepareSoundAndMusic() {
		
		// 効果音をロード
		try {
			btnPressedSound = SoundFactory.createSoundFromAsset(
					getBaseActivity().getSoundManager(), getBaseActivity(),
					"door03.wav");
			weapon01Sound = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "burst00.wav");
			;
			weapon02Sound = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "freeze03.wav");
			;
			weapon03Sound = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "crash20_a.wav");
			;
			zombieAppearSound = SoundFactory.createSoundFromAsset(
					getBaseActivity().getSoundManager(), getBaseActivity(),
					"buble00.wav");
			;
			gameoverSound = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "gara00.wav");
			;
			comboSound = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "pyoro62.wav");
			;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 『戻る』ボタン押下
	 * (non-Javadoc)
	 * @see com.canujohann.scenes.KeyListenScene#dispatchKeyEvent(android.view.KeyEvent)
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		
		//[return] button
		if (e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (isGameOver) {
				return false;
			}
			
			//if pause, we stop pause
			if (isPaused) {
				
				// detach系のメソッドは別スレッドで
				getBaseActivity().runOnUpdateThread(new Runnable() {
					public void run() {
						for (int i = 0; i < pauseBg.getChildCount(); i++) {
							// 忘れずにタッチの検知を無効に
							unregisterTouchArea((ButtonSprite) pauseBg.getChildByIndex(i));
						}
						pauseBg.detachSelf();
						pauseBg.detachChildren();
					}
				});

				resumeGame();

				isPaused = false;
				return true;
			} else {
				return false;
			}
			
			//menu
		} else if (e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_MENU) {
			
			// ポーズ中でなければポーズ画面を出す
			if (!isPaused) {
				showMenu();
			}
			
			return true;
		}
		return false;
	}

	
	/*
	 * 主人公を端末の傾きに合わせて移動する関数
	 */
	public void updateByActivity(float velX, float velY) {
		
		if (isPaused || isGameOver) {
			return;
		}

		// 遊び方画面が出ている時は更新しない
		if (isHelpVisible) {
			return;
		}
		
		//score 
		currentScore++;
		currentScoreText.setText("Score " + currentScore);

		// 傾きが0ならば何もしない
		if ((velX != 0) || (velY != 0)) {
			
			
			// 移動の上限、下限を設定
			int minX = 0;
			int minY = 0;
			int maxX = (int) getBaseActivity().getEngine().getCamera().getWidth()	- (int) boySprite.getWidth();
			int maxY = (int) getBaseActivity().getEngine().getCamera().getHeight() - (int) boySprite.getHeight();

			// 移動
			if (boyNewX >= minX) {
				boyNewX += velX;
			} else {
				boyNewX = minX;
			}
			if (boyNewX <= maxX) {
				boyNewX += velX;
			} else {
				boyNewX = maxX;
			}
			if (boyNewY >= minY) {
				boyNewY += velY;
			} else {
				boyNewY = minY;
			}
			if (boyNewY <= maxY) {
				boyNewY += velY;
			} else {
				boyNewY = maxY;
			}

			// 移動前の座標
			float[] currentXY = { boySprite.getX(), boySprite.getY() };
			
			/*
			 * check before moving if collides with wall
			 * we don't move the real pig, only the one hidden (boyFirst)
			 */
			hasContactWithWall = false;
			boyFirst.setPosition(boyNewX, boyNewY);
			
			if(wallArray!= null){
				for(int j=0; j<wallArray.size(); j ++){
					if(boyFirst.collidesWith(wallArray.get(j))){
							hasContactWithWall=true;
							break;
//						}						
						
					}
				}
			}
			
			
			/*
			 * If no collision with wall, we move pig
			 * if collision, no move
			 */
			if(!hasContactWithWall){
				
				boySprite.setPosition(boyNewX, boyNewY);
				
				// 移動後の座標
				float[] destinationXY = { boySprite.getX(), boySprite.getY() };
	
				// 壁にぶつかっている時等移動しない時には角度は変更しない
				if (currentXY[0] != destinationXY[0] || currentXY[1] != destinationXY[1]) {
					
					// 移動前と移動後の座標の2点の角度を計算
					double angle = getAngleByTwoPosition(currentXY, destinationXY);
					boySprite.setRotation((float) angle);					
				}
				
			}else{
				boyFirst.setPosition(currentXY[0], currentXY[1]);
				boyNewX = currentXY[0];
				boyNewY = currentXY[1];
			}
			
			
		}
		
		
		for (int i = 0; i < zombieArray.size(); i++) {
			
			AnimatedSprite zombie = zombieArray.get(i);

			if(zombie.getTag() != TAG_ZOMBIE_FIX_01 && zombie.getTag() != TAG_ZOMBIE_FIX_02){
				
					// ゾンビの種類によってスピードを変更する
					float[] zombieXY = { zombie.getX(), zombie.getY() };
					float[] boyXY = { boySprite.getX(), boySprite.getY() };
					
					double angle = getAngleByTwoPosition(zombieXY, boyXY);
	
					// 移動距離と角度からx方向、y方向の移動量を求める
					float distanceZombies = 0f;
					if (zombie.getTag() == TAG_ZOMBIE_01) {
						distanceZombies = 1.0f + (((zombieArray.size() - 1) - i) * 0.3f);
					} else {
						distanceZombies = 2.0f + (((zombieArray.size() - 1) - i) * 0.5f);
					}
	
					float x = -(float) (distanceZombies * Math.cos(angle * Math.PI / 180.0));
					float y = -(float) (distanceZombies * Math.sin(angle * Math.PI / 180.0));
					
					/*
					 * 
					 */
					zombieFirst.setPosition(zombie.getX() + x, zombie.getY() + y);
					boolean hasContact = false;					
					if(wallArray!= null){
						for(int j=0; j<wallArray.size(); j ++){
							if(zombieFirst.collidesWith(wallArray.get(j))){
									hasContact=true;
									break;								
							}
						}
					}
					

					if(!hasContact){						
						zombie.setPosition(zombieFirst.getX() , zombieFirst.getY());
						zombie.setRotation((float) angle);
					}else{						
						zombieFirst.setPosition(zombie.getX(), zombie.getY());						
					}
											
					if (zombie.collidesWith(boySprite)) {
	
						float xDistance = zombie.getX() - boySprite.getX();
						float yDistance = zombie.getY() - boySprite.getY();
		
						double distance = Math.sqrt(Math.pow(xDistance, 2)	+ Math.pow(yDistance, 2));
						if (distance < 30) {
							showGameOver();
						}
					}
					
					

					
					
					
			}
		}
		
		//items
		for (int i = 0; i < itemArray.size(); i++) {

			Sprite item = itemArray.get(i);

			//when touching item
			if (item.collidesWith(boySprite)) {
				
				fireWeapon(item);
				
				//remove item
				item.detachSelf();
				itemArray.remove(item);
			}
		}
		
		
	}

	// ゾンビを一体出現
	public void showNewZombie() {
		
		for (int i = 0; i < zombieArray.size(); i++) {
			
			
			final AnimatedSprite zombieSprite = zombieArray.get(i);
			

			if(zombieSprite.getTag() == TAG_ZOMBIE_FIX_01 || zombieSprite.getTag() == TAG_ZOMBIE_FIX_02){
				
				// 画面上に穴があれば2秒後にゾンビを出現させる
				TimerHandler delayHandler = new TimerHandler(2.0f,	new ITimerCallback() {
	
							public void onTimePassed(TimerHandler pTimerHandler) {
								
								if(zombieSprite.getTag() == TAG_ZOMBIE_FIX_01){
									zombieSprite.setTag(TAG_ZOMBIE_01);
								}else{
									zombieSprite.setTag(TAG_ZOMBIE_02);
								}
								
								//Stop mouvement mougna-mougna
								zombieSprite.unregisterEntityModifiers(new IEntityModifierMatcher() {
								public boolean matches(IModifier<IEntity> pObject) {
									return true;
								}});
								
								// アニメーション開始
								zombieSprite.setAlpha(1.0f);
								zombieSprite.animate(100);			
								
							}
						});
				
				registerUpdateHandler(delayHandler);
			}
			
		}
		
		
		//敵の数
		int zombieCount = 0;	
		for (int i = 0; i < getChildCount(); i++) {
			if (getChildByIndex(i).getTag() == TAG_ZOMBIE_01
					|| getChildByIndex(i).getTag() == TAG_ZOMBIE_02
					|| getChildByIndex(i).getTag() == TAG_ZOMBIE_FIX_01
					|| getChildByIndex(i).getTag() == TAG_ZOMBIE_FIX_02) {
				zombieCount++;
			}
		}
		
		
		if (zombieCount < 5) {
			
			AnimatedSprite bad = getABadGuy();
			
			// 出現場所の座標
			int x = (int) (Math.random() * (getBaseActivity().getEngine().getCamera().getWidth() - bad.getWidth()));
			int y = (int) (Math.random() * (getBaseActivity().getEngine().getCamera().getHeight() - bad.getHeight()));

			bad.setPosition(x, y);
			bad.setZIndex(3);
			attachChild(bad);
			sortChildren();
			
			//sound for a bad guy
			zombieAppearSound.play();

			//clignotement
			bad.registerEntityModifier(	new LoopEntityModifier(
					new SequenceEntityModifier(
						new AlphaModifier(1, 0, 1), 
						new AlphaModifier(1, 1, 0)
					)
			));

			// 配列に追加
			zombieArray.add(bad);
			
		}
		
		// 3秒後にもう一度呼び出す
		registerUpdateHandler(new TimerHandler(5, new ITimerCallback() {
			public void onTimePassed(TimerHandler pTimerHandler) {
				showNewZombie();
			}
		}));
	}

	/* 敵を生成 */
	private AnimatedSprite getABadGuy(){
		
		AnimatedSprite zombieSprite;
		int r = (int) (Math.random() * 10);
		
		if (r > 7) {
			// 速いやつ
			zombieSprite = getBaseActivity().getResourceUtil().getAnimatedSprite("boucher2.png", 1, 2);
			zombieSprite.setTag(TAG_ZOMBIE_FIX_02);
		} else  {
			// 遅いやつ
			zombieSprite = getBaseActivity().getResourceUtil().getAnimatedSprite("boucher.png", 1, 2);
			zombieSprite.setTag(TAG_ZOMBIE_FIX_01);
		} 
		
		return zombieSprite;
	}
	
	
	/* アイテムを一個出現 */
	public void showNewWeapon() {

		// アイテム用Sprite
		AnimatedSprite itemSprite = null;
		// それぞれ1/10の確率で出現
		int r = (int) (Math.random() * 10);
		if (r == 0) {
			itemSprite = getBaseActivity().getResourceUtil().getAnimatedSprite("bomb.png",1,2);
			itemSprite.setTag(TAG_ITEM_01);
		} else if (r == 1) {
			itemSprite = getBaseActivity().getResourceUtil().getAnimatedSprite("dynamite.png",1,2);
			itemSprite.setTag(TAG_ITEM_02);
		}

		
		// アイテム出現に当選したら
		if (itemSprite != null) {
		
			// 出現場所の座標
			int x = (int) (Math.random() * (getBaseActivity().getEngine()
					.getCamera().getWidth() - itemSprite.getWidth()));
			int y = (int) (Math.random() * (getBaseActivity().getEngine()
					.getCamera().getHeight() - itemSprite.getHeight()));

			itemSprite.setZIndex(zIndexItem);
			itemSprite.setPosition(x, y);
			itemSprite.animate(200);
			attachChild(itemSprite);
			
			// z-indexを適用
			sortChildren();

			itemArray.add(itemSprite);
		}

		// 3秒後に再度呼び出し
		TimerHandler handler = new TimerHandler(3, new ITimerCallback() {
			public void onTimePassed(TimerHandler pTimerHandler) {
				showNewWeapon();
			}
		});
		
		registerUpdateHandler(handler);
	}

	
	//
	public void showWalls(int nb) {
		
		//障害物
		Sprite[] listWalls = new Sprite[nb];
		
		for(int i=0; i< nb; i++){
						
			//偶数を使って、spriteを定義
			listWalls[i] = getBaseActivity().getResourceUtil().getSprite("wall.png");
						
			//xY設定
			int x = (int) (Math.random() * (getBaseActivity().getEngine().getCamera().getWidth() - listWalls[i].getWidth()));
			int y = (int) (Math.random() * (getBaseActivity().getEngine().getCamera().getHeight() - listWalls[i].getHeight()));
						
			listWalls[i].setZIndex(zIndexItem);
			listWalls[i].setPosition(x, y);
			listWalls[i].setRotation((float)(Math.random())* 360.0f);

			attachChild(listWalls[i]);
			
			// z-indexを適用
			sortChildren();

			wallArray.add(listWalls[i]);
			
		}

	}

	

	/*
	 * attack
	 */
	public void fireWeapon(Sprite item) {
		
		// 攻撃エフェクトを格納する配列
		ArrayList<Sprite> weaponSpriteArray = new ArrayList<Sprite>();

		//bomb
		if (item.getTag() == TAG_ITEM_01) {
			
			weapon01Sound.play();
			

			//animation
			final Sprite weaponSprite = new Sprite(0, 0, weaponTextureArray[0],	getBaseActivity().getVertexBufferObjectManager());
			weaponSprite.setPosition(boySprite.getX() + boySprite.getWidth()/ 2 - weaponSprite.getWidth() / 2, boySprite.getY()
					+ boySprite.getHeight() / 2 - weaponSprite.getHeight() / 2);
			weaponSprite.setZIndex(zIndexItem);
			weaponSprite.setBlendFunction(GL10.GL_SRC_ALPHA,GL10.GL_ONE_MINUS_SRC_ALPHA);
			weaponSprite.setAlpha(0);
			attachChild(weaponSprite);
			sortChildren();

			weaponSpriteArray.add(weaponSprite);

			//fadein-fadeout
			weaponSprite.registerEntityModifier(new SequenceEntityModifier(
					new FadeInModifier(0.2f), new FadeOutModifier(0.7f)));

			//after 1sec
			registerUpdateHandler(new TimerHandler(1, new ITimerCallback() {
				public void onTimePassed(TimerHandler pTimerHandler) {
					weaponSprite.detachSelf();
				}
			}));
		}
		
		// 氷攻撃
		else{
			weapon02Sound.play();
			for (int i = 0; i < 2; i++) {
				final Sprite weaponSprite = new Sprite(0, 0,
						weaponTextureArray[1 + i], getBaseActivity()
								.getVertexBufferObjectManager());
				switch (i) {
				case 0:
					weaponSprite.setPosition(
							boySprite.getX() + boySprite.getWidth() / 2
									- weaponSprite.getWidth() / 2, 0);
					break;
				case 1:
					weaponSprite.setPosition(0,
							boySprite.getY() + boySprite.getHeight() / 2
									- weaponSprite.getHeight() / 2);
					break;
				}
				weaponSprite.setZIndex(zIndexItem);
				// アルファ値の設定を可能に
				weaponSprite.setBlendFunction(GL10.GL_SRC_ALPHA,
						GL10.GL_ONE_MINUS_SRC_ALPHA);
				weaponSprite.setAlpha(0);
				attachChild(weaponSprite);
				sortChildren();

				weaponSpriteArray.add(weaponSprite);

				// 攻撃アイテムのアニメーション
				weaponSprite.registerEntityModifier(new SequenceEntityModifier(
						new FadeInModifier(0.2f), new FadeOutModifier(0.7f)));
				// アニメーション終了後に削除
				registerUpdateHandler(new TimerHandler(1, new ITimerCallback() {
					public void onTimePassed(TimerHandler pTimerHandler) {
						weaponSprite.detachSelf();
					}
				}));
			}
		}
		
		// 画面から削除されたゾンビを格納する配列
		final ArrayList<AnimatedSprite> detachedZombieArray = new ArrayList<AnimatedSprite>();
		
		// 衝突判定、ゾンビを消す
		for (Sprite weapon : weaponSpriteArray) {
			
			for (int i = 0; i < zombieArray.size(); i++) {
				
				//only for zombies moving
				if(zombieArray.get(i).getTag() != TAG_ZOMBIE_FIX_01 &&  zombieArray.get(i).getTag() != TAG_ZOMBIE_FIX_02){
				
					final AnimatedSprite zombie = zombieArray.get(i);
					
					if (zombie.collidesWith(weapon)) {
						// 既に追加済みでなければ配列に格納
						if (!detachedZombieArray.contains(zombie)) {
							detachedZombieArray.add(zombie);
						}
						// ゾンビをフェードアウト
						zombie.registerEntityModifier(new FadeOutModifier(0.7f));
						registerUpdateHandler(new TimerHandler(0.8f,
								new ITimerCallback() {
									public void onTimePassed(
											TimerHandler pTimerHandler) {
										zombie.detachSelf();
									}
								}));
					}
				}
			}
		}

		// 一回の攻撃で複数のゾンビを倒した時はコンボボーナスを与える
		if (detachedZombieArray.size() > 1) {
			comboSound.play();
			BitmapFont bitmapFont;
			if (detachedZombieArray.size() == 2) {
				bitmapFont = comboBMFontArray[0];
			} else if (detachedZombieArray.size() == 3) {
				bitmapFont = comboBMFontArray[1];
			} else {
				bitmapFont = comboBMFontArray[2];
			}
			bitmapFont.load();

			// ビットマップフォントを元にスコアを表示
			final Text comboText = new Text(0, 0, bitmapFont, "x"
					+ detachedZombieArray.size(), 20, new TextOptions(
					HorizontalAlign.CENTER), getBaseActivity()
					.getVertexBufferObjectManager());
			comboText.setPosition(boySprite.getX(), boySprite.getY());
			attachChild(comboText);

			// コンボのテキストを移動アニメーションさせる
			comboText.registerEntityModifier(new SequenceEntityModifier(
					new DelayModifier(2.0f), new MoveModifier(0.5f, comboText
							.getX(), currentScoreText.getX(), comboText.getY(),
							currentScoreText.getY(), EaseBackIn.getInstance()),
					new FadeOutModifier(0.5f)));
			final int multiplayer = detachedZombieArray.size();

			// 得点にコンボ数を乗算し適用する
			registerUpdateHandler(new TimerHandler(3, new ITimerCallback() {
				public void onTimePassed(TimerHandler pTimerHandler) {
					comboText.detachSelf();
					currentScore *= multiplayer;
				}
			}));
		}

		// 画面から削除されたゾンビを配列から削除
		for (AnimatedSprite zombie : detachedZombieArray) {
			zombieArray.remove(zombie);
		}
	}

	// ゲームオーバー
	public void showGameOver() {

		if (isGameOver) {
			return;
		}
		
		isGameOver = true;

		gameoverSound.play();
		
		// ハイスコア更新時はハイスコアのテキストも更新
		if (currentScore > SPUtil.getInstance(getBaseActivity()).getHighScore()) {
			
			// ハイスコアを保存
			SPUtil.getInstance(getBaseActivity()).setHighScore(currentScore);
			highScoreText.setText("Highscore "
					+ SPUtil.getInstance(getBaseActivity()).getHighScore());
		}

		// ゲームオーバー画面の背景画像
		Sprite resultBg = getBaseActivity().getResourceUtil().getSprite("result_bg.png");
		
		resultBg.setPosition(0, -getBaseActivity().getEngine().getCamera()	.getWidth());
		resultBg.setZIndex(zIndexGameOverLayer);
		attachChild(resultBg);
		sortChildren();

		// 主人公を縮小しながらフェードアウト
		boySprite.registerEntityModifier(new ParallelEntityModifier(
				new ScaleModifier(0.7f, 1f, 0.0f), 
				new FadeOutModifier(0.7f)
		));
		
		//stop movement of enemies
		for(int i=0; i <zombieArray.size();i++){
			zombieArray.get(0).stopAnimation();
		}

		// 全てのアップデートハンドラを削除
		unregisterUpdateHandlers(new IUpdateHandlerMatcher() {
			public boolean matches(IUpdateHandler pObject) {
				return true;
			}
		});
			
		
		// 上からゲームオーバー画面が振ってくるアニメーション
		resultBg.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(2.0f), new MoveModifier(1.0f,
						resultBg.getX(), resultBg.getX(), resultBg.getY(), 0,
						EaseQuadOut.getInstance())));

		Sprite title = getBaseActivity().getResourceUtil().getSprite(
				"result_title.png");
		placeToCenterX(title, 20);
		resultBg.attachChild(title);

		BitmapFont bitmapFont = new BitmapFont(getBaseActivity()
				.getTextureManager(), getBaseActivity().getAssets(),
				"font/result.fnt");
		bitmapFont.load();

		// ビットマップフォントを元にスコアを表示
		Text resultText = new Text(0, 0, bitmapFont, "" + currentScore, 20,
				new TextOptions(HorizontalAlign.CENTER), getBaseActivity()
						.getVertexBufferObjectManager());
		resultText.setPosition(getBaseActivity().getEngine().getCamera()
				.getWidth()
				/ 2.0f - resultText.getWidth() / 2.0f, 90);
		resultBg.attachChild(resultText);

		// ボタン類

		ButtonSprite btnRetry = getBaseActivity().getResourceUtil()
				.getButtonSprite("result_btn_02.png", "result_btn_02.png");
		btnRetry.setPosition(10, 225);
		btnRetry.setTag(MENU_RETRY);
		btnRetry.setOnClickListener(this);
		resultBg.attachChild(btnRetry);
		registerTouchArea(btnRetry);

		ButtonSprite btnScore = getBaseActivity().getResourceUtil()
				.getButtonSprite("result_btn_03.png", "result_btn_03.png");
		btnScore.setPosition(getBaseActivity().getEngine().getCamera()
				.getWidth()	/ 2.0f - (btnScore.getWidth()/2), 225);
		btnScore.setTag(MENU_RANKING);
		btnScore.setOnClickListener(this);
		resultBg.attachChild(btnScore);
		registerTouchArea(btnScore);

		ButtonSprite btnMenu = getBaseActivity().getResourceUtil()
				.getButtonSprite("result_btn_04.png", "result_btn_04.png");
		btnMenu.setPosition(getBaseActivity().getEngine().getCamera()
				.getWidth()-btnMenu.getWidth()-10, 225);
		btnMenu.setTag(MENU_MENU);
		btnMenu.setOnClickListener(this);
		resultBg.attachChild(btnMenu);
		registerTouchArea(btnMenu);
	}

	
	/*
	 * メニュの表示
	 */
	public void showMenu() {
		if (isGameOver) {
			return;
		}
		pauseGame();

		// 四角形を描画
		pauseBg = new Rectangle(0, 0, getBaseActivity().getEngine().getCamera()
				.getWidth(), getBaseActivity().getEngine().getCamera()
				.getHeight(), getBaseActivity().getVertexBufferObjectManager());
		pauseBg.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		pauseBg.setColor(0, 0, 0);
		pauseBg.setAlpha(0.7f);
		attachChild(pauseBg);

		try {
			
			ButtonSprite btnMenu = getBaseActivity()
					.getResourceUtil()
					.getButtonSprite("result_btn_05.png", "result_btn_05.png");
			placeToCenterX(btnMenu, 100);
			btnMenu.setTag(MENU_RESUME);
			btnMenu.setOnClickListener(this);
			pauseBg.attachChild(btnMenu);
			registerTouchArea(btnMenu);

			ButtonSprite btnTweet = getBaseActivity().getResourceUtil()
					.getButtonSprite("result_btn_02.png", "result_btn_02.png");
			placeToCenterX(btnTweet, 220);
			btnTweet.setTag(MENU_RETRY);
			btnTweet.setOnClickListener(this);
			pauseBg.attachChild(btnTweet);
			registerTouchArea(btnTweet);

			ButtonSprite btnRanking = getBaseActivity().getResourceUtil()
					.getButtonSprite("result_btn_04.png", "result_btn_04.png");
			placeToCenterX(btnRanking, 340);
			btnRanking.setTag(MENU_MENU);
			btnRanking.setOnClickListener(this);
			pauseBg.attachChild(btnRanking);
			registerTouchArea(btnRanking);
		} catch (Exception e) {
			e.printStackTrace();
		}

		isPaused = true;
	}

	// ゲームを一時的に停止
	public void pauseGame() {
		for (int i = 0; i < getChildCount(); i++) {
			if (getChildByIndex(i) instanceof AnimatedSprite) {
				((AnimatedSprite) getChildByIndex(i)).stopAnimation();
			}
		}
		unregisterUpdateHandlers(new IUpdateHandlerMatcher() {
			public boolean matches(IUpdateHandler pObject) {
				return true;
			}
		});

	}

	// 一時停止されたゲームを再開
	public void resumeGame() {
		for (int i = 0; i < getChildCount(); i++) {
			if (getChildByIndex(i) instanceof AnimatedSprite) {
				((AnimatedSprite) getChildByIndex(i)).stopAnimation();
			}
		}

//		for (Sprite hole : holeArray) {
//			hole.registerEntityModifier(new LoopEntityModifier(
//					new SequenceEntityModifier(new ScaleModifier(0.2f, 1.0f,
//							1.1f), new ScaleModifier(0.2f, 1.1f, 1.0f))));
//		}

		TimerHandler timerHandler = new TimerHandler(1, new ITimerCallback() {
			public void onTimePassed(TimerHandler pTimerHandler) {
				showNewZombie();
				showNewWeapon();
			}
		});
		registerUpdateHandler(timerHandler);
	}

//	private TimerHandler zombieAttackHandler = new TimerHandler(1f / 60f, true,
//			new ITimerCallback() {
//				public void onTimePassed(TimerHandler pTimerHandler) {
//					for (int i = 0; i < zombieArray.size(); i++) {
//						AnimatedSprite zombie = zombieArray.get(i);
//
//						if (zombie.getTag() == TAG_ZOMBIE_01
//								|| zombie.getTag() == TAG_ZOMBIE_02) {
//							float[] zombieXY = { zombie.getX(), zombie.getY() };
//							float[] boyXY = { boySprite.getX(),
//									boySprite.getY() };
//							double angle = getAngleByTwoPosition(zombieXY,
//									boyXY);
//
//							float distance = 8f;
//
//							float x = -(float) (distance * Math.cos(angle
//									* Math.PI / 180.0));
//							float y = -(float) (distance * Math.sin(angle
//									* Math.PI / 180.0));
//
//							// 移動
//							zombie.setPosition(zombie.getX() + x, zombie.getY()
//									+ y);
//							// 向きを変更
//							zombie.setRotation((float) angle);
//
//						} else if (zombie.getTag() == TAG_ZOMBIE_03) {
//							zombie.setRotation(zombie.getRotation() + 1);
//						}
//					}
//				}
//			});

	// 2点間の角度を求める公式
	private double getAngleByTwoPosition(float[] start, float[] end) {
		double result = 0;

		float xDistance = end[0] - start[0];
		float yDistance = end[1] - start[1];

		result = Math.atan2((double) yDistance, (double) xDistance) * 180
				/ Math.PI;
		result += 180;

		return result;
	}

	public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
			float pTouchAreaLocalY) {
		btnPressedSound.play();
		switch (pButtonSprite.getTag()) {
		case MENU_RESUME:
			getBaseActivity().runOnUpdateThread(new Runnable() {
				public void run() {
					for (int i = 0; i < pauseBg.getChildCount(); i++) {
						// 忘れずにタッチの検知を無効に
						unregisterTouchArea((ButtonSprite) pauseBg
								.getChildByIndex(i));
					}
					pauseBg.detachSelf();
					pauseBg.detachChildren();
				}
			});

			resumeGame();

			isPaused = false;
			break;
		case MENU_RETRY:
			MainScene newScene = new MainScene(getBaseActivity());
			getBaseActivity().refreshRunningScene(newScene);
			break;
		case MENU_MENU:
			getBaseActivity().backToInitial();
			break;
		case MENU_RANKING:			
			Swarm.enableAlternativeMarketCompatability();
			Swarm.init(getBaseActivity(), Consts.SWARM_APP_ID, Consts.SWARM_APP_KEY);
			SwarmLeaderboard.submitScore(Consts.LEADERBOARD_ID, (int)currentScore);			
			break;
		}
	}
}
