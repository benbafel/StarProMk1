package com.benbafel.prototipospots.models

import android.provider.ContactsContract

data class User(var name: String?,
                var email: String,
                var areasOfInterest: Int,
                var expertise: String
)
