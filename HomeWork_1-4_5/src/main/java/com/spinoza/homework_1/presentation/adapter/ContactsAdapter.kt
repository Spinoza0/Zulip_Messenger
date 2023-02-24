package com.spinoza.homework_1.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.spinoza.homework_1.databinding.ContactItemBinding
import com.spinoza.homework_1.domain.Contact

class ContactsAdapter : ListAdapter<Contact, ContactViewHolder>(ContactsDiffCallback()) {
    var onItemClick: ((Contact) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ContactItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }
}