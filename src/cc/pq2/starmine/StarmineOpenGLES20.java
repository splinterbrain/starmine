package cc.pq2.starmine;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class StarmineOpenGLES20 extends Activity {
	private GLSurfaceView mGLView;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		mGLView = new StarmineOpenGLES20SurfaceView(this);
		setContentView(mGLView);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		mGLView.onPause();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		mGLView.onResume();
	}
}

class StarmineOpenGLES20SurfaceView extends GLSurfaceView{
	public StarmineOpenGLES20SurfaceView(Context context){
		super(context);
		
		setEGLContextClientVersion(2);
		setRenderer(new StarmineOpenGLES20Renderer());
	}
}