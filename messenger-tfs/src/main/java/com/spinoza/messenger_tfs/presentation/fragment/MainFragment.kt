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
import com.spinoza.messenger_tfs.presentation.cicerone.Screens


class MainFragment : Fragment(), OnItemSelectedListener {

    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding ?: throw RuntimeException("FragmentMainBinding == null")

    lateinit var navigatorHolder: NavigatorHolder
    lateinit var router: Router
    private val localNavigator by lazy {
        AppNavigator(requireActivity(), R.id.fragmentContainer)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            when (binding.bottomNavigationView.selectedItemId) {
                R.id.menu_channels -> {
                    requireActivity().moveTaskToBack(true)
                    requireActivity().finish()
                }
                R.id.menu_people, R.id.menu_profile -> {
                    router.replaceScreen(Screens.Channels())
                    binding.bottomNavigationView.selectedItemId = R.id.menu_channels
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cicerone = Cicerone.create()
        navigatorHolder = cicerone.getNavigatorHolder()
        router = cicerone.router
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

        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            onBackPressedCallback
        )

        binding.bottomNavigationView.setOnItemSelectedListener(this)

        if (savedInstanceState == null) {
            router.newRootScreen(Screens.Channels())
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_channels -> router.replaceScreen(Screens.Channels())
            R.id.menu_people -> router.replaceScreen(Screens.People())
            R.id.menu_profile -> router.replaceScreen(Screens.Profile())
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        navigatorHolder.setNavigator(localNavigator)
    }

    override fun onPause() {
        super.onPause()
        navigatorHolder.removeNavigator()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }
}