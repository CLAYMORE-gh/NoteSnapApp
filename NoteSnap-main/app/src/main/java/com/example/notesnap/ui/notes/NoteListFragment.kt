package com.example.notesnap.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.notesnap.R
import com.example.notesnap.databinding.FragmentNoteListBinding
import com.example.notesnap.ui.detail.NoteDetailFragment
import kotlinx.coroutines.launch

class NoteListFragment : Fragment() {

    private var _binding: FragmentNoteListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteListViewModel by viewModels { NoteListViewModel.Factory }

    private var searchView: SearchView? = null
    private var pendingQuery: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNoteListBinding.inflate(inflater, container, false)
        pendingQuery = arguments?.getString(ARG_QUERY)?.takeIf { it.isNotBlank() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = NoteAdapter { note ->
            val bundle = Bundle().apply { putLong(NoteDetailFragment.ARG_NOTE_ID, note.id) }
            findNavController().navigate(R.id.action_pages_to_detail, bundle)
        }
        binding.recyclerNotes.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notes.collect { notes ->
                    adapter.submitList(notes)
                    binding.textCount.text = when (notes.size) {
                        0 -> ""
                        1 -> getString(R.string.note_count_one)
                        else -> getString(R.string.note_count, notes.size)
                    }
                    updateEmptyState(notes.isEmpty())
                }
            }
        }

        applyPendingQuery()
        setupMenu()
    }

    private fun applyPendingQuery() {
        val query = pendingQuery ?: return
        pendingQuery = null
        viewModel.setQuery(query)
        view?.post {
            val item = searchView?.tag
            if (item is MenuItem) {
                item.expandActionView()
                searchView?.setQuery(query, false)
                searchView?.clearFocus()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.isVisible = isEmpty
        if (!isEmpty) return

        if (viewModel.currentQuery.isNotBlank()) {
            binding.textEmptyTitle.setText(R.string.no_search_results)
            binding.textEmptyMessage.setText(R.string.search_notes_hint)
        } else {
            binding.textEmptyTitle.setText(R.string.empty_notes_title)
            binding.textEmptyMessage.setText(R.string.empty_notes_message)
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_note_list, menu)

                val searchItem = menu.findItem(R.id.action_search)
                val view = searchItem.actionView as SearchView
                searchView = view
                view.tag = searchItem
                view.queryHint = getString(R.string.search_notes_hint)
                view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        view.clearFocus()
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.setQuery(newText.orEmpty())
                        return true
                    }
                })
            }

            override fun onPrepareMenu(menu: Menu) {
                val query = viewModel.currentQuery
                if (query.isNotBlank()) {
                    val item = menu.findItem(R.id.action_search)
                    item.expandActionView()
                    (item.actionView as SearchView).setQuery(query, false)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
                R.id.action_new_note -> {
                    findNavController().navigate(R.id.action_pages_to_editor)
                    true
                }
                else -> false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        searchView = null
    }

    companion object {
        private const val ARG_QUERY = "query"
    }
}
