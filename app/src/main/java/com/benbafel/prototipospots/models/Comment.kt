package com.benbafel.prototipospots.models

import java.io.Serializable

data class Comment(
    val id: String,
    val user: String,
    val userComment: String) :Serializable
