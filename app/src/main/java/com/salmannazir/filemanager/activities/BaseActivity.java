package com.salmannazir.filemanager.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


import com.salmannazir.filemanager.R;
import com.salmannazir.filemanager.fragments.BaseFragment;

import java.util.Stack;


/**
 * Created by Salman Nazir on 06/05/16.
 */
public abstract class BaseActivity extends FragmentActivity implements BaseFragment.FragmentNavigationHelper{

    ProgressDialog mProgressDialog;
    private BaseFragment mCurrentFragment;
    //    private SharedPreferences prefs;
    private Stack<Fragment> mFragments = new Stack<Fragment>();



    @Override
    public void addFragment(BaseFragment f, boolean clearBackStack, boolean addToBackstack) {
   //     addFragment(f, R.id.fragment_container, clearBackStack, addToBackstack);
    }

    public void addFragment(BaseFragment f, int layoutId, boolean clearBackStack, boolean addToBackStack) {
        if(clearBackStack) {
            clearFragmentBackStack();
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
  //      transaction.add(R.id.fragment_container, f);
        if(addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();

        mCurrentFragment = f;
        mFragments.push(f);

        onFragmentBackStackChanged();
    }

    @Override
    public void replaceFragment(BaseFragment f, boolean clearBackStack, boolean addToBackstack) {
   //     replaceFragment(f, R.id.fragment_container, clearBackStack, addToBackstack);
    }

    public void replaceFragment(BaseFragment f, int layoutId, boolean clearBackStack, boolean addToBackstack) {
        if(clearBackStack) {
            clearFragmentBackStack();
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(layoutId, f);
        if(addToBackstack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();

        mCurrentFragment = f;
        mFragments.push(f);

        onFragmentBackStackChanged();
    }

    @Override
    public void onBack() {
        if(getSupportFragmentManager().getBackStackEntryCount() < 1) {
            finish();
            return;
        }
        getSupportFragmentManager().popBackStack();
        mFragments.pop();
        mCurrentFragment = (BaseFragment) (mFragments.isEmpty() ? null : ((mFragments.peek() instanceof BaseFragment) ? mFragments.peek() : null));

        onFragmentBackStackChanged();
    }

    public void clearFragmentBackStack() {
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount() - 1; i++) {
            fm.popBackStack();
        }

        if(!mFragments.isEmpty()) {
            Fragment homeFragment = mFragments.get(0);
            mFragments.clear();
            mFragments.push(homeFragment);
        }

    }

    public void onFragmentBackStackChanged() {


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
//			boolean flag = false;
//			if(mCurrentFragment != null) {
//				flag = mCurrentFragment.onKeyDown(keyCode, event);
//			}
//			if(flag) {
//				return flag;
//			}
            if(getSupportFragmentManager().getBackStackEntryCount() < 1) {
                finish();
                return true;
            }
            else {
                onBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public BaseFragment getCurrentFragment() {
        return mCurrentFragment;
    }

    public void setCurrentFragment(BaseFragment mCurrentFragment) {
        this.mCurrentFragment = mCurrentFragment;
    }

//	public SharedPreferences getPrefs() {
//		return prefs;
//	}

    @Override
    protected void onStop() {
        super.onStop();
    }



    public void showDialog(String message) {
        // TODO Auto-gensherated method stub
        if(mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this,"", message);
            mProgressDialog.setCancelable(true);

            return;
        }
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(message);
        if(!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }
    public void dismissDialog() {
        // TODO Auto-generated method stub
        mProgressDialog.dismiss();
    }
    public void showToast(String text) {
        // TODO Auto-generated method stub
        Toast.makeText(BaseActivity.this, text, Toast.LENGTH_SHORT).show();
    }


}
