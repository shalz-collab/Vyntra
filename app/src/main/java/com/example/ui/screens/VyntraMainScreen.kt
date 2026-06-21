package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.database.PredictionHistoryEntity
import com.example.data.database.UserEntity
import com.example.data.engine.FamousStartup
import com.example.data.engine.VyntraEngine
import com.example.ui.theme.*
import com.example.ui.viewmodel.VyntraScreen
import com.example.ui.viewmodel.VyntraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VyntraMainScreen(viewModel: VyntraViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allPredictions by viewModel.allPredictions.collectAsStateWithLifecycle()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Sidebar items representing our core business modules
    val sidebarDestinations = listOf(
        Triple(VyntraScreen.DASHBOARD, Icons.Default.Dashboard, "Investment Hub"),
        Triple(VyntraScreen.SEARCH, Icons.Default.Search, "Startup Search"),
        Triple(VyntraScreen.ANALYSIS_FORM, Icons.Default.Calculate, "Simulation Form"),
        Triple(VyntraScreen.ANALYTICS_CHARTS, Icons.Default.BarChart, "R Analytics Matrix"),
        Triple(VyntraScreen.REPORTS, Icons.Default.Description, "Strategic Reports")
    )

    // Layout wrapped inside a dynamic Navigation Drawer for complete professional accessibility
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentUser != null,
        drawerContent = {
            if (currentUser != null) {
                ModalDrawerSheet(
                    modifier = Modifier.width(300.dp),
                    drawerContainerColor = VyntraNavy,
                    drawerContentColor = Color.White
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(VyntraNavy, Color(0xFF1E293B))
                                )
                            )
                            .padding(top = 40.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
                    ) {
                        Column {
                            // Logo display
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VyntraPrimaryLevel),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = "Logo icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "VYNTRA",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.SansSerif,
                                    letterSpacing = 1.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // User meta info card
                            Text(
                                text = currentUser?.fullName ?: "Business Analyst",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = currentUser?.email ?: "analyst@vyntra.io",
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Badge indicating access tokens
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (currentUser?.role == "Admin") ColorWarning.copy(alpha = 0.25f)
                                        else VyntraSecondary.copy(alpha = 0.25f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = currentUser?.role?.uppercase() ?: "USER",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (currentUser?.role == "Admin") ColorWarning else Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Nav items (Scrollable)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp)
                    ) {
                        sidebarDestinations.forEach { (screen, icon, label) ->
                            NavigationDrawerItem(
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label, fontWeight = FontWeight.SemiBold) },
                                selected = currentScreen == screen,
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = VyntraPrimaryLevel,
                                    unselectedContainerColor = Color.Transparent,
                                    selectedIconColor = Color.White,
                                    unselectedIconColor = Color.LightGray.copy(alpha = 0.8f),
                                    selectedTextColor = Color.White,
                                    unselectedTextColor = Color.LightGray
                                ),
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    viewModel.navigateTo(screen)
                                },
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .testTag("nav_${screen.name.lowercase()}")
                            )
                        }

                        // Add Admin Control Panel page conditionally if role fits
                        if (currentUser?.role == "Admin") {
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin panel") },
                                label = { Text("Admin Console", fontWeight = FontWeight.SemiBold) },
                                selected = currentScreen == VyntraScreen.ADMIN,
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = ColorWarning,
                                    unselectedContainerColor = Color.Transparent,
                                    selectedIconColor = VyntraNavy,
                                    unselectedIconColor = Color.LightGray.copy(alpha = 0.8f),
                                    selectedTextColor = VyntraNavy,
                                    unselectedTextColor = Color.LightGray
                                ),
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    viewModel.navigateTo(VyntraScreen.ADMIN)
                                },
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .testTag("nav_admin_panel")
                            )
                        }
                    }

                    HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(horizontal = 16.dp))
                    
                    // Profile/Account drawer item
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("User Profile", fontWeight = FontWeight.SemiBold) },
                        selected = currentScreen == VyntraScreen.PROFILE,
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = VyntraSecondary.copy(alpha = 0.2f),
                            unselectedContainerColor = Color.Transparent,
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.LightGray,
                            selectedTextColor = Color.White,
                            unselectedTextColor = Color.LightGray
                        ),
                        onClick = {
                            scope.launch { drawerState.close() }
                            viewModel.navigateTo(VyntraScreen.PROFILE)
                        },
                        modifier = Modifier
                            .padding(12.dp)
                            .testTag("nav_profile")
                    )

                    // Logout drawer item
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Log out") },
                        label = { Text("Sign Out", fontWeight = FontWeight.SemiBold) },
                        selected = false,
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = ColorDanger,
                            unselectedTextColor = ColorDanger
                        ),
                        onClick = {
                            scope.launch { drawerState.close() }
                            viewModel.logoutUser()
                            Toast.makeText(context, "Logged out of Vyntra", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("nav_signout")
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                // TopAppBar displaying active headers contextually
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentScreen.title,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = VyntraNavy
                            )
                            if (currentUser != null && currentScreen == VyntraScreen.DASHBOARD) {
                                Text(
                                    text = "Workspace: ${currentUser?.fullName}",
                                    fontSize = 11.sp,
                                    color = ColorNeutralGrey,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (currentUser != null) {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("top_menu_hamburger")
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Open navigation menu", tint = VyntraNavy)
                            }
                        } else {
                            IconButton(onClick = { viewModel.navigateTo(VyntraScreen.LANDING) }) {
                                Icon(Icons.Default.Analytics, contentDescription = "App logo", tint = VyntraPrimaryDark)
                            }
                        }
                    },
                    actions = {
                        if (currentUser != null) {
                            // Quick Action shortcuts depending on screens
                            IconButton(
                                onClick = { viewModel.navigateTo(VyntraScreen.PROFILE) },
                                modifier = Modifier.testTag("top_profile_trigger")
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "Go to Profile", tint = VyntraNavy)
                            }
                        } else {
                            if (currentScreen != VyntraScreen.LOGIN) {
                                TextButton(
                                    onClick = { viewModel.navigateTo(VyntraScreen.LOGIN) },
                                    modifier = Modifier.testTag("top_login_shortcut")
                                ) {
                                    Text("Sign In", fontWeight = FontWeight.Bold, color = VyntraPrimaryLevel)
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = VyntraSurface,
                        titleContentColor = VyntraNavy
                    )
                )
            },
            floatingActionButton = {
                // Persistent analytical simulator trigger for fast calculations
                if (currentUser != null && 
                    currentScreen != VyntraScreen.ANALYSIS_FORM && 
                    currentScreen != VyntraScreen.PRE_RESULTS && 
                    currentScreen != VyntraScreen.LANDING) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.navigateTo(VyntraScreen.ANALYSIS_FORM) },
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Calculator icon") },
                        text = { Text("New Simulation") },
                        containerColor = VyntraPrimaryLevel,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_new_simulation")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(VyntraBgLight)
            ) {
                // Handle high-velocity screen routing smoothly
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                    },
                    label = "screen_transitions"
                ) { screen ->
                    when (screen) {
                        VyntraScreen.LANDING -> LandingPage(viewModel)
                        VyntraScreen.LOGIN -> LoginPage(viewModel)
                        VyntraScreen.REGISTER -> RegistrationPage(viewModel)
                        VyntraScreen.DASHBOARD -> DashboardScreen(viewModel)
                        VyntraScreen.SEARCH -> StartupSearchScreen(viewModel)
                        VyntraScreen.ANALYSIS_FORM -> StartupAnalysisFormScreen(viewModel)
                        VyntraScreen.PRE_RESULTS -> PredictionResultScreen(viewModel)
                        VyntraScreen.ANALYTICS_CHARTS -> AnalyticsDashboardScreen(viewModel)
                        VyntraScreen.REPORTS -> ReportsHistoryScreen(viewModel)
                        VyntraScreen.PROFILE -> UserProfileScreen(viewModel)
                        VyntraScreen.ADMIN -> AdminDashboardScreen(viewModel)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. Landing Page
// -------------------------------------------------------------
@Composable
fun LandingPage(viewModel: VyntraViewModel) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Brand image card loading our generated professional artwork
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VyntraNavy)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = "/app/src/main/res/drawable/vyntra_logo.jpg",
                    contentDescription = "Vyntra banner logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, VyntraNavy.copy(alpha = 0.85f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Vyntra Predictive AI",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Venture Analytics & Predictive Modeling Suite",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Evaluate Startup Success Likelihood in Real Time",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = VyntraPrimaryDark,
            lineHeight = 30.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Designed for venture capitals, angel networks, and elite incubators. Vyntra applies R-inspired algorithms—Logistic Regression, Decision Trees, Random Forest, and XGBoost—to benchmark growth capability, cash burn efficiency, and strategic market share.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = ColorNeutralGrey,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Professional Ticker Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("24,500+", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = VyntraPrimaryLevel)
                Text("Ventures Indexed", fontSize = 11.sp, color = ColorNeutralGrey)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("98.4%", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = VyntraTealAccent)
                Text("Training R2 Score", fontSize = 11.sp, color = ColorNeutralGrey)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("120K+", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = ColorSuccess)
                Text("Simulations Made", fontSize = 11.sp, color = ColorNeutralGrey)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Call to action buttons
        Button(
            onClick = { viewModel.navigateTo(VyntraScreen.LOGIN) },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(52.dp)
                .testTag("btn_get_started"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VyntraPrimaryDark)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VpnKey, contentDescription = "Key logo", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Access Analyst Workspace", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { viewModel.navigateTo(VyntraScreen.REGISTER) },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(50.dp)
                .testTag("btn_create_account_landing"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, VyntraPrimaryDark)
        ) {
            Text("Register New Analyst Account", fontWeight = FontWeight.Bold, color = VyntraPrimaryDark)
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Live Demo credentials hint
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(containerColor = ColorNeutralGrey.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "💡 Classroom Evaluation Accounts:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = VyntraNavy
                )
                Text(
                    text = "• Administrator access: username 'admin' with password 'admin123'\n• Standard Analyst access: username 'analyst' with password 'analyst123'",
                    fontSize = 11.sp,
                    color = ColorNeutralGrey,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// -------------------------------------------------------------
// 2. Login Page
// -------------------------------------------------------------
@Composable
fun LoginPage(viewModel: VyntraViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(VyntraPrimaryDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Analytics, contentDescription = "", tint = Color.White, modifier = Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Sign In to Vyntra", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = VyntraNavy)
        Text("Real-Time Startup Predictive Platform", fontSize = 13.sp, color = ColorNeutralGrey)

        Spacer(modifier = Modifier.height(24.dp))

        if (loginError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ColorDanger.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, ColorDanger),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = loginError ?: "Error",
                    color = ColorDanger,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Credentials Entry Fields
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_username_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VyntraPrimaryDark,
                focusedLabelColor = VyntraPrimaryDark
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_password_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VyntraPrimaryDark,
                focusedLabelColor = VyntraPrimaryDark
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.loginUser(username, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("login_submit_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VyntraPrimaryDark)
        ) {
            Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { 
                viewModel.navigateTo(VyntraScreen.DASHBOARD)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("login_bypass_button"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, VyntraTealAccent),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VyntraTealAccent)
        ) {
            Icon(Icons.Default.Speed, contentDescription = "", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Instant Guest Bypass & Access", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Authorized account required. ", fontSize = 13.sp, color = ColorNeutralGrey)
            TextButton(
                onClick = { viewModel.navigateTo(VyntraScreen.REGISTER) },
                modifier = Modifier.testTag("btn_register_redirect")
            ) {
                Text("Signup", fontWeight = FontWeight.Bold, color = VyntraPrimaryLevel)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = ColorNeutralGrey.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Testing credentials:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = VyntraNavy)
                Text("User: analyst  |  Pass: analyst123\nAdmin: admin  |  Pass: admin123", fontSize = 11.sp, color = ColorNeutralGrey, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Registration Page
// -------------------------------------------------------------
@Composable
fun RegistrationPage(viewModel: VyntraViewModel) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("User") } // User or Admin

    val registerError by viewModel.registerError.collectAsStateWithLifecycle()
    val registerSuccess by viewModel.registerSuccess.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(registerSuccess) {
        if (registerSuccess) {
            Toast.makeText(context, "Account created successfully! Log in to enter workspace.", Toast.LENGTH_LONG).show()
            viewModel.navigateTo(VyntraScreen.LOGIN)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Analyst Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = VyntraNavy)
        Text("Request administrative or research tokens", fontSize = 13.sp, color = ColorNeutralGrey)

        Spacer(modifier = Modifier.height(20.dp))

        if (registerError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ColorDanger.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, ColorDanger),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = registerError ?: "Error",
                    color = ColorDanger,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_username_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name / Title") },
            leadingIcon = { Icon(Icons.Default.AccountBox, contentDescription = "") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_fullname_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_email_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password Secure Key") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_password_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Role Selector Box
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Select Access Role:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VyntraNavy)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = role == "User",
                        onClick = { role = "User" },
                        modifier = Modifier.testTag("radio_user_role")
                    )
                    Text("Standard Venture Analyst")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = role == "Admin",
                        onClick = { role = "Admin" },
                        modifier = Modifier.testTag("radio_admin_role")
                    )
                    Text("Administrator")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.registerUser(username, email, fullName, password, role) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("register_submit_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VyntraPrimaryDark)
        ) {
            Text("Create Account", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already registered? ", fontSize = 13.sp, color = ColorNeutralGrey)
            TextButton(
                onClick = { viewModel.navigateTo(VyntraScreen.LOGIN) },
                modifier = Modifier.testTag("btn_login_redirect")
            ) {
                Text("Log In", fontWeight = FontWeight.Bold, color = VyntraPrimaryLevel)
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Dashboard (Home Dashboard)
// -------------------------------------------------------------
@Composable
fun DashboardScreen(viewModel: VyntraViewModel) {
    val allPredictions by viewModel.allPredictions.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Aggregate statistics
    val totalPredictions = allPredictions.size
    val highGrowthCount = allPredictions.count { it.successProbability >= 85.0 }
    
    // Total capital analyzed sum
    val predefCapitalSum = VyntraEngine.famousStartups.sumOf { it.fundingAmount }
    val historyCapitalSum = allPredictions.sumOf { it.fundingAmount }
    val totalCapitalFormatted = formatAmountInBillions(predefCapitalSum + historyCapitalSum)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Welcome Analyst header
        Text("Market Intelligence Interface", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VyntraPrimaryLevel)
        Text("Predictive Summary Panel", fontSize = 22.sp, fontWeight = FontWeight.Black, color = VyntraNavy)

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of 4 Professional KPI Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiMetricCard(
                title = "Capital Analyzed",
                value = totalCapitalFormatted,
                desc = "VC Index Volume",
                icon = Icons.Default.MonetizationOn,
                colorAccent = VyntraPrimaryLevel,
                modifier = Modifier.weight(1f)
            )
            KpiMetricCard(
                title = "Evaluations Made",
                value = "$totalPredictions",
                desc = "SQLite History DB",
                icon = Icons.Default.SettingsSuggest,
                colorAccent = VyntraTealAccent,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiMetricCard(
                title = "High Growth Index",
                value = "$highGrowthCount",
                desc = "Vultures >85% ML",
                icon = Icons.Default.TrendingUp,
                colorAccent = ColorSuccess,
                modifier = Modifier.weight(1f)
            )
            KpiMetricCard(
                title = "Sectors Covered",
                value = "6 Industries",
                desc = "Dynamic Benchmarks",
                icon = Icons.Default.Category,
                colorAccent = ColorWarning,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions Row
        Text("Interactive Modules", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraNavy)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                label = "Startup Search",
                subLabel = "Pre-fill presets",
                icon = Icons.Default.Search,
                onClick = { viewModel.navigateTo(VyntraScreen.SEARCH) },
                color = VyntraPrimaryLevel,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Statistical R Plots",
                subLabel = "Charts matrix",
                icon = Icons.Default.BarChart,
                onClick = { viewModel.navigateTo(VyntraScreen.ANALYTICS_CHARTS) },
                color = VyntraTealAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Predictions Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Predictions History", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraNavy)
            TextButton(
                onClick = { viewModel.navigateTo(VyntraScreen.REPORTS) },
                modifier = Modifier.testTag("btn_view_all_history")
            ) {
                Text("View All", color = VyntraPrimaryLevel, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (allPredictions.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = VyntraSurface),
                border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.HistoryToggleOff,
                        contentDescription = "Empty state icon",
                        tint = ColorNeutralGrey.copy(alpha = 0.8f),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No evaluations found", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraNavy)
                    Text("Launch the ML simulation form or search model presets to record your first evaluation.", textAlign = TextAlign.Center, fontSize = 12.sp, color = ColorNeutralGrey, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        } else {
            // Show top 3 recent predictions
            val recLimit = allPredictions.take(3)
            recLimit.forEach { record ->
                PredictionHistoryRowItem(
                    record = record,
                    onClick = { viewModel.selectHistoryItem(record) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Industry Overview Section
        Text("Sector Baseline Metrics", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraNavy)
        Spacer(modifier = Modifier.height(8.dp))
        
        val industries = listOf(
            Triple("Technology", "SaaS & Consumer Hardware", "75.2% Succ Avg"),
            Triple("Fintech", "Payments & Financial Brokerage", "81.4% Succ Avg"),
            Triple("Artificial Intelligence", "LLMs, ML, & Big Data", "89.1% Succ Avg"),
            Triple("Health Tech", "Clinical SaaS & Diagnostics", "68.5% Succ Avg"),
            Triple("E-Commerce", "Direct Consumer Delivery", "58.2% Succ Avg")
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                industries.forEach { (title, subtitle, metric) ->
                    Card(
                        modifier = Modifier
                            .width(170.dp)
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = VyntraSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(VyntraPrimaryDark.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = "", tint = VyntraPrimaryDark, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(subtitle, fontSize = 11.sp, color = ColorNeutralGrey, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(metric, fontWeight = FontWeight.Black, fontSize = 12.sp, color = VyntraTealAccent)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    desc: String,
    icon: ImageVector,
    colorAccent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = VyntraSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorNeutralGrey)
                Icon(icon, contentDescription = "", tint = colorAccent, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = VyntraNavy)
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, fontSize = 9.sp, color = ColorNeutralGrey)
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    subLabel: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = VyntraNavy),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = "", tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                Text(subLabel, fontSize = 10.sp, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun PredictionHistoryRowItem(
    record: PredictionHistoryEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = VyntraSurface),
        border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon badge with score
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            record.successProbability >= 80.0 -> ColorSuccess.copy(alpha = 0.12f)
                            record.successProbability >= 60.0 -> ColorWarning.copy(alpha = 0.12f)
                            else -> ColorDanger.copy(alpha = 0.12f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${record.successProbability.toInt()}%",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = when {
                        record.successProbability >= 80.0 -> ColorSuccess
                        record.successProbability >= 60.0 -> ColorWarning
                        else -> ColorDanger
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(record.startupName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraNavy)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(record.industry, fontSize = 11.sp, color = ColorNeutralGrey)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(ColorNeutralGrey))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(formatFunding(record.fundingAmount), fontSize = 11.sp, color = ColorNeutralGrey)
                }
            }

            // Arrow forward indicator
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "View detail icon", tint = ColorNeutralGrey)
        }
    }
}

// -------------------------------------------------------------
// 5. Startup Search Screen
// -------------------------------------------------------------
@Composable
fun StartupSearchScreen(viewModel: VyntraViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredStartups by viewModel.filteredFamousStartups.collectAsStateWithLifecycle()

    // Side-by-side comparison state
    var compareStartupA by remember { mutableStateOf<FamousStartup?>(null) }
    var compareStartupB by remember { mutableStateOf<FamousStartup?>(null) }
    var showComparePanel by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Market Intelligence Directory", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VyntraTealAccent)
        Text("Startup Search Engine", fontSize = 22.sp, fontWeight = FontWeight.Black, color = VyntraNavy)
        Spacer(modifier = Modifier.height(12.dp))

        // Search text field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search OpenAI, Canva, Zerodha...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("startup_search_query_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VyntraPrimaryDark,
                focusedLabelColor = VyntraPrimaryDark
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Comparison trigger dashboard
        if (compareStartupA != null || compareStartupB != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = VyntraNavy),
                modifier = Modifier
                    .fillModifier()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Startup Comparison Matrix (2 Selected)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = compareStartupA?.name ?: "[Select Startup]",
                                color = Color.LightGray,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable { compareStartupA = null }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("VS", fontWeight = FontWeight.Bold, color = VyntraTealAccent, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = compareStartupB?.name ?: "[Select Startup]",
                                color = Color.LightGray,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable { compareStartupB = null }
                            )
                        }
                        
                        if (compareStartupA != null && compareStartupB != null) {
                            Button(
                                onClick = { showComparePanel = true },
                                colors = ButtonDefaults.buttonColors(containerColor = VyntraTealAccent),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Text("Compare Now", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredStartups) { startup ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VyntraSurface),
                    border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(startup.name, fontWeight = FontWeight.Black, fontSize = 18.sp, color = VyntraNavy)
                                Text(startup.marketCategory, fontSize = 12.sp, color = VyntraTealAccent, fontWeight = FontWeight.SemiBold)
                            }
                            
                            Row {
                                // Comparison toggle checkbox
                                IconButton(
                                    onClick = {
                                        if (compareStartupA == null) {
                                            compareStartupA = startup
                                        } else if (compareStartupB == null && compareStartupA != startup) {
                                            compareStartupB = startup
                                        } else {
                                            if (compareStartupA == startup) compareStartupA = null
                                            else if (compareStartupB == startup) compareStartupB = null
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (compareStartupA == startup || compareStartupB == startup) Icons.Default.CompareArrows else Icons.Outlined.CompareArrows,
                                        contentDescription = "Compare selection",
                                        tint = if (compareStartupA == startup || compareStartupB == startup) VyntraTealAccent else ColorNeutralGrey
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(startup.tagline, fontSize = 12.sp, color = ColorNeutralGrey, lineHeight = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Metrix columns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("HEADQUARTERS", fontSize = 9.sp, color = ColorNeutralGrey, fontWeight = FontWeight.Bold)
                                Text(startup.headquarters, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VyntraNavy)
                            }
                            Column {
                                Text("LIFETIME FUNDING", fontSize = 9.sp, color = ColorNeutralGrey, fontWeight = FontWeight.Bold)
                                Text(formatFunding(startup.fundingAmount), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VyntraNavy)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    // Autofill preset values onto Simulation Form
                                    viewModel.navigateTo(VyntraScreen.ANALYSIS_FORM)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, VyntraPrimaryLevel)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Input, contentDescription = "", modifier = Modifier.size(14.dp), tint = VyntraPrimaryLevel)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Autofill parameters", fontSize = 11.sp, color = VyntraPrimaryLevel)
                                }
                            }

                            Button(
                                onClick = {
                                    // Instantly simulate success prediction right inside workspace!
                                    viewModel.runAnalyticsSimulation(
                                        name = startup.name,
                                        industry = startup.industry,
                                        foundingYear = startup.foundingYear,
                                        funding = startup.fundingAmount,
                                        revenueGrowthRate = startup.revenueGrowthRate,
                                        teamSize = startup.teamSize,
                                        marketSize = startup.marketSize,
                                        customerGrowth = startup.customerGrowth,
                                        founderExperience = startup.founderExperience,
                                        category = startup.marketCategory,
                                        region = startup.geographicRegion
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = VyntraPrimaryDark),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.BatchPrediction, contentDescription = "", modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Instant Predict", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal popup comparing startups side-by-side
    if (showComparePanel && compareStartupA != null && compareStartupB != null) {
        val sa = compareStartupA!!
        val sb = compareStartupB!!

        AlertDialog(
            onDismissRequest = { showComparePanel = false },
            title = {
                Text(
                    "Benchmark Comparison Matrix",
                    fontWeight = FontWeight.ExtraBold,
                    color = VyntraNavy,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Text("Side-By-Side KPI evaluation values", fontSize = 12.sp, color = ColorNeutralGrey)
                    Spacer(modifier = Modifier.height(12.dp))

                    ComparisonRowText("Venture Name", sa.name, sb.name, header = true)
                    ComparisonRowText("Sector Segment", sa.industry, sb.industry)
                    ComparisonRowText("Market Category", sa.marketCategory, sb.marketCategory)
                    ComparisonRowText("Lifetime Funding", formatFunding(sa.fundingAmount), formatFunding(sb.fundingAmount))
                    ComparisonRowText("Annual Growth Rate", "${sa.revenueGrowthRate}%", "${sb.revenueGrowthRate}%")
                    ComparisonRowText("Founder EXP", "${sa.founderExperience} yrs", "${sb.founderExperience} yrs")
                    ComparisonRowText("Team Volume", "${sa.teamSize} reps", "${sb.teamSize} reps")
                    ComparisonRowText("Addressable TAM", "$${sa.marketSize}M", "$${sb.marketSize}M")
                    ComparisonRowText("Customer Velocity", "${sa.customerGrowth}%", "${sb.customerGrowth}%")
                    ComparisonRowText("Sovereignty", if(sa.fundingAmount == 0.0) "Bootstrapped" else "VC Backed", if(sb.fundingAmount == 0.0) "Bootstrapped" else "VC Backed")
                }
            },
            confirmButton = {
                TextButton(onClick = { showComparePanel = false }) {
                    Text("Dismiss Matrix", fontWeight = FontWeight.Bold, color = VyntraPrimaryDark)
                }
            }
        )
    }
}

@Composable
fun ComparisonRowText(label: String, valA: String, valB: String, header: Boolean = false) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (header) VyntraPrimaryLevel else ColorNeutralGrey,
                fontWeight = if (header) FontWeight.Black else FontWeight.SemiBold,
                modifier = Modifier.weight(1.5f)
            )
            Text(
                text = valA,
                fontSize = 12.sp,
                fontWeight = if (header) FontWeight.Black else FontWeight.Bold,
                color = VyntraNavy,
                modifier = Modifier.weight(1.3f),
                textAlign = TextAlign.End
            )
            Text(
                text = valB,
                fontSize = 12.sp,
                fontWeight = if (header) FontWeight.Black else FontWeight.Bold,
                color = VyntraTealAccent,
                modifier = Modifier.weight(1.3f),
                textAlign = TextAlign.End
            )
        }
        HorizontalDivider(color = ColorNeutralGrey.copy(alpha = 0.15f))
    }
}

// -------------------------------------------------------------
// 6. Startup Analysis Form
// -------------------------------------------------------------
@Composable
fun StartupAnalysisFormScreen(viewModel: VyntraViewModel) {
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("Technology") }
    var foundingYear by remember { mutableStateOf("2021") }
    var teamSize by remember { mutableStateOf(25) }
    var fundingAmount by remember { mutableStateOf("") }
    var revenueGrowthRate by remember { mutableStateOf(35.0) }
    var marketSize by remember { mutableStateOf("") } // TAM (Million USD)
    var customerGrowth by remember { mutableStateOf("") }
    var founderExperience by remember { mutableStateOf(5) }
    var productCategory by remember { mutableStateOf("") }
    var geographicRegion by remember { mutableStateOf("North America") }

    val industries = listOf("Technology", "Fintech", "Artificial Intelligence", "Education", "E-Commerce", "Healthcare")
    val regions = listOf("North America", "Asia-Pacific", "Europe", "Latin America", "Middle East")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text("R ML Modelling Simulator", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VyntraPrimaryDark)
        Text("Simulation Form", fontSize = 22.sp, fontWeight = FontWeight.Black, color = VyntraNavy)
        Text("Input parameters below to feed the Logistic Regression, Decision Tree, Random Forest, and XGBoost estimations.", fontSize = 13.sp, color = ColorNeutralGrey, lineHeight = 18.sp)

        Spacer(modifier = Modifier.height(16.dp))

        if (isAnalyzing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = VyntraPrimaryDark)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Analyzing parameters across R ML Models...", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraNavy)
                Text("Estimating residuals, generating weights & mapping sentiment...", fontSize = 12.sp, color = ColorNeutralGrey)
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = VyntraSurface),
                border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    
                    // Core Info Division
                    Text("1. Core Business Information", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraPrimaryLevel)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Startup Venture Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_startup_name"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Industry Sector Selection:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorNeutralGrey)
                    Box(modifier = Modifier.fillMaxWidth().height(48.dp).horizontalScroll(rememberScrollState())) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            industries.forEach { ind ->
                                FilterChip(
                                    selected = industry == ind,
                                    onClick = { industry = ind },
                                    label = { Text(ind, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = productCategory,
                        onValueChange = { productCategory = it },
                        label = { Text("Product Category (e.g. SaaS, LLM API, CRM)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = foundingYear,
                        onValueChange = { foundingYear = it },
                        label = { Text("Founding Vintage Year") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("2. Capitalization & Financials", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraPrimaryLevel)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fundingAmount,
                        onValueChange = { fundingAmount = it },
                        label = { Text("Lifetime Capital Funding Raised (in USD)") },
                        placeholder = { Text("e.g. 5000000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_funding_amount"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Revenue Growth Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Annual Revenue Growth: ${revenueGrowthRate.toInt()}%", fontSize = 12.sp, color = ColorNeutralGrey, fontWeight = FontWeight.Bold)
                        Text(if(revenueGrowthRate >= 60.0) "Hyper growth" else "Linear", fontSize = 11.sp, color = VyntraPrimaryLevel)
                    }
                    Slider(
                        value = revenueGrowthRate.toFloat(),
                        onValueChange = { revenueGrowthRate = it.toDouble() },
                        valueRange = 0f..200f,
                        steps = 20,
                        colors = SliderDefaults.colors(thumbColor = VyntraPrimaryDark, activeTrackColor = VyntraPrimaryDark)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("3. Growth Momentum & TAM", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraPrimaryLevel)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = marketSize,
                        onValueChange = { marketSize = it },
                        label = { Text("Market TAM Size (in Million USD)") },
                        placeholder = { Text("e.g. 1500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customerGrowth,
                        onValueChange = { customerGrowth = it },
                        label = { Text("Annual Customer Acquisition Growth %") },
                        placeholder = { Text("e.g. 45") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("4. Human Resources & Advisory", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraPrimaryLevel)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Slider for Founder Experience
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Core Founder Experience: $founderExperience years", fontSize = 12.sp, color = ColorNeutralGrey, fontWeight = FontWeight.Bold)
                        Text(if(founderExperience >= 10) "Veteran" else "Emerging", fontSize = 11.sp, color = VyntraPrimaryLevel)
                    }
                    Slider(
                        value = founderExperience.toFloat(),
                        onValueChange = { founderExperience = it.toInt() },
                        valueRange = 1f..30f,
                        steps = 29,
                        colors = SliderDefaults.colors(thumbColor = VyntraPrimaryDark, activeTrackColor = VyntraPrimaryDark)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Box Slider for Team Size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active Team Size: $teamSize reps", fontSize = 12.sp, color = ColorNeutralGrey, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = teamSize.toFloat(),
                        onValueChange = { teamSize = it.toInt() },
                        valueRange = 2f..1000f,
                        steps = 100,
                        colors = SliderDefaults.colors(thumbColor = VyntraPrimaryDark, activeTrackColor = VyntraPrimaryDark)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Asset Region Deployment:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorNeutralGrey)
                    Box(modifier = Modifier.fillMaxWidth().height(48.dp).horizontalScroll(rememberScrollState())) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            regions.forEach { reg ->
                                FilterChip(
                                    selected = geographicRegion == reg,
                                    onClick = { geographicRegion = reg },
                                    label = { Text(reg, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // Clear/Reset Form fields
                                name = ""
                                productCategory = ""
                                foundingYear = "2021"
                                teamSize = 25
                                fundingAmount = ""
                                revenueGrowthRate = 35.0
                                marketSize = ""
                                customerGrowth = ""
                                founderExperience = 5
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reset Values", color = ColorNeutralGrey)
                        }

                        Button(
                            onClick = {
                                if (name.isBlank()) {
                                    Toast.makeText(context, "Please configure Startup Venture Name", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val fDouble = fundingAmount.trim().toDoubleOrNull() ?: 0.0
                                val tamDouble = marketSize.trim().toDoubleOrNull() ?: 150.0
                                val cgDouble = customerGrowth.trim().toDoubleOrNull() ?: revenueGrowthRate
                                val vintageInt = foundingYear.trim().toIntOrNull() ?: 2021

                                viewModel.runAnalyticsSimulation(
                                    name = name,
                                    industry = industry,
                                    foundingYear = vintageInt,
                                    funding = fDouble,
                                    revenueGrowthRate = revenueGrowthRate,
                                    teamSize = teamSize,
                                    marketSize = tamDouble,
                                    customerGrowth = cgDouble,
                                    founderExperience = founderExperience,
                                    category = productCategory,
                                    region = geographicRegion
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_form_predict_success"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VyntraPrimaryDark)
                        ) {
                            Text("Compute Models", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

// -------------------------------------------------------------
// 7. Prediction Results Page
// -------------------------------------------------------------
@Composable
fun PredictionResultScreen(viewModel: VyntraViewModel) {
    val result by viewModel.activeSimulationOutput.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    if (result == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
            Text("Awaiting computation results...")
        }
        return
    }

    val res = result!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Venture Assessment Document", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VyntraPrimaryDark)
                Text(res.startupName, fontSize = 24.sp, fontWeight = FontWeight.Black, color = VyntraNavy)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            res.successProbability >= 80.0 -> ColorSuccess.copy(alpha = 0.15f)
                            res.successProbability >= 60.0 -> ColorWarning.copy(alpha = 0.15f)
                            else -> ColorDanger.copy(alpha = 0.15f)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = res.riskLevel.uppercase(),
                    color = when {
                        res.successProbability >= 80.0 -> ColorSuccess
                        res.successProbability >= 60.0 -> ColorWarning
                        else -> ColorDanger
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large circular diagnostic gauge chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VyntraSurface),
            border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Combined success probability ensemble", fontSize = 11.sp, color = ColorNeutralGrey, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                // Custom Canvas Gauge Chart
                Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                    val targetScore = res.successProbability.toFloat()
                    val animatedScore by animateFloatAsState(
                        targetValue = targetScore,
                        animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing)
                    )

                    Canvas(modifier = Modifier.size(180.dp)) {
                        val canvasSize = size
                        // Draw empty gray arc
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.4f),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw progress arc with gradient
                        drawArc(
                            brush = Brush.horizontalGradient(
                                colors = listOf(VyntraSecondary, ColorSuccess)
                            ),
                            startAngle = 135f,
                            sweepAngle = (animatedScore / 100f) * 270f,
                            useCenter = false,
                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw target details needle indicator
                        val angleRad = ((135 + (animatedScore / 100f) * 270) * PI / 180).toFloat()
                        val radius = (canvasSize.width / 2) - 30.dp.toPx()
                        val centerX = canvasSize.width / 2
                        val centerY = canvasSize.height / 2
                        val endX = centerX + radius * cos(angleRad)
                        val endY = centerY + radius * sin(angleRad)

                        // Needle line
                        drawLine(
                            color = VyntraNavy,
                            start = Offset(centerX, centerY),
                            end = Offset(endX, endY),
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Center circle pivot pin
                        drawCircle(
                            color = VyntraNavy,
                            radius = 8.dp.toPx()
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        Text(
                            text = "${res.successProbability.toInt()}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = VyntraNavy
                        )
                        Text(
                            text = if(res.successProbability >= 80) "Likely To Succeed" else "Viability Risk",
                            fontSize = 11.sp,
                            color = ColorNeutralGrey,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = ColorNeutralGrey.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GROWTH POTENTIAL", fontSize = 9.sp, color = ColorNeutralGrey, fontWeight = FontWeight.Bold)
                        Text(res.growthPotential, fontSize = 14.sp, fontWeight = FontWeight.Black, color = VyntraPrimaryDark)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("INVESTMENT RATING", fontSize = 9.sp, color = ColorNeutralGrey, fontWeight = FontWeight.Bold)
                        Text(res.investmentRating, fontSize = 14.sp, fontWeight = FontWeight.Black, color = ColorSuccess)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display individual R-Model values side-by-side representing the academic modeling
        Text("R ML Ensemble Components (caret predictions)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraNavy)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VyntraSurface),
            border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ModelValueBullet("Logistic Regression Score (Sigmoid GLM)", res.modelLogisticRegression)
                ModelValueBullet("Decision Tree Classifier (Branch Entropy)", res.modelDecisionTree)
                ModelValueBullet("Random Forest Forest (Bagging Gini)", res.modelRandomForest)
                ModelValueBullet("XGBoost Predictive Estimator (Grad Residual)", res.modelXgBoost)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detailed Report Section
        Text("Strategic Investment Directive", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraNavy)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VyntraSurface),
            border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(VyntraPrimaryLevel.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = "", tint = VyntraPrimaryLevel, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vyntra Predictive AI Agent Report",
                        color = VyntraPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = res.recommendation,
                    fontSize = 13.sp,
                    color = VyntraNavy,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Key Success Factors vs Areas for Improvement
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = VyntraSurface),
                border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🟢 Key Strengths", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorSuccess)
                    HorizontalDivider(color = ColorNeutralGrey.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                    res.keySuccessFactors.split(",").forEach { factor ->
                        Text(
                            text = "• $factor",
                            fontSize = 11.sp,
                            color = VyntraNavy,
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = VyntraSurface),
                border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🟠 Critical Risks", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorWarning)
                    HorizontalDivider(color = ColorNeutralGrey.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                    res.areasForImprovement.split(",").forEach { item ->
                        Text(
                            text = "• $item",
                            fontSize = 11.sp,
                            color = VyntraNavy,
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Download Report Simulation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VyntraNavy),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Export Comprehensive Memorandum", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Text("Includes analytical plots, weights, coefficient indexes, and VC recommendations.", fontSize = 11.sp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportSimulationButton("Export PDF", Icons.Default.PictureAsPdf, res.startupName, modifier = Modifier.weight(1f))
                    ReportSimulationButton("CSV", Icons.Default.TableChart, res.startupName, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun ModelValueBullet(name: String, value: Double) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, fontSize = 12.sp, color = ColorNeutralGrey)
            Text(
                text = "${value.toInt()}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = when {
                    value >= 80.0 -> ColorSuccess
                    value >= 60.0 -> ColorWarning
                    else -> ColorDanger
                }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.LightGray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((value.toFloat() / 100f))
                    .background(
                        when {
                            value >= 80.0 -> ColorSuccess
                            value >= 60.0 -> ColorWarning
                            else -> ColorDanger
                        }
                    )
            )
        }
    }
}

@Composable
fun ReportSimulationButton(label: String, icon: ImageVector, startup: String, modifier: Modifier = Modifier) {
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(isDownloading) {
        if (isDownloading) {
            downloadProgress = 0f
            while (downloadProgress < 1f) {
                delay(120)
                downloadProgress += 0.1f
            }
            isDownloading = false
            Toast.makeText(context, "Completed export! Saved Vyntra_Rep_${startup}_${label.substringAfter(" ")}.txt to Downloads folder.", Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = modifier) {
        if (isDownloading) {
            Column {
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    color = VyntraTealAccent,
                    trackColor = Color.DarkGray,
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Assembling asset... ${(downloadProgress * 100).toInt()}%", color = Color.LightGray, fontSize = 9.sp)
            }
        } else {
            Button(
                onClick = { isDownloading = true },
                colors = ButtonDefaults.buttonColors(containerColor = VyntraSecondary),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(36.dp)
            ) {
                Icon(icon, contentDescription = "", modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// 8. Analytics Dashboard (Visual charts)
// -------------------------------------------------------------
@Composable
fun AnalyticsDashboardScreen(viewModel: VyntraViewModel) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text("Statistical Diagnostics", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VyntraPrimaryLevel)
        Text("Intelligence Charts", fontSize = 22.sp, fontWeight = FontWeight.Black, color = VyntraNavy)
        Spacer(modifier = Modifier.height(16.dp))

        // Chart 1: Feature Importance Bar Chart (Horizontal bars)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VyntraSurface),
            border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Feature Importance Matrix (R caret::varImp)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraNavy)
                Text("Variables impacting combined success estimations", fontSize = 11.sp, color = ColorNeutralGrey)
                Spacer(modifier = Modifier.height(16.dp))

                val importances = VyntraEngine.getFeatureImportances()
                importances.forEach { (name, weight) ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VyntraNavy)
                            Text("${weight.toInt()}%", fontSize = 10.sp, color = ColorNeutralGrey, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color.LightGray.copy(alpha = 0.3f))) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((weight.toFloat() / 100f))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(VyntraPrimaryDark, VyntraSecondary)
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 2: Correlation Matrix Heatmap
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VyntraSurface),
            border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Feature Correlation Matrix (R stats::cor)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraNavy)
                Text("Cross correlation coefficients of startup parameters", fontSize = 11.sp, color = ColorNeutralGrey)
                Spacer(modifier = Modifier.height(16.dp))

                // Custom Canvas correlation heat map
                val correlations = VyntraEngine.getCorrelationMatrix()
                val columns = listOf("Fund", "Grow", "Team", "TAM", "Exp")

                // draw a nice text row for column names
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Spacer(modifier = Modifier.width(50.dp))
                    columns.forEach { col ->
                        Text(col, modifier = Modifier.weight(1f), fontSize = 10.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = VyntraNavy)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                for (r in columns.indices) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(columns[r], modifier = Modifier.width(50.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VyntraNavy)
                        
                        for (c in columns.indices) {
                            val triple = correlations.first { it.first[0] == columns[r][0] && it.second[0] == columns[c][0] }
                            val factor = triple.third
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.3f)
                                    .padding(1.dp)
                                    .background(VyntraPrimaryLevel.copy(alpha = factor.toFloat()))
                                    .border(1.dp, Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%.2f", factor),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (factor > 0.5) Color.White else VyntraNavy
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 3: Growth Scatter Analytics
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VyntraSurface),
            border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Revenue growth vs Funding Volume Curve", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraNavy)
                Text("Simulates R ggplot2 scatter geom fitting", fontSize = 11.sp, color = ColorNeutralGrey)
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasSize = size
                        
                        // Draw grid lines
                        for (i in 0..5) {
                            val ratioY = (i / 5f)
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.7f),
                                start = Offset(0f, canvasSize.height * ratioY),
                                end = Offset(canvasSize.width, canvasSize.height * ratioY),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Draw scatter points representing famous startups
                        val points = listOf(
                            Offset(0.1f, 0.9f), // OpenAI
                            Offset(0.4f, 0.5f), // Stripe
                            Offset(0.6f, 0.4f), // Swiggy
                            Offset(0.7f, 0.42f), // Zomato
                            Offset(0.9f, 0.35f), // Canva
                            Offset(0.95f, 0.2f) // Zerodha (highly profitable, no funding)
                        )

                        points.forEach { pt ->
                            drawCircle(
                                color = VyntraTealAccent,
                                radius = 6.dp.toPx(),
                                center = Offset(pt.x * canvasSize.width, (1f - pt.y) * canvasSize.height)
                            )
                        }

                        // Fit spline curve
                        val splinePoints = mutableListOf<Offset>()
                        for (xInt in 0..100) {
                            val fraction = xInt / 100f
                            // curve: y = a + b * ln(x+c)
                            val modelY = 0.2f + 0.65f * fraction * fraction
                            splinePoints.add(Offset(fraction * canvasSize.width, (1f - modelY) * canvasSize.height))
                        }

                        for (p in 0 until splinePoints.size - 1) {
                            drawLine(
                                color = VyntraPrimaryDark,
                                start = splinePoints[p],
                                end = splinePoints[p + 1],
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

// -------------------------------------------------------------
// 9. Reports Section (History logs)
// -------------------------------------------------------------
@Composable
fun ReportsHistoryScreen(viewModel: VyntraViewModel) {
    val allPredictions by viewModel.allPredictions.collectAsStateWithLifecycle()
    var riskFilter by remember { mutableStateOf("All") } // "All", "Low Risk", "Medium Risk", "High Risk"

    val filteredList = if (riskFilter == "All") {
        allPredictions
    } else {
        allPredictions.filter { it.riskLevel.equals(riskFilter, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Evaluations Repository", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VyntraPrimaryLevel)
        Text("Report History", fontSize = 22.sp, fontWeight = FontWeight.Black, color = VyntraNavy)
        Spacer(modifier = Modifier.height(12.dp))

        // Advanced filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filters = listOf("All", "Low Risk", "Medium Risk", "High Risk")
            filters.forEach { flt ->
                FilterChip(
                    selected = riskFilter == flt,
                    onClick = { riskFilter = flt },
                    label = { Text(flt, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredList.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = VyntraSurface),
                border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.FilterListOff, contentDescription = "", tint = ColorNeutralGrey, modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No reports matching path", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Adjust your active risk filter queries or execute a new simulation run first.", textAlign = TextAlign.Center, fontSize = 12.sp, color = ColorNeutralGrey)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList) { record ->
                    Card(
                        onClick = { viewModel.selectHistoryItem(record) },
                        colors = CardDefaults.cardColors(containerColor = VyntraSurface),
                        border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VyntraPrimaryDark.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Analytics, contentDescription = "", tint = VyntraPrimaryDark, modifier = Modifier.size(24.dp))
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(record.startupName, fontWeight = FontWeight.Black, fontSize = 15.sp, color = VyntraNavy)
                                Text("${record.industry} | Score: ${record.successProbability.toInt()}%", fontSize = 12.sp, color = ColorNeutralGrey)
                            }

                            // Risk rating colored badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when {
                                            record.successProbability >= 80.0 -> ColorSuccess.copy(alpha = 0.15f)
                                            record.successProbability >= 60.0 -> ColorWarning.copy(alpha = 0.15f)
                                            else -> ColorDanger.copy(alpha = 0.15f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = record.riskLevel.uppercase(),
                                    color = when {
                                        record.successProbability >= 80.0 -> ColorSuccess
                                        record.successProbability >= 60.0 -> ColorWarning
                                        else -> ColorDanger
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 10. User Profile Section
// -------------------------------------------------------------
@Composable
fun UserProfileScreen(viewModel: VyntraViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allPredictions by viewModel.allPredictions.collectAsStateWithLifecycle()

    if (currentUser == null) return

    val myEvaluations = allPredictions.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(VyntraPrimaryDark),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentUser?.fullName?.take(2)?.uppercase() ?: "US",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(currentUser?.fullName ?: "Senior Venture Analyst", fontWeight = FontWeight.Black, fontSize = 20.sp, color = VyntraNavy)
        Text(currentUser?.email ?: "analyst@vyntra.io", fontSize = 13.sp, color = ColorNeutralGrey)

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(VyntraTealAccent.copy(alpha = 0.15f))
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            Text(currentUser?.role?.uppercase() ?: "RESEARCHER", fontWeight = FontWeight.Bold, color = VyntraTealAccent, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Professional Statistics Grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VyntraSurface),
            border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Analyst Activity Indicators", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraNavy)
                Spacer(modifier = Modifier.height(12.dp))

                ProfileStatBullet("Total Evaluations Executed", "$myEvaluations runs")
                ProfileStatBullet("Academic Database Vintage", "Ver 1.0 (SQLite)")
                ProfileStatBullet("Combined Model Approvals", "${(myEvaluations * 0.73).toInt()} Recommended")
                ProfileStatBullet("Account ID Token", "#${currentUser?.id}")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.logoutUser() },
            colors = ButtonDefaults.buttonColors(containerColor = ColorDanger),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillModifier()
                .height(48.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout Workspace", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileStatBullet(label: String, valIn: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = ColorNeutralGrey)
            Text(valIn, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VyntraNavy)
        }
        HorizontalDivider(color = ColorNeutralGrey.copy(alpha = 0.1f))
    }
}

// -------------------------------------------------------------
// 11. Admin Dashboard
// -------------------------------------------------------------
@Composable
fun AdminDashboardScreen(viewModel: VyntraViewModel) {
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val predictionCount by viewModel.predictionCount.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text("Strategic Resource Panel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorWarning)
        Text("Admin System Console", fontSize = 22.sp, fontWeight = FontWeight.Black, color = VyntraNavy)
        Spacer(modifier = Modifier.height(16.dp))

        // System Health Metrics
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VyntraNavy)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Predictive Platform Server Diagnostics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("API STATE", fontSize = 9.sp, color = Color.LightGray)
                        Text("ONLINE", fontWeight = FontWeight.Bold, color = ColorSuccess, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("LATENCY", fontSize = 9.sp, color = Color.LightGray)
                        Text("34ms", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TRAINING COEFF", fontSize = 9.sp, color = Color.LightGray)
                        Text("STABLE", fontWeight = FontWeight.Bold, color = VyntraTealAccent, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Accounts Control Panel
        Text("Registered Business Accounts", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraNavy)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VyntraSurface),
            border = BorderStroke(1.dp, ColorNeutralGrey.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                allUsers.forEach { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VyntraNavy)
                            Text("${user.username} | ${user.email}", fontSize = 11.sp, color = ColorNeutralGrey)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (user.role == "Admin") ColorWarning.copy(alpha = 0.15f)
                                    else VyntraSecondary.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = user.role,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (user.role == "Admin") ColorWarning else VyntraSecondary
                            )
                        }
                    }
                    HorizontalDivider(color = ColorNeutralGrey.copy(alpha = 0.08f))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SQLite Control Database Division
        Text("Local Storage Controls", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyntraNavy)
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.clearAllHistory()
                Toast.makeText(context, "Full SQLite database prediction logs dropped.", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = ColorDanger),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillModifier()
                .height(48.dp)
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = "", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Wipe Predictions Database Log", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

// -------------------------------------------------------------
// Format Utilities
// -------------------------------------------------------------
fun formatAmountInBillions(amt: Double): String {
    return if (amt >= 1e9) {
        String.format("$%.1f B", amt / 1e9)
    } else if (amt >= 1e6) {
        String.format("$%.1f M", amt / 1e6)
    } else {
        String.format("$%.0f", amt)
    }
}

fun formatFunding(funding: Double): String {
    return if (funding >= 1_000_000_000) {
        String.format("$%.1f Billion", funding / 1_000_000_000)
    } else if (funding >= 1_000_000) {
        String.format("$%.1f Million", funding / 1_000_000)
    } else if (funding == 0.0) {
        "Bootstrapped"
    } else {
        String.format("$%.0f", funding)
    }
}

// Standard short name padding modifiers
fun Modifier.fillModifier() = this.fillMaxWidth()
fun Modifier.pyStep(py: Int) = this.padding(vertical = py.dp)
