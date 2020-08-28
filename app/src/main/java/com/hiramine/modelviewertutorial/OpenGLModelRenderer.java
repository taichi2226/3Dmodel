package com.hiramine.modelviewertutorial;

import android.graphics.Paint;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLModelRenderer extends OpenGLTrackRenderer {
    // メンバー変数
    private Model m_model;
    private Paint m_paintMessageTexture;
    private int         m_iMessageTextureID;
    private FloatBuffer m_fbVertexMessageTexture;
    private FloatBuffer m_fbTextureMessageTexture;


    // アクセサ
    public Model getModel() {
        return m_model;
    }

    public void setModel(Model model) {
        m_model = model;
    }

    @Override
    protected void renderScene() {
        renderModel();
    }

    protected void renderModel() {
        if (null == m_model
                || null == m_model.getVertexBuffer()) {
            return;
        }

        GL10 gl = getGL();

        // 頂点配列の有効化
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        // 頂点配列の指定
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, m_model.getVertexBuffer());

        // 面の描画
        if (null != m_model.getTriangleVertexIndexBuffer()) {
            gl.glColor4f(0.5f, 0.5f, 0.0f, 1.0f);
            gl.glDrawElements(GL10.GL_TRIANGLES,
                    m_model.getTriangleVertexIndexBuffer().capacity(),
                    GL10.GL_UNSIGNED_SHORT,
                    m_model.getTriangleVertexIndexBuffer().position(0));
        }

        // 線の描画
        if (null != m_model.getEdgeVertexIndexBuffer()) {
            gl.glLineWidth(2.0f);
            gl.glColor4f(0.0f, 0.5f, 0.5f, 1.0f);
            gl.glDrawElements(GL10.GL_LINES,
                    m_model.getEdgeVertexIndexBuffer().capacity(),
                    GL10.GL_UNSIGNED_SHORT,
                    m_model.getEdgeVertexIndexBuffer().position(0));
        }

        // 点の描画
        // if( null != m_model.getVertexBuffer() )
        {
            gl.glPointSize(5.0f);
            gl.glColor4f(0.5f, 0.0f, 0.5f, 1.0f);
            gl.glDrawArrays(GL10.GL_POINTS, 0, m_model.getVertexCount());
        }

        // 頂点配列の無効化
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);


    }

    public void createMessageTexture()
    {
        destroyMessageTexture();

        GL10  gl            = getGL();
        int[] aiTextureName = new int[1];
        gl.glGenTextures( 1, aiTextureName, 0 );
        m_iMessageTextureID = aiTextureName[0];
    }

    public void destroyMessageTexture()
    {
        if( 0 == m_iMessageTextureID )
        {
            return;
        }

        GL10  gl            = getGL();
        int[] aiTextureName = { m_iMessageTextureID };
        gl.glDeleteTextures( 1, aiTextureName, 0 );
        m_iMessageTextureID = 0;
    }

}