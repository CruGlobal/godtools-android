package org.cru.godtools.account

import android.os.Parcel
import android.os.Parcelable
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginResponseTest {
    @Test
    fun `LoginResponse - Success - Parcelable`() {
        val data = writeParcelable(LoginResponse.Success)
        assertEquals(LoginResponse.Success, readParcelable(data))
    }

    @Test
    fun `LoginResponse - Error - Parcelable`() {
        val data = writeParcelable(LoginResponse.Error())
        assertEquals(LoginResponse.Error::class, readParcelable(data)!!::class)
    }

    @Test
    fun `LoginResponse - Error - UserNotFound - Parcelable`() {
        val data = writeParcelable(LoginResponse.Error.UserNotFound)
        assertEquals(LoginResponse.Error.UserNotFound, readParcelable(data))
    }

    @Test
    fun `LoginResponse - Error - UserAlreadyExists - Parcelable`() {
        val data = writeParcelable(LoginResponse.Error.UserAlreadyExists)
        assertEquals(LoginResponse.Error.UserAlreadyExists, readParcelable(data))
    }

    private fun writeParcelable(obj: Parcelable) = Parcel.obtain()
        .apply { writeParcelable(obj, 0) }
        .marshall()

    private fun readParcelable(bytes: ByteArray) = Parcel.obtain()
        .apply {
            unmarshall(bytes, 0, bytes.size)
            setDataPosition(0)
        }
        .readParcelable(this::class.java.classLoader, LoginResponse::class.java)
}
