package com.spinoza.messenger_tfs.presentation.feature.channels

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.CreateChannelDialogBinding
import com.spinoza.messenger_tfs.databinding.DialogChannelActionsBinding
import com.spinoza.messenger_tfs.databinding.FragmentChannelsPageBinding
import com.spinoza.messenger_tfs.di.channels.DaggerChannelsComponent
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.network.WebLimitation
import com.spinoza.messenger_tfs.presentation.adapter.MainDelegateAdapter
import com.spinoza.messenger_tfs.presentation.feature.channels.adapter.ChannelDelegate
import com.spinoza.messenger_tfs.presentation.feature.channels.adapter.CreateChannelDelegate
import com.spinoza.messenger_tfs.presentation.feature.channels.adapter.TopicDelegate
import com.spinoza.messenger_tfs.presentation.feature.channels.model.ChannelsPageScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.channels.model.ChannelsPageScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.channels.model.ChannelsPageScreenState
import com.spinoza.messenger_tfs.presentation.feature.channels.model.ChannelsScreenState
import com.spinoza.messenger_tfs.presentation.feature.channels.model.SearchQuery
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsFragmentSharedViewModel
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsPageFragmentViewModel
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.factory.ViewModelFactory
import com.spinoza.messenger_tfs.presentation.util.DIRECTION_DOWN
import com.spinoza.messenger_tfs.presentation.util.DIRECTION_UP
import com.spinoza.messenger_tfs.presentation.util.closeApplication
import com.spinoza.messenger_tfs.presentation.util.getAppComponent
import com.spinoza.messenger_tfs.presentation.util.getThemeColor
import com.spinoza.messenger_tfs.presentation.util.off
import com.spinoza.messenger_tfs.presentation.util.on
import com.spinoza.messenger_tfs.presentation.util.showCheckInternetConnectionDialog
import com.spinoza.messenger_tfs.presentation.util.showConfirmationDialog
import com.spinoza.messenger_tfs.presentation.util.showError
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class ChannelsPageFragment : Fragment() {

    @Inject
    lateinit var channelsAdapter: MainDelegateAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var webLimitation: WebLimitation

    private val store: ChannelsPageFragmentViewModel by viewModels { viewModelFactory }

    private val sharedStore: ChannelsFragmentSharedViewModel by activityViewModels {
        viewModelFactory
    }

    private var isSubscribed = true
    private val isShowingChannelMenu = AtomicBoolean(false)

    private var _binding: FragmentChannelsPageBinding? = null
    private val binding: FragmentChannelsPageBinding
        get() = _binding ?: throw RuntimeException("FragmentChannelsPageBinding == null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChannelsPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parseParams()
        DaggerChannelsComponent.factory()
            .create(requireContext().getAppComponent(), isSubscribed)
            .inject(this)
        if (savedInstanceState != null) {
            store.accept(ChannelsPageScreenEvent.Ui.CheckLoginStatus)
        }
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        with(channelsAdapter) {
            addDelegate(
                ChannelDelegate(
                    getString(R.string.channel_name_template),
                    { channelItem ->
                        val messagesFilter = MessagesFilter(channelItem.channel, Topic())
                        store.accept(ChannelsPageScreenEvent.Ui.OpenMessagesScreen(messagesFilter))
                    },
                    { channelItem ->
                        store.accept(ChannelsPageScreenEvent.Ui.ShowChannelMenu(channelItem))
                    }
                ) { channelItem ->
                    store.accept(ChannelsPageScreenEvent.Ui.OnChannelClick(channelItem))
                }
            )
            addDelegate(
                TopicDelegate(
                    requireContext().getThemeColor(R.attr.even_topic_color),
                    requireContext().getThemeColor(R.attr.odd_topic_color)
                ) { messagesFilter ->
                    store.accept(ChannelsPageScreenEvent.Ui.OpenMessagesScreen(messagesFilter))
                }
            )
            addDelegate(CreateChannelDelegate {
                showCreateChannelDialog()
            })
        }
        binding.recyclerViewChannels.adapter = channelsAdapter
        binding.recyclerViewChannels.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                store.accept(ChannelsPageScreenEvent.Ui.OnScrolled)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    store.accept(ChannelsPageScreenEvent.Ui.ScrollStateDragging)
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    store.accept(
                        ChannelsPageScreenEvent.Ui.ScrollStateIdle(
                            recyclerView.canScrollVertically(DIRECTION_UP),
                            recyclerView.canScrollVertically(DIRECTION_DOWN)
                        )
                    )
                }
            }
        })
        binding.recyclerViewChannels.itemAnimator = null
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                store.state.collect(::handleState)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedStore.state.collect(::handleSharedScreenState)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                store.effects.collect(::handleEffect)
            }
        }
    }

    private fun handleState(state: ChannelsPageScreenState) {
        if (state.isLoading) {
            binding.shimmerLarge.on()
        } else {
            binding.shimmerLarge.off()
        }
        state.items?.let {
            channelsAdapter.submitList(it)
        }
    }

    private fun handleEffect(effect: ChannelsPageScreenEffect) {
        when (effect) {
            is ChannelsPageScreenEffect.ShowChannelMenu -> showChannelMenu(effect)
            is ChannelsPageScreenEffect.Failure.Error ->
                showError("${getString(R.string.error_channels)} ${effect.value}")

            is ChannelsPageScreenEffect.Failure.Network ->
                showCheckInternetConnectionDialog({
                    store.accept(ChannelsPageScreenEvent.Ui.Load)
                }) {
                    closeApplication()
                }
        }
    }

    private fun handleSharedScreenState(state: ChannelsScreenState) {
        state.filter?.let { filter ->
            if (filter.isSubscribed() == isSubscribed) {
                store.accept(
                    ChannelsPageScreenEvent.Ui.Filter(ChannelsFilter(filter.text, isSubscribed))
                )
            }
        }
    }

    private fun showChannelMenu(effect: ChannelsPageScreenEffect.ShowChannelMenu) {
        if (isShowingChannelMenu.get()) return
        isShowingChannelMenu.set(true)
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogChannelActionsBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        val channelName = effect.channelItem.channel.name
        with(dialogBinding) {
            textViewTitle.text =
                String.format(getString(R.string.channel_name_template), channelName)
            textViewSubscribe.isVisible = effect.isItemSubscribeVisible
            textViewUnsubscribe.isVisible = effect.isItemUnsubscribeVisible
            textViewDelete.isVisible = effect.isItemDeleteVisible
            textViewSubscribe.setOnClickListener {
                store.accept(ChannelsPageScreenEvent.Ui.SubscribeToChannel(channelName))
                dialog.dismiss()
            }
            textViewUnsubscribe.setOnClickListener {
                store.accept(ChannelsPageScreenEvent.Ui.UnsubscribeFromChannel(channelName))
                dialog.dismiss()
            }
            textViewDelete.setOnClickListener {
                confirmDeleteChannel(effect.channelItem.channel)
                dialog.dismiss()
            }
        }
        dialog.setOnDismissListener {
            isShowingChannelMenu.set(false)
        }
        dialog.show()
    }

    private fun confirmDeleteChannel(channel: Channel) {
        showConfirmationDialog(
            title = getString(R.string.confirm_delete_channel),
            message = channel.name,
            onPositiveClickCallback = {
                store.accept(ChannelsPageScreenEvent.Ui.DeleteChannel(channel.channelId))
            }
        )
    }

    private fun showCreateChannelDialog() {
        val dialogFields = CreateChannelDialogBinding.inflate(layoutInflater)
        with(dialogFields.inputChannelName) {
            inputType = InputType.TYPE_CLASS_TEXT
            filters = arrayOf(InputFilter.LengthFilter(webLimitation.getMaxChannelName()))
        }
        with(dialogFields.inputChannelDescription) {
            inputType = InputType.TYPE_CLASS_TEXT
            filters = arrayOf(InputFilter.LengthFilter(webLimitation.getMaxChannelDescription()))
        }
        showConfirmationDialog(
            title = getString(R.string.create_channel),
            view = dialogFields.root,
            positiveButtonTitleResId = R.string.create,
            negativeButtonTitleResId = R.string.cancel,
            onPositiveClickCallback = {
                store.accept(
                    ChannelsPageScreenEvent.Ui.CreateChannel(
                        dialogFields.inputChannelName.text,
                        dialogFields.inputChannelDescription.text
                    )
                )
            }
        )
    }

    private fun parseParams() {
        isSubscribed = arguments?.getBoolean(PARAM_IS_SUBSCRIBED, true) ?: true
    }

    override fun onResume() {
        super.onResume()
        store.accept(ChannelsPageScreenEvent.Ui.UpdateMessageCount)
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerLarge.off()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        channelsAdapter.clear()
        binding.recyclerViewChannels.adapter = null
        _binding = null
    }

    private fun SearchQuery.isSubscribed() = screenPosition % 2 == 0

    companion object {

        private const val PARAM_IS_SUBSCRIBED = "isSubscribed"

        fun newInstance(isSubscribed: Boolean): ChannelsPageFragment {
            return ChannelsPageFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(PARAM_IS_SUBSCRIBED, isSubscribed)
                }
            }
        }
    }
}