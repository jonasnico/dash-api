package com.dash.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubEvent(
    val id: String,
    val type: String,
    val repo: GitHubRepo,
    val payload: GitHubPayload,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class GitHubRepo(
    val name: String,
    val url: String
)

@Serializable
data class GitHubPayload(
    val ref: String? = null,
    val commits: List<GitHubCommit>? = null,
    val action: String? = null,
    @SerialName("pull_request") val pullRequest: GitHubPullRequest? = null,
    val issue: GitHubIssue? = null
)

@Serializable
data class GitHubCommit(
    val message: String
)

@Serializable
data class GitHubPullRequest(
    val title: String,
    @SerialName("html_url") val htmlUrl: String
)

@Serializable
data class GitHubIssue(
    val title: String,
    @SerialName("html_url") val htmlUrl: String
)
