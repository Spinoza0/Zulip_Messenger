package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.spinoza.messenger_tfs.databinding.FragmentMainPeopleBinding

class MainPeopleFragment : Fragment() {

    private var _binding: FragmentMainPeopleBinding? = null
    private val binding: FragmentMainPeopleBinding
        get() = _binding ?: throw RuntimeException("FragmentMainPeopleBinding == null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMainPeopleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): MainPeopleFragment {
            return MainPeopleFragment()
        }
    }
}