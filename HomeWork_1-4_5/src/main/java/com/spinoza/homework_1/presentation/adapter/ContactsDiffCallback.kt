package com.spinoza.homework_1.presentation.adapter

import androidx.recyclerview.widget.DiffUtil
import com.spinoza.homework_1.domain.Contact

class ContactsDiffCallback : DiffUtil.ItemCallback<Contact>() {
    override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem == newItem
    }
}

