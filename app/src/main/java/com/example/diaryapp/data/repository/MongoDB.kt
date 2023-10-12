package com.example.diaryapp.data.repository

import com.example.diaryapp.BuildConfig
import com.example.diaryapp.model.Diary
import io.realm.kotlin.Realm
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration

object MongoDB: MongoRepository {

    private val app = App.create(BuildConfig.APP_ID)
    private val user = app.currentUser
    private lateinit var ream: Realm

    override fun configureTheReam() {
        if (user != null) {
            val config = SyncConfiguration.Builder(user, setOf(Diary::class))
                .initialSubscriptions { sub ->
                    add(
                        query = sub.query(Diary::class, "ownerId == $0", user.id),
                        name = "User's Diaries"
                    )
                }
                .log(LogLevel.ALL)
                .build()
            ream = Realm.open(config)
        }
    }
}