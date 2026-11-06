package com.example.notesnap.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.notesnap.R
import com.example.notesnap.data.ServiceLocator
import com.example.notesnap.databinding.FragmentNoteDetailBinding
import com.example.notesnap.ui.editor.NoteEditorFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

class NoteDetailFragment : Fragment() {

    private var _binding: FragmentNoteDetailBinding? = null
    private val binding get() = _binding!!

    private var noteId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteId = arguments?.getLong(ARG_NOTE_ID) ?: 0L
        setupMenu()
    }

    override fun onResume() {
        super.onResume()
        loadNote()
    }

    private fun loadNote() {
        viewLifecycleOwner.lifecycleScope.launch {
            val note = ServiceLocator.noteRepository.getNote(noteId)
            if (_binding == null) return@launch

            if (note == null) {
                findNavController().popBackStack()
                return@launch
            }

            binding.textDetailTitle.text = note.title
            binding.textDetailContent.text = note.content
            binding.textDetailContent.isVisible = note.content.isNotBlank()
            binding.textDetailEmpty.isVisible = note.content.isBlank()

            val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            binding.textDetailDate.text = if (note.updatedAt != note.createdAt) {
                getString(R.string.updated_at, dateFormat.format(Date(note.updatedAt)))
            } else {
                getString(R.string.created_at, dateFormat.format(Date(note.createdAt)))
            }

            (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title =
                note.title
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_note_detail, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
                R.id.action_edit -> {
                    val bundle = Bundle().apply { putLong(NoteEditorFragment.ARG_NOTE_ID, noteId) }
                    findNavController().navigate(R.id.action_detail_to_editor, bundle)
                    true
                }
                R.id.action_delete -> {
                    confirmDelete()
                    true
                }
                else -> false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun confirmDelete() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_note_title)
            .setMessage(R.string.delete_note_message)
            .setPositiveButton(R.string.delete) { _, _ -> deleteNote() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteNote() {
        viewLifecycleOwner.lifecycleScope.launch {
            val note = ServiceLocator.noteRepository.getNote(noteId) ?: return@launch
            ServiceLocator.noteRepository.deleteNote(note)
            Toast.makeText(requireContext(), R.string.note_deleted, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_NOTE_ID = "noteId"
    }
}
