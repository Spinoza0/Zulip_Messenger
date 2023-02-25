package com.spinoza.homework_1.presentation.adapter

import androidx.recyclerview.widget.RecyclerView
import com.spinoza.homework_1.databinding.ContactItemBinding
import com.spinoza.homework_1.domain.Contact

class ContactViewHolder(
    private val binding: ContactItemBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(contact: Contact, onItemClick: (Contact) -> Unit) {
        binding.textViewName.text = contact.name
        binding.textViewPhone.text = contact.phone
        itemView.setOnClickListener { onItemClick.invoke(contact) }
    }
}
