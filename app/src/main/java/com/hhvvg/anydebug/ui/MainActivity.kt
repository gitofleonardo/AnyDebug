package com.hhvvg.anydebug.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hhvvg.anydebug.databinding.ActivityMainBinding
import com.hhvvg.anydebug.util.GITHUB_PAGE_URL

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupViews()
    }

    private fun setupViews() {
        binding.getCodeFab.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(GITHUB_PAGE_URL)
            startActivity(intent)
        }
    }
}