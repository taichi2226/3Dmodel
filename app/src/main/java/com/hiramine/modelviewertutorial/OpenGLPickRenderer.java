package com.hiramine.modelviewertutorial;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

public class OpenGLPickRenderer extends OpenGLModelRenderer {
    enum ERenderMode
    {
        RM_RENDER,
        RM_PICK_ELEMENTTYPE,
        RM_PICK_ELEMENTID,
    }

    enum ERenderElementType
    {
        RET_POINT( 1 ),
        RET_LINE( 2 ),
        RET_FACE( 3 );

        private int value;

        ERenderElementType( int i )
        {
            value = i;
        }

        public int getValue()
        {
            return value;
        }
    }

    // 定数
    protected static final int NAMEARRAYSIZE    = 10;    // 名前列の大きさ
    private static final   int REDMASK          = 0xF800;
    private static final   int GREENMASK        = 0x7E0;
    private static final   int BLUEMASK         = 0x1F;

    protected static final int PICKREGIONOFFSET = 10;    // ピック領域の上下左右のオフセット量

    // メンバー変数
    protected int[] m_aiName = new int[NAMEARRAYSIZE];
    private ByteBuffer m_btbVertexIdColor;
    private ByteBuffer m_btbTriangleIdColor;

    // アクセサ
    public ByteBuffer getVertexIdColorBuffer()
    {
        return m_btbVertexIdColor;
    }

    public ByteBuffer getTriangleIdColorBuffer()
    {
        return m_btbTriangleIdColor;
    }

    protected void index2rgb( int iIndex, byte[] abtRGB )
    {
        abtRGB[0] = (byte)( ( iIndex & REDMASK ) >> 11 << 3 );// 赤は、5ビット、32階調
        abtRGB[1] = (byte)( ( iIndex & GREENMASK ) >> 5 << 2 );// 緑は、6ビット、64階調
        abtRGB[2] = (byte)( ( iIndex & BLUEMASK ) << 3 );// 青は、5ビット、32階調
    }

    @Override
    public void setModel( Model model )
    {
        Arrays.fill( m_aiName, 0 );

        super.setModel( model );

        if( null == model )
        {
            m_btbVertexIdColor = null;
            m_btbTriangleIdColor = null;
            return;
        }

        byte[] abtRGB = { 0, 0, 0 };

        int    iCountPoint      = model.getVertexCount();
        byte[] abt4PointIdColor = new byte[iCountPoint * 4];
        for( int i = 0; i < iCountPoint; i++ )
        {
            index2rgb( i, abtRGB );
            abt4PointIdColor[i * 4 + 0] = abtRGB[0];
            abt4PointIdColor[i * 4 + 1] = abtRGB[1];
            abt4PointIdColor[i * 4 + 2] = abtRGB[2];
            abt4PointIdColor[i * 4 + 3] = (byte)255;
        }
        m_btbVertexIdColor = makeByteBuffer( abt4PointIdColor );

        int    iCountTriangle      = model.getTriangleCount();
        byte[] abt4TriangleIdColor = new byte[iCountTriangle * 3 * 4];
        for( int i = 0; i < iCountTriangle; i++ )
        {
            index2rgb( i, abtRGB );
            abt4TriangleIdColor[i * 12 + 0] = abtRGB[0];
            abt4TriangleIdColor[i * 12 + 1] = abtRGB[1];
            abt4TriangleIdColor[i * 12 + 2] = abtRGB[2];
            abt4TriangleIdColor[i * 12 + 3] = (byte)255;
            abt4TriangleIdColor[i * 12 + 4] = abtRGB[0];
            abt4TriangleIdColor[i * 12 + 5] = abtRGB[1];
            abt4TriangleIdColor[i * 12 + 6] = abtRGB[2];
            abt4TriangleIdColor[i * 12 + 7] = (byte)255;
            abt4TriangleIdColor[i * 12 + 8] = abtRGB[0];
            abt4TriangleIdColor[i * 12 + 9] = abtRGB[1];
            abt4TriangleIdColor[i * 12 + 10] = abtRGB[2];
            abt4TriangleIdColor[i * 12 + 11] = (byte)255;
        }
        m_btbTriangleIdColor = makeByteBuffer( abt4TriangleIdColor );
    }
    @Override
    protected void renderScene()
    {
        renderModel( ERenderMode.RM_RENDER );
    }

