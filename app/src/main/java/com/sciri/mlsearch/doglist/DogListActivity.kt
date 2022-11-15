package com.sciri.mlsearch.doglist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.sciri.mlsearch.api.ApiResponseStatus
import com.sciri.mlsearch.databinding.ActivityDogListBinding
import com.sciri.mlsearch.dogdetail.DogDetailActivity
import com.sciri.mlsearch.dogdetail.DogDetailActivity.Companion.DOG_KEY

private const val SPAN_COUNT = 3

class DogListActivity : AppCompatActivity() {
    private val dogListViewModel: DogListViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDogListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recycler = binding.dogRecycler
        val progressBar = binding.includeProgress.loadingWheel
        recycler.layoutManager = GridLayoutManager(this, SPAN_COUNT)
        val adapter = DogAdapter()
        adapter.setOnItemClickListener {
            val intent = Intent(this, DogDetailActivity::class.java)
            intent.putExtra(DOG_KEY, it)
            startActivity(intent)
        }

        recycler.adapter = adapter

        dogListViewModel.dogList.observe(this) { dogList ->
            adapter.submitList(dogList)
        }

        dogListViewModel.status.observe(this) { status ->
            when (status) {
                is ApiResponseStatus.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, getString(status.errorId), Toast.LENGTH_SHORT).show()
                }
                is ApiResponseStatus.Loading -> progressBar.visibility = View.VISIBLE
                is ApiResponseStatus.Success -> progressBar.visibility = View.GONE
            }
        }
    }
}