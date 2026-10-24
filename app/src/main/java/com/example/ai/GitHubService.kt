package com.example.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GitHubRepo(
    val name: String,
    val fullName: String,
    val description: String,
    val language: String,
    val stars: Int,
    val forks: Int,
    val isPrivate: Boolean,
    val defaultBranch: String
)

data class GitHubFileNode(
    val path: String,
    val name: String,
    val type: String, // "blob" or "tree"
    val size: Long,
    val sha: String
)

data class GitHubUserProfile(
    val login: String,
    val name: String,
    val avatarUrl: String,
    val bio: String,
    val publicRepos: Int,
    val followers: Int,
    val following: Int = 0,
    val totalPrivateRepos: Int = 0,
    val htmlUrl: String = ""
)

class GitHubService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetchAuthenticatedUser(token: String): Result<GitHubUserProfile> =
        withContext(Dispatchers.IO) {
            try {
                val cleanToken = token.trim()
                if (cleanToken.isEmpty()) {
                    return@withContext Result.failure(IllegalArgumentException("Personal Access Token cannot be empty"))
                }
                val request = Request.Builder()
                    .url("https://api.github.com/user")
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "AlterCode-Android-IDE")
                    .header("Authorization", "Bearer $cleanToken")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string().orEmpty()
                        val msg = try {
                            JSONObject(errorBody).optString("message", "HTTP ${response.code}")
                        } catch (e: Exception) {
                            "HTTP ${response.code}: ${response.message}"
                        }
                        return@withContext Result.failure(Exception("GitHub Authentication failed ($msg). Please verify your token permissions."))
                    }
                    val bodyString = response.body?.string().orEmpty()
                    val json = JSONObject(bodyString)
                    val login = json.optString("login", "")
                    val name = json.optString("name", login)
                    val profile = GitHubUserProfile(
                        login = login,
                        name = if (name.isNotBlank()) name else login,
                        avatarUrl = json.optString("avatar_url", ""),
                        bio = json.optString("bio", ""),
                        publicRepos = json.optInt("public_repos", 0),
                        followers = json.optInt("followers", 0),
                        following = json.optInt("following", 0),
                        totalPrivateRepos = json.optInt("total_private_repos", 0),
                        htmlUrl = json.optString("html_url", "https://github.com/$login")
                    )
                    Result.success(profile)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun fetchAuthenticatedUserRepos(token: String): Result<List<GitHubRepo>> =
        withContext(Dispatchers.IO) {
            try {
                val cleanToken = token.trim()
                val request = Request.Builder()
                    .url("https://api.github.com/user/repos?sort=updated&per_page=100&affiliation=owner,collaborator")
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "AlterCode-Android-IDE")
                    .header("Authorization", "Bearer $cleanToken")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Failed to fetch user repositories (${response.code})"))
                    }
                    val jsonArr = JSONArray(response.body?.string().orEmpty())
                    val repos = mutableListOf<GitHubRepo>()
                    for (i in 0 until jsonArr.length()) {
                        val obj = jsonArr.getJSONObject(i)
                        repos.add(
                            GitHubRepo(
                                name = obj.optString("name", "repo"),
                                fullName = obj.optString("full_name", ""),
                                description = obj.optString("description", "No description provided"),
                                language = obj.optString("language", "Code"),
                                stars = obj.optInt("stargazers_count", 0),
                                forks = obj.optInt("forks_count", 0),
                                isPrivate = obj.optBoolean("private", false),
                                defaultBranch = obj.optString("default_branch", "main")
                            )
                        )
                    }
                    Result.success(repos)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun fetchUserProfile(username: String, token: String? = null): Result<GitHubUserProfile> =
        withContext(Dispatchers.IO) {
            try {
                val cleanUser = username.trim().removePrefix("@")
                if (cleanUser.isEmpty()) {
                    return@withContext Result.failure(IllegalArgumentException("Username cannot be empty"))
                }
                val requestBuilder = Request.Builder()
                    .url("https://api.github.com/users/$cleanUser")
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "AlterCode-Android-IDE")

                if (!token.isNullOrBlank()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }

                client.newCall(requestBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("GitHub API returned ${response.code}: ${response.message}"))
                    }
                    val bodyString = response.body?.string().orEmpty()
                    val json = JSONObject(bodyString)
                    val profile = GitHubUserProfile(
                        login = json.optString("login", cleanUser),
                        name = json.optString("name", cleanUser),
                        avatarUrl = json.optString("avatar_url", ""),
                        bio = json.optString("bio", ""),
                        publicRepos = json.optInt("public_repos", 0),
                        followers = json.optInt("followers", 0)
                    )
                    Result.success(profile)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun fetchUserRepos(username: String, token: String? = null): Result<List<GitHubRepo>> =
        withContext(Dispatchers.IO) {
            try {
                val cleanUser = username.trim().removePrefix("@")
                if (cleanUser.isEmpty()) {
                    return@withContext Result.failure(IllegalArgumentException("Username cannot be empty"))
                }
                val url = if (token.isNullOrBlank()) {
                    "https://api.github.com/users/$cleanUser/repos?sort=updated&per_page=30"
                } else {
                    "https://api.github.com/user/repos?sort=updated&per_page=30"
                }

                val requestBuilder = Request.Builder()
                    .url(url)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "AlterCode-Android-IDE")

                if (!token.isNullOrBlank()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }

                client.newCall(requestBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Failed to fetch repositories (${response.code})"))
                    }
                    val jsonArr = JSONArray(response.body?.string().orEmpty())
                    val repos = mutableListOf<GitHubRepo>()
                    for (i in 0 until jsonArr.length()) {
                        val obj = jsonArr.getJSONObject(i)
                        repos.add(
                            GitHubRepo(
                                name = obj.optString("name", "repo"),
                                fullName = obj.optString("full_name", ""),
                                description = obj.optString("description", "No description provided"),
                                language = obj.optString("language", "Code"),
                                stars = obj.optInt("stargazers_count", 0),
                                forks = obj.optInt("forks_count", 0),
                                isPrivate = obj.optBoolean("private", false),
                                defaultBranch = obj.optString("default_branch", "main")
                            )
                        )
                    }
                    Result.success(repos)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun fetchRepoTree(owner: String, repo: String, branch: String = "main", token: String? = null): Result<List<GitHubFileNode>> =
        withContext(Dispatchers.IO) {
            try {
                val url = "https://api.github.com/repos/$owner/$repo/git/trees/$branch?recursive=1"
                val requestBuilder = Request.Builder()
                    .url(url)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "AlterCode-Android-IDE")

                if (!token.isNullOrBlank()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }

                client.newCall(requestBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Failed to load repo tree (${response.code})"))
                    }
                    val root = JSONObject(response.body?.string().orEmpty())
                    val treeArr = root.optJSONArray("tree") ?: JSONArray()
                    val nodes = mutableListOf<GitHubFileNode>()
                    for (i in 0 until treeArr.length()) {
                        val obj = treeArr.getJSONObject(i)
                        val path = obj.optString("path", "")
                        val type = obj.optString("type", "blob")
                        val size = obj.optLong("size", 0L)
                        val sha = obj.optString("sha", "")
                        val name = path.substringAfterLast('/')
                        nodes.add(GitHubFileNode(path = path, name = name, type = type, size = size, sha = sha))
                    }
                    Result.success(nodes)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun fetchRawFileContent(owner: String, repo: String, branch: String, filePath: String, token: String? = null): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                // If token is provided, prefer api.github.com to allow accessing private repos
                val requestBuilder = if (!token.isNullOrBlank()) {
                    Request.Builder()
                        .url("https://api.github.com/repos/$owner/$repo/contents/$filePath?ref=$branch")
                        .header("Accept", "application/vnd.github.v3.raw")
                        .header("Authorization", "Bearer $token")
                        .header("User-Agent", "AlterCode-Android-IDE")
                } else {
                    Request.Builder()
                        .url("https://raw.githubusercontent.com/$owner/$repo/$branch/$filePath")
                        .header("User-Agent", "AlterCode-Android-IDE")
                }

                client.newCall(requestBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Failed to read file (${response.code})"))
                    }
                    val content = response.body?.string().orEmpty()
                    Result.success(content)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
