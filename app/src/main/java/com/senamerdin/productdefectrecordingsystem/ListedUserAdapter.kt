package com.senamerdin.productdefectrecordingsystem

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListedUserAdapter(
    private val context: Context,
    private val userList: List<User>,
    private val listener: Listener
) : RecyclerView.Adapter<ListedUserAdapter.UserViewHolder>() {

    interface Listener {
        fun onItemClick(user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.recyclerview_deleteuser, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user, listener)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.textViewGelenKullanıcı)

        fun bind(user: User, listener: Listener) {
            itemView.setOnClickListener {
                listener.onItemClick(user)
            }

            usernameTextView.text = user.name
        }
    }
}
