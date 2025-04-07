package com.rexvit.browser.view.customView;


import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.rexvit.browser.fragment.TabsFragment;


public class ItemSwipeHelper extends ItemTouchHelper.Callback {
    private int direction;
    private TabsFragment.ViewAdapter itemTouchHelperAdapter;

    @Override 
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
        return false;
    }

    public ItemSwipeHelper(int i, TabsFragment.ViewAdapter viewAdapter) {
        this.direction = i;
        this.itemTouchHelperAdapter = viewAdapter;
    }

    @Override 
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int i = this.direction;
        return makeMovementFlags(0, i != 1 ? i != 2 ? 0 : 4 : 8);
    }

    @Override 
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
        this.itemTouchHelperAdapter.swipeToDelete(viewHolder.getAdapterPosition());
    }
}
