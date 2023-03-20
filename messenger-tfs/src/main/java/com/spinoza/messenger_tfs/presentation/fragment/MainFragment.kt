package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentMainBinding
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import com.spinoza.messenger_tfs.presentation.ui.getThemeColor


class MainFragment : Fragment(), OnItemSelectedListener {

    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding ?: throw RuntimeException("FragmentMainBinding == null")

    private lateinit var navigatorHolder: NavigatorHolder
    private lateinit var localRouter: Router
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cicerone = Cicerone.create()
        navigatorHolder = cicerone.getNavigatorHolder()
        localRouter = cicerone.router

        if (savedInstanceState == null) {
            localRouter.newRootScreen(Screens.ItemChannels())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavigation()
        setupOnBackPressedCallback()
        setupStatusBar()
    }

    private fun setupOnBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (binding.bottomNavigationView.selectedItemId) {
                    R.id.menu_channels -> {
                        requireActivity().moveTaskToBack(true)
                        requireActivity().finish()
                    }
                    R.id.menu_people, R.id.menu_profile -> {
                        localRouter.replaceScreen(Screens.ItemChannels())
                        binding.bottomNavigationView.selectedItemId = R.id.menu_channels
                    }
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            onBackPressedCallback
        )
    }

    private fun setupNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener(this)
        val localNavigator =
            AppNavigator(requireActivity(), R.id.fragmentContainer, childFragmentManager)
        navigatorHolder.setNavigator(localNavigator)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_channels -> localRouter.replaceScreen(Screens.ItemChannels())
            R.id.menu_people -> localRouter.replaceScreen(Screens.ItemPeople())
            R.id.menu_profile -> localRouter.replaceScreen(Screens.ItemProfile())
        }
        return true
    }

    override fun onStop() {
        super.onStop()
        navigatorHolder.removeNavigator()
        onBackPressedCallback.remove()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupStatusBar() {
        requireActivity().window.statusBarColor =
            requireContext().getThemeColor(R.attr.background_500_color)
    }

    companion object {

        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }
}