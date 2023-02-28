package org.kimp.tfs.hw1.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.kimp.tfs.hw1.HomeworkApplication
import org.kimp.tfs.hw1.data.adapter.ContactsAdapter
import org.kimp.tfs.hw1.data.model.ContactInfo
import org.kimp.tfs.hw1.databinding.ActivityFirstLayoutBinding
import timber.log.Timber

@AndroidEntryPoint
class FirstActivity: AppCompatActivity() {
    private lateinit var binding: ActivityFirstLayoutBinding

    private val contactsAdapter = ContactsAdapter(listOf())

    @SuppressLint("NotifyDataSetChanged")
    private val secondActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            Timber.tag(HomeworkApplication.TAG)
                .i("Received OK result from the SecondActivity")

            if (it.data?.hasExtra("contacts") == true) {
                contactsAdapter.contacts = it.data?.getParcelableArrayExtra("contacts")
                    ?.map { x -> x as ContactInfo }
                    ?.toList() ?: listOf()
            } else {
                contactsAdapter.contacts = listOf()
            }

            contactsAdapter.notifyDataSetChanged()
        } else {
            Timber.tag(HomeworkApplication.TAG)
                .i("SecondActivity didn't return OK result")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFirstLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.contactsRecyclerView.adapter = contactsAdapter

        connectActions()
    }

    private fun connectActions() {
        binding.nextButtonCard.requestDataButton.setOnClickListener {
            secondActivityResult.launch(
                Intent(this, SecondActivity::class.java)
            )
        }
    }
}
