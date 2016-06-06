/*
    Open Manager, an open source file manager for the Android system
    Copyright (C) 2009, 2010, 2011  Joe Berria <nexesdevelopment@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.salmannazir.filemanager.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.salmannazir.filemanager.R;
import com.salmannazir.filemanager.businesslogic.EventHandler;
import com.salmannazir.filemanager.businesslogic.FileManager;
import com.salmannazir.filemanager.prefs.Constants;

import java.io.File;


public final class MainActivity extends AppCompatActivity implements OnClickListener, View.OnCreateContextMenuListener {

    private FileManager mFileManager;
    private EventHandler mEventHandler;
    private EventHandler.ListAdapter mListAdapter;

    private SharedPreferences mSettings;
    private boolean mReturnIntent = false;
    private boolean mHoldingFile = false;
    private boolean mHoldingZip = false;
    private boolean mUseBackKey = true;
    private String mCopiedTarget;
    private String mZippedTarget;
    private String mSelectedListItem;                //item from context menu
    private TextView mDirectoryPathLabel, mDetailLabel, mStorageLabel;
    private RecyclerView mRecyclerView;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        if (EventHandler.checkStoragePermission(this))
            init(savedInstanceState);
        else
            EventHandler.requestStoragePermission(this);
    }

    private void init(Bundle savedInstanceState) {


        /*sets the respective ListAdapter for our MainActivity (ListActivity) and
         *forward it to EventHandler class.
         */
        /*read settings*/
        mSettings = getSharedPreferences(Constants.PREFS_NAME, 0);
        boolean hide = mSettings.getBoolean(Constants.PREFS_HIDDEN, false);
        boolean thumb = mSettings.getBoolean(Constants.PREFS_THUMBNAIL, true);
        int space = mSettings.getInt(Constants.PREFS_STORAGE, View.VISIBLE);
        int color = mSettings.getInt(Constants.PREFS_COLOR, -1);
        int sort = mSettings.getInt(Constants.PREFS_SORT, 3);

        mFileManager = new FileManager();
        mFileManager.setShowHiddenFiles(hide);
        mFileManager.setDirectorySortType(sort);

        if (savedInstanceState != null)
            mEventHandler = new EventHandler(MainActivity.this, mFileManager, savedInstanceState.getString("location"));
        else
            mEventHandler = new EventHandler(MainActivity.this, mFileManager);
        mEventHandler.setTextColor(color);
        mEventHandler.setShowThumbnails(thumb);


        mListAdapter = mEventHandler.new ListAdapter(this, this);

        mEventHandler.setListAdapter(mListAdapter);
        mRecyclerView = (RecyclerView) findViewById(R.id.listView);
        //mRecylcerView.setOnItemClickListener(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mListAdapter);

        registerForContextMenu(mRecyclerView);

        mStorageLabel = (TextView) findViewById(R.id.storage_label);
        mDetailLabel = (TextView) findViewById(R.id.detail_label);
        mDirectoryPathLabel = (TextView) findViewById(R.id.path_label);

        fab = (FloatingActionButton) findViewById(R.id.createBt);
        fab1 = (FloatingActionButton) findViewById(R.id.newFileBt);
