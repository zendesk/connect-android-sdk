package io.outbound.sdk.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.outbound.sdk.R;
import io.outbound.sdk.activity.AdminController;

/**
 * Created by jophde on 6/22/15 for Outbound.
 */
public class PinLinearLayout extends LinearLayout implements PinController {
    private EditText pin1, pin2, pin3, pin4;

    public PinLinearLayout(Context context) {
        super(context);
    }

    public PinLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public PinLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PinLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();

        pin1 = (EditText) findViewById(R.id.pin1);
        pin2 = (EditText) findViewById(R.id.pin2);
        pin3 = (EditText) findViewById(R.id.pin3);
        pin4 = (EditText) findViewById(R.id.pin4);

        Watcher watcher = new Watcher(pin1, pin2, pin3, pin4);
        pin1.addTextChangedListener(watcher);
        pin2.addTextChangedListener(watcher);
        pin3.addTextChangedListener(watcher);
        pin4.addTextChangedListener(watcher);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void reset() {
        pin1.setText("");
        pin2.setText("");
        pin3.setText("");
        pin4.setText("");
        pin1.requestFocus();
    }

    class Watcher implements TextWatcher {

        private final List<EditText> pins;

        Watcher(EditText... p) {

            pins = new LinkedList<>(Arrays.asList(p));
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override public void afterTextChanged(Editable s) {
            String text = s.toString();
            if (text.length() == 1) {
                EditText pin = (EditText) getFocusedChild();
                Iterator<EditText> iterator = pins.listIterator(pins.indexOf(pin) + 1);

                if (iterator.hasNext()) {
                    pin.clearFocus();
                    iterator.next().requestFocus();
                }

                getHandler().post(new Runnable() {
                    @Override public void run() {
                        if (hasPin()) {
                            ((AdminController) getContext()).onPin(getPin()); // Handler hasn't updated the 4th pin's text yet ;)
                        }
                    }
                });
            }
        }

        boolean hasPin() {
            for (EditText pin : pins) {
                if (pin.getText().toString().length() != 1) {
                    return false;
                }
            }

            return true;
        }

        String getPin() {
            StringBuilder sb = new StringBuilder();
            for (EditText pin : pins) {
                sb.append(pin.getText().toString());
            }

            return sb.toString();
        }
    }
}
