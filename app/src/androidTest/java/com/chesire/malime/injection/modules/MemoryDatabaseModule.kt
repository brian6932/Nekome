package com.chesire.malime.injection.modules

import android.content.Context
import androidx.room.Room
import com.chesire.malime.database.RoomDB
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Suppress("unused")
@Module
object MemoryDatabaseModule {
    @Provides
    @Singleton
    @JvmStatic
    fun provideDB(context: Context): com.chesire.malime.database.RoomDB {
        return Room
            .inMemoryDatabaseBuilder(context, com.chesire.malime.database.RoomDB::class.java)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    @JvmStatic
    fun provideSeries(db: com.chesire.malime.database.RoomDB) = db.series()

    @Provides
    @Singleton
    @JvmStatic
    fun provideUser(db: com.chesire.malime.database.RoomDB) = db.user()
}
