package com.tocletoque.thebreedinglab.model

val pedigreeRegistry = mutableMapOf<String, Dog>()
val childrenByParent = mutableMapOf<String, MutableList<String>>()

fun registerDog(dog: Dog) {
    pedigreeRegistry[dog.name] = dog
    dog.mother?.let {
        childrenByParent.getOrPut(it) { mutableListOf() }.add(dog.name)
    }
    dog.father?.let {
        childrenByParent.getOrPut(it) { mutableListOf() }.add(dog.name)
    }
}

fun isParentChild(dog1: Dog, dog2: Dog): Boolean {
    return dog1.name == dog2.mother ||
            dog1.name == dog2.father ||
            dog2.name == dog1.mother ||
            dog2.name == dog1.father
}

fun areFullSiblings(dog1: Dog, dog2: Dog): Boolean {
    return dog1.mother != null && dog1.father != null &&
            dog1.mother == dog2.mother &&
            dog1.father == dog2.father &&
            dog1.name != dog2.name
}

fun areHalfSiblings(dog1: Dog, dog2: Dog): Boolean {
    var shared = 0
    if (dog1.mother != null && dog1.mother == dog2.mother) shared++
    if (dog1.father != null && dog1.father == dog2.father) shared++
    return shared == 1
}

fun getChildren(parentName: String): List<String> {
    return childrenByParent[parentName]?.toList() ?: emptyList()
}

fun getFullSiblings(dog: Dog): List<String> {
    if (dog.mother == null || dog.father == null) return emptyList()
    val fromMother = childrenByParent[dog.mother]?.toSet() ?: emptySet()
    val fromFather = childrenByParent[dog.father]?.toSet() ?: emptySet()
    val sibs = (fromMother intersect fromFather) - dog.name
    return sibs.sorted()
}

fun getHalfSiblings(dog: Dog): List<String> {
    if (dog.mother == null && dog.father == null) return emptyList()
    var half = mutableSetOf<String>()
    dog.mother?.let { half.addAll(childrenByParent[it] ?: emptyList()) }
    dog.father?.let { half.addAll(childrenByParent[it] ?: emptyList()) }
    val full = getFullSiblings(dog).toSet()
    half.removeAll(full)
    half.remove(dog.name)
    return half.sorted()
}