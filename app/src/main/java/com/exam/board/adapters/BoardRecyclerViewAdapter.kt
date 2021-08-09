package com.exam.board.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.exam.board.databinding.BoardItemBinding
import com.exam.board.entities.State

/**
 * [RecyclerView.Adapter] for showing board
 */
class BoardRecyclerViewAdapter(val listener: ItemClickListener): RecyclerView.Adapter<BoardRecyclerViewAdapter.ViewHolder>() {
    private val states = Array(16) { State.BLANK }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BoardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(states[position])
    }

    override fun getItemCount() = 16

    fun getItem(position: Int): State = states[position]

    fun selectColumn(column: Int, isPlayerTurn: Boolean): Boolean {
        for (row in 3 downTo 0) {
            val pos = row*4 + column
            if(states[pos] == State.BLANK) {
                states[pos] = if(isPlayerTurn) State.PLAYER else State.COMPUTER
                notifyItemChanged(pos)
                return true
            }
        }

        return false
    }

    fun clearBoard() {
        for(i in states.indices) states[i] = State.BLANK
    }

    interface ItemClickListener {
        fun onItemClick(position: Int)
    }

    inner class ViewHolder(binding: BoardItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val circleView = binding.circle

        fun bind(state: State) {
            circleView.updateView(state)
            circleView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}