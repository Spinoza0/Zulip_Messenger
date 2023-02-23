package com.spinoza.homework_1.presentation.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.spinoza.homework_1.R
import com.spinoza.homework_1.databinding.ActivityMainBinding
import com.spinoza.homework_1.domain.ContactsList
import com.spinoza.homework_1.domain.getContactsListFromIntent
import com.spinoza.homework_1.presentation.adapter.ContactsAdapter
import com.spinoza.homework_1.presentation.utils.Constants.Companion.EXTRA_CONTACTS_LIST
import com.spinoza.homework_1.presentation.utils.Constants.Companion.EXTRA_ERROR_TEXT

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val unknownError by lazy {
        getString(R.string.unknown_error)
    }

    private val noContacts by lazy {
        getString(R.string.no_contacts)
    }

    private val contactsAdapter by lazy { ContactsAdapter() }

    private val getContacts =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            val data: Intent? = result.data
            if (data == null) {
                showError(unknownError)
            }
            if (result.resultCode == Activity.RESULT_OK) {
                data?.let {
                    val contactsList = getContactsListFromIntent(data)
                    setupVisibility(View.VISIBLE, View.GONE)
                    if (contactsList.value.isNotEmpty()) {
                        contactsAdapter.submitList(contactsList.value)
                    } else {
                        showError(noContacts)
                    }
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                data?.let {
                    var error = data.getStringExtra(EXTRA_ERROR_TEXT)
                    if (error == null || error.isEmpty()) {
                        error = unknownError
                    }
                    showError(error)
                }
            }
        }

    private fun showError(error: String) {
        setupVisibility(View.GONE, View.VISIBLE)
        binding.textViewError.text = error
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
    }

    private fun setupVisibility(recyclerViewVisibility: Int, textViewErrorVisibility: Int) {
        binding.recyclerViewResult.visibility = recyclerViewVisibility
        binding.textViewError.visibility = textViewErrorVisibility
    }

    private fun setupListeners() {
        binding.buttonStartWork.setOnClickListener {
            getContacts.launch(GetContactsActivity.newIntent(this))
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewResult.adapter = contactsAdapter
        contactsAdapter.openDialer = { openDialer(it) }
    }

    private fun openDialer(phone: String) {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone}")))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_CONTACTS_LIST, ContactsList(contactsAdapter.currentList))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        getContactsListFromBundle(savedInstanceState).apply {
            contactsAdapter.submitList(this.value)
        }
    }

    private fun getContactsListFromBundle(bundle: Bundle): ContactsList {
        val contactsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(EXTRA_CONTACTS_LIST, ContactsList::class.java)
        } else {
            @Suppress("deprecation")
            bundle.getParcelable<ContactsList>(EXTRA_CONTACTS_LIST) as ContactsList
        }
        contactsList?.let { value ->
            return value
        }

        throw RuntimeException("Parameter ContactsList not found in bundle")
    }
}