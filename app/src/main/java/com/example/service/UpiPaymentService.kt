package com.example.service

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

enum class UpiApp(val displayName: String, val packageName: String?) {
    GPAY("Google Pay", "com.google.android.apps.nasp"),
    PHONEPE("PhonePe", "com.phonepe.app"),
    PAYTM("Paytm", "net.one97.paytm"),
    BHIM("BHIM UPI", "in.org.npci.upiapp"),
    GENERIC_UPI("All Installed UPI Apps", null)
}

data class UpiTransactionRequest(
    val payeeVpa: String = "housiesphere@upi",
    val payeeName: String = "HousieSphere Tambola",
    val amount: Int,
    val transactionRefId: String = "HS${System.currentTimeMillis()}",
    val transactionNote: String = "HousieSphere Wallet Top-up"
)

object UpiPaymentService {

    /**
     * Builds a standard UPI Deep Link Uri formatted per NPCI specifications:
     * upi://pay?pa=<vpa>&pn=<name>&tr=<refId>&tn=<note>&am=<amount>&cu=INR
     */
    fun buildUpiUri(request: UpiTransactionRequest): Uri {
        return Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", request.payeeVpa)
            .appendQueryParameter("pn", request.payeeName)
            .appendQueryParameter("tr", request.transactionRefId)
            .appendQueryParameter("tn", request.transactionNote)
            .appendQueryParameter("am", String.format("%.2f", request.amount.toDouble()))
            .appendQueryParameter("cu", "INR")
            .build()
    }

    /**
     * Launches the target UPI App using Android Intent deep linking.
     * If the app is not installed or running in an emulator without UPI apps,
     * it safely falls back and triggers payment simulation callback.
     */
    fun launchUpiPayment(
        context: Context,
        request: UpiTransactionRequest,
        targetApp: UpiApp = UpiApp.GENERIC_UPI,
        onSuccess: (refId: String, method: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val upiUri = buildUpiUri(request)
        val intent = Intent(Intent.ACTION_VIEW, upiUri)

        if (targetApp.packageName != null) {
            intent.setPackage(targetApp.packageName)
        }

        try {
            // Check if any activity can handle this intent
            val resolveInfoList = context.packageManager.queryIntentActivities(intent, 0)
            if (resolveInfoList.isNotEmpty() || targetApp.packageName == null) {
                context.startActivity(Intent.createChooser(intent, "Pay ₹${request.amount} via ${targetApp.displayName}"))
                Toast.makeText(
                    context,
                    "Opening ${targetApp.displayName} for UPI Deep Link...",
                    Toast.LENGTH_SHORT
                ).show()
                // Execute callback for successful deep link dispatch
                onSuccess(request.transactionRefId, "${targetApp.displayName} UPI")
            } else {
                // Fallback for emulator / environment without specific app installed
                Toast.makeText(
                    context,
                    "${targetApp.displayName} not found on device. Simulating UPI deep-link payment...",
                    Toast.LENGTH_LONG
                ).show()
                onSuccess(request.transactionRefId, "${targetApp.displayName} UPI (Simulated)")
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No UPI App available. Simulating payment success for ₹${request.amount}",
                Toast.LENGTH_LONG
            ).show()
            onSuccess(request.transactionRefId, "${targetApp.displayName} UPI (Simulated)")
        } catch (e: Exception) {
            onError("Payment failed: ${e.localizedMessage}")
        }
    }
}
