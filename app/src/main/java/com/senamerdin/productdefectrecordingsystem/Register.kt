package com.senamerdin.productdefectrecordingsystem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.senamerdin.productdefectrecordingsystem.databinding.ActivityRegisterBinding

class Register : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var ref: DatabaseReference
    private lateinit var ref2: DatabaseReference

    private lateinit var binding : ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        db = Firebase.database

        ref = FirebaseDatabase.getInstance().getReference("users")
        ref2 = FirebaseDatabase.getInstance().getReference("products")

        val currentUserMail = auth.currentUser?.email

        Log.i("current_user", currentUserMail.toString())

        /*ref
            //.orderByChild("email")
            //.equalTo(currentUserMail)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.i("snapShot", snapshot.toString())
                    val cildren = snapshot!!.children

                    cildren.forEach {
                        //val email = it.child("email").getValue()
                        val name = it.child("name").getValue<String>()
                        val email = it.child("email").getValue<String>()
                        val key = it.key

                        if (key != null) {
                            Log.i("key", key)
                        }

                        if (email != null) {
                            Log.i("email", email)
                        }

                        if (name != null) {
                            Log.i("name", name)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })*/

        ref2.child("product1").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("snapshot2: ", snapshot.toString())
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }




    //kaydet butonuna bastıktan sonra kullanıcıyı kaydeder
    fun addUser(view: View) {
        val name = binding.editTextAd.text.toString().uppercase()
        val email = binding.editTextKullanCAd.text.toString()
        val userType = binding.editTextKullanCTur.text.toString().lowercase()
        val password = binding.editTextSifre.text.toString()

        if(name.isNotEmpty() && email.isNotEmpty() && userType.isNotEmpty() && password.isNotEmpty()){
            //kullanıcı veritabanında oluşturulur
            val newUser2 = User(name, email, password, userType)
            db.getReference("users").push().setValue(newUser2)

            //kullanıcı authentication'da oluşturulur
            auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                Toast.makeText(applicationContext, "yeni kullanıcı oluşturuldu...", Toast.LENGTH_LONG).show()
                val intent = Intent(this@Register, Admin::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener {
                Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }else{
            Toast.makeText(applicationContext, "eksik bilgi !!!", Toast.LENGTH_LONG).show()
        }

    }

}