package com.sciri.mlsearch.api.dtos

import com.sciri.mlsearch.domains.Dog

class DogDTOMapper {

    fun fromDogDTOToDogDomain(dogDTO: DogDTO): Dog {
        return Dog(
            dogDTO.id,
            dogDTO.index,
            dogDTO.name,
            dogDTO.type,
            dogDTO.heightMale,
            dogDTO.heightFemale,
            dogDTO.imageUrl,
            dogDTO.lifeExpectancy,
            dogDTO.temperament,
            dogDTO.weightFemale,
            dogDTO.weightMale
        )
    }

    fun formDogDTOListToDogDomainList(dogDtoList: List<DogDTO>): List<Dog> {
        return dogDtoList.map {
            fromDogDTOToDogDomain(it)
        }
    }
}