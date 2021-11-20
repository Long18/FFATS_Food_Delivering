package client.william.ffats.Common;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import client.william.ffats.MainActivity;
import client.william.ffats.R;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_TIME = 3000;

    ImageView imgBrand,imgLogo;
    TextView txtPowerd;

    Animation left_right,right_left,bottom;
    SharedPreferences tutorialScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        createConstuctor();
    }

    private void createConstuctor() {
        imgBrand = findViewById(R.id.background_brand);
        imgLogo = findViewById(R.id.background_logo);
        txtPowerd = findViewById(R.id.powered_line);


        left_right = AnimationUtils.loadAnimation(this,R.anim.anim_left_to_right);
        right_left = AnimationUtils.loadAnimation(this,R.anim.anim_right_to_left);
        bottom = AnimationUtils.loadAnimation(this,R.anim.anim_top_to_bottom);

        txtPowerd.setAnimation(bottom);
        imgBrand.setAnimation(left_right);
        imgLogo.setAnimation(left_right);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tutorialScreen = getSharedPreferences("tutorialScreen",MODE_PRIVATE);
                boolean isFirstTime = tutorialScreen.getBoolean("firstTime",true);

                if (isFirstTime){
                    SharedPreferences.Editor editor = tutorialScreen.edit();
                    editor.putBoolean("firstTime",false);
                    editor.commit();

                    Intent intent = new Intent(getApplicationContext(), Tutorial.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        },SPLASH_TIME);

        this.overridePendingTransition(R.anim.anim_left_to_right,R.anim.anim_right_to_left);
    }
}