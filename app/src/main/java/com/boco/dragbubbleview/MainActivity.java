package com.boco.dragbubbleview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private DragBubbleView mDragBubbleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button reCreateBtn = (Button) findViewById(R.id.reCreateBtn);
        mDragBubbleView = (DragBubbleView) findViewById(R.id.dragBubble);
        reCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDragBubbleView.reCreate();
                Random random = new Random();
                switch (random.nextInt(3)) {
                    case 0:
                        mDragBubbleView.setText("9");
                        break;
                    case 1:
                        mDragBubbleView.setText("19");
                        break;
                    case 2:
                        mDragBubbleView.setText("99+");
                        break;
                }

            }
        });
        mDragBubbleView.setOnBubbleStateListener(new DragBubbleView.OnBubbleStateListener() {
            @Override
            public void onDrag() {
                Log.e("---> ", "拖拽气泡");
            }

            @Override
            public void onMove() {
                Log.e("---> ", "移动气泡");
            }

            @Override
            public void onRestore() {
                Log.e("---> ", "气泡恢复原来位置");
            }

            @Override
            public void onDismiss() {
                Log.e("---> ", "气泡消失");
            }
        });
    }
}
