package com.fintrack.project.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Tien ich xu ly danh muc (mau va icon).
 * Phu thuoc: Material Icons.
 * Duoc su dung boi cac man hinh danh muc/bieu do.
 */
object CategoryUtils {
    /**
     * Chuyen mau string sang Color.
     * @param colorString Chuoi mau dang #RRGGBB.
     * @param defaultColor Mau mac dinh khi loi.
     * @return Color da parse.
     */
    fun resolveCategoryColor(colorString: String?, defaultColor: Color): Color {
        return try {
            if (colorString != null && colorString.startsWith("#")) {
                Color(android.graphics.Color.parseColor(colorString))
            } else defaultColor
        } catch (e: Exception) {
            defaultColor
        }
    }

    /**
     * Chon icon theo khoa danh muc.
     * @param iconKey Khoa icon hoac ten danh muc.
     * @return ImageVector tuong ung.
     */
    fun resolveCategoryIcon(iconKey: String?): ImageVector {
        return when (iconKey) {
            "ic_food", "Ăn uống" -> Icons.Default.Fastfood
            "ic_school", "Giáo dục" -> Icons.Default.School
            "ic_hospital", "Sức khỏe", "Y tế" -> Icons.Default.LocalHospital
            "ic_movie", "Giải trí" -> Icons.Default.Movie
            "ic_car", "Giao thông", "Di chuyển" -> Icons.Default.DirectionsCar
            "ic_home", "Nhà ở" -> Icons.Default.Home
            "ic_shopping", "Mua sắm" -> Icons.Default.ShoppingCart
            "ic_money", "Lương" -> Icons.Default.AttachMoney
            "ic_gift", "Thưởng" -> Icons.Default.CardGiftcard
            "ic_store", "Kinh doanh" -> Icons.Default.Store
            "ic_trending_up", "Đầu tư" -> Icons.Default.TrendingUp
            else -> Icons.Default.MoreHoriz
        }
    }
}
