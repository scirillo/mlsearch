package com.sciri.mlsearch.dogdetail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import coil.load
import com.sciri.mlsearch.R
import com.sciri.mlsearch.domains.Dog
import com.sciri.mlsearch.api.ApiResponseStatus
import com.sciri.mlsearch.databinding.ActivityDogDetailBinding

class DogDetailActivity : AppCompatActivity() {
    companion object {
        const val DOG_KEY = "dog"
        const val IS_RECOGNITION_KEY = "is_recognition"
    }

    val viewModel: DogDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val biding = ActivityDogDetailBinding.inflate(layoutInflater)
        setContentView(biding.root)

        val dog = intent?.extras?.getParcelable<Dog>(DOG_KEY)
        val isRecognition = intent?.extras?.getBoolean(IS_RECOGNITION_KEY)?: false
        if (dog == null) {
            Toast.makeText(this, R.string.dog_not_found, Toast.LENGTH_SHORT).show()
            this.finish()
            return
        }

        biding.dogIndex.text = getString(R.string.dog_index_format, dog.index)
        biding.lifeExpectancy.text = getString(R.string.dog_life_expectancy_format, dog.lifeExpectancy)
        biding.dog = dog
        biding.dogImage.load(dog.imageUrl) {
            crossfade(true)
        }
        biding.closeButton.setOnClickListener {
            if (isRecognition) {
                viewModel.addDogToUser(dog.id)
            } else {
                this.finish()
            }
        }
        viewModel.status.observe(this) { status ->
            when (status) {
                is ApiResponseStatus.Error -> {
                    biding.loadingWheel.visibility = View.GONE
                    Toast.makeText(this, getString(status.errorId), Toast.LENGTH_SHORT).show()
                }
                is ApiResponseStatus.Loading -> biding.loadingWheel.visibility = View.VISIBLE
                is ApiResponseStatus.Success -> {
                    biding.loadingWheel.visibility = View.GONE
                    this.finish()
                }
            }
        }
    }
}