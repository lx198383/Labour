package com.circledialog.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.circledialog.params.ButtonParams;
import com.circledialog.params.CircleParams;
import com.circledialog.res.drawable.SelectorBtn;
import com.circledialog.res.values.CircleColor;

/**
 * 对话框单个按钮的视图
 * Created by hupei on 2017/3/30.
 */
class SingleButton extends ScaleTextView {
    private ButtonParams mButtonParams;

    public SingleButton(Context context, CircleParams params) {
        super(context);
        init(params);
    }

    private void init(CircleParams params) {
        mButtonParams = params.getNegativeParams() != null ? params.getNegativeParams()
                : params.getPositiveParams();

        setText(mButtonParams.text);
        setTextSize(mButtonParams.textSize);
        setTextColor(mButtonParams.textColor);
        setHeight(mButtonParams.height);

        //如果取消按钮没有背景色，则使用默认色
        int backgroundColor = mButtonParams.backgroundColor != 0 ? mButtonParams.backgroundColor : CircleColor
                .bgDialog;
        int radius = params.getDialogParams().radius;
        setBackground(new SelectorBtn(backgroundColor, 0, 0, radius, radius));

        regOnClickListener();
    }

    private void regOnClickListener() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mButtonParams.dismiss();
                if (mButtonParams.listener != null) mButtonParams.listener.onClick(v);
            }
        });
    }

    public void regOnInputClickListener(final EditText input) {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = input.getText().toString();
                if (!TextUtils.isEmpty(text))
                    mButtonParams.dismiss();
                if (mButtonParams.inputListener != null)
                    mButtonParams.inputListener.onClick(text, v);
            }
        });
    }
}
