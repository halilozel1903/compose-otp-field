package io.github.halilozel1903.otp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

/**
 * The only place that touches Google Play services. play-services-auth-api-phone is a compileOnly dependency, so
 * nothing here may run before [isAvailable] said the classes exist.
 */
internal object SmsUserConsent {
    /** A compile-time copy of the Play services constant, so reading it never loads Play services classes. */
    const val EXTRA_SMS_MESSAGE: String = SmsRetriever.EXTRA_SMS_MESSAGE

    private const val RETRIEVER_CLASS = "com.google.android.gms.auth.api.phone.SmsRetriever"

    fun isAvailable(): Boolean = try {
        Class.forName(RETRIEVER_CLASS)
        true
    } catch (e: ClassNotFoundException) {
        false
    } catch (e: LinkageError) {
        false
    }

    /** Starts SMS User Consent and registers the receiver; returns it so it can be unregistered, or `null`. */
    fun listen(
        context: Context,
        senderPhoneNumber: String?,
        onConsentIntent: (Intent) -> Unit,
        onTimeout: () -> Unit,
    ): BroadcastReceiver? = try {
        SmsRetriever.getClient(context).startSmsUserConsent(senderPhoneNumber)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != SmsRetriever.SMS_RETRIEVED_ACTION) return
                val extras = intent.extras ?: return
                val status = extras.parcelable<Status>(SmsRetriever.EXTRA_STATUS) ?: return
                when (status.statusCode) {
                    CommonStatusCodes.SUCCESS ->
                        extras.parcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)?.let(onConsentIntent)
                    CommonStatusCodes.TIMEOUT -> onTimeout()
                }
            }
        }
        val filter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, SmsRetriever.SEND_PERMISSION, null, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter, SmsRetriever.SEND_PERMISSION, null)
        }
        receiver
    } catch (e: Exception) {
        // No Google Play services on this device, or it refused the request.
        null
    } catch (e: LinkageError) {
        null
    }

    private inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelable(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelable<T>(key)
        }
}
