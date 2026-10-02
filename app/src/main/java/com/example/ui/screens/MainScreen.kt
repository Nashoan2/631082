package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import kotlin.system.exitProcess
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.ripple
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import com.example.ui.components.AlmamlakaLogoBadge
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import com.example.data.ButtonLayout
import com.example.data.ButtonSize
import com.example.data.AppActionButton
import com.example.data.UiCustomizationConfig
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.mandatoryTextFieldColors
import com.example.ui.theme.LocalShadedFieldColor
import com.example.ui.theme.LocalShadedFieldBorder
import com.example.ui.theme.optionalYellowTextFieldColors
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.util.ArabicNumberHelper
import com.example.util.PrintHelper

@Composable
fun MainScreen(viewModel: InvoiceViewModel) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  val scrollState = rememberScrollState()

  LaunchedEffect(uiState.toastMessage) {
    uiState.toastMessage?.let { msg ->
      Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
      viewModel.clearToast()
    }
  }

  val currentTheme = uiState.uiCustomizationConfig.currentTheme()
  val themePrimary = parseHexColor(uiState.uiCustomizationConfig.customPrimaryColorHex.ifBlank { currentTheme.primaryHex })
  val themeSecondary = parseHexColor(uiState.uiCustomizationConfig.customSecondaryColorHex.ifBlank { currentTheme.secondaryHex })
  val themeBg = parseHexColor(uiState.uiCustomizationConfig.customBgColorHex.ifBlank { currentTheme.bgHex })
  val themeAccent = parseHexColor(currentTheme.accentHex)
  val isDarkTheme = currentTheme.id == "DARK_LUXURY" || currentTheme.id == "MIDNIGHT_BLACK"

  val bgColors = if (isDarkTheme) {
    listOf(themeBg, Color(0xFF111827), Color(0xFF050508))
  } else {
    listOf(
      Color(
        red = (themePrimary.red * 0.22f + 0.16f).coerceIn(0f, 1f),
        green = (themePrimary.green * 0.22f + 0.18f).coerceIn(0f, 1f),
        blue = (themePrimary.blue * 0.22f + 0.22f).coerceIn(0f, 1f)
      ),
      Color(0xFF262C36),
      Color(0xFF1E232B)
    )
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = bgColors
        )
      )
      .statusBarsPadding()
      .navigationBarsPadding()
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
      // 1. ترويسة الشاشة الرئيسية (شعار المملكة محاط بإطار نيون متوهج على اليسار، واسم المتجر بخط أبيض عريض مع خط نيون على اليمين)
      CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 18.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // شعار المتجر داخل إطار نيون دائري مشع (على اليسار) - ينقل إلى الإعدادات عند الضغط عليه
          val logoInteraction = remember { MutableInteractionSource() }
          val logoPressed by logoInteraction.collectIsPressedAsState()
          val logoScale by animateFloatAsState(
            targetValue = if (logoPressed) 0.90f else 1.0f,
            animationSpec = spring(stiffness = Spring.StiffnessHigh)
          )
          Box(
            modifier = Modifier
              .graphicsLayer {
                scaleX = logoScale
                scaleY = logoScale
              }
              .size(86.dp)
              .shadow(
                elevation = if (logoPressed) 6.dp else 16.dp,
                shape = CircleShape,
                spotColor = themeSecondary,
                ambientColor = themePrimary
              )
              .background(
                Brush.sweepGradient(
                  listOf(
                    themeSecondary,
                    themePrimary,
                    themeAccent,
                    themeSecondary
                  )
                ),
                CircleShape
              )
              .clip(CircleShape)
              .clickable(
                interactionSource = logoInteraction,
                indication = ripple(bounded = false, radius = 45.dp, color = themeSecondary)
              ) { viewModel.setSettingsModalVisible(true) }
              .padding(3.dp),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF04091A), CircleShape)
                .padding(2.dp),
              contentAlignment = Alignment.Center
            ) {
              AlmamlakaLogoBadge(
                size = 76.dp,
                logoBase64 = uiState.storeConfig.logoBase64
              )
            }
          }

          // اسم المتجر مع السنة المالية في الترويسة
          Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(end = 4.dp)
          ) {
            // محدد السنة المالية الحالية مع زر الحفظ عند التبديل
            var selectedYearInHeader by remember(uiState.activeFiscalYear) { mutableStateOf(uiState.activeFiscalYear) }
            var yearMenuExpanded by remember { mutableStateOf(false) }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              // إذا تم اختيار سنة أخرى غير النشطة: يظهر زر "حفظ" فوراً
              if (selectedYearInHeader != uiState.activeFiscalYear) {
                val saveInteraction = remember { MutableInteractionSource() }
                val savePressed by saveInteraction.collectIsPressedAsState()
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color(0xFF15803D),
                  border = BorderStroke(1.2.dp, Color(0xFF86EFAC)),
                  shadowElevation = if (savePressed) 1.dp else 4.dp,
                  modifier = Modifier
                    .height(29.dp)
                    .clickable(
                      interactionSource = saveInteraction,
                      indication = ripple(color = Color.White)
                    ) {
                      viewModel.switchFiscalYear(selectedYearInHeader)
                    }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Icon(Icons.Default.Save, contentDescription = "حفظ", tint = Color.White, modifier = Modifier.size(14.dp))
                    Text(
                      text = "حفظ",
                      color = Color.White,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Black
                    )
                  }
                }
              }

              // شارة وقائمة السنة المالية (تظهر السنة الحالية والسنوات المقفلة فقط)
              Box {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (selectedYearInHeader != uiState.activeFiscalYear) Color(0x40F59E0B) else Color(0x3300F2FE),
                  border = BorderStroke(
                    1.dp,
                    if (selectedYearInHeader != uiState.activeFiscalYear) Color(0xFFF59E0B) else themeSecondary.copy(alpha = 0.7f)
                  ),
                  modifier = Modifier
                    .height(29.dp)
                    .clickable { yearMenuExpanded = true }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Text("📅", fontSize = 11.sp)
                    Text(
                      text = "$selectedYearInHeader",
                      color = Color.White,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Black
                    )
                    Text("▾", color = themeSecondary, fontSize = 11.sp)
                  }
                }

                DropdownMenu(
                  expanded = yearMenuExpanded,
                  onDismissRequest = { yearMenuExpanded = false }
                ) {
                  uiState.availableFiscalYears.forEach { yr ->
                    DropdownMenuItem(
                      text = {
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                          Text(
                            text = "$yr",
                            fontWeight = if (yr == selectedYearInHeader) FontWeight.Black else FontWeight.Bold,
                            color = if (yr == uiState.activeFiscalYear) Color(0xFF16A34A) else Color.Unspecified,
                            fontSize = 14.sp
                          )
                          if (yr == uiState.activeFiscalYear) {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFDCFCE7)) {
                              Text(
                                text = "الحالية",
                                fontSize = 9.sp,
                                color = Color(0xFF15803D),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                              )
                            }
                          } else {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFEF2F2)) {
                              Text(
                                text = "مقفلة",
                                fontSize = 9.sp,
                                color = Color(0xFFB91C1C),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                              )
                            }
                          }
                        }
                      },
                      onClick = {
                        selectedYearInHeader = yr
                        yearMenuExpanded = false
                      }
                    )
                  }
                }
              }
            }

            // اسم المتجر (ينقل إلى الإعدادات عند الضغط عليه)
            Text(
              text = uiState.storeConfig.storeNameAr.ifBlank { "المملكة للإلكترونيات" },
              color = Color.White,
              fontSize = 24.sp,
              fontWeight = FontWeight.Black,
              textAlign = TextAlign.End,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { viewModel.setSettingsModalVisible(true) }
            )

            // خط النيون الأفقي المتوهج أسفل العنوان
            Box(
              modifier = Modifier
                .width(180.dp)
                .height(3.5.dp)
                .background(
                  Brush.horizontalGradient(
                    colors = listOf(
                      themeSecondary,
                      themePrimary,
                      themeAccent
                    )
                  ),
                  RoundedCornerShape(2.dp)
                )
            )
          }
        }
      }

      // إذا كانت استمارة الفاتورة مغلقة: نعرض الواجهة الرئيسية المطابقة تماماً للصورة
      if (!uiState.isFormVisible) {
        // 2. شبكة الاختصارات السريعة (2x2) مطابقة تماماً للصورة
        val allConfigShortcuts = uiState.uiCustomizationConfig.buttons.filter { it.isQuickShortcut }
        val defaultFourIds = listOf("CURRENCY_CONVERTER", "ALL_CUSTOMERS", "PERCENTAGE_CALCULATOR", "FULL_CALCULATOR")
        val activeShortcuts = if (allConfigShortcuts.isNotEmpty()) {
          // ترتيب الاختصارات لضمان ظهور الـ 4 الأساسية بنفس ترتيب الصورة
          val sorted = allConfigShortcuts.sortedBy { btn ->
            val idx = defaultFourIds.indexOf(btn.id)
            if (idx >= 0) idx else 99
          }
          sorted
        } else {
          UiCustomizationConfig.defaultButtons().filter { it.isQuickShortcut }
        }

        if (uiState.uiCustomizationConfig.showQuickShortcutsBar && activeShortcuts.isNotEmpty()) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            activeShortcuts.chunked(2).forEach { rowShortcuts ->
              CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  rowShortcuts.forEach { shortcut ->
                    QuickShortcutCard(
                      shortcut = shortcut,
                      modifier = Modifier.weight(1f),
                      onClick = { viewModel.executeActionButton(shortcut, context) }
                    )
                  }
                  if (rowShortcuts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                  }
                }
              }
            }
          }
        }

        // 3. أزرار العمليات الرئيسية (كبسولات ثلاثية الأبعاد بلمعة زجاجية وأيقونات كروية مطابقة للصورة)
        val visibleButtons = uiState.uiCustomizationConfig.buttons.filter { it.isVisible }
        val activeButtons = if (visibleButtons.isNotEmpty()) visibleButtons else UiCustomizationConfig.defaultButtons().filter { it.isVisible }

        val btnHeight = if (uiState.uiCustomizationConfig.buttonSize.heightDp > 0) {
          uiState.uiCustomizationConfig.buttonSize.heightDp.dp
        } else 54.dp

        if (uiState.uiCustomizationConfig.buttonLayout == ButtonLayout.DOUBLE) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            activeButtons.chunked(2).forEach { pair ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                pair.forEach { btn ->
                  MainAppActionButton(
                    button = btn,
                    height = btnHeight,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.executeActionButton(btn, context) }
                  )
                }
                if (pair.size == 1) {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }
          }
        } else {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            activeButtons.forEach { btn ->
              MainAppActionButton(
                button = btn,
                height = btnHeight,
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.executeActionButton(btn, context) }
              )
            }
          }
        }
      } else {
        // عند فتح استمارة إدخال أو تعديل الفاتورة: تظهر داخل بطاقة حديثة ونظيفة مع زر إغلاق للرجوع للرئيسية
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF1)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCFD8DC)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Text(
                    text = if (uiState.editingInvoiceId != null) "✏️ تعديل الفاتورة (${uiState.invNum})" else "📋 إدخال بيانات الفاتورة",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111827)
                  )
                }
                IconButton(onClick = { viewModel.closeInvoiceForm() }) {
                  Icon(Icons.Default.Close, contentDescription = "إغلاق والرجوع للرئيسية", tint = Color(0xFF6B7280))
                }
              }
              HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = Color(0xFFE5E7EB)
              )
              InvoiceFormSection(viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}

