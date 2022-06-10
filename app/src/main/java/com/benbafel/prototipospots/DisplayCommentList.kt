package com.benbafel.prototipospots

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.benbafel.prototipospots.databinding.ActivityDisplayCommentListBinding
import com.benbafel.prototipospots.databinding.ActivityDisplaySpotInfoBinding
import com.benbafel.prototipospots.models.Comment
import com.benbafel.prototipospots.models.CommentAdapter
import com.benbafel.prototipospots.models.CommentList
import kotlinx.android.synthetic.main.activity_display_comment_list.*


private const val TAG = "DisplayCommentList"
class DisplayCommentList : AppCompatActivity() {
    private lateinit var binding: ActivityDisplayCommentListBinding
    private lateinit var comAdapter: CommentAdapter
    private lateinit var comList: CommentList
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisplayCommentListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //recieve list
        comList = intent.getSerializableExtra(EXTRA_COMM_LIST) as CommentList
        //set layout manager on recycler view
        rvListaComentarios.layoutManager= LinearLayoutManager(this)
        //set adapter on the RV
        comAdapter = CommentAdapter(this,comList.commentList,object : CommentAdapter.OnClickListener{
            override fun onItemClick(position: Int) {
                Log.i(TAG,"onItemClick$position")
            }
        })
        rvListaComentarios.adapter = comAdapter
    }
}