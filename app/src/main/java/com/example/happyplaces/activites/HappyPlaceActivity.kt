package com.example.happyplaces.activites

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.happyplaces.launcherContracts.CaptureImageContract
import com.example.happyplaces.R
import com.example.happyplaces.database.HappyPlacesDBManager
import com.example.happyplaces.databinding.ActivityHappyPlaceBinding
import com.example.happyplaces.databinding.ImageChooserBinding
import com.example.happyplaces.models.HappyPlacesModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class HappyPlaceActivity : AppCompatActivity() {
    private var selectedImageUri: Uri? = null

    companion object {
        private val IMAGE_DIR: String = "HappyPlacesImages"
    }
    private val happyPlacesModel = HappyPlacesModel(
        null,
        "",
       "",
        "",
        "",
        "",
        null,
        null
    )
    var isEditable:Boolean = false
    private val imageChooser: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            if (it != null) {
                val imageBitmap = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    MediaStore.Images.Media.getBitmap(this.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(this.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
                imageBitmap?.let {
                    lifecycleScope.launch {
                        selectedImageUri = saveCompressedFileFromBitmap(imageBitmap)
                        selectedImageUri?.let { it1 ->
                            setPickedImage(it1)
                        }
                    }
                }
            }
        }
    private val imageCaptureContract: ActivityResultLauncher<Intent> =
        registerForActivityResult(CaptureImageContract()) { (success, bitmap) ->
            if (success) {
                lifecycleScope.launch {
                    selectedImageUri = saveCompressedFileFromBitmap(bitmap!!)
                    selectedImageUri?.let {
                        setPickedImage(it)
                    }
                }

            }
        }

    private val launchMapsActivityResultContracts: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            it.data?.extras?.let {
                binding.locationEdt.setText(it.getString("address"))
                happyPlacesModel.latitude = it.getDouble("lat")
                happyPlacesModel.longitude = it.getDouble("long")
            }
        }
    }

    private val datFormat: SimpleDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    lateinit var binding: ActivityHappyPlaceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = "Place Details"
            binding.toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed();
            }
        }
        intent?.let {
           val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelableExtra(MainActivity.HAPPY_PLACE_DATA, HappyPlacesModel::class.java)
            } else {
                it.getParcelableExtra(MainActivity.HAPPY_PLACE_DATA) as? HappyPlacesModel
            }
            if(data!=null) {
                isEditable = true
                setUpDataWithUI(data)
                binding.saveBtn.text = "Update"
            }
        }

        binding.dateEdt.setOnClickListener {
            openDatePicker()
        }
        binding.locationImgView.setOnClickListener {
            openBottomSheet()
        }
        binding.locationEdt.setOnClickListener {
            val intent = Intent(this@HappyPlaceActivity, MapsActivity::class.java)
            intent.putExtra("lat",happyPlacesModel.latitude)
            intent.putExtra("long",happyPlacesModel.longitude)
            launchMapsActivityResultContracts.launch(intent)
        }
        binding.saveBtn.setOnClickListener {
            lifecycleScope.launch {
                if(isEditable){
                    updateHappyPlaces()
                }else {
                    saveHappyPlaces()
                }
            }
        }
    }

    private fun openDatePicker() {
        val datePicker = DatePickerDialog(this, R.style.DatePickerStyle)
        datePicker.setOnDateSetListener { view, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal[Calendar.YEAR] = year
            cal[Calendar.MONTH] = month
            cal[Calendar.DAY_OF_MONTH] = dayOfMonth
            binding.dateEdt.setText(datFormat.format(cal.time))
        }
        datePicker.show()
        datePicker.getButton(DatePickerDialog.BUTTON_NEGATIVE)
            .setTextColor(resources.getColor(R.color.deep_purple, theme))
        datePicker.getButton(DatePickerDialog.BUTTON_POSITIVE)
            .setTextColor(resources.getColor(R.color.deep_purple, theme))
    }

    private fun setPickedImage(uri: Uri) {
        binding.locationImgView.setImageURI(uri)
    }

    private fun setPickedImageBitmap(bitmap: Bitmap) {
        binding.locationImgView.setImageBitmap(bitmap)
    }

    private fun captureImage() {
        Dexter.withContext(this).withPermission(android.Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    try {
                        imageCaptureContract.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                    } catch (e: java.lang.Exception) {
                        Log.i("HappyPlaces", e.toString())
                    }
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(this@HappyPlaceActivity, "Permission Denied", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {

                }
            }).check()

    }

    private fun openBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this@HappyPlaceActivity)
        val bottomSheetBinding = ImageChooserBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)
        bottomSheetDialog.setCanceledOnTouchOutside(true)
        bottomSheetBinding.pickFromPhotosTxt.setOnClickListener {
            imageChooser.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            bottomSheetDialog.dismiss()
        }
        bottomSheetBinding.pickFromCameraTxt.setOnClickListener {
            captureImage()
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private suspend fun saveCompressedFileFromBitmap(bitmap: Bitmap): Uri? {
        val dialog = ProgressDialog.show(
            this@HappyPlaceActivity, "",
            "Saving image...", true
        )
        dialog.show()
        val uri = withContext(Dispatchers.IO) {
            if (selectedImageUri != null) {
                File(selectedImageUri!!.path).delete()
            }
            val contextWrapper = ContextWrapper(applicationContext)
            val dir = contextWrapper.getDir(IMAGE_DIR, ContextWrapper.MODE_PRIVATE)
            val file = File(dir, "Image_${System.currentTimeMillis()}.jpeg")
            val outputStream = FileOutputStream(file)
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
                outputStream.close()

            } catch (e: java.lang.Exception) {
                Toast.makeText(this@HappyPlaceActivity, "Failed to save image", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                outputStream.close()

            }
            Uri.parse(file.absolutePath)
        }
        dialog.dismiss()
        return uri
    }

    private suspend fun saveHappyPlaces() {

        happyPlacesModel.address = binding.locationEdt.text.toString()
        happyPlacesModel.title =  binding.titleEdt.text.toString()
        happyPlacesModel.desc =  binding.descriptionEdt.text.toString()
        happyPlacesModel.date =  binding.dateEdt.text.toString()
        happyPlacesModel.imageUrl =  selectedImageUri?.path.orEmpty()
        when{
            happyPlacesModel.title.isNullOrBlank() ->{
                binding.titleEdt.error = "Title can not be empty"
            }
            happyPlacesModel.desc.isNullOrBlank() ->{
                binding.descriptionEdt.error = "Description can not be empty"
            }
            happyPlacesModel.date.isNullOrBlank() ->{
                binding.dateEdt.error = "Date can not be empty"
            }
            happyPlacesModel.address.isNullOrBlank() ->{
                binding.locationEdt.error = "Date can not be empty"
            }
            happyPlacesModel.imageUrl.isNullOrBlank() ->{
                Toast.makeText(this@HappyPlaceActivity,"Please select an image",Toast.LENGTH_SHORT).show()
            }
            happyPlacesModel.address.isNullOrBlank() ->{
                binding.dateEdt.error = "Please pick an address"
            }
            else->{
                withContext(Dispatchers.IO) {
                    HappyPlacesDBManager.instance().insertHappyPlace(happyPlacesModel)
                }
                setResult(Activity.RESULT_OK)
                finish()
            }
        }

    }

    private suspend fun updateHappyPlaces() {

        happyPlacesModel.address = binding.locationEdt.text.toString()
        happyPlacesModel.title =  binding.titleEdt.text.toString()
        happyPlacesModel.desc =  binding.descriptionEdt.text.toString()
        happyPlacesModel.date =  binding.dateEdt.text.toString()
        happyPlacesModel.imageUrl =  selectedImageUri?.path.orEmpty()
        when{
            happyPlacesModel.title.isNullOrBlank() ->{
                binding.titleEdt.error = "Title can not be empty"
            }
            happyPlacesModel.desc.isNullOrBlank() ->{
                binding.descriptionEdt.error = "Description can not be empty"
            }
            happyPlacesModel.date.isNullOrBlank() ->{
                binding.dateEdt.error = "Date can not be empty"
            }
            happyPlacesModel.address.isNullOrBlank() ->{
                binding.locationEdt.error = "Date can not be empty"
            }
            happyPlacesModel.imageUrl.isNullOrBlank() ->{
                Toast.makeText(this@HappyPlaceActivity,"Please select an image",Toast.LENGTH_SHORT).show()
            }
            happyPlacesModel.address.isNullOrBlank() ->{
                binding.dateEdt.error = "Please pick an address"
            }
            else->{
                withContext(Dispatchers.IO) {
                    HappyPlacesDBManager.instance().updateHappyPlace(happyPlacesModel)
                }
                setResult(Activity.RESULT_OK)
                finish()
            }
        }

    }



    private fun setUpDataWithUI(data:HappyPlacesModel){
        happyPlacesModel.apply {
            address = data.address
            latitude = data.latitude
            longitude = data.longitude
            title = data.title
            desc = data.desc
            id = data.id
            imageUrl = data.imageUrl
            date = data.date
        }
        selectedImageUri = Uri.parse(happyPlacesModel.imageUrl)
        binding.dateEdt.setText(happyPlacesModel.date)
        binding.descriptionEdt.setText(happyPlacesModel.desc)
        binding.titleEdt.setText(happyPlacesModel.title)
        binding.locationEdt.setText(happyPlacesModel.address)
        setPickedImage(Uri.parse(happyPlacesModel.imageUrl))

    }

}