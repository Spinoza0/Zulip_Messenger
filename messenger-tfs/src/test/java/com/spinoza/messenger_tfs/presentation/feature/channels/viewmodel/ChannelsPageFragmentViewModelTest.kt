package com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.spinoza.messenger_tfs.domain.usecase.channels.GetChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.channels.GetStoredChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.channels.GetStoredTopicsUseCase
import com.spinoza.messenger_tfs.domain.usecase.channels.GetTopicUseCase
import com.spinoza.messenger_tfs.domain.usecase.channels.GetTopicsUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.GetChannelEventsUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.RegisterEventQueueUseCase
import com.spinoza.messenger_tfs.presentation.feature.channels.model.ChannelsPageScreenEvent
import com.spinoza.messenger_tfs.stub.AppRouterStub
import com.spinoza.messenger_tfs.stub.DaoRepositoryStub
import com.spinoza.messenger_tfs.stub.WebRepositoryStub
import com.spinoza.messenger_tfs.util.MainDispatcherRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChannelsPageFragmentViewModelTest {

    @get:Rule
    val viewModelRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `ChannelsPageScreenEventUiLoad returns not empty list`() = runTest {
        val viewModel = createViewModel(this.backgroundScope)
        val itemsBefore = viewModel.state.value.items
        val event = ChannelsPageScreenEvent.Ui.Load

        viewModel.accept(event)

        val itemsAfter = viewModel.state.value.items
        assertNotEquals(itemsBefore, itemsAfter)
        assertEquals(true, itemsAfter?.isNotEmpty())
    }

    private fun createViewModel(scope: CoroutineScope?): ChannelsPageFragmentViewModel {
        val webRepository = WebRepositoryStub()
        val daoRepository = DaoRepositoryStub()
        return ChannelsPageFragmentViewModel(
            isSubscribed = true,
            router = AppRouterStub(),
            getStoredChannelsUseCase = GetStoredChannelsUseCase(daoRepository),
            getStoredTopicsUseCase = GetStoredTopicsUseCase(daoRepository),
            getTopicsUseCase = GetTopicsUseCase(webRepository),
            getChannelsUseCase = GetChannelsUseCase(webRepository),
            getTopicUseCase = GetTopicUseCase(webRepository),
            getChannelEventsUseCase = GetChannelEventsUseCase(webRepository),
            registerEventQueueUseCase = RegisterEventQueueUseCase(webRepository),
            deleteEventQueueUseCase = DeleteEventQueueUseCase(webRepository),
            defaultDispatcher = mainDispatcherRule.testDispatcher,
            customCoroutineScope = scope
        )
    }
}