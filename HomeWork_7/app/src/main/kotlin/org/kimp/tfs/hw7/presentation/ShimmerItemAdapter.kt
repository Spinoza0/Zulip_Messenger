package org.kimp.tfs.hw7.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlin.reflect.KClass

class ShimmerItemAdapter<T : ViewBinding>(
    private val count: Int,
    private val type: KClass<T>
) : RecyclerView.Adapter<ShimmerItemAdapter<T>.ShimmerItemViewHolder>() {
    override fun getItemCount() = count

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimmerItemViewHolder {
        val inflateMethod = type.java
            .getMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.java
            )
        return ShimmerItemViewHolder(
            inflateMethod.invoke(
                null,
                LayoutInflater.from(parent.context),
                parent,
                false
            ) as ViewBinding
        )
    }

    override fun onBindViewHolder(holder: ShimmerItemViewHolder, position: Int) {}

    inner class ShimmerItemViewHolder(
        binding: ViewBinding
    ) : RecyclerView.ViewHolder(binding.root)
}
