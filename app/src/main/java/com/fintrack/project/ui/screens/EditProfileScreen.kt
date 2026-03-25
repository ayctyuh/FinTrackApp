package com.fintrack.project.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf<User?>(null) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)
            val dbUser = FinTrackDatabase.getInstance(context).userDao().getUserById(userId)

            currentUser = dbUser
            if (dbUser != null) {
                name = dbUser.username
                email = dbUser.email
                phone = dbUser.phoneNumber ?: "+84 XXXXXXXX"
                dob = dbUser.dateOfBirth ?: ""
                gender = dbUser.gender ?: ""
                address = dbUser.address ?: ""
            }
        }
    }

    Scaffold(
        bottomBar = { ProfileBottomNavigationBar(onHomeClick = onHomeClick) },
        containerColor = Color(0xFFF8FAFC),
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            // --- HEADER XANH ĐỒNG BỘ TUYỆT ĐỐI ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF1A3FBF), Color(0xFF3B82F6))))
            ) {
                Box(modifier = Modifier.size(160.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-40).dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
                Box(modifier = Modifier.size(100.dp).align(Alignment.BottomStart).offset(x = (-30).dp, y = 20.dp).background(Color.White.copy(alpha = 0.08f), CircleShape))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TOOLBAR (Cố định 40dp)
                    Box(modifier = Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp).size(36.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.ChevronLeft, "Quay lại", tint = Color.White)
                        }
                        Text("Chỉnh sửa hồ sơ", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // AVATAR
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier.size(90.dp).clip(CircleShape).border(3.dp, Color.White, CircleShape).background(Color(0xFF2E5BFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Person, null, tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                        Box(
                            modifier = Modifier.size(28.dp).offset(x = (-4).dp, y = (-4).dp).background(Color.White, CircleShape).padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2E5BFF), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(name.ifEmpty { "---" }, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("ID: ${currentUser?.id ?: "---"}", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- FORM CHỈNH SỬA ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("THÔNG TIN TÀI KHOẢN", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileTextField(label = "Họ và tên", value = name, onValueChange = { name = it })
                    ProfileTextField(label = "Số điện thoại", value = phone, onValueChange = {}, readOnly = true)
                    ProfileTextField(label = "Email", value = email, onValueChange = {}, readOnly = true)
                    ProfileTextField(label = "Ngày sinh", value = dob, onValueChange = { dob = it }, placeholder = "DD/MM/YYYY")
                    ProfileTextField(label = "Giới tính", value = gender, onValueChange = { gender = it }, placeholder = "Nam / Nữ / Khác")
                    ProfileTextField(label = "Địa chỉ", value = address, onValueChange = { address = it }, placeholder = "Nhập địa chỉ của bạn")

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                currentUser?.let { user ->
                                    val updatedUser = user.copy(
                                        username = name,
                                        dateOfBirth = dob,
                                        gender = gender,
                                        address = address
                                    )
                                    withContext(Dispatchers.IO) {
                                        FinTrackDatabase.getInstance(context).userDao().updateUser(updatedUser)
                                    }
                                    Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                                    onBackClick()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5BFF)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cập nhật hồ sơ", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileTextField(label: String, value: String, onValueChange: (String) -> Unit, readOnly: Boolean = false, placeholder: String = "") {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label, color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = value, onValueChange = onValueChange, readOnly = readOnly,
            placeholder = { Text(placeholder, color = Color(0xFFCBD5E1), fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF1F5F9), unfocusedContainerColor = Color(0xFFF1F5F9),
                disabledContainerColor = Color(0xFFF8FAFC), focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent,
                focusedTextColor = Color(0xFF1E293B), unfocusedTextColor = Color(0xFF1E293B), disabledTextColor = Color(0xFF94A3B8)
            ),
            shape = RoundedCornerShape(12.dp), enabled = !readOnly
        )
    }
}