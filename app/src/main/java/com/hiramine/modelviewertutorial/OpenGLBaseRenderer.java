package com.hiramine.modelviewertutorial;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLBaseRenderer implements GLSurfaceView.Renderer {

    private static final android.opengl.GLU GLU = null;

    // static関数
    // byteバッファーの作成
    public static ByteBuffer makeByteBuffer(byte[] arr )
    {
        ByteBuffer bb = ByteBuffer.allocateDirect( arr.length * SIZEOF_BYTE ); // byte:1byte
        bb.order( ByteOrder.nativeOrder() );
        bb.put( arr );
        bb.position( 0 );
        return bb;
    }

    // shortバッファーの作成
    public static ShortBuffer makeShortBuffer(short[] arr )
    {
        ByteBuffer bb = ByteBuffer.allocateDirect( arr.length * SIZEOF_SHORT ); // short:2byte
        bb.order( ByteOrder.nativeOrder() );
        ShortBuffer sb = bb.asShortBuffer();
        sb.put( arr );
        sb.position( 0 );
        return sb;
    }

    // floatバッファーの作成
    public static FloatBuffer makeFloatBuffer(float[] arr )
    {
        ByteBuffer bb = ByteBuffer.allocateDirect( arr.length * SIZEOF_FLOAT );
        bb.order( ByteOrder.nativeOrder() );
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put( arr );
        fb.position( 0 );
        return fb;
    }

    // 定数
    private static final int SIZEOF_BYTE  = Byte.SIZE / 8;    // Byte.SIZEで、byte型のビット数が得られるので、8で割って、バイト数を得る
    private static final int SIZEOF_SHORT = Short.SIZE / 8;    // Short.SIZEで、short型のビット数が得られるので、8で割って、バイト数を得る
    private static final int SIZEOF_FLOAT = Float.SIZE / 8;    // Float.SIZEで、float型のビット数が得られるので、8で割って、バイト数を得る
    // メンバー変数
    private GL10 m_gl;    // OpenGLオブジェクト
    private FloatBuffer m_fbVertex;                        // 軸の頂点
    private ByteBuffer  m_fbColor;

    // コンストラクタ
    public OpenGLBaseRenderer()
    {
        float[] af3Vertex = { 0.0f, 0.0f, 0.0f,
                10.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 10.0f, 0.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 10.0f, };
        byte[] abt3Color = { (byte)255, 0, 0, (byte)255,
                (byte)255, 0, 0, (byte)255,
                0, (byte)255, 0, (byte)255,
                0, (byte)255, 0, (byte)255,
                0, 0, (byte)255, (byte)255,
                0, 0, (byte)255, (byte)255, };
        m_fbVertex = makeFloatBuffer( af3Vertex );
        m_fbColor = makeByteBuffer( abt3Color );
    }



    private int m_iWidth;    // サーフェースサイズ
    private int m_iHeight;    // サーフェースサイズ
    private boolean m_bViewingFrustumValid;            // 視野角錐台設定の有効性
    private boolean m_bViewingTransformValid;            // 視点座標変換設定の有効性


    // アクセサ
    public GL10 getGL() {
        return m_gl;
    }

    public int getWidth() {
        return m_iWidth;
    }

    public int getHeight() {
        return m_iHeight;
    }

    public boolean isViewingFrustumValid() {
        return m_bViewingFrustumValid;
    }

    public boolean isViewingTransformValid() {
        return m_bViewingTransformValid;
    }

    void setViewingFrustumValid(boolean arg) {
        m_bViewingFrustumValid = arg;
    }

    void setViewingTransformValid(boolean arg) {
        m_bViewingTransformValid = arg;
    }

    // サーフェースが作成された時、再作成された時
    // （レンダリングスレッドの起動時および、アンドロイドデバイスのスリープからの復帰時）
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        m_gl = gl;

        // クリア処理
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // クリアカラー
        gl.glClearDepthf(1.0f); // クリア深度

        // デプス処理
        gl.glEnable(GL10.GL_DEPTH_TEST); // デプステスト
        gl.glDepthFunc(GL10.GL_LEQUAL); // デプスファンクの設定（同じか、手前にあるもので上描いていく）
        gl.glDepthMask(true); // デプスバッファーへの書き込み許可

        // ポリゴン処理
        gl.glDisable(GL10.GL_CULL_FACE); // 裏を向いている面のカリング
        gl.glEnable(GL10.GL_NORMALIZE); // 法線処理
        gl.glDisable(GL10.GL_POLYGON_OFFSET_FILL); // ポリゴンオフセットフィル
        gl.glPolygonOffset(1.0f, 1.0f); // ポリゴンオフセット量

        // ライティング処理
        gl.glDisable(GL10.GL_LIGHTING); // 光源
        gl.glDisable(GL10.GL_COLOR_MATERIAL); // カラー設定値をマテリアルとして使用

        // ブレンド処理
        gl.glDisable(GL10.GL_BLEND); // 半透明およびアンチエイリアシング
        gl.glDisable(GL10.GL_POINT_SMOOTH); // 点のスムース処理
        gl.glDisable(GL10.GL_LINE_SMOOTH); // 線のスムース処理
        gl.glShadeModel(GL10.GL_SMOOTH); // シェーディングモード
    }

    // サーフェースが変更された時
    // （サーフェースが作成された後および、サーフェースのサイズが変更された時）
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        m_iWidth = width;
        m_iHeight = height;

        // ビューポート設定
        setupViewport();

        // 視野角錐台設定の無効化（描画処理時に再設定され、有効化される）
        setViewingFrustumValid(false);

        // 視点座標変換設定の無効化（描画処理時に再設定され、有効化される）
        setViewingTransformValid(false);
    }

    // ビューポート設定
    private void setupViewport() {
        m_gl.glViewport(0, 0, m_iWidth, m_iHeight);


    }
    // フレームの描画
    @Override
    public void onDrawFrame( GL10 gl )
    {
        gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );

        if( !isViewingFrustumValid() )
        {
            setupViewingFrustum();
        }

        if( !isViewingTransformValid() )
        {
            setupViewingTransform();
        }

        preRenderScene();

        gl.glPushMatrix();
        renderStockScene();
        gl.glPopMatrix();

        gl.glPushMatrix();
        renderScene();
        gl.glPopMatrix();

        postRenderScene();
    }

    // 視野角錐台設定
    protected void setupViewingFrustum()
    {
        m_gl.glMatrixMode( GL10.GL_PROJECTION );
        m_gl.glLoadIdentity();
        m_gl.glOrthof( -m_iWidth * 0.5f / 10.0f,
                m_iWidth * 0.5f / 10.0f,
                -m_iHeight * 0.5f / 10.0f,
                m_iHeight * 0.5f / 10.0f,
                0.1f,
                1000.0f );
        m_gl.glMatrixMode( GL10.GL_MODELVIEW );

        setViewingFrustumValid( true );
    }

    // 視点座標変換設定
    protected void setupViewingTransform()
    {
        m_gl.glMatrixMode( GL10.GL_MODELVIEW );
        m_gl.glLoadIdentity();
        GLU.gluLookAt( m_gl,
                0.0f, 0.0f, 500,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f );

        setViewingTransformValid( true );
    }

    // シーン描画前の処理
    protected void preRenderScene()
    {
    }

    // シーン描画後の処理
    protected void postRenderScene()
    {
    }

    // ストックシーンの描画
    protected void renderStockScene()
    {
        renderAxis();
    }

    // シーンの描画
    protected void renderScene()
    {
    }

    // 軸の描画
    private void renderAxis()
    {
        m_gl.glEnableClientState( GL10.GL_VERTEX_ARRAY );
        m_gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
        m_gl.glVertexPointer( 23, GL10.GL_FLOAT, 0, m_fbVertex );
        m_gl.glColorPointer( 24, // Must be 4.
                GL10.GL_UNSIGNED_BYTE,
                0,
                m_fbColor );

        m_gl.glLineWidth( 2.0f );
        m_gl.glDrawArrays( GL10.GL_LINES, 0, 6 );

        m_gl.glDisableClientState( GL10.GL_VERTEX_ARRAY );
        m_gl.glDisableClientState( GL10.GL_COLOR_ARRAY );
    }

    // サーフェースがが破棄されようとする時
    public void preSurfaceDestroy()
    {
    }
}
