package com.wizarpos.tlvs.debug.parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cloudpos.pinpad.newui.R;
import com.wizarpos.tlvs.debug.config.UiInfo;
import com.wizarpos.tlvs.debug.config.UiInfoHelper;


public class NewUIGenerator {
    private static volatile NewUIGenerator instance = null;

    private NewUIGenerator() {}

    public static NewUIGenerator getInstance() {
        if (instance == null) {
            synchronized (NewUIGenerator.class) {
                if (instance == null) {
                    instance = new NewUIGenerator();
                }
            }
        }
        return instance;
    }

    public View createNewUiView(Context context, UiInfo uiInfo, byte[] keyboard){
        try {
//            Logger.debug("getNewUiView(%s)", uiInfo);
            UiInfoHelper.getInstance().initEnv(uiInfo);
            Bitmap bitmap = UiInfoHelper.getInstance().updateBitmap(keyboard);
//            BitmapHelper.saveBitmapAsPng(bitmap, new File("sdcard/test/test.png"));
            int topResId = uiInfo.getTextLoc() == null ? R.layout.input_pin: R.layout.input_pin;
            return createOverlayLayout(context, bitmap, uiInfo.getUiLoc(), uiInfo.getTextLoc(), topResId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private FrameLayout createOverlayLayout(
            final Context context,
            final Bitmap bitmap,
            final Rect bitmapRect,   // top rect；If not, pass null
            Rect topRect,   // top rect；If not, pass null
            final int topLayoutRes // top rect of the resources layout
    ) {
        final FrameLayout root = new FrameLayout(context);
        FrameLayout.LayoutParams rootLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        root.setLayoutParams(rootLp);
        root.setBackgroundColor(Color.TRANSPARENT);

        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(bitmap);

        FrameLayout.LayoutParams bmpLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        root.addView(imageView, bmpLp);

        final View topView = LayoutInflater.from(context).inflate(topLayoutRes, root, false);
        topView.setId(R.id.topPanel);
        root.addView(topView);

        if (topRect != null) {

            FrameLayout.LayoutParams topLp = new FrameLayout.LayoutParams(
                    topRect.width(),
                    topRect.height()
            );
            topLp.leftMargin = topRect.left;
            topLp.topMargin = topRect.top;
            topView.setLayoutParams(topLp);

            topView.bringToFront();

        } else {
            root.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            root.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                            int topH = topView.getHeight();
                            int desiredTop = bitmapRect.top - topH;

                            int finalTop = Math.max(0, desiredTop);

                            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) topView.getLayoutParams();
                            if (lp == null) {
                                lp = new FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.WRAP_CONTENT
                                );
                            }
                            lp.width = FrameLayout.LayoutParams.MATCH_PARENT;
                            lp.height = topH;
                            lp.leftMargin = 0;
                            lp.topMargin = finalTop;

                            topView.setLayoutParams(lp);
                            topView.bringToFront();
                        }
                    }
            );
        }
        return root;
    }


}
