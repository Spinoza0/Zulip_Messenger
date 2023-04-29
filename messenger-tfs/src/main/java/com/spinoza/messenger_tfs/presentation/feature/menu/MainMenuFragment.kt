package com.spinoza.messenger_tfs.presentation.feature.menu

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
import com.spinoza.messenger_tfs.databinding.FragmentMainMenuBinding
import com.spinoza.messenger_tfs.presentation.feature.app.utils.closeApplication
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import com.spinoza.messenger_tfs.presentation.util.getThemeColor

class MainMenuFragment : Fragment(), OnItemSelectedListener {

    private var _binding: FragmentMainMenuBinding? = null
    private val binding: FragmentMainMenuBinding
        get() = _binding ?: throw RuntimeException("FragmentMainMenuBinding == null")

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
        _binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupScreen()
    }

    private fun setupOnBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (binding.bottomNavigationView.selectedItemId) {
                    R.id.menu_channels -> closeApplication()
                    R.id.menu_people, R.id.menu_profile -> {
                        binding.bottomNavigationView.selectedItemId = R.id.menu_channels
                    }
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun setupNavigation() {
        val localNavigator =
            AppNavigator(requireActivity(), R.id.fragmentContainer, childFragmentManager)
        navigatorHolder.setNavigator(localNavigator)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId != binding.bottomNavigationView.selectedItemId) {
            when (item.itemId) {
                R.id.menu_channels -> localRouter.backTo(Screens.ItemChannels())
                R.id.menu_people -> localRouter.navigateTo(Screens.ItemPeople())
                R.id.menu_profile -> localRouter.navigateTo(Screens.ItemProfile())
            }
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        setupNavigation()
        setupOnBackPressedCallback()
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

    private fun setupScreen() {
        requireActivity().window.statusBarColor =
            requireContext().getThemeColor(R.attr.background_500_color)
        binding.bottomNavigationView.setOnItemSelectedListener(this)
    }

    companion object {

        fun newInstance(): MainMenuFragment {
            return MainMenuFragment()
        }
    }
}