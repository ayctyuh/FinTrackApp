package com.fintrack.project.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fintrack.project.MainActivity
import com.fintrack.project.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Service lang nghe thong bao he thong de nhan giao dich ngan hang.
 * Phu thuoc: Android Notification API, `BankNotificationParser`, SharedPreferences.
 * Duoc su dung boi man hinh nhap tu thong bao (BankNotificationImportSheet).
 */
class BankNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "BankNotiService"

        // SharedPreferences keys
        const val PREFS_NAME      = "bank_notification_prefs"
        const val KEY_PENDING_TXN = "pending_bank_txns"

        // Notification channel cho alert của FinTrack
        private const val CHANNEL_ID   = "fintrack_bank_import"
        private const val CHANNEL_NAME = "Giao dịch ngân hàng"

        // Notification ID cố định (update thay vì spam)
        private const val SUMMARY_NOTIFICATION_ID = 9001

        // ──────────────────────────────────────────────────────────────────────
        // Helpers để UI đọc/xoá hàng chờ
        // ──────────────────────────────────────────────────────────────────────

        private val gson = Gson()
        private val listType = object : TypeToken<MutableList<ParsedBankTransaction>>() {}.type

        /**
         * Lay danh sach giao dich dang cho xac nhan.
         * @param context Context de doc SharedPreferences.
         */
        fun getPendingTransactions(context: Context): List<ParsedBankTransaction> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json  = prefs.getString(KEY_PENDING_TXN, null) ?: return emptyList()
            return try {
                gson.fromJson(json, listType) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        /**
         * Xoa tat ca giao dich trong hang cho.
         * @param context Context de ghi SharedPreferences.
         */
        fun clearPendingTransactions(context: Context) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().remove(KEY_PENDING_TXN).apply()
        }

        /**
         * Xoa mot giao dich theo index.
         * @param context Context de ghi SharedPreferences.
         * @param index Vi tri can xoa.
         */
        fun removePendingTransaction(context: Context, index: Int) {
            val list = getPendingTransactions(context).toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
                savePendingList(context, list)
            }
        }

        /**
         * Kiem tra quyen Notification Access.
         * @param context Context de doc Settings.
         */
        fun isNotificationAccessGranted(context: Context): Boolean {
            val enabledPackages = android.provider.Settings
                .Secure
                .getString(context.contentResolver, "enabled_notification_listeners")
                ?: return false
            return enabledPackages.contains(context.packageName)
        }

        // ─── Private helpers ──────────────────────────────────────────────────

        /**
         * Luu danh sach giao dich vao hang cho.
         * @param context Context de ghi SharedPreferences.
         * @param list Danh sach can luu.
         */
        private fun savePendingList(context: Context, list: List<ParsedBankTransaction>) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PENDING_TXN, gson.toJson(list))
                .apply()
        }

        /**
         * Them giao dich vao hang cho, co chong trung lap.
         * @param context Context de ghi SharedPreferences.
         * @param txn Giao dich vua parse.
         */
        private fun addPendingTransaction(context: Context, txn: ParsedBankTransaction) {
            val list = getPendingTransactions(context).toMutableList()

            // Chống trùng: bỏ qua nếu cùng số tiền + loại + cách nhau < 5 giây
            val isDuplicate = list.any { existing ->
                existing.amount == txn.amount &&
                        existing.type   == txn.type   &&
                        kotlin.math.abs(existing.timestamp - txn.timestamp) < 5_000L
            }
            if (isDuplicate) {
                Log.d(TAG, "Bỏ qua giao dịch trùng: ${txn.amount}")
                return
            }

            list.add(0, txn) // Mới nhất lên đầu
            savePendingList(context, list)
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Vòng đời Service
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Khoi tao service va channel thong bao.
     */
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "BankNotificationListenerService đã khởi động")
    }

    /**
     * Huy service khi he thong dung.
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "BankNotificationListenerService đã dừng")
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Nhận thông báo mới
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Nhan thong bao moi tu he thong.
     * @param sbn Thong bao he thong.
     */
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val extras = sbn.notification?.extras ?: return

            val title   = extras.getCharSequence("android.title")?.toString()   ?: ""
            val content = extras.getCharSequence("android.text")?.toString()    ?: ""
            val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""

            // Ưu tiên bigText nếu có (thường dài hơn, đầy đủ hơn)
            val bodyText = if (bigText.isNotBlank()) bigText else content

            Log.d(TAG, "Thông báo từ [${sbn.packageName}] title=$title")

            val parsed = BankNotificationParser.parse(title, bodyText) ?: return

            Log.i(TAG, "✅ Parse thành công: ${parsed.bankName} ${parsed.type} ${parsed.amount}")

            addPendingTransaction(applicationContext, parsed)
            showSummaryNotification()

        } catch (e: Exception) {
            Log.e(TAG, "Lỗi xử lý thông báo: ${e.message}", e)
        }
    }

    /**
     * Xu ly khi thong bao bi go.
     * @param sbn Thong bao he thong.
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Không cần xử lý
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Hiện notification tóm tắt cho user
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Hien thong bao tom tat so giao dich cho xac nhan.
     */
    private fun showSummaryNotification() {
        val pendingCount = getPendingTransactions(applicationContext).size

        // Intent mở thẳng màn hình TransactionHistory khi bấm vào notification
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "transaction_history")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)   // Dùng icon app hiện có
            .setContentTitle("FinTrack – Giao dịch mới")
            .setContentText("Có $pendingCount giao dịch ngân hàng chờ xác nhận")
            .setSubText("Nhấn để xem & nhập vào FinTrack")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true) // Chỉ rung/âm thanh lần đầu
            .build()

        notificationManager.notify(SUMMARY_NOTIFICATION_ID, notification)
    }

    /**
     * Tao notification channel cho Android O+.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Thông báo khi phát hiện giao dịch ngân hàng mới"
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  HƯỚNG DẪN TÍCH HỢP VÀO DỰ ÁN
 * ═══════════════════════════════════════════════════════════════════════════
 *
 *  1. AndroidManifest.xml — thêm vào trong <application>:
 *
 *     <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
 *
 *     <service
 *         android:name=".service.BankNotificationListenerService"
 *         android:label="FinTrack – Đọc thông báo ngân hàng"
 *         android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
 *         android:exported="true">
 *         <intent-filter>
 *             <action android:name="android.service.notification.NotificationListenerService" />
 *         </intent-filter>
 *         <meta-data
 *             android:name="android.service.notification.default_filter_types"
 *             android:value="conversations" />
 *     </service>
 *
 *  2. Trong TransactionHistoryScreen.kt — thêm nút "Nhập từ thông báo":
 *     Xem file BankNotificationImportSheet.kt để biết cách dùng.
 *
 * ═══════════════════════════════════════════════════════════════════════════
 */