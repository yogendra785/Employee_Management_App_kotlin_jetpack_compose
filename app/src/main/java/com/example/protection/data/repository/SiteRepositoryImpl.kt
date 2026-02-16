package com.example.protection.data.repository

import com.example.protection.domain.model.Site
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SiteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SiteRepository {

    private val siteCollection = firestore.collection("sites")

    override fun getAllSites(): Flow<List<Site>> = callbackFlow {
        // Listen for real-time updates ordered by creation time
        val listener = siteCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val sites = snapshot.toObjects(Site::class.java)
                    trySend(sites)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveSite(site: Site) {
        val docRef = if (site.id.isEmpty()) {
            siteCollection.document() // Create new ID if empty
        } else {
            siteCollection.document(site.id) // Use existing ID if updating
        }

        // Save the site with the ID embedded
        val siteWithId = site.copy(id = docRef.id)
        docRef.set(siteWithId).await()
    }

    override suspend fun deleteSite(siteId: String) {
        siteCollection.document(siteId).delete().await()
    }

    override suspend fun getSiteById(siteId: String): Site? {
        return try {
            siteCollection.document(siteId).get().await().toObject(Site::class.java)
        } catch (e: Exception) {
            null
        }
    }
}