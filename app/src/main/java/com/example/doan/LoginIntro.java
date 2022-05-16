package com.example.doan;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginIntro extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    ImageView iv_intro;
    TextView tv_intro, tv_intro1;
    Animation topAnim, bottomAnim;
    private static int DELAY_TIME = 6000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_intro);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        firebaseAuth = FirebaseAuth.getInstance();
        getView();
        iv_intro.setAnimation(topAnim);
        tv_intro.setAnimation(bottomAnim);
        tv_intro1.setAnimation(bottomAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LoginIntro.this, LoginActivity.class);
                Pair[] pairs = new Pair[3];
                pairs[0] = new Pair(iv_intro, "splash_img");
                pairs[1] = new Pair(tv_intro, "splash_text");
                pairs[2] = new Pair(tv_intro1, "splash_text1");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(LoginIntro.this, pairs);
                startActivity(intent,options.toBundle());
                finish();
            }
        },DELAY_TIME);

    }

    public void getView(){
        iv_intro = findViewById(R.id.iv_intro);
        tv_intro = findViewById(R.id.tv_intro);
        tv_intro1 = findViewById(R.id.tv_intro1);
    }


}