package com.fintrack.project.service

import com.fintrack.project.data.model.TransactionType

/**
 * Kết quả sau khi parse thông báo ngân hàng.
 */
data class ParsedBankTransaction(
    val amount: Double,
    val type: TransactionType,
    val bankName: String,
    val description: String,
    val balance: Double? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val rawText: String = ""
)

object BankNotificationParser {

    // ─── Nhận diện ngân hàng ────────────────────────────────────────────────

    private val BANK_KEYWORDS: Map<String, List<String>> = mapOf(
        "Techcombank" to listOf("TCB", "Techcombank", "TECHCOMBANK"),
        "Vietcombank"  to listOf("VCB", "Vietcombank",  "VIETCOMBANK"),
        "MB Bank"      to listOf("MBBank", "MB Bank",   "MBBANK", "MB:"),
        "Vietinbank"   to listOf("Vietinbank", "VTB",   "VIETINBANK", "CTG"),
        "BIDV"         to listOf("BIDV"),
        "Agribank"     to listOf("Agribank", "AGB",     "AGRIBANK"),
        "TPBank"       to listOf("TPBank", "TP Bank",   "TPBANK"),
        "ACB"          to listOf("ACB:"),
        "Sacombank"    to listOf("Sacombank", "STB",    "SACOMBANK"),
        "VPBank"       to listOf("VPBank", "VP Bank",   "VPBANK"),
        "SHB"          to listOf("SHB:"),
        "HDBank"       to listOf("HDBank", "HDBANK"),
        "MSB"          to listOf("MSB:"),
        "OCB"          to listOf("OCB:"),
        "Nam A Bank"   to listOf("NamABank", "Nam A"),
        "Eximbank"     to listOf("Eximbank", "EIB"),
        "LienVietPost" to listOf("LPBank", "LienViet"),
    )

    // ─── Regex parse số tiền ────────────────────────────────────────────────

    // ĐÃ SỬA: Bắt buộc phải có chữ VND, VNĐ, vnd, đ hoặc d để tránh bắt nhầm số điện thoại
    private val SIGNED_AMOUNT = Regex(
        """([+\-])\s*([\d][.\d,]*\d|\d+)\s*(VND|VNĐ|vnd|đ|d\b)""",
        RegexOption.IGNORE_CASE
    )

    private val DEBIT_KEYWORDS  = Regex("""GHI\s*NO[:\s]*([\d][.\d,]*\d|\d+)\s*(?:VND|VNĐ|vnd|đ)?""",  RegexOption.IGNORE_CASE)
    private val CREDIT_KEYWORDS = Regex("""GHI\s*CO[:\s]*([\d][.\d,]*\d|\d+)\s*(?:VND|VNĐ|vnd|đ)?""",  RegexOption.IGNORE_CASE)
    private val MB_CREDIT = Regex("""CONG[:\s]*([\d][.\d,]*\d|\d+)""",  RegexOption.IGNORE_CASE)
    private val MB_DEBIT  = Regex("""TRICH[:\s]*([\d][.\d,]*\d|\d+)""", RegexOption.IGNORE_CASE)
    private val AGB_CREDIT = Regex("""PS\s*CO[:\s]*([\d][.\d,]*\d|\d+)""", RegexOption.IGNORE_CASE)
    private val AGB_DEBIT  = Regex("""PS\s*NO[:\s]*([\d][.\d,]*\d|\d+)""", RegexOption.IGNORE_CASE)

    private val BALANCE_REGEX = Regex(
        """(?:SD|So du|Số dư|SDhientai|SD hien tai|So du:|Balance)[:\s]+\+?([\d][.\d,]*\d|\d+)\s*(?:VND|VNĐ|vnd|đ)?""",
        RegexOption.IGNORE_CASE
    )

    private val DESCRIPTION_REGEX = Regex(
        """(?:ND|Noi dung|Nội dung|Mo ta|Mô tả|Content)[:\s]+([^\n.;]{3,80})""",
        RegexOption.IGNORE_CASE
    )

    // ─── Từ khoá loại bỏ thông báo quảng cáo ───────────────────────────────

