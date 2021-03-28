package com.example.galleryapp229852.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import com.example.galleryapp229852.R
import com.example.galleryapp229852.adapter.GalleryImageAdapter
import com.example.galleryapp229852.adapter.GalleryImageClickListener
import com.example.galleryapp229852.adapter.Image
import com.example.galleryapp229852.fragment.GalleryFullscreenFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.lang.reflect.Type
import java.nio.file.Files.createFile
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), GalleryImageClickListener {

    // gallery column count
    private val SPAN_COUNT = 1
    private var imageList = ArrayList<Image>()
    var displayList = ArrayList<Image>()
    lateinit var galleryAdapter: GalleryImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // init adapter
        galleryAdapter = GalleryImageAdapter(displayList)
        galleryAdapter.listener = this
        var itemTouchHelper = ItemTouchHelper(SwipeToDelete(galleryAdapter))
        itemTouchHelper.attachToRecyclerView(findViewById(R.id.recyclerView))
        // init recyclerview
        recyclerView.layoutManager = GridLayoutManager(this, SPAN_COUNT)
        recyclerView.adapter = galleryAdapter

        // load images
        loadData()
        loadImages()
    }

    private fun loadImages() {
        if (imageList.isEmpty()) {
            imageList.add(Image("https://i.ibb.co/wBYDxLq/beach.jpg", "Beach Houses"))
            imageList.add(Image("https://i.ibb.co/gM5NNJX/butterfly.jpg", "Butterfly"))
            imageList.add(Image("https://i.ibb.co/10fFGkZ/car-race.jpg", "Car Racing"))
            imageList.add(Image("https://i.ibb.co/ygqHsHV/coffee-milk.jpg", "Coffee with Milk"))
            imageList.add(Image("https://i.ibb.co/7XqwsLw/fox.jpg", "Fox"))
            imageList.add(Image("https://i.ibb.co/L1m1NxP/girl.jpg", "Mountain Girl"))
            imageList.add(Image("https://i.ibb.co/wc9rSgw/desserts.jpg", "Desserts Table"))
            imageList.add(Image("https://i.ibb.co/wdrdpKC/kitten.jpg", "Kitten"))
            imageList.add(Image("https://i.ibb.co/dBCHzXQ/paris.jpg", "Paris Eiffel"))
            imageList.add(Image("https://i.ibb.co/JKB0KPk/pizza.jpg", "Pizza Time"))
            imageList.add(Image("https://i.ibb.co/VYYPZGk/salmon.jpg", "Salmon "))
            imageList.add(Image("https://i.ibb.co/JvWpzYC/sunset.jpg", "Sunset in Beach"))

        }
        displayList.addAll(imageList)
        galleryAdapter.notifyDataSetChanged()
    }

    override fun onClick(position: Int) {
        // handle click of image

        val bundle = Bundle()
        bundle.putSerializable("images", displayList)
        bundle.putInt("position", position)

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val galleryFragment = GalleryFullscreenFragment()
        galleryFragment.arguments = bundle
        galleryFragment.show(fragmentTransaction, "gallery")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.camera_menu, menu)
        val menuItem = menu!!.findItem(R.id.searchOption)
        if (menuItem != null) {
            val searchView = menuItem.actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText!!.isNotEmpty()) {
                        displayList.clear()
                        val search = newText.toLowerCase(Locale.getDefault())
                        imageList.forEach {
                            if (it.title.toLowerCase(Locale.getDefault()).contains(search)) {
                                displayList.add(it)
                            }
                        }
                        recyclerView.adapter!!.notifyDataSetChanged()

                    } else {
                        displayList.clear()
                        displayList.addAll(imageList)
                        recyclerView.adapter!!.notifyDataSetChanged()
                    }
                    return true
                }


            })
        }
        return super.onCreateOptionsMenu(menu)
    }

    private val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show()
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.camera_option) {
            dispatchTakePictureIntent()
            return true
        }
        if (item.itemId == R.id.saveButton) {
            saveData()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveData() {
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        //PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(imageList)
        editor.putString("task list", json)
        editor.apply()
        editor.commit()
        val toast = Toast.makeText(applicationContext, "Saved!", Toast.LENGTH_LONG)
        toast.show()
    }

    private inline fun <reified T> genericType(): Type = object : TypeToken<T>() {}.type


    private fun loadData() {
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        //PreferenceManager.getDefaultSharedPreferences(this)
        val gson = Gson()
        val json = sharedPreferences.getString("task list", null)
        val type = genericType<ArrayList<Image>>()
        imageList.clear()
        imageList = gson.fromJson(json, type)
    }

    private lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            displayList.add(Image(mediaScanIntent.data.toString(), currentPhotoPath))

            sendBroadcast(mediaScanIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
            galleryAddPic()
            //imageList.add(data.extras?.get("data") as Image)
            galleryAdapter.notifyDataSetChanged()
        }
    }

}