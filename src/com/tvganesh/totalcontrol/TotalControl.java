/* 
 * Developed by Tinniam V Ganesh, 30 May  2013
 * Uses Box2D physics engine and AndEngine
 * 
 */

package com.tvganesh.totalcontrol;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.opengl.view.RenderSurfaceView;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;


import org.andengine.entity.text.Text;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;

import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;

public class TotalControl extends SimpleBaseGameActivity implements IAccelerationListener {
	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	public static final float PIXEL_TO_METER_RATIO_DEFAULT = 32.0f;
	
	private BitmapTextureAtlas mBitmapTextureAtlas;
    
    private Scene mScene;
    
    private PhysicsWorld mPhysicsWorld;
	private TextureRegion mBlackBallTextureRegion,mBlueBallTextureRegion;
	private TextureRegion mPurpleBallTextureRegion,mGreenBallTextureRegion;
	private TextureRegion mBallTextureRegion;
	private TextureRegion mBrickTextureRegion;
    private static FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(50f, 0.0f, 0.5f);
    
    Sprite blackBall, blueBall, purpleBall, greenBall;
    AdView adView;

    
	public EngineOptions onCreateEngineOptions() {
		
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}
	
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");	
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 80, 80, TextureOptions.BILINEAR);		
		
		this.mBallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "black.png", 0, 0);
		this.mBitmapTextureAtlas.load();		
		
		this.mBlackBallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "ball_black.png",8, 8);
		this.mBitmapTextureAtlas.load();	
		
		this.mBlueBallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "ball_blue.png",26, 26);
		this.mBitmapTextureAtlas.load();	
		
		this.mPurpleBallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "ball_purple.png",44, 44);
		this.mBitmapTextureAtlas.load();
		
		this.mGreenBallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "ball_green.png",62, 62);
		this.mBitmapTextureAtlas.load();
		
	
		
		this.enableAccelerationSensor(this);
	

	}
	
	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
		
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
		
		// Create a Maze scene
		this.initGame(mScene);
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		return mScene;		
		
	}
	
	public void initGame(Scene mScene){
		
		
		Body blackBallBody,blueBallBody,greenBallBody,purpleBallBody;
	
		
		//Create the floor,ceiling and walls
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.0f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);
		
		// Create the balls in the maze, with no restitution and a small coefficient of friction
		final FixtureDef gameFixtureDef = PhysicsFactory.createFixtureDef(200f, 0.0f, 0.05f);
		
		blackBall = new Sprite(358, 200, this.mBlackBallTextureRegion, this.getVertexBufferObjectManager());
		blackBallBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, blackBall, BodyType.DynamicBody, gameFixtureDef);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(blackBall, blackBallBody, true, true));
		this.mScene.attachChild(blackBall);
		
		greenBall = new Sprite(358, 358, this.mGreenBallTextureRegion, this.getVertexBufferObjectManager());
		greenBallBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, greenBall, BodyType.DynamicBody, gameFixtureDef);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(greenBall, greenBallBody, true, true));
		this.mScene.attachChild(greenBall);
		
		blueBall = new Sprite(316, 316, this.mBlueBallTextureRegion, this.getVertexBufferObjectManager());
		blueBallBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, blueBall, BodyType.DynamicBody, gameFixtureDef);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(blueBall, blueBallBody, true, true));
		this.mScene.attachChild(blueBall);
		
		purpleBall = new Sprite(270, 220, this.mPurpleBallTextureRegion, this.getVertexBufferObjectManager());
		purpleBallBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, purpleBall, BodyType.DynamicBody, gameFixtureDef);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(purpleBall, purpleBallBody, true, true));
		this.mScene.attachChild(purpleBall);
		
		// Create the maze using small bodies (200) placed in a circle
		int MAXBODIES = 200;
		int nBodies = 200;
		final Line Line[] = new Line[nBodies];
		float centers[][] = new float[nBodies][2];
		final Sprite circles[] = new Sprite[nBodies];
		final Body circlesBody[] = new Body[nBodies];
		final Line connectionLine[] = new Line[nBodies];
		//Draw a circle
		
		float x[] = new float[nBodies];
		float y[] = new float[nBodies];
		
		float cx = 360.0f;
		float cy = 200.0f;

		final float PI=3.1415f;
		float lineWidth = 5.0f;
		
		// Create appropriate gaps for the game
		for(int h = 0; h < 5; h++) {
			nBodies =  MAXBODIES - h*20;
			float rx = 200.0f - h*42;
			float ry = 200.0f - h*42;
			//Log.d("rx:","test:"+ rx + "nBodies" + nBodies);
			
			for (int i=0; i<nBodies; i++) {
				FIXTURE_DEF = PhysicsFactory.createFixtureDef(10f, 0.0f, 0.0f);
			
				//Circle : x= r cos (theta) 
				//         y = r sin (theta)
				
				// Create appropriate openings in the maze
			    if(h == 1){
			    	if((i > 120) && (i < 133))
			    		continue;
			    }
			    if(h == 2){
			    	if((i > 40) && (i < 52))
			    		continue;
			    }
			    if(h == 3){
			    	if((i> 120) && (i < 132))
			    	   continue;
			    }
			    if(h == 4)
			    {
			    	if((i > 5) && (i < 30))
			    		continue;
			    }
				float angle = (2 * PI* i)/nBodies;
				x[i] = cx + rx * (float)Math.sin(angle);
				y[i] = cy + ry * (float)Math.cos(angle);
			
				centers[i][0] = x[i];
				centers[i][1] = y[i];
			
		     
				Vector2 v1 = new Vector2(x[i],y[i]);
				final VertexBufferObjectManager vb = this.getVertexBufferObjectManager();
				circles[i] = new Sprite(x[i], y[i], this.mBallTextureRegion, this.getVertexBufferObjectManager());
				circlesBody[i] = PhysicsFactory.createCircleBody(this.mPhysicsWorld, circles[i], BodyType.StaticBody, FIXTURE_DEF);
			  
				this.mScene.attachChild(circles[i]);
	  
			  
			} 	// End for 
		}
		      
					
			this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		
	}


	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(), pAccelerationData.getY());
		this.mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
		
	}


	@Override
	public void onResumeGame() {
		super.onResumeGame();

		this.enableAccelerationSensor(this);

	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();

		this.disableAccelerationSensor();
	}
	
	// Create the frame with the Frame for displaying Ads from AdMob
	@Override 
	protected void onSetContentView() {
        // CREATING the parent FrameLayout // 
        final FrameLayout frameLayout = new FrameLayout(this);

        // CREATING the layout parameters, fill the screen //
        final FrameLayout.LayoutParams frameLayoutLayoutParams =
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                             FrameLayout.LayoutParams.MATCH_PARENT);

        // CREATING a Smart Banner View //
        this.adView = new AdView(this, AdSize.SMART_BANNER, "a151a647ddece24");

        // Doing something I'm not 100% sure on, but guessing by the name //
        adView.refreshDrawableState();
        adView.setVisibility(AdView.VISIBLE);

        // ADVIEW layout, show at the bottom of the screen //
        final FrameLayout.LayoutParams adViewLayoutParams =
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                                             FrameLayout.LayoutParams.WRAP_CONTENT,
                                             Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);

        // REQUEST an ad (Test ad) //
        /*AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice("41000003e4583000");
        adView.loadAd(adRequest);*/

        // RENDER the add on top of the scene //
        this.mRenderSurfaceView = new RenderSurfaceView(this);
        mRenderSurfaceView.setRenderer(mEngine, this);

        // SURFACE layout ? //
        final android.widget.FrameLayout.LayoutParams surfaceViewLayoutParams =
                new FrameLayout.LayoutParams(super.createSurfaceViewLayoutParams());

        // ADD the surface view and adView to the frame //
        frameLayout.addView(this.mRenderSurfaceView, surfaceViewLayoutParams);
        frameLayout.addView(adView, adViewLayoutParams);
 
        // SHOW AD //
        this.setContentView(frameLayout, frameLayoutLayoutParams);
  } // End of onSetContentView() //	

}
