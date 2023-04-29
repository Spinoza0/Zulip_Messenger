package com.spinoza.messenger_tfs.presentation.feature.people

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentPeopleBinding
import com.spinoza.messenger_tfs.di.people.DaggerPeopleComponent
import com.spinoza.messenger_tfs.presentation.feature.people.adapter.PeopleAdapter
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenState
import com.spinoza.messenger_tfs.presentation.util.getAppComponent
import com.spinoza.messenger_tfs.presentation.util.getInstanceState
import com.spinoza.messenger_tfs.presentation.util.getParam
import com.spinoza.messenger_tfs.presentation.util.off
import com.spinoza.messenger_tfs.presentation.util.on
import com.spinoza.messenger_tfs.presentation.util.restoreInstanceState
import com.spinoza.messenger_tfs.presentation.util.showCheckInternetConnectionDialog
import com.spinoza.messenger_tfs.presentation.util.showError
import vivid.money.elmslie.android.base.ElmFragment
import vivid.money.elmslie.android.storeholder.LifecycleAwareStoreHolder
import vivid.money.elmslie.android.storeholder.StoreHolder
import vivid.money.elmslie.coroutines.ElmStoreCompat
import javax.inject.Inject

class PeopleFragment : ElmFragment<PeopleScreenEvent, PeopleScreenEffect, PeopleScreenState>() {

    @Inject
    lateinit var peopleStore: ElmStoreCompat<
            PeopleScreenEvent,
            PeopleScreenState,
            PeopleScreenEffect,
            PeopleScreenCommand>

    private var _binding: FragmentPeopleBinding? = null
    private val binding: FragmentPeopleBinding
        get() = _binding ?: throw RuntimeException("FragmentPeopleBinding == null")

    private var recyclerViewState: Parcelable? = null
    private var recyclerViewStateOnDestroy: Parcelable? = null

    override val initEvent: PeopleScreenEvent
        get() = PeopleScreenEvent.Ui.Init

    override val storeHolder:
            StoreHolder<PeopleScreenEvent, PeopleScreenEffect, PeopleScreenState> by lazy {
        LifecycleAwareStoreHolder(lifecycle) { peopleStore }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerPeopleComponent.factory().create(context.getAppComponent(), lifecycle).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPeopleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            recyclerViewState = it.getParam<Parcelable>(PARAM_RECYCLERVIEW_STATE)
        }
        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewUsers.adapter = PeopleAdapter(::onUserClickListener)
        binding.recyclerViewUsers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                store.accept(
                    PeopleScreenEvent.Ui.OnScrolled(
                        recyclerView.canScrollVertically(PeopleScreenEvent.DIRECTION_UP),
                        recyclerView.canScrollVertically(PeopleScreenEvent.DIRECTION_DOWN),
                        dy
                    )
                )
            }
        })
    }

    private fun setupListeners() {
        binding.editTextSearch.doOnTextChanged { text, _, _, _ ->
            store.accept(PeopleScreenEvent.Ui.Filter(text.toString()))
        }
    }

    override fun render(state: PeopleScreenState) {
        if (state.isLoading) {
            if (isPeopleListEmpty()) binding.shimmerLarge.on()
        } else {
            binding.shimmerLarge.off()
        }
        state.users?.let {
            (binding.recyclerViewUsers.adapter as PeopleAdapter).submitList(it) {
                if (recyclerViewState != null) {
                    binding.recyclerViewUsers.restoreInstanceState(recyclerViewState)
                    recyclerViewState = null
                }
            }
        }
    }

    override fun handleEffect(effect: PeopleScreenEffect) {
        when (effect) {
            is PeopleScreenEffect.Failure.ErrorLoadingUsers ->
                showError("${getString(R.string.error_loading_users)} ${effect.value}")

            is PeopleScreenEffect.Failure.ErrorNetwork ->
                showCheckInternetConnectionDialog(
                    { store.accept(PeopleScreenEvent.Ui.Load) }
                ) {
                    store.accept(PeopleScreenEvent.Ui.OpenMainMenu)
                }
        }
    }

    private fun onUserClickListener(userId: Long) {
        store.accept(PeopleScreenEvent.Ui.ShowUserInfo(userId))
    }

    private fun isPeopleListEmpty(): Boolean {
        return (binding.recyclerViewUsers.adapter as PeopleAdapter).itemCount == NO_ITEMS
    }

    override fun onResume() {
        super.onResume()
        store.accept(PeopleScreenEvent.Ui.Filter(binding.editTextSearch.text.toString()))
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerLarge.off()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        recyclerViewState = if (_binding != null) {
            binding.recyclerViewUsers.getInstanceState()
        } else {
            recyclerViewStateOnDestroy
        }
        outState.putParcelable(PARAM_RECYCLERVIEW_STATE, recyclerViewState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerViewStateOnDestroy = binding.recyclerViewUsers.getInstanceState()
        _binding = null
    }

    companion object {

        private const val NO_ITEMS = 0
        private const val PARAM_RECYCLERVIEW_STATE = "recyclerViewState"

        fun newInstance(): PeopleFragment {
            return PeopleFragment()
        }
    }
}