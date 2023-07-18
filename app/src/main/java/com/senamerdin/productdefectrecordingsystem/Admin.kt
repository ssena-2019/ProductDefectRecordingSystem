package com.senamerdin.productdefectrecordingsystem

import android.content.Intent
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.senamerdin.productdefectrecordingsystem.databinding.ActivityAdminBinding
import com.senamerdin.productdefectrecordingsystem.databinding.ActivityRegisterBinding

class Admin : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var binding: ActivityAdminBinding
    private lateinit var db : FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        db = Firebase.database

        val currentUser = auth.currentUser
        val adminUserName = currentUser?.email.toString()

        val adminName = binding.textViewAdminName

        db.getReference("users").orderByChild("email").equalTo(adminUserName)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children

                    children.forEach {
                        val name = it.child("name").getValue<String>()
                        if(name != null){
                            adminName.text = "Admin Adı:  " + name
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


    }

    //çıkış yapmak için menü oluşturma
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.signout_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.sign_out) {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    //kullanıcı eklemek
    fun AddUser(view: View){
        val intent = Intent(this@Admin, Register::class.java)
        startActivity(intent)
    }

    fun DeleteUser(view: View){
        //Toast.makeText(applicationContext, "Lütfen silinecek kullanıcıya tıklayınız !", Toast.LENGTH_LONG).show()

        val intent = Intent(this@Admin, DeleteUser::class.java)
        startActivity(intent)

    }

    fun passToControllerSide(view: View){
        val intent = Intent(this@Admin, ControllerBarcodeScreen::class.java )
        startActivity(intent)
    }

    fun passToFixerSide(view: View){
        val intent = Intent(this@Admin, FixerBarcodeScreen::class.java )
        startActivity(intent)
    }
}