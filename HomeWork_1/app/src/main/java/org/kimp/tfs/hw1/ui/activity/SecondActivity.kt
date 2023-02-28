package org.kimp.tfs.hw1.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.kimp.tfs.hw1.databinding.ActivitySecondLayoutBinding

@AndroidEntryPoint
class SecondActivity: AppCompatActivity() {
    private lateinit var binding: ActivitySecondLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySecondLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}