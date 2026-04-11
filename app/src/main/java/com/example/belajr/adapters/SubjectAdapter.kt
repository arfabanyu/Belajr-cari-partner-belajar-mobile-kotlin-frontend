package com.example.belajr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.belajr.R

data class Subject(val name: String, val icon: Int)

class SubjectAdapter(
    private val subjects: List<Subject>,
    private val onSubjectClick: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    class SubjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvSubjectName)
        val ivIcon: ImageView = view.findViewById(R.id.ivSubjectIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjects[position]
        holder.tvName.text = subject.name
        holder.ivIcon.setImageResource(subject.icon)
        holder.itemView.setOnClickListener { onSubjectClick(subject) }
    }

    override fun getItemCount() = subjects.size
}