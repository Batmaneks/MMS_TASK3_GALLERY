package com.example.galleryapp229852.activity

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.example.galleryapp229852.adapter.GalleryImageAdapter

class SwipeToDelete(var adapter: GalleryImageAdapter):ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
    override fun onMove(
        p0: RecyclerView,
        p1: RecyclerView.ViewHolder,
        p2: RecyclerView.ViewHolder
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, p1: Int) {
        var pos = viewHolder.adapterPosition
        adapter.deleteItem(pos)
    }
}