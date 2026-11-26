package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BreakSage
import com.example.util.AmbientSoundType
import com.example.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmbientSoundBar(
    selectedSound: AmbientSoundType,
    volume: Float,
    strings: AppStrings,
    onSoundSelected: (AmbientSoundType) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDrawer by remember { mutableStateOf(false) }
    val isPlaying = selectedSound != AmbientSoundType.NONE
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Animated sound equalizer bar heights
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_bars")
    val barHeight1 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )
    val barHeight2 by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )
    val barHeight3 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )

    // Main Trigger Card (Sekmece / Çekmece Tetikleyici Kart)
    val ambientCardShape = RoundedCornerShape(18.dp)
    val ambientCardInteraction = remember { MutableInteractionSource() }
    Card(
        shape = ambientCardShape,
        modifier = modifier
            .fillMaxWidth()
            .glassMorphism(
                shape = ambientCardShape,
                accentColor = if (isPlaying) BreakSage else null,
                accentAlpha = if (isPlaying) 0.06f else 0f,
                elevated = isPlaying,
                borderWidth = 1.1.dp,
                shadowElevation = 10.dp,
                interactionSource = ambientCardInteraction,
                pressScale = 0.975f,
                pressDepth = 2.dp
            )
            .clickable(
                interactionSource = ambientCardInteraction,
                indication = LocalIndication.current
            ) { showDrawer = true }
            .testTag("ambient_sound_card"),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Left Icon / Sound Equalizer Box
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isPlaying) BreakSage.copy(alpha = 0.18f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(barHeight1.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(BreakSage)
                            )
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(barHeight2.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(BreakSage)
                            )
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(barHeight3.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(BreakSage)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = strings.ambientTitle,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isPlaying) strings.ambientPlaying else strings.ambientTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPlaying) BreakSage else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isPlaying) {
                            Text(
                                text = " • %${(volume * 100).toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Text(
                        text = if (isPlaying) selectedSound.getDisplayName(strings)
                               else strings.ambientSelectPrompt,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right action buttons: Mute (if playing) + Arrow to open Drawer
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPlaying) {
                    val quickMuteInteraction = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = { onSoundSelected(AmbientSoundType.NONE) },
                        interactionSource = quickMuteInteraction,
                        modifier = Modifier
                            .size(36.dp)
                            .floatingPressEffect(
                                interactionSource = quickMuteInteraction,
                                pressScale = 0.90f,
                                pressDepth = 2.dp
                            )
                            .testTag("ambient_quick_mute_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = strings.ambientMuteButton,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = strings.ambientDrawerTitle,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet / Drawer for Ambient Sounds
    if (showDrawer) {
        ModalBottomSheet(
            onDismissRequest = { showDrawer = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.testTag("ambient_sounds_drawer")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.ambientDrawerTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.ambientDrawerSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showDrawer = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Volume & Quick Mute Bar inside Drawer
                val volumeShape = RoundedCornerShape(14.dp)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassMorphism(
                            shape = volumeShape,
                            borderWidth = 1.dp,
                            shadowElevation = 4.dp
                        ),
                    shape = volumeShape,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (isPlaying) onSoundSelected(AmbientSoundType.NONE)
                                else onSoundSelected(AmbientSoundType.RAIN)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = strings.ambientMuteButton,
                                tint = if (isPlaying) BreakSage else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Slider(
                            value = volume,
                            onValueChange = { newVol ->
                                onVolumeChanged(newVol)
                                if (newVol <= 0.01f && isPlaying) {
                                    onSoundSelected(AmbientSoundType.NONE)
                                } else if (newVol > 0.01f && !isPlaying) {
                                    onSoundSelected(AmbientSoundType.RAIN)
                                }
                            },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = BreakSage,
                                activeTrackColor = BreakSage
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp)
                                .testTag("drawer_volume_slider")
                        )

                        Text(
                            text = "%${(volume * 100).toInt()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Sounds List / Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AmbientSoundType.values().forEach { sound ->
                        val isSelected = selectedSound == sound
                        val isMuteOption = sound == AmbientSoundType.NONE

                        val soundCardShape = RoundedCornerShape(16.dp)
                        val soundCardInteraction = remember { MutableInteractionSource() }
                        Card(
                            shape = soundCardShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassMorphism(
                                    shape = soundCardShape,
                                    accentColor = if (isSelected) BreakSage else null,
                                    accentAlpha = if (isSelected) 0.08f else 0f,
                                    elevated = isSelected,
                                    borderWidth = 1.dp,
                                    shadowElevation = 5.dp,
                                    interactionSource = soundCardInteraction,
                                    pressScale = 0.975f,
                                    pressDepth = 2.dp
                                )
                                .clickable(
                                    interactionSource = soundCardInteraction,
                                    indication = LocalIndication.current
                                ) {
                                    if (isSelected && !isMuteOption) {
                                        onSoundSelected(AmbientSoundType.NONE)
                                    } else {
                                        onSoundSelected(sound)
                                    }
                                }
                                .testTag("drawer_sound_card_${sound.name.lowercase()}"),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) BreakSage.copy(alpha = 0.18f)
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = sound.getVectorIcon(),
                                            contentDescription = null,
                                            tint = if (isSelected) BreakSage else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = sound.getDisplayName(strings),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                            ),
                                            color = if (isSelected) BreakSage else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = sound.getDescription(strings),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (isSelected && !isMuteOption) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = BreakSage.copy(alpha = 0.2f),
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.GraphicEq,
                                                contentDescription = null,
                                                tint = BreakSage,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = strings.taskActiveBadge,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = BreakSage
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Done / Dismiss Button
                val closeDrawerInteraction = remember { MutableInteractionSource() }
                val closeDrawerShape = RoundedCornerShape(14.dp)
                Button(
                    onClick = { showDrawer = false },
                    interactionSource = closeDrawerInteraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .coloredGlassButton(
                            containerColor = MaterialTheme.colorScheme.primary,
                            shape = closeDrawerShape,
                            interactionSource = closeDrawerInteraction,
                            shadowElevation = 9.dp,
                            pressScale = 0.95f,
                            pressDepth = 3.dp
                        )
                        .testTag("close_ambient_drawer_button"),
                    shape = closeDrawerShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    Text(
                        text = strings.settingsDismiss,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
