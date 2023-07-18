package com.senamerdin.productdefectrecordingsystem

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListedFixerAdapter(private val context: Context, private val choosenList: ArrayList<Defect>, private val listener: FixerMainScreen) :
    RecyclerView.Adapter<ListedFixerAdapter.ChoosenDefectViewHolder>() {

    interface Listener{
        //fun onItemClick(defect: Defect)
        fun onButtonDelClick(defect: Defect)
        fun onButtonPhotoClick(defect: Defect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoosenDefectViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recyclerview_fixer, parent, false)
        return ChoosenDefectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChoosenDefectViewHolder, position: Int) {
        val defect = choosenList[position]
        holder.bind(defect, listener)
    }

    override fun getItemCount(): Int {
        return choosenList.size
    }

    inner class ChoosenDefectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.textViewHataAdi)
        private val buttonDel: Button = itemView.findViewById(R.id.buttonDelFix)
        private val buttonPhoto:Button = itemView.findViewById(R.id.buttonPhotoFix)

        fun bind(defect: Defect, listener: FixerMainScreen) {

            buttonDel.setOnClickListener {
                listener.onButtonDelClick(defect)
            }

            buttonPhoto.setOnClickListener {
                listener.onButtonPhotoClick(defect)
            }


            txtName.text = defect.name
        }

    }

    fun addItem(defect: Defect) {
        choosenList.add(defect)
        notifyItemInserted(choosenList.size - 1)
    }
}

