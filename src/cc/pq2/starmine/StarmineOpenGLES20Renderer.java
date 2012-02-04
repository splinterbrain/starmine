package cc.pq2.starmine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

public class StarmineOpenGLES20Renderer implements Renderer {
	
	private final String TAG = "STARMINEOPENGLES20RENDERER";
	
	private long lastTick;
	
	private FloatBuffer triangleVB;
	private FloatBuffer pointVB;
	private float[] velocities;
	private int mProgram;
	private int maPositionHandle;	

	private int muMVPMatrixHandle;
    private float[] mMVPMatrix = new float[16];
    private float[] mMMatrix = new float[16];
    private float[] mVMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
	
	/* @formatter:off */
	private final String vertexShaderCode = new StringBuilder()
	.append("uniform mat4 uMVPMatrix;\n")
	.append("attribute vec4 vPosition;\n")
	.append("void main(){\n")
	.append("gl_PointSize = 2.0;\n")
	.append("gl_Position = uMVPMatrix * vPosition;\n")
	.append("}\n")
	.toString();

	private final String fragmentShaderCode = new StringBuilder()
	.append("precision mediump float; \n")
	.append("void main(){\n")
	.append("gl_FragColor = vec4(1.0, 0.2, 0.8, 1.0);\n")
	.append("}\n")
	.toString();
	/* @formatter:on */

	private void initShapes() {
		/* @formatter:off */
		float triangleCoords[] = { 
				-0.5f, -0.25f, 0, 
				0.5f, -0.25f, 0, 
				0.0f, 0.559f, 0 };
		
		float[] pointCoords = new float[300];
		velocities = new float[pointCoords.length];
		Random r = new Random();
		for(int i=0;i<300;i++){
			pointCoords[i] = r.nextFloat()*2.0f-1.0f;
			Log.v(TAG,String.valueOf(pointCoords[i]));
		}
		
		float a1, a2, a3;
		float b1=0.0f, b2=0.0f, b3=1.0f;
		for(int i=0;i<100;i++){
//			b1 = 0, b2 = 0, b3 = 1
//			a1 = -x, a2 = -y, a3 = -z
//			cross = a2b3-a3b2, a3b1-a1b3, a1b2-a2b1
			a1 = pointCoords[i*3];
			a2 = pointCoords[i*3+1];
			a3 = pointCoords[i*3+2];
			velocities[i*3] = (a2*b3-a3*b2)*0.01f;
			velocities[i*3+1] = (a3*b1-a1*b3)*0.01f;
			velocities[i*3+2] = (a1*b2-a2*b1)*0.01f;
		}
		
		
		/* @formatter:on */

//		ByteBuffer vbb = ByteBuffer.allocateDirect(triangleCoords.length * 4);
//		vbb.order(ByteOrder.nativeOrder());
//		triangleVB = vbb.asFloatBuffer();
//		triangleVB.put(triangleCoords);
//		triangleVB.position(0);
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(pointCoords.length*4);
		vbb.order(ByteOrder.nativeOrder());
		pointVB = vbb.asFloatBuffer();
		pointVB.put(pointCoords);
		pointVB.position(0);
		
	}

	private int loadShader(int type, String shaderCode) {
		int shader = GLES20.glCreateShader(type);

		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		
		return shader;
		
	}

	@Override
	public void onSurfaceCreated(GL10 ridiculous, EGLConfig arg1) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		this.initShapes();
		
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, this.vertexShaderCode);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, this.fragmentShaderCode);
		
		mProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(mProgram, vertexShader);
		GLES20.glAttachShader(mProgram, fragmentShader);
		GLES20.glLinkProgram(mProgram);
		
		this.maPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
	}

	@Override
	public void onDrawFrame(GL10 ridiculous) {
		long elapsed = 0;
		long now = System.currentTimeMillis();
		if(lastTick != 0){
			elapsed = (now - lastTick);
		}
		lastTick = now;
		
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		GLES20.glUseProgram(this.mProgram);
		
//		GLES20.glVertexAttribPointer(this.maPositionHandle, 3, GLES20.GL_FLOAT, false, 12, triangleVB);
//		float[] points = pointVB.array();
		for(int i=0;i<pointVB.limit();i++){
			this.velocities[i] -= (pointVB.get(i))*0.001f;
			pointVB.put(i, pointVB.get(i) + elapsed*this.velocities[i]);
		}
		
		GLES20.glVertexAttribPointer(this.maPositionHandle, 3, GLES20.GL_FLOAT, false, 12, pointVB);
		GLES20.glEnableVertexAttribArray(maPositionHandle);
		

//		long time = SystemClock.uptimeMillis() % 4000L;
		int time = 0;
		float angle = 0.090f * time;
//		float offset = 10.0f * time;
		Matrix.setRotateM(this.mMMatrix, 0, angle, 1.0f, 0, 1.0f);
		
		Matrix.multiplyMM(this.mMVPMatrix, 0, this.mVMatrix, 0, this.mMMatrix, 0);
		Matrix.multiplyMM(this.mMVPMatrix, 0, this.mProjMatrix, 0, this.mMVPMatrix, 0);
		
		GLES20.glUniformMatrix4fv(this.muMVPMatrixHandle, 1, false, this.mMVPMatrix, 0);
		
//		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 100);
		
		
	}

	@Override
	public void onSurfaceChanged(GL10 ridiculous, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		
		float ratio = (float)width/height;
		
		Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 1, 10);
		
		this.muMVPMatrixHandle = GLES20.glGetUniformLocation(this.mProgram, "uMVPMatrix");
		
		Matrix.setLookAtM(this.mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0.0f, 1.0f, 0.0f);
	}

}
