package com.example.newsclient.data.model

/**
 * @author Shi
 * @version 1.0
 * the basic data model for the news
 */

data class News(
    val id: String,
    val title: String,
    val content: String,
    val author: String,
    val date: String,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val source: String? = null
) {
    override fun toString(): String {
        return "News(id='$id', title='$title', content='$content', author='$author', date='$date', imageUrl=$imageUrl, source=$source)"
    }
}