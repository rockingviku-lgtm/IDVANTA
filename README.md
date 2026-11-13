# IDVANTA — Professional Document Creator

**Tagline:** *Your Identity. Your Documents. Your Style.*  
**Developer Credit:** Made by Unity  
**Developed by:** Seven Ink Digital Developers Team (Made in India 🇮🇳)  
**Target Audience:** For School Teachers, Students and Working Professionals.  
**Support Email:** `seveninkdigitaldevelopers@gmail.com`

## Overview

IDVANTA is a native Android application built with **Kotlin**, **Jetpack Compose**, **Material 3**, **MVVM + Clean Architecture**, **Room Database**, **Hilt**, **DataStore**, **Google AdMob (`play-services-ads`)**, and **Google User Messaging Platform (`user-messaging-platform`)**.

Document creation, draft storage, photo embedding, and PDF rendering run locally on your device without requiring an account, while banner advertising and UMP consent management require internet connectivity.

## Core Modules

1. **Minimalist Splash & Studio Home Screen**: Clean startup screen (`IDVANTA` + developer emblem + `Made by Unity`) transitioning to a responsive dashboard (`NavigationBar` on phones, `NavigationRail` on wide tablets/foldables) with full, unwrapped module tiles.
2. **CV & Resume Maker (20 Distinct Templates)**: Form-driven studio supporting Student CV, Fresher CV, Professional Resume, Experienced Resume, Academic CV, Minimal Resume, and ATS-Friendly Resume with live preview, section reordering, custom headings, font/color customization, Undo/Redo, and Room autosave.
3. **ID Card Makers (15 Distinct Templates)**: Dedicated School, College, Employee, and Teacher ID card studios featuring standard CR80 dimensions (`85.6 × 53.98 mm`), Front & Back card layouts, ZXing QR code generation, institution logo upload, and an Institution-Authorized toggle (demo cards without institutional authorization include a visible `SAMPLE` watermark).
4. **Personal Biodata Maker (6 Layouts)**: Bilingual English & Hindi (`Noto Sans Devanagari`) personal and family dossier generator.
5. **Bulk Student ID Generator**: Manual entry, RFC-4180 CSV import (with built-in sample CSV), duplicate detection, batch selection, and multi-page A4 print-ready CR80 sheet generation (8 cards per page + back sheets).
6. **PDF Engine & Print/Share Pipeline**: Native `android.graphics.pdf.PdfDocument` + `StaticLayout` Unicode/Devanagari rendering, in-app `PdfRenderer` preview, Storage Access Framework (`CreateDocument`) Save As, `FileProvider` Share, and Android `PrintManager` printing.
7. **Legal, Privacy & Consent Controls**: Settings → **Legal & Information** provides Privacy Policy, Disclaimer, Terms and Conditions, Cookies & Identifiers disclosure, About Us, Contact Us (`mailto:seveninkdigitaldevelopers@gmail.com`), and UMP **Privacy Options** (when required by region).

## Advertising & Test vs Release Configuration

- **Debug / Test Builds (`assembleDebug`)**: Configured with Google's official sample AdMob App ID (`ca-app-pub-3940256099942544~3347511713`) and sample Anchored Adaptive Banner Ad Unit ID (`ca-app-pub-3940256099942544/9214589741`) with `BuildConfig.IS_TEST_AD_CONFIG = true`.
- **Release Builds (`assembleRelease`)**: Configured via `ADMOB_APP_ID` and `ADMOB_BANNER_AD_UNIT_ID` environment variables (`BuildConfig.IS_TEST_AD_CONFIG = false`); test ad unit IDs are never bundled into release builds.

## Build & Test Commands

- Debug / Test APK: `gradle :app:assembleDebug` (Output: `app/build/outputs/apk/debug/app-debug.apk`)
- Release APK (requires signing & AdMob env vars `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`, `ADMOB_APP_ID`, `ADMOB_BANNER_AD_UNIT_ID`): `gradle :app:assembleRelease`
- Unit & Robolectric Tests: `gradle :app:testDebugUnitTest`
