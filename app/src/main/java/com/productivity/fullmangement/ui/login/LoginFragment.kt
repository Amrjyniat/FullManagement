package com.productivity.fullmangement.ui.login

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.productivity.fullmangement.R
import com.productivity.fullmangement.ui.theme.FacebookBlue
import com.productivity.fullmangement.ui.theme.FullMangementComposeTheme
import com.productivity.fullmangement.ui.theme.GoogleLightRed
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.login_fragment) {

    private val viewModel: LoginActivityViewModel by viewModels()

    @ExperimentalMaterialApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ComposeView>(R.id.composeView).apply {
            // Dispose the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FullMangementComposeTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(color = MaterialTheme.colors.background) {
                        Container()
                    }
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun Container() {
        Box {
            Image(
                painter = painterResource(id = R.drawable.bc_sign_up_page),
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.2f),
                contentScale = ContentScale.Crop,
                contentDescription = "background"
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.appicon_log_in_page),
                    modifier = Modifier
                        .padding(top = 100.dp)
                        .size(150.dp),
                    contentDescription = "App Icon"
                )
                Text(
                    text = stringResource(R.string.organize_your_life_smartly),
                    modifier = Modifier.background(color = Color.White).padding(top = 10.dp),
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.W600
                )
                Column(
                    modifier = Modifier
                        .padding(top = 100.dp)
                        .fillMaxWidth()
                        .padding(start = 30.dp, end = 30.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    SignupButton(
                        iconResId = R.drawable.face_icon_btn_sign_up,
                        textButton = stringResource(id = R.string.continue_with_facebook),
                        FacebookBlue
                    )
                    SignupButton(
                        iconResId = R.drawable.ic_google_btn_sign_up,
                        textButton = stringResource(id = R.string.continue_with_google),
                        GoogleLightRed
                    )
                    SignupButton(
                        iconResId = R.drawable.ic_email_btn_sign_up,
                        textButton = stringResource(id = R.string.continue_with_email),
                        Color.White
                    )
                }

            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun SignupButton(@DrawableRes iconResId: Int, textButton: String, color: Color) {
        Card(
            backgroundColor = color,
            elevation = 0.dp,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (color == Color.White) Color.Gray else Color.Transparent,
            ),
            onClick = {},

            ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    tint = Color.Unspecified,
                    modifier = Modifier.padding(start = 4.dp),
                    contentDescription = "Facebook",
                )
                Text(
                    text = textButton,
                    color = if (color == Color.White) Color.Black else Color.White,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.body1,
                    fontSize = 18.sp
                )
            }
        }

    }

    @ExperimentalMaterialApi
    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        FullMangementComposeTheme {
            Container()
        }
    }


}