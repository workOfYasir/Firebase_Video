package io.librehealth.firebasevideo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void moveToUploadImageAct(View view)
    {
        try
        {
            startActivity(new Intent(this,UploadVideo.class));
        }
        catch (Exception e)
        {
            Toast.makeText(this, "moveToUploadImageAct:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void moveToDownloadImageAct(View view)
    {
        try
        {
            startActivity(new Intent(this,DownloadActivity.class));
        }
        catch (Exception e)
        {
            Toast.makeText(this, "moveToUploadImageAct:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
