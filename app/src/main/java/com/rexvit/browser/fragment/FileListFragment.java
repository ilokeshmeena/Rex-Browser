package com.rexvit.browser.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rexvit.browser.R;
import com.rexvit.browser.adapter.FileBrowserAdapter;
import com.rexvit.browser.database.DataUpdatedEvent;
import com.rexvit.browser.models.Directory;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class FileListFragment extends Fragment implements FileBrowserAdapter.OnDirectoryClickListener {
    private static final String FILE_PATH = "file_path";
    FileBrowserAdapter adapter;
    LinearLayout emptyDirectory;
    FloatingActionButton febSelectDirectory;
    Context mContext;
    private String mFilePath;
    ProgressBar progressBarlistDirectory;
    RecyclerView recyclerView;
    List<Directory> dirList = new ArrayList();
    FileFilter fileFilter = new FileFilter() { 
        @Override 
        public final boolean accept(File file) {
            return FileListFragment.acceptFile(file);
        }
    };
    View.OnClickListener onClickListener = new View.OnClickListener() { 
        @Override 
        public void onClick(View view) {
            EventBus.getDefault().post(new DataUpdatedEvent.DirectorySelected(FileListFragment.this.mFilePath));
            ((Activity) FileListFragment.this.mContext).finish();
        }
    };

    @Override 
    public void onDirectoryClicked(Directory directory) {
    }

    public static FileListFragment newInstance(String str) {
        FileListFragment fileListFragment = new FileListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("file_path", str);
        fileListFragment.setArguments(bundle);
        return fileListFragment;
    }

    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getArguments() != null) {
            this.mFilePath = getArguments().getString("file_path") + "/";
        }
        this.mContext = getContext();
    }

    @Override 
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_browse_pdf);
        this.progressBarlistDirectory = (ProgressBar) view.findViewById(R.id.progress_bar_list_dir);
        this.emptyDirectory = (LinearLayout) view.findViewById(R.id.empty_state_directory);
        FloatingActionButton floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab_select_directory);
        this.febSelectDirectory = floatingActionButton;
        floatingActionButton.setOnClickListener(this.onClickListener);
        new ListDirectory().execute(new Void[0]);
    }

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_file_list, viewGroup, false);
    }

    @Override 
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override 
    public void onDetach() {
        super.onDetach();
    }

    public List<Directory> getFiles(String str) {
        File[] listFiles;
        File file = new File(str);
        ArrayList arrayList = new ArrayList();
        if (file.isDirectory() && (listFiles = file.listFiles(this.fileFilter)) != null) {
            for (File file2 : listFiles) {
                int size = file2.isDirectory() ? getFiles(file2.getAbsolutePath()).size() : 0;
                Directory directory = new Directory();
                directory.setName(file2.getName());
                directory.setPath(file2.getAbsolutePath());
                directory.setItems(size);
                arrayList.add(directory);
            }
        }
        Collections.sort(arrayList, new Comparator() { 
            @Override 
            public final int compare(Object obj, Object obj2) {
                int compareToIgnoreCase;
                compareToIgnoreCase = ((Directory) obj).getName().compareToIgnoreCase(((Directory) obj2).getName());
                return compareToIgnoreCase;
            }
        });
        return arrayList;
    }

    
    public static  boolean acceptFile(File file) {
        return file.isDirectory() && !file.isHidden();
    }

    
    public class ListDirectory extends AsyncTask<Void, Void, Void> {
        public ListDirectory() {
        }

        @Override 
        protected void onPreExecute() {
            super.onPreExecute();
            FileListFragment.this.progressBarlistDirectory.setVisibility(View.VISIBLE);
            FileListFragment.this.recyclerView.setBackgroundColor(FileListFragment.this.getResources().getColor(R.color.white));
            FileListFragment.this.recyclerView.setLayoutManager(new LinearLayoutManager(FileListFragment.this.mContext, RecyclerView.VERTICAL, false));
        }

        
        @Override 
        public Void doInBackground(Void... voidArr) {
            FileListFragment fileListFragment = FileListFragment.this;
            fileListFragment.dirList = fileListFragment.getFiles(fileListFragment.mFilePath);
            FileListFragment.this.adapter = new FileBrowserAdapter(FileListFragment.this.mContext, FileListFragment.this.dirList);
            return null;
        }

        
        @Override 
        public void onPostExecute(Void r3) {
            super.onPostExecute( r3);
            FileListFragment.this.progressBarlistDirectory.setVisibility(View.GONE);
            FileListFragment.this.recyclerView.setAdapter(FileListFragment.this.adapter);
            if (FileListFragment.this.dirList.size() == 0) {
                FileListFragment.this.emptyDirectory.setVisibility(View.VISIBLE);
            } else {
                FileListFragment.this.emptyDirectory.setVisibility(View.GONE);
            }
        }
    }
}
