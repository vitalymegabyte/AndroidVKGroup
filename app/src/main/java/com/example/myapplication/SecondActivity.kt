package com.example.myapplication

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.android.synthetic.main.activity_second.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

var displayWidth : Int = 0
var groupData : GroupKeeper? = null
class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        //val note = CardView(this)
        //note.setContentPadding(10,10,10,10)
        val activity = this
        displayWidth = applicationContext.resources.displayMetrics.widthPixels

        GlobalScope.launch {
            getGroup(activity)
            getWall(activity)
        }
    }
    suspend fun getGroup(activity: SecondActivity) {
        val url = "https://api.vk.com/method/groups.getById?group_id=146026097&fields=name,photo_200&access_token=e5f72dffe5f72dffe5f72dff68e5858bbeee5f7e5f72dffbb1edb76f7916e08c926eb33&v=5.110"
        val client = HttpClient()
        var output : String = ""
        client.get<HttpStatement>(url).execute { response: HttpResponse ->
            // Response is not downloaded here.
            val channel = response.receive<ByteReadChannel>()
            channel.toInputStream().bufferedReader().use {
                output = it.readText()
            }
        }
        val answer: GroupResponse = Gson().fromJson<GroupResponse>(output, GroupResponse::class.java)
        Log.i("Avatar", answer.toString())
        val bmp : Bitmap? = withContext(Dispatchers.IO) { Picasso.get().load(answer.response.first().photo_200).get() }
        groupData = GroupKeeper(answer.response.first().name, bmp)
    }
    suspend fun getWall(activity: SecondActivity) {
        val url = "https://api.vk.com/method/wall.get?owner_id=-146026097&count=100&filter=owner&access_token=e5f72dffe5f72dffe5f72dff68e5858bbeee5f7e5f72dffbb1edb76f7916e08c926eb33&v=5.110"
        val client = HttpClient()

        var output : String = ""
        //Log.i("Sample test", "Query started")
        client.get<HttpStatement>(url).execute { response: HttpResponse ->
            // Response is not downloaded here.
            val channel = response.receive<ByteReadChannel>()
            channel.toInputStream().bufferedReader().use {
                output = it.readText()
            }
        }
        val answer: Answer = Gson().fromJson<Answer>(output, Answer::class.java)
        //Log.i("Attachments", answer.toString())
        //Log.i("Sample test", output.length.toString())
        //val json_obj = JSONObject(output)
        if(answer.response == null) {
            Log.e("Connection", "Can`t connect, more info in next log")
            Log.e("Connection", output)
        }
        else {
            val adapter = ListAdapter(answer.response.items)
            activity.runOnUiThread() {
                list_recycler_view.apply() {
                    layoutManager = LinearLayoutManager(activity)
                    this.adapter = adapter
                }
                progressBar.visibility = View.INVISIBLE
            }
        }

    }
}