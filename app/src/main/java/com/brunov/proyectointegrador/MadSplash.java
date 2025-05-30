package com.brunov.proyectointegrador;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MadSplash extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mad_splash);

        openApp();

        ImageView mark=findViewById(R.id.mark);
        TextView m = findViewById(R.id.M);
        TextView mad = findViewById(R.id.MAD);
        TextView around = findViewById(R.id.around);

        Animation drop = AnimationUtils.loadAnimation(this,R.anim.drop);
        Animation letter1 = AnimationUtils.loadAnimation(this,R.anim.letters);

        Animation letter2 = AnimationUtils.loadAnimation(this,R.anim.letters);

        Animation letter3 = AnimationUtils.loadAnimation(this,R.anim.around);
        mark.startAnimation(drop);

        new Handler().postDelayed(() -> {
            m.setVisibility(View.VISIBLE);
            m.startAnimation(letter1);

            mad.setVisibility(View.VISIBLE);
            mad.startAnimation(letter2);

        },2000);

        new Handler().postDelayed(() ->{

            around.setVisibility(View.VISIBLE);
            around.startAnimation(letter3);
        },3000);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void openApp(){
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(MadSplash.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        },5000);
    }
}