package org.kimp.tfs.hw1.data.model

import android.os.Parcel
import android.os.Parcelable

data class ContactInfo(
    val id: String,
    val name: String,
    val phone: String,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!) { }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(phone)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ContactInfo> {
        override fun createFromParcel(parcel: Parcel): ContactInfo {
            return ContactInfo(parcel)
        }

        override fun newArray(size: Int): Array<ContactInfo?> {
            return arrayOfNulls(size)
        }
    }
}
