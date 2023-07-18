package com.senamerdin.productdefectrecordingsystem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.senamerdin.productdefectrecordingsystem.databinding.ActivityControllerMainScreenBinding

// Kamera kullanımı için gerekli kütüphaneleri içe aktarın
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class ControllerMainScreen : AppCompatActivity(), ListedDefectAdapter.Listener {

    private lateinit var binding: ActivityControllerMainScreenBinding

    private lateinit var defectRecyclerView: RecyclerView
    private lateinit var targetRecyclerView: RecyclerView

    private var selectedPosition: Int = -1

    private lateinit var defectList: ArrayList<Defect>
    private lateinit var choosenList: ArrayList<Defect>

    private lateinit var refDefect: DatabaseReference
    private lateinit var refProduct: DatabaseReference

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var targetLayoutManager: LinearLayoutManager
    private lateinit var adapter: ListedDefectAdapter
    private lateinit var targetAdapter: ChoosenDefectAdapter

    var urunTur: String = ""
    var barcodeNumber: String = ""

    var photoDefectCode: String = ""
    val storage = FirebaseStorage.getInstance()
    val storageReference = storage.reference

    // Veritabanı işlemleri için gerekli bağlantıları sağlayın
    val database = FirebaseDatabase.getInstance()
    //val reference = database.getReference("tablo_adi")

    // İstek kodu için bir sabit tanımlayın
    private val KAMERA_ISTEK_KODU = 100

    // Geçerli hata nesnesini depolamak için bir değişken tanımlayın
    private var suankiHata: Defect? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityControllerMainScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        refDefect = FirebaseDatabase.getInstance().getReference("defects")
        refProduct = FirebaseDatabase.getInstance().getReference("products")


        defectRecyclerView = binding.defectListRecyclerView
        targetRecyclerView = binding.recyclerviewTarget

        layoutManager = LinearLayoutManager(this)
        defectRecyclerView.layoutManager = layoutManager

        targetLayoutManager = LinearLayoutManager(this)
        targetRecyclerView.layoutManager = targetLayoutManager

        defectList = arrayListOf()
        choosenList = arrayListOf()


        targetAdapter = ChoosenDefectAdapter(this, choosenList, this@ControllerMainScreen)
        targetRecyclerView.adapter = targetAdapter

        adapter = ListedDefectAdapter(this, defectList, this@ControllerMainScreen)
        defectRecyclerView.adapter = adapter

        val textBarcode = binding.textViewBarcode
        val textType = binding.textViewTur
        val textModel = binding.textViewModel
        val textSerie = binding.textViewSeriNo
        val textSequence = binding.textViewSiraNo
        val textColor = binding.textViewRenk
        val textController = binding.textViewController

        val intent = getIntent()
        val productBarcode = intent.getStringExtra("barcode")
        val productType = intent.getStringExtra("tur")
        val productModel = intent.getStringExtra("model")
        val productSerieNo = intent.getStringExtra("seriNo")
        val productSequenceNo = intent.getStringExtra("siraNo")
        val productColor = intent.getStringExtra("renk")
        val productController = intent.getStringExtra("controller")

        urunTur = productType.toString()
        barcodeNumber = productBarcode.toString()

        textBarcode.text = "Barkod Numarası: $productBarcode"
        textType.text = "Tür: $productType"
        textModel.text = "Model: $productModel"
        textSerie.text = "Seri No: $productSerieNo"
        textSequence.text = "Sıra No: $productSequenceNo"
        textColor.text = "Renk: $productColor"
        textController.text = "Controller Adı: $productController"

        //önceki hataları getirir.
        refProduct.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productSnapshot = snapshot.child(barcodeNumber)
                if (productSnapshot.exists() && productSnapshot.child("defectCode").hasChildren()) {
                    val defectCodesSnapshot = productSnapshot.child("defectCode")

                    choosenList.clear()

                    for (defectSnapshot in defectCodesSnapshot.children) {
                        val defectCode = defectSnapshot.key
                        val defectName = defectSnapshot.child("name").getValue<String>()
                        val defectTur = defectSnapshot.child("tur").getValue<String>()
                        val fixed = defectSnapshot.child("fixed").getValue<String>()
                        val url = defectSnapshot.child("url").getValue<String>()

                        if(defectCode != null && defectName != null && defectTur != null && url != null) {
                            val defect = Defect(defectCode!!, defectName!!, defectTur!!, fixed!!, url!!)
                            if(fixed.equals("0")) {
                                choosenList.add(defect)
                            }
                        } else {
                            Toast.makeText(applicationContext, "hata var", Toast.LENGTH_LONG).show()
                        }
                    }

                    targetAdapter.notifyDataSetChanged()

                } else {
                    // Ürüne ait hata bulunamadı.
                    choosenList.clear()
                    targetAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

    override fun onItemClick1(defect: Defect) {

        val position = defectList.indexOf(defect)
        if (position != -1) {
            // Öğeyi hedef listeye ekle
            val selectedDefect = defectList[position]
            choosenList.add(selectedDefect)
            System.out.println(defect.toString())
            refProduct.child(barcodeNumber).child("defectCode").child(defect.defectCode).setValue(defect)

            // Öğeyi kaynak listeden kaldır
            defectList.removeAt(position)

            // Adapterlara değişiklikleri bildir
            adapter.notifyDataSetChanged()
            targetAdapter.notifyDataSetChanged()
        }
    }

    override fun onItemClick2(defect: Defect) {
        TODO("Not yet implemented")
    }

    //eklenen hatayı silmek için method.
    override fun onButton1Click(defect: Defect) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Uyarı")
            .setMessage("Hatayı geri almak istediğinize emin misiniz?")
            .setPositiveButton("Evet") { dialog, _ ->
                //Toast.makeText(applicationContext, "silinmeye basıldı", Toast.LENGTH_LONG).show()
                val position = choosenList.indexOf(defect)
                if (position != -1) {
                    // Öğeyi hedef listeye ekle
                    adapter.addItem(defect)

                    // Öğeyi kaynak listeden kaldır
                    choosenList.removeAt(position)
                    targetRecyclerView.adapter?.notifyItemRemoved(position)
                }

                val productBarcode = barcodeNumber

                refProduct.child(productBarcode).child("defectCode").addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.childrenCount > 1){
                            refProduct.child(productBarcode).child("defectCode").child(defect.defectCode).removeValue()
                        }
                        else{
                            refProduct.child(productBarcode).child("defectCode").setValue("0")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Hata durumunda yapılacak işlemler
                    }
                })
            }
            .setNegativeButton("Hayır") { dialog, _ ->
                // Hiçbir işlem yapma
            }
            .create()

        alertDialog.show()
    }

    //fotoğraf çekme işlemini çalıştırır.
    fun onButton2Click(defect: Defect) {
        photoDefectCode = defect.defectCode
        dispatchTakePictureIntent()
    }

    //hataları getir butonuna tıklandığında ürünün türüne göre hataları getirir.
    fun getDatasButton(view: View){
        getDefectDatas(urunTur)
    }

    //eklenen ürünü siler.
    fun UrunSilButton(view: View){

        refProduct.orderByChild("barcode").equalTo(barcodeNumber).addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(productSnapshot in snapshot.children){
                    val photoPath = "fotograflar/" + productSnapshot.key
                    System.out.println(photoPath)
                    val photoRef = storageReference.child(photoPath) // Silinecek fotoğrafın depolama yolunu belirtin

                    photoRef.listAll().addOnSuccessListener { listResult ->
                        val deleteTasks = mutableListOf<Task<Void>>()
                        for (fileReference in listResult.items) {
                            val deleteTask = fileReference.delete()
                            deleteTasks.add(deleteTask)
                        }
                        Tasks.whenAllComplete(deleteTasks).addOnCompleteListener {
                            photoRef.delete().addOnSuccessListener {
                                // Klasör ve içindeki tüm dosyalar başarıyla silindi
                            }.addOnFailureListener { exception ->
                                // Klasör silinirken bir hata oluştu
                            }
                        }
                    }.addOnFailureListener { exception ->
                        // Dosyaları listeleme işleminde bir hata oluştu
                    }

                    productSnapshot.ref.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumunda yapılacak işlemler
            }
        })

        Toast.makeText(applicationContext, "Ürün Silindi...", Toast.LENGTH_LONG).show()

        val intent = Intent(this, ControllerBarcodeScreen::class.java)
        startActivity(intent)
        finish()
    }

    //ürün ve varsa hataları onaylanır, ana ekrana döner.
    fun UrunOkButton(view: View){
        Toast.makeText(applicationContext, "Ürün eklendi...", Toast.LENGTH_LONG).show()

        val intent = Intent(this, ControllerBarcodeScreen::class.java)
        startActivity(intent)
        finish()
    }

    //hataları recyclerview'a getirir.
    fun getDefectDatas(tur: String) {
        defectList.clear()
        refDefect.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (dataSnapshot in snapshot.children) {
                    val defectHashMap = dataSnapshot.value as? HashMap<String, Any>
                    defectHashMap?.let { defectHashMap ->
                        val defect = convertHashMapToDefect(defectHashMap)

                        if(defect.tur == tur) {
                            //Toast.makeText(applicationContext, "yuzey"+urunYuzey, Toast.LENGTH_LONG).show()
                            defectList.add(defect)
                        }

                    }
                }

                //defectRecyclerView.layoutManager = layoutManager
                adapter = ListedDefectAdapter(applicationContext, defectList, this@ControllerMainScreen)
                defectRecyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

    //Bu fonksiyon, bir HashMap<String, Any> nesnesini kullanarak bir "Defect" nesnesi oluşturur.
    fun convertHashMapToDefect(hashMap: HashMap<String, Any>): Defect {
        //System.out.println(hashMap)
        val defectCode = hashMap["defectCode"] as? String
        val name = hashMap["name"] as? String
        val tur = hashMap["tur"] as? String
        val fixed = hashMap["fixed"] as? String
        val url = hashMap["url"] as? String

        return Defect(defectCode ?: "", name ?: "", tur ?: "", fixed?: "", url?: "")
    }

    // Kamera etkinliğini başlatmak ve fotoğrafı çekmek için metod
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, KAMERA_ISTEK_KODU)
        }
    }

    // Kamera etkinliğinin sonucunu yöneten metod
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == KAMERA_ISTEK_KODU && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            // Çekilen fotoğrafı veritabanına kaydetme
            saveImageToDatabase(imageBitmap)
        }
    }

    // Çekilen fotoğrafı veritabanına kaydetmek için metod
    private fun saveImageToDatabase(imageBitmap: Bitmap) {
        val imageData = convertImageToByteArray(imageBitmap)
        // Fotoğrafı belirli bir depolama alanına yükleyin
        val photoName = barcodeNumber + "/" + photoDefectCode + ".jpg"
        System.out.println(photoName)
        System.out.println(photoDefectCode)
        val photoRef = storageReference.child("fotograflar").child(photoName) // Depolama yolu ve dosya adı belirleyin
        val uploadTask = photoRef.putBytes(imageData) // imageData, fotoğrafın binary verisini temsil eder

        // Yükleme işlemi tamamlandığında, fotoğrafın Storage URL'sini alın
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            photoRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val photoUrl = task.result.toString()

                // Fotoğraf URL'sini veritabanına kaydedin
                if(photoDefectCode != "") {
                    refProduct.child(barcodeNumber).child("defectCode").child(photoDefectCode).child("url").setValue(photoUrl)
                } else {
                    Toast.makeText(applicationContext, "yazdırma hatası", Toast.LENGTH_LONG).show()
                }

            } else {
                // Yükleme veya URL alma işleminde bir hata oluştu
            }
        }
    }



    fun convertImageToByteArray(image: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }




}
