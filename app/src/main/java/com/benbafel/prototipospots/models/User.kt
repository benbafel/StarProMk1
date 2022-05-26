package com.benbafel.prototipospots.models

import android.provider.ContactsContract

data class User(var name: String?,
                var email: String,
                private var password: String,
                var areasOfInterest: List<String>,
                var expertise: String,
                var notifications: Boolean,
)
