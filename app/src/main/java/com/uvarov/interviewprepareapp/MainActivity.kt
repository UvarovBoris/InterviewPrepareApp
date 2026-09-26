package com.uvarov.interviewprepareapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.uvarov.interviewprepareapp.ui.questions.QuestionsScreen
import com.uvarov.interviewprepareapp.ui.theme.InterviewPrepareAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InterviewPrepareAppTheme {
                QuestionsScreen()
            }
        }
    }
}
