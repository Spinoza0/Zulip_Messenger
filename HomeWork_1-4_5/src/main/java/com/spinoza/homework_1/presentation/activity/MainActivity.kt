package com.spinoza.homework_1.presentation.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.spinoza.homework_1.R
import com.spinoza.homework_1.databinding.ActivityMainBinding
import com.spinoza.homework_1.domain.Contact
import com.spinoza.homework_1.domain.GetContactsResult
import com.spinoza.homework_1.presentation.adapter.ContactsAdapter

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val contactsAdapter = ContactsAdapter()

    private val getContactsLauncher = registerForActivityResult(
        GetContactsContract(),
        ::handleGetContactsResult
    )

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
            getContactsLauncher.launch(null)
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewResult.adapter = contactsAdapter
        contactsAdapter.onItemClick = ::openDialer
    }

    private fun openDialer(contact: Contact) {
        if (contact.phone.isNotEmpty()) {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}")))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_CONTACTS_LIST, ArrayList(contactsAdapter.currentList))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        contactsAdapter.submitList(getContactsListFromBundle(savedInstanceState))
    }

    private fun getContactsListFromBundle(bundle: Bundle): List<Contact> {
        val contactsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelableArrayList(EXTRA_CONTACTS_LIST, Contact::class.java)
        } else {
            @Suppress("deprecation")
            bundle.getParcelableArrayList(EXTRA_CONTACTS_LIST)
        }

        return contactsList ?: throw RuntimeException("Parameter ContactsList not found in bundle")
    }

    private fun showError(error: String = "") {
        setupVisibility(View.GONE, View.VISIBLE)
        binding.textViewError.text = error.ifEmpty {
            getString(R.string.unknown_error)
        }
    }

    private fun handleGetContactsResult(getContactsResult: GetContactsResult) {
        when (getContactsResult) {
            is GetContactsResult.Error -> {
                showError(getContactsResult.message)
            }
            is GetContactsResult.Success -> {
                val contacts = getContactsResult.contacts
                if (contacts.isNotEmpty()) {
                    setupVisibility(View.VISIBLE, View.GONE)
                    contactsAdapter.submitList(contacts)
                } else {
                    showError(getString(R.string.no_contacts))
                }
            }
        }
    }

    companion object {
        private const val EXTRA_CONTACTS_LIST = "contacts"
    }
}