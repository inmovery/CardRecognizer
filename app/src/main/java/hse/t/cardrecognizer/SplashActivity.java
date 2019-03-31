package hse.t.cardrecognizer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SplashActivity extends AppCompatActivity {

    private Button mButtonScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mButtonScan = (Button)findViewById(R.id.scan);

        mButtonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dep = new Intent(SplashActivity.this,MainActivity.class);
                startActivity(dep);
            }
        });

    }
}
