package com.salmannazir.filemanager.fragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.view.View;


public class BaseFragment extends Fragment {
	public FragmentNavigationHelper fragmentHelper;


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			fragmentHelper = (FragmentNavigationHelper) activity;
		} catch (Exception e) {
		}

	}
	public FragmentNavigationHelper getHelper() {
		return this.fragmentHelper;
	}


	public interface FragmentNavigationHelper {

		public void addFragment(BaseFragment f, boolean clearBackStack, boolean addToBackStack);

		public void addFragment(BaseFragment f, int layoutId, boolean clearBackStack, boolean addToBackStack);

		public void replaceFragment(BaseFragment f, boolean clearBackStack, boolean addToBackStack);

		public void replaceFragment(BaseFragment f, int layoutId, boolean clearBackStack, boolean addToBackStack);

		public void onBack();

		public void showDialog(String message);

		public void dismissDialog();

		public void showToast(String message);

		public void hideKeyboard(View v);
		public void showKeyboard();

	}





}
