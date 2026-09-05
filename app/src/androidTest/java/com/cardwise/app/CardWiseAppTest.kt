package com.cardwise.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cardwise.app.ui.CardWiseApp
import com.cardwise.app.ui.theme.CardWiseTheme
import org.junit.Rule
import org.junit.Test

class CardWiseAppTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreenShowsCoreMessage() {
        composeTestRule.setContent {
            CardWiseTheme {
                CardWiseApp()
            }
        }

        composeTestRule.onNodeWithText("Pay smarter with CardWise.").assertIsDisplayed()
    }
}
