package com.skychx.androidweather.logic.model

import com.google.gson.annotations.SerializedName

//{
//  "status": "ok",
//  "query": "北京",
//  "places": [
//    {
//      "id": "B000A83AJN",
//      "name": "北京市",
//      "place_id": "a-B000A83AJN",
//      "formatted_address": "中国北京市",
//      "location": {
//        "lat": 39.9041999,
//        "lng": 116.4073963
//    }
//  ]
//}
data class PlaceResponse(val status: String, val places: List<Place>)

data class Place(val name: String, val location: Location, @SerializedName("formatted_address") val address: String)

data class Location(val lng: String, val lat: String)