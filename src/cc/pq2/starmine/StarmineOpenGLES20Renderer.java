package cc.pq2.starmine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.opengl.GLSurfaceView.Renderer;

public class StarmineOpenGLES20Renderer implements Renderer {
	
	private FloatBuffer triangleVB;
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
		float triangleCoords[] = { -0.5f, -0.25f, 0, 0.5f, -0.25f, 0, 0.0f,
				0.559f, 0 };

		ByteBuffer vbb = ByteBuffer.allocateDirect(triangleCoords.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		triangleVB = vbb.asFloatBuffer();
		triangleVB.put(triangleCoords);
		triangleVB.position(0);
	}

	private int loadShader(int type, String shaderCode) {
		int shader = GLES20.glCreateShader(type);

		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		
		return shader;
		
	}

	@Override
	public void onSurfaceCreated(GL10 ridiculous, EGLConfig arg1) {
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
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
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		GLES20.glUseProgram(this.mProgram);
		
		GLES20.glVertexAttribPointer(this.maPositionHandle, 3, GLES20.GL_FLOAT, false, 12, triangleVB);
		GLES20.glEnableVertexAttribArray(maPositionHandle);

		Matrix.multiplyMM(this.mMVPMatrix, 0, this.mProjMatrix, 0, this.mVMatrix, 0);
		GLES20.glUniformMatrix4fv(this.muMVPMatrixHandle, 1, false, this.mMVPMatrix, 0);
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

		
//		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 3);
		
		
	}

	@Override
	public void onSurfaceChanged(GL10 ridiculous, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		
		float ratio = (float)width/height;
		
		Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
		
		this.muMVPMatrixHandle = GLES20.glGetUniformLocation(this.mProgram, "uMVPMatrix");
		
		Matrix.setLookAtM(this.mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0.0f, 1.0f, 0.0f);
	}

}
