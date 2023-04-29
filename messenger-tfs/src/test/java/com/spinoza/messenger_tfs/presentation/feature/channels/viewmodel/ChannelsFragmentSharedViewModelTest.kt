package com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.spinoza.messenger_tfs.presentation.feature.channels.model.ChannelsScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.channels.model.SearchQuery
import com.spinoza.messenger_tfs.presentation.util.MainDispatcherRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

class ChannelsFragmentSharedViewModelTest {

    @get:Rule
    val viewModelRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `ChannelsScreenEventUiFilter emits new SearchQuery`() = runBlocking {
        val viewModel = createViewModel()
        val searchQueryBefore = viewModel.state.value.filter
        val newSearchQuery = SearchQuery(0, "string")
        val event = ChannelsScreenEvent.Ui.Filter(newSearchQuery)

        viewModel.accept(event)

        delay((ChannelsFragmentSharedViewModel.DELAY_BEFORE_FILTER_CHANGE * 1.1f).toLong())
        val searchQueryAfter = viewModel.state.value.filter
        assertNotEquals(newSearchQuery, searchQueryBefore)
        assertEquals(newSearchQuery, searchQueryAfter)
    }

    private fun createViewModel(): ChannelsFragmentSharedViewModel {
        return ChannelsFragmentSharedViewModel()
    }
}