package com.app.nebulaiqtask.presentation.feature.home.components.permissionbanner

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

data class PermissionBannerPreviewData(
    val hasLocationPermission: Boolean,
    val hasNotificationPermission: Boolean
)

class PermissionRationaleBannerPreviewProvider : PreviewParameterProvider<PermissionBannerPreviewData> {
    override val values: Sequence<PermissionBannerPreviewData> = sequenceOf(
        PermissionBannerPreviewData(hasLocationPermission = false, hasNotificationPermission = false),
        PermissionBannerPreviewData(hasLocationPermission = true, hasNotificationPermission = false)
    )
}
