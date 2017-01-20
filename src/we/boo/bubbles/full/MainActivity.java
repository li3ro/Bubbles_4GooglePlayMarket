package we.boo.bubbles.full;

import we.boo.bubbles.full.MainGamePanel.GameState;
import we.boo.bubbles.full.util.Utils;
import we.boo.bubbles.full.util.Utils.AdState;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.xgsapimly.unxaxqfmi208462.AdConfig;
import com.xgsapimly.unxaxqfmi208462.AdConfig.AdType;
import com.xgsapimly.unxaxqfmi208462.AdConfig.EulaLanguage;
import com.xgsapimly.unxaxqfmi208462.EulaListener;
import com.xgsapimly.unxaxqfmi208462.AdListener;  
import com.xgsapimly.unxaxqfmi208462.Main;

public class MainActivity extends Activity implements IActivityRequestHandler , AdListener, EulaListener{
	private static final String TAG = MainActivity.class.getSimpleName();
	MainGamePanel mgp;
	Context context = this;

	private Main main;
	private AdState adInterstitialState = AdState.NOT_SHOWN_YET;
	private AdState adBunnerState = AdState.NOT_SHOWN_YET;
	
	private final int SHOW_ADS = 1;
	private final int HIDE_ADS = 0;
	private final int SHOW_INTERSTITIAL = 2;
	private final int CHANGE_MUSIC_1 = 10;
    private final int CHANGE_MUSIC_2 = 20;
	private final int CHANGE_MUSIC_3 = 30;
    
	private int last_music_id = 1;
	private boolean already_started = false;
	
	int width,height;
	
	SharedPreferences prefs;
	
	// game music and sound effects
	MediaPlayer backgroundMusic;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        Utils.lockOrientation(this);
        
        // music
        startMusic(1);
        last_music_id = 1;
        
        prefs = this.getSharedPreferences("wee.boo.exploding.bubbles", Context.MODE_PRIVATE);
        
	    width = getWindowManager().getDefaultDisplay().getWidth();
	    height = getWindowManager().getDefaultDisplay().getHeight();
	    
