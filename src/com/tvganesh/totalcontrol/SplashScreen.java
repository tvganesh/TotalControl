/* 
 * Developed by Tinniam V Ganesh, 30 May  2013
 * Uses Box2D physics engine and AndEngine
 * 
 */

package com.tvganesh.totalcontrol;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class SplashScreen extends Activity {
	private long ms=0;
	private long splashTime=2000;
	private boolean splashActive = true;
	private boolean paused=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		Thread mythread = new Thread() {
    	public void run() {
    		try {
    			while (splashActive && ms < splashTime) {
    				if(!paused)
    					ms=ms+100;
    				sleep(100);
    			}
    		} catch(Exception e) {}
    		finally {
    			Intent intent = new Intent(SplashScreen.this, TotalControl.class);
        		startActivity(intent);
    		}
        	}
    };
    mythread.start(); 
	}
}
