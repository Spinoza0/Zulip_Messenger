package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.spinoza.messenger_tfs.databinding.FragmentMainProfileBinding

class MainProfileFragment : Fragment() {

    private var _binding: FragmentMainProfileBinding? = null
    private val binding: FragmentMainProfileBinding
        get() = _binding ?: throw RuntimeException("FragmentMainProfileBinding == null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMainProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): MainProfileFragment {
            return MainProfileFragment()
        }
    }
}