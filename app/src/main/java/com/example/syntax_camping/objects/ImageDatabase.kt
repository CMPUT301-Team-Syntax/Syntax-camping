package com.example.syntax_camping

import android.util.Log
import com.example.syntax_camping.dataClasses.StoredImage
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.android.gms.tasks.Tasks

//Author: Sorachat Chaichana
//Date: 2026-11-26
//Object class to handle getting image from Firebase Storage for admin's image browsing screen.
//Since firebase will take time to actually load and functions depend on things to load first,
//suspended functions is just to make it pause its execution until an operation finishes or something.
object ImageDatabase {

    private val storage = FirebaseStorage.getInstance(
        "gs://syntaxcamping.firebasestorage.app"
    )

    private val storageRef = storage.reference   //Reference to our storage folder.

    //Gets all images as a list of StoredImage objects from a specific category.
    suspend fun getImages(category: String): List<StoredImage> {

        val images = mutableListOf<StoredImage>()

        val folders = storageRef.child(category)
            .listAll().await().prefixes

        //Gets images from all folders, then create a StorageImage class object for every image.
        for (folder in folders) {
            val files = folder.listAll().await().items  //Wait till firebase storage retrieves all its folders.

            //For each file in the folders, get its Url (to display) and metadata (for info).
            for (file in files) {
                val url = file.downloadUrl.await().toString()
                val metadata = file.metadata.await()

                images.add(
                    StoredImage(
                        name = file.name,
                        downloadUrl = url,
                        storagePath = file.path,
                        parentId = folder.name,
                        category = category,
                        uploadedAt = metadata.creationTimeMillis
                    )
                )
            }
        }
        return images
    }

    //Gets all images from campsites and users folder
    suspend fun getAllImages(): List<StoredImage> {
        return getImages("campsites") + getImages("users")
    }

    //Retrieve the name of the owner/campsite depending on its catagory.
    suspend fun getImageOwnerName(image: StoredImage): String {

        val document = FirebaseFirestore.getInstance()
            .collection(image.category)
            .document(image.parentId)
            .get()
            .await()

        return when (image.category) {
            "campsites" -> document.getString("name") ?: "Unknown campsite."
            "users" -> document.getString("displayName") ?: "Unknown user."
            else -> "Unknown (neither campsite nor user)."
        }
    }

    //Function to delete image from Firebase storage (and Url links in firestore)
    suspend fun deleteImage(image: StoredImage) {
        val db = FirebaseFirestore.getInstance()
        val documentRef = db.collection(image.category)
            .document(image.parentId)

        val document = documentRef.get().await()

        //1. Remove image reference from Firestore.
        if (document.exists()) {
            when (image.category) {
                "campsites" -> {
                    documentRef.update("images",
                        FieldValue.arrayRemove(image.downloadUrl)
                    ).await()
                }

                "users" -> {
                    if (document.getString("profileImageUrl") == image.downloadUrl) {
                        documentRef.update("profileImageUrl", "").await()
                    }
                }
            }
        }

        //2. Delete the actual image file from Firebase storage.
        storageRef.child(image.storagePath).delete().await()
    }

    fun deleteImages(
        category: String,
        parentId: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (category !in listOf("users", "campsites") ||
            parentId.isBlank() ||
            parentId.contains("/")
            ) { onComplete(false)
                return
        }

        val storageRef = FirebaseStorage
            .getInstance("gs://syntaxcamping.firebasestorage.app")
            .reference
            .child("$category/$parentId")

        //Delete all images associated to the parent ID.
        storageRef.listAll()
            .addOnSuccessListener { result ->

                val deleteTasks = result.items.map { image -> image.delete() }

                Tasks.whenAll(deleteTasks)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }


}