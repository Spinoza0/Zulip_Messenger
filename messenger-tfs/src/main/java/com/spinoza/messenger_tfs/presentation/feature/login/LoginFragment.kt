package com.spinoza.messenger_tfs.presentation.feature.login

import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentLoginBinding
import com.spinoza.messenger_tfs.di.login.DaggerLoginComponent
import com.spinoza.messenger_tfs.presentation.feature.app.utils.getAppComponent
import com.spinoza.messenger_tfs.presentation.feature.app.utils.showCheckInternetConnectionDialog
import com.spinoza.messenger_tfs.presentation.feature.app.utils.showError
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenState
import vivid.money.elmslie.android.base.ElmFragment
import vivid.money.elmslie.android.storeholder.LifecycleAwareStoreHolder
import vivid.money.elmslie.android.storeholder.StoreHolder
import vivid.money.elmslie.coroutines.ElmStoreCompat
import javax.inject.Inject

class LoginFragment : ElmFragment<LoginScreenEvent, LoginScreenEffect, LoginScreenState>() {

    @Inject
    lateinit var loginStore: ElmStoreCompat<
            LoginScreenEvent,
            LoginScreenState,
            LoginScreenEffect,
            LoginScreenCommand>

    private var _binding: FragmentLoginBinding? = null
    private val binding: FragmentLoginBinding
        get() = _binding ?: throw RuntimeException("FragmentLoginBinding == null")

    override val initEvent: LoginScreenEvent
        get() = LoginScreenEvent.Ui.Init

    override val storeHolder:
            StoreHolder<LoginScreenEvent, LoginScreenEffect, LoginScreenState> by lazy {
        LifecycleAwareStoreHolder(lifecycle) { loginStore }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerLoginComponent.factory().create(context.getAppComponent(), lifecycle).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        binding.textViewForgotPassword.movementMethod = LinkMovementMethod.getInstance()
        store.accept(LoginScreenEvent.Ui.CheckPreviousLogin(requireContext(), getParamLogout()))
    }

    override fun render(state: LoginScreenState) {
        with(binding) {
            progressBar.isVisible = state.isCheckingLogin
            editTextEmail.isVisible = state.isNeedLogin
            editTextPassword.isVisible = state.isNeedLogin
            buttonLogin.isVisible = state.isNeedLogin
            textViewForgotPassword.isVisible = state.isNeedLogin
        }
    }

    override fun handleEffect(effect: LoginScreenEffect) {
        when (effect) {
            is LoginScreenEffect.ButtonStatus -> binding.buttonLogin.isEnabled = effect.isEnabled
            is LoginScreenEffect.Failure.ErrorLogin -> showError(
                String.format(getString(R.string.error_login), effect.value)
            )
            is LoginScreenEffect.Failure.ErrorNetwork -> {
                showError(String.format(getString(R.string.error_network), effect.value))
                showCheckInternetConnectionDialog({ }) {
                    store.accept(LoginScreenEvent.Ui.Exit)
                }
            }
        }
    }

    private fun setupListeners() {
        with(binding) {
            editTextEmail.doOnTextChanged { text, _, _, _ ->
                store.accept(LoginScreenEvent.Ui.NewEmailText(text))
            }
            editTextPassword.doOnTextChanged { text, _, _, _ ->
                store.accept(LoginScreenEvent.Ui.NewPasswordText(text))
            }
            buttonLogin.setOnClickListener {
                store.accept(
                    LoginScreenEvent.Ui.ButtonPressed(
                        editTextEmail.text.toString(),
                        editTextPassword.text.toString()
                    )
                )
            }
        }
    }

    private fun getParamLogout(): Boolean {
        return arguments?.getBoolean(PARAM_LOGOUT, false) ?: false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        private const val PARAM_LOGOUT = "logout"

        fun newInstance(logout: Boolean = false): LoginFragment {
            return LoginFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(PARAM_LOGOUT, logout)
                }
            }
        }
    }
}