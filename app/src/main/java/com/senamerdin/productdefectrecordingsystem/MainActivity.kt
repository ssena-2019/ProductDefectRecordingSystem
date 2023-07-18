package com.senamerdin.productdefectrecordingsystem

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputBinding
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.senamerdin.productdefectrecordingsystem.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    //giriş sayfası

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseDatabase
    private lateinit var ref : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        db = Firebase.database
        ref = FirebaseDatabase.getInstance().getReference("users")

        val currentUser = auth.currentUser
        val currentEmail = currentUser?.email.toString()

        //kullanıcı daha önce oturum açmış ise
        if (currentUser != null) {

            ref.orderByChild("email").equalTo(currentEmail)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children

                        children.forEach {
                            val currentUserType = it.child("userType").getValue<String>()

                            if (currentUserType != null) {
                                if (currentUserType.equals("controller")) {
                                    val intent = Intent(
                                        this@MainActivity,
                                        ControllerBarcodeScreen::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                }
                                if (currentUserType.equals("fixer")) {
                                    val intent =
                                        Intent(this@MainActivity, FixerBarcodeScreen::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                if (currentUserType.equals("admin")) {
                                    val intent = Intent(this@MainActivity, Admin::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "kullanıcı türü bulunamadı!!!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

        }

    }

    fun girisYap(view: View){
        val email = binding.editTextUserName.text.toString()
        val password = binding.editTextPassword.text.toString()

        if(email.equals("") || password.equals("")){
            Toast.makeText(this, "lütfen kullanıcı adı veya şifre giriniz!!!", Toast.LENGTH_LONG).show()
        }else{
            auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {

                ref.orderByChild("email").equalTo(email).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children

                        children.forEach {
                            val currentUserType = it.child("userType").getValue<String>()

                            if(currentUserType != null){
                                if(currentUserType.equals("controller")){
                                    val intent = Intent(this@MainActivity, ControllerBarcodeScreen::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                if(currentUserType.equals("fixer")){
                                    val intent = Intent(this@MainActivity, FixerBarcodeScreen::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                if(currentUserType.equals("admin")){
                                    val intent = Intent(this@MainActivity, Admin::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }else{
                                Toast.makeText(applicationContext, "kullanıcı türü bulunamadı!!!", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

            }.addOnFailureListener{
                Toast.makeText(this@MainActivity, "hatalı kullanıcı adı veya şifre!!!", Toast.LENGTH_LONG).show()
            }
        }
    }
}