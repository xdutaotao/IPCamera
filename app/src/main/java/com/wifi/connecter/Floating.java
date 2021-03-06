/*
 * Wifi Connecter
 *
 * Copyright (c) 20101 Kevin Yuan (farproc@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 **/

package com.wifi.connecter;


import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.realtek.simpleconfig.SCParams;
import com.rockchip.tutk.R;


//import android.util.Log;

/**
 * A dialog-like floating activity
 * @author Kevin Yuan
 *
 */
public class Floating extends Activity {
//	private static final String TAG = "Floating";

	private static final int[] BUTTONS = {R.id.button1, R.id.button2, R.id.button3};

	private View mView;
	private ViewGroup mContentViewContainer;
	private Content mContent;

	private EditText passwdText;
    private int minPasswdLen;
    private int maxPasswdLen;
	private Button connectButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// It will not work if we setTheme here.
		// Please add android:theme="@android:style/Theme.Dialog" to any descendant class in AndroidManifest.xml!
		// See http://code.google.com/p/android/issues/detail?id=4394
		setTheme(android.R.style.Theme_Dialog);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);

		mView = View.inflate(this, R.layout.floating, null);
		final DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mView.setMinimumWidth(Math.min(dm.widthPixels, dm.heightPixels) - 20);
		setContentView(mView);

		mContentViewContainer = (ViewGroup) mView.findViewById(R.id.content);
	}

	private void setDialogContentView(final View contentView) {
//        Log.d(TAG, "setDialogContentView\n");
		mContentViewContainer.removeAllViews();
		//mContentViewContainer.addView(contentView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		/* FILL_PARENT is renamed MATCH_PARENT in API Level 8 and higher */
		mContentViewContainer.addView(contentView, new LayoutParams(LayoutParams.MATCH_PARENT , LayoutParams.WRAP_CONTENT));
	}

	public void setContent(Content content) {
		mContent = content;
		if(SCParams.mConnectQuiet) {
//			Log.d(TAG, "startConnectQuiet()");
			mContent.startConnectQuiet();
		} else {
//			Log.d(TAG, "refreshContent()");
			refreshContent();
		}
	}

	public void refreshContent() {
		setDialogContentView(mContent.getView());
		((TextView)findViewById(R.id.title)).setText(mContent.getTitle()); //getTitle

		final int btnCount = mContent.getButtonCount();
		if(btnCount > BUTTONS.length) {
			throw new RuntimeException(String.format("%d exceeds maximum button count: %d!", btnCount, BUTTONS.length));
		}
		findViewById(R.id.buttons_view).setVisibility(btnCount > 0 ? View.VISIBLE : View.GONE);
		for(int buttonId:BUTTONS) {
			final Button btn = (Button) findViewById(buttonId);
			btn.setOnClickListener(null);
			btn.setVisibility(View.GONE);
		}

		for(int btnIndex = 0; btnIndex < btnCount; btnIndex++){
			final Button btn = (Button)findViewById(BUTTONS[btnIndex]);
//	        Log.d(TAG, "getButtonText(" + btnIndex + "): " + mContent.getButtonText(btnIndex));
			btn.setText(mContent.getButtonText(btnIndex));
			btn.setVisibility(View.VISIBLE);
			btn.setOnClickListener(mContent.getButtonOnClickListener(btnIndex));

			/* Enable the connect button or not */
			if(btnIndex == 0 && !SCParams.IsOpenNetwork) {
				passwdText = (EditText)mView.findViewById(R.id.Password_EditText);
				int passwdLen = passwdText.getText().toString().length();
				if(SCParams.SecurityType.equals("WPA") || SCParams.SecurityType.equals("WPA2")) {
					minPasswdLen = 8;
					maxPasswdLen = 64;
				} else if (SCParams.SecurityType.equals("WEP")) {
					minPasswdLen = 5;
					maxPasswdLen = 26;
				} else {
					minPasswdLen = 1;
					maxPasswdLen = 64;
				}
				connectButton = btn;
				if(passwdLen < minPasswdLen)
					connectButton.setEnabled(false);
				passwdText.addTextChangedListener(mTextWatcher);
			}
		}
	}

	TextWatcher mTextWatcher = new TextWatcher() {

	    @Override
	    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
	            int arg3) {
	        // TODO Auto-generated method stub
	    }

	    @Override
	    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
	        Editable editable = passwdText.getText();
	        int textLen = editable.length();

//	        Log.d(TAG, "len: " + textLen);
	        if (textLen < minPasswdLen) {
	        	connectButton.setEnabled(false);
	        } else if(textLen>=minPasswdLen && textLen <=maxPasswdLen) {
	        	connectButton.setEnabled(true);
	        } else {
	            int selEndIndex = Selection.getSelectionEnd(editable);
	            String str = editable.toString();
	            //截取新字符串
	            String newStr = str.substring(0, maxPasswdLen);
	            passwdText.setText(newStr);
	            editable = passwdText.getText();

	            //新字符串的长度
	            int newLen = editable.length();
	            //旧光标位置超过字符串长度
	            if(selEndIndex > newLen)
	            {
	                selEndIndex = editable.length();
	            }
	            //设置新光标所在的位置
	            Selection.setSelection(editable, selEndIndex);
	        }
	    }

	    @Override
	    public void afterTextChanged(Editable arg0) {
	        // TODO Auto-generated method stub
	    }
	};

	public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		if(mContent != null) {
			mContent.onCreateContextMenu(menu, v, menuInfo);
		}
	}

	public boolean onContextItemSelected (MenuItem item) {
		if(mContent != null) {
			return mContent.onContextItemSelected(item);
		}
		return false;
	}


	public interface Content {
		CharSequence getTitle();
		View getView();
		void startConnectQuiet();
		int getButtonCount();
		CharSequence getButtonText(int index);
		OnClickListener getButtonOnClickListener(int index);
		void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);
		boolean onContextItemSelected(MenuItem item);
	}
}
