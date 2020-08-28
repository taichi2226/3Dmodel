package com.hiramine.modelviewertutorial;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Model {
    // メンバー変数
    private FloatBuffer m_fbVertex;                // 頂点の座標値の配列（３つの座標値で１頂点）
    private ShortBuffer m_sbTriangleVertexIndex;    // 三角形の頂点の番号の配列（３つの頂点番号で１三角形）（unsigned shortの上限は65535）
    private ShortBuffer m_sbEdgeVertexIndex;        // 稜線の番号配列（２つの頂点番号で１稜線）（unsigned shortの上限は65535）

    // コンストラクタ
    public Model( float[] af3Vertex )
    {
        m_fbVertex = OpenGLBaseRenderer.makeFloatBuffer( af3Vertex );

        // 頂点の数
        int iCountPoint = af3Vertex.length / 3;
        // 三角形の数
        int iCountTriangle = iCountPoint / 3;

        short[] asTriangleVertexIndex = new short[iCountTriangle * 3];
        for( int iIndexTriangle = 0; iIndexTriangle < iCountTriangle; iIndexTriangle++ )
        {
            asTriangleVertexIndex[iIndexTriangle * 3 + 0] = (short)( iIndexTriangle * 3 + 0 );
            asTriangleVertexIndex[iIndexTriangle * 3 + 1] = (short)( iIndexTriangle * 3 + 1 );
            asTriangleVertexIndex[iIndexTriangle * 3 + 2] = (short)( iIndexTriangle * 3 + 2 );
        }
        m_sbTriangleVertexIndex = OpenGLBaseRenderer.makeShortBuffer( asTriangleVertexIndex );

        // 稜線の数は、三角形の数の３倍。稜線の頂点の数は、稜線の数の２倍
        short[] asEdgeVertexIndex = new short[iCountTriangle * 3 * 2];
        for( int iIndexTriangle = 0; iIndexTriangle < iCountTriangle; iIndexTriangle++ )
        {
            asEdgeVertexIndex[iIndexTriangle * 6 + 0] = asTriangleVertexIndex[iIndexTriangle * 3 + 0];
            asEdgeVertexIndex[iIndexTriangle * 6 + 1] = asTriangleVertexIndex[iIndexTriangle * 3 + 1];
            asEdgeVertexIndex[iIndexTriangle * 6 + 2] = asTriangleVertexIndex[iIndexTriangle * 3 + 1];
            asEdgeVertexIndex[iIndexTriangle * 6 + 3] = asTriangleVertexIndex[iIndexTriangle * 3 + 2];
            asEdgeVertexIndex[iIndexTriangle * 6 + 4] = asTriangleVertexIndex[iIndexTriangle * 3 + 2];
            asEdgeVertexIndex[iIndexTriangle * 6 + 5] = asTriangleVertexIndex[iIndexTriangle * 3 + 0];
        }
        m_sbEdgeVertexIndex = OpenGLBaseRenderer.makeShortBuffer( asEdgeVertexIndex );
    }

    // アクセサ
    public FloatBuffer getVertexBuffer()
    {
        return m_fbVertex;
    }

    public ShortBuffer getTriangleVertexIndexBuffer()
    {
        return m_sbTriangleVertexIndex;
    }

    public ShortBuffer getEdgeVertexIndexBuffer()
    {
        return m_sbEdgeVertexIndex;
    }

    public int getVertexCount()
    {
        return m_fbVertex.capacity() / 3;
    }

    public int getTriangleCount()
    {
        return getVertexCount() / 3;
    }

    public int getEdgeCount()
    {
        return getTriangleCount() * 3;
    }
}