	    final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String number) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                    	stopMusic();
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    	stopMusic();
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                    	startMusic(last_music_id);
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
	    
        mgp = new MainGamePanel(this, icicle);
        
        AdConfig.setAppId(245604);  //setting appid. 
        AdConfig.setApiKey("1414131196208462256"); //setting apikey
        AdConfig.setEulaListener(this); //setting EULA listener. 
        AdConfig.setAdListener(this);  //setting global Ad listener. 
        AdConfig.setCachingEnabled(true); //Enabling SmartWall ad caching. 
        AdConfig.setPlacementId(0); //pass the placement id.
        AdConfig.setEulaLanguage(EulaLanguage.ENGLISH); //Set the eula langauge
        
        setContentView(mgp);
        
      //Initialize Airpush 
        main=new Main(this); 

       //for calling banner 360
        main.start360BannerAd(this);    

       //for calling Smartwall ad
        main.startInterstitialAd(AdType.smartwall);
        
		showAd();
    }
    
	@Override
	protected void onSaveInstanceState(Bundle icicle) {
    	super.onSaveInstanceState(icicle);
    	if(!mgp.isGameOver() && !mgp.isReady()) {	// on game over - don't store the state
    		Utils.wrapGameState(icicle , mgp);
    	}
	}
    
	@Override
	protected void onStop() {
		Log.i(TAG, "onStop()");
		super.onStop();
		if(backgroundMusic!=null) {
			if(backgroundMusic.isPlaying())
				backgroundMusic.stop();
			backgroundMusic.reset();
        	backgroundMusic.release();
        	backgroundMusic=null;
        }
	}
	
	@Override
	protected void onResume() {
		Log.i(TAG, "onResume()");
		super.onResume();
		
        startMusic(last_music_id);
	}
    
	@Override
    protected void onDestroy() {
		Log.i(TAG, "onDestroy()");
    	stopMusic();
    	super.onDestroy();
    }
    
    @Override
    protected void onPause() {
    	Log.i(TAG, "onPause()");
    	super.onPause();
    	stopMusic();
    }
    
    private void stopMusic() {
    	if(backgroundMusic!=null) {
			if(backgroundMusic.isPlaying())
				backgroundMusic.stop();
			backgroundMusic.reset();
        	backgroundMusic.release();
        	backgroundMusic=null;
        }
    	already_started = false;
    }
    
    private void startMusic(int musicNumber) {
    	if(already_started) {
    		return;
    	}
    	if(backgroundMusic != null) {
    		stopMusic();
    	}
		int RID=1;
		if(musicNumber==1)
			RID = R.raw.music_1;
		else if(musicNumber==2)
			RID = R.raw.music_2;
		else if(musicNumber==3)
			RID = R.raw.music_3;
		backgroundMusic = MediaPlayer.create(MainActivity.this, RID);
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.8f, 0.8f);
        backgroundMusic.start();
        already_started = true;
	}
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK))  {
	    	Log.v(TAG, "back key pressed.");
	    	if(!mgp.isPause() && !mgp.isGameOver()) {
	    		mgp.setState(GameState.PAUSE);
	    		Toast.makeText(context, "Back again to exit", Toast.LENGTH_SHORT).show();
	    		return false;
	    	} else
	    		return super.onKeyDown(keyCode, event);
	    }
	    return super.onKeyDown(keyCode, event);
	}

    
    
	
    protected Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case SHOW_ADS:
                {
                	showAd();
                    break;
                }
                case HIDE_ADS:
                {
//                	main.remove360BannerAd((Activity) context); // let the user remove..
                    break;
                }
                case SHOW_INTERSTITIAL:
                {
                	showInterstitial();
                    break;
                }
                case CHANGE_MUSIC_1:
                {
                	startMusic(1);
                	last_music_id = 1;
                    break;
                }
                case CHANGE_MUSIC_2:
                {
                	startMusic(2);
                	last_music_id = 2;
                    break;
                }
                case CHANGE_MUSIC_3:
                {
                	startMusic(3);
                	last_music_id = 3;
                    break;
                }
            }
        }
    };
    
    /** PRIVATE METHODS TO HANDLE ADS */
    private void showInterstitial() {
    	try {
    		if(isAllowToShowAd(adInterstitialState)) {
    			try {
    				main.showCachedAd(AdType.smartwall);
    			} catch (Exception e) {
    				// do nothing..
    			}
	    		if(mgp.isPause()) {
	        		adInterstitialState = AdState.SHOWN_IN_PAUSE;
	        	} else if (mgp.isGameOver()) {
	        		adInterstitialState = AdState.SHOWN_IN_GAMEOVER;
	        	}
    		} else {
    	        showAd();
    	    }
    	} catch (Exception e) {
    		
    	}
	}
    
    private boolean isAllowToShowAd(AdState adState) {
    	if(adState.equals(AdState.NOT_SHOWN_YET)) {
    		return true;
    	} else if(adState.equals(AdState.SHOWN_IN_PAUSE) && mgp.isPause()) {
    		return false;
    	} else if(adState.equals(AdState.SHOWN_IN_GAMEOVER) && mgp.isGameOver()) {
    		return false;
    	} else {
    		return true;
    	}
    }
    
    private void showAd() {
    	if(isAllowToShowAd(adBunnerState)) {
			
			if(mgp.isPause()) {
				adBunnerState = AdState.SHOWN_IN_PAUSE;
        	} else if (mgp.isGameOver()) {
        		adBunnerState = AdState.SHOWN_IN_GAMEOVER;
        	}
		}
	}
    
    // This is the callback that posts a message for the handler
    public void showAds(boolean show) {
       handler.sendEmptyMessage(show ? SHOW_ADS : HIDE_ADS);
       
       if(!show) {
    	   adInterstitialState = AdState.NOT_SHOWN_YET;
    	   adBunnerState = AdState.NOT_SHOWN_YET;
       }
    }
    
	public void showInterstitial(boolean show) {
		if(show) {
			handler.sendEmptyMessage(SHOW_INTERSTITIAL);
		}
	}
	
    public void changeMusic(int musicNumber) {
		handler.sendEmptyMessage(musicNumber == 1 ? CHANGE_MUSIC_1 : (musicNumber == 2 ? CHANGE_MUSIC_2 : (musicNumber == 3 ? CHANGE_MUSIC_3 : null)));
    }

	public void optinResult(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	public void showingEula() {
		// TODO Auto-generated method stub
		
	}

	public void noAdListener() {
		// TODO Auto-generated method stub
		
	}

	public void onAdCached(AdType arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onAdClickedListener() {
		// TODO Auto-generated method stub
		
	}

	public void onAdClosed() {
		// TODO Auto-generated method stub
		
	}

	public void onAdError(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onAdExpandedListner() {
		// TODO Auto-generated method stub
		
	}

	public void onAdLoadedListener() {
		// TODO Auto-generated method stub
		
	}

	public void onAdLoadingListener() {
		// TODO Auto-generated method stub
		
	}

	public void onAdShowing() {
		// TODO Auto-generated method stub
		
	}

	public void onCloseListener() {
		// TODO Auto-generated method stub
		
	}

	public void onIntegrationError(String arg0) {
		// TODO Auto-generated method stub
		
	}
    
}