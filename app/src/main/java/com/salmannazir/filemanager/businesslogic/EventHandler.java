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

package com.salmannazir.filemanager.businesslogic;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.salmannazir.filemanager.R;
import com.salmannazir.filemanager.activities.DirectoryInformationActivity;
import com.salmannazir.filemanager.activities.HelpManager;
import com.salmannazir.filemanager.activities.SnapshotActivity;
import com.salmannazir.filemanager.prefs.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class EventHandler implements OnClickListener {


    private final Context mContext;
    private final FileManager mFileManger;
    private ThumbnailCreator mThumbnail;
    private ListAdapter mListAdapter;

    private boolean multi_select_flag = false;
    private boolean delete_after_copy = false;
    private boolean thumbnail_flag = true;
    private int mColor = Color.WHITE;

    private String mCurrentPhotoPath;


    //the list used to feed info into the array adapter and when multi-select is on
    private ArrayList<String> mDataSource, mMultiSelectedData;
    private TextView mDirectoryPathLabel;
    private TextView mInformationLabel;

    public EventHandler(Context context, final FileManager mFileManager) {
        mContext = context;
        this.mFileManger = mFileManager;

        mDataSource = new ArrayList<String>(mFileManger.setHomeDir(Constants.HOME_PATH));
    }



    public EventHandler(Context context, final FileManager manager, String location) {
        mContext = context;
        mFileManger = manager;

        mDataSource = new ArrayList<String>(mFileManger.getNextDirectory(location, true));
    }


    public void setListAdapter(ListAdapter adapter) {
        mListAdapter = adapter;
    }


    public void updateLabels(TextView directoryPath, TextView textLabel) {
        mDirectoryPathLabel = directoryPath;
        mInformationLabel = textLabel;
    }


    public void setTextColor(int color) {
        mColor = color;
    }


    public void setShowThumbnails(boolean show) {
        thumbnail_flag = show;
    }

    public void setDeleteAfterCut(boolean delete) {
        delete_after_copy = delete;
    }


    public boolean isMultiSelectedFiles() {
        return multi_select_flag;
    }

    public boolean hasMultiSelectData() {
        return (mMultiSelectedData != null && mMultiSelectedData.size() > 0);
    }


    public void searchFile(String name) {
        new BackgroundWork(Constants.SEARCH_TYPE).execute(name);
    }


    public void deleteFile(String name) {
        new BackgroundWork(Constants.DELETE_TYPE).execute(name);
    }

    public void copyFile(String previousLocation, String newLocation) {
        String[] data = {previousLocation, newLocation};

        new BackgroundWork(Constants.COPY_TYPE).execute(data);
    }


    public void copyMultiSelectFiles(String newLocation) {
        String[] data;
        int index = 1;

        if (mMultiSelectedData.size() > 0) {
            data = new String[mMultiSelectedData.size() + 1];
            data[0] = newLocation;

            for (String s : mMultiSelectedData)
                data[index++] = s;

            new BackgroundWork(Constants.COPY_TYPE).execute(data);
        }
    }


    public void doUnZipFile(String file, String path) {
        new BackgroundWork(Constants.UNZIP_TYPE).execute(file, path);
    }


    public void unZipFileToDirectory(String name, String newDir, String oldDir) {
        new BackgroundWork(Constants.UNZIPTO_TYPE).execute(name, newDir, oldDir);
    }


    public void zipFile(String zipPath) {
        new BackgroundWork(Constants.ZIP_TYPE).execute(zipPath);
    }


    public void stopThumbnailThread() {
        if (mThumbnail != null) {
            mThumbnail.setCancelThumbnails(true);
            mThumbnail = null;
        }
    }

    public void handleBackButton() {
        if (mFileManger.getCurrentDirectory().equals(Constants.HOME_PATH)) {
            if (multi_select_flag) {
                mListAdapter.removeMultiSelect(true);
                Toast.makeText(mContext, "Multi-select option is now off",
                        Toast.LENGTH_SHORT).show();
            }

            stopThumbnailThread();
            updateDirectory(mFileManger.getPreviousDirectory());
            if (mDirectoryPathLabel != null)
                mDirectoryPathLabel.setText(mFileManger.getCurrentDirectory());
        }
    }
    public void handleSnapShot() {
//        Intent snapshot = new Intent(mContext, SnapshotActivity.class);
//        mContext.startActivity(snapshot);
        zipFile(Constants.HOME_PATH);
    }


    public void handleHomeButton() {
        if (multi_select_flag) {
            mListAdapter.removeMultiSelect(true);
            Toast.makeText(mContext, "Multi-select option is now off",
                    Toast.LENGTH_SHORT).show();
        }

        stopThumbnailThread();
        updateDirectory(mFileManger.setHomeDir(Constants.HOME_PATH));
        if (mDirectoryPathLabel != null)
            mDirectoryPathLabel.setText(mFileManger.getCurrentDirectory());
    }

    public void handleInfoButton() {
        Intent info = new Intent(mContext, DirectoryInformationActivity.class);
        info.putExtra("PATH_NAME", mFileManger.getCurrentDirectory());
        mContext.startActivity(info);
    }

    public void handleHelpButton() {
        Intent help = new Intent(mContext, HelpManager.class);
        mContext.startActivity(help);
    }

    public void handleCreateNewFile(final Activity mContext, final String directory) {

        new MaterialDialog.Builder(mContext)
                .title("Please Select One Option to Create")
                .items(R.array.file_options)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int index, CharSequence text) {
                        /**
                         * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected radio button to actually be selected.
                         **/
                        if (index == 0) {
                            Toast.makeText(mContext, "New File Clicked", Toast.LENGTH_SHORT).show();
                            if (checkStoragePermission(mContext)) {
                                showNewFileCreateDialog(mContext);

                            } else
                                requestStoragePermission(mContext);


                        } else if (index == 1) {
                            Toast.makeText(mContext, "Image Clicked", Toast.LENGTH_SHORT).show();
                            showNewImageCreateDialog(mContext, directory);
                        }
                        return false;
                    }
                })
                .positiveText("Create")
                .show();


    }

    public static boolean checkStoragePermission(Context mContext) {
        if (Build.VERSION.SDK_INT >= 23) {
            // Verify that all required contact permissions have been granted.
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            return false;
        }
        return true;
    }

    public static void requestStoragePermission(final Activity mContext) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new MaterialDialog.Builder(mContext)
                    .title("Storage Access Required")
                    .content("Please grant storage access to operate")
                    .positiveText("Grant Permission")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // TODO
                            ActivityCompat
                                    .requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_STORAGE_PERMISSION);
                            dialog.dismiss();
                        }
                    })
                    .negativeText("Cancel")
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mContext.finish();
                        }
                    })
                    .cancelable(false)
                    .show();

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.


        } else {
            // Contact permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_STORAGE_PERMISSION);
        }
    }

    private void showNewImageCreateDialog(final Activity mContext, final String directory) {
        boolean wrapInScrollView = true;

        new MaterialDialog.Builder(mContext)
                .title("Create New Image File")
                .customView(R.layout.new_image_layout, wrapInScrollView)
                .positiveText("Capture Image")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                        Toast.makeText(mContext, "+ve Clicked", Toast.LENGTH_SHORT).show();
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (cameraIntent.resolveActivity(mContext.getPackageManager()) != null) {
                            // Create the File where the photo should go
                            File photoFile = null;
                            try {
                                EditText imageNameEditText = (EditText) dialog.findViewById(R.id.image_name);
                                photoFile = createImageFile(imageNameEditText.getText().toString(), directory);
                            } catch (IOException ex) {
                                // Error occurred while creating the File
                                Log.i("MainActivity", "IOException");
                            }
                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                                mContext.startActivityForResult(cameraIntent, Constants.CAMERA_REQUEST);
                            }
                        }
                    }
                })
                .show();
    }

    private File createImageFile(String imageFileName, String directory) throws IOException {
        String path = directory + "/" + imageFileName + ".jpg";
        File image = new File(path);
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void showNewFileCreateDialog(final Activity mContext) {
        boolean wrapInScrollView = true;
        new MaterialDialog.Builder(mContext)
                .title("Create New Text File")
                .customView(R.layout.new_file_layout, wrapInScrollView)
                .positiveText("Create File")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                        Toast.makeText(mContext, "+ve Clicked", Toast.LENGTH_SHORT).show();
                        EditText mTextFileName = (EditText) dialog.findViewById(R.id.et_file_name);
                        EditText mTextFileBody = (EditText) dialog.findViewById(R.id.et_file_body);

                        if (mTextFileName != null && mTextFileName.length() > 0) {
                            mFileManger.createTextFile(mContext, mTextFileName.getText().toString(),
                                    mTextFileBody.getText().toString());
                            updateDirectory(mFileManger.getNextDirectory(mFileManger.getCurrentDirectory(), true));
                        } else {
                            Toast.makeText(mContext, "Please Enter File Name", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .negativeText("Cancel")
                .show();
    }

    public void handleMultiSelectButton() {
        if (multi_select_flag) {
            mListAdapter.removeMultiSelect(true);

        } else {
            LinearLayout hidden_layout =
                    (LinearLayout) ((Activity) mContext).findViewById(R.id.hidden_buttons);

            multi_select_flag = true;
            hidden_layout.setVisibility(LinearLayout.VISIBLE);
        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.back_button:
                handleBackButton();
                break;

            case R.id.home_button:
                handleHomeButton();
                break;

            case R.id.hidden_move:
            case R.id.hidden_copy:
                /* check if user selected objects before going further */
                if (mMultiSelectedData == null || mMultiSelectedData.isEmpty()) {
                    mListAdapter.removeMultiSelect(true);
                    break;
                }

                if (v.getId() == R.id.hidden_move)
                    delete_after_copy = true;

                mInformationLabel.setText("Holding " + mMultiSelectedData.size() +
                        " file(s)");

                mListAdapter.removeMultiSelect(false);
                break;

            case R.id.hidden_delete:
                /* check if user selected objects before going further */
                if (mMultiSelectedData == null || mMultiSelectedData.isEmpty()) {
                    mListAdapter.removeMultiSelect(true);
                    break;
                }

                final String[] data = new String[mMultiSelectedData.size()];
                int at = 0;

                for (String string : mMultiSelectedData)
                    data[at++] = string;

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("Are you sure you want to delete " +
                        data.length + " files? This cannot be " +
                        "undone after that.");
                builder.setCancelable(false);
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new BackgroundWork(Constants.DELETE_TYPE).execute(data);
                        mListAdapter.removeMultiSelect(true);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListAdapter.removeMultiSelect(true);
                        dialog.cancel();
                    }
                });

                builder.create().show();
                break;
        }
    }

    public String getData(int position) {

        if (position > mDataSource.size() - 1 || position < 0)
            return null;

        return mDataSource.get(position);
    }


    public void updateDirectory(ArrayList<String> content) {
        if (!mDataSource.isEmpty())
            mDataSource.clear();

        for (String data : content)
            mDataSource.add(data);

        mListAdapter.notifyDataSetChanged();
    }




    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
        private final int KB = 1024;
        private final int MG = KB * KB;
        private final int GB = MG * KB;
        private String display_size;
        private ArrayList<Integer> positions;
        private LinearLayout hidden_layout;
        OnClickListener onClickListener;
        View.OnCreateContextMenuListener onCreateContextMenuListener;

        private int position;

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView topView;
            public TextView bottomView;
            public ImageView icon;
            public ImageView mSelectImage;    //multi-select check mark icon

            public ViewHolder(View view) {
                super(view);
                topView = (TextView) view.findViewById(R.id.top_view);
                bottomView = (TextView) view.findViewById(R.id.bottom_view);
                icon = (ImageView) view.findViewById(R.id.row_image);
                mSelectImage = (ImageView) view.findViewById(R.id.multiselect_icon);

                view.setOnClickListener(onClickListener);
                view.setOnCreateContextMenuListener(onCreateContextMenuListener);
            }
        }

        public ListAdapter(OnClickListener onClickListener, View.OnCreateContextMenuListener onCreateContextMenuListener) {
            this.onClickListener = onClickListener;
            this.onCreateContextMenuListener = onCreateContextMenuListener;
        }

        @Override
        public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = inflater.inflate(R.layout.tablerow, parent, false);

            ViewHolder mViewHolder = new ViewHolder(convertView);
            return mViewHolder;
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            holder.itemView.setOnLongClickListener(null);
            super.onViewRecycled(holder);
        }

        @Override
        public void onBindViewHolder(final ListAdapter.ViewHolder mViewHolder, int position) {

            mViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    setPosition(mViewHolder.getAdapterPosition());
                    return false;
                }
            });
            int num_items = 0;
            String temp = mFileManger.getCurrentDirectory();
            File file = new File(temp + "/" + mDataSource.get(position));
            String[] list = file.list();

            if (list != null)
                num_items = list.length;


            if (positions != null && positions.contains(position))
                mViewHolder.mSelectImage.setVisibility(ImageView.VISIBLE);
            else
                mViewHolder.mSelectImage.setVisibility(ImageView.GONE);

            //mViewHolder.topView.setTextColor(mColor);
            //mViewHolder.bottomView.setTextColor(mColor);

            if (mThumbnail == null)
                mThumbnail = new ThumbnailCreator(52, 52);

            if (file != null && file.isFile()) {
                String ext = file.toString();
                String sub_extenstion = ext.substring(ext.lastIndexOf(".") + 1);

    			/* This series of else if statements will determine which
                 * icon is displayed
    			 */
                if (sub_extenstion.equalsIgnoreCase("pdf")) {
                    mViewHolder.icon.setImageResource(R.drawable.pdf);

                } else if (sub_extenstion.equalsIgnoreCase("mp3") ||
                        sub_extenstion.equalsIgnoreCase("wma") ||
                        sub_extenstion.equalsIgnoreCase("m4a") ||
                        sub_extenstion.equalsIgnoreCase("m4p")) {

                    mViewHolder.icon.setImageResource(R.drawable.music);

                } else if (sub_extenstion.equalsIgnoreCase("png") ||
                        sub_extenstion.equalsIgnoreCase("jpg") ||
                        sub_extenstion.equalsIgnoreCase("jpeg") ||
                        sub_extenstion.equalsIgnoreCase("gif") ||
                        sub_extenstion.equalsIgnoreCase("tiff")) {

                    if (thumbnail_flag && file.length() != 0) {
                        Bitmap thumb = mThumbnail.isBitmapCached(file.getPath());

                        if (thumb == null) {
                            final Handler handle = new Handler(new Handler.Callback() {
                                public boolean handleMessage(Message msg) {
                                    notifyDataSetChanged();

                                    return true;
                                }
                            });

                            mThumbnail.createNewThumbnail(mDataSource, mFileManger.getCurrentDirectory(), handle);

                            if (!mThumbnail.isAlive())
                                mThumbnail.start();

                        } else {
                            mViewHolder.icon.setImageBitmap(thumb);
                        }

                    } else {
                        mViewHolder.icon.setImageResource(R.drawable.image);
                    }

                } else if (sub_extenstion.equalsIgnoreCase("zip") ||
                        sub_extenstion.equalsIgnoreCase("gzip") ||
                        sub_extenstion.equalsIgnoreCase("gz")) {

                    mViewHolder.icon.setImageResource(R.drawable.zip);

                } else if (sub_extenstion.equalsIgnoreCase("m4v") ||
                        sub_extenstion.equalsIgnoreCase("wmv") ||
                        sub_extenstion.equalsIgnoreCase("3gp") ||
                        sub_extenstion.equalsIgnoreCase("mp4")) {

                    mViewHolder.icon.setImageResource(R.drawable.movies);

                } else if (sub_extenstion.equalsIgnoreCase("doc") ||
                        sub_extenstion.equalsIgnoreCase("docx")) {

                    mViewHolder.icon.setImageResource(R.drawable.word);

                } else if (sub_extenstion.equalsIgnoreCase("xls") ||
                        sub_extenstion.equalsIgnoreCase("xlsx")) {

                    mViewHolder.icon.setImageResource(R.drawable.excel);

                } else if (sub_extenstion.equalsIgnoreCase("ppt") ||
                        sub_extenstion.equalsIgnoreCase("pptx")) {

                    mViewHolder.icon.setImageResource(R.drawable.ppt);

                } else if (sub_extenstion.equalsIgnoreCase("html")) {
                    mViewHolder.icon.setImageResource(R.drawable.html32);

                } else if (sub_extenstion.equalsIgnoreCase("xml")) {
                    mViewHolder.icon.setImageResource(R.drawable.xml32);

                } else if (sub_extenstion.equalsIgnoreCase("conf")) {
                    mViewHolder.icon.setImageResource(R.drawable.config32);

                } else if (sub_extenstion.equalsIgnoreCase("apk")) {
                    mViewHolder.icon.setImageResource(R.drawable.appicon);

                } else if (sub_extenstion.equalsIgnoreCase("jar")) {
                    mViewHolder.icon.setImageResource(R.drawable.jar32);

                } else {
                    mViewHolder.icon.setImageResource(R.drawable.text);
                }

            } else if (file != null && file.isDirectory()) {
                if (file.canRead() && file.list() != null && file.list().length > 0)
                    mViewHolder.icon.setImageResource(R.drawable.folder_full);
                else
                    mViewHolder.icon.setImageResource(R.drawable.folder);
            }

            String permission = getFilePermissions(file);

            if (file.isFile()) {
                double size = file.length();
                if (size > GB)
                    display_size = String.format("%.2f Gb ", (double) size / GB);
                else if (size < GB && size > MG)
                    display_size = String.format("%.2f Mb ", (double) size / MG);
                else if (size < MG && size > KB)
                    display_size = String.format("%.2f Kb ", (double) size / KB);
                else
                    display_size = String.format("%.2f bytes ", (double) size);

                if (file.isHidden())
                    mViewHolder.bottomView.setText("(hidden) | " + display_size + " | " + permission);
                else
                    mViewHolder.bottomView.setText(display_size + " | " + permission);

            } else {
                if (file.isHidden())
                    mViewHolder.bottomView.setText("(hidden) | " + num_items + " items | " + permission);
                else
                    mViewHolder.bottomView.setText(num_items + " items | " + permission);
            }

            mViewHolder.topView.setText(file.getName());

        }


        @Override
        public int getItemCount() {
            return mDataSource.size();
        }

        public void addMultiPosition(int index, String path) {
            if (positions == null)
                positions = new ArrayList<Integer>();

            if (mMultiSelectedData == null) {
                positions.add(index);
                add_multiSelect_file(path);

            } else if (mMultiSelectedData.contains(path)) {
                if (positions.contains(index))
                    positions.remove(new Integer(index));

                mMultiSelectedData.remove(path);

            } else {
                positions.add(index);
                add_multiSelect_file(path);
            }

            notifyDataSetChanged();
        }

        public void removeMultiSelect(boolean clearData) {
            hidden_layout = (LinearLayout) ((Activity) mContext).findViewById(R.id.hidden_buttons);
            hidden_layout.setVisibility(LinearLayout.GONE);
            multi_select_flag = false;

            if (positions != null && !positions.isEmpty())
                positions.clear();

            if (clearData)
                if (mMultiSelectedData != null && !mMultiSelectedData.isEmpty())
                    mMultiSelectedData.clear();

            notifyDataSetChanged();
        }

        public String getFilePermissions(File file) {
            String per = "-";

            if (file.isDirectory())
                per += "d";
            if (file.canRead())
                per += "r";
            if (file.canWrite())
                per += "w";

            return per;
        }


        private void add_multiSelect_file(String src) {
            if (mMultiSelectedData == null)
                mMultiSelectedData = new ArrayList<String>();

            mMultiSelectedData.add(src);
        }
    }


    private class BackgroundWork extends AsyncTask<String, Void, ArrayList<String>> {
        private String file_name;
        private ProgressDialog mProgressDialog;
        private int type;
        private int copy_rtn;

        private BackgroundWork(int type) {
            this.type = type;
        }


        @Override
        protected void onPreExecute() {

            switch (type) {
                case Constants.SEARCH_TYPE:
                    mProgressDialog = ProgressDialog.show(mContext, "Searching",
                            "Searching current file system...",
                            true, true);
                    break;

                case Constants.COPY_TYPE:
                    mProgressDialog = ProgressDialog.show(mContext, "Copying",
                            "Copying file...",
                            true, false);
                    break;

                case Constants.UNZIP_TYPE:
                    mProgressDialog = ProgressDialog.show(mContext, "Unzipping",
                            "Unpacking zip file please wait...",
                            true, false);
                    break;

                case Constants.UNZIPTO_TYPE:
                    mProgressDialog = ProgressDialog.show(mContext, "Unzipping",
                            "Unpacking zipped file please wait...",
                            true, false);
                    break;

                case Constants.ZIP_TYPE:
                    mProgressDialog = ProgressDialog.show(mContext, "Zipping",
                            "Zipping uncompressed folder...",
                            true, false);
                    break;

                case Constants.DELETE_TYPE:
                    mProgressDialog = ProgressDialog.show(mContext, "Deleting",
                            "Deleting files Please wait...",
                            true, false);
                    break;
            }
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            switch (type) {
                case Constants.SEARCH_TYPE:
                    file_name = params[0];
                    ArrayList<String> found = mFileManger.searchInDirectory(mFileManger.getCurrentDirectory(),
                            file_name);
                    return found;

                case Constants.COPY_TYPE:
                    int len = params.length;

                    if (mMultiSelectedData != null && !mMultiSelectedData.isEmpty()) {
                        for (int i = 1; i < len; i++) {
                            copy_rtn = mFileManger.doCopyToDirectory(params[i], params[0]);

                            if (delete_after_copy)
                                mFileManger.deleteTargetFile(params[i]);
                        }
                    } else {
                        copy_rtn = mFileManger.doCopyToDirectory(params[0], params[1]);

                        if (delete_after_copy)
                            mFileManger.deleteTargetFile(params[0]);
                    }

                    delete_after_copy = false;
                    return null;

                case Constants.UNZIP_TYPE:
                    mFileManger.extractZipFiles(params[0], params[1]);
                    return null;

                case Constants.UNZIPTO_TYPE:
                    mFileManger.extractZipFilesFromDir(params[0], params[1], params[2]);
                    return null;

                case Constants.ZIP_TYPE:
               //     mFileManger.createZipFile(params[0]);
                    mFileManger.zipFileAtPath(Constants.HOME_PATH,Constants.HOME_PATH+"/Kueski.zip");
                    return null;

                case Constants.DELETE_TYPE:
                    int size = params.length;

                    for (int i = 0; i < size; i++)
                        mFileManger.deleteTargetFile(params[i]);

                    return null;
            }
            return null;
        }


        @Override
        protected void onPostExecute(final ArrayList<String> file) {
            final CharSequence[] names;
            int len = file != null ? file.size() : 0;

            switch (type) {
                case Constants.SEARCH_TYPE:
                    if (len == 0) {
                        Toast.makeText(mContext, "Couldn't find file" + file_name,
                                Toast.LENGTH_SHORT).show();

                    } else {
                        names = new CharSequence[len];

                        for (int i = 0; i < len; i++) {
                            String entry = file.get(i);
                            names[i] = entry.substring(entry.lastIndexOf("/") + 1, entry.length());
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("Found " + len + " file(s)");
                        builder.setItems(names, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int position) {
                                String path = file.get(position);
                                updateDirectory(mFileManger.getNextDirectory(path.
                                        substring(0, path.lastIndexOf("/")), true));
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                    mProgressDialog.dismiss();
                    break;

                case Constants.COPY_TYPE:
                    if (mMultiSelectedData != null && !mMultiSelectedData.isEmpty()) {
                        multi_select_flag = false;
                        mMultiSelectedData.clear();
                    }

                    if (copy_rtn == 0)
                        Toast.makeText(mContext, "File successfully copied and pasted",
                                Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(mContext, "Copy pasted failed", Toast.LENGTH_SHORT).show();

                    mProgressDialog.dismiss();
                    mInformationLabel.setText("");
                    updateDirectory(mFileManger.getNextDirectory(mFileManger.getCurrentDirectory(), true));
                    break;

                case Constants.UNZIP_TYPE:
                    updateDirectory(mFileManger.getNextDirectory(mFileManger.getCurrentDirectory(), true));
                    mProgressDialog.dismiss();
                    break;

                case Constants.UNZIPTO_TYPE:
                    updateDirectory(mFileManger.getNextDirectory(mFileManger.getCurrentDirectory(), true));
                    mProgressDialog.dismiss();
                    break;

                case Constants.ZIP_TYPE:
                    updateDirectory(mFileManger.getNextDirectory(mFileManger.getCurrentDirectory(), true));
                    mProgressDialog.dismiss();
                    break;

                case Constants.DELETE_TYPE:
                    if (mMultiSelectedData != null && !mMultiSelectedData.isEmpty()) {
                        mMultiSelectedData.clear();
                        multi_select_flag = false;
                    }

                    updateDirectory(mFileManger.getNextDirectory(mFileManger.getCurrentDirectory(), true));
                    mProgressDialog.dismiss();
                    mInformationLabel.setText("");
                    break;
            }
        }
    }
}