@Composable
fun FormFieldLabel(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text,
    fontSize = 13.sp,
    fontWeight = FontWeight.Bold,
    color = Color(0xFF212529),
    modifier = modifier.padding(bottom = 4.dp)
  )
}

@Composable
fun StyledInputContainer(
  modifier: Modifier = Modifier,
  isYellowTheme: Boolean = true,
  onClick: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  val dynamicShadedColor = LocalShadedFieldColor.current
  val dynamicShadedBorder = LocalShadedFieldBorder.current

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(46.dp)
      .background(dynamicShadedColor, RoundedCornerShape(8.dp))
      .border(1.2.dp, dynamicShadedBorder, RoundedCornerShape(8.dp))
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
      .padding(horizontal = 10.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    content()
  }
}

@Composable
fun InvoiceFormSection(viewModel: InvoiceViewModel) {
  val uiState by viewModel.uiState.collectAsState()
  var invTypeMenuExpanded by remember { mutableStateOf(false) }
  var subTypeMenuExpanded by remember { mutableStateOf(false) }
  var currencyMenuExpanded by remember { mutableStateOf(false) }
  var showCustomerPickerDialog by remember { mutableStateOf(false) }
  var customerSearchQuery by remember { mutableStateOf("") }

  val configFields = uiState.uiCustomizationConfig.formFields
  fun isFieldVisible(id: String): Boolean {
    val f = configFields.find { it.id == id }
    return f?.isVisible ?: true
  }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 1. الصف الأول: نوع الفاتورة | رقم الفاتورة | العملة
    if (isFieldVisible("INV_HEADER")) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // نوع الفاتورة (Right in RTL)
        Column(modifier = Modifier.weight(1f)) {
          FormFieldLabel(text = "نوع الفاتورة")
          Box(modifier = Modifier.fillMaxWidth()) {
            StyledInputContainer(
              isYellowTheme = false,
              onClick = { invTypeMenuExpanded = true }
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = "قائمة نوع الفاتورة",
                  tint = Color(0xFF212529)
                )
                Text(
                  text = uiState.invType,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF212529)
                )
              }
            }
            DropdownMenu(
              expanded = invTypeMenuExpanded,
              onDismissRequest = { invTypeMenuExpanded = false }
            ) {
              DropdownMenuItem(
                text = { Text("نقداً", fontWeight = FontWeight.Bold) },
                onClick = {
                  viewModel.updateInvType("نقداً")
                  invTypeMenuExpanded = false
                }
              )
              DropdownMenuItem(
                text = { Text("أجل", fontWeight = FontWeight.Bold) },
                onClick = {
                  viewModel.updateInvType("أجل")
                  invTypeMenuExpanded = false
                }
              )
            }
          }
        }

        // رقم الفاتورة (Center in RTL)
        Column(modifier = Modifier.weight(1f)) {
          FormFieldLabel(text = "رقم الفاتورة")
          StyledInputContainer(isYellowTheme = false) {
            BasicTextField(
              value = uiState.invNum,
              onValueChange = { viewModel.updateInvNum(it) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212529),
                textAlign = TextAlign.Center
              ),
              cursorBrush = SolidColor(Color(0xFF5E258D)),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        // العملة (Left in RTL - مرتبة ريال يمني رقم 1)
        Column(modifier = Modifier.weight(1f)) {
          FormFieldLabel(text = "العملة")
          Box(modifier = Modifier.fillMaxWidth()) {
            val currLabel = when (uiState.currency) {
              "YER" -> "YER"
              "$" -> "USD"
              "SAR" -> "SAR"
              else -> uiState.currency
            }
            StyledInputContainer(
              isYellowTheme = false,
              onClick = { currencyMenuExpanded = true }
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = "قائمة العملة",
                  tint = Color(0xFF212529)
                )
                Text(
                  text = currLabel,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF212529)
                )
              }
            }
            DropdownMenu(
              expanded = currencyMenuExpanded,
              onDismissRequest = { currencyMenuExpanded = false }
            ) {
              // ريال يمني رقم واحد كما في الصورة والطلب
              DropdownMenuItem(
                text = { Text("YER (ريال يمني)", fontWeight = FontWeight.Bold) },
                onClick = {
                  viewModel.updateCurrency("YER")
                  currencyMenuExpanded = false
                }
              )
              DropdownMenuItem(
                text = { Text("($) USD (دولار)", fontWeight = FontWeight.Bold) },
                onClick = {
                  viewModel.updateCurrency("$")
                  currencyMenuExpanded = false
                }
              )
              DropdownMenuItem(
                text = { Text("(SR) SAR (ريال سعودي)", fontWeight = FontWeight.Bold) },
                onClick = {
                  viewModel.updateCurrency("SAR")
                  currencyMenuExpanded = false
                }
              )
            }
          }
        }
      }

      // تاريخ الفاتورة (يظهر فقط عند الضغط على تعديل فاتورة سابقة)
      if (uiState.editingInvoiceId != null) {
        Column(modifier = Modifier.fillMaxWidth()) {
          FormFieldLabel(text = "📅 تاريخ الفاتورة (قابل للتعديل)")
          StyledInputContainer(isYellowTheme = true) {
            BasicTextField(
              value = uiState.invoiceDate,
              onValueChange = { viewModel.updateInvoiceDate(it) },
              singleLine = true,
              textStyle = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5E258D),
                textAlign = TextAlign.Start
              ),
              cursorBrush = SolidColor(Color(0xFF5E258D)),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }

    // 2. اسم العميل ورقم الحساب (في الفاتورة الآجل: رقم الحساب أولاً واسم المشترك تحته)
    if (isFieldVisible("CUSTOMER_NAME")) {
      if (uiState.invType == "أجل") {
        // --- 1. أولاً: رقم حساب العميل ---
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            FormFieldLabel(text = "رقم حساب العميل")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              // زر فتح دليل العملاء
              Surface(
                onClick = {
                  customerSearchQuery = ""
                  showCustomerPickerDialog = true
                },
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFEFF6FF),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = Color(0xFF1D4ED8),
                    modifier = Modifier.size(14.dp)
                  )
                  Text(
                    text = "دليل العملاء",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D4ED8)
                  )
                }
              }

              // زر توليد رقم حساب جديد
              Surface(
                onClick = { viewModel.generateNewCustomerAccount() },
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFFEF3C7),
                border = BorderStroke(1.dp, Color(0xFFFDE68A))
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Text("⚡", fontSize = 12.sp)
                  Text(
                    text = "رقم جديد",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E)
                  )
                }
              }
            }
          }
          StyledInputContainer(isYellowTheme = true) {
            BasicTextField(
              value = uiState.customerAccount,
              onValueChange = { viewModel.updateCustomerAccount(it) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212529),
                textAlign = TextAlign.Start
              ),
              cursorBrush = SolidColor(Color(0xFF5E258D)),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        // --- 2. اسم المشترك / العميل ---
        Column(modifier = Modifier.fillMaxWidth()) {
          FormFieldLabel(text = "اسم المشترك / العميل")
          StyledInputContainer(isYellowTheme = true) {
            BasicTextField(
              value = uiState.customerName,
              onValueChange = { viewModel.updateCustomerName(it) },
              singleLine = true,
              textStyle = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212529),
                textAlign = TextAlign.Start
              ),
              cursorBrush = SolidColor(Color(0xFF5E258D)),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        // بطاقة الرصيد وصاحب الحساب
        val matchedCustomer = if (uiState.customerAccount.isNotBlank()) {
          uiState.customers.find {
            com.example.util.ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == com.example.util.ArabicNumberHelper.toEngDigits(uiState.customerAccount).trim()
          }
        } else if (uiState.customerName.isNotBlank()) {
          uiState.customers.find {
            it.name.trim().equals(uiState.customerName.trim(), ignoreCase = true)
          }
        } else null

        if (matchedCustomer != null) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            border = BorderStroke(1.dp, Color(0xFF81C784))
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("👤 صاحب الحساب:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF333333))
                Text(matchedCustomer.name, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF1B5E20))
              }
              HorizontalDivider(thickness = 0.8.dp, color = Color(0xFFA5D6A7))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("💰 الرصيد الحالي للعميل:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF333333))
                val bal = matchedCustomer.balance
                val statusText = if (bal > 0) " (عليه دين سابق)" else if (bal < 0) " (له رصيد دائن)" else " (متزن / 0)"
                val balColor = if (bal > 0) Color(0xFFC62828) else if (bal < 0) Color(0xFF2E7D32) else Color(0xFF455A64)
                Text(
                  text = "${com.example.util.ArabicNumberHelper.formatAmount(bal)} ${uiState.currency}$statusText",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 14.sp,
                  color = balColor
                )
              }
            }
          }
        }
      } else {
        // فاتورة نقداً: اسم العميل / الحساب فقط
        Column(modifier = Modifier.fillMaxWidth()) {
          FormFieldLabel(text = "اسم العميل / المشترك")
          StyledInputContainer(isYellowTheme = true) {
            BasicTextField(
              value = uiState.customerName,
              onValueChange = { viewModel.updateCustomerName(it) },
              singleLine = true,
              textStyle = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212529),
                textAlign = TextAlign.Start
              ),
              cursorBrush = SolidColor(Color(0xFF5E258D)),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }

    // 3. رقم الاشتراك / الكارت | سعر الاشتراك
    if (isFieldVisible("CARD_AND_PRICE") || isFieldVisible("CARD_ID")) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // رقم الاشتراك / الكارت (Right in RTL)
        Column(modifier = Modifier.weight(1f)) {
          FormFieldLabel(text = "رقم الاشتراك / الكارت")
          StyledInputContainer(isYellowTheme = true) {
            BasicTextField(
              value = uiState.cardId,
              onValueChange = { viewModel.updateCardId(it) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              textStyle = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212529),
                textAlign = TextAlign.Start
              ),
              cursorBrush = SolidColor(Color(0xFF5E258D)),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        // سعر الاشتراك (Left in RTL)
        Column(modifier = Modifier.weight(1f)) {
          FormFieldLabel(text = "سعر الاشتراك")
          StyledInputContainer(isYellowTheme = true) {
            BasicTextField(
              value = uiState.price,
              onValueChange = { viewModel.updatePrice(it) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              textStyle = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212529),
                textAlign = TextAlign.Start
              ),
              cursorBrush = SolidColor(Color(0xFF5E258D)),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }

    // 4. العدد (الكمية) | نوع التجديد / المدة
    if (isFieldVisible("QTY_AND_TYPE")) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // العدد (الكمية) (Right in RTL)
        Column(modifier = Modifier.weight(1f)) {
          FormFieldLabel(text = "العدد (الكمية)")
          StyledInputContainer(isYellowTheme = true) {
            BasicTextField(
              value = uiState.qty,
              onValueChange = { viewModel.updateQty(it) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212529),
                textAlign = TextAlign.Start
              ),
              cursorBrush = SolidColor(Color(0xFF5E258D)),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        // نوع التجديد / المدة (Left in RTL)
        Column(modifier = Modifier.weight(1f)) {
          FormFieldLabel(text = "نوع التجديد / المدة")
          Box(modifier = Modifier.fillMaxWidth()) {
            val typeLabel = if (uiState.subscriptionType == "months") "أشهر (اشتراك عادي)" else "بطولة"
            StyledInputContainer(
              isYellowTheme = true,
              onClick = { subTypeMenuExpanded = true }
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = "قائمة نوع التجديد",
                  tint = Color(0xFF495057)
                )
                Text(
                  text = typeLabel,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF212529),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
            DropdownMenu(
              expanded = subTypeMenuExpanded,
              onDismissRequest = { subTypeMenuExpanded = false }
            ) {
              DropdownMenuItem(
                text = { Text("أشهر (اشتراك عادي)", fontWeight = FontWeight.Bold) },
                onClick = {
                  viewModel.updateSubscriptionType("months")
                  subTypeMenuExpanded = false
                }
              )
              DropdownMenuItem(
                text = { Text("بطولة (مثل كأس العالم)", fontWeight = FontWeight.Bold) },
                onClick = {
                  viewModel.updateSubscriptionType("tournament")
                  subTypeMenuExpanded = false
                }
              )
            }
          }
        }
      }
    }

    // 5. الوصف الرئيسي
    if (isFieldVisible("DESCRIPTION")) {
      Column(modifier = Modifier.fillMaxWidth()) {
        FormFieldLabel(text = "الوصف الرئيسي")
        StyledInputContainer(isYellowTheme = true) {
          BasicTextField(
            value = uiState.desc,
            onValueChange = { viewModel.updateDesc(it) },
            singleLine = true,
            textStyle = TextStyle(
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF212529),
              textAlign = TextAlign.Start
            ),
            cursorBrush = SolidColor(Color(0xFF5E258D)),
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // 6. تاريخ الانتهاء
    if (isFieldVisible("END_DATE")) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          FormFieldLabel(text = "📅 تاريخ الانتهاء")
          if (uiState.editingInvoiceId != null) {
            Text(
              text = "🔒 تاريخ الانتهاء ثابت لا يتغير عند التعديل",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF2E7D32)
            )
          }
        }
        StyledInputContainer(isYellowTheme = true) {
          BasicTextField(
            value = uiState.endDate,
            onValueChange = {
              if (uiState.editingInvoiceId == null) {
                viewModel.updateEndDate(it)
              }
            },
            readOnly = uiState.editingInvoiceId != null,
            singleLine = true,
            textStyle = TextStyle(
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = if (uiState.editingInvoiceId != null) Color(0xFF2E7D32) else Color(0xFF212529),
              textAlign = TextAlign.Start
            ),
            cursorBrush = SolidColor(Color(0xFF5E258D)),
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // 7. الأصناف الإضافية وأزرار الحفظ والإغلاق
    if (isFieldVisible("EXTRA_ITEMS")) {
      if (uiState.extraItems.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "الأصناف / التفاصيل الإضافية 🧾",
          fontWeight = FontWeight.Bold,
          color = Color(0xFF5E258D),
          fontSize = 15.sp,
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          textAlign = TextAlign.Center
        )

        uiState.extraItems.forEachIndexed { index, item ->
          ExtraItemCard(
            itemNumber = index + 2,
            item = item,
            onUpdate = { desc, price, qty, curr ->
              viewModel.updateExtraItem(item.id, desc, price, qty, curr)
            },
            onDelete = { viewModel.removeExtraItem(item.id) }
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // الصف السفلي للأزرار: زر "حفظ الفاتورة 💾" وزر "+ إضافة تفصيل"
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // حفظ الفاتورة (Right in RTL)
        Button(
          onClick = { viewModel.saveCurrentInvoice() },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E6F38)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1.15f)
            .height(48.dp)
        ) {
          val saveLabel = if (uiState.editingInvoiceId != null) "💾 حفظ التعديلات" else "💾 حفظ الفاتورة"
          Text(saveLabel, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // + إضافة تفصيل (Left in RTL)
        Button(
          onClick = { viewModel.addExtraItem() },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3E8C)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
        ) {
          Text("+ إضافة تفصيل", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // زر إغلاق الفاتورة (كامل العرض باللون الأحمر الغامق كما في الصورة)
      Button(
        onClick = { viewModel.closeInvoiceForm() },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B0303)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text("❌", fontSize = 15.sp)
          Text(
            text = "إغلاق الفاتورة",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Text("❌", fontSize = 15.sp)
        }
      }
    }
  }

  // مربع حوار اختيار عميل مسجل
  if (showCustomerPickerDialog) {
    AlertDialog(
      onDismissRequest = { showCustomerPickerDialog = false },
      title = {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("اختيار عميل مسجل 👥", fontWeight = FontWeight.Bold, fontSize = 17.sp)
          IconButton(onClick = { showCustomerPickerDialog = false }) {
            Icon(Icons.Default.Close, contentDescription = "إغلاق")
          }
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = customerSearchQuery,
            onValueChange = { customerSearchQuery = it },
            placeholder = { Text("بحث بالاسم أو رقم الحساب...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          val filtered = uiState.customers.filter {
            val q = customerSearchQuery.trim()
            if (q.isEmpty()) true
            else it.name.contains(q, ignoreCase = true) || it.accountNumber.contains(q)
          }

          if (filtered.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
              contentAlignment = Alignment.Center
            ) {
              Text("لا توجد نتائج مطابقة", color = Color.Gray, fontSize = 13.sp)
            }
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              items(filtered) { customer ->
                Card(
                  onClick = {
                    viewModel.selectCustomerForInvoice(customer)
                    showCustomerPickerDialog = false
                  },
                  colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                  border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827))
                      Text("رقم الحساب: ${customer.accountNumber}", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                    Text(
                      text = "${com.example.util.ArabicNumberHelper.formatAmount(customer.balance)} ${uiState.currency}",
                      fontWeight = FontWeight.ExtraBold,
                      fontSize = 13.sp,
                      color = if (customer.balance > 0) Color(0xFFDC2626) else if (customer.balance < 0) Color(0xFF16A34A) else Color(0xFF4B5563)
                    )
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showCustomerPickerDialog = false }) {
          Text("إلغاء", fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}

@Composable
fun ExtraItemCard(
  itemNumber: Int,
  item: com.example.data.ExtraItem,
  onUpdate: (String, Double, Double, String) -> Unit,
  onDelete: () -> Unit
) {
  var desc by remember(item.description) { mutableStateOf(item.description) }
  var priceStr by remember(item.price) { mutableStateOf(if (item.price == 0.0) "" else item.price.toString()) }
  var qtyStr by remember(item.qty) { mutableStateOf(if (item.qty == 1.0) "1" else item.qty.toString()) }
  var curr by remember(item.currency) { mutableStateOf(item.currency) }
  var currExpanded by remember { mutableStateOf(false) }

  val shadedBg = LocalShadedFieldColor.current
  val shadedBorder = LocalShadedFieldBorder.current

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
    border = androidx.compose.foundation.BorderStroke(1.2.dp, shadedBorder)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("الصنف رقم $itemNumber", fontWeight = FontWeight.ExtraBold, color = Color(0xFF2B5797))
        IconButton(onClick = onDelete) {
          Icon(Icons.Default.Delete, contentDescription = "Delete item", tint = Color(0xFFDC3545))
        }
      }

      Text("التفصيل", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = desc,
        onValueChange = {
          desc = it
          onUpdate(desc, priceStr.toDoubleOrNull() ?: 0.0, qtyStr.toDoubleOrNull() ?: 1.0, curr)
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)),
        colors = mandatoryTextFieldColors(customBgColor = shadedBg, customBorderColor = shadedBorder)
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(modifier = Modifier.weight(1f)) {
          Text("السعر", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = priceStr,
            onValueChange = {
              priceStr = it
              onUpdate(desc, priceStr.toDoubleOrNull() ?: 0.0, qtyStr.toDoubleOrNull() ?: 1.0, curr)
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)),
            colors = mandatoryTextFieldColors(customBgColor = shadedBg, customBorderColor = shadedBorder)
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text("الكمية", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = qtyStr,
            onValueChange = {
              qtyStr = it
              onUpdate(desc, priceStr.toDoubleOrNull() ?: 0.0, qtyStr.toDoubleOrNull() ?: 1.0, curr)
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)),
            colors = mandatoryTextFieldColors(customBgColor = shadedBg, customBorderColor = shadedBorder)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Column(modifier = Modifier.fillMaxWidth()) {
        Text("العملة", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = curr,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
              IconButton(onClick = { currExpanded = true }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Currency", tint = Color(0xFF0F172A))
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .clickable { currExpanded = true },
            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)),
            colors = mandatoryTextFieldColors(customBgColor = shadedBg, customBorderColor = shadedBorder)
          )
          DropdownMenu(expanded = currExpanded, onDismissRequest = { currExpanded = false }) {
          DropdownMenuItem(text = { Text("YER (ريال يمني - YR)", fontWeight = FontWeight.Bold) }, onClick = {
            curr = "YER"
            currExpanded = false
            onUpdate(desc, priceStr.toDoubleOrNull() ?: 0.0, qtyStr.toDoubleOrNull() ?: 1.0, curr)
          })
          DropdownMenuItem(text = { Text("USD ($)", fontWeight = FontWeight.Bold) }, onClick = {
            curr = "$"
            currExpanded = false
            onUpdate(desc, priceStr.toDoubleOrNull() ?: 0.0, qtyStr.toDoubleOrNull() ?: 1.0, curr)
          })
          DropdownMenuItem(text = { Text("SAR (ريال سعودي - SR)", fontWeight = FontWeight.Bold) }, onClick = {
            curr = "SAR"
            currExpanded = false
            onUpdate(desc, priceStr.toDoubleOrNull() ?: 0.0, qtyStr.toDoubleOrNull() ?: 1.0, curr)
          })
        }
      }
    }

      val itemTotal = (priceStr.toDoubleOrNull() ?: 0.0) * (qtyStr.toDoubleOrNull() ?: 1.0)
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "سعر الصنف الكلي: ${ArabicNumberHelper.formatAmount(itemTotal)} $curr",
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFFD32F2F),
        fontSize = 13.sp
      )
    }
  }
}

private fun getButtonGradientsAndBorder(button: AppActionButton): Pair<List<Color>, Color> {
  if (button.colorHex.isNotBlank()) {
    try {
      val clean = button.colorHex.removePrefix("#").trim()
      val baseColor = if (clean.length == 6) {
        Color(0xFF000000 or clean.toLong(16))
      } else if (clean.length == 8) {
        Color(clean.toLong(16))
      } else null

      if (baseColor != null) {
        val isDefault = when (button.id) {
          "NEW_INVOICE" -> clean.equals("3B1E08", true)
          "SAVED_INVOICES" -> clean.equals("361A07", true)
          "OPEN_REPORT" -> clean.equals("283B4F", true)
          "CUSTOMERS" -> clean.equals("4F3BE0", true)
          "RECEIPT_VOUCHER" -> clean.equals("02592F", true)
          "PAYMENT_VOUCHER" -> clean.equals("DC2626", true)
          "ALL_CUSTOMERS" -> clean.equals("1D4ED8", true)
          "SETTINGS" -> clean.equals("182230", true)
          "EXIT_APP" -> clean.equals("9E1212", true)
          else -> false
        }
        if (!isDefault) {
          return Pair(
            listOf(
              baseColor,
              Color(
                (baseColor.red * 0.72f).coerceIn(0f, 1f),
                (baseColor.green * 0.72f).coerceIn(0f, 1f),
                (baseColor.blue * 0.72f).coerceIn(0f, 1f),
                1f
              ),
              Color(
                (baseColor.red * 0.42f).coerceIn(0f, 1f),
                (baseColor.green * 0.42f).coerceIn(0f, 1f),
                (baseColor.blue * 0.42f).coerceIn(0f, 1f),
                1f
              )
            ),
            baseColor
          )
        }
      }
    } catch (_: Exception) {}
  }

  return when (button.id) {
    "NEW_INVOICE" -> Pair(
      listOf(Color(0xFF65330D), Color(0xFF472106), Color(0xFF2B1202)),
      Color(0xFF8B4513)
    )
    "SAVED_INVOICES" -> Pair(
      listOf(Color(0xFF734019), Color(0xFF532B0E), Color(0xFF351A05)),
      Color(0xFF9E5B28)
    )
    "OPEN_REPORT" -> Pair(
      listOf(Color(0xFF0B5D9E), Color(0xFF063F6E), Color(0xFF032542)),
      Color(0xFF1A82D2)
    )
    "CUSTOMERS" -> Pair(
      listOf(Color(0xFF581C87), Color(0xFF3B0F5D), Color(0xFF24063C)),
      Color(0xFF8427D6)
    )
    "RECEIPT_VOUCHER" -> Pair(
      listOf(Color(0xFF059640), Color(0xFF03702E), Color(0xFF01461B)),
      Color(0xFF10B981)
    )
    "PAYMENT_VOUCHER" -> Pair(
      listOf(Color(0xFFDC2626), Color(0xFFA51717), Color(0xFF6B0B0B)),
      Color(0xFFEF4444)
    )
    "ALL_CUSTOMERS" -> Pair(
      listOf(Color(0xFF1D4ED8), Color(0xFF163CA8), Color(0xFF0E256C)),
      Color(0xFF3B82F6)
    )
    "CUSTOMERS_WITH_BALANCE" -> Pair(
      listOf(Color(0xFF6A1B9A), Color(0xFF4A148C), Color(0xFF31075C)),
      Color(0xFF8E24AA)
    )
    "SETTINGS" -> Pair(
      listOf(Color(0xFF233348), Color(0xFF172332), Color(0xFF0B131C)),
      Color(0xFF3D5470)
    )
    "EXIT_APP" -> Pair(
      listOf(Color(0xFFB91C1C), Color(0xFF8A1111), Color(0xFF550808)),
      Color(0xFFEF4444)
    )
    else -> Pair(
      listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF020617)),
      Color(0xFF334155)
    )
  }
}

@Composable
fun QuickShortcutCard(
  shortcut: AppActionButton,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val (borderColor, iconGradient, displayLabel) = remember(shortcut.id, shortcut.label) {
    when (shortcut.id) {
      "CURRENCY_CONVERTER" -> Triple(
        Color(0xFF10B981),
        listOf(Color(0xFF10B981), Color(0xFF059669)),
        "محول العملات"
      )
      "ALL_CUSTOMERS" -> Triple(
        Color(0xFF2563EB),
        listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
        "جميع العملاء"
      )
      "PERCENTAGE_CALCULATOR" -> Triple(
        Color(0xFF6366F1),
        listOf(Color(0xFF818CF8), Color(0xFF4F46E5)),
        if (shortcut.label == "النسبة المئوية") "حاسبة النسبة المئ..." else shortcut.label
      )
      "FULL_CALCULATOR" -> Triple(
        Color(0xFFDC2626),
        listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
        if (shortcut.label == "الآلة الحاسبة") "آلة حاسبه النسبة" else shortcut.label
      )
      else -> Triple(
        Color(0xFF2563EB),
        listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
        shortcut.label
      )
    }
  }

  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.94f else 1.0f,
    animationSpec = spring(stiffness = Spring.StiffnessHigh)
  )

  Surface(
    onClick = onClick,
    interactionSource = interactionSource,
    shape = RoundedCornerShape(14.dp),
    color = Color(0xFFF7F2E7),
    border = BorderStroke(if (isPressed) 2.2.dp else 1.8.dp, borderColor),
    shadowElevation = if (isPressed) 1.dp else 3.dp,
    modifier = modifier
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .height(52.dp)
  ) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 7.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // أيقونة الاختصار داخل مربع ملون ثلاثي الأبعاد بلمعة ناعمة (على اليسار)
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color.Transparent,
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
          modifier = Modifier
            .size(38.dp)
            .background(Brush.verticalGradient(iconGradient), RoundedCornerShape(10.dp))
        ) {
          Box(contentAlignment = Alignment.Center) {
            when (shortcut.id) {
              "CURRENCY_CONVERTER" -> Icon(
                Icons.Default.SyncAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
              "ALL_CUSTOMERS" -> Icon(
                Icons.Default.People,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(21.dp)
              )
              "PERCENTAGE_CALCULATOR" -> Icon(
                Icons.Default.Percent,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
              "FULL_CALCULATOR" -> Icon(
                Icons.Default.Calculate,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(21.dp)
              )
              else -> {
                if (shortcut.iconEmoji.isNotEmpty()) {
                  Text(shortcut.iconEmoji, fontSize = 16.sp)
                } else {
                  Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }
          }
        }

        // اسم الاختصار بخط أسود عريض وواضح (على اليمين)
        Text(
          text = displayLabel,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF111827),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(end = 4.dp)
        )
      }
    }
  }
}

@Composable
fun MainAppActionButton(
  button: AppActionButton,
  height: androidx.compose.ui.unit.Dp = 54.dp,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val (gradientColors, borderColor) = remember(button.id, button.colorHex) {
    getButtonGradientsAndBorder(button)
  }

  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.955f else 1.0f,
    animationSpec = spring(stiffness = Spring.StiffnessHigh)
  )

  Surface(
    onClick = onClick,
    interactionSource = interactionSource,
    shape = RoundedCornerShape(28.dp),
    color = Color.Transparent,
    contentColor = Color.White,
    border = BorderStroke(if (isPressed) 1.8.dp else 1.2.dp, if (isPressed) Color.White.copy(alpha = 0.85f) else borderColor.copy(alpha = 0.65f)),
    shadowElevation = if (isPressed) 1.dp else 4.dp,
    modifier = modifier
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .height(height)
      .clip(RoundedCornerShape(28.dp))
      .background(
        Brush.verticalGradient(gradientColors),
        RoundedCornerShape(28.dp)
      )
      .drawBehind {
        // خط لمعة زجاجي خفيف على الحافة العلوية للزر
        drawLine(
          color = Color.White.copy(alpha = 0.22f),
          start = Offset(size.width * 0.15f, 2f),
          end = Offset(size.width * 0.85f, 2f),
          strokeWidth = 2.5f,
          cap = StrokeCap.Round
        )
      }
  ) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(start = 7.dp, end = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // الدائرة الكروية ثلاثية الأبعاد (3D Orb) للأيقونة على اليسار
        Box(
          modifier = Modifier
            .size(40.dp)
            .shadow(3.dp, CircleShape)
            .background(
              Brush.radialGradient(
                colors = listOf(
                  Color.White.copy(alpha = 0.32f),
                  Color.Black.copy(alpha = 0.42f)
                ),
                center = Offset(24f, 24f),
                radius = 55f
              ),
              CircleShape
            )
            .border(
              1.2.dp,
              Brush.verticalGradient(
                listOf(
                  Color.White.copy(alpha = 0.45f),
                  Color.White.copy(alpha = 0.12f)
                )
              ),
              CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          when (button.id) {
            "NEW_INVOICE" -> Icon(
              Icons.Default.Add,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
            "SAVED_INVOICES" -> Icon(
              Icons.Default.Folder,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            "OPEN_REPORT" -> Icon(
              Icons.Default.Description,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            "CUSTOMERS" -> Icon(
              Icons.Default.People,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(21.dp)
            )
            "RECEIPT_VOUCHER" -> Icon(
              Icons.Default.Payments,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            "PAYMENT_VOUCHER" -> Icon(
              Icons.AutoMirrored.Filled.ReceiptLong,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            "ALL_CUSTOMERS" -> Icon(
              Icons.AutoMirrored.Filled.Assignment,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            "SETTINGS" -> Icon(
              Icons.Default.Settings,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            "EXIT_APP" -> Icon(
              Icons.AutoMirrored.Filled.ExitToApp,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            "CUSTOMER_DISPLAY" -> Icon(
              Icons.Default.PhoneAndroid,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            "PRINT_INVOICE" -> Icon(
              Icons.Default.Print,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            "EXPORT_PDF" -> Icon(
              Icons.Default.PictureAsPdf,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            "ROOM_BACKUP" -> Icon(
              Icons.Default.Backup,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            "REPORT_CONTROL" -> Icon(
              Icons.Default.BarChart,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            else -> {
              if (button.iconEmoji.isNotEmpty()) {
                Text(button.iconEmoji, fontSize = 18.sp)
              } else {
                Icon(
                  Icons.Default.ChevronRight,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }

        // سهم التوجيه > وعنوان الزر بخط أبيض عريض على اليمين (مطابق تماماً للصورة)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.95f),
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = button.label,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }
  }
}
