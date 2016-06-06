/*
    Open Manager For Tablets, an open source file manager for the Android system
    Copyright (C) 2011  Joe Berria <nexesdevelopment@gmail.com>

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

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.salmannazir.filemanager.prefs.Constants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by Salman Nazir on 06/06/2016.
 */

public class FileManager {


    private boolean mShowHiddenFiles = false;
    private int mDirectorySortType = Constants.SORT_ALPHA;
    private long mDirectorySize = 0;
    private Stack<String> mDirectoryPathStack;
    private ArrayList<String> mDirectoryContent;


    public FileManager() {
        mDirectoryContent = new ArrayList<String>();
        mDirectoryPathStack = new Stack<String>();


        createDirectory(Environment.getExternalStorageDirectory().getPath(), "Kueski");
        createDirectory(Environment.getExternalStorageDirectory().getPath(), "Kueski/snapshots");
        createDirectory(Environment.getExternalStorageDirectory().getPath(), "Kueski/data");
//		mDirectoryPathStack.push("/");
        mDirectoryPathStack.push(Constants.HOME_PATH);
    }


    public String getCurrentDirectory() {
        return mDirectoryPathStack.peek();
    }


    public ArrayList<String> setHomeDir(String name) {
        //This will eventually be placed as a settings item
        mDirectoryPathStack.clear();
//		mDirectoryPathStack.push("/");
        mDirectoryPathStack.push(name);

        return populateList();
    }


    public void setShowHiddenFiles(boolean choice) {
        mShowHiddenFiles = choice;
    }


    public void setDirectorySortType(int type) {
        mDirectorySortType = type;
    }


    public ArrayList<String> getPreviousDirectory() {
        int size = mDirectoryPathStack.size();

        if (size >= 2)
            mDirectoryPathStack.pop();

        else if (size == 0)
            mDirectoryPathStack.push(Constants.HOME_PATH);

        return populateList();
    }


    public ArrayList<String> getNextDirectory(String path, boolean isFullPath) {
        int size = mDirectoryPathStack.size();

        if (!path.equals(mDirectoryPathStack.peek()) && !isFullPath) {
            if (size == 1)
                mDirectoryPathStack.push(Constants.HOME_PATH + "/" + path);
            else
                mDirectoryPathStack.push(mDirectoryPathStack.peek() + "/" + path);
        } else if (!path.equals(mDirectoryPathStack.peek()) && isFullPath) {
            mDirectoryPathStack.push(path);
        }

        return populateList();
    }


    public int doCopyToDirectory(String oldDirectory, String newDirectory) {
        File old_file = new File(oldDirectory);
        File temp_dir = new File(newDirectory);
        byte[] data = new byte[Constants.BUFFER];
        int read = 0;

        if (old_file.isFile() && temp_dir.isDirectory() && temp_dir.canWrite()) {
            String file_name = oldDirectory.substring(oldDirectory.lastIndexOf("/"), oldDirectory.length());
            File cp_file = new File(newDirectory + file_name);

            try {
                BufferedOutputStream mBufferedOutputStream = new BufferedOutputStream(
                        new FileOutputStream(cp_file));
                BufferedInputStream mBufferedInputStream = new BufferedInputStream(
                        new FileInputStream(old_file));

                while ((read = mBufferedInputStream.read(data, 0, Constants.BUFFER)) != -1)
                    mBufferedOutputStream.write(data, 0, read);

                mBufferedOutputStream.flush();
                mBufferedInputStream.close();
                mBufferedOutputStream.close();

            } catch (FileNotFoundException e) {
                Log.e("FileNotFoundException", e.getMessage());
                return -1;

            } catch (IOException e) {
                Log.e("IOException", e.getMessage());
                return -1;
            }

        } else if (old_file.isDirectory() && temp_dir.isDirectory() && temp_dir.canWrite()) {
            String files[] = old_file.list();
            String dir = newDirectory + oldDirectory.substring(oldDirectory.lastIndexOf("/"), oldDirectory.length());
            int len = files.length;

            if (!new File(dir).mkdir())
                return -1;

            for (int i = 0; i < len; i++)
                doCopyToDirectory(oldDirectory + "/" + files[i], dir);

        } else if (!temp_dir.canWrite())
            return -1;

        return 0;
    }


