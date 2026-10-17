package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SnippetDao {
    @Query("SELECT * FROM snippets ORDER BY createdAt DESC")
    fun getAllSnippets(): Flow<List<SnippetEntity>>

    @Query("SELECT * FROM snippets WHERE id = :id LIMIT 1")
    suspend fun getSnippetById(id: Long): SnippetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: SnippetEntity): Long

    @Update
    suspend fun updateSnippet(snippet: SnippetEntity)

    @Delete
    suspend fun deleteSnippet(snippet: SnippetEntity)

    @Query("DELETE FROM snippets WHERE id = :id")
    suspend fun deleteSnippetById(id: Long)

    @Query("DELETE FROM snippets")
    suspend fun deleteAllSnippets()

    @Query("SELECT COUNT(*) FROM snippets")
    suspend fun getSnippetCount(): Int

    @Query("DELETE FROM snippets WHERE title IN ('SlidingWindowLimiter: Kotlin → Rust', 'Payload Sanitizer: Refactor & Hardening')")
    suspend fun deleteDefaultSeedSnippets()
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("DELETE FROM projects")
    suspend fun deleteAllProjects()

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun getProjectCount(): Int

    @Query("DELETE FROM projects WHERE name IN ('AlterCode Cloudflare Worker', 'AlterCode Android Client Core')")
    suspend fun deleteDefaultSeedProjects()

    // Project Files
    @Query("SELECT * FROM project_files WHERE projectId = :projectId ORDER BY fileName ASC")
    fun getFilesForProject(projectId: Long): Flow<List<ProjectFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectFile(file: ProjectFileEntity): Long

    @Update
    suspend fun updateProjectFile(file: ProjectFileEntity)

    @Delete
    suspend fun deleteProjectFile(file: ProjectFileEntity)

    @Query("DELETE FROM project_files WHERE id = :fileId")
    suspend fun deleteProjectFileById(fileId: Long)

    @Query("DELETE FROM project_files WHERE projectId = :projectId")
    suspend fun deleteFilesForProject(projectId: Long)

    @Query("DELETE FROM project_files")
    suspend fun deleteAllProjectFiles()

    @Query("DELETE FROM project_files WHERE projectId NOT IN (SELECT id FROM projects)")
    suspend fun deleteOrphanedFiles()
}

@Dao
interface UsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(record: UsageRecordEntity): Long

    @Query("SELECT COUNT(*) FROM usage_records WHERE timestamp >= :sinceTimestamp")
    suspend fun countUsageSince(sinceTimestamp: Long): Int

    @Query("DELETE FROM usage_records WHERE timestamp < :cutoffTimestamp")
    suspend fun purgeOldUsage(cutoffTimestamp: Long)
}

@Database(
    entities = [
        SnippetEntity::class,
        ProjectEntity::class,
        ProjectFileEntity::class,
        UsageRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SnippetDatabase : RoomDatabase() {
    abstract fun snippetDao(): SnippetDao
    abstract fun projectDao(): ProjectDao
    abstract fun usageDao(): UsageDao

    companion object {
        @Volatile
        private var INSTANCE: SnippetDatabase? = null

        fun getInstance(context: Context): SnippetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SnippetDatabase::class.java,
                    "altercode_vault.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
