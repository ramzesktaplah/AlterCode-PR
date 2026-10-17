package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class SnippetRepository(context: Context) {
    private val database = SnippetDatabase.getInstance(context)
    private val snippetDao = database.snippetDao()
    private val projectDao = database.projectDao()
    private val usageDao = database.usageDao()

    val allSnippets: Flow<List<SnippetEntity>> = snippetDao.getAllSnippets()
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    suspend fun saveSnippet(snippet: SnippetEntity): Long {
        return snippetDao.insertSnippet(snippet)
    }

    suspend fun deleteSnippet(id: Long) {
        snippetDao.deleteSnippetById(id)
    }

    suspend fun deleteAllSnippets() {
        snippetDao.deleteAllSnippets()
    }

    suspend fun saveProject(project: ProjectEntity): Long {
        return projectDao.insertProject(project)
    }

    suspend fun deleteProject(id: Long) {
        projectDao.deleteFilesForProject(id)
        projectDao.deleteProjectById(id)
    }

    suspend fun deleteAllProjects() {
        projectDao.deleteAllProjectFiles()
        projectDao.deleteAllProjects()
    }

    fun getFilesForProject(projectId: Long): Flow<List<ProjectFileEntity>> {
        return projectDao.getFilesForProject(projectId)
    }

    suspend fun saveProjectFile(file: ProjectFileEntity): Long {
        return projectDao.insertProjectFile(file)
    }

    suspend fun deleteProjectFile(fileId: Long) {
        projectDao.deleteProjectFileById(fileId)
    }

    suspend fun logUsage(
        actionType: String,
        languagePair: String,
        routedProvider: String,
        deviceFingerprint: String
    ) {
        usageDao.insertUsage(
            UsageRecordEntity(
                actionType = actionType,
                languagePair = languagePair,
                routedProvider = routedProvider,
                deviceFingerprintHash = deviceFingerprint
            )
        )
    }

    suspend fun countUsageSince(sinceTimestamp: Long): Int {
        return usageDao.countUsageSince(sinceTimestamp)
    }

    /**
     * Purges default demo seed data so user workspace remains strictly clean.
     */
    suspend fun purgeDefaultSeedData() {
        snippetDao.deleteDefaultSeedSnippets()
        projectDao.deleteDefaultSeedProjects()
        projectDao.deleteOrphanedFiles()
    }
}
