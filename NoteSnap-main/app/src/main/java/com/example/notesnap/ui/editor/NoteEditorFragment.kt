package com.example.notesnap.ui.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.notesnap.R
import com.example.notesnap.data.ServiceLocator
import com.example.notesnap.data.model.Note
import com.example.notesnap.databinding.FragmentNoteEditorBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class NoteEditorFragment : Fragment() {

    private var _binding: FragmentNoteEditorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteEditorViewModel by viewModels { NoteEditorViewModel.Factory }

    private var existingNoteId: Long = 0L

    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            showDiscardDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNoteEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        existingNoteId = arguments?.getLong(ARG_NOTE_ID, 0L) ?: 0L
        val prefill = arguments?.getString(ARG_EXTRACTED_TEXT)

        binding.editContent.setText(prefill.orEmpty())
        if (!prefill.isNullOrBlank() && binding.editTitle.text.isNullOrBlank()) {
            binding.editTitle.setText(R.string.scan_title)
        }

        if (existingNoteId != 0L) {
            viewLifecycleOwner.lifecycleScope.launch {
                val note = viewModel.loadNote(existingNoteId)
                if (note != null && _binding != null) {
                    binding.editTitle.setText(note.title)
                    binding.editCategory.setText(note.category)
                    binding.editContent.setText(note.content)
                }
            }
        }

        val watcher = SimpleTextWatcher { updateDirtyState() }
        binding.editTitle.addTextChangedListener(watcher)
        binding.editCategory.addTextChangedListener(watcher)
        binding.editContent.addTextChangedListener(watcher)

        setupMenu()
        updateDirtyState()
    }

    private fun updateDirtyState() {
        backCallback.isEnabled = viewModel.isDirty(
            existingNoteId,
            binding.editTitle.text?.toString().orEmpty(),
            binding.editContent.text?.toString().orEmpty(),
            binding.editCategory.text?.toString().orEmpty(),
        )
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_note_editor, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
                R.id.action_save -> {
                    saveNote()
                    true
                }
                else -> false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun saveNote() {
        val title = binding.editTitle.text?.toString()?.trim().orEmpty()
        val category = binding.editCategory.text?.toString()?.trim().orEmpty()
        val content = binding.editContent.text?.toString()?.trim().orEmpty()

        if (title.isBlank() && content.isBlank()) {
            binding.editContent.error = getString(R.string.content_hint)
            return
        }

        val finalTitle = title.ifBlank {
            content.lineSequence().firstOrNull().orEmpty().take(40)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.save(existingNoteId, finalTitle, content, category)
            Toast.makeText(requireContext(), R.string.note_saved, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun showDiscardDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.discard_changes_title)
            .setMessage(R.string.discard_changes_message)
            .setPositiveButton(R.string.discard) { _, _ ->
                backCallback.isEnabled = false
                findNavController().popBackStack()
            }
            .setNegativeButton(R.string.keep_editing, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_NOTE_ID = "noteId"
        const val ARG_EXTRACTED_TEXT = "extractedText"
    }
}
