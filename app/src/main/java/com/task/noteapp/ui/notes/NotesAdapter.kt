package com.task.noteapp.ui.notes

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.task.noteapp.R
import com.task.noteapp.data.Note
import com.task.noteapp.databinding.NotesItemBinding

class NotesAdapter(private val listener: OnItemClickListener) :
    ListAdapter<Note, NotesAdapter.NotesViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val binding = NotesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        val currentNote = getItem(position)
        holder.bind(currentNote)
    }

    inner class NotesViewHolder(private val binding: NotesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val note = getItem(position)
                        listener.onItemClick(note)
                    }
                }

                noteCompleteButton.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val note = getItem(position)
                        listener.onButtonClicked(note)
                    }
                }
            }
        }

        fun bind(note: Note) {
            binding.apply {
                noteTitle.text = note.title
                noteDescription.text = note.description
                if (note.completed) {
                    noteCompleteButton.isInvisible = true
                }

                noteEditedIcon.isGone = note.updated == note.created

                noteCreatedLabel.text = "At: ${note.formattedCreatedDate}"

                noteImage.isVisible = !TextUtils.isEmpty(note.imageUrl)
                if (note.imageUrl.isNotEmpty()) {
                    Glide.with(noteImage)
                        .load(note.imageUrl)
                        .fitCenter()
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_warning)
                        .fallback(R.drawable.ic_fallback)
                        .into(noteImage)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(note: Note)
        fun onButtonClicked(note: Note)
    }

    class DiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }


}