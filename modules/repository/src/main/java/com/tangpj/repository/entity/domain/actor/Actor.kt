package com.tangpj.repository.entity.domain.actor

import androidx.room.Ignore
import com.tangpj.github.entity.BaseEntity
import kotlinx.android.parcel.Parcelize

@Parcelize
open class Actor(
        @field:Ignore
        override val id : String,
        @field:Ignore
        open val login: String,
        @field:Ignore
        open val avatarUrl: String? = null
) : BaseEntity(id)