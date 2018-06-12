package com.vinit.foldernaut.fragments;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.vinit.foldernaut.ClickSound;
import com.vinit.foldernaut.FileMovementStatusFeedback;
import com.vinit.foldernaut.FileMovementStatusUpdater;
import com.vinit.foldernaut.OnBackPressedListener;
import com.vinit.foldernaut.R;
import com.vinit.foldernaut.UserInputDialog;
import com.vinit.foldernaut.YesNoDialog;
import com.vinit.foldernaut.adapters.FileAdapter;
import com.vinit.foldernaut.adapters.RecyclerViewClickListener;
import com.vinit.foldernaut.adapters.RecyclerViewLongClickListener;
import com.vinit.foldernaut.objects.FileObject;
import com.vinit.foldernaut.objects.UserInputDialogClickListener;
import com.vinit.foldernaut.objects.YesNoDialogClickListener;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ExploreFragment extends Fragment implements RecyclerViewClickListener, RecyclerViewLongClickListener,
        OnBackPressedListener, YesNoDialogClickListener, UserInputDialogClickListener {

    private List<FileObject> fileObjectList = new ArrayList<>();
    private List<FileObject> selectedFiles = new ArrayList<>();
    private List<FileObject> tobeDeletedFiles = new ArrayList<>();
    private Menu menu;
    FileAdapter fileAdapter;
    String parentDir;
    TextView pathAtTop;
    String currentWorkingDirectory = Environment.getExternalStorageDirectory().toString(); // Default to SD root
    android.support.v7.widget.Toolbar toolbar;
    boolean cabMenuEnabled = false;
    boolean cabMenuRequired = false;
    boolean cut = false;
    RecyclerView explorerRv;
    private List<Integer> scrollPos = new ArrayList<Integer>();
    ViewGroup vg = null;
    YesNoDialog yesNoDialog;
    UserInputDialog userInputDialog;

    public ExploreFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        vg = container;
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        toolbar = (android.support.v7.widget.Toolbar) getActivity().findViewById(R.id.toolbar);
        setHasOptionsMenu(true); // Otherwise, the new menu won't be inflated
        //((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
        pathAtTop = (TextView)view.findViewById(R.id.explorertext);
        explorerRv = (RecyclerView)view.findViewById(R.id.recycler_view_explorer);
        RecyclerView.LayoutManager lm = new GridLayoutManager(getActivity().getApplicationContext(),1);
        listFiles(Environment.getExternalStorageDirectory().toString()+"/");
        parentDir = ""; // will be used for navigating up in file tree
        fileAdapter = new FileAdapter(fileObjectList, getActivity().getApplicationContext(),
                this, this);
        explorerRv.setLayoutManager(lm);
        explorerRv.setHasFixedSize(true);
        explorerRv.setAdapter(fileAdapter);
        fileAdapter.notifyDataSetChanged();

        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if(fab!=null) {

            explorerRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                        fab.hide();
                    } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                        fab.show();
                    }
                }
            });

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String message;

                    Drawable drawable = fab.getDrawable();
                    if (drawable instanceof Animatable) {
                        ((Animatable) drawable).start();
                    }

                    if(!(new File(currentWorkingDirectory+"New Folder")).exists()) {
                       if((new File(currentWorkingDirectory+"New Folder")).mkdir()) {
                           message = "New folder created";
                           fileObjectList.clear();
                           toolbar.getMenu().clear();
                           toolbar.inflateMenu(R.menu.fragment_explore_menu);
                           fileAdapter.setActionMode(false);
                           fileAdapter.notifyDataSetChanged(); // Clear the adapter first
                           listFiles(currentWorkingDirectory); // Build up the child directory
                           parentDir = (new File(currentWorkingDirectory)).getParent();
                           fileAdapter.notifyDataSetChanged(); // Populate recyclerview with child directory
                       }
                       else
                           message = "Could not add the new folder!";
                    }
                    else
                        message = "A folder with the same name already exists";

                    Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }
            });
        }

        return view;
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        (new ClickSound(getActivity().getApplicationContext(), R.raw.buttonglassmp3)).play();
        //add scroll pos to stack
        scrollPos.add(((GridLayoutManager)explorerRv.getLayoutManager()).findFirstCompletelyVisibleItemPosition());

        switch (v.getId()) {
            case R.id.list_item_folder_icon:

                break;
            case R.id.list_item_folder_name:

                break;
            case R.id.list_item_container:
                toolbar.getMenu().clear();
                if(cabMenuEnabled){
                    if(cabMenuRequired) {
                        toolbar.inflateMenu(R.menu.toolbar_contextual_menu);
                    } else {
                        // Exit the context action mode
                        toolbar.inflateMenu(R.menu.fragment_explore_menu);
                        fileAdapter.setActionMode(false); // Remove checkboxes
                        fileAdapter.notifyDataSetChanged();
                    }
                } else {
                    toolbar.inflateMenu(R.menu.fragment_explore_menu);
                }

                String selectedRoot = fileObjectList.get(position).getFilepath();
                if(fileObjectList.get(position).isDirectory()) {
                    fileObjectList.clear();
                    fileAdapter.setActionMode(false); // Open child directory with no checkboxes
                    fileAdapter.notifyDataSetChanged(); // Clear the adapter first
                    listFiles(selectedRoot+"/"); // Build up the child directory
                    parentDir = (new File(selectedRoot+"/")).getParent();
                    fileAdapter.notifyDataSetChanged(); // Populate recyclerview with child directory
                }
                else {
                    //IS NOT A DIRECTORY. HANDLE THE FILE
                    MimeTypeMap myMime = MimeTypeMap.getSingleton();
                    Intent newIntent = new Intent(Intent.ACTION_VIEW);
                    String mimeType = myMime.getMimeTypeFromExtension(fileExt(fileObjectList.get(position).getFilepath().substring(1)));
                    newIntent.setDataAndType(Uri.fromFile(new File(fileObjectList.get(position).getFilepath())),mimeType);
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        getActivity().getApplicationContext().startActivity(newIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getActivity().getApplicationContext(), "No handler for this type of file.", Toast.LENGTH_LONG).show();
                    }
                }
                break;

            case R.id.list_item_folder_checkBox:
                if(!fileObjectList.get(position).isSelected()) { //if the file is not previously selected
                    //Toast.makeText(getActivity().getApplicationContext(),"Checked at pos "+position, Toast.LENGTH_SHORT).show();
                    fileObjectList.get(position).setSelected(true); //mark the file as selected
                    selectedFiles.add(fileObjectList.get(position)); //Add to selected files list
                }
                else {
                    //Toast.makeText(getActivity().getApplicationContext(),"Un-Checked at pos "+position, Toast.LENGTH_SHORT).show();
                    fileObjectList.get(position).setSelected(false); //mark the file as Un-selected
                    selectedFiles.remove(fileObjectList.get(position));
                }

                fileAdapter.notifyDataSetChanged();
        }
    }

    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    @Override
    public void recyclerViewListLongClicked(View v, int position) {
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.toolbar_contextual_menu);
        cabMenuEnabled = true;
        fileAdapter.setActionMode(true); // Show checkboxes and context menu

        if (menu != null) {
            for (int i = 0; i < menu.size(); i++) {
                Drawable drawable = menu.getItem(i).getIcon();
                if (drawable instanceof Animatable) {
                    ((Animatable) drawable).start();
                }
            }
        }

        for(FileObject file: fileObjectList)
            file.setSelected(false); // Unmark all previously selected files
        selectedFiles.clear(); // Clear selected files list
        fileAdapter.notifyDataSetChanged(); // tell adapter to show checkboxes (setActionMode=true)

        // Now select the file which was long clicked
        if(!fileObjectList.get(position).isSelected()) { //if the file is not previously selected
            Toast.makeText(getActivity().getApplicationContext(),"Checked at pos "+position, Toast.LENGTH_SHORT).show();
            fileObjectList.get(position).setSelected(true); //mark the file as selected
            selectedFiles.add(fileObjectList.get(position)); //Add to selected files list
        }
        else {
            Toast.makeText(getActivity().getApplicationContext(),"Un-Checked at pos "+position, Toast.LENGTH_SHORT).show();
            fileObjectList.get(position).setSelected(false); //mark the file as Un-selected
            selectedFiles.remove(fileObjectList.get(position));
        }

        fileAdapter.notifyDataSetChanged();

    }

    @Override
    public void onBackPressed() {
        try {
            ((GridLayoutManager) explorerRv.getLayoutManager()).scrollToPositionWithOffset(scrollPos.get(scrollPos.size()-1),0);
            scrollPos.remove(scrollPos.size()-1); //forget the scroll position
        } catch (Exception e) {}

        toolbar.getMenu().clear();
        if(cabMenuEnabled && cabMenuRequired) {
            toolbar.inflateMenu(R.menu.toolbar_contextual_menu);
        }
        else {
            toolbar.inflateMenu(R.menu.fragment_explore_menu);
        }

        if(fileAdapter.actionModeEnabled()) { // If in context menu mode, back press will exit this mode first
            fileAdapter.setActionMode(false); // Remove checkboxes
            fileAdapter.notifyDataSetChanged();
        } else {
            try {
                if(listFiles(parentDir)!=0) {
                    fileObjectList.clear();
                    fileAdapter.setActionMode(false); // Clear checkboxes if displayed
                    fileAdapter.notifyDataSetChanged();
                    listFiles(parentDir+"/");
                    fileAdapter.notifyDataSetChanged();
                    parentDir = (new File(parentDir+"/")).getParent();
                }
                else {
                    Toast.makeText(getActivity().getApplicationContext(),"Can't go up", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            }
            catch (Exception e) {
                Toast.makeText(getActivity().getApplicationContext(),"Can't go up", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear(); //To remove menu items from other fragments
        inflater.inflate(R.menu.fragment_explore_menu, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }

        this.menu = menu;

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        (new ClickSound(getActivity().getApplicationContext(), R.raw.buttonglassmp3)).play();

        Drawable drawable = item.getIcon();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }

        int id = item.getItemId();

        String message = "";

        if (id == R.id.action_new_folder) {
            if(!(new File(currentWorkingDirectory+"New Folder")).exists()) {
                if((new File(currentWorkingDirectory+"New Folder")).mkdir()) {
                    message = "New folder created";
                    fileObjectList.clear();
                    toolbar.getMenu().clear();
                    toolbar.inflateMenu(R.menu.fragment_explore_menu);
                    fileAdapter.setActionMode(false);
                    fileAdapter.notifyDataSetChanged(); // Clear the adapter first
                    listFiles(currentWorkingDirectory); // Build up the child directory
                    parentDir = (new File(currentWorkingDirectory)).getParent();
                    fileAdapter.notifyDataSetChanged(); // Populate recyclerview with child directory
                }
                else
                    message = "Could not add the new folder!";
            }
            else
                message = "A folder with the same name already exists";

            Snackbar.make(vg, message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            return true;
        }

        if (id == R.id.context_menu_select_all) {
            selectedFiles.clear();
            // Mark all files as selected
            for(FileObject file: fileObjectList) {
                file.setSelected(true);
                selectedFiles.add(file);
            }
            fileAdapter.setActionMode(true); // Show checkboxes; should be all ticked because of file.setSelected(true);
            fileAdapter.notifyDataSetChanged();
            Toast.makeText(getActivity().getApplicationContext(),selectedFiles.size()
                    +" files selected", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if(id == R.id.context_menu_delete) {
            System.out.println("Selected file list size: " + selectedFiles.size());
            System.out.println("Adapter action mode: "+ fileAdapter.actionModeEnabled());
            cabMenuRequired = false;

            if(selectedFiles.size()>0) {
                yesNoDialog = new YesNoDialog();
                yesNoDialog.setTargetFragment(this, 0);
                Bundle args = new Bundle();
                args.putString("title_key", "Confirm detete");
                args.putString("body_key", "Are you sure you want to delete "+ selectedFiles.size() + " item(s)? This cannot be undone.");
                yesNoDialog.setArguments(args);
                yesNoDialog.show(getActivity().getSupportFragmentManager(), "delete_dialog_tag");

                // Actual delete task is in the DialogFragment callback methods
            }

            return true;
        }
        else if(id == R.id.context_menu_rename) {
            cabMenuRequired = false;
            if(fileAdapter.actionModeEnabled() && selectedFiles.size() == 1) {

                userInputDialog = new UserInputDialog();
                userInputDialog.setTargetFragment(this, 0);
                Bundle args = new Bundle();
                args.putString("title_key", "Rename");
                args.putString("body_key", selectedFiles.get(0).getFilename());
                userInputDialog.setArguments(args);
                userInputDialog.show(getActivity().getSupportFragmentManager(), "userinput_dialog_tag");

            }
        }
        else if(id == R.id.context_menu_cut) {
            cabMenuRequired = true;

            Snackbar.make(vg, selectedFiles.size() +" files clipped", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            tobeDeletedFiles = selectedFiles;
            cabMenuEnabled = true;
            fileAdapter.setActionMode(false);
            fileAdapter.notifyDataSetChanged();
            return true;
        }

        else if(id == R.id.context_menu_copy) {
            cabMenuRequired = true;
            Snackbar.make(vg, selectedFiles.size() +" files copied", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            cabMenuEnabled = true;
            fileAdapter.setActionMode(false);
            fileAdapter.notifyDataSetChanged();
            return true;
        }
        else if(id == R.id.context_menu_paste) {
            cabMenuRequired = false;
            cabMenuEnabled = false;

            System.out.println("Files will be copied");
            if(selectedFiles.size()>0) {
                ImprovedCopyTask improvedCopyTask = new ImprovedCopyTask(currentWorkingDirectory);
                improvedCopyTask.execute(selectedFiles);
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(),"Empty clipboard", Toast.LENGTH_SHORT).show();
            }

            return true;
        }
        else if(id == R.id.action_search) {
            final MenuItem myActionMenuItem = toolbar.getMenu().findItem(R.id.action_search);
            final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    SearchTask searchTask = new SearchTask(myActionMenuItem, searchView);
                    searchTask.execute(query);
                    return false; //hide keyboard
                }
                @Override
                public boolean onQueryTextChange(String s) {
                    // UserFeedback.show( "SearchOnQueryTextChanged: " + s);
                    return false;
                }
            });
        }
        else if(id == R.id.action_close_app) {
            getActivity().finish();
        }


        return super.onOptionsItemSelected(item);
    }

    public void recursiveDelete(File file) {
        if(file.exists()) {
            if(file.isDirectory()) {
                for (File f : file.listFiles()) {
                    recursiveDelete(f);
                }
            }
            file.delete();
        }
    }

    public boolean renameFileFolder(File file, String newname) {
        return file.renameTo(new File(file.getParent()+"/"+newname));
    }

    public int listFiles(String root) {
        File f = new File(root);
        //This is the onboard memory card of 32GB
        //Not the one plugged in from outside (2GB one)
        currentWorkingDirectory = root; // Used while pasting files

        File[] files = f.listFiles();
        try {
            for (File inFile : files) {
                String TAG = "Vinit";
                if (inFile.isDirectory()) {
                    //Log.d(TAG, inFile.getPath());
                    fileObjectList.add(new FileObject(inFile.getName(), inFile.getPath(),
                            R.drawable.ic_folder_black_24dp,
                            true, inFile.list().length, false, new Date(inFile.lastModified())));
                } else {
                    //Log.d(TAG, inFile.getPath());
                    Uri selectedUri = Uri.fromFile(inFile);
                    String fileExtension
                            = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
                    String mimeType
                            = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
                    System.out.println("Mimetype: "+mimeType);

                    int icon;

                    try {
                        if(mimeType.contains("pdf"))
                            icon = R.drawable.ic_pdf_black_24dp;
                        else if(mimeType.contains("audio"))
                            icon = R.drawable.ic_library_music_black_24dp;
                        else if(mimeType.contains("video"))
                            icon = R.drawable.ic_movie_black_24dp;
                        else
                            icon = R.drawable.ic_insert_drive_file_black_24dp;
                    } catch (Exception e) {
                        icon = R.drawable.ic_insert_drive_file_black_24dp;
                    }

                    fileObjectList.add(new FileObject(inFile.getName(), inFile.getPath(), icon,
                            false, 0, false, new Date(inFile.lastModified())));
                }
            }
            Collections.sort(fileObjectList); //sort by filename
            try {
                root = root.replace(Environment.getExternalStorageDirectory().toString(), "SD Card");
                root = root.replace("/", " | ");
                root = root.substring(0, root.lastIndexOf(" | "));
                pathAtTop.setText(root);
            } catch(Exception e) {
                pathAtTop.setText(root);
            }
            return files.length;
        } catch (Exception e) {
            return 0;
        }

    }

    @Override
    public void onInputYesClick() {

        System.out.println("Message received in parent : " + userInputDialog.getNewname());
        renameFileFolder(new File(selectedFiles.get(0).getFilepath()), userInputDialog.getNewname());
        userInputDialog.dismiss();
        //Reload the target folder
        fileObjectList.clear();
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.fragment_explore_menu);
        fileAdapter.setActionMode(false);
        fileAdapter.notifyDataSetChanged(); // Clear the adapter first
        listFiles(currentWorkingDirectory); // Build up the child directory
        parentDir = (new File(currentWorkingDirectory)).getParent();
        fileAdapter.notifyDataSetChanged(); // Populate recyclerview with child directory

    }

    @Override
    public void onInputNoClick() {

        userInputDialog.dismiss();

    }

    private class ImprovedCopyTask extends AsyncTask<List<FileObject>, Double, Long> implements FileMovementStatusFeedback {

        String destinationPath;
        private NotificationCompat.Builder notifbuilder;
        final int notify_id = 1;
        NotificationManager notificationManager;
        ProgressDialog progressDialog;

        private ImprovedCopyTask(String destinationPath) {
            this.destinationPath = destinationPath;
        }

        @Override
        protected void onPreExecute() {
            //showAchartEngineDialog();
            //Ready resources for showing progress in notification
            notifbuilder = new NotificationCompat.Builder(getActivity().getApplicationContext());
            notificationManager = (NotificationManager) getActivity()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notifbuilder.setSmallIcon(R.drawable.notificonicon)
                        .setContentTitle("Copying")
                        .setContentText("Copying in backgroung..")
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX);


            //Ready resources for showing foreground progress
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMax(100);
            progressDialog.setMessage("Copying");
            progressDialog.setTitle("Copying");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        }

        @Override
        protected Long doInBackground(List<FileObject>... lists) {

            long totalSize = 0;
            List<FileObject> filesToCopy = lists[0];

            for(FileObject fo : filesToCopy) {
                System.out.println("Copying : "+fo.getFilepath() + ", File name:" + fo.getFilename());
                File sourceFile = new File(fo.getFilepath());

                String fullpath = destinationPath+fo.getFilename();

                while((new File(fullpath).exists())) {
                    fullpath = fullpath+"(copy)";
                }

                File destinationFile = new File(fullpath);
                FileMovementStatusUpdater mFileMovementStatusUpdater = new FileMovementStatusUpdater();
                long s = System.currentTimeMillis();

                try {
                    mFileMovementStatusUpdater.monitor(destinationFile, FileUtils.sizeOfDirectory(sourceFile), this);
                    FileUtils.copyDirectory(sourceFile, destinationFile);
                } catch (Exception e) {
                    mFileMovementStatusUpdater.monitor(destinationFile, sourceFile.length(), this);
                    try {
                        FileUtils.copyFile(sourceFile, destinationFile);
                    } catch (IOException e1) {
                        System.out.println("Directory/file copy failed:");
                        e1.printStackTrace();
                    }
                }

                mFileMovementStatusUpdater.stopMonitoring();

                System.out.println("Elapsed time [fileCopyUsingApacheCommons]: "+ (System.currentTimeMillis()-s) + "ms");

                try {
                    totalSize = totalSize + FileUtils.sizeOfDirectory(sourceFile);
                } catch (Exception e) {
                    totalSize = totalSize + sourceFile.length();
                }
            }
            return totalSize;
        }

        @Override
        public void notifyStatus(double percentMoved, double speedInMB) {
            publishProgress(percentMoved, speedInMB);
            System.out.println("Progress: " + percentMoved);
            System.out.println("Speed: " + speedInMB +" MB/s");
        }

        @Override
        protected void onProgressUpdate(Double... values) {
            if(progressDialog.isShowing()) {
                progressDialog.setProgress(values[0].intValue());
            }
            else {
                notifbuilder.setProgress(100, values[0].intValue(), false);
                notificationManager.notify(notify_id,notifbuilder.build());
            }
        }

        @Override
        protected void onPostExecute(Long copiedFileSize) {

            if(tobeDeletedFiles.size() > 0) {
                for(FileObject fo:tobeDeletedFiles) {
                    System.out.println("File to delete: "+fo.getFilepath());
                    recursiveDelete(new File(fo.getFilepath()));
                }
                tobeDeletedFiles.clear();
            }

            fileObjectList.clear();
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.fragment_explore_menu);
            fileAdapter.setActionMode(false);
            fileAdapter.notifyDataSetChanged(); // Clear the adapter first
            listFiles(currentWorkingDirectory); // Build up the child directory
            parentDir = (new File(currentWorkingDirectory)).getParent();
            fileAdapter.notifyDataSetChanged(); // Populate recyclerview with child directory
            System.out.println(copiedFileSize + " bytes copied.");
            /*if(dialog.isShowing())
                dialog.dismiss(); */
            if(progressDialog.isShowing())
                progressDialog.dismiss();

            // First check if notification is visible or not
            notifbuilder.setProgress(0,0,false);
            notifbuilder.setContentTitle("Done");
            notifbuilder.setContentText("Done");
            notificationManager.notify(notify_id,notifbuilder.build());

        }

    }

    private class SearchTask extends AsyncTask<String, Integer, Integer> {

        MenuItem myActionMenuItem;
        SearchView searchView;
        ProgressDialog pd;

        private SearchTask(MenuItem myActionMenuItem, SearchView searchView) {
            this.myActionMenuItem = myActionMenuItem;
            this.searchView = searchView;
        }

        @Override
        protected void onPreExecute() {
            fileObjectList.clear();
            fileAdapter.setActionMode(false);
            fileAdapter.notifyDataSetChanged();
            pd = new ProgressDialog(getActivity());
            pd.setTitle("Please wait");
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            int found=0;
            Collection<File> files = FileUtils.listFiles(new File(currentWorkingDirectory), null, true);
            for(File file:files) {
                if(file.getName().toLowerCase().contains(strings[0].toLowerCase())) {
                    System.out.println(file.getPath() + " : " + file.getName());
                    if (file.isDirectory()) {
                        //Log.d(TAG, inFile.getPath());
                        fileObjectList.add(new FileObject(file.getName(), file.getPath(),
                                R.drawable.ic_folder_black_24dp,
                                true, file.list().length, false, new Date(file.lastModified())));
                    } else {
                        //Log.d(TAG, inFile.getPath());
                        Uri selectedUri = Uri.fromFile(file);
                        String fileExtension
                                = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
                        String mimeType
                                = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
                        System.out.println("Mimetype: "+mimeType);

                        int icon;

                        try {
                            if(mimeType.contains("pdf"))
                                icon = R.drawable.ic_pdf_black_24dp;
                            else if(mimeType.contains("audio"))
                                icon = R.drawable.ic_library_music_black_24dp;
                            else if(mimeType.contains("video"))
                                icon = R.drawable.ic_movie_black_24dp;
                            else
                                icon = R.drawable.ic_insert_drive_file_black_24dp;
                        } catch (Exception e) {
                            icon = R.drawable.ic_insert_drive_file_black_24dp;
                        }

                        fileObjectList.add(new FileObject(file.getName(), file.getPath(), icon,
                                false, 0, false, new Date(file.lastModified())));
                    }
                    found = found+1;
                    publishProgress(found);
                }
            }
            return found;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            pd.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            pd.dismiss();
            Snackbar.make(vg, integer + " results found", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            fileAdapter.notifyDataSetChanged();
            if( ! searchView.isIconified()) {
                searchView.setIconified(true);
            }
            myActionMenuItem.collapseActionView();
            RecyclerView.LayoutManager lm = new GridLayoutManager(getActivity().getApplicationContext(),2);
            explorerRv.setLayoutManager(lm);
            explorerRv.setAdapter(fileAdapter);
            fileAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onYesClick() {

        yesNoDialog.dismiss();
        System.out.println("Detete confirm dialog -> Yes clicked");
        if(fileAdapter.actionModeEnabled() && selectedFiles.size() > 0) {
            for(FileObject fo:selectedFiles) {
                System.out.println("File to delete: "+fo.getFilepath());
                recursiveDelete(new File(fo.getFilepath()));
            }
        }
        fileObjectList.clear();
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.fragment_explore_menu);
        fileAdapter.setActionMode(false);
        fileAdapter.notifyDataSetChanged(); // Clear the adapter first
        listFiles(currentWorkingDirectory); // Build up the child directory
        parentDir = (new File(currentWorkingDirectory)).getParent();
        fileAdapter.notifyDataSetChanged(); // Populate recyclerview with child directory
    }

    @Override
    public void onNoClick() {

        yesNoDialog.dismiss();

    }

}
