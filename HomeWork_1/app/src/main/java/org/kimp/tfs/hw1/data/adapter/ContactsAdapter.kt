package org.kimp.tfs.hw1.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.kimp.tfs.hw1.data.model.ContactInfo
import org.kimp.tfs.hw1.databinding.ViewContactCardBinding

class ContactsAdapter(
    var contacts: List<ContactInfo>
): RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder =
        ContactsViewHolder(
            ViewContactCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.binding.contact = contacts[position]
    }

    inner class ContactsViewHolder(
        val binding: ViewContactCardBinding
    ): RecyclerView.ViewHolder(binding.root) {}
}
