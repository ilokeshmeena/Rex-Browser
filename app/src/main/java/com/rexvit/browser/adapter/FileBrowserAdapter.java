package com.rexvit.browser.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.rexvit.browser.R;
import com.rexvit.browser.models.Directory;

import java.util.List;


public class FileBrowserAdapter extends RecyclerView.Adapter<FileBrowserAdapter.PdfViewHolder> {
    private final String TAG = "FileBrowserAdapter";
    private OnDirectoryClickListener directoryClickListener;
    private Context mContext;
    private List<Directory> mDirectories;

    
    public interface OnDirectoryClickListener {
        void onDirectoryClicked(Directory directory);
    }

    
    public class PdfViewHolder extends RecyclerView.ViewHolder {
        private TextView lastModified;
        private TextView pdfHeader;
        private RelativeLayout pdfWrapper;

        private PdfViewHolder(View view) {
            super(view);
            this.pdfHeader = (TextView) view.findViewById(R.id.pdf_header);
            this.lastModified = (TextView) view.findViewById(R.id.pdf_last_modified);
            this.pdfWrapper = (RelativeLayout) view.findViewById(R.id.pdf_wrapper);
        }
    }

    public FileBrowserAdapter(Context context, List<Directory> list) {
        this.mDirectories = list;
        this.mContext = context;
        if (context instanceof OnDirectoryClickListener) {
            this.directoryClickListener = (OnDirectoryClickListener) context;
            return;
        }
        throw new RuntimeException(this.mContext.toString() + " must implement OnDirectoryClickListener");
    }

    @Override 
    public PdfViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new PdfViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_browse_pdf, viewGroup, false));
    }

    @Override 
    public void onBindViewHolder(final PdfViewHolder pdfViewHolder, int i) {
        Directory directory = this.mDirectories.get(i);
        pdfViewHolder.pdfHeader.setText(directory.getName());
        pdfViewHolder.lastModified.setText(directory.getItems() + " " + this.mContext.getString(R.string.items));
        pdfViewHolder.pdfWrapper.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                directoryClicked(pdfViewHolder.getAdapterPosition());
            }
        });
    }

  

    @Override 
    public int getItemCount() {
        return this.mDirectories.size();
    }

    private void directoryClicked(int i) {
        OnDirectoryClickListener onDirectoryClickListener = this.directoryClickListener;
        if (onDirectoryClickListener != null) {
            onDirectoryClickListener.onDirectoryClicked(this.mDirectories.get(i));
        }
    }
}
