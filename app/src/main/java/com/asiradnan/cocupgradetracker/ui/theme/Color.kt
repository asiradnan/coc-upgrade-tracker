package com.asiradnan.cocupgradetracker.ui.theme

import androidx.compose.ui.graphics.Color

// Clash of Clans Settings Window Palette
// Sampled directly from the in-game Settings screen:
// Warm light stone-grey window canvas + glossy 3D lime buttons + black-outlined white labels

// Window Canvas & Panel Surfaces (CoC Settings window)
val MenuBackground = Color(0xFFE6E5E0)
val MenuSurfaceLow = Color(0xFFF4F3EF)
val MenuSurface = Color(0xFFECEBE6)
val MenuSurfaceVariant = Color(0xFFDCDAD2)
val MenuSurfaceHighest = Color(0xFFCBC8BF)

// Legacy aliases kept for compatibility across screens
val SlateBackground = MenuBackground
val SlateSurfaceLowest = Color(0xFFD5D3CB)
val OnSlateBackground = Color(0xFF24211D)

// Text & Borders on Light Grey Panels (e.g. "Consent Choices" caption & etched lines)
val MenuText = Color(0xFF24211D)
val MenuCaptionText = Color(0xFF605D57)
val MenuOutline = Color(0xFF6B6760)
val MenuDivider = Color(0xFFBDBBB4)
val MenuDividerHighlight = Color(0xFFF7F6F2)

// Thick near-black outline used around Clash buttons, bars, and outlined text
val ClashInkOutline = Color(0xFF171512)

// ---- Bevel Button tokens (top gloss / upper body / lower body / 3D bottom lip) ----
// Lime green, sampled directly from the CoC Settings buttons ("English", "More Settings")
val ButtonLimeGlossTop = Color(0xFFC8F576)
val ButtonLimeGlossBottom = Color(0xFF9BE048)
val ButtonLimeTop = Color(0xFF8CD938)
val ButtonLimeBottom = Color(0xFF5EB31E)
val ButtonLimeLip = Color(0xFF3F7F16)
val ButtonLimeGloss = ButtonLimeGlossTop

// Candy blue (navigation / info)
val ButtonBlueGlossTop = Color(0xFFA8DDFF)
val ButtonBlueGlossBottom = Color(0xFF6CC4FF)
val ButtonBlueTop = Color(0xFF52B4FF)
val ButtonBlueBottom = Color(0xFF2682DE)
val ButtonBlueLip = Color(0xFF165294)
val ButtonBlueGloss = ButtonBlueGlossTop

// Candy gold (primary highlight)
val ButtonGoldGlossTop = Color(0xFFFFEC9E)
val ButtonGoldGlossBottom = Color(0xFFFFD95A)
val ButtonGoldTop = Color(0xFFFFCC33)
val ButtonGoldBottom = Color(0xFFE59412)
val ButtonGoldLip = Color(0xFF9C5E04)
val ButtonGoldGloss = ButtonGoldGlossTop

// Candy red (destructive)
val ButtonRedGlossTop = Color(0xFFFFB5AB)
val ButtonRedGlossBottom = Color(0xFFFF7A66)
val ButtonRedTop = Color(0xFFF55A47)
val ButtonRedBottom = Color(0xFFD42C1E)
val ButtonRedLip = Color(0xFF87140B)
val ButtonRedGloss = ButtonRedGlossTop

// Neutral light stone (cancel / secondary)
val ButtonNeutralGlossTop = Color(0xFFFFFFFF)
val ButtonNeutralGlossBottom = Color(0xFFF2F0EA)
val ButtonNeutralTop = Color(0xFFE8E6DE)
val ButtonNeutralBottom = Color(0xFFC9C6BC)
val ButtonNeutralLip = Color(0xFF8F8C82)
val ButtonNeutralGloss = ButtonNeutralGlossTop
val OnButtonNeutral = Color(0xFF2B2722)

// Disabled (CoC grey)
val ButtonDisabledTop = Color(0xFFB4B2AB)
val ButtonDisabledBottom = Color(0xFF96938C)
val ButtonDisabledLip = Color(0xFF6A6862)
val OnButtonDisabled = Color(0xD9FFFFFF)

// ---- Panel header bands ----
val PanelHeaderStone = Color(0xFF575045)
val PanelHeaderTownHall = Color(0xFF5C5242)
val PanelHeaderBuilderBase = Color(0xFF3B4D66)
val PanelHeaderDanger = Color(0xFF7D241C)

// ---- Slider knob (white round knob with ink outline) ----
val SliderKnobTop = Color(0xFFFAFAFA)
val SliderKnobBottom = Color(0xFFD6D6D6)

// Clash Gold / Amber (Builders & Primary Highlights)
val ClashGold = Color(0xFFD98A0B)
val ClashGoldBright = Color(0xFFFFC933)
val OnClashGold = Color(0xFF261600)
val ClashGoldContainer = Color(0xFFFFE09E)
val OnClashGoldContainer = Color(0xFF593500)

// Clash Elixir Magenta-Purple (Laboratory & Research)
val ElixirPurple = Color(0xFFB8338A)
val OnElixirPurple = Color(0xFFFFFFFF)
val ElixirContainer = Color(0xFFF7D4EB)
val OnElixirContainer = Color(0xFF59103F)

// Clash Action Green (Free Slots, Positive Actions & Goblin)
val ClashGreen = Color(0xFF5EB31E)
val ClashGreenBright = Color(0xFF84E038)
val OnClashGreen = Color(0xFFFFFFFF)
val ClashGreenContainer = Color(0xFFD3F5BD)
val OnClashGreenContainer = Color(0xFF1A4F03)

// Clash Sky Blue (Navigation & Info Actions)
val ClashBlue = Color(0xFF2E8AE6)
val ClashBlueBright = Color(0xFF4DA6FF)
val OnClashBlue = Color(0xFFFFFFFF)

// Clash Crimson Red (Destructive & Free Alert Text)
val ClashRed = Color(0xFFFF5242)
val OnClashRed = Color(0xFFFFFFFF)
val ClashRedContainer = Color(0xFFFCDAD7)
val OnClashRedContainer = Color(0xFF6E1109)