    protected void renderModel( ERenderMode eRenderMode )
    {
        Model model = getModel();
        if( null == model
                || null == model.getVertexBuffer() )
        {
            return;
        }

        GL10 gl = getGL();

        if( !( gl instanceof GL11) )
        {
            return;
        }
        GL11 gl11 = (GL11)gl;

        // 頂点配列の有効化
        gl.glEnableClientState( GL10.GL_VERTEX_ARRAY );

        // 頂点配列の指定
        gl.glVertexPointer( 3, GL10.GL_FLOAT, 0, model.getVertexBuffer() );

        // 面の描画
        if( null != model.getTriangleVertexIndexBuffer() )
        {
            if( ERenderMode.RM_PICK_ELEMENTID == eRenderMode )
            {
                gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
                gl.glColorPointer( 4, // Must be 4.
                        GL10.GL_UNSIGNED_BYTE,
                        0,
                        m_btbTriangleIdColor );
            }
            else if( ERenderMode.RM_PICK_ELEMENTTYPE == eRenderMode )
            {
                gl.glColor4f( 1.0f, 0.0f, 0.0f, 1.0f );
            }
            else
            {
                gl.glColor4f( 0.5f, 0.5f, 0.0f, 1.0f );
            }
            gl.glDrawElements( GL10.GL_TRIANGLES,
                    model.getTriangleVertexIndexBuffer().capacity(),
                    GL10.GL_UNSIGNED_SHORT,
                    model.getTriangleVertexIndexBuffer().position( 0 ) );
            gl.glDisableClientState( GL10.GL_COLOR_ARRAY );
            // ピック面の描画
            if( ERenderMode.RM_RENDER == eRenderMode )
            {
                if( ERenderElementType.RET_FACE.getValue() == m_aiName[1] )
                {
                    int iIndexTriangle = m_aiName[2];
                    gl.glColor4f( 1.0f, 1.0f, 0.0f, 1.0f );
                    gl.glDrawElements( GL10.GL_TRIANGLES,
                            3,
                            GL10.GL_UNSIGNED_SHORT,
                            model.getTriangleVertexIndexBuffer().position( 3 * iIndexTriangle ) );
                }
            }
        }

        // 線の描画
        if( null != model.getEdgeVertexIndexBuffer() )
        {
            gl.glLineWidth( 2.0f );
            if( ERenderMode.RM_PICK_ELEMENTID == eRenderMode )
            {
                int    iCountTriangle = model.getTriangleCount();
                int    iIndexTriangle;
                int    i3;
                int    iIndexEdge;
                byte[] abtRGB         = { 0, 0, 0 };
                for( iIndexTriangle = 0; iIndexTriangle < iCountTriangle; ++iIndexTriangle )
                {
                    for( i3 = 0; i3 < 3; ++i3 )
                    {
                        iIndexEdge = iIndexTriangle * 3 + i3;
                        index2rgb( iIndexEdge, abtRGB );
                        gl11.glColor4ub( abtRGB[0], abtRGB[1], abtRGB[2], (byte)255 );
                        gl.glDrawElements( GL10.GL_LINES,
                                2,
                                GL10.GL_UNSIGNED_SHORT,
                                model.getEdgeVertexIndexBuffer().position( 2 * iIndexEdge ) );
                    }
                }
            }
            else
            {
                if( ERenderMode.RM_PICK_ELEMENTTYPE == eRenderMode )
                {
                    gl.glColor4f( 0.0f, 1.0f, 0.0f, 1.0f );
                }
                else
                {
                    gl.glColor4f( 0.0f, 0.5f, 0.5f, 1.0f );
                }
                gl.glDrawElements( GL10.GL_LINES,
                        model.getEdgeVertexIndexBuffer().capacity(),
                        GL10.GL_UNSIGNED_SHORT,
                        model.getEdgeVertexIndexBuffer().position( 0 ) );
            }
            // ピック線の描画
            if( ERenderMode.RM_RENDER == eRenderMode )
            {
                if( ERenderElementType.RET_LINE.getValue() == m_aiName[1] )
                {
                    int iIndexEdge = m_aiName[2];
                    gl.glLineWidth( 5.0f );
                    gl.glColor4f( 0.0f, 1.0f, 1.0f, 1.0f );
                    gl.glDrawElements( GL10.GL_LINES,
                            2,
                            GL10.GL_UNSIGNED_SHORT,
                            model.getEdgeVertexIndexBuffer().position( 2 * iIndexEdge ) );
                }
            }
        }

        // 点の描画
        // if( null != model.getVertexBuffer() )
        {
            gl.glPointSize( 5.0f );
            if( ERenderMode.RM_PICK_ELEMENTID == eRenderMode )
            {
                gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
                gl.glColorPointer( 4, // Must be 4.
                        GL10.GL_UNSIGNED_BYTE,
                        0,
                        m_btbVertexIdColor );
            }
            else if( ERenderMode.RM_PICK_ELEMENTTYPE == eRenderMode )
            {
                gl.glColor4f( 0.0f, 0.0f, 1.0f, 1.0f );
            }
            else
            {
                gl.glColor4f( 0.5f, 0.0f, 0.5f, 1.0f );
            }
            gl.glDrawArrays( GL10.GL_POINTS, 0, model.getVertexCount() );
            gl.glDisableClientState( GL10.GL_COLOR_ARRAY );
            // ピック点の描画
            if( ERenderMode.RM_RENDER == eRenderMode )
            {
                if( ERenderElementType.RET_POINT.getValue() == m_aiName[1] )
                {
                    gl.glPointSize( 10.0f );
                    gl.glColor4f( 1.0f, 0.0f, 1.0f, 1.0f );
                    int iIndexPoint = m_aiName[2];
                    gl.glDrawArrays( GL10.GL_POINTS, iIndexPoint, 1 );
                }
            }
        }

        // 頂点配列の無効化
        gl.glDisableClientState( GL10.GL_VERTEX_ARRAY );
    }

