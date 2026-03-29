package com.fintrack.project.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryUtils {
    fun resolveCategoryColor(colorString: String?, defaultColor: Color): Color {
        return try {
            if (colorString != null && colorString.startsWith("#")) {
                Color(android.graphics.Color.parseColor(colorString))
            } else defaultColor
        } catch (e: Exception) {
            defaultColor
        }
    }

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
