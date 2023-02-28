package org.kimp.tfs.hw1.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.kimp.tfs.hw1.databinding.ActivityFirstLayoutBinding

@AndroidEntryPoint
class FirstActivity: AppCompatActivity() {
    private lateinit var binding: ActivityFirstLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFirstLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
