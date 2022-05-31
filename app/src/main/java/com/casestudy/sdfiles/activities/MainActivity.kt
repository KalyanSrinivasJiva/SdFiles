package com.casestudy.sdfiles.activities



import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.casestudy.sdfiles.adapters.DataListAdapter
import com.casestudy.sdfiles.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import java.io.File


class MainActivity : AppCompatActivity() {

    private val requestCode = 100
    private var disposable: Disposable? = null

    private val dataList = ArrayList<String>()
    private lateinit var dataListAdapter: DataListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "SD files"

        dataListAdapter = DataListAdapter(dataList)
        val layoutManager = LinearLayoutManager(applicationContext)
        dataRecyclerView.layoutManager = layoutManager
        dataRecyclerView.adapter = dataListAdapter

        list()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun  list(){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED&& checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCode)
        } else {
            listExternalStorage()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                listExternalStorage()
            } else {
                Toast.makeText(this, "Until you grant the permission, I cannot list the files", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    override fun onPause() {
        super.onPause()
        this.disposable?.dispose()
    }

    private fun listExternalStorage() {
        val state = Environment.getExternalStorageState()

        if (Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state) {

            this.disposable = Observable.fromPublisher(FileLister(Environment.getExternalStorageDirectory()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    dataList.add(it)
                    println(it)
                }, {
                    Log.e("MainActivity", "Error in listing files from the SD card", it)
                }, {
                    Toast.makeText(this, "Successfully listed all the files!", Toast.LENGTH_SHORT)
                        .show()
                    this.disposable?.dispose()
                    this.disposable = null

                    dataListAdapter.notifyDataSetChanged()
                })
        }
    }

    private class FileLister(val directory: File) : Publisher<String> {

        private lateinit var subscriber: Subscriber<in String>

        override fun subscribe(s: Subscriber<in String>?) {
            if (s == null) {
                return
            }
            this.subscriber = s
            this.listFiles(this.directory)
            this.subscriber.onComplete()
        }

        /**
         * Recursively list files from a given directory.
         */
        private fun listFiles(directory: File) {

            val files = directory.listFiles()

            val pdfPattern = ".pdf"
            val docPattern = ".docx"
            val docxPattern = ".doc"
            val mp4Pattern = ".mp4"
            val jpgPattern = ".jpg"
            val jpegPattern = ".jpeg"
            val pngPattern = ".png"

            if (files != null) {
                for (file in files) {

                    if (file != null) {
                        if (file.isDirectory) {
                            listFiles(file)
                        } else {
                            if (file.name.endsWith(pdfPattern)||file.name.endsWith(docPattern)||file.name.endsWith(docxPattern)||file.name.endsWith(mp4Pattern)||file.name.endsWith(jpegPattern)||file.name.endsWith(jpgPattern)||file.name.endsWith(pngPattern)){
                                subscriber.onNext(file.absolutePath)
                                }
                            }
                        }
                    }
                }
            }
        }




}


