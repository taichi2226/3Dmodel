package com.hiramine.modelviewertutorial;

import android.opengl.GLU;
import android.opengl.Matrix;

import javax.microedition.khronos.opengles.GL10;

public class OpenGLTrackRenderer extends OpenGLBaseRenderer {
    private static final android.opengl.GLU GLU = null;

    // トラッキングモード
    enum ETrackingMode
    {
        TM_NONE,
        TM_ROTATE,
        TM_PAN,
        TM_ZOOM,
    }

    // メンバー変数
    private ETrackingMode m_eTrackingMode = ETrackingMode.TM_NONE;    // トラッキングモード
    private float m_fLastX;                                    // トラッキング中の直前座標X
    private float m_fLastY;                                    // トラッキング中の直前座標Y
    private float m_fRenderingRate;                                // 描画倍率
    protected float[] m_f16ObjectForm = new float[16];            // オブジェクトフォーム
    protected float m_fRenderingCenterX;                            // 描画中心座標X
    protected float m_fRenderingCenterY;                            // 描画中心座標Y
    private float[] m_f16MatrixTemp1 = new float[16];            // テンポラリ行列
    private float[] m_f16MatrixTemp2 = new float[16];            // テンポラリ行列

    // コンストラクタ
    public OpenGLTrackRenderer()
    {
        m_fRenderingRate = 10.0f;
        Matrix.setIdentityM( m_f16ObjectForm, 0 );
    }

    // アクセサ
    public ETrackingMode getTrackingMode()
    {
        return m_eTrackingMode;
    }

    // トラッキング操作の開始
    public void beginTracking( float fX, float fY, ETrackingMode eTrackingMode )
    {
        m_eTrackingMode = eTrackingMode;
        m_fLastX = fX;
        m_fLastY = fY;
    }

    // トラッキング操作の終了
    public void endTracking()
    {
        m_eTrackingMode = ETrackingMode.TM_NONE;
    }

    // トラッキング操作
    public void doTracking( float x, float y )
    {
        float deltaX = x - m_fLastX;
        float deltaY = y - m_fLastY;
        m_fLastX = x;
        m_fLastY = y;
        if( 0 == deltaX && 0 == deltaY )
        {
            return;
        }
        switch( m_eTrackingMode )
        {
            case TM_NONE:
                break;
            case TM_ROTATE:
            {
                // クライアント領域の縦横の短い方の長さ分のピクセル動かすと半周（180度）回るように
                float fAngle_deg = (float)( Math.sqrt( deltaX * deltaX + deltaY * deltaY ) * 180.0 / ( getWidth() < getHeight() ? getWidth() : getHeight() ) );
                // 回転行列の計算
                Matrix.setRotateM( m_f16MatrixTemp1, 0, fAngle_deg, deltaY, deltaX, 0.0f );
                // フォーム行列に回転行列をかける
                Matrix.multiplyMM( m_f16MatrixTemp2, 0, m_f16MatrixTemp1, 0, m_f16ObjectForm, 0 );
                // フォーム行列の更新（Matrix.multiplyMM関数は、入力行列と結果行列がオーバーラップする場合、結果は未定という仕様なので）
                System.arraycopy( m_f16MatrixTemp2, 0, m_f16ObjectForm, 0, 16 );
                // 視点座標変換設定の無効化（描画処理時に再設定され、有効化される）
                setViewingTransformValid( false );
            }
            break;
            case TM_PAN:
            {
                m_fRenderingCenterX -= deltaX / m_fRenderingRate;
                m_fRenderingCenterY += deltaY / m_fRenderingRate;
                // 視点座標変換設定の無効化（描画処理時に再設定され、有効化される）
                setViewingTransformValid( false );
            }
            break;
            case TM_ZOOM:
            { // 拡大・縮小による描画倍率操作
                // +500で倍率2倍に、+250で倍率1.5倍に、
                // -500で倍率0.5倍に、-1000で倍率0.25倍に
                m_fRenderingRate *= (float)Math.pow( 2.0, -deltaY * 0.002 );
                // 視野角錐台設定の無効化（描画処理時に再設定され、有効化される）
                setViewingFrustumValid( false );
            }
            break;
        }
    }

    // 視野角錐台設定
    @Override
    protected void setupViewingFrustum()
    {
        GL10 gl = getGL();
        gl.glMatrixMode( GL10.GL_PROJECTION );
        gl.glLoadIdentity();
        gl.glOrthof( -getWidth() * 0.5f / m_fRenderingRate, // left
                getWidth() * 0.5f / m_fRenderingRate, // right
                -getHeight() * 0.5f / m_fRenderingRate, // bottom
                getHeight() * 0.5f / m_fRenderingRate, // top
                0.1f, // near
                1000.0f ); // far
        gl.glMatrixMode( GL10.GL_MODELVIEW );

        setViewingFrustumValid( true );
    }

    // 視点座標変換
    @Override
    protected void setupViewingTransform()
    {
        GL10 gl = getGL();
        gl.glMatrixMode( GL10.GL_MODELVIEW );
        gl.glLoadIdentity();
        GLU.gluLookAt( gl,
                m_fRenderingCenterX, m_fRenderingCenterY, 500,
                m_fRenderingCenterX, m_fRenderingCenterY, 0.0f,
                0.0f, 1.0f, 0.0f );
        gl.glMultMatrixf( m_f16ObjectForm, 0 ); // 表示回転（＝モデル回転）

        setViewingTransformValid( true );
    }
}
