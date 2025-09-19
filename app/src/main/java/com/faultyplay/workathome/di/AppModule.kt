package com.faultyplay.workathome.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.faultyplay.workathome.data.local.converter.SerializationConverters
import com.faultyplay.workathome.data.local.dao.HouseDao
import com.faultyplay.workathome.data.local.dao.TaskDao
import com.faultyplay.workathome.data.local.dao.TaskSuggestionDao
import com.faultyplay.workathome.data.local.db.WorkAtHomeDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideConverters(json: Json): SerializationConverters = SerializationConverters(json)

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        converters: SerializationConverters
    ): WorkAtHomeDatabase = Room.databaseBuilder(
        context,
        WorkAtHomeDatabase::class.java,
        "work_at_home.db"
    )
        .addTypeConverter(converters)
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideHouseDao(database: WorkAtHomeDatabase): HouseDao = database.houseDao()

    @Provides
    fun provideTaskDao(database: WorkAtHomeDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideSuggestionDao(database: WorkAtHomeDatabase): TaskSuggestionDao =
        database.taskSuggestionDao()

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("user_preferences.preferences_pb") }
    )
}
