package com.senamerdin.productdefectrecordingsystem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.senamerdin.productdefectrecordingsystem.databinding.ActivityFixerMainScreenBinding


class FixerMainScreen : AppCompatActivity() {

    private lateinit var binding: ActivityFixerMainScreenBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private lateinit var refDefect: DatabaseReference
    private lateinit var refProduct: DatabaseReference

    private lateinit var recyclerViewFixer: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var fixerAdapter: ListedFixerAdapter

    var productControllerName: String = ""
    var productFixerName: String = ""
    var barcodeNumber: String = ""

    private lateinit var defectList: ArrayList<Defect>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFixerMainScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val intent = getIntent()
        val productBarcode = intent.getStringExtra("barcode")
        val productType = intent.getStringExtra("tur")
        val productModel = intent.getStringExtra("model")
        val productSerie = intent.getStringExtra("seriNo")
        val productSequence = intent.getStringExtra("siraNo")
        val productColor = intent.getStringExtra("renk")

        val textBarcode = binding.textViewBarcodeFix
        val textType = binding.textViewUrunTurFix
        val textModel = binding.textViewUrunModelFix
        val textSerie = binding.textViewUrunSeriFix
        val textSequence = binding.textViewUrunSiraFix
        val textColor = binding.textViewRenkFix
        val textController = binding.textViewControllerFix
        val textFixer = binding.textViewFixerFix

        textBarcode.text = "Barkod Numarası: $productBarcode"
        textType.text = "Tür: $productType"
        textModel.text = "Model: $productModel"
        textSerie.text = "Seri No: $productSerie"
        textSequence.text = "Sıra No: $productSequence"
        textColor.text = "Renk: $productColor"

        barcodeNumber = productBarcode.toString()

        refUsers = FirebaseDatabase.getInstance().getReference("users")
        refProduct = FirebaseDatabase.getInstance().getReference("products")
        refDefect = FirebaseDatabase.getInstance().getReference("defects")

        auth = Firebase.auth
        val currentUser = auth.currentUser
        val currentUserMail = currentUser?.email.toString()

        //fixer ismini ayrıştırma ve ekrana yazdırma.
        refUsers.orderByChild("email").equalTo(currentUserMail)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children

                    children.forEach {
                        val fixerName = it.child("name").getValue<String>()
                        productFixerName = fixerName.toString()
                        textFixer.text = "Fixer Ad: ${productFixerName}"
                        refProduct.child(barcodeNumber).child("fixer").setValue(productFixerName)

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        //controller ismini ayrıştırma ve ekrana yazdırma.
        refProduct.child(barcodeNumber).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var controller = snapshot.child("controller").getValue<String>()
                productControllerName = controller.toString()
                //System.out.println("controller: " + productControllerName)
                textController.text = "Controller Ad: ${productControllerName}"
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        //recyclerview'ı layoutta görünür yapar
        recyclerViewFixer = binding.recyclerviewGelenHata
        layoutManager = LinearLayoutManager(this)
        recyclerViewFixer.layoutManager = layoutManager


        defectList = arrayListOf()

        fixerAdapter = ListedFixerAdapter(this, defectList, this@FixerMainScreen)
        recyclerViewFixer.adapter = fixerAdapter


        refProduct.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productSnapshot = snapshot.child(barcodeNumber)
                if (productSnapshot.exists() && productSnapshot.child("defectCode").hasChildren()) {
                    val defectCodesSnapshot = productSnapshot.child("defectCode")

                    defectList.clear()

                    for (defectSnapshot in defectCodesSnapshot.children) {
                        val defectCode = defectSnapshot.key
                        val defectName = defectSnapshot.child("name").getValue<String>()
                        val defectTur = defectSnapshot.child("tur").getValue<String>()
                        val defectFixed = defectSnapshot.child("fixed").getValue<String>()
                        val defectUrl = defectSnapshot.child("url").getValue<String>()
                        val defect = Defect(defectCode!!, defectName!!, defectTur!!, defectFixed!!, defectUrl!!)

                        if(defectFixed.equals("0")) {
                            defectList.add(defect)
                        }
                    }
                    fixerAdapter.notifyDataSetChanged()

                } else {
                    // Ürüne ait hata bulunamadı
                    defectList.clear()
                    fixerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // İşlem iptal edildiğinde yapılacaklar
            }
        })

    }

    //hata düzeltme tıklaması
    fun onButtonDelClick(defect: Defect) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Hata Silme")
            .setMessage("Hatayı düzeltmek istediğinize emin misiniz?")
            .setPositiveButton("Evet") { dialog, _ ->
                //Toast.makeText(applicationContext, "Silinmeye basıldı", Toast.LENGTH_LONG).show()

                val position = defectList.indexOf(defect)
                if (position != -1) {
                    // Öğeyi kaynak listeden kaldır
                    defectList.removeAt(position)
                    recyclerViewFixer.adapter?.notifyItemRemoved(position)
                }

                val productBarcode = barcodeNumber

                refProduct.child(productBarcode).child("defectCode")
                    .child(defect.defectCode).child("fixed").setValue("1")

            }
            .setNegativeButton("Hayır") { dialog, _ ->
                // Hiçbir işlem yapma
            }
            .create()

        alertDialog.show()
    }

    //hata fotoğraf görüntüleme
    fun onButtonPhotoClick(defect: Defect) {
        showPopoutImageView(defect.url)
    }

    private fun showPopoutImageView(imageUrl: String) {
        // Popout ImageView'in görüntüleneceği bir AlertDialog oluşturun
        val alertDialogBuilder = AlertDialog.Builder(this)

        // Popout ImageView'in görüntüleneceği View'ı oluşturun
        val imageViewLayout = LayoutInflater.from(this).inflate(R.layout.popout_image_layout, null)
        val popoutImageView: ImageView = imageViewLayout.findViewById(R.id.popoutImageView)

        // Resmi Glide, Picasso gibi bir kütüphane kullanarak yükleyin
        Glide.with(this)
            .load(imageUrl)
            .into(popoutImageView)

        // AlertDialogBuilder'a ImageView'i ekleyin
        alertDialogBuilder.setView(imageViewLayout)

        // AlertDialog'ı oluşturun ve gösterin
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }




}
