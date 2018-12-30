package de.yochyo.yBooru

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import de.yochyo.danbooruAPI.Api
import de.yochyo.yBooru.api.Post
import de.yochyo.yBooru.utils.runAsync

class PreviewManager(val context: Context,val view: RecyclerView) {
    var page = 1

    private val dataSet = ArrayList<Post?>(200)
    private val adapter = Adapter()
    private val layoutManager = GridLayoutManager(context, 3)

    init {
        view.layoutManager = layoutManager
        view.adapter = adapter
        view.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(layoutManager.findLastVisibleItemPosition()+1 >= dataSet.size){
                    loadPictures(++page)
                }
                println("${layoutManager.findLastVisibleItemPosition()} from ${dataSet.size}")
            }
        })
    }

    fun loadPictures(page: Int, vararg tags: String){
        val posts = Api.getPosts(page, tags)
        var i = dataSet.size
        for(post in posts){
            dataSet.add(null)
            val index = i++
            runAsync(context, {Api.downloadImage(post.filePreviewURL, "${post.id}Preview")}){
                dataSet[index] = post
                adapter.notifyItemChanged(index)
            }
        }
        adapter.notifyDataSetChanged()
    }



    private inner class Adapter : RecyclerView.Adapter<MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder = MyViewHolder((LayoutInflater.from(parent.context).inflate(R.layout.recycle_view_grid, parent, false) as ImageView))
        override fun getItemCount(): Int = dataSet.size
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val post = dataSet[position]
            if (post != null) {
                val bitmap = cache.getCachedBitmap("${post.id}Preview")
                if (bitmap != null) {
                    holder.imageView.setImageBitmap(bitmap)
                    return
                }
            }
            holder.imageView.setImageDrawable(null)
        }
    }
}


private class MyViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)