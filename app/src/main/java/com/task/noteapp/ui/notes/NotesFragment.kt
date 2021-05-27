package com.task.noteapp.ui.notes

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.task.noteapp.R
import com.task.noteapp.data.Note
import com.task.noteapp.data.SortPriority
import com.task.noteapp.databinding.FragmentNotesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotesFragment : Fragment(R.layout.fragment_notes), NotesAdapter.OnItemClickListener {

    private val viewModel: NotesViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNotesBinding.bind(view)

        val notesAdapter = NotesAdapter(this)

        binding.apply {
            notesRecyclerView.apply {
                adapter = notesAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)

            }


            ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val note = notesAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onNoteSwiped(note)
                }
            }).attachToRecyclerView(notesRecyclerView)
        }

        binding.addNotesFab.setOnClickListener {
            viewModel.onAddNewNoteClick()
        }

        viewModel.notes.observe(viewLifecycleOwner) { notes: List<Note> ->
            if (notes.isEmpty()) {
                binding.emptyListLayout.isVisible = true
                binding.notesRecyclerView.isVisible = false
            } else {
                binding.emptyListLayout.isVisible = false
                binding.notesRecyclerView.isVisible = true
                notesAdapter.submitList(notes)
                notesAdapter.notifyDataSetChanged()
            }

//            notesAdapter.submitList(notes)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.notesEvent.collect { event ->
                when (event) {
                    is NotesViewModel.NotesEvent.ShowDeleteCompleteMessage -> {
                        Snackbar.make(
                            requireView(),
                            "Note Deleted",
                            Snackbar.LENGTH_LONG
                        )
                            .setAction("UNDO") {
                                viewModel.undoDeleteClick(event.note)
                            }.show()
                    }

                    is NotesViewModel.NotesEvent.ShowUpdateCompleteMessage -> {
                        Snackbar.make(
                            requireView(),
                            "Note Completed",
                            Snackbar.LENGTH_LONG
                        )
                            .setAction("UNDO") {
                                viewModel.onNoteUpdated(event.note)
                            }.show()
                    }

                    is NotesViewModel.NotesEvent.NavigateToAddNoteScreen -> {
                        val action =
                            NotesFragmentDirections.actionNotesFragmentToAddEditNoteFragment(null)
                        findNavController().navigate(action)
                    }

                    is NotesViewModel.NotesEvent.NavigateToNoteEditScreen -> {
                        val action =
                            NotesFragmentDirections.actionNotesFragmentToAddEditNoteFragment(note = event.note)
                        findNavController().navigate(action)
                    }

                    is NotesViewModel.NotesEvent.NavigateToDeleteAllCompletedScreen -> {
                        val action =
                            NotesFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                        findNavController().navigate(action)
                    }
                }
            }
        }

        setHasOptionsMenu(true)
    }


    override fun onItemClick(note: Note) {
        viewModel.onNoteSelected(note)
    }

    override fun onButtonClicked(note: Note) {
        viewModel.onNoteCompleted(note)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.notes_fragment_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_field_hint)

        // handle search query here
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchString.value = newText.toString()
                return true
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_notes).isChecked =
                viewModel.selectionFlow.first().hideCompleted

            when (viewModel.selectionFlow.first().sortPriority) {
                SortPriority.BY_TITLE -> {
                    menu.findItem(R.id.action_filter_by_title).isChecked = true
                }
                SortPriority.BY_CREATED_DATE -> {
                    menu.findItem(R.id.action_filter_by_creation_date).isChecked = true
                }
                SortPriority.BY_UPDATED_DATE -> {
                    menu.findItem(R.id.action_filter_by_last_modified_date).isChecked = true
                }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter_by_title -> {
                item.isChecked = !item.isChecked
                viewModel.onSortOrderSelected(SortPriority.BY_TITLE)
                true
            }
            R.id.action_filter_by_creation_date -> {
                item.isChecked = !item.isChecked
                viewModel.onSortOrderSelected(SortPriority.BY_CREATED_DATE)
                true
            }
            R.id.action_filter_by_last_modified_date -> {
                item.isChecked = !item.isChecked
                viewModel.onSortOrderSelected(SortPriority.BY_UPDATED_DATE)
                true
            }
            R.id.action_hide_completed_notes -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedClick(item.isChecked)
                true
            }
            R.id.action_delete_all_completed_notes -> {
                viewModel.deleteAllCompletedNotes()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}