package com.example.wacloneapp.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.wacloneapp.R
import com.example.wacloneapp.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private var imageUrl: String? = null
    private var partnerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setSupportActionBar(toolbar_profile)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_profile.setNavigationOnClickListener { onBackPressed() }

        if (userId.isNullOrEmpty()) {
            finish()
        }
//        partnerId = intent.getStringExtra(PARAM_OTHER_USER_ID)

        progress_layout.setOnTouchListener { v, event -> true }

        btn_apply.setOnClickListener {
            onApply()
        }

        btn_delete_account.setOnClickListener {
            onDelete()
        }

        imbtn_profile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PHOTO)
        }

        if (partnerId != null) {
            imbtn_profile.visibility = View.GONE
            btn_apply.visibility = View.GONE
            btn_delete_account.visibility = View.GONE
            populateInfo(partnerId)
        } else {
            populateInfo(userId.toString())
        }
    }

    private fun populateInfo(idProfile: String?) {
        progress_layout.visibility = View.VISIBLE
        firebaseDb.collection(DATA_USERS).document(idProfile!!).get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                imageUrl = user?.imageUrl
                edt_name_profile.setText(user?.name, TextView.BufferType.EDITABLE)
                
                edt_phone_profile.setText(user?.phone, TextView.BufferType.EDITABLE)
                edt_status_profile.setText(user?.status, TextView.BufferType.EDITABLE)
                if (imageUrl != null) {
                    populateImage(
                        this, user?.imageUrl, img_profile,
                        R.drawable.ic_user
                    )
                }
                progress_layout.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                finish()
            }
    }

    fun onApply() {
        progress_layout.visibility = View.VISIBLE
        val name = edt_name_profile.text.toString()
        val phone = edt_phone_profile.text.toString()
        val status = edt_status_profile.text.toString()
        val map = HashMap<String, Any>()
        map[DATA_USER_NAME] = name
        map[DATA_USER_PHONE] = phone
        map[DATA_USER_STATUS] = status

        firebaseDb.collection(DATA_USERS).document(userId!!).update(map)
            .addOnSuccessListener {
                Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
                progress_layout.visibility = View.GONE
            }
    }

    private fun onDelete() {
        progress_layout.visibility = View.VISIBLE
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("This will delete your Profile Information. Are you sure?")
            .setPositiveButton("Yes") { dialog, which ->
                firebaseDb.collection(DATA_USERS).document(userId!!).delete()
                firebaseStorage.child(DATA_IMAGES).child(userId).delete()
                firebaseAuth.currentUser?.delete()
                    ?.addOnSuccessListener {
                        finish()
                    }
                    ?.addOnFailureListener {
                        finish()
                    }
                progress_layout.visibility = View.GONE
                Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, which ->
                progress_layout.visibility = View.GONE
            }
            .setCancelable(false)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            storeImage(data?.data)
        }
    }

    private fun storeImage(uri: Uri?) {
        if (uri != null) {
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()
            progress_layout.visibility = View.VISIBLE
            val filePath = firebaseStorage.child(DATA_IMAGES).child(userId!!)

            filePath.putFile(uri)
                .addOnSuccessListener {
                    filePath.downloadUrl
                        .addOnSuccessListener {
                            val url = it.toString()
                            firebaseDb.collection(DATA_USERS)
                                .document(userId)
                                .update(DATA_USER_IMAGE_URL, url)
                                .addOnSuccessListener {
                                    imageUrl = url
                                    populateImage(
                                        this, imageUrl, img_profile,
                                        R.drawable.ic_user
                                    )
                                }
                            progress_layout.visibility = View.GONE
                        }
                        .addOnFailureListener {
                            onUploadFailured()
                        }
                }
                .addOnFailureListener {
                    onUploadFailured()
                }
        }
    }

    private fun onUploadFailured() {
        Toast.makeText(this, "Image upload failed. Please try again later.", Toast.LENGTH_SHORT).show()
        progress_layout.visibility = View.GONE
    }
}
