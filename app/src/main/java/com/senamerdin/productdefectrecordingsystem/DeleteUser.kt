package com.senamerdin.productdefectrecordingsystem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.senamerdin.productdefectrecordingsystem.databinding.ActivityDeleteUserBinding

class DeleteUser : AppCompatActivity(), ListedUserAdapter.Listener {

    private lateinit var binding: ActivityDeleteUserBinding
    private lateinit var ref: DatabaseReference
    private lateinit var usersList: ArrayList<User>

    private lateinit var userListRecyclerView: RecyclerView

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: ListedUserAdapter

    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteUserBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ref = FirebaseDatabase.getInstance().getReference("users")

        userListRecyclerView = binding.userListRecyclerView

        layoutManager = LinearLayoutManager(this)
        userListRecyclerView.layoutManager = layoutManager
        usersList = arrayListOf()

        adapter = ListedUserAdapter(this, usersList, this)
        userListRecyclerView.adapter = adapter

        currentUser = FirebaseAuth.getInstance().currentUser!!

        retrieveUsers()

        Toast.makeText(applicationContext, "Silinecek kullanıcıya tıklayınız!", Toast.LENGTH_LONG)
            .show()

    }

    //veritababından kullanıcıları alıp recyclerview üzerine yazar.
    fun retrieveUsers() {

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()

                for (dataSnapshot in snapshot.children) {
                    val userHashMap = dataSnapshot.value as? HashMap<String, Any>
                    userHashMap?.let { userHashMap ->
                        val user = convertHashMapToDefect(userHashMap)
                        usersList.add(user)
                    }
                }

                //defectRecyclerView.layoutManager = layoutManager
                adapter = ListedUserAdapter(applicationContext, usersList, this@DeleteUser)
                userListRecyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    //Bu fonksiyon, bir HashMap<String, Any> nesnesini kullanarak bir "User" nesnesi oluşturur.
    fun convertHashMapToDefect(hashMap: HashMap<String, Any>): User {
        val email = hashMap["email"] as? String
        val name = hashMap["name"] as? String
        val password = hashMap["password"] as? String
        val userType = hashMap["userType"] as? String

        return User(name ?: "", email ?: "", password ?: "", userType ?: "")
    }

    override fun onItemClick(user: User) {
        val selectedUserRef = ref.orderByChild("email").equalTo(user.email)
        val selectedUserMail = user.email
        val selectedUserPassword = user.password

        selectedUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    userSnapshot.ref.removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "Kullanıcı Silindi...", Toast.LENGTH_LONG).show()

                            val user = Firebase.auth.currentUser!!
                            val credential = EmailAuthProvider
                                .getCredential(selectedUserMail!!, selectedUserPassword!!)

                            user.reauthenticate(credential)
                                .addOnCompleteListener {
                                    Toast.makeText(applicationContext, "re-authenticaded", Toast.LENGTH_LONG).show()}

                            user.delete()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(applicationContext, "account deleted", Toast.LENGTH_LONG).show()
                                    }
                                }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                applicationContext,
                                "Kullanıcı silinirken bir hata oluştu: ${exception.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    break // Sadece tıklanan kullanıcıyı silmek için döngüyü sonlandır
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    applicationContext,
                    "Veritabanı hatası: ${databaseError.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }



}




