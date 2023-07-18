package com.senamerdin.productdefectrecordingsystem
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListedDefectAdapter(private val context: Context, private val defectList: ArrayList<Defect>, private val listener: Listener) :
    RecyclerView.Adapter<ListedDefectAdapter.DefectViewHolder>() {


    interface Listener{
        fun onItemClick1(defect: Defect)
        abstract fun onItemClick2(defect: Defect)
        abstract fun onButton1Click(defect: Defect)
    }

    class DefectViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val txtName: TextView = itemView.findViewById(R.id.textViewGelenHata)

        fun bind(defect: Defect, listener: Listener) {
            itemView.setOnClickListener {
                listener.onItemClick1(defect)
            }

            txtName.text = defect.name
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefectViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recyclerview_alldefectlist, parent, false)
        return DefectViewHolder(view)
    }

    override fun onBindViewHolder(holder: DefectViewHolder, position: Int) {
        val defect = defectList[position]
        holder.bind(defect, listener)
    }

    override fun getItemCount(): Int {
        return defectList.size
    }

    fun addItem(defect: Defect) {
        defectList.add(defect)
        notifyItemInserted(defectList.size - 1)
    }

    /*inner class DefectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.textViewGelenHata)

        fun bind(defect: Defect) {
            txtName.text = defect.name
        }
    }*/

}
