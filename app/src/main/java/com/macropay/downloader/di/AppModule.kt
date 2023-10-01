package com.macropay.downloader.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class) //Crea componentes Singleton, que exitiran hasta que se cierre la app
class AppModule
{

/*
    @Component
    interface WarComponent {
        fun provideWarDagger(): MonSinConexion
    }
*/

}