    private val PROMO_KEYWORDS = listOf(
        "uu dai", "ưu đãi", "khuyen mai", "khuyến mãi",
        "cashback", "hoàn tiền", "hoan tien",
        "giam gia", "giảm giá", "voucher", "coupon",
        "diem thuong", "điểm thưởng",
        "click vao", "click ngay", "truy cap", "truy cập",
        "dang ky ngay", "đăng ký ngay",
        "mo the", "mở thẻ",
    )

    private val TRANSACTION_KEYWORDS = listOf(
        "ghi no", "ghi co", "so du", "số dư",
        "sd:", "ps no", "ps co", "cong ", "trich ",
        "vnd", " đ ", "bien dong", "biến động",
    )

    // ─── API chính ───────────────────────────────────────────────────────────

    fun parse(title: String, content: String): ParsedBankTransaction? {
        val fullText = "$title $content"
        val lowerText = fullText.lowercase()

        val bankName = detectBank(fullText) ?: return null

        if (isPromotional(lowerText)) return null
        if (!isTransactionNotification(lowerText)) return null

        val (amount, type) = parseAmountAndType(fullText) ?: return null
        if (amount <= 0) return null

        val balance = parseBalance(fullText)
        val description = parseDescription(fullText)
            ?: buildDefaultDescription(bankName, type)

        return ParsedBankTransaction(
            amount      = amount,
            type        = type,
            bankName    = bankName,
            description = description,
            balance     = balance,
            rawText     = fullText
        )
    }

    private fun detectBank(text: String): String? =
        BANK_KEYWORDS.entries.firstOrNull { (_, keywords) ->
            keywords.any { text.contains(it, ignoreCase = true) }
        }?.key

    private fun isPromotional(lowerText: String): Boolean {
        val hasPromo = PROMO_KEYWORDS.any { lowerText.contains(it) }
        val hasTransaction = TRANSACTION_KEYWORDS.any { lowerText.contains(it) }
        return hasPromo && !hasTransaction
    }

    private fun isTransactionNotification(lowerText: String): Boolean =
        TRANSACTION_KEYWORDS.any { lowerText.contains(it) }

    private fun parseAmountAndType(text: String): Pair<Double, TransactionType>? {
        SIGNED_AMOUNT.find(text)?.let { match ->
            val sign   = match.groupValues[1]
            val amount = parseAmount(match.groupValues[2]) ?: return@let
            val type   = if (sign == "+") TransactionType.INCOME else TransactionType.EXPENSE
            return Pair(amount, type)
        }

        DEBIT_KEYWORDS.find(text)?.let  { return Pair(parseAmount(it.groupValues[1]) ?: return@let, TransactionType.EXPENSE) }
        CREDIT_KEYWORDS.find(text)?.let { return Pair(parseAmount(it.groupValues[1]) ?: return@let, TransactionType.INCOME) }
        MB_CREDIT.find(text)?.let { return Pair(parseAmount(it.groupValues[1]) ?: return@let, TransactionType.INCOME) }
        MB_DEBIT.find(text)?.let  { return Pair(parseAmount(it.groupValues[1]) ?: return@let, TransactionType.EXPENSE) }
        AGB_CREDIT.find(text)?.let { return Pair(parseAmount(it.groupValues[1]) ?: return@let, TransactionType.INCOME) }
        AGB_DEBIT.find(text)?.let  { return Pair(parseAmount(it.groupValues[1]) ?: return@let, TransactionType.EXPENSE) }

        return null
    }

    private fun parseBalance(text: String): Double? =
        BALANCE_REGEX.find(text)?.let { parseAmount(it.groupValues[1]) }

    private fun parseDescription(text: String): String? =
        DESCRIPTION_REGEX.find(text)?.groupValues?.get(1)?.trim()?.take(100)

    private fun buildDefaultDescription(bank: String, type: TransactionType): String =
        if (type == TransactionType.INCOME) "Thu nhập từ $bank" else "Chi tiêu tại $bank"

    private fun parseAmount(raw: String): Double? {
        if (raw.isBlank()) return null
        return try {
            val cleaned = raw.replace(",", "").replace(".", "")
            val value = cleaned.toDouble()
            if (value <= 0) null else value
        } catch (e: NumberFormatException) {
            null
        }
    }
}