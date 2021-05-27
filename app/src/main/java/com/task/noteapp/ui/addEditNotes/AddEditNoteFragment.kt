package com.task.noteapp.ui.addEditNotes

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.task.noteapp.R
import com.task.noteapp.databinding.FragmentAddEditNoteBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditNoteFragment : Fragment(R.layout.fragment_add_edit_note) {

    private val viewModel: AddEditViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditNoteBinding.bind(view)

        binding.apply {
            editTextNoteTitle.setText(viewModel.noteTitle)
            editTextNoteDescription.setText(viewModel.noteDescription)
            editTextImageString.setText(viewModel.noteImageUrl)

            if (viewModel.noteImageUrl.isNotEmpty()) {
                noteImage.isVisible = true
                Glide.with(noteImage)
                    .load(viewModel.noteImageUrl)
                    .fitCenter()
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_warning)
                    .fallback(R.drawable.ic_fallback)
                    .into(noteImage)
            }


            showKeyboardWithView(editTextNoteTitle)


            editTextNoteTitle.addTextChangedListener {
                viewModel.noteTitle = it.toString()
            }

            editTextNoteDescription.addTextChangedListener {
                viewModel.noteDescription = it.toString()
            }

            editTextImageString.addTextChangedListener {
                viewModel.noteImageUrl = it.toString()
            }


            fabNoteSave.setOnClickListener {
                viewModel.onSaveNote()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditNotesEvent.collect { event ->
                val action =
                    AddEditNoteFragmentDirections.actionAddEditNoteFragmentToNotesFragment()
                findNavController().navigate(action)

                findNavController().popBackStack()

                hideKeyboard()

                val toastMessage: String = getToastMessage(event)

                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
            }
        }

        setHasOptionsMenu(true)
    }

    private fun getToastMessage(event: AddEditViewModel.AddEditNotesEvent): String {
        return when (event) {
            is AddEditViewModel.AddEditNotesEvent.NavigateToNotesScreenOnDelete -> {
                "Note deleted"
            }
            AddEditViewModel.AddEditNotesEvent.NavigateToNotesScreenOnCreate -> {
                "Note created"
            }
            AddEditViewModel.AddEditNotesEvent.NavigateToNotesScreenOnUpdate -> {
                "Note updated"
            }
        }
    }

    private fun getInputManager(): InputMethodManager =
        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    private fun showKeyboardWithView(view: View) {
        view.requestFocus()
        val imm = getInputManager()
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val inputMethodManager = getInputManager()

        val currentFocusedView = requireActivity().currentFocus
        currentFocusedView?.let {
            inputMethodManager.hideSoftInputFromWindow(
                currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        return inflater.inflate(R.menu.add_edit_note_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share_note -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                    .setData(Uri.parse("mailto:"))
                    .putExtra(Intent.EXTRA_EMAIL, arrayOf("ikiugualf@gmail.com"))
                    .putExtra(Intent.EXTRA_SUBJECT, "I would like to work with you.")
                    .putExtra(Intent.EXTRA_TEXT, "Hi Alfred, I hope you are doing well.")
                startActivity(intent)
                true
            }
            R.id.action_delete_note -> {
                viewModel.note?.id?.let { viewModel.onDeleteNote(it) }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}