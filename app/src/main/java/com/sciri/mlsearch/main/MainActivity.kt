package com.sciri.mlsearch.main

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.sciri.mlsearch.*
import com.sciri.mlsearch.R
import com.sciri.mlsearch.api.ApiResponseStatus
import com.sciri.mlsearch.api.interceptors.ApiServiceInterceptor
import com.sciri.mlsearch.auth.LoginActivity
import com.sciri.mlsearch.databinding.ActivityMainBinding
import com.sciri.mlsearch.dogdetail.DogDetailActivity
import com.sciri.mlsearch.dogdetail.DogDetailActivity.Companion.DOG_KEY
import com.sciri.mlsearch.dogdetail.DogDetailActivity.Companion.IS_RECOGNITION_KEY
import com.sciri.mlsearch.doglist.DogListActivity
import com.sciri.mlsearch.domains.Dog
import com.sciri.mlsearch.domains.User
import com.sciri.mlsearch.machinelearning.Classifier
import com.sciri.mlsearch.machinelearning.DogRecognition
import org.tensorflow.lite.support.common.FileUtil
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private var isCameraReady: Boolean = false
    private lateinit var classifier: Classifier
    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                setUpCamera()
            } else {
                Toast.makeText(
                    this,
                    "U need to accept camera permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = User.getLoggedInUser(this)
        if (user == null) {
            openLoginActivity()
            return
        } else {
            ApiServiceInterceptor.setSessionToken(user.authenticationToken)
        }

        binding.settingsFab.setOnClickListener {
            openSettingsActivity()
        }

        binding.dogListFab.setOnClickListener {
            openDogListActivity()
        }


        /* binding.takePhotoFab.setOnClickListener {
             takePhoto()
         }*/

        requestCameraPermission()

        viewModel.status.observe(this) { status ->
            when (status) {
                is ApiResponseStatus.Error -> {
                    binding.includeProgress.loadingWheel.visibility = View.GONE
                    Toast.makeText(this, getString(status.errorId), Toast.LENGTH_SHORT).show()
                }
                is ApiResponseStatus.Loading -> binding.includeProgress.loadingWheel.visibility =
                    View.VISIBLE
                is ApiResponseStatus.Success -> binding.includeProgress.loadingWheel.visibility =
                    View.GONE
            }
        }

        viewModel.dog.observe(this) { dog ->
            if (dog != null) {
                openDogDetailActivity(dog)
            } else {
                Toast.makeText(this, "No se encontro el dogi", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.dogRecognition.observe(this) {
            enableTakePhotoButton(it)
        }
    }

    private fun openDogDetailActivity(dog: Dog) {
        val intent = Intent(this, DogDetailActivity::class.java)
        intent.putExtra(DOG_KEY, dog)
        intent.putExtra(IS_RECOGNITION_KEY, true)
        startActivity(intent)
    }

    private fun openDogListActivity() {
        startActivity(Intent(this, DogListActivity::class.java))
    }

    private fun openSettingsActivity() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun openLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                setUpCamera()
            }
            shouldShowRequestPermissionRationale(CAMERA) -> {
                AlertDialog.Builder(this).setTitle("Aceptme pleasseee")
                    .setMessage("Necesitas aceptar la camara")
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        requestPermissionLauncher.launch(CAMERA)
                    }.setNegativeButton(android.R.string.cancel) { _, _ ->

                    }.setCancelable(false).show()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(CAMERA)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name) + ".jpg").apply {
                mkdirs()
            }
        }
        return if (mediaDir != null && mediaDir.exists()) {
            mediaDir
        } else {
            filesDir
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider = cameraProviderFuture.get()
            // Preview
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                viewModel.recognizeImage(imageProxy)
                //enableTakePhotoButton(dogRecognition)
            }

            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                this, cameraSelector,
                preview, imageCapture, imageAnalysis
            )
        }, ContextCompat.getMainExecutor(this))
    }


    private fun enableTakePhotoButton(dogRecognition: DogRecognition) {
        if (dogRecognition.confidence > 70.0) {
            binding.takePhotoFab.alpha = 1f
            binding.takePhotoFab.setOnClickListener {
                viewModel.getDogByMlId(dogRecognition.id)
            }
        } else {
            binding.takePhotoFab.alpha = 0.2f
            binding.takePhotoFab.setOnClickListener(null)
        }
    }

    private fun setUpCamera() {
        binding.cameraPreview.post {
            imageCapture = ImageCapture.Builder()
                .setTargetRotation(binding.cameraPreview.display.rotation)
                .build()

            cameraExecutor = Executors.newSingleThreadExecutor()
            isCameraReady = true
            startCamera()
        }
    }

    private fun takePhoto() {
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(getOutputDirectory()).build()
        imageCapture.takePicture(outputFileOptions, cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error taking picture guy ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    //val photoUri = outputFileResults.savedUri
                    // val bitmap = BitmapFactory.decodeFile(photoUri?.path)

                    //val dogRecognition = classifier.recognizeImage(bitmap).first()
                    // viewModel.getDogByMlId(dogRecognition.id)
                    //openFullImageActivity(photoUri.toString())
                }
            })
    }


    override fun onStart() {
        super.onStart()
        viewModel.setUpClassifier(
            FileUtil.loadMappedFile(this@MainActivity, MODEL_PATH),
            FileUtil.loadLabels(this@MainActivity, LABELS_PATH)
        )
    }

    private fun openFullImageActivity(photoUri: String) {
        val intent = Intent(this, FullImageActivity::class.java)
        intent.putExtra(FullImageActivity.PHOTO_URI, photoUri)
        startActivity(intent)
    }
}