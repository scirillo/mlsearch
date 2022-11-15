package com.sciri.mlsearch.api.dtos

import com.sciri.mlsearch.api.dtos.UserDTO
import com.sciri.mlsearch.domains.User

class UserDTOMapper {
    fun fromUserDTOToUserDomain(userDTO: UserDTO): User = User(
        userDTO.id,
        userDTO.email,
        userDTO.authenticationToken,
    )
}