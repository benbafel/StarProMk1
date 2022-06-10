package com.benbafel.prototipospots.models

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.benbafel.prototipospots.R

private const val TAG = "CommentAdapter"
class CommentAdapter (val context: Context, val comments: List<Comment>, val onClickListener:OnClickListener) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    interface OnClickListener{
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.comments_recycler_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount() = comments.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]
        holder.itemView.setOnClickListener{
            Log.i(TAG,"taped on position $position")
            onClickListener.onItemClick(position)
        }
        val commUser = holder.itemView.findViewById<TextView>(R.id.tvUserName)
        val commentary = holder.itemView.findViewById<TextView>(R.id.tvComment)
        commUser.text = comment.user
        commentary.text = "'${comment.userComment}'"
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}