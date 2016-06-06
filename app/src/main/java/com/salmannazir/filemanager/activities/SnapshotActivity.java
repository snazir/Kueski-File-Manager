package com.salmannazir.filemanager.activities;

import android.app.Activity;
import android.os.Bundle;

import com.salmannazir.filemanager.R;
import com.salmannazir.filemanager.fragments.SnapshotFragment;

public class SnapshotActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snapshot);

        SnapshotFragment fragment  = new SnapshotFragment();
        replaceFragment(fragment,true,false);
    }
}
