package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.*
import kotlinx.android.synthetic.main.activity_second.*
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.note.view.*

class NoteViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.note, parent, false)) {
    private var mUsernameView: TextView = itemView.username
    private var mDatetimeView: TextView = itemView.dateTime
    private var mContentView: TextView = itemView.content
    private var mPhotoView: ImageView = itemView.photoAttachment
    private var mVideoDescription: TextView = itemView.videoDescription
    private var mAvatarView: ImageView = itemView.avatar


    init {
    }

    fun bind(post: Post) {
        if(post.copy_history == null) {
            mUsernameView.text = groupData?.name
            mAvatarView.setImageBitmap(groupData?.photo)
            mDatetimeView.text = getDateTime(post.date)
            mContentView.text = post.text
            mVideoDescription.visibility = View.INVISIBLE
            mVideoDescription.text = "" //Если не делать такой инициализации, то значения будут браться из соседних записей, чего нам не надо
            bindPhoto(getPhotoAttachment(post))
        } else {
            bind(post.copy_history.first())
            mDatetimeView.text = getDateTime(post.date)
        }
    }
    fun bindPhoto(url: String) {
        if(url != "") {
            //Log.i("Attachments", url)

            Picasso.get().load(url).into(mPhotoView)
        } else {
            mPhotoView.visibility = View.INVISIBLE
        }
    }
    val sdf = SimpleDateFormat("d MMM в HH:mm")
    private fun getDateTime(l: Long): String? {
        return try {
            val netDate = Date(l * 1000)
            Log.i("DateTime", netDate.time.toString())
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }
    private fun getPhotoAttachment(_obj: Post): String {
        if(_obj.attachments != null && _obj.attachments.count() > 0) {
            for(attachment in _obj.attachments) {
                if(attachment.type == "photo") {
                    val best = bestSize(attachment.photo!!.sizes)
                    Log.i("Attachments", best.url + " " + best.width)
                    return best.url
                }
                if(attachment.type == "video") {
                    mVideoDescription.text = "▶️ " + attachment.video!!.title
                    mVideoDescription.visibility = View.VISIBLE
                    val best = bestSize(attachment.video.image)
                    Log.i("Attachments", best.url + " " + best.width)
                    return best.url
                }
            }
        }
        return ""
    }

    private fun bestSize(sizes: Array<Size>): Size { //Оказалось, что размеры даются фиг пойми как, поэтому придётся писать отдельную функцию под это дело
        var greatest: Size = sizes.first()
        for(size in sizes) {
            if(size.width > displayWidth)
                return size
            if(size.width > greatest.width)
                greatest = size
        }
        return greatest
    }
}