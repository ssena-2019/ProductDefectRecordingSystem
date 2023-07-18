package com.senamerdin.productdefectrecordingsystem

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.senamerdin.productdefectrecordingsystem.databinding.ActivityControllerBarcodeScreenBinding
import com.senamerdin.productdefectrecordingsystem.databinding.ActivityFixerBarcodeScreenBinding

class FixerBarcodeScreen : AppCompatActivity() {

    //for scanner code
    var scannedResult: String = ""
    lateinit var receivedBarcode : ArrayList<String>
    var productModel : String = ""
    var productType : String = ""
    var productFixerName : String = ""
    var productSerieNumber : String = ""
    var productSequenceNumber : String = ""
    var productColorCode : String = ""

    var barcodeInfo : String = ""

    private lateinit var binding: ActivityFixerBarcodeScreenBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var db: FirebaseDatabase
    private lateinit var refUsers : DatabaseReference
    private lateinit var refProducts : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFixerBarcodeScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        refProducts = FirebaseDatabase.getInstance().getReference("products")

        auth = Firebase.auth

        //açılışta bilgi girme layoutunu gizler
        val linearLayoutBarcodeInfoFix = findViewById<LinearLayout>(R.id.linearLayoutBarcodeInfoFix)
        linearLayoutBarcodeInfoFix.visibility = View.INVISIBLE


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

    //barkod bilgisi girme layoutunu görünür yapma
    fun EnterBarcodeInfoFix(view: View){
        val linearLayoutBarcodeInfoFix = findViewById<LinearLayout>(R.id.linearLayoutBarcodeInfoFix)
        linearLayoutBarcodeInfoFix.visibility = View.VISIBLE
    }

    fun manuelBarcodeFix(view: View){
        barcodeInfo = binding.editTextBarcodeFix.text.toString()
        scannedResult = barcodeInfo

        refProducts.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.hasChild(scannedResult)){
                    barcodeEliminationAdd(scannedResult)
                }
                else{
                    Toast.makeText(applicationContext, "ürün henüz işlenmedi...", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    fun ScanBarcodeFix(view: View){
        run {
            IntentIntegrator(this@FixerBarcodeScreen).initiateScan();
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                scannedResult = result.contents

                refProducts.addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.hasChild(scannedResult)){
                            barcodeEliminationAdd(scannedResult)
                        }
                        else{
                            Toast.makeText(applicationContext, "ürün henüz işlenmedi...", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            } else {
                // Okutma işlemi başarısız veya iptal edildi
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("scannedResult", scannedResult)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        savedInstanceState.let {
            scannedResult = it.getString("scannedResult").toString()
        }
    }

    //barkodu ayrıştıran method
    fun barcodeEliminationAdd(scannedResult: String) {

        receivedBarcode = scannedResult.split("-") as ArrayList<String>
        productType = receivedBarcode.get(0)
        productModel = receivedBarcode.get(1)

        //seri numarasını int olarak kullanıyoruz
        productSerieNumber = receivedBarcode.get(2).toInt().toString()

        //serideki sıra numarasını int olarak kullanıyoruz
        productSequenceNumber = receivedBarcode.get(3).toInt().toString()

        productColorCode = receivedBarcode.get(4)


        //mobilya adlandırmadaki şartları belirleme
        if (productType.equals("S")) {
            productType = "Koltuk"

            if (productModel.equals("001")) {
                productModel = "Tekli"
            }
            if (productModel.equals("002")) {
                productModel = "İkili"
            }
            if (productModel.equals("003")) {
                productModel = "Üçlü"
            }
        }
        if (productType.equals("B")) {
            productType = "Kitaplık"

            if (productModel.equals("000")) {
                productModel = "Kapaksız"
            }
            if (productModel.equals("001")) {
                productModel = "Kapaklı"
            }
        }
        if (productType.equals("T")) {
            productType = "Sehpa"

            if (productModel.equals("000")) {
                productModel = "Cam"
            }
            if (productModel.equals("001")) {
                productModel = "Tahta"
            }
        }

        if (productColorCode == "93389") {
            productColorCode = "Bej"
        }
        if (productColorCode == "82873") {
            productColorCode = "Kiremit"
        }
        if (productColorCode == "19389") {
            productColorCode = "Beyaz"
        }


        //TODO("sonraki sayfaya intent ile bilgi gönderme")
        val intent = Intent(applicationContext, FixerMainScreen::class.java)
        intent.putExtra("barcode", scannedResult)
        intent.putExtra("tur", productType)
        intent.putExtra("model", productModel)
        intent.putExtra("seriNo", productSerieNumber)
        intent.putExtra("siraNo", productSequenceNumber)
        intent.putExtra("renk", productColorCode)
        startActivity(intent)

    }

}