    public void extractZipFilesFromDir(String zipName, String toDir, String fromDir) {
        if (!(toDir.charAt(toDir.length() - 1) == '/'))
            toDir += "/";
        if (!(fromDir.charAt(fromDir.length() - 1) == '/'))
            fromDir += "/";

        String org_path = fromDir + zipName;

        extractZipFiles(org_path, toDir);
    }


    public void extractZipFiles(String zip_file, String directory) {
        byte[] data = new byte[Constants.BUFFER];
        String name, path, zipDir;
        ZipEntry entry;
        ZipInputStream zipstream;

        if (!(directory.charAt(directory.length() - 1) == '/'))
            directory += "/";

        if (zip_file.contains("/")) {
            path = zip_file;
            name = path.substring(path.lastIndexOf("/") + 1,
                    path.length() - 4);
            zipDir = directory + name + "/";

        } else {
            path = directory + zip_file;
            name = path.substring(path.lastIndexOf("/") + 1,
                    path.length() - 4);
            zipDir = directory + name + "/";
        }

        new File(zipDir).mkdir();

        try {
            zipstream = new ZipInputStream(new FileInputStream(path));

            while ((entry = zipstream.getNextEntry()) != null) {
                String buildDir = zipDir;
                String[] dirs = entry.getName().split("/");

                if (dirs != null && dirs.length > 0) {
                    for (int i = 0; i < dirs.length - 1; i++) {
                        buildDir += dirs[i] + "/";
                        new File(buildDir).mkdir();
                    }
                }

                int read = 0;
                FileOutputStream out = new FileOutputStream(
                        zipDir + entry.getName());
                while ((read = zipstream.read(data, 0, Constants.BUFFER)) != -1)
                    out.write(data, 0, read);

                zipstream.closeEntry();
                out.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean zipFileAtPath(String sourcePath, String toLocation) {
        final int BUFFER = 2048;

        File sourceFile = new File(sourcePath);
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());
            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void zipSubFolder(ZipOutputStream out, File folder,
                              int basePathLength) throws IOException {

        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    public String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }

    public int renameTargetFile(String filePath, String newFileName) {
        File src = new File(filePath);
        String ext = "";
        File dest;

        if (src.isFile())
            /*get file extension*/
            if (filePath.contains("."))
                ext = filePath.substring(filePath.lastIndexOf("."), filePath.length());

        if (newFileName.length() < 1)
            return -1;

        String temp = filePath.substring(0, filePath.lastIndexOf("/"));

        dest = new File(temp + "/" + newFileName + ext);
        if (src.renameTo(dest))
            return 0;
        else
            return -1;
    }


    public int createDirectory(String path, String name) {
        int len = path.length();

        if (len < 1 || len < 1)
            return -1;

        if (path.charAt(len - 1) != '/')
            path += "/";
        File file = new File(path + name);

        if (!file.exists() && file.mkdir())
            return 0;

        return -1;
    }

    public void createTextFile(Context context, String sFileName, String sBody) {
        try {
//            File root = new File(Environment.getExternalStorageDirectory(), "Text File");
            //	File root = new File(getCurrentDirectory(), "Text File");

//			if (!root.exists()) {
//                root.mkdirs();
//            }
            File gpxfile = new File(getCurrentDirectory(), sFileName.contains(".") ? sFileName : sFileName + ".txt");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();

            Toast.makeText(context, "Text File Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int deleteTargetFile(String path) {
        File target = new File(path);

        if (target.exists() && target.isFile() && target.canWrite()) {
            target.delete();
            return 0;
        } else if (target.exists() && target.isDirectory() && target.canRead()) {
            String[] file_list = target.list();

            if (file_list != null && file_list.length == 0) {
                target.delete();
                return 0;

            } else if (file_list != null && file_list.length > 0) {

                for (int i = 0; i < file_list.length; i++) {
                    File temp_f = new File(target.getAbsolutePath() + "/" + file_list[i]);

                    if (temp_f.isDirectory())
                        deleteTargetFile(temp_f.getAbsolutePath());
                    else if (temp_f.isFile())
                        temp_f.delete();
                }
            }
            if (target.exists())
                if (target.delete())
                    return 0;
        }
        return -1;
    }


    public boolean isDirectory(String file) {
        return new File(mDirectoryPathStack.peek() + "/" + file).isDirectory();
    }


    public static String integerToIPAddress(int ipAdress) {
        String ascii_address = "";
        int[] num = new int[4];

        num[0] = (ipAdress & 0xff000000) >> 24;
        num[1] = (ipAdress & 0x00ff0000) >> 16;
        num[2] = (ipAdress & 0x0000ff00) >> 8;
        num[3] = ipAdress & 0x000000ff;

        ascii_address = num[0] + "." + num[1] + "." + num[2] + "." + num[3];

        return ascii_address;
    }

    public ArrayList<String> searchInDirectory(String dir, String pathName) {
        ArrayList<String> names = new ArrayList<String>();
        searchFile(dir, pathName, names);

        return names;
    }

    public long getDirectorySize(String path) {
        getDirSize(new File(path));

        return mDirectorySize;
    }


    private static final Comparator alph = new Comparator<String>() {
        @Override
        public int compare(String arg0, String arg1) {
            return arg0.toLowerCase().compareTo(arg1.toLowerCase());
        }
    };

    private final Comparator size = new Comparator<String>() {
        @Override
        public int compare(String arg0, String arg1) {
            String dir = mDirectoryPathStack.peek();
            Long first = new File(dir + "/" + arg0).length();
            Long second = new File(dir + "/" + arg1).length();

            return first.compareTo(second);
        }
    };

    private final Comparator type = new Comparator<String>() {
        @Override
        public int compare(String arg0, String arg1) {
            String ext = null;
            String ext2 = null;
            int ret;

            try {
                ext = arg0.substring(arg0.lastIndexOf(".") + 1, arg0.length()).toLowerCase();
                ext2 = arg1.substring(arg1.lastIndexOf(".") + 1, arg1.length()).toLowerCase();

            } catch (IndexOutOfBoundsException e) {
                return 0;
            }
            ret = ext.compareTo(ext2);

            if (ret == 0)
                return arg0.toLowerCase().compareTo(arg1.toLowerCase());

            return ret;
        }
    };


    private ArrayList<String> populateList() {

        if (!mDirectoryContent.isEmpty())
            mDirectoryContent.clear();

        File file = new File(mDirectoryPathStack.peek());

        if (file.exists() && file.canRead()) {
            String[] list = file.list();
            int len = list.length;

			/* add files/folder to arraylist depending on hidden status */
            for (int i = 0; i < len; i++) {
                if (!mShowHiddenFiles) {
                    if (list[i].toString().charAt(0) != '.')
                        mDirectoryContent.add(list[i]);

                } else {
                    mDirectoryContent.add(list[i]);
                }
            }

			/* sort the arraylist that was made from above for loop */
            switch (mDirectorySortType) {
                case Constants.SORT_NONE:
                    //no sorting needed
                    break;

                case Constants.SORT_ALPHA:
                    Object[] tt = mDirectoryContent.toArray();
                    mDirectoryContent.clear();

                    Arrays.sort(tt, alph);

                    for (Object a : tt) {
                        mDirectoryContent.add((String) a);
                    }
                    break;

                case Constants.SORT_SIZE:
                    int index = 0;
                    Object[] size_ar = mDirectoryContent.toArray();
                    String dir = mDirectoryPathStack.peek();

                    Arrays.sort(size_ar, size);

                    mDirectoryContent.clear();
                    for (Object a : size_ar) {
                        if (new File(dir + "/" + (String) a).isDirectory())
                            mDirectoryContent.add(index++, (String) a);
                        else
                            mDirectoryContent.add((String) a);
                    }
                    break;

                case Constants.SORT_TYPE:
                    int dirIndex = 0;
                    Object[] type_ar = mDirectoryContent.toArray();
                    String current = mDirectoryPathStack.peek();

                    Arrays.sort(type_ar, type);
                    mDirectoryContent.clear();

                    for (Object a : type_ar) {
                        if (new File(current + "/" + (String) a).isDirectory())
                            mDirectoryContent.add(dirIndex++, (String) a);
                        else
                            mDirectoryContent.add((String) a);
                    }
                    break;
            }

        } else {
            mDirectoryContent.add("Emtpy");
        }

        return mDirectoryContent;
    }


    private void zip_folder(File file, ZipOutputStream zout) throws IOException {
        byte[] data = new byte[Constants.BUFFER];
        int read;

        if (file.isFile()) {
            ZipEntry entry = new ZipEntry(file.getName());
            zout.putNextEntry(entry);
            BufferedInputStream instream = new BufferedInputStream(
                    new FileInputStream(file));

            while ((read = instream.read(data, 0, Constants.BUFFER)) != -1)
                zout.write(data, 0, read);

            zout.closeEntry();
            instream.close();

        } else if (file.isDirectory()) {
            String[] list = file.list();
            int len = list.length;

            for (int i = 0; i < len; i++)
                zip_folder(new File(file.getPath() + "/" + list[i]), zout);
        }
    }


    private void getDirSize(File path) {
        File[] list = path.listFiles();
        int len;

        if (list != null) {
            len = list.length;

            for (int i = 0; i < len; i++) {
                try {
                    if (list[i].isFile() && list[i].canRead()) {
                        mDirectorySize += list[i].length();

                    } else if (list[i].isDirectory() && list[i].canRead() && !isSymlink(list[i])) {
                        getDirSize(list[i]);
                    }
                } catch (IOException e) {
                    Log.e("IOException", e.getMessage());
                }
            }
        }
    }

    // Inspired by org.apache.commons.io.FileUtils.isSymlink()
    private static boolean isSymlink(File file) throws IOException {
        File fileInCanonicalDir = null;
        if (file.getParent() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }
        return !fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
    }

    private void searchFile(String dir, String fileName, ArrayList<String> n) {
        File root_dir = new File(dir);
        String[] list = root_dir.list();

        if (list != null && root_dir.canRead()) {
            int len = list.length;

            for (int i = 0; i < len; i++) {
                File check = new File(dir + "/" + list[i]);
                String name = check.getName();

                if (check.isFile() && name.toLowerCase().
                        contains(fileName.toLowerCase())) {
                    n.add(check.getPath());
                } else if (check.isDirectory()) {
                    if (name.toLowerCase().contains(fileName.toLowerCase()))
                        n.add(check.getPath());

                    else if (check.canRead() && !dir.equals(Constants.HOME_PATH))
                        searchFile(check.getAbsolutePath(), fileName, n);
                }
            }
        }
    }


    public static void addDirToZipArchive(ZipOutputStream zos, File fileToZip, String parrentDirectoryName) throws Exception {
        if (fileToZip == null || !fileToZip.exists()) {
            return;
        }

        String zipEntryName = fileToZip.getName();
        if (parrentDirectoryName != null && !parrentDirectoryName.isEmpty()) {
            zipEntryName = parrentDirectoryName + "/" + fileToZip.getName();
        }

        if (fileToZip.isDirectory()) {
            System.out.println("+" + zipEntryName);
            for (File file : fileToZip.listFiles()) {
                addDirToZipArchive(zos, file, zipEntryName);
            }
        } else {
            System.out.println("   " + zipEntryName);
            byte[] buffer = new byte[1024];
            FileInputStream fis = new FileInputStream(fileToZip);
            zos.putNextEntry(new ZipEntry(zipEntryName));
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
            fis.close();
        }
    }

    public static boolean zipDirectory(String srcDir, String outputZipFile) {
        try {
            FileOutputStream fos = new FileOutputStream(outputZipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            addDirToZipArchive(zos, new File(srcDir), null);
            zos.flush();
            fos.flush();
            zos.close();
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
