package com.faultyplay.workathome.di

import com.faultyplay.workathome.data.repository.FirebaseAuthRepository
import com.faultyplay.workathome.data.repository.HouseRepositoryImpl
import com.faultyplay.workathome.data.repository.TaskRepositoryImpl
import com.faultyplay.workathome.domain.repository.AuthRepository
import com.faultyplay.workathome.domain.repository.HouseRepository
import com.faultyplay.workathome.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(repository: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindHouseRepository(repository: HouseRepositoryImpl): HouseRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(repository: TaskRepositoryImpl): TaskRepository
}
