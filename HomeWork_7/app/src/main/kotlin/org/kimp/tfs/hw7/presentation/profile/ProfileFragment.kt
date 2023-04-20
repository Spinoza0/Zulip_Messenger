package org.kimp.tfs.hw7.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kimp.tfs.hw7.R
import org.kimp.tfs.hw7.data.api.Profile
import org.kimp.tfs.hw7.databinding.FragmentProfileBinding
import org.kimp.tfs.hw7.presentation.base.UserNetworkStatusView
import org.kimp.tfs.hw7.presentation.profile.elm.Effect
import org.kimp.tfs.hw7.presentation.profile.elm.Event
import org.kimp.tfs.hw7.presentation.profile.elm.State
import org.kimp.tfs.hw7.utils.getNormalColor
import timber.log.Timber
import vivid.money.elmslie.android.base.ElmFragment
import vivid.money.elmslie.android.storeholder.StoreHolder
import vivid.money.elmslie.core.store.Store
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : ElmFragment<Event, Effect, State>() {
    private lateinit var binding: FragmentProfileBinding

    @Inject lateinit var injectedStore: Store<Event, Effect, State>
    @Inject lateinit var imageLoader: ImageLoader

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(
            inflater, container, false
        )


        store.accept(Event.Ui.AuthenticatedUserRequested)


        binding.navigateBackButton.setOnClickListener {
            store.stop()
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    override fun render(state: State) {
        Timber.tag(TAG).i("Try to render state: $state")

        if (state.isLoading) {
            binding.contentShimmer.showShimmer(true)
            binding.contentShimmer.startShimmer()

            binding.userAvatarImageView.setImageDrawable(null)
            binding.userAvatarImageView.setBackgroundColor(requireContext().getNormalColor())

            ResourcesCompat.getDrawable(resources, R.drawable.shimmer_text_view, requireContext().theme).also {
                binding.userNameTextView.background = it
                binding.userNetworkStatusView.background = it
            }

            binding.userNetworkStatusView.status = UserNetworkStatusView.UserNetworkStatus.UNKNOWN
            binding.userNameTextView.text = ""
        }
        else if (state.loadedProfile != null) {
            binding.contentShimmer.stopShimmer()
            binding.contentShimmer.hideShimmer()

            renderProfile(state.loadedProfile)
        }
        else if (state.error != null) {
            Timber.tag(TAG).e(state.error, "Unable to load authenticated user")

            Snackbar.make(
                binding.root,
                getString(R.string.profile_fragment_loading_error, ERROR_DELAY_SECONDS),
                Snackbar.LENGTH_LONG
            ).show()

            lifecycleScope.launch(Dispatchers.IO) {
                Thread.sleep(ERROR_DELAY_SECONDS * 1000)
                store.accept(Event.Ui.AuthenticatedUserRequested)
            }
        }
    }

    private fun renderProfile(profile: Profile) {
        binding.userNameTextView.text = profile.fullName
        binding.userNetworkStatusView.status = when (profile.isActive) {
            true -> UserNetworkStatusView.UserNetworkStatus.ONLINE
            else -> UserNetworkStatusView.UserNetworkStatus.IDLE
        }

        binding.userNetworkStatusView.background = null
        binding.userNameTextView.background = null

        ImageRequest.Builder(requireContext())
            .data(profile.avatarUrl)
            .allowHardware(true)
            .crossfade(200)
            .placeholder(R.drawable.user_placeholder)
            .target(binding.userAvatarImageView)
            .build()
            .also(imageLoader::enqueue)
    }

    override val initEvent: Event = Event.Ui.FragmentInitialized

    override val storeHolder: StoreHolder<Event, Effect, State>
        get() = object : StoreHolder<Event, Effect, State> {
            override val isStarted: Boolean
                get() = this@ProfileFragment::injectedStore.isInitialized
            override val store: Store<Event, Effect, State>
                get() = this@ProfileFragment.injectedStore
        }

    companion object {
        const val TAG = "ProfileFragment"
        const val ERROR_DELAY_SECONDS = 10L
    }
}
