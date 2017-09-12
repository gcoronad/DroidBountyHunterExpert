package training.edu.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import training.edu.droidbountyhunter.ActivityOpenGLFugitivos;
import training.edu.droidbountyhunter.R;

/**
 * Created by gcoronad on 08/09/2017.
 */

public class SimpleRender implements GLSurfaceView.Renderer {

    private Context context;
    private FloatBuffer vertexBuffer;
    private FloatBuffer texturaBuffer;
    private ShortBuffer indexBuffer;
    private int carasLength;

    public SimpleRender(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d("AR", "textura modificada " + width + "x" + height);
        float positivo = ActivityOpenGLFugitivos.distorsion;
        float negativo = ActivityOpenGLFugitivos.distorsion * -1.0f;

        float vertices[] = {
                negativo, 1f, 0f,
                -1f, -1f, 0f,
                0f, -1f, 0f,
                1f, -1f, 0f,
                positivo, 1f, 0f
        };

        short caras[] = {
                0, 1, 2,
                0, 2, 4,
                4, 2, 3
        };

        carasLength = caras.length;

        float textura[] = {
                0f, 0f,
                0f, 1f,
                0.5f, 1f,
                1f, 1f,
                1f, 0f
        };

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        ByteBuffer tbb = ByteBuffer.allocateDirect(vertices.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        texturaBuffer = tbb.asFloatBuffer();
        texturaBuffer.put(textura);
        texturaBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(vertices.length * 2);
        cbb.order(ByteOrder.nativeOrder());
        indexBuffer = cbb.asShortBuffer();
        indexBuffer.put(caras);
        indexBuffer.position(0);

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45.0f, (float) width/(float)height, 0.1f, 100);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glTranslatef(0,0,-7);
        draw(gl);
    }

    public void draw(GL10 gl){
        cargarTextura(gl);

        gl.glFrontFace(GL10.GL_CCW);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texturaBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, carasLength, GL10.GL_UNSIGNED_SHORT, indexBuffer);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    public void cargarTextura(GL10 gl10) {
        Bitmap bitmap;
        if (ActivityOpenGLFugitivos.defaultValue.equalsIgnoreCase("0")) {
            bitmap = PictureTools.decodeSampledBitmapFromUri(ActivityOpenGLFugitivos.foto, 200, 200);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        }

        int[] textureIdS = new int[1];
        gl10.glGenTextures(1, textureIdS, 0);
        int texId = textureIdS[0];
        gl10.glEnable(GL10.GL_TEXTURE_2D);
        gl10.glBindTexture(GL10.GL_TEXTURE_2D, texId);
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
    }
}
