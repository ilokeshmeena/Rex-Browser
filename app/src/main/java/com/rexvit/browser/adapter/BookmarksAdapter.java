package com.rexvit.browser.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.rexvit.browser.R;
import com.rexvit.browser.database.BookmarksDb;
import com.rexvit.browser.utils.Utils;

import java.util.List;

public class BookmarksAdapter extends BaseAdapter {
    private List<BookmarksDb> mList;
    private LayoutInflater mInflater;

    public BookmarksAdapter(Context context, List<BookmarksDb> list) {
        mList = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_bookmarks, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textViewLink = convertView.findViewById(R.id.uriBookmarks);
            viewHolder.textViewTitle = convertView.findViewById(R.id.titleBookmarks);
            viewHolder.textViewTime = convertView.findViewById(R.id.timeBookmarks);
            viewHolder.imageViewIcon = convertView.findViewById(R.id.icBookmarks2);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        BookmarksDb bookmark = mList.get(position);
        if (bookmark.getBookmarks() != null && bookmark.getTitle() != null) {
            viewHolder.textViewLink.setText(Utils.getTitleForSearchBar(bookmark.getBookmarks()));
            viewHolder.textViewTitle.setText(bookmark.getTitle());
            viewHolder.textViewTime.setText(bookmark.getTime());

            TextDrawable.Builder drawableBuilder = (TextDrawable.Builder) TextDrawable.builder();
            TextDrawable textDrawable;
            if (bookmark.getIconColor() == 0) {
                textDrawable = drawableBuilder.buildRound(bookmark.getIcon(), Utils.randomColors());
            } else {
                textDrawable = drawableBuilder.buildRound(bookmark.getIcon(), bookmark.getIconColor());
            }
            viewHolder.imageViewIcon.setImageDrawable(textDrawable);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView textViewLink;
        TextView textViewTitle;
        TextView textViewTime;
        ImageView imageViewIcon;
    }
}