//        fab2 = (FloatingActionButton) findViewById(R.id.newDirectoryBt);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
//        fab2.setOnClickListener(this);

        mDirectoryPathLabel.setText(Constants.HOME_PATH);

        updateStorageLabel();
        mStorageLabel.setVisibility(space);

        mEventHandler.updateLabels(mDirectoryPathLabel, mDetailLabel);


        int[] button_id = {R.id.hidden_copy,
                R.id.hidden_delete, R.id.hidden_move};

        for (int id :
                button_id) {
            findViewById(id).setOnClickListener(mEventHandler);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mFileManager != null)
            outState.putString("location", mFileManager.getCurrentDirectory());
    }

    private void returnIntentResults(File data) {
        mReturnIntent = false;

        Intent ret = new Intent();
        ret.setData(Uri.fromFile(data));
        setResult(RESULT_OK, ret);

        finish();
    }

    private void updateStorageLabel() {
        long total, aval;
        int kb = 1024;

        StatFs fs = new StatFs(Environment.
                getExternalStorageDirectory().getPath());

        total = fs.getBlockCount() * (fs.getBlockSize() / kb);
        aval = fs.getAvailableBlocks() * (fs.getBlockSize() / kb);

        mStorageLabel.setText(String.format("sdcard: Total %.2f GB " +
                        "\t\tAvailable %.2f GB",
                (double) total / (kb * kb), (double) aval / (kb * kb)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        SharedPreferences.Editor editor = mSettings.edit();
        boolean check;
        boolean thumbnail;
        int color, sort, space;



        if (requestCode == Constants.SETTING_REQ && resultCode == RESULT_CANCELED) {
            //save the information we get from settings activity
            check = data.getBooleanExtra("HIDDEN", false);
            thumbnail = data.getBooleanExtra("THUMBNAIL", true);
            color = data.getIntExtra("COLOR", -1);
            sort = data.getIntExtra("SORT", 0);
            space = data.getIntExtra("SPACE", View.VISIBLE);

            editor.putBoolean(Constants.PREFS_HIDDEN, check);
            editor.putBoolean(Constants.PREFS_THUMBNAIL, thumbnail);
            editor.putInt(Constants.PREFS_COLOR, color);
            editor.putInt(Constants.PREFS_SORT, sort);
            editor.putInt(Constants.PREFS_STORAGE, space);
            editor.commit();

            mFileManager.setShowHiddenFiles(check);
            mFileManager.setDirectorySortType(sort);
            mEventHandler.setTextColor(color);
            mEventHandler.setShowThumbnails(thumbnail);
            mStorageLabel.setVisibility(space);
            mEventHandler.updateDirectory(mFileManager.getNextDirectory(mFileManager.getCurrentDirectory(), true));
        } else if (requestCode == Constants.CAMERA_REQUEST && resultCode == RESULT_OK) {
            Toast.makeText(MainActivity.this, "Received Immage", Toast.LENGTH_SHORT).show();
            mEventHandler.updateDirectory(mFileManager.getNextDirectory(mFileManager.getCurrentDirectory(), true));

        }

    }

    /* ================Menus, options menu and context menu start here=================*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (mReturnIntent)
            menu.findItem(R.id.multi_select).setVisible(false);
        else
            menu.findItem(R.id.multi_select).setVisible(true);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_directory:
                showDialog(R.id.new_directory);
                return true;
            case R.id.takeSnapshot:
                mEventHandler.handleSnapShot();
                return true;

            case R.id.search:
                showDialog(R.id.search);
                return true;

            case R.id.settings:
                Intent settings_int = new Intent(this, SettingsActivity.class);
                settings_int.putExtra("HIDDEN", mSettings.getBoolean(Constants.PREFS_HIDDEN, false));
                settings_int.putExtra("THUMBNAIL", mSettings.getBoolean(Constants.PREFS_THUMBNAIL, true));
                settings_int.putExtra("COLOR", mSettings.getInt(Constants.PREFS_COLOR, -1));
                settings_int.putExtra("SORT", mSettings.getInt(Constants.PREFS_SORT, 0));
                settings_int.putExtra("SPACE", mSettings.getInt(Constants.PREFS_STORAGE, View.VISIBLE));

                startActivityForResult(settings_int, Constants.SETTING_REQ);
                return true;

            case R.id.quit:
                finish();
                return true;
            case R.id.home_button:
                mEventHandler.handleHomeButton();
                return true;
            case R.id.new_file:
                // new file handler

                mEventHandler.handleCreateNewFile(MainActivity.this, mFileManager.getCurrentDirectory());

                return true;
            case R.id.info:
                mEventHandler.handleInfoButton();
                return true;
            case R.id.multi_select:
                mEventHandler.handleMultiSelectButton();
                return true;
            case R.id.help:
                mEventHandler.handleHelpButton();
                return true;
        }
        return false;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
        super.onCreateContextMenu(menu, v, info);

        menu.clear();
        boolean multi_data = mEventHandler.hasMultiSelectData();
        mSelectedListItem = mEventHandler.getData(((EventHandler.ListAdapter) mRecyclerView.getAdapter()).getPosition());

    	/* is it a directory and is multi-select turned off */
        if (mFileManager.isDirectory(mSelectedListItem) && !mEventHandler.isMultiSelectedFiles()) {
            menu.setHeaderTitle("Folder operations");
            menu.add(0, Constants.D_MENU_DELETE, 0, "Delete Folder");
            menu.add(0, Constants.D_MENU_RENAME, 0, "Rename Folder");
            menu.add(0, Constants.D_MENU_COPY, 0, "Copy Folder");
            menu.add(0, Constants.D_MENU_MOVE, 0, "Move(Cut) Folder");
            menu.add(0, Constants.D_MENU_PASTE, 0, "Paste into folder").setEnabled(mHoldingFile ||
                    multi_data);

        /* is it a file and is multi-select turned off */
        } else if (!mFileManager.isDirectory(mSelectedListItem) && !mEventHandler.isMultiSelectedFiles()) {
            menu.setHeaderTitle("File Operations");
            menu.add(0, Constants.F_MENU_DELETE, 0, "Delete File");
            menu.add(0, Constants.F_MENU_RENAME, 0, "Rename File");
            menu.add(0, Constants.F_MENU_COPY, 0, "Copy File");
            menu.add(0, Constants.F_MENU_MOVE, 0, "Move(Cut) File");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case Constants.D_MENU_DELETE:
            case Constants.F_MENU_DELETE:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Warning ");
                builder.setIcon(R.drawable.warning);
                builder.setMessage("Deleting " + mSelectedListItem +
                        " cannot be undone. Are you sure you want to delete?");
                builder.setCancelable(false);

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mEventHandler.deleteFile(mFileManager.getCurrentDirectory() + "/" + mSelectedListItem);
                    }
                });
                AlertDialog alert_d = builder.create();
                alert_d.show();
                return true;

            case Constants.D_MENU_RENAME:
                showDialog(Constants.D_MENU_RENAME);
                return true;

            case Constants.F_MENU_RENAME:
                showDialog(Constants.F_MENU_RENAME);
                return true;


            case Constants.F_MENU_MOVE:
            case Constants.D_MENU_MOVE:
            case Constants.F_MENU_COPY:
            case Constants.D_MENU_COPY:
                if (item.getItemId() == Constants.F_MENU_MOVE || item.getItemId() == Constants.D_MENU_MOVE)
                    mEventHandler.setDeleteAfterCut(true);

                mHoldingFile = true;

                mCopiedTarget = mFileManager.getCurrentDirectory() + "/" + mSelectedListItem;
                mDetailLabel.setText("Holding " + mSelectedListItem);
                return true;


            case Constants.D_MENU_PASTE:
                boolean multi_select = mEventHandler.hasMultiSelectData();

                if (multi_select) {
                    mEventHandler.copyMultiSelectFiles(mFileManager.getCurrentDirectory() + "/" + mSelectedListItem);

                } else if (mHoldingFile && mCopiedTarget.length() > 1) {

                    mEventHandler.copyFile(mCopiedTarget, mFileManager.getCurrentDirectory() + "/" + mSelectedListItem);
                    mDetailLabel.setText("");
                }

                mHoldingFile = false;
                return true;

            case Constants.D_MENU_ZIP:
                String dir = mFileManager.getCurrentDirectory();

                mEventHandler.zipFile(dir + "/" + mSelectedListItem);
                return true;

            case Constants.D_MENU_UNZIP:
                if (mHoldingZip && mZippedTarget.length() > 1) {
                    String current_dir = mFileManager.getCurrentDirectory() + "/" + mSelectedListItem + "/";
                    String old_dir = mZippedTarget.substring(0, mZippedTarget.lastIndexOf("/"));
                    String name = mZippedTarget.substring(mZippedTarget.lastIndexOf("/") + 1, mZippedTarget.length());

                    if (new File(mZippedTarget).canRead() && new File(current_dir).canWrite()) {
                        mEventHandler.unZipFileToDirectory(name, current_dir, old_dir);
                        mDirectoryPathLabel.setText(current_dir);

                    } else {
                        Toast.makeText(this, "You do not have permission to unzip " + name,
                                Toast.LENGTH_SHORT).show();
                    }
                }

                mHoldingZip = false;
                mDetailLabel.setText("");
                mZippedTarget = "";
                return true;
        }
        return false;
    }

    public void animateFAB() {

        if (isFabOpen) {

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
//            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
//            fab2.setClickable(false);
            isFabOpen = false;
            Log.d("Kueski", "Close");

        } else {

            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
//            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
//            fab2.setClickable(true);
            isFabOpen = true;
            Log.d("Kueski", "open");

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQUEST_STORAGE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init(null);

                } else {
                    Toast.makeText(MainActivity.this, "Storage permission is required", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }

        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.createBt:
                animateFAB();
                break;
            case R.id.newFileBt:
                mEventHandler.handleCreateNewFile(MainActivity.this, mFileManager.getCurrentDirectory());
                animateFAB();
                break;
            case R.id.cardView:
                onItemClick(mRecyclerView.getChildLayoutPosition(v));
                break;
//            case R.id.newDirectoryBt:
//                showDialog(R.id.new_directory);
//                animateFAB();
//                break;
        }
    }
    /* ================Menus, options menu and context menu end here=================*/

    @Override
    protected Dialog onCreateDialog(int id) {
        final Dialog dialog = new Dialog(MainActivity.this);

        switch (id) {
            case R.id.new_directory:
                dialog.setContentView(R.layout.input_layout);
                dialog.setTitle("Create New Directory");
                dialog.setCancelable(false);

                ImageView icon = (ImageView) dialog.findViewById(R.id.input_icon);
                icon.setImageResource(R.drawable.newfolder);

                TextView label = (TextView) dialog.findViewById(R.id.input_label);
                label.setText(mFileManager.getCurrentDirectory());
                final EditText input = (EditText) dialog.findViewById(R.id.input_inputText);

                Button cancel = (Button) dialog.findViewById(R.id.input_cancel_b);
                Button create = (Button) dialog.findViewById(R.id.input_create_b);

                create.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (input.getText().length() > 1) {
                            if (mFileManager.createDirectory(mFileManager.getCurrentDirectory() + "/", input.getText().toString()) == 0)
                                Toast.makeText(MainActivity.this,
                                        "Folder " + input.getText().toString() + " created",
                                        Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(MainActivity.this, "New folder was not created", Toast.LENGTH_SHORT).show();
                        }

                        dialog.dismiss();
                        String temp = mFileManager.getCurrentDirectory();
                        mEventHandler.updateDirectory(mFileManager.getNextDirectory(temp, true));
                    }
                });
                cancel.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
            case Constants.D_MENU_RENAME:
            case Constants.F_MENU_RENAME:
                dialog.setContentView(R.layout.input_layout);
                dialog.setTitle("Rename " + mSelectedListItem);
                dialog.setCancelable(false);

                ImageView rename_icon = (ImageView) dialog.findViewById(R.id.input_icon);
                rename_icon.setImageResource(R.drawable.rename);

                TextView rename_label = (TextView) dialog.findViewById(R.id.input_label);
                rename_label.setText(mFileManager.getCurrentDirectory());
                final EditText rename_input = (EditText) dialog.findViewById(R.id.input_inputText);

                Button rename_cancel = (Button) dialog.findViewById(R.id.input_cancel_b);
                Button rename_create = (Button) dialog.findViewById(R.id.input_create_b);
                rename_create.setText("Rename");

                rename_create.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (rename_input.getText().length() < 1)
                            dialog.dismiss();

                        if (mFileManager.renameTargetFile(mFileManager.getCurrentDirectory() + "/" + mSelectedListItem, rename_input.getText().toString()) == 0) {
                            Toast.makeText(MainActivity.this, mSelectedListItem + " renamed to " + rename_input.getText().toString(),
                                    Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(MainActivity.this, mSelectedListItem + " was not renamed", Toast.LENGTH_LONG).show();

                        dialog.dismiss();
                        String temp = mFileManager.getCurrentDirectory();
                        mEventHandler.updateDirectory(mFileManager.getNextDirectory(temp, true));
                    }
                });
                rename_cancel.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;

            case Constants.SEARCH_B:
            case R.id.search:
                dialog.setContentView(R.layout.input_layout);
                dialog.setTitle("Search");
                dialog.setCancelable(false);

                ImageView searchIcon = (ImageView) dialog.findViewById(R.id.input_icon);
                searchIcon.setImageResource(R.drawable.search);

                TextView search_label = (TextView) dialog.findViewById(R.id.input_label);
                search_label.setText("Search for a file");
                final EditText search_input = (EditText) dialog.findViewById(R.id.input_inputText);

                Button search_button = (Button) dialog.findViewById(R.id.input_create_b);
                Button cancel_button = (Button) dialog.findViewById(R.id.input_cancel_b);
                search_button.setText("Search");

                search_button.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        String temp = search_input.getText().toString();

                        if (temp.length() > 0)
                            mEventHandler.searchFile(temp);
                        dialog.dismiss();
                    }
                });

                cancel_button.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                break;
        }
        return dialog;
    }


    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        String current = mFileManager.getCurrentDirectory();

        if (keycode == KeyEvent.KEYCODE_SEARCH) {
            showDialog(Constants.SEARCH_B);

            return true;

        } else if (keycode == KeyEvent.KEYCODE_BACK && mUseBackKey && !current.equals(Constants.HOME_PATH)) {
            if (mEventHandler.isMultiSelectedFiles()) {
                mListAdapter.removeMultiSelect(true);
                Toast.makeText(MainActivity.this, "Multi-select is now off", Toast.LENGTH_SHORT).show();

            } else {
                //stop updating thumbnail icons if its running
                mEventHandler.stopThumbnailThread();
                mEventHandler.updateDirectory(mFileManager.getPreviousDirectory());
                mDirectoryPathLabel.setText(mFileManager.getCurrentDirectory());
            }
            return true;

        } else if (keycode == KeyEvent.KEYCODE_BACK && mUseBackKey && current.equals(Constants.HOME_PATH)) {
            Toast.makeText(MainActivity.this, "Press back again to quit.", Toast.LENGTH_SHORT).show();

            if (mEventHandler.isMultiSelectedFiles()) {
                mListAdapter.removeMultiSelect(true);
                Toast.makeText(MainActivity.this, "Multi-select is now off", Toast.LENGTH_SHORT).show();
            }

            mUseBackKey = false;
            mDirectoryPathLabel.setText(mFileManager.getCurrentDirectory());

            return false;

        } else if (keycode == KeyEvent.KEYCODE_BACK && !mUseBackKey && current.equals(Constants.HOME_PATH)) {
            finish();

            return false;
        }
        return false;
    }

    public void onItemClick(int position) {
        final String item = mEventHandler.getData(position);
        boolean multiSelect = mEventHandler.isMultiSelectedFiles();
        File file = new File(mFileManager.getCurrentDirectory() + "/" + item);
        String item_ext = null;

        try {
            item_ext = item.substring(item.lastIndexOf("."), item.length());

        } catch (IndexOutOfBoundsException e) {
            item_ext = "";
        }


        if (multiSelect) {
            mListAdapter.addMultiPosition(position, file.getPath());

        } else {
            if (file.isDirectory()) {
                if (file.canRead()) {
                    mEventHandler.stopThumbnailThread();
                    mEventHandler.updateDirectory(mFileManager.getNextDirectory(item, false));
                    mDirectoryPathLabel.setText(mFileManager.getCurrentDirectory());

		    		/*set back button switch to true
                     * (this will be better implemented later)
		    		 */
                    if (!mUseBackKey)
                        mUseBackKey = true;

                } else {
                    Toast.makeText(this, "Can't read folder due to permissions",
                            Toast.LENGTH_SHORT).show();
                }
            }

	    	/*music file selected--add more audio formats*/
            else if (item_ext.equalsIgnoreCase(".mp3") ||
                    item_ext.equalsIgnoreCase(".m4a") ||
                    item_ext.equalsIgnoreCase(".mp4")) {

                if (mReturnIntent) {
                    returnIntentResults(file);
                } else {
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_VIEW);
                    i.setDataAndType(Uri.fromFile(file), "audio/*");
                    startActivity(i);
                }
            }

	    	/*photo file selected*/
            else if (item_ext.equalsIgnoreCase(".jpeg") ||
                    item_ext.equalsIgnoreCase(".jpg") ||
                    item_ext.equalsIgnoreCase(".png") ||
                    item_ext.equalsIgnoreCase(".gif") ||
                    item_ext.equalsIgnoreCase(".tiff")) {

                if (file.exists()) {
                    if (mReturnIntent) {
                        returnIntentResults(file);

                    } else {
                        Intent picIntent = new Intent();
                        picIntent.setAction(Intent.ACTION_VIEW);
                        picIntent.setDataAndType(Uri.fromFile(file), "image/*");
                        startActivity(picIntent);
                    }
                }
            }

	    	/*video file selected--add more video formats*/
            else if (item_ext.equalsIgnoreCase(".m4v") ||
                    item_ext.equalsIgnoreCase(".3gp") ||
                    item_ext.equalsIgnoreCase(".wmv") ||
                    item_ext.equalsIgnoreCase(".mp4") ||
                    item_ext.equalsIgnoreCase(".ogg") ||
                    item_ext.equalsIgnoreCase(".wav")) {

                if (file.exists()) {
                    if (mReturnIntent) {
                        returnIntentResults(file);

                    } else {
                        Intent movieIntent = new Intent();
                        movieIntent.setAction(Intent.ACTION_VIEW);
                        movieIntent.setDataAndType(Uri.fromFile(file), "video/*");
                        startActivity(movieIntent);
                    }
                }
            }

	    	/*zip file */
            else if (item_ext.equalsIgnoreCase(".zip")) {

                if (mReturnIntent) {
                    returnIntentResults(file);

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    AlertDialog alert;
                    mZippedTarget = mFileManager.getCurrentDirectory() + "/" + item;
                    CharSequence[] option = {"Extract here", "Extract to..."};

                    builder.setTitle("Extract");
                    builder.setItems(option, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    String dir = mFileManager.getCurrentDirectory();
                                    mEventHandler.doUnZipFile(item, dir + "/");
                                    break;

                                case 1:
                                    mDetailLabel.setText("Holding " + item +
                                            " to extract");
                                    mHoldingZip = true;
                                    break;
                            }
                        }
                    });

                    alert = builder.create();
                    alert.show();
                }
            }

	    	/* gzip files, this will be implemented later */
            else if (item_ext.equalsIgnoreCase(".gzip") ||
                    item_ext.equalsIgnoreCase(".gz")) {

                if (mReturnIntent) {
                    returnIntentResults(file);

                } else {
                    //TODO:
                }
            }

	    	/*pdf file selected*/
            else if (item_ext.equalsIgnoreCase(".pdf")) {

                if (file.exists()) {
                    if (mReturnIntent) {
                        returnIntentResults(file);

                    } else {
                        Intent pdfIntent = new Intent();
                        pdfIntent.setAction(Intent.ACTION_VIEW);
                        pdfIntent.setDataAndType(Uri.fromFile(file),
                                "application/pdf");

                        try {
                            startActivity(pdfIntent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(this, "Sorry, couldn't find a pdf viewer",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

	    	/*Android application file*/
            else if (item_ext.equalsIgnoreCase(".apk")) {

                if (file.exists()) {
                    if (mReturnIntent) {
                        returnIntentResults(file);

                    } else {
                        Intent apkIntent = new Intent();
                        apkIntent.setAction(Intent.ACTION_VIEW);
                        apkIntent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        startActivity(apkIntent);
                    }
                }
            }

	    	/* HTML file */
            else if (item_ext.equalsIgnoreCase(".html")) {

                if (file.exists()) {
                    if (mReturnIntent) {
                        returnIntentResults(file);

                    } else {
                        Intent htmlIntent = new Intent();
                        htmlIntent.setAction(Intent.ACTION_VIEW);
                        htmlIntent.setDataAndType(Uri.fromFile(file), "text/html");

                        try {
                            startActivity(htmlIntent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(this, "Sorry, couldn't find a HTML viewer",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

	    	/* text file*/
            else if (item_ext.equalsIgnoreCase(".txt")) {

                if (file.exists()) {
                    if (mReturnIntent) {
                        returnIntentResults(file);

                    } else {
                        Intent txtIntent = new Intent();
                        txtIntent.setAction(Intent.ACTION_VIEW);
                        txtIntent.setDataAndType(Uri.fromFile(file), "text/plain");

                        try {
                            startActivity(txtIntent);
                        } catch (ActivityNotFoundException e) {
                            txtIntent.setType("text/*");
                            startActivity(txtIntent);
                        }
                    }
                }
            }

	    	/* generic intent */
            else {
                if (file.exists()) {
                    if (mReturnIntent) {
                        returnIntentResults(file);

                    } else {
                        Intent generic = new Intent();
                        generic.setAction(Intent.ACTION_VIEW);
                        generic.setDataAndType(Uri.fromFile(file), "text/plain");

                        try {
                            startActivity(generic);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(this, "Sorry, couldn't find anything " +
                                            "to open " + file.getName(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }
}
