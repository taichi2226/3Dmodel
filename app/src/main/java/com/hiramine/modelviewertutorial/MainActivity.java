package com.hiramine.modelviewertutorial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // 定数
    private static final int REQUEST_FILESELECT = 0;
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1; // 外部ストレージ読み込みパーミッション要求時の識別コード

    //定義
    private ModelViewerView m_modelviewerview;
    private ModelViewerRenderer mRenderer;
    private String m_strInitialDir = Environment.getExternalStorageDirectory().getPath();    // 初期フォルダ



    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        // GLSurfaceViewの取得

        m_modelviewerview = findViewById( R.id.glview );

    }

    // 初回表示時、および、ポーズからの復帰時
    @Override
    protected void onResume()
    {
        super.onResume();
        //m_modelviewerview.onResume();
        // 外部ストレージ読み込みパーミッション要求
        requestReadExternalStoragePermission();
    }

    // 外部ストレージ読み込みパーミッション要求
    private void requestReadExternalStoragePermission()
    {
        if( PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE ) )
        {    // パーミッションは付与されている
            return;
        }
        // パーミッションは付与されていない。
        // パーミッションリクエスト
        ActivityCompat.requestPermissions( this,
                new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
                REQUEST_PERMISSION_READ_EXTERNAL_STORAGE );
    }

    // パーミッション要求ダイアログの操作結果
    @Override
    public void onRequestPermissionsResult( int requestCode, String[] permissions, int[] grantResults )
    {
        m_modelviewerview = findViewById( R.id.glview );
        switch( requestCode )
        {
            case REQUEST_PERMISSION_READ_EXTERNAL_STORAGE:
                if( grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED )
                {
                    // 許可されなかった場合
                    Toast.makeText( this, "Permission denied.", Toast.LENGTH_SHORT ).show();
                    finish();    // アプリ終了宣言
                    return;
                }
                break;
            default:
                break;
        }
    }

    // 別のアクティビティ（か別のアプリ）に移行したことで、バックグラウンドに追いやられた時
    @Override
    protected void onPause()
    {

        super.onPause();

       // m_modelviewerview.onPause();

    }




    // オプションメニュー作成時の処理
    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.activity_main, menu );
      /*  switch( m_modelviewerview.m_eTrackingMode_1fingerdrag )
        {
            case TM_NONE:
                menu.findItem( R.id.menuitem_drag_none ).setChecked( true );
                break;
            case TM_ROTATE:
                menu.findItem( R.id.menuitem_drag_rotate ).setChecked( true );
                break;
            case TM_PAN:
                menu.findItem( R.id.menuitem_drag_pan ).setChecked( true );
                break;
            case TM_ZOOM:
                menu.findItem( R.id.menuitem_drag_zoom ).setChecked( true );
                break;
        }
        menu.findItem( R.id.menuitem_render_point ).setChecked( m_modelviewerview.getRenderer().m_bRenderPoint );
        menu.findItem( R.id.menuitem_render_line ).setChecked( m_modelviewerview.getRenderer().m_bRenderLine );
        menu.findItem( R.id.menuitem_render_face ).setChecked( m_modelviewerview.getRenderer().m_bRenderFace );
        menu.findItem( R.id.menuitem_pick_point ).setChecked( m_modelviewerview.getRenderer().m_bPickPoint );
        menu.findItem( R.id.menuitem_pick_line ).setChecked( m_modelviewerview.getRenderer().m_bPickLine );
        menu.findItem( R.id.menuitem_pick_face ).setChecked( m_modelviewerview.getRenderer().m_bPickFace );*/
        return true;

    }

    // オプションメニューのアイテム選択時の処理
    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {

        m_modelviewerview = (ModelViewerView) findViewById( R.id.glview );

        switch( item.getItemId() )
        {
            case R.id.menuitem_drag_none:
                item.setChecked( true );
                m_modelviewerview.m_eTrackingMode_1fingerdrag = OpenGLTrackRenderer.ETrackingMode.TM_NONE;
                return true;
            case R.id.menuitem_drag_rotate:
                item.setChecked( true );
                m_modelviewerview.m_eTrackingMode_1fingerdrag = OpenGLTrackRenderer.ETrackingMode.TM_ROTATE;
                return true;
            case R.id.menuitem_drag_pan:
                item.setChecked( true );
                m_modelviewerview.m_eTrackingMode_1fingerdrag = OpenGLTrackRenderer.ETrackingMode.TM_PAN;
                return true;
            case R.id.menuitem_drag_zoom:
                item.setChecked( true );
                m_modelviewerview.m_eTrackingMode_1fingerdrag = OpenGLTrackRenderer.ETrackingMode.TM_ZOOM;
                return true;
            case R.id.menuitem_render_point:
                item.setChecked( !item.isChecked() );
                m_modelviewerview.getRenderer().m_bRenderPoint = item.isChecked();
                m_modelviewerview.requestRender(); // 再描画
                return true;
            case R.id.menuitem_render_line:
                item.setChecked( !item.isChecked() );
                m_modelviewerview.getRenderer().m_bRenderLine = item.isChecked();
                m_modelviewerview.requestRender(); // 再描画
                return true;
            case R.id.menuitem_render_face:
                item.setChecked( !item.isChecked() );
                m_modelviewerview.getRenderer().m_bRenderFace = item.isChecked();
                m_modelviewerview.requestRender(); // 再描画
                return true;
            case R.id.menuitem_pick_point:
                item.setChecked( !item.isChecked() );
                m_modelviewerview.getRenderer().m_bPickPoint = item.isChecked();
                return true;
            case R.id.menuitem_pick_line:
                item.setChecked( !item.isChecked() );
                m_modelviewerview.getRenderer().m_bPickLine = item.isChecked();
                return true;
            case R.id.menuitem_pick_face:
                item.setChecked( !item.isChecked() );
                m_modelviewerview.getRenderer().m_bPickFace = item.isChecked();
                return true;
            case R.id.menuitem_file_open:
                // ファイル選択アクティビティ
                Intent intent = new Intent( this, FileSelectionActivity.class );
                intent.putExtra( FileSelectionActivity.EXTRA_INITIAL_DIR, m_strInitialDir );
                intent.putExtra( FileSelectionActivity.EXTRA_EXT, "stl" );
                startActivityForResult( intent, REQUEST_FILESELECT );
                return true;
        }
        return true;
    }

    // アクティビティ呼び出し結果の取得
    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent intent ) {
        m_modelviewerview = findViewById( R.id.glview );
        super.onActivityResult(requestCode, resultCode, intent);
        if (REQUEST_FILESELECT == requestCode && RESULT_OK == resultCode) {
            Bundle extras = intent.getExtras();
            if (null != extras) {
                File file = (File) extras.getSerializable(FileSelectionActivity.EXTRA_FILE);
                m_modelviewerview.loadModelFile(Objects.requireNonNull(file).getPath(), mRenderer);
                m_strInitialDir = file.getParent();
            }
        }
    }



}