    private int rgb2index( byte r, byte g, byte b )
    {
        // (符号付きbyte値 & 0xFF) で、符号なしbyte値が得られる
        return ( ( ( r & 0xFF ) >> 3 ) << 11 )
                + ( ( ( g & 0xFF ) >> 2 ) << 5 )
                + ( ( b & 0xFF ) >> 3 );
    }

    // 「要素タイプの優先順位は、点、線、面
    // 　同じ要素タイプの場合は、ピック領域の中心に近いものが優先」
    //  に従い、ピックピクセルを一つに絞る
    protected void DecidePickNameArray( int[][] aaiName )
    {
        int  iId_selected          = -1;
        int  iElementType_selected = ERenderElementType.RET_FACE.getValue() + 1;
        long lSquareDist_selected  = ( 2 + PICKREGIONOFFSET ) * ( 2 + PICKREGIONOFFSET ) + ( 2 + PICKREGIONOFFSET ) * ( 2 + PICKREGIONOFFSET );
        long lSquareDist_current;
        int  x;
        int  y;
        for( int i = 0; i < aaiName.length; ++i )
        {
            if( 0 == aaiName[i][1] )
            { // モデルの外側
                continue;
            }
            if( iElementType_selected < aaiName[i][1] )
            { // 要素タイプ的に、優先順位が低い
                continue;
            }
            if( iElementType_selected > aaiName[i][1] )
            { // 要素タイプ的に、優先順位が低い
                iId_selected = i;
                iElementType_selected = aaiName[i][1];
                x = i % ( 1 + 2 * PICKREGIONOFFSET ) - PICKREGIONOFFSET;
                y = i / ( 1 + 2 * PICKREGIONOFFSET ) - PICKREGIONOFFSET;
                lSquareDist_selected = x * x + y * y;
                continue;
            }
            // 要素タイプ的に、優先順位が同じ場合は、ピック領域の中心に近いものが優先度が高い。
            x = i % ( 1 + 2 * PICKREGIONOFFSET ) - PICKREGIONOFFSET;
            y = i / ( 1 + 2 * PICKREGIONOFFSET ) - PICKREGIONOFFSET;
            lSquareDist_current = x * x + y * y;
            if( lSquareDist_selected > lSquareDist_current )
            {
                iId_selected = i;
                iElementType_selected = aaiName[i][1];
                lSquareDist_selected = lSquareDist_current;
                continue;
            }
        }
        // 名前列メンバの更新
        System.arraycopy( aaiName[iId_selected], 0, m_aiName, 0, NAMEARRAYSIZE );
    }

