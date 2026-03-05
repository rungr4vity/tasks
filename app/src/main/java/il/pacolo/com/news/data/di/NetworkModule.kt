package il.pacolo.com.news.data.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import il.pacolo.com.news.data.remote.ApiService
import il.pacolo.com.news.security.NativeKeys
import il.pacolo.com.news.utils.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object  NetworkModule {

    @Provides
    @Singleton
    fun provideApiKey():String = NativeKeys.getWeatherApiKey()


    //Retrofit
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL_WEATHER)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

    @Provides
    fun providesOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder().build()


    // Room
    // application or @ApplicationContext context: Context

//    @Provides
//    @Singleton
//    fun providesLocationsDatabase(application: Application): LocationsDatabase {
//        return Room.databaseBuilder(
//            application,
//            LocationsDatabase::class.java,
//            "locations_db")
//            .fallbackToDestructiveMigration()
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun providesLocationsDao(locationsDatabase: LocationsDatabase):LocationsDatabaseDao {
//        return locationsDatabase.locationsDao()
//    }


}