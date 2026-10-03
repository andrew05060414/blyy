package com.azurlane.blyy.ui.screens.config

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.compose.material3.AlertDialog
import com.azurlane.blyy.data.model.ApiConfig
import com.azurlane.blyy.data.model.JiuxinPreset
import com.azurlane.blyy.data.model.PersonaConfig
import com.azurlane.blyy.data.model.Ship
import com.azurlane.blyy.ui.components.AdaptiveScreenBackground
import com.azurlane.blyy.ui.components.BlyyBottomSheet
import com.azurlane.blyy.ui.components.BlyyPanel
import com.azurlane.blyy.ui.components.BlyyPrimaryButton
import com.azurlane.blyy.ui.components.BlyyEntrance
import com.azurlane.blyy.ui.components.RobustAvatar
import com.azurlane.blyy.ui.screens.chat.AvatarPickerSheet
import com.azurlane.blyy.ui.components.BlyySectionPanel
import com.azurlane.blyy.ui.components.BlyyTopBar
import com.azurlane.blyy.ui.components.StableOutlinedTextField
import com.azurlane.blyy.ui.theme.AppSpacing
import com.azurlane.blyy.ui.theme.AppTypography
import com.azurlane.blyy.ui.theme.ChatColors
import com.azurlane.blyy.ui.theme.LocalIsDark
import com.azurlane.blyy.util.findActivityViewModelStoreOwner
import com.azurlane.blyy.viewmodel.ConnectionTestState
import com.azurlane.blyy.viewmodel.JiuxinViewModel
import com.azurlane.blyy.viewmodel.ModelListState
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceShipPickerSheet(
    viewModel: JiuxinViewModel,
    currentShipName: String,
    onDismiss: () -> Unit,
    onShipSelected: (Ship) -> Unit
) {
    val filteredShips by viewModel.filteredShipList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.shipSearchQuery.collectAsStateWithLifecycle()

    BlyyBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().height(480.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.Lg, vertical = AppSpacing.Md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "选择舰娘语音", style = AppTypography.TitleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm)) {
                    if (currentShipName.isNotBlank()) {
                        Text(
                            text = "清除",
                            style = AppTypography.BodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.clickable {
                                viewModel.saveVoiceShipName("")
                                viewModel.saveVoiceShipAvatar("")
                                onDismiss()
                            }.padding(AppSpacing.Sm)
                        )
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, contentDescription = "关闭") }
                }
            }

            StableOutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setShipSearchQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.Lg),
                placeholder = { Text("搜索舰娘名称...") },
                singleLine = true,
                textStyle = AppTypography.BodyMedium,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(AppSpacing.Corner.Sm)
            )

            Spacer(modifier = Modifier.height(AppSpacing.Sm))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(filteredShips, key = { it.name }) { ship ->
                        val isSelected = ship.name == currentShipName
                        // 啾信功能仅使用网络加载获取的头像
                        val effectiveAvatar = ship.avatarUrl
                        Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(AppSpacing.Corner.Md))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent)
                            .clickable { onShipSelected(ship) }
                            .padding(horizontal = AppSpacing.Lg, vertical = AppSpacing.Sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Md)
                    ) {
                        RobustAvatar(
                            url = effectiveAvatar,
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                                .border(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), CircleShape),
                            fallbackContent = {
                                Icon(Icons.Rounded.SmartToy, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = ship.name, style = AppTypography.TitleSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = "${ship.type} · ${ship.faction}", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (isSelected) {
                            Icon(Icons.Rounded.Check, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
