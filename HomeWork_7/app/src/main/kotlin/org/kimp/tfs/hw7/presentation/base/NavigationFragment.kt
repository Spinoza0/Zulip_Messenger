package org.kimp.tfs.hw7.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import dagger.hilt.android.AndroidEntryPoint
import org.kimp.tfs.hw7.databinding.FragmentNavigationBinding

@AndroidEntryPoint
class NavigationFragment : Fragment() {
    private lateinit var binding: FragmentNavigationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNavigationBinding.inflate(
            inflater, container, false
        )

        NavigationUI.setupWithNavController(
            binding.bottomNavigationView,
            (childFragmentManager.findFragmentById(
                binding.navigationContentContainerView.id
            ) as NavHostFragment).navController
        )

        return binding.root
    }
}
