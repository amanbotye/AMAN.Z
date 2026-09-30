package ye.aman.admin.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    data object Dashboard : Screen("dashboard", "الرئيسية")
    data object Customers : Screen("customers", "العملاء")
    data object Numbers : Screen("numbers", "الأرقام")
    data object Operations : Screen("operations", "التشغيل والمهام")
    data object Settings : Screen("settings", "النظام والإعدادات")
}
