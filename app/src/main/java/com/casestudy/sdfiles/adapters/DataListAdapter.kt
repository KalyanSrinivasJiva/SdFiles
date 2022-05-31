package com.casestudy.sdfiles.adapters

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.annotation.NonNull
import android.support.v4.content.FileProvider
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.casestudy.sdfiles.R
import java.io.File
import java.util.*


internal class DataListAdapter(private var itemsList: List<String>) :
    RecyclerView.Adapter<DataListAdapter.MyViewHolder>() {
    private var context: Context? = null
    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemTextView: TextView = view.findViewById(R.id.itemTextView)
        var dataLayout: LinearLayout = view.findViewById(R.id.dataLayout)
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.data_item, parent, false)
        context = parent.context
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemsList[position]
        val tokens = StringTokenizer(item, "/")
        val fileName = tokens.toList().last().toString() // this will contain "Fruit"
        holder.itemTextView.text = fileName

        holder.dataLayout.setOnClickListener {

            val myMime = MimeTypeMap.getSingleton()
            val newIntent = Intent(Intent.ACTION_VIEW)
            val mimeType = myMime.getMimeTypeFromExtension(fileExt(item))
            val file = File(item)
            newIntent.setDataAndType(context?.let { it1 -> FileProvider.getUriForFile(it1, context!!.applicationContext.packageName + ".provider", file) }, mimeType)
            newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            newIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            try {
                context?.startActivity(newIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No handler for this type of file.", Toast.LENGTH_LONG)
                    .show()
            }

        }
    }

    private fun fileExt(url: String): String? {
        var url = url
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"))
        }
        return if (url.lastIndexOf(".") == -1) {
            null
        } else {
            var ext = url.substring(url.lastIndexOf(".") + 1)
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"))
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"))
            }
            ext.toLowerCase()
        }
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }
}