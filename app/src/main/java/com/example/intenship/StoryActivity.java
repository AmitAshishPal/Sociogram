package com.example.intenship;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoryActivity extends AppCompatActivity {

    CircleImageView storyphoto;
    TextView storyname, storytext;
    LottieAnimationView lottie;

    String android, robot, books;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        android = "Android is a mobile/desktop operating system based on a modified version of the Linux kernel and " +
                "other open source software, designed primarily for touchscreen mobile devices such as smartphones and tablets. Android is developed by a consortium of " +
                "developers known as the Open Handset Alliance and " +
                "commercially sponsored by Google. It was unveiled in November 2007, with the first commercial Android device, the HTC Dream, being launched in September 2008.";

        robot = "A robot is a machine—especially one programmable by a computer—capable of carrying out a complex series of actions automatically." +
                "A robot can be guided by an external control device, or the control may be embedded within. Robots may be " +
                "constructed to evoke human form, but most robots are task-performing machines, designed with an emphasis on stark functionality, " +
                "rather than expressive aesthetics.";

        books = "A book is a medium for recording information in the form of writing or images, typically composed of many pages (made of papyrus, parchment, " +
                "vellum, or paper) bound together and protected by a cover.The technical term for this physical arrangement is codex (plural, codices). " +
                "In the history of hand-held physical supports for extended written " +
                "compositions or records, the codex replaces its predecessor, the scroll. A single sheet in a codex is a leaf and each side of a leaf is a page.";

        storyname = findViewById(R.id.storyname);
        storyphoto= findViewById(R.id.storyphoto);
        storytext = findViewById(R.id.storytext);



        Bundle bundle = getIntent().getExtras();
        int resId = bundle.getInt("photo");
        lottie = findViewById(R.id.lottie);

        storyphoto.setImageResource(resId);
        storyname.setText(getIntent().getStringExtra("name"));

        if(storyname.getText().toString().equals("Android")){
            storytext.setText(android);
        }
        else if(storyname.getText().toString().equals("Robot")){
            storytext.setText(robot);
        }
        else if(storyname.getText().toString().equals("Books")){
            storytext.setText(books);
        }
        lottie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lottie.playAnimation();
            }
        });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(StoryActivity.this, DashActivity.class);
                startActivity(intent);
                overridePendingTransition(0,0);
                finish();

            }
        }, 5000);



    }
}