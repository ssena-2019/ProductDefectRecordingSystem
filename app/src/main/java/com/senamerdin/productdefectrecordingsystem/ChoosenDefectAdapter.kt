package com.senamerdin.productdefectrecordingsystem

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChoosenDefectAdapter(private val context: Context,
                           private val choosenList: ArrayList<Defect>,
                           private val listener: ControllerMainScreen
) : RecyclerView.Adapter<ChoosenDefectAdapter.ChoosenDefectViewHolder>() {

    interface Listener{
        //fun onItemClick2(defect: Defect)
        fun onButton1Click(defect: Defect)
        fun onButton2Click(defect: Defect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoosenDefectViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recyclerview_secilenhata, parent, false)
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
        private val txtName: TextView = itemView.findViewById(R.id.textViewSecilenHata)
        val buttonDel: Button = itemView.findViewById(R.id.buttonDelDefect)
        val buttonPhoto: Button = itemView.findViewById(R.id.buttonPhoto)


        fun bind(defect: Defect, listener: ControllerMainScreen) {

            buttonDel.setOnClickListener {
                listener.onButton1Click(defect)
            }

            buttonPhoto.setOnClickListener {
                listener.onButton2Click(defect)
            }

            txtName.text = defect.name
        }

    }


    fun addItem(defect: Defect) {
        choosenList.add(defect)
        notifyItemInserted(choosenList.size - 1)
    }
} 