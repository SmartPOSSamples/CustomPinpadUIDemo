package com.cloudpos.pinpad.newui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wizarpos.tlvs.debug.testcase.TlvsUiValidator;

public class ImageViewActivity extends Activity {

    private int inputWidthValue = 0;
    private int inputHeightValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        final ImageLineView view = (ImageLineView) findViewById(R.id.image_line_view);
        view.setImageResource(R.drawable.below3);
        view.setInputSize(132, 70);
        view.setOnLineConfirmedListener(new ImageLineView.OnLineConfirmedListener() {
            @Override
            public void onLineConfirmed(int distance) {
                Toast.makeText(ImageViewActivity.this, "Confirmed: " + distance, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onKeyArraysGenerated(int[][] keyLocs, int[][] functionKeyLocs) {
                Log.d("keyLocs", java.util.Arrays.deepToString(keyLocs));
                Log.d("functionKeyLocs", java.util.Arrays.deepToString(functionKeyLocs));
            }
        });

        final EditText inputWidth = (EditText) findViewById(R.id.input_width);
        final EditText inputHeight = (EditText) findViewById(R.id.input_height);
        Button btnApply = (Button) findViewById(R.id.btn_apply_size);
        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String wStr = inputWidth.getText().toString();
                String hStr = inputHeight.getText().toString();
                try {
                    inputWidthValue = Integer.parseInt(wStr);
                    inputHeightValue = Integer.parseInt(hStr);
                    view.setInputSize(inputWidthValue, inputHeightValue);
                } catch (NumberFormatException e) {
                    Toast.makeText(ImageViewActivity.this, "Please input correct W and H", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btnShowPinpad = (Button) findViewById(R.id.btn_show_pinpad);
        btnShowPinpad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[][] keyLocs = view.getGeneratedKeyLocs();
                int[][] functionKeyLocs = view.getGeneratedFunctionKeyLocs();
                if (keyLocs == null || functionKeyLocs == null) {
                    Toast.makeText(ImageViewActivity.this, "Please confirm 4 horizontal lines and 3 vertical lines first", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(ImageViewActivity.this, InputPINActivity.class);
                intent.putExtra("keyLocs", keyLocs);
                intent.putExtra("functionKeyLocs", functionKeyLocs);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}