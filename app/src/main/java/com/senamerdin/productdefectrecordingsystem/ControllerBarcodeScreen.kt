package com.senamerdin.productdefectrecordingsystem

//barcode scanner
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.senamerdin.productdefectrecordingsystem.databinding.ActivityControllerBarcodeScreenBinding

class ControllerBarcodeScreen : AppCompatActivity() {

    //for scanner code
    var scannedResult: String = ""
    lateinit var receivedBarcode: ArrayList<String>
    var productModel: String = ""
    var productType: String = ""
    var productControllerName: String = ""
    var productSerieNumber: String = ""
    var productSequenceNumber: String = ""
    var productColorCode: String = ""
    var productDefectCode: String = "0"

    var barcodeInfo: String = ""

    private lateinit var sendlist: ArrayList<Defect>


    private lateinit var binding: ActivityControllerBarcodeScreenBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var refUsers: DatabaseReference
    private lateinit var refProducts: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityControllerBarcodeScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        val currentUser = auth.currentUser
        val currentUserMail = currentUser?.email.toString()

        db = Firebase.database
        refUsers = db.getReference("users")
        refProducts = db.getReference("products")

        sendlist = arrayListOf()

        //controller ismini ayrıştırma
        refUsers.orderByChild("email").equalTo(currentUserMail)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children

                    children.forEach {
                        val userName = it.child("name").getValue<String>()
                        productControllerName = userName.toString()
                        Log.i("product controller: ", productControllerName)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //Toast.makeText(applicationContext, "hata mesajı", Toast.LENGTH_LONG).show()
                }
            })


        val linearLayoutBarcodeInfoCon = findViewById<LinearLayout>(R.id.linearLayoutBarcodeInfoCon)
        linearLayoutBarcodeInfoCon.visibility = View.INVISIBLE

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

    //barcode bilgisinin girildiği laoyut görünür yapılır
    fun EnterBarcodeInfoCon(view: View) {
        val linearLayoutBarcodeInfoCon = findViewById<LinearLayout>(R.id.linearLayoutBarcodeInfoCon)
        linearLayoutBarcodeInfoCon.visibility = View.VISIBLE
    }

    //barkod bilgisi manuel giriş yapılır
    fun manuelBarcodeCon(view: View) {
        barcodeInfo = binding.editTextBarcode.text.toString()

        scannedResult = barcodeInfo

        refProducts.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild(scannedResult)) {
                    //sadece intent ile geçiş yap
                    barcodeEliminationAdd(scannedResult)

                } else {
                    barcodeEliminationAdd(scannedResult)
                    UrunEkle(scannedResult, productType, productModel, productControllerName,
                        productSerieNumber, productSequenceNumber, productColorCode,
                        "", productDefectCode)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    //barkodu çalıştırmak için fonk.
    fun ScanBarcodeCon(view: View) {
        run {
            IntentIntegrator(this@ControllerBarcodeScreen).initiateScan();
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                scannedResult = result.contents

                refProducts.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.hasChild(scannedResult)) {
                            //barkod okutulduktan sonra barkodu ayrıştırır.
                            barcodeEliminationAdd(scannedResult)

                        } else {
                            //eğer ürün yoksa barkod bilgilerini ayrıştırır ve ürün ekler.
                            barcodeEliminationAdd(scannedResult)
                            UrunEkle(scannedResult, productType, productModel, productControllerName,productSerieNumber,
                            productSequenceNumber, productColorCode,"",productDefectCode)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Sorgu iptal edildi veya hata oluştu
                    }
                })

            } else {
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
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
        val intent = Intent(applicationContext, ControllerMainScreen::class.java)
        intent.putExtra("barcode", scannedResult)
        intent.putExtra("tur", productType)
        intent.putExtra("model", productModel)
        intent.putExtra("controller", productControllerName)
        intent.putExtra("seriNo", productSerieNumber)
        intent.putExtra("siraNo", productSequenceNumber)
        intent.putExtra("renk", productColorCode)
        startActivity(intent)

    }

    fun UrunEkle(barcode:String,tur:String,model:String, controller:String,
                 seri:String,sira:String,renk:String,fixer:String,hataKod:String){

        val newProduct = Product(barcode, tur, model, controller, fixer, seri, sira, renk, hataKod)

        //yeni ürünü sisteme kaydetme
        refProducts.child(scannedResult).setValue(newProduct)

        //Toast.makeText(applicationContext, "ürün eklendi...", Toast.LENGTH_LONG).show()
    }

}