    public boolean doPicking( float fX, float fY )
    {
        if( null == getModel() )
        {
            return false;
        }

        GL10 gl = getGL();

        if( !( gl instanceof GL11ExtensionPack) )
        {
            return false;
        }
        GL11ExtensionPack gl11ex = (GL11ExtensionPack)gl;

        // テクスチャの生成
        int[] aiTexture = { 0 };
        gl.glGenTextures( 1, aiTexture, 0 );
        gl.glBindTexture( GL10.GL_TEXTURE_2D, aiTexture[0] );
        gl.glTexImage2D( GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, getWidth(), getHeight(), 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, null );
        gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST ); // 縮小は、もっとも近い要素で補完
        gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR ); // 拡大は、線形で補完
        gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE ); // 画像の端のピクセルの色を使う
        gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE ); // 画像の端のピクセルの色を使う
        gl.glBindTexture( GL10.GL_TEXTURE_2D, 0 );

        // レンダーバッファの生成
        int[] aiRenderBuffer = { 0 };
        gl11ex.glGenRenderbuffersOES( 1, aiRenderBuffer, 0 );
        // レンダーバッファのバインド
        gl11ex.glBindRenderbufferOES( GL11ExtensionPack.GL_RENDERBUFFER_OES, aiRenderBuffer[0] );
        // レンダーバッファの深度、サイズの設定
        gl11ex.glRenderbufferStorageOES( GL11ExtensionPack.GL_RENDERBUFFER_OES,
                GL11ExtensionPack.GL_DEPTH_COMPONENT16, // 「GL_DEPTH_COMPONENT」では、glCheckFramebufferStatusOESでINCOMPLETEになる。
                getWidth(),
                getHeight() );
        gl11ex.glBindRenderbufferOES( GL11ExtensionPack.GL_RENDERBUFFER_OES, 0 ); // バインド解除

        // フレームバッファの生成
        int[] aiFrameBuffer = { 0 };
        gl11ex.glGenFramebuffersOES( 1, aiFrameBuffer, 0 );
        // フレームバッファのバインド
        gl11ex.glBindFramebufferOES( GL11ExtensionPack.GL_FRAMEBUFFER_OES, aiFrameBuffer[0] );

        // カラーアタッチメントとしてテクスチャを指定する
        gl11ex.glFramebufferTexture2DOES( GL11ExtensionPack.GL_FRAMEBUFFER_OES,
                GL11ExtensionPack.GL_COLOR_ATTACHMENT0_OES,
                GL10.GL_TEXTURE_2D,
                aiTexture[0],
                0 );

        // 深度アタッチメントとしてレンダーバッファを指定する
        gl11ex.glFramebufferRenderbufferOES( GL11ExtensionPack.GL_FRAMEBUFFER_OES,
                GL11ExtensionPack.GL_DEPTH_ATTACHMENT_OES,
                GL11ExtensionPack.GL_RENDERBUFFER_OES,
                aiRenderBuffer[0] );

        int iCountHit = 0;
        int iStatus   = gl11ex.glCheckFramebufferStatusOES( GL11ExtensionPack.GL_FRAMEBUFFER_OES );
        if( GL11ExtensionPack.GL_FRAMEBUFFER_COMPLETE_OES == iStatus )
        {
            int[][] aaiName = new int[( 1 + 2 * PICKREGIONOFFSET ) * ( 1 + 2 * PICKREGIONOFFSET )][];
            for( int i = 0; i < aaiName.length; ++i )
            {
                aaiName[i] = new int[NAMEARRAYSIZE];
            }
            byte r;
            byte g;
            byte b;
            // ピック領域の色の取得（１ピクセルは、4つのbyteデータ(r,g,b,a)）
            byte[]     abtPixel = new byte[4 * ( 1 + 2 * PICKREGIONOFFSET ) * ( 1 + 2 * PICKREGIONOFFSET )];
            ByteBuffer btbPixel = makeByteBuffer( abtPixel );

            gl.glDisable( GL10.GL_LIGHTING ); // 光
            gl.glDisable( GL10.GL_BLEND ); // 半透明およびアンチエイリアシング

            // ピック描画（要素タイプ別）
            gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );
            renderModel( ERenderMode.RM_PICK_ELEMENTTYPE );
            gl.glReadPixels( (int)fX - PICKREGIONOFFSET,
                    getHeight() - (int)fY - PICKREGIONOFFSET,
                    1 + 2 * PICKREGIONOFFSET,
                    1 + 2 * PICKREGIONOFFSET,
                    GL10.GL_RGBA,
                    GL10.GL_UNSIGNED_BYTE,
                    btbPixel );
            // ピック領域の色をピック名配列に変換
            for( int i = 0; i < aaiName.length; ++i )
            {
                r = btbPixel.get( i * 4 + 0 );
                g = btbPixel.get( i * 4 + 1 );
                b = btbPixel.get( i * 4 + 2 );
                if( (byte)255 == r )
                { // 面
                    aaiName[i][1] = ERenderElementType.RET_FACE.getValue();
                    ++iCountHit;
                }
                else if( (byte)255 == g )
                { // 線
                    aaiName[i][1] = ERenderElementType.RET_LINE.getValue();
                    ++iCountHit;
                }
                else if( (byte)255 == b )
                { // 点
                    aaiName[i][1] = ERenderElementType.RET_POINT.getValue();
                    ++iCountHit;
                }
            }
            if( 0 != iCountHit )
            { // ヒットあり
                // ピック描画（要素番号別）
                gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );
                renderModel( ERenderMode.RM_PICK_ELEMENTID );
                gl.glReadPixels( (int)fX - PICKREGIONOFFSET,
                        getHeight() - (int)fY - PICKREGIONOFFSET,
                        1 + 2 * PICKREGIONOFFSET,
                        1 + 2 * PICKREGIONOFFSET,
                        GL10.GL_RGBA,
                        GL10.GL_UNSIGNED_BYTE,
                        btbPixel );
                // ピック領域の色をピック名配列に変換
                for( int i = 0; i < aaiName.length; ++i )
                {
                    r = btbPixel.get( i * 4 + 0 );
                    g = btbPixel.get( i * 4 + 1 );
                    b = btbPixel.get( i * 4 + 2 );
                    // 色を番号に変換し、名前配列にセット
                    aaiName[i][2] = rgb2index( r, g, b );
                }

                // ピックピクセルを一つに絞って、名前列メンバの更新
                DecidePickNameArray( aaiName );
            }
        }

        // ウィンドウシステムが提供するフレームバッファに差し替え
        gl11ex.glBindFramebufferOES( GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0 );

        // クリーンアップ
        gl.glDeleteTextures( 1, aiTexture, 0 );
        gl11ex.glDeleteRenderbuffersOES( 1, aiRenderBuffer, 0 );
        gl11ex.glDeleteFramebuffersOES( 1, aiFrameBuffer, 0 );

        return ( 0 != iCountHit );
    }
}
