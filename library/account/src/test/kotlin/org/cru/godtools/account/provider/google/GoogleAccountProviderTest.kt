package org.cru.godtools.account.provider.google

import android.content.Context
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.play.auth.signin.GoogleSignInKtx
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class GoogleAccountProviderTest {
    private val lastSignedInAccount = MutableStateFlow<GoogleSignInAccount?>(null)

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private lateinit var provider: GoogleAccountProvider

    @BeforeTest
    fun setup() {
        mockkObject(GoogleSignInKtx)
        every { GoogleSignInKtx.getLastSignedInAccountFlow(any()) } returns lastSignedInAccount
        mockkStatic(GoogleSignIn::class)
        every { GoogleSignIn.getLastSignedInAccount(any()) } answers { lastSignedInAccount.value }
        provider = GoogleAccountProvider(mockk(), context, mockk())
    }

    @AfterTest
    fun cleanup() {
        unmockkStatic(GoogleSignIn::class)
        unmockkObject(GoogleSignInKtx)
    }

    // region userIdFlow()
    @Test
    fun `userIdFlow()`() = runTest {
        val account = GoogleSignInAccount.createDefault()
        val userId = UUID.randomUUID().toString()
        provider.prefs.edit { putString(GoogleAccountProvider.PREF_USER_ID(account), userId) }

        provider.userIdFlow().test {
            runCurrent()
            assertNull(expectMostRecentItem())

            lastSignedInAccount.value = account
            runCurrent()
            assertEquals(userId, expectMostRecentItem())

            lastSignedInAccount.value = null
            runCurrent()
            assertNull(expectMostRecentItem())
        }
    }

    @Test
    fun `userIdFlow() - emits when account updates userId`() = runTest {
        val account = GoogleSignInAccount.createDefault()

        provider.userIdFlow().test {
            runCurrent()
            assertNull(expectMostRecentItem())

            lastSignedInAccount.value = account
            runCurrent()

            val userId1 = UUID.randomUUID().toString()
            provider.prefs.edit { putString(GoogleAccountProvider.PREF_USER_ID(account), userId1) }
            runCurrent()
            assertEquals(userId1, expectMostRecentItem())

            val userId2 = UUID.randomUUID().toString()
            provider.prefs.edit { putString(GoogleAccountProvider.PREF_USER_ID(account), userId2) }
            runCurrent()
            assertEquals(userId2, expectMostRecentItem())
        }
    }
    // endregion userIdFlow